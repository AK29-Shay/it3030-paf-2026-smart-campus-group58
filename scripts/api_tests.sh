#!/usr/bin/env bash
set -euo pipefail

BASE="${BASE:-http://localhost:8080}"
PYTHON_BIN="${PYTHON_BIN:-python}"
DIVIDER="============================================================"

json_value() {
  "$PYTHON_BIN" -c "import sys,json; print(json.load(sys.stdin).get('$1',''))"
}

call_json() {
  local label="$1"
  local command="$2"
  echo ""
  echo ">>> $label"
  printf '%s' "$command" | "$PYTHON_BIN" -c "import re,sys; print(re.sub(r'Bearer [A-Za-z0-9._-]+','Bearer <redacted>',sys.stdin.read()))"
  local output
  output=$(eval "$command")
  if ! echo "$output" | "$PYTHON_BIN" -m json.tool 2>/dev/null; then
    echo "$output"
  fi
}

redact_token() {
  "$PYTHON_BIN" -c "import json,sys; data=json.load(sys.stdin); data['token']='<redacted>'; print(json.dumps(data, indent=2))" 2>/dev/null || cat
}

login() {
  local email="$1"
  local role="$2"
  curl -s -X POST "$BASE/api/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$email\",\"password\":\"ChangeMe123!\",\"role\":\"$role\"}"
}

echo ""
echo "$DIVIDER"
echo "Smart Campus API Evidence"
date "+%Y-%m-%d %H:%M:%S"
echo "$DIVIDER"

ADMIN_LOGIN=$(login "admin@example.com" "ADMIN")
USER_LOGIN=$(login "student@example.com" "USER")
TECH_LOGIN=$(login "technician@example.com" "TECHNICIAN")

ADMIN_TOKEN=$(echo "$ADMIN_LOGIN" | json_value token)
USER_TOKEN=$(echo "$USER_LOGIN" | json_value token)
TECH_TOKEN=$(echo "$TECH_LOGIN" | json_value token)
ADMIN_LOGIN_REDACTED=$(echo "$ADMIN_LOGIN" | redact_token)

if [ -z "$ADMIN_TOKEN" ] || [ -z "$USER_TOKEN" ] || [ -z "$TECH_TOKEN" ]; then
  echo "Failed to obtain one or more JWT tokens."
  echo "$ADMIN_LOGIN"
  echo "$USER_LOGIN"
  echo "$TECH_LOGIN"
  exit 1
fi

ADMIN_AUTH="Authorization: Bearer $ADMIN_TOKEN"
USER_AUTH="Authorization: Bearer $USER_TOKEN"
TECH_AUTH="Authorization: Bearer $TECH_TOKEN"

call_json "GET /" "curl -s '$BASE/'"
call_json "POST /api/auth/login (ADMIN)" "printf '%s' '$ADMIN_LOGIN_REDACTED'"
call_json "GET /api/auth/me (USER)" "curl -s '$BASE/api/auth/me' -H '$USER_AUTH'"
call_json "GET /api/resources" "curl -s '$BASE/api/resources' -H '$USER_AUTH'"
call_json "GET /api/bookings (ADMIN)" "curl -s '$BASE/api/bookings' -H '$ADMIN_AUTH'"
call_json "GET /api/bookings/my-bookings (USER)" "curl -s '$BASE/api/bookings/my-bookings' -H '$USER_AUTH'"
call_json "GET /api/tickets (ADMIN)" "curl -s '$BASE/api/tickets' -H '$ADMIN_AUTH'"
call_json "GET /api/tickets/technician/{id} (TECHNICIAN)" "curl -s '$BASE/api/tickets/technician/3' -H '$TECH_AUTH'"
call_json "GET /api/notifications/unread/count (USER)" "curl -s '$BASE/api/notifications/unread/count' -H '$USER_AUTH'"
call_json "GET /api/admin/command-center (ADMIN)" "curl -s '$BASE/api/admin/command-center' -H '$ADMIN_AUTH'"

echo ""
echo "$DIVIDER"
echo "API evidence complete."
echo "$DIVIDER"
