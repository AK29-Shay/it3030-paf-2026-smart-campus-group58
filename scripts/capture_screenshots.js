const { execFileSync, execSync } = require("child_process");
const fs = require("fs");
const path = require("path");
const puppeteer = require("puppeteer");

const ROOT = path.resolve(__dirname, "..");
const SCREENSHOT_DIR = path.join(ROOT, "docs", "screenshots");
const FRONTEND_BASE = process.env.FRONTEND_BASE || "http://localhost:5173";
const API_BASE = process.env.API_BASE || "http://localhost:8080";
const GITHUB_REPO = process.env.GITHUB_REPO || "https://github.com/AK29-Shay/it3030-paf-2026-smart-campus-group58";
const CAPTURE_MODE = process.env.CAPTURE_MODE || "all";

const accounts = {
  USER: { email: "student@example.com", password: "ChangeMe123!", role: "USER" },
  ADMIN: { email: "admin@example.com", password: "ChangeMe123!", role: "ADMIN" },
  TECHNICIAN: { email: "technician@example.com", password: "ChangeMe123!", role: "TECHNICIAN" },
};

fs.mkdirSync(SCREENSHOT_DIR, { recursive: true });

function safeName(name) {
  return name.replace(/[^a-z0-9_-]+/gi, "_").toLowerCase();
}

