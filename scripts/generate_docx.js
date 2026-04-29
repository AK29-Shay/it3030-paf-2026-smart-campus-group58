const fs = require('fs');
const path = require('path');
const { marked } = require('marked');
const HTMLToDOCX = require('html-to-docx');

const mdPath = path.join(__dirname, '..', 'docs', 'Final_Report_Draft.md');
const docxPath = path.join(__dirname, '..', 'docs', 'Final_Report.docx');

async function convert() {
  try {
    console.log('Reading Markdown file...');
    const mdContent = fs.readFileSync(mdPath, 'utf-8');
    
    console.log('Converting Markdown to HTML...');
    const htmlContent = marked.parse(mdContent);
    
    // Add basic styling for the DOCX
    const fullHtml = `
      <!DOCTYPE html>
      <html>
        <head>
          <meta charset="utf-8">
          <style>
            body { font-family: Arial, sans-serif; font-size: 11pt; line-height: 1.5; }
            h1 { font-size: 18pt; font-weight: bold; margin-bottom: 12pt; }
            h2 { font-size: 14pt; font-weight: bold; margin-bottom: 10pt; margin-top: 16pt; }
            h3 { font-size: 12pt; font-weight: bold; margin-bottom: 8pt; }
            p { margin-bottom: 10pt; }
            ul, ol { margin-bottom: 10pt; }
            li { margin-bottom: 4pt; }
            pre { background-color: #f4f4f4; padding: 10px; font-family: monospace; }
            code { background-color: #f4f4f4; padding: 2px 4px; font-family: monospace; }
          </style>
        </head>
        <body>
          ${htmlContent}
        </body>
      </html>
    `;
    
    console.log('Generating DOCX file...');
    const docxBuffer = await HTMLToDOCX(fullHtml, null, {
      table: { row: { cantSplit: true } },
      footer: true,
      pageNumber: true,
    });
    
    fs.writeFileSync(docxPath, docxBuffer);
    console.log('Successfully created DOCX file at: ' + docxPath);
  } catch (error) {
    console.error('Error generating DOCX:', error);
  }
}

convert();
