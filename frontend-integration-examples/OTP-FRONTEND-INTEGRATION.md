# OTP (SMS / WhatsApp) — frontend integration

Backend sends a **6-digit code** to the user’s phone via **WhatsApp (Meta Cloud API)** or **SMS (MSG91)**. The code is **never** returned in API responses.

**Base URL:** use your API host (e.g. `https://api.example.com` or `http://localhost:8080`).

### Backend: SMS without MSG91 (local dev only)

If you see `SMS OTP is not configured`, either configure MSG91 for real SMS **or** use simulate mode (OTP appears in **server logs** at `WARN`, not on a phone):

1. **Profile `dev`:** run the API with `spring.profiles.active=dev` (IntelliJ: *Active profiles* = `dev`). This loads `application-dev.yml` with `otp.sms.simulate: true`.
2. **Or env:** `OTP_SMS_SIMULATE=true`

**Never** use simulate in production.

---

## Endpoints

| Method | Path | Purpose |
|--------|------|---------|
| `POST` | `/api/otp/send` | Request a new OTP |
| `POST` | `/api/otp/verify` | Submit the code the user entered |

All requests must use **`Content-Type: application/json`**.

---

## 1. Send OTP

### Request

**`POST /api/otp/send`**

```json
{
  "phone_number": "+919304683214",
  "channel": "whatsapp"
}
```

| Field | Required | Description |
|-------|----------|-------------|
| `phone_number` | Yes | Mobile number. May include `+` and spaces; backend normalizes (e.g. 10-digit India → `91…`). |
| `channel` | Yes | **`"whatsapp"`** or **`"sms"`** (case-insensitive). Omitting this causes `{"channel":"Channel is required"}`. |

### Success — `201 Created`

```json
{
  "status": "sent",
  "to": "+919304683214",
  "channel": "whatsapp"
}
```

The OTP is delivered only on the device (WhatsApp or SMS), not in this payload.

### Common errors

| HTTP | Example body | Meaning |
|------|----------------|---------|
| `400` | Field-keyed validation map, e.g. `{"phone_number":"Phone number is required","channel":"channel must be whatsapp or sms"}` | Invalid or missing JSON fields |
| `429` | `{"error":"Please wait before requesting another code."}` | Resend cooldown (default ~60s) |
| `503` | `{"error":"…"}` | Channel not configured (Meta WhatsApp, or MSG91 / dev simulate for SMS) |
| `502` | `{"error":"…"}` | Provider (e.g. MSG91/Meta) rejected the request |

---

## 2. Verify OTP

### Request

**`POST /api/otp/verify`**

```json
{
  "phone_number": "+919304683214",
  "otp": "123456"
}
```

| Field | Required | Description |
|-------|----------|-------------|
| `phone_number` | Yes | Same normalization as send; use the **same** number the user used for send. |
| `otp` | Yes | The 6-digit code (string is fine: `"012345"`). |

### Success — `200 OK`

```json
{
  "valid": true,
  "status": "approved"
}
```

### Wrong / expired code — still `200 OK` (check `valid`)

```json
{
  "valid": false,
  "status": "invalid"
}
```

Possible `status` values when `valid` is `false`:

- `invalid` — wrong code (or bad request)
- `expired_or_missing` — no active code or past TTL (~10 minutes by default)
- `too_many_attempts` — too many wrong tries (HTTP may be `429`)

---

## Frontend checklist

1. **Snake_case JSON keys:** use `phone_number`, not `phoneNumber`, unless you add your own mapping layer.
2. **Always send `channel` on `/api/otp/send`** — `"whatsapp"` or `"sms"`.
3. **CORS:** if the SPA is on another origin, the backend must allow that origin (already configured for common cases).
4. **Same phone string** for send and verify (normalization handles most formats; keep country code consistent).
5. **UX:** show cooldown after send if you get `429`; show “resend after X seconds” aligned with backend cooldown.

---

## Examples

### `fetch` (send + verify)

```javascript
const API = "http://localhost:8080";

async function sendOtp(phoneNumber, channel /* "whatsapp" | "sms" */) {
  const res = await fetch(`${API}/api/otp/send`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      phone_number: phoneNumber,
      channel: channel,
    }),
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.error || JSON.stringify(data));
  return data;
}

async function verifyOtp(phoneNumber, otp) {
  const res = await fetch(`${API}/api/otp/verify`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({
      phone_number: phoneNumber,
      otp: String(otp).trim(),
    }),
  });
  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data.error || JSON.stringify(data));
  return data; // { valid, status }
}
```

### Axios

```javascript
import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080",
  headers: { "Content-Type": "application/json" },
});

export async function sendOtp(phone_number, channel) {
  const { data } = await api.post("/api/otp/send", { phone_number, channel });
  return data;
}

export async function verifyOtp(phone_number, otp) {
  const { data } = await api.post("/api/otp/verify", { phone_number, otp });
  return data;
}
```

### React (minimal flow)

```tsx
const [phone, setPhone] = useState("");
const [channel, setChannel] = useState<"whatsapp" | "sms">("whatsapp");
const [otp, setOtp] = useState("");
const [msg, setMsg] = useState("");

async function onSend() {
  try {
    await sendOtp(phone, channel);
    setMsg("Code sent. Check your phone.");
  } catch (e) {
    setMsg(String(e));
  }
}

async function onVerify() {
  const r = await verifyOtp(phone, otp);
  if (r.valid) setMsg("Verified");
  else setMsg(`Not verified: ${r.status}`);
}
```

---

## Operational notes (for your team)

- **WhatsApp:** requires an approved Meta template with **one body variable** for the code; name/lang must match server config (`OTP_WHATSAPP_TEMPLATE`, etc.).
- **SMS:** requires MSG91 Flow template + env vars (`OTP_SMS_ENABLED`, `MSG91_AUTH_KEY`, `MSG91_FLOW_TEMPLATE_ID`, etc.).

If send returns **503**, the chosen channel is not configured on the server yet.
