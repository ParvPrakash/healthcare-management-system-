const puppeteer = require('puppeteer-core');
const fs = require('fs');
const path = require('path');

const useCaseMermaid = `flowchart LR
    subgraph Actors ["System Actors"]
        Admin(("👑 Administrator"))
        Doctor(("🩺 Doctor"))
        Patient(("👤 Patient"))
    end

    subgraph AdminCases ["Admin Capabilities"]
        A1["Manage Users & Doctors"]
        A2["View Clinic Analytics & KPIs"]
        A3["Inspect Audit Security Logs"]
        A4["System Settings & Demo Reset"]
        A5["JDBC Connectivity Diagnostics"]
    end

    subgraph DoctorCases ["Doctor Capabilities"]
        D1["Manage Weekly Practice Schedule"]
        D2["Review Appointment Consult Queue"]
        D3["Sign EHR Medical Records & Rx"]
        D4["Inspect Patient Star Feedback"]
    end

    subgraph PatientCases ["Patient Capabilities"]
        P1["Self-Service Registration"]
        P2["Search Specialist Catalog"]
        P3["Live Slot Booking (Concurrency Locked)"]
        P4["View Prescriptions & EHR Records"]
        P5["Submit Doctor Star Reviews"]
    end

    Admin --> A1
    Admin --> A2
    Admin --> A3
    Admin --> A4
    Admin --> A5

    Doctor --> D1
    Doctor --> D2
    Doctor --> D3
    Doctor --> D4

    Patient --> P1
    Patient --> P2
    Patient --> P3
    Patient --> P4
    Patient --> P5
`;

const html = `<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <script src="https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.min.js"></script>
  <style>
    body {
      margin: 0;
      padding: 30px;
      background: #FFFBF8;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
      display: flex;
      flex-direction: column;
      align-items: center;
    }
    .header {
      text-align: center;
      margin-bottom: 20px;
    }
    h1 {
      color: #3A2B25;
      font-size: 24px;
      margin: 0 0 6px 0;
    }
    p {
      color: #8A7268;
      margin: 0;
      font-size: 13px;
    }
    #diagram {
      background: #FFFFFF;
      padding: 24px;
      border-radius: 16px;
      box-shadow: 0 8px 24px rgba(165, 83, 58, 0.08);
      border: 1px solid #F3E3D8;
    }
  </style>
</head>
<body>
  <div class="header">
    <h1>System Use-Case Diagram — Multi-Role Actor Interactions</h1>
    <p>Role-Based Access Control: Administrator, Doctor, and Patient Workflows</p>
  </div>
  <div id="diagram">
    <pre class="mermaid">${useCaseMermaid}</pre>
  </div>
  <script>
    mermaid.initialize({
      startOnLoad: true,
      theme: 'neutral',
      themeVariables: {
        primaryColor: '#FFF5EE',
        primaryTextColor: '#3A2B25',
        primaryBorderColor: '#E8906B',
        lineColor: '#C96F4A',
        secondaryColor: '#FFFBF8',
        tertiaryColor: '#FFFFFF'
      }
    });
  </script>
</body>
</html>`;

(async () => {
  const browser = await puppeteer.launch({
    executablePath: '/Applications/Brave Browser.app/Contents/MacOS/Brave Browser',
    headless: 'new',
    args: ['--no-sandbox', '--disable-setuid-sandbox']
  });

  const page = await browser.newPage();
  await page.setViewport({ width: 1400, height: 1100, deviceScaleFactor: 2 });
  await page.setContent(html, { waitUntil: 'load', timeout: 30000 });
  await page.waitForSelector('#diagram svg', { timeout: 15000 });

  const ucEl = await page.$('#diagram');
  const targetPath = path.join(__dirname, 'docs', 'use-case-diagram.png');
  await ucEl.screenshot({ path: targetPath });
  console.log('Generated docs/use-case-diagram.png');

  await browser.close();
})().catch(err => {
  console.error(err);
  process.exit(1);
});
