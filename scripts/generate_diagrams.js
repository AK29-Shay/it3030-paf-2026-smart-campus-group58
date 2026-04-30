const fs = require("fs");
const path = require("path");
const puppeteer = require("puppeteer");

const ROOT = path.resolve(__dirname, "..");
const DIAGRAM_DIR = path.join(ROOT, "docs", "diagrams");
const MERMAID_BROWSER_PATH = path.join(__dirname, "node_modules", "mermaid", "dist", "mermaid.min.js");

const diagrams = [
  {
    source: "overall-architecture.mmd",
    output: "overall-architecture.png",
    title: "Overall Architecture Diagram",
  },
  {
    source: "rest-api-architecture.mmd",
    output: "rest-api-architecture.png",
    title: "REST API Architecture Diagram",
  },
  {
    source: "frontend-architecture.mmd",
    output: "frontend-architecture.png",
    title: "Frontend Architecture Diagram",
  },
];

function escapeHtml(value) {
  return value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;");
}

async function renderDiagram(browser, diagram, mermaidSource) {
  const mmdPath = path.join(DIAGRAM_DIR, diagram.source);
  const pngPath = path.join(DIAGRAM_DIR, diagram.output);
  const definition = fs.readFileSync(mmdPath, "utf8");

  const page = await browser.newPage();
  await page.setViewport({ width: 1800, height: 1100, deviceScaleFactor: 2 });
  await page.setContent(
    `<!doctype html>
    <html>
      <head>
        <meta charset="utf-8" />
        <style>
          body {
            margin: 0;
            background: #f8fafc;
            font-family: Arial, sans-serif;
            color: #111827;
          }
          .frame {
            display: inline-block;
            padding: 34px;
            background: #ffffff;
            border: 1px solid #dbe4f0;
            border-radius: 10px;
          }
          h1 {
            margin: 0 0 20px;
            font-size: 28px;
            line-height: 1.2;
            color: #11185c;
          }
          .mermaid {
            background: #ffffff;
          }
        </style>
      </head>
      <body>
        <section class="frame">
          <h1>${escapeHtml(diagram.title)}</h1>
          <div class="mermaid">${escapeHtml(definition)}</div>
        </section>
        <script>${mermaidSource}</script>
        <script>
          mermaid.initialize({
            startOnLoad: true,
            securityLevel: "loose",
            theme: "base",
            themeVariables: {
              primaryColor: "#eef2ff",
              primaryTextColor: "#11185c",
              primaryBorderColor: "#4f46e5",
              lineColor: "#2563eb",
              secondaryColor: "#ecfeff",
              tertiaryColor: "#f8fafc",
              fontFamily: "Arial"
            }
          });
        </script>
      </body>
    </html>`,
    { waitUntil: "load" },
  );

  await page.waitForSelector(".mermaid svg", { timeout: 15000 });
  await page.$eval(".mermaid svg", (svg) => {
    svg.setAttribute("width", svg.getBBox().width + 24);
    svg.setAttribute("height", svg.getBBox().height + 24);
  });

  const frame = await page.$(".frame");
  await frame.screenshot({ path: pngPath });
  await page.close();

  console.log(`Generated ${pngPath}`);
}

async function main() {
  if (!fs.existsSync(MERMAID_BROWSER_PATH)) {
    throw new Error("Missing mermaid dependency. Run `npm install` in the scripts directory.");
  }

  const mermaidSource = fs.readFileSync(MERMAID_BROWSER_PATH, "utf8");
  const browser = await puppeteer.launch({ headless: "new" });

  try {
    for (const diagram of diagrams) {
      await renderDiagram(browser, diagram, mermaidSource);
    }
  } finally {
    await browser.close();
  }
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
