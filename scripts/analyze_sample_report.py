#!/usr/bin/env python3
import html
import re
import sys
import zipfile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]

if hasattr(sys.stdout, "reconfigure"):
    sys.stdout.reconfigure(encoding="utf-8", errors="replace")


def clean_lines(text):
    lines = [re.sub(r"\s+", " ", line).strip() for line in text.replace("\xa0", " ").splitlines()]
    return [line for line in lines if line]


def extract_docx(path):
    with zipfile.ZipFile(path) as archive:
        xml = archive.read("word/document.xml").decode("utf-8", errors="ignore")
    text = re.sub(r"</w:p[^>]*>", "\n", xml)
    text = re.sub(r"<[^>]+>", "", text)
    return clean_lines(html.unescape(text))


def extract_pdf(path):
    try:
        from pypdf import PdfReader
    except ImportError:
        from PyPDF2 import PdfReader

    reader = PdfReader(str(path))
    pages = []
    for index, page in enumerate(reader.pages, start=1):
        pages.append((index, clean_lines(page.extract_text() or "")))
    return pages


def print_docx_summary(path):
    lines = extract_docx(path)
    print(f"\n=== {path.name} ===")
    print(f"Extracted lines: {len(lines)}")
    for index, line in enumerate(lines, start=1):
        print(f"{index:03}: {line}")


def print_pdf_summary(path):
    pages = extract_pdf(path)
    print(f"\n=== {path.name} ===")
    print(f"Pages: {len(pages)}")
    for page_number, lines in pages:
        print(f"\n--- Page {page_number} ---")
        for index, line in enumerate(lines, start=1):
            print(f"{page_number}.{index:03}: {line}")


def main():
    print_docx_summary(ROOT / "Sample_Final_Report_GroupID.docx")
    print_pdf_summary(ROOT / "PAF_Assignment-2026.pdf")
    print_pdf_summary(ROOT / "IT3030_PAF_2026_Marking_Rubric.pdf")


if __name__ == "__main__":
    main()
