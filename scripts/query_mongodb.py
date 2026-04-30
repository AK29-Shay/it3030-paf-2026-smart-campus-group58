#!/usr/bin/env python3
import os
from pathlib import Path
from urllib.parse import urlparse

try:
    from pymongo import MongoClient
except ImportError as exc:
    raise SystemExit("pymongo is required. Install it with: pip install pymongo") from exc


ROOT = Path(__file__).resolve().parents[1]
ENV_PATHS = [
    ROOT / "backend" / "smartcampus" / ".env",
    ROOT / ".env",
]


def load_env_file():
    values = {}
    for path in ENV_PATHS:
        if not path.exists():
            continue
        for raw_line in path.read_text(encoding="utf-8", errors="ignore").splitlines():
            line = raw_line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, value = line.split("=", 1)
            values[key.strip()] = value.strip().strip('"').strip("'")
    return values


def mask_uri(uri):
    if "://" not in uri or "@" not in uri:
        return uri
    scheme, rest = uri.split("://", 1)
    return f"{scheme}://<credentials>@{rest.split('@', 1)[1]}"


def database_name(uri, env_values):
    explicit = os.environ.get("MONGODB_DATABASE") or env_values.get("MONGODB_DATABASE")
    if explicit:
        return explicit

    parsed = urlparse(uri)
    name = parsed.path.strip("/")
    return name or "smartcampus"


def main():
    env_values = load_env_file()
    uri = (
        os.environ.get("SPRING_DATA_MONGODB_URI")
        or os.environ.get("MONGODB_URI")
        or env_values.get("SPRING_DATA_MONGODB_URI")
        or env_values.get("MONGODB_URI")
        or "mongodb://127.0.0.1:27017/smartcampus"
    )
    db_name = database_name(uri, env_values)

    print("--------------------------------------------------")
    print(" Smart Campus MongoDB Evidence")
    print("--------------------------------------------------")
    print(f"URI: {mask_uri(uri)}")
    print(f"Database: {db_name}")

    client = MongoClient(uri, serverSelectionTimeoutMS=5000)
    db = client[db_name]
    collections = sorted(db.list_collection_names())

    print("--------------------------------------------------")
    print(f"{'Collection':<28} {'Documents':>10}")
    print("--------------------------------------------------")

    for collection_name in collections:
        count = db[collection_name].count_documents({})
        print(f"{collection_name:<28} {count:>10}")

    print("--------------------------------------------------")


if __name__ == "__main__":
    try:
        main()
    except Exception as exc:
        print(f"Connection failed: {exc}")
        raise SystemExit(1)
