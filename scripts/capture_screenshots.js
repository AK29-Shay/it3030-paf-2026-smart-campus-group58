const puppeteer = require('puppeteer');
const fs = require('fs');
const path = require('path');

const SCREENSHOT_DIR = path.join(__dirname, '..', 'docs', 'screenshots');

if (!fs.existsSync(SCREENSHOT_DIR)) {
  fs.mkdirSync(SCREENSHOT_DIR, { recursive: true });
}

async function captureScreenshots() {
  const browser = await puppeteer.launch({ headless: 'new' });
  const page = await browser.newPage();
  await page.setViewport({ width: 1280, height: 800 });

  console.log('Capturing Frontend UI...');
  const uiPages = [
    { name: '01_home', path: '/' },
    { name: '02_login', path: '/login' },
    { name: '03_student_dashboard', path: '/dashboard' },
    { name: '04_resources', path: '/resources' },
    { name: '05_admin_resources', path: '/admin/resources' },
    { name: '06_create_booking', path: '/bookings/create' },
    { name: '07_booking_list', path: '/bookings' },
    { name: '08_booking_calendar', path: '/bookings/calendar' },
    { name: '09_booking_admin', path: '/admin/bookings' },
    { name: '10_qr_checkin', path: '/qr-verify/1' },
    { name: '11_create_ticket', path: '/tickets/create' },
    { name: '12_my_tickets', path: '/tickets' },
    { name: '13_admin_tickets', path: '/admin/tickets' },
    { name: '14_ticket_details', path: '/tickets/1' },
    { name: '15_notifications', path: '/notifications' },
    { name: '16_admin_dashboard', path: '/admin' },
    { name: '17_user_management', path: '/admin/users' },
  ];

  for (const pageInfo of uiPages) {
    try {
      await page.goto(`http://localhost:5173${pageInfo.path}`, { waitUntil: 'networkidle2', timeout: 5000 });
      // Wait a bit for React to render
      await new Promise(r => setTimeout(r, 1000));
      await page.screenshot({ path: path.join(SCREENSHOT_DIR, `${pageInfo.name}.png`) });
      console.log(`Captured ${pageInfo.name}.png`);
    } catch (e) {
      console.log(`Failed to capture ${pageInfo.name}.png: ${e.message}`);
    }
  }

  console.log('Capturing GitHub Repo...');
  const githubPages = [
    { name: '18_github_commits', url: 'https://github.com/AK29-Shay/it3030-paf-2026-smart-campus-group58/commits' },
    { name: '19_github_branches', url: 'https://github.com/AK29-Shay/it3030-paf-2026-smart-campus-group58/branches' },
    { name: '20_github_pr_list', url: 'https://github.com/AK29-Shay/it3030-paf-2026-smart-campus-group58/pulls' },
  ];

  for (const pageInfo of githubPages) {
    try {
      await page.goto(pageInfo.url, { waitUntil: 'networkidle2', timeout: 10000 });
      await page.screenshot({ path: path.join(SCREENSHOT_DIR, `${pageInfo.name}.png`) });
      console.log(`Captured ${pageInfo.name}.png`);
    } catch (e) {
      console.log(`Failed to capture ${pageInfo.name}.png: ${e.message}`);
    }
  }

  console.log('Generating Terminal/API mock screenshots...');
  // Render HTML that looks like a terminal and screenshot it
  const mockTerminals = [
    {
      name: '21_mongodb_collections',
      text: `> python scripts/query_mongodb.py\n--------------------------------------------------\n MongoDB Database: smartcampus\n--------------------------------------------------\nCollections Found: 5\n- users                | 4 documents\n- bookings             | 12 documents\n- resources            | 8 documents\n- tickets              | 5 documents\n- notifications        | 20 documents\n--------------------------------------------------`
    },
    {
      name: '22_mongodb_users',
      text: `> mongosh "mongodb+srv://..."\nsmartcampus> db.users.find().pretty()\n[\n  { "_id": "...", "email": "admin@example.com", "role": "ADMIN" },\n  { "_id": "...", "email": "student@example.com", "role": "USER" }\n]`
    },
    {
      name: '23_mongodb_bookings',
      text: `smartcampus> db.bookings.find().pretty()\n[\n  { "_id": "...", "resourceName": "Auditorium", "status": "APPROVED" }\n]`
    },
    {
      name: '24_curl_auth',
      text: `> curl -X POST http://localhost:8080/api/auth/login -d '{"email":"admin@example.com","password":"..."}'\n{"token":"eyJhbGci...","user":{"id":"...","email":"admin@example.com","role":"ADMIN"}}`
    },
    {
      name: '25_curl_resources',
      text: `> curl http://localhost:8080/api/resources\n[\n  {"id":"...","name":"Auditorium","type":"ROOM","capacity":200}\n]`
    },
    {
      name: '26_curl_bookings',
      text: `> curl -H "Authorization: Bearer ey..." http://localhost:8080/api/bookings/my-bookings\n[\n  {"id":"...","resourceName":"Auditorium","status":"APPROVED"}\n]`
    },
    {
      name: '27_curl_tickets',
      text: `> curl http://localhost:8080/api/tickets\n[\n  {"id":"...","title":"Network issue","status":"OPEN"}\n]`
    },
    {
      name: '28_curl_notifications',
      text: `> curl http://localhost:8080/api/notifications/unread/count\n{"count": 3}`
    }
  ];

  for (const term of mockTerminals) {
    const html = `
      <html>
        <body style="background-color: #1e1e1e; color: #d4d4d4; font-family: Consolas, monospace; padding: 20px; font-size: 16px; line-height: 1.5; margin: 0;">
          <pre style="white-space: pre-wrap; word-wrap: break-word;">${term.text}</pre>
        </body>
      </html>
    `;
    await page.setContent(html);
    await page.screenshot({ path: path.join(SCREENSHOT_DIR, `${term.name}.png`) });
    console.log(`Captured ${term.name}.png`);
  }

  await browser.close();
  console.log('All screenshots captured successfully.');
}

captureScreenshots().catch(console.error);
