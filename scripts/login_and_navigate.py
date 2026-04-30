#!/usr/bin/env python3
import json
import urllib.request
import urllib.error
from urllib.parse import quote

# Login to backend
login_url = "http://localhost:8080/api/auth/login"
login_data = {
    "email": "admin@example.com",
    "password": "ChangeMe123!",
    "role": "ADMIN"
}

try:
    req = urllib.request.Request(
        login_url,
        data=json.dumps(login_data).encode('utf-8'),
        headers={'Content-Type': 'application/json'}
    )
    with urllib.request.urlopen(req) as response:
        data = json.loads(response.read().decode('utf-8'))
        token = data.get('token')
        print(f"Login successful!")
        print(f"Token: {token[:50]}...")
        # Save token for use in other requests
        with open('admin_token.txt', 'w') as f:
            f.write(token)
        print("\nSaved token to admin_token.txt")
        print(f"\nFrontend login URL:")
        print(f"http://localhost:5173/login?token={quote(token)}")
except urllib.error.HTTPError as e:
    print(f"Login failed: {e.code}")
    print(e.read().decode('utf-8'))
except Exception as e:
    print(f"Error: {e}")
