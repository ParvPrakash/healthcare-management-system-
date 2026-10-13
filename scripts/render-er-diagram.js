const puppeteer = require('puppeteer-core');
const fs = require('fs');
const path = require('path');

const mermaidCode = `erDiagram
    users ||--o| doctors : "specializes as (1:1)"
    users ||--o| patients : "registered as (1:1)"
    users ||--o{ notifications : "receives (1:N)"
    users ||--o{ audit_logs : "triggers (1:N)"
    doctors ||--o{ schedules : "defines (1:N)"
    doctors ||--o{ time_slots : "allocates (1:N)"
    doctors ||--o{ appointments : "consults (1:N)"
    patients ||--o{ appointments : "books (1:N)"
    time_slots ||--o| appointments : "reserves (1:1)"
    appointments ||--o| medical_records : "generates (1:1)"
    doctors ||--o{ medical_records : "authors (1:N)"
    patients ||--o{ medical_records : "possesses (1:N)"
    appointments ||--o| feedback : "rated by (1:1)"
    doctors ||--o{ feedback : "receives (1:N)"
    patients ||--o{ feedback : "submits (1:N)"

    users {
        int id PK
        string name
        string email UK
        string password_hash
        enum role
        enum status
        int failed_attempts
        datetime locked_until
        timestamp created_at
    }
    doctors {
        int id PK,FK
        string specialization
        int experience
        decimal consultation_fee
        string room_number
        text bio
    }
    patients {
        int id PK,FK
        int age
        string gender
        string phone
        string blood_group
        text address
        string emergency_contact
    }
    schedules {
        int id PK
        int doctor_id FK
        string day_of_week
        time start_time
        time end_time
        int slot_duration_minutes
        boolean is_active
    }
    time_slots {
        int id PK
        int doctor_id FK
        date slot_date
        time start_time
        time end_time
        boolean is_booked
        int version
    }
    appointments {
        int id PK
        int doctor_id FK
        int patient_id FK
        int slot_id FK,UK
        date appointment_date
        time start_time
        time end_time
        enum status
        string reason
        text notes
    }
    medical_records {
        int id PK
        int appointment_id FK
        int patient_id FK
        int doctor_id FK
        text diagnosis
        text prescription
        string vitals_blood_pressure
        string vitals_heart_rate
        string vitals_temperature
        date record_date
    }
    feedback {
        int id PK
        int appointment_id FK,UK
        int doctor_id FK
        int patient_id FK
        int rating
        text comment
    }
    notifications {
        int id PK
        int user_id FK
        string title
        text message
        string type
        boolean is_read
    }
    system_settings {
        string setting_key PK
        text setting_value
        string description
    }
    audit_logs {
        int id PK
        int user_id FK
        string action
        text details
        string ip_address
    }
    login_attempts {
        int id PK
        string email
        string ip_address
        boolean success
        timestamp attempted_at
    }
`;

const html = `<!DOCTYPE html>
<html>
<head>
  <meta charset="utf-8">
  <script src="https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.min.js"></script>
  <style>
    body {
      margin: 0;
      padding: 40px;
      background: #FFFBF8;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif;
      display: flex;
      flex-direction: column;
      align-items: center;
    }
    .header {
      text-align: center;
      margin-bottom: 24px;
    }
    h1 {
      color: #3A2B25;
      font-size: 26px;
      margin: 0 0 6px 0;
    }
    p {
      color: #8A7268;
      margin: 0;
      font-size: 14px;
    }
    #diagram {
      background: #FFFFFF;
      padding: 30px;
      border-radius: 16px;
      box-shadow: 0 8px 30px rgba(165, 83, 58, 0.08);
      border: 1px solid #F3E3D8;
    }
  </style>
</head>
<body>
  <div class="header">
    <h1>CareFirst Healthcare System — Entity-Relationship Diagram</h1>
    <p>3NF Normalized Relational Schema with Constraints, Foreign Keys &amp; Indexes</p>
  </div>
  <div id="diagram">
    <pre class="mermaid">${mermaidCode}</pre>
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
  await page.setViewport({ width: 1920, height: 1400, deviceScaleFactor: 2 });
  await page.setContent(html, { waitUntil: 'networkidle0' });
  await page.waitForSelector('#diagram svg', { timeout: 15000 });

  const element = await page.$('#diagram');
  const outputPath = path.join(__dirname, 'docs', 'er-diagram.png');
  await element.screenshot({ path: outputPath });

  console.log('Successfully generated ER Diagram at: ' + outputPath);
  await browser.close();
})().catch(err => {
  console.error(err);
  process.exit(1);
});
