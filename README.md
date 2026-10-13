# CareFirst — Online Healthcare Management System

> Modern, full-featured Healthcare Information System built with 100% Java (Jakarta Servlet 6, JSP, JSTL, HikariCP, MySQL, and Embedded Tomcat 10.1).

---

## 🚀 One-Command Local Run

### Prerequisites
1. **Java 17+** (`java -version`)
2. **Maven 3.8+** (`mvn -version`)
3. **MySQL 8.0+** running locally on port 3306

### Quick Start
You do **not** need to manually run SQL files or create database tables. On first startup, the app automatically checks MySQL, creates `healthcare_db`, applies schema migrations, and seeds demo data idempotently.

Run the application:
```bash
mvn compile exec:java
```
Once started, visit: **[http://localhost:8080](http://localhost:8080)**

---

## 🌐 Share with Friends (Temporary Public HTTPS Link)

You can share your local running app with friends, colleagues, or mobile devices via a temporary, free, secure HTTPS tunnel. No cloud hosting or firewall port-forwarding required.

### Tunnel Tool Installation

The sharing script uses **Cloudflare Tunnel** (`cloudflared`) by default, with automatic fallback to **ngrok**.

#### Option A: Cloudflare Tunnel (Recommended — 100% Free, No Sign-up Needed)
- **macOS (Homebrew):**
  ```bash
  brew install cloudflared
  ```
- **Windows (winget):**
  ```powershell
  winget install Cloudflare.cloudflared
  ```
- **Linux (Debian/Ubuntu):**
  ```bash
  sudo apt-get install cloudflared
  ```

#### Option B: ngrok (Alternative)
- Download from [ngrok.com/download](https://ngrok.com/download) or install via brew: `brew install ngrok`
- Authenticate if prompted: `ngrok config add-authtoken <your-token>`

---

### Step-by-Step: Sharing Your App

1. **Start MySQL**  
   Ensure MySQL server is running locally (e.g., `brew services start mysql` on macOS, or via MySQL Workbench / Windows Services).

2. **Run the Sharing Script**  
   - **macOS / Linux:**
     ```bash
     ./share.sh
     ```
   - **Windows:**
     ```cmd
     share.bat
     ```
   *The script automatically checks application health at `http://localhost:8080/health`, launches the tunnel, and generates your temporary HTTPS link.*

3. **Copy the Public Link**  
   The terminal will print your public HTTPS address:
   ```text
   🎉 YOUR TEMPORARY PUBLIC LINK IS LIVE!
   👉 https://random-words-here.trycloudflare.com
   ```

4. **Send the Link to Friends**  
   Share the link! Friends can open it directly from their phones or laptops anywhere on the internet.

5. **Stop Sharing**  
   Press `Ctrl + C` in the terminal to cleanly terminate the tunnel and local server.

---

## 🔑 Demo Logins

All accounts are pre-seeded and feature 1-click auto-fill buttons on the login page:

| Role | Email | Password | Dashboard Features |
| :--- | :--- | :--- | :--- |
| 👑 **Admin** | `admin@carefirst.com` | `Admin@123` | System settings, user registry, audit logs, clinic analytics, demo database reset |
| 🩺 **Doctor** | `doctor.sarah@carefirst.com` | `Doctor@123` | Appointment queue, patient consults, EHR diagnosis entry, reviews |
| 👤 **Patient** | `john.doe@example.com` | `Patient@123` | Specialist doctor catalog, live slot booking, prescriptions, EHR records |

---

## 🔄 Restoring Demo Data

When friends test the system (create bookings, leave reviews, or test cancellations), you can restore the system back to its original demo state at any time:

- **From the Web UI (Admin):**  
  Log in as Admin &rarr; navigate to **Settings** (`/admin/settings`) &rarr; click **"⚡ Reset Demo Database"**.
- **From MySQL CLI:**  
  ```bash
  mysql -u root healthcare_db < sql/reset-demo.sql
  ```

---

## 🛡️ Tunnel & Security Architecture

1. **Proxy & Tunnel Headers (`RemoteIpValve`):**  
   Configured to honour `X-Forwarded-Proto`, `X-Forwarded-For`, and `X-Forwarded-Host`. All internal redirects and links preserve `https://` across the tunnel.
2. **Hardened Session Cookies:**  
   - `HttpOnly`: Enabled on session cookies to defend against XSS.
   - `SameSite=Lax`: Enabled to prevent cross-site request forgery while remaining compatible with external tunnel navigation.
   - `Secure`: Attached automatically whenever traffic passes through HTTPS (`https://*.trycloudflare.com`), while allowing local testing over plain `http://localhost:8080`.
3. **Registration IP Throttling:**  
   Patient self-registration is rate-limited to 5 accounts per hour per IP address to prevent spam bots.
4. **Context Path Portability:**  
   All asset paths and controller redirects dynamically evaluate `request.getContextPath()`.

---

## 🔧 Troubleshooting

### 1. Port 8080 is already busy
If port 8080 is in use by another process:
- **macOS / Linux:**
  ```bash
  lsof -i :8080
  kill -9 <PID>
  ```
- **Windows:**
  ```cmd
  netstat -ano | findstr :8080
  taskkill /PID <PID> /F
  ```
- Or run the app on a custom port by setting the `PORT` environment variable:
  ```bash
  PORT=9090 ./share.sh
  ```

### 2. MySQL is not running / Connection refused
If you see a database connection error:
- Ensure MySQL is active: `mysqladmin -u root ping`
- Check credentials in `src/main/resources/db.properties` or provide environment variables:
  ```bash
  export DB_USERNAME="root"
  export DB_PASSWORD="yourpassword"
  ./share.sh
  ```

### 3. Tunnel link changes on every run
Temporary Cloudflare quick tunnels generate a new random domain (`*.trycloudflare.com`) on each launch. If you need a permanent branded domain, configure a named Cloudflare Tunnel with `cloudflared tunnel login`.

### 4. Laptop sleep kills the link
Because the application runs on your local machine, putting your laptop to sleep, disconnecting Wi-Fi, or closing the lid will suspend the tunnel. Keep your computer awake while friends are browsing.

### 5. Health Check Endpoint
To inspect container and database health at any time:
```bash
curl -i http://localhost:8080/health
```
Expected output:
```json
{"status":"UP","database":"OK","uptimeSeconds":42,"timestamp":1791559999000}
```
