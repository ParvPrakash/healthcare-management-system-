const puppeteer = require('puppeteer-core');
const fs = require('fs');
const path = require('path');

const BASE_URL = 'http://localhost:8080';
const SCREENSHOT_DIR = path.join(__dirname, 'docs', 'screenshots');

if (!fs.existsSync(SCREENSHOT_DIR)) {
  fs.mkdirSync(SCREENSHOT_DIR, { recursive: true });
}

const VIEWPORTS = {
  desktop: { width: 1280, height: 800, deviceScaleFactor: 2 },
  tablet: { width: 768, height: 1024, deviceScaleFactor: 2 },
  mobile: { width: 375, height: 812, isMobile: true, hasTouch: true, deviceScaleFactor: 2 }
};

async function checkHorizontalScroll(page, pageName, vpName) {
  const scrollWidth = await page.evaluate(() => document.documentElement.scrollWidth);
  const innerWidth = await page.evaluate(() => window.innerWidth);
  const hasOverflow = scrollWidth > innerWidth;
  if (hasOverflow) {
    console.warn(`[WARN] Horizontal scroll detected on ${pageName} (${vpName}): scrollWidth=${scrollWidth}, innerWidth=${innerWidth}`);
  } else {
    console.log(`[PASS] Responsive check OK for ${pageName} (${vpName}): width=${innerWidth}px, scrollWidth=${scrollWidth}px`);
  }
}

async function loginAndCapture(browser, email, password, targetUrl, roleName) {
  const context = await browser.createBrowserContext();
  const page = await context.newPage();

  // Desktop capture
  await page.setViewport(VIEWPORTS.desktop);
  await page.goto(`${BASE_URL}/login`, { waitUntil: 'networkidle0' });
  await page.waitForSelector('#loginEmail');
  await page.type('#loginEmail', email);
  await page.type('#loginPassword', password);
  await Promise.all([
    page.waitForNavigation({ waitUntil: 'networkidle0' }),
    page.click('button[type="submit"]')
  ]);

  await page.goto(targetUrl, { waitUntil: 'networkidle0' });
  await new Promise(r => setTimeout(r, 600)); // Allow counter animations & transitions
  await checkHorizontalScroll(page, `${roleName}-Dashboard`, 'desktop');
  await page.screenshot({ path: path.join(SCREENSHOT_DIR, `${roleName.toLowerCase()}-dashboard-desktop.png`) });

  // Mobile capture
  await page.setViewport(VIEWPORTS.mobile);
  await page.reload({ waitUntil: 'networkidle0' });
  await new Promise(r => setTimeout(r, 600));
  await checkHorizontalScroll(page, `${roleName}-Dashboard`, 'mobile');
  await page.screenshot({ path: path.join(SCREENSHOT_DIR, `${roleName.toLowerCase()}-dashboard-mobile.png`) });

  await context.close();
}

(async () => {
  const browser = await puppeteer.launch({
    executablePath: '/Applications/Brave Browser.app/Contents/MacOS/Brave Browser',
    headless: 'new',
    args: ['--no-sandbox', '--disable-setuid-sandbox']
  });

  const page = await browser.newPage();

  console.log('--- 1. Capturing Landing Page ---');
  await page.setViewport(VIEWPORTS.desktop);
  await page.goto(`${BASE_URL}/`, { waitUntil: 'networkidle0' });
  await checkHorizontalScroll(page, 'Landing', 'desktop');
  await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'landing-desktop.png') });

  await page.setViewport(VIEWPORTS.tablet);
  await checkHorizontalScroll(page, 'Landing', 'tablet');
  await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'landing-tablet.png') });

  await page.setViewport(VIEWPORTS.mobile);
  await checkHorizontalScroll(page, 'Landing', 'mobile');
  await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'landing-mobile.png') });

  console.log('--- 2. Capturing Login Page ---');
  await page.setViewport(VIEWPORTS.desktop);
  await page.goto(`${BASE_URL}/login`, { waitUntil: 'networkidle0' });
  await checkHorizontalScroll(page, 'Login', 'desktop');
  await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'login-desktop.png') });

  await page.setViewport(VIEWPORTS.mobile);
  await checkHorizontalScroll(page, 'Login', 'mobile');
  await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'login-mobile.png') });

  console.log('--- 3. Capturing Register Page ---');
  await page.setViewport(VIEWPORTS.desktop);
  await page.goto(`${BASE_URL}/register`, { waitUntil: 'networkidle0' });
  await checkHorizontalScroll(page, 'Register', 'desktop');
  await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'register-desktop.png') });

  await page.setViewport(VIEWPORTS.mobile);
  await checkHorizontalScroll(page, 'Register', 'mobile');
  await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'register-mobile.png') });

  console.log('--- 4. Capturing DB Test Diagnostic Page ---');
  await page.setViewport(VIEWPORTS.desktop);
  await page.goto(`${BASE_URL}/db-test`, { waitUntil: 'networkidle0' });
  await checkHorizontalScroll(page, 'DB-Test', 'desktop');
  await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'db-test-desktop.png') });

  await page.setViewport(VIEWPORTS.mobile);
  await checkHorizontalScroll(page, 'DB-Test', 'mobile');
  await page.screenshot({ path: path.join(SCREENSHOT_DIR, 'db-test-mobile.png') });

  await page.close();

  console.log('--- 5. Capturing Admin Dashboard ---');
  await loginAndCapture(browser, 'admin@carefirst.com', 'Admin@123', `${BASE_URL}/admin/dashboard`, 'admin');

  console.log('--- 6. Capturing Doctor Dashboard ---');
  await loginAndCapture(browser, 'doctor.sarah@carefirst.com', 'Doctor@123', `${BASE_URL}/doctor/dashboard`, 'doctor');

  console.log('--- 7. Capturing Patient Dashboard ---');
  await loginAndCapture(browser, 'john.doe@example.com', 'Patient@123', `${BASE_URL}/patient/dashboard`, 'patient');

  console.log('--- All screenshots captured successfully! ---');
  await browser.close();
})().catch(err => {
  console.error('Screenshot capture failed:', err);
  process.exit(1);
});
