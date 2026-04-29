import os
from pymongo import MongoClient
import urllib.parse

# Use the credentials
username = "SnehaDhaya"
password = urllib.parse.quote_plus("Snehadhaya@2002")
uri = f"mongodb+srv://{username}:{password}@cluster0.wergga1.mongodb.net/?retryWrites=true&w=majority"

try:
    client = MongoClient(uri, serverSelectionTimeoutMS=5000, tls=True, tlsAllowInvalidCertificates=True)
    db = client["smartcampus"]
    
    print("--------------------------------------------------")
    print(" MongoDB Database: smartcampus")
    print("--------------------------------------------------")
    
    collections = db.list_collection_names()
    print(f"Collections Found: {len(collections)}")
    for coll in collections:
        count = db[coll].count_documents({})
        print(f"- {coll.ljust(20)} | {count} documents")
        
    print("--------------------------------------------------")
except Exception as e:
    print(f"Connection Failed: {e}")
