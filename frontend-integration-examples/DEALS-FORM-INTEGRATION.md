# Deals API — integrating after the planning form

This guide describes how to wire **lead capture** to the backend **after** the user finishes the planning form (name, services, venues, budgets, etc.). The backend uses a **two-step** model: register the phone first, then attach full details to that deal.

---

## Base URL

| Environment | Example |
|-------------|---------|
| Local | `http://localhost:8080/api/deals` |
| Production | `https://<your-host>/api/deals` |

Configure the same value your app uses for `VITE_DEALS_API_URL` (or equivalent), **without** a trailing slash.

**Headers** (both steps):

- `Content-Type: application/json`
- `Accept: application/json`

---

## End-to-end flow

```
┌─────────────────────┐     POST /init          ┌──────────────┐
│ Floating widget /   │ ──────────────────────► │ Backend      │
│ early phone capture │   { contact_number }    │ creates deal │
└─────────────────────┘                         └──────┬───────┘
         │                                               │
         │  Read id from response (see below)             │
         ▼                                               │
┌─────────────────────┐     User completes form          │
│ /start-planning     │ ◄────────────────────────────────┘
│ ?dealId=…&phone=…   │
└──────────┬──────────┘
           │
           │  Final submit
           ▼
┌─────────────────────┐     PUT /{dealId}/details      ┌──────────────┐
│ Planning form       │ ─────────────────────────────► │ Backend      │
│ (name + categories) │   { name, categories[] }        │ updates deal │
└─────────────────────┘                                 └──────────────┘
```

1. **Step A — `POST /init`**  
   Call when you have a valid phone (e.g. after OTP or floating button).  
   Response includes a deal identifier; store it and pass **`dealId`** in the URL (or state) for the planning form.

2. **Step B — `PUT /{dealId}/details`**  
   Call on **final form submit** when `dealId` is present.  
   Send the user’s **name** and one **category** object per selected service.

3. **Fallback — no `dealId`**  
   If the user reaches the final step **without** a prior init (no `dealId` in the URL), the app typically calls **`POST /init`** again with **`contact_number`** only. That **does not** send name or categories. To persist full lead data in one request, you would need either a frontend change (always run init first) or a dedicated backend endpoint (not the default contract).

---

## Step A: `POST /init`

**Request**

```http
POST /api/deals/init
```

```json
{
  "contact_number": "+919876543210"
}
```

| Field | Type | Required | Notes |
|-------|------|----------|--------|
| `contact_number` | string | yes | E.g. `+91` + 10 digits. Backend validates presence. |

**Success (201)** — read **any** of these for the deal id (they are the same value):

```json
{
  "deal_id": 42,
  "dealId": 42,
  "id": 42,
  "message": "Deal processed successfully with contact number: +919876543210"
}
```

**Frontend checklist**

- Parse: `data.id ?? data.deal_id ?? data.dealId`
- Redirect or navigate: `/start-planning?dealId=<id>&phone=<encoded-phone>` (query shape is up to your app; only `dealId` is required for step B).

**Typical errors**

- **400** — missing/blank `contact_number` (validation).
- **500** — server error; show a generic message and optional retry.

---

## Step B: `PUT /{dealId}/details`

**Request**

```http
PUT /api/deals/42/details
```

Path segment **`42`** is the integer from step A.

```json
{
  "name": "Priya Sharma",
  "categories": [
    {
      "name": "Photography",
      "venue": "Mumbai",
      "budget": 500000,
      "event_date": "2026-11-22",
      "expected_gathering": "100-300"
    },
    {
      "name": "Makeup",
      "venue": "Mumbai",
      "budget": 150000,
      "event_date": "2026-11-22",
      "expected_gathering": "100-300"
    }
  ]
}
```

| Field | Type | Required | Notes |
|-------|------|----------|--------|
| `name` | string | yes | Full name from the final step. |
| `categories` | array | yes | At least one item. |

**Each category object**

| Field | Type | Required | Notes |
|-------|------|----------|--------|
| `name` | string | yes | Must be **exactly** one of: `Photography`, `Makeup`, `Planning & Decor`. |
| `venue` | string | no | City or free text; omit if empty. |
| `budget` | number | no | INR; omit if not set. |
| `event_date` | string | no | `YYYY-MM-DD`; omit if date not confirmed. |
| `expected_gathering` | string | no | Guest count or range, e.g. `"250"` or `"100-300"` (max 64 chars). A JSON number is also accepted. |

**Success (200)** — body is the updated deal DTO (includes `id`, `user_name`, `contact_number`, `category`, etc.). The UI usually only needs to know the request succeeded.

**Typical errors**

- **404** — `dealId` does not exist.
- **400** — deal already fully updated, invalid category name, or validation failure (e.g. `expected_gathering` longer than 64 characters).
- **500** — server error.

---

## Example: `fetch` after form submit

Replace `DEALS_BASE` with your configured base URL (e.g. `http://localhost:8080/api/deals`).

```javascript
async function submitPlanningDetails(dealId, name, categories) {
  const res = await fetch(`${DEALS_BASE}/${dealId}/details`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
    },
    body: JSON.stringify({
      name,
      categories: categories.map((c) => {
        const row = { name: c.name };
        if (c.venue) row.venue = c.venue;
        if (c.budget != null && c.budget !== '') row.budget = Number(c.budget);
        if (c.event_date) row.event_date = c.event_date;
        if (c.expected_gathering != null && c.expected_gathering !== '')
          row.expected_gathering = String(c.expected_gathering);
        return row;
      }),
    }),
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(`Deals API ${res.status}: ${text || res.statusText}`);
  }

  return res.json();
}
```

**Omit empty optional fields** so the backend does not receive `""` for numbers or dates unless you intentionally support that.

---

## Example: init from phone (widget or form entry)

```javascript
async function initDealFromPhone(contactNumber) {
  const res = await fetch(`${DEALS_BASE}/init`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
    },
    body: JSON.stringify({ contact_number: contactNumber }),
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(`Deals init ${res.status}: ${text || res.statusText}`);
  }

  const data = await res.json();
  const dealId = data.id ?? data.deal_id ?? data.dealId;
  if (dealId == null) throw new Error('Init response missing deal id');
  return dealId;
}
```

---

## TypeScript types (reference)

```typescript
export type LeadCategoryName = 'Photography' | 'Makeup' | 'Planning & Decor';

export interface DealCategoryPayload {
  name: LeadCategoryName;
  venue?: string;
  budget?: number;
  event_date?: string; // YYYY-MM-DD
  expected_gathering?: string;
}

export interface DealDetailsPayload {
  name: string;
  categories: DealCategoryPayload[];
}

export interface DealInitPayload {
  contact_number: string;
}
```

---

## CORS

If the browser calls the API from a different origin (e.g. Vite on port 5173), the Spring app must allow that origin. See `CORS-FIX-LOCALHOST-5173.md` and `CORS-TROUBLESHOOTING.md` in this repo if requests fail with CORS errors.

---

## Related

- Full endpoint list and blog/page-view contracts: keep your generated **`tbs-frontend` API reference** in sync when the UI changes.
- Legacy **`POST /api/deals`** (single request with contact + categories) may still exist for older clients; the **recommended** flow for the new form is **`/init` + `PUT .../details`**.
