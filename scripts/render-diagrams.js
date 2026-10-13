const puppeteer = require('puppeteer-core');
const fs = require('fs');
const path = require('path');

const archMermaid = `flowchart TD
    Client["🌐 Web Browser (Desktop / Mobile Client)"] -->|HTTPS / HTTP| FilterChain["🛡️ Servlet Filter Interceptor Chain"]
    
    subgraph Filters ["Filter Layer (Cross-Cutting Concerns)"]
        FilterChain --> F1["EncodingFilter (UTF-8)"]
        F1 --> F2["LoggingFilter (Metrics/SLF4J)"]
        F2 --> F3["TunnelSecurityFilter (SSL/Headers)"]
        F3 --> F4["AuthFilter (Session Check)"]
        F4 --> F5["RoleFilter (RBAC Enforcer)"]
    end
    
    F5 -->|Authorized Request| Servlets
    
    subgraph Controllers ["Presentation Layer (Jakarta Servlet 6.0)"]
        Servlets["BaseServlet Controllers\\n(Auth, Dashboard, User, Appointment, DbTest)"]
    end
    
    Servlets -->|Business Invocations| Services
    Servlets -->|Forward HTML Request| JSP["🎨 View Layer (JSP 3.1 + JSTL + CSS Tokens)"]
    
    subgraph Business ["Service Layer (Transactions & Coordination)"]
        Services["Business Services & Cache\\n(AuthService, AppointmentService, DoctorCache)"]
        Services <--> Concurrency["⚡ Concurrency Managers\\n(SlotLockManager, ReminderScheduler)"]
    end
    
    Services -->|Data Persistence| DAOs
    
    subgraph Persistence ["Persistence Layer (JDBC & Connection Pool)"]
        DAOs["Data Access Objects (RowMapper + PreparedStatement)\\n(UserDao, AppointmentDao, RecordDao)"]
        DAOs --> Pool["🏊 HikariCP Connection Pool (Max: 20)"]
    end
    
    Pool -->|TCP / Port 3306| DB[("🗄️ MySQL 8.0+ Database (3NF Relational Schema)")]
`;

const useCaseMermaid = `flowchart LR
    subgraph Actors ["System Actors"]
        Admin(("👑 Administrator"))
        Doctor(("🩺 Doctor"))
        Patient(("👤 Patient"))
    end

    subgraph AdminCases ["Admin Capabilities"]
        A1["Manage Users & Roles"]
        A2["View Clinic Analytics & KPIs"]
        A3["Inspect Audit Security Logs"]
        A4["Configure System Settings & Reset Demo"]
        A5["Run JDBC Connectivity Diagnostics"]
    end

    subgraph DoctorCases ["Doctor Capabilities"]
        D1["Manage Weekly Practice Schedule"]
        D2["Review Appointment Consult Queue"]
        D3["Sign EHR Medical Records & Prescriptions"]
        D4["Inspect Patient Star Feedback"]
    end

    subgraph PatientCases ["Patient Capabilities"]
        P1["Register & Self-Service Login"]
        P2["Search Specialist Catalog"]
        P3["Live Slot Booking with Concurrency Locks"]
        P4["View Prescriptions & Medical History"]
        P5["Submit Doctor Ratings & Reviews"]
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

function getHtml(title, subtitle, code) {
  return `<!DOCTYPE html>
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
    <h1>${title}</h1>
    <p>${subtitle}</p>
  </div>
  <div id="diagram">
    <pre class="mermaid">${code}</pre>
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
}

(async () => {
  const browser = await puppeteer.launch({
    executablePath: '/Applications/Brave Browser.app/Contents/MacOS/Brave Browser',
    headless: 'new',
    args: ['--no-sandbox', '--disable-setuid-sandbox']
  });

  const page = await browser.newPage();
  await page.setViewport({ width: 1400, height: 1100, deviceScaleFactor: 2 });

  // 1. Architecture Diagram
  const archHtml = getHtml('System Architecture — Layered MVC & Security Filter Flow', 'Browser → Filters → Servlets → Services → DAOs → HikariCP → MySQL', archMermaid);
  await page.setContent(archHtml, { waitUntil: 'networkidle0' });
  await page.waitForSelector('#diagram svg', { timeout: 15000 });
  const archEl = await page.$('#diagram');
  await archEl.screenshot({ path: path.join(__dirname, 'docs', 'architecture-diagram.png') });
  console.log('Generated docs/architecture-diagram.png');

  // 2. Use Case Diagram
  const ucHtml = getHtml('System Use-Case Diagram — Multi-Role Actor Interactions', 'Role-Based Access Control: Administrator, Doctor, and Patient Workflows', useCaseMermaid);
  await page.setContent(ucHtml, { waitUntil: 'networkidle0' });
  await page.waitForSelector('#diagram svg', { timeout: 15000 });
  const ucEl = await page.$('#diagram');
  await ucEl.screenshot({ path: path.join(__dirname, 'docs', 'use-case-diagram.png') });
  console.log('Generated docs/use-case-diagram.png');

  await browser.close();
})().catch(err => {
  console.error(err);
  process.exit(1);
});