function escapeHtml(value) {
  return String(value)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

async function capturePage(page, name, url) {
  const file = path.join(SCREENSHOT_DIR, `${safeName(name)}.png`);
  await page.goto(url, { waitUntil: "networkidle2", timeout: 25000 });
  await new Promise((resolve) => setTimeout(resolve, 900));
  await page.screenshot({ path: file, fullPage: true });
  console.log(`Captured ${file}`);
}

async function captureExternalPage(page, name, url) {
  const file = path.join(SCREENSHOT_DIR, `${safeName(name)}.png`);
  await page.goto(url, { waitUntil: "domcontentloaded", timeout: 15000 });
  await new Promise((resolve) => setTimeout(resolve, 1200));
  await page.screenshot({ path: file, fullPage: true });
  console.log(`Captured ${file}`);
}

async function login(page, role) {
  const account = accounts[role];
  const result = await page.evaluate(
    async ({ apiBase, account }) => {
      const response = await fetch(`${apiBase}/api/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(account),
      });

      if (!response.ok) {
        return { ok: false, status: response.status, text: await response.text() };
      }

      const payload = await response.json();
      localStorage.setItem("token", payload.token);
      return { ok: true };
    },
    { apiBase: API_BASE, account },
  );

  if (!result.ok) {
    throw new Error(`Login failed for ${role}: ${result.status} ${result.text}`);
  }
}

async function apiLogin(role) {
  const account = accounts[role];
  const response = await fetch(`${API_BASE}/api/auth/login`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(account),
  });

  if (!response.ok) {
    return null;
  }

  return response.json();
}

async function resolveQrLookupPath() {
  try {
    const auth = await apiLogin("ADMIN");
    if (!auth?.token) {
      return "/qr-verify/1";
    }

    const response = await fetch(`${API_BASE}/api/bookings`, {
      headers: { Authorization: `Bearer ${auth.token}` },
    });
    if (!response.ok) {
      return "/qr-verify/1";
    }

    const bookings = await response.json();
    const booking = bookings.find((item) => ["APPROVED", "CHECKED_IN"].includes(item.status)) || bookings[0];
    return booking?.id ? `/qr-verify/${booking.id}` : "/qr-verify/1";
  } catch {
    return "/qr-verify/1";
  }
}

async function captureRolePages(browser, role, pages) {
  const page = await browser.newPage();
  await page.setViewport({ width: 1440, height: 950 });
  await page.goto(FRONTEND_BASE, { waitUntil: "networkidle2", timeout: 25000 });
  await login(page, role);

  for (const item of pages) {
    try {
      await capturePage(page, item.name, `${FRONTEND_BASE}${item.path}`);
    } catch (error) {
      console.log(`Failed ${item.name}: ${error.message}`);
    }
  }

  await page.close();
}

function runCommand(command, args, options = {}) {
  try {
    return execFileSync(command, args, {
      cwd: ROOT,
      encoding: "utf8",
      timeout: options.timeout || 90000,
      env: { ...process.env, PYTHONIOENCODING: "utf-8" },
    });
  } catch (error) {
    return [
      `$ ${command} ${args.join(" ")}`,
      "",
      error.stdout || "",
      error.stderr || "",
      `Command failed with status ${error.status ?? "unknown"}`,
    ].join("\n");
  }
}

function runShell(command) {
  try {
    return execSync(command, {
      cwd: ROOT,
      encoding: "utf8",
      timeout: 120000,
      env: { ...process.env, PYTHONIOENCODING: "utf-8" },
    });
  } catch (error) {
    return [
      `$ ${command}`,
      "",
      error.stdout || "",
      error.stderr || "",
      `Command failed with status ${error.status ?? "unknown"}`,
    ].join("\n");
  }
}

function normalizeGithubRepo(value) {
  if (/^https?:\/\//i.test(value)) {
    return value.replace(/\/$/, "");
  }
  return `https://github.com/${value.replace(/^\/+|\/+$/g, "")}`;
}

function commandExists(command) {
  const checker = process.platform === "win32" ? "where" : "which";
  try {
    execFileSync(checker, [command], { encoding: "utf8", stdio: "ignore" });
    return true;
  } catch {
    return false;
  }
}

function apiEvidenceCommand() {
  if (commandExists("bash")) {
    return {
      label: "bash scripts/api_tests.sh",
      command: "bash",
      args: ["scripts/api_tests.sh"],
    };
  }

  return {
    label: "powershell -NoProfile -ExecutionPolicy Bypass -File scripts/api_tests.ps1",
    command: "powershell",
    args: ["-NoProfile", "-ExecutionPolicy", "Bypass", "-File", "scripts/api_tests.ps1"],
  };
}

async function captureTerminal(page, name, title, command, output) {
  const html = `
    <html>
      <body style="margin:0;background:#111827;color:#e5e7eb;font-family:Consolas,Menlo,monospace;">
        <main style="padding:24px;min-height:640px;">
          <div style="color:#93c5fd;font-weight:700;margin-bottom:12px;">${escapeHtml(title)}</div>
          <div style="color:#fbbf24;margin-bottom:16px;">$ ${escapeHtml(command)}</div>
          <pre style="white-space:pre-wrap;word-break:break-word;font-size:15px;line-height:1.45;margin:0;">${escapeHtml(output)}</pre>
        </main>
      </body>
    </html>
  `;
  await page.setViewport({ width: 1280, height: 820 });
  await page.setContent(html, { waitUntil: "load" });
  const file = path.join(SCREENSHOT_DIR, `${safeName(name)}.png`);
  await page.screenshot({ path: file, fullPage: true });
  console.log(`Captured ${file}`);
}

async function main() {
  const browser = await puppeteer.launch({ headless: "new" });

  if (CAPTURE_MODE === "all" || CAPTURE_MODE === "ui") {
    const qrLookupPath = await resolveQrLookupPath();
    const publicPage = await browser.newPage();
    await publicPage.setViewport({ width: 1440, height: 950 });
    for (const item of [
      { name: "01_home", path: "/" },
      { name: "02_login", path: "/login" },
      { name: "03_signup", path: "/signup" },
      { name: "04_forgot_password", path: "/forgot-password" },
      { name: "05_qr_public_lookup", path: qrLookupPath },
    ]) {
      try {
        await capturePage(publicPage, item.name, `${FRONTEND_BASE}${item.path}`);
      } catch (error) {
        console.log(`Failed ${item.name}: ${error.message}`);
      }
    }
    await publicPage.close();

    await captureRolePages(browser, "USER", [
      { name: "06_student_dashboard", path: "/dashboard/student" },
      { name: "07_resources", path: "/resources" },
      { name: "08_booking_list", path: "/bookings" },
      { name: "09_booking_calendar", path: "/bookings/calendar" },
      { name: "10_create_booking", path: "/create" },
      { name: "11_create_ticket", path: "/tickets/create" },
      { name: "12_my_tickets", path: "/tickets/my" },
      { name: "13_notifications", path: "/notifications" },
    ]);

    await captureRolePages(browser, "TECHNICIAN", [
      { name: "14_technician_dashboard", path: "/dashboard/technician" },
      { name: "15_technician_tickets", path: "/tickets/technician" },
    ]);

    await captureRolePages(browser, "ADMIN", [
      { name: "16_admin_dashboard", path: "/admin" },
      { name: "17_command_center", path: "/admin/command-center" },
      { name: "18_admin_resources", path: "/admin/resources" },
      { name: "19_admin_bookings", path: "/admin/bookings" },
      { name: "20_admin_tickets", path: "/tickets/admin" },
      { name: "21_user_management", path: "/admin/users" },
      { name: "22_mock_scanner", path: "/scanner-mock" },
    ]);
  }

  if (CAPTURE_MODE === "all" || CAPTURE_MODE === "github" || CAPTURE_MODE === "evidence") {
    const githubBase = normalizeGithubRepo(GITHUB_REPO);
    const githubPage = await browser.newPage();
    await githubPage.setViewport({ width: 1440, height: 950 });
    for (const item of [
      { name: "23_github_commits", url: `${githubBase}/commits` },
      { name: "24_github_branches", url: `${githubBase}/branches` },
      { name: "25_github_pull_requests", url: `${githubBase}/pulls` },
    ]) {
      try {
        await captureExternalPage(githubPage, item.name, item.url);
      } catch (error) {
        console.log(`Failed ${item.name}: ${error.message}`);
      }
    }
    await githubPage.close();
  }

  if (CAPTURE_MODE === "all" || CAPTURE_MODE === "terminal" || CAPTURE_MODE === "evidence") {
    const terminalPage = await browser.newPage();
    const mongoOutput = runCommand("python", ["scripts/query_mongodb.py"], { timeout: 30000 });
    await captureTerminal(terminalPage, "26_mongodb_collections", "MongoDB Evidence", "python scripts/query_mongodb.py", mongoOutput);

    const apiCommand = apiEvidenceCommand();
    const apiOutput = runCommand(apiCommand.command, apiCommand.args, { timeout: 120000 });
    await captureTerminal(terminalPage, "27_api_curl_evidence", "API Curl Evidence", apiCommand.label, apiOutput);

    await terminalPage.close();
  }

  await browser.close();
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
