const fs = require("fs");
const path = require("path");
const { marked } = require("marked");
const HTMLToDOCX = require("html-to-docx");

const ROOT = path.resolve(__dirname, "..");
const markdownPath = path.join(ROOT, "docs", "Final_Report_Draft.md");
const docxPath = path.join(ROOT, "docs", "Final_Report.docx");

async function main() {
  if (!fs.existsSync(markdownPath)) {
    throw new Error(`Missing report draft: ${markdownPath}`);
  }

  const markdown = fs.readFileSync(markdownPath, "utf8");
  const html = marked.parse(markdown);
  const documentHtml = `
    <!doctype html>
    <html>
      <head>
        <meta charset="utf-8" />
        <style>
          body { font-family: Arial, sans-serif; font-size: 11pt; line-height: 1.45; color: #111827; }
          h1 { font-size: 20pt; margin: 0 0 12pt; }
          h2 { font-size: 15pt; margin: 18pt 0 8pt; }
          h3 { font-size: 12pt; margin: 12pt 0 6pt; }
          p { margin: 0 0 8pt; }
          table { border-collapse: collapse; width: 100%; margin: 8pt 0 12pt; }
          th, td { border: 1px solid #9ca3af; padding: 5pt; vertical-align: top; }
          th { background: #eef2ff; font-weight: bold; }
          code, pre { font-family: Consolas, monospace; }
          pre { background: #f3f4f6; padding: 8pt; white-space: pre-wrap; }
        </style>
      </head>
      <body>${html}</body>
    </html>
  `;

  const buffer = await HTMLToDOCX(documentHtml, null, {
    table: { row: { cantSplit: true } },
    footer: true,
    pageNumber: true,
  });

  fs.writeFileSync(docxPath, buffer);
  console.log(`Generated ${docxPath}`);
}

main().catch((error) => {
  console.error(error);
  process.exitCode = 1;
});
