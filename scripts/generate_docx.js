const fs = require("fs");
const path = require("path");
const { execFileSync } = require("child_process");
const { marked } = require("marked");
const HTMLToDOCX = require("html-to-docx");

const ROOT = path.resolve(__dirname, "..");
const markdownPath = path.join(ROOT, "docs", "Final_Report_Draft.md");
const docxPath = path.join(ROOT, "docs", "Final_Report.docx");

const diagramImages = [
  "diagrams/overall-architecture.png",
  "diagrams/rest-api-architecture.png",
  "diagrams/frontend-architecture.png",
];

function titleFromFile(filePath) {
  return path
    .basename(filePath, path.extname(filePath))
    .replace(/^\d+_/, "")
    .replace(/[-_]+/g, " ")
    .replace(/\b\w/g, (letter) => letter.toUpperCase());
}

function imageDataUri(src) {
  const normalized = src.replace(/\\/g, "/").replace(/^docs\//, "");
  const absolutePath = path.join(ROOT, "docs", normalized);

  if (!fs.existsSync(absolutePath)) {
    return src;
  }

  const extension = path.extname(absolutePath).slice(1).toLowerCase() || "png";
  const mime = extension === "jpg" || extension === "jpeg" ? "image/jpeg" : "image/png";
  const encoded = fs.readFileSync(absolutePath).toString("base64");
  return `data:${mime};base64,${encoded}`;
}

function prepareMarkdown(markdown) {
  let diagramIndex = 0;

  return markdown
    .replace(/```mermaid\n[\s\S]*?```/g, () => {
      const src = diagramImages[diagramIndex] || diagramImages[diagramImages.length - 1];
      diagramIndex += 1;
      return `![${titleFromFile(src)}](${src})`;
    })
    .replace(/\*\*Screenshot placeholder:\*\* Insert generated .*? diagram image here\.\n?/g, "")
    .replace(
      /- Insert screenshot `docs\/screenshots\/([^`]+)`/g,
      (_, fileName) => `![Screenshot: ${titleFromFile(fileName)}](screenshots/${fileName})`,
    );
}

function embedLocalImages(html) {
  return html.replace(/<img src="([^"]+)" alt="([^"]*)">/g, (match, src, alt) => {
    const dataUri = imageDataUri(src);
    const caption = alt ? `<p class="caption">${alt}</p>` : "";
    return `<p><img src="${dataUri}" alt="${alt}" width="650" /></p>${caption}`;
  });
}

async function main() {
  if (!fs.existsSync(markdownPath)) {
    throw new Error(`Missing report draft: ${markdownPath}`);
  }

  execFileSync("node", ["scripts/generate_diagrams.js"], {
    cwd: ROOT,
    stdio: "inherit",
  });

  const markdown = prepareMarkdown(fs.readFileSync(markdownPath, "utf8"));
  const html = embedLocalImages(marked.parse(markdown));
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
          figure { margin: 10pt 0 14pt; page-break-inside: avoid; }
          img { max-width: 100%; height: auto; border: 1px solid #d1d5db; }
          .caption { font-size: 9pt; color: #4b5563; margin: 3pt 0 8pt; font-style: italic; }
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
