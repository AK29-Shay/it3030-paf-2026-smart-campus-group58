#!/bin/bash
# ============================================================
#  Smart Campus Operations Hub — API Evidence (curl tests)
#  Group 58 | IT3030 PAF 2026
#  Prerequisites: backend running on localhost:8080
#  Run: bash scripts/api_tests.sh
# ============================================================

BASE="http://localhost:8080"
DIVIDER="============================================================"

echo ""
echo "$DIVIDER"
echo "  Smart Campus API Test Evidence"
echo "  $(date '+%Y-%m-%d %H:%M:%S')"
echo "$DIVIDER"

# ── Helpers ────────────────────────────────────────────────
hit() {
  echo ""
  echo "► $1"
  echo "  $2"
  echo ""
  eval "$3" | python3 -m json.tool 2>/dev/null || eval "$3"
  echo ""
}

# ── 1. Health ───────────────────────────────────────────────
echo ""
echo "=== [1] Health Check ==="
hit "GET /actuator/health" \
    "Verify the Spring Boot server is running" \
    "curl -s $BASE/actuator/health"

# ── 2. Auth — Signup ────────────────────────────────────────
echo "=== [2] Auth — Signup (POST /api/auth/signup) ==="
SIGNUP=$(curl -s -X POST "$BASE/api/auth/signup" \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Evidence User","email":"evidence_'$(date +%s)'@test.com","password":"Evidence@123"}')
echo "$SIGNUP" | python3 -m json.tool 2>/dev/null || echo "$SIGNUP"
TOKEN=$(echo "$SIGNUP" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('token',''))" 2>/dev/null)

# ── 3. Auth — Login ─────────────────────────────────────────
echo ""
echo "=== [3] Auth — Login (POST /api/auth/login) ==="
LOGIN=$(curl -s -X POST "$BASE/api/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"student@example.com","password":"ChangeMe123!"}')
echo "$LOGIN" | python3 -m json.tool 2>/dev/null || echo "$LOGIN"
if [ -z "$TOKEN" ]; then
  TOKEN=$(echo "$LOGIN" | python3 -c "import sys,json; d=json.load(sys.stdin); print(d.get('token',''))" 2>/dev/null)
fi
echo "  [JWT token obtained: ${#TOKEN} chars]"

AUTH="Authorization: Bearer $TOKEN"

# ── 4. Auth — Get Me ────────────────────────────────────────
echo ""
echo "=== [4] Auth — GET /api/auth/me ==="
curl -s "$BASE/api/auth/me" -H "$AUTH" | python3 -m json.tool 2>/dev/null

# ── 5. Resources — GET ALL ──────────────────────────────────
echo ""
echo "=== [5] Resources — GET /api/resources ==="
curl -s "$BASE/api/resources" -H "$AUTH" | python3 -m json.tool 2>/dev/null

# ── 6. Resources — GET BY TYPE ──────────────────────────────
echo ""
echo "=== [6] Resources — GET /api/resources/type/ROOM ==="
curl -s "$BASE/api/resources/type/ROOM" -H "$AUTH" | python3 -m json.tool 2>/dev/null

# ── 7. Bookings — GET My Bookings ───────────────────────────
echo ""
echo "=== [7] Bookings — GET /api/bookings/my-bookings ==="
curl -s "$BASE/api/bookings/my-bookings" -H "$AUTH" | python3 -m json.tool 2>/dev/null

# ── 8. Bookings — Availability Check ────────────────────────
echo ""
echo "=== [8] Bookings — GET /api/bookings/availability ==="
curl -s "$BASE/api/bookings/availability?resourceName=LH1&start=2026-05-01T09:00:00&end=2026-05-01T10:00:00" \
  -H "$AUTH" | python3 -m json.tool 2>/dev/null

# ── 9. Tickets — GET My Tickets ─────────────────────────────
echo ""
echo "=== [9] Tickets — GET /api/tickets ==="
curl -s "$BASE/api/tickets" -H "$AUTH" | python3 -m json.tool 2>/dev/null

# ── 10. Notifications — GET Unread Count ────────────────────
echo ""
echo "=== [10] Notifications — GET /api/notifications/unread/count ==="
curl -s "$BASE/api/notifications/unread/count" -H "$AUTH" | python3 -m json.tool 2>/dev/null

echo ""
echo "$DIVIDER"
echo "  ✅  All API tests complete — screenshot this terminal for evidence."
echo "$DIVIDER"
echo ""
