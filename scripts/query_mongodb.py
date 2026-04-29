#!/usr/bin/env python3
"""
Smart Campus Operations Hub — MongoDB Evidence Script
Group 58 | IT3030 PAF 2026
Run: python scripts/query_mongodb.py
Requires: pip install pymongo tabulate
"""

from pymongo import MongoClient
from tabulate import tabulate
from datetime import datetime

MONGO_URI = (
    "mongodb+srv://SnehaDhaya:Snehadhaya_2002@cluster0.wergga1.mongodb.net/"
    "smartcampus?retryWrites=true&w=majority"
)

DB_NAME = "smartcampus"

def print_header(title: str):
    line = "=" * 70
    print(f"\n{line}")
    print(f"  {title}")
    print(f"{line}")

def fmt(val):
    """Format a value for table display."""
    if val is None:
        return "—"
    if isinstance(val, datetime):
        return val.strftime("%Y-%m-%d %H:%M")
    if isinstance(val, list):
        return f"[{len(val)} items]"
    s = str(val)
    return s[:55] + "…" if len(s) > 55 else s

def show_collection(db, name: str, columns: list[str], limit: int = 10):
    col = db[name]
    total = col.count_documents({})
    docs = list(col.find({}, {c: 1 for c in columns}).limit(limit))

    print_header(f"Collection: {name}  ({total} total documents)")

    if not docs:
        print("  (empty)")
        return

    rows = []
    for d in docs:
        row = [fmt(d.get(c, "—")) for c in columns]
        rows.append(row)

    print(tabulate(rows, headers=columns, tablefmt="rounded_outline"))


def main():
    print("\n" + "=" * 70)
    print("  Smart Campus Operations Hub — MongoDB Evidence")
    print(f"  Generated: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"  Database : {DB_NAME}")
    print("=" * 70)

    client = MongoClient(MONGO_URI, serverSelectionTimeoutMS=10000)
    db = client[DB_NAME]

    # List all collections
    collections = sorted(db.list_collection_names())
    print(f"\n📦 Collections found ({len(collections)}):\n")
    for c in collections:
        count = db[c].count_documents({})
        print(f"   • {c:<30} {count:>5} documents")

    # Users
    show_collection(db, "users",
        ["name", "email", "role", "provider"])

    # Resources
    show_collection(db, "resources",
        ["name", "type", "category", "status", "location", "capacity"])

    # Bookings
    show_collection(db, "bookings",
        ["resourceName", "bookedBy", "status", "startTime", "endTime"])

    # Tickets
    show_collection(db, "tickets",
        ["title", "category", "priority", "status", "createdBy"])

    # Notifications
    show_collection(db, "notifications",
        ["userId", "type", "message", "read"])

    print("\n" + "=" * 70)
    print("  ✅  Query complete — screenshot this terminal for evidence.")
    print("=" * 70 + "\n")

    client.close()


if __name__ == "__main__":
    main()
