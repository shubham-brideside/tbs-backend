# Deal creation — frontend integration guide

This document describes how the frontend should create deals with the **current simplified flow**: collect **name** and **phone number** only. The backend handles the rest (default category, optional fields).

---

## Base URL

| Environment | Example |
|-------------|---------|
| Local | `http://localhost:8080/api/deals` |
| Production | `https://<your-host>/api/deals` |

Set your env variable (e.g. `VITE_DEALS_API_URL`) to this base path **without** a trailing slash.

**Headers** (all requests):

- `Content-Type: application/json`
- `Accept: application/json`

---

## Overview

Deal creation is a **two-step** process:

| Step | Endpoint | When to call | Frontend sends |
|------|----------|--------------|----------------|
| 1 | `POST /init` | When phone is captured (e.g. after OTP) | `contact_number` |
| 2 | `PUT /{dealId}/details` | On final form submit | `name` |

You do **not** need to send `categories`, `event_date`, `venue`, `budget`, or `expected_gathering`. If omitted, the backend defaults the deal to category **Planning** (`category_id = 4`).

```
┌──────────────────┐   POST /init              ┌─────────────┐
│ Phone capture    │ ────────────────────────► │ Backend     │
│ (OTP / widget)   │   { contact_number }      │ deal id=42  │
└────────┬─────────┘                           └──────┬──────┘
         │                                            │
         │  Store dealId (42)                         │
         ▼                                            │
┌──────────────────┐   User enters name                │
│ Lead form        │                                   │
└────────┬─────────┘                                   │
         │                                             │
         │  PUT /42/details                            │
         │  { name }                                   │
         ▼                                             ▼
┌──────────────────┐                           Deal saved with
│ Success / redirect│                          name + phone + Planning
└──────────────────┘
```

---

## Step 1 — Initialize deal (`POST /init`)

Call this as soon as you have a valid phone number.

### Request

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
| `contact_number` | string | yes | Include country code, e.g. `+91` + 10 digits |

Extra fields (e.g. `name`) are **ignored** at this step. Do not rely on them being saved here.

### Success — `201 Created`

```json
{
  "deal_id": 42,
  "dealId": 42,
  "id": 42,
  "message": "Deal processed successfully with contact number: +919876543210"
}
```

**Parse the deal id** using any of the aliases (they are the same value):

```javascript
const dealId = data.id ?? data.deal_id ?? data.dealId;
```

Store `dealId` in URL state, session storage, or React state for step 2.

### Behaviour notes

- If a deal already exists for the same phone, the backend **reuses** that deal and refreshes its timestamp (returns the existing id).
- The deal is created with placeholder name `"TBS"` until step 2 runs.

### Errors

| Status | Cause |
|--------|--------|
| `400` | Missing or blank `contact_number` |
| `500` | Server error |

---

## Step 2 — Submit name (`PUT /{dealId}/details`)

Call this when the user submits the form with their name.

### Request

```http
PUT /api/deals/42/details
```

**Minimum payload** (recommended for the current form):

```json
{
  "name": "Priya Sharma"
}
```

| Field | Type | Required | Notes |
|-------|------|----------|--------|
| `name` | string | yes | User's full name |
| `categories` | array | no | Omit entirely; backend defaults to **Planning** |

### What the backend sets automatically

When `categories` is omitted or empty:

| Field | Value |
|-------|--------|
| Category name | `Planning` |
| `category_id` | `4` |
| `event_date` | `null` |
| `venue` | `null` |
| `budget` | `null` |
| `expected_gathering` | `null` |

Phone number comes from step 1 — **do not send** `contact_number` in this request.

### Success — `200 OK`

```json
{
  "id": 42,
  "userName": "Priya Sharma",
  "contactNumber": "+919876543210",
  "categoryId": 4,
  "eventDate": null,
  "venue": null,
  "budget": null,
  "expectedGathering": null,
  "createdAt": "2026-05-29 14:30:00",
  "updatedAt": "2026-05-29 14:35:00"
}
```

The UI usually only needs to confirm `res.ok`; the full body is optional to display.

### Errors

| Status | Cause |
|--------|--------|
| `404` | `dealId` does not exist |
| `400` | Deal was already fully configured (name is no longer `"TBS"`) |
| `400` | Validation failed (e.g. blank `name`) |
| `500` | Server error |

---

## Optional: send categories explicitly

If you later collect services on the form, you may send `categories`. When provided, they override the default.

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
    }
  ]
}
```

### Allowed category names

| `name` value | `category_id` |
|--------------|---------------|
| `Photography` | 1 |
| `Wedding Photography` | 1 |
| `Makeup` | 2 |
| `Planning and Decor` | 3 |
| `Planning & Decor` | 3 |
| `Planning` | 4 |

Category names are case-insensitive. You cannot pass `category_id` directly — the backend derives it from `name`.

### Optional category fields

| Field | Type | Notes |
|-------|------|--------|
| `venue` | string | Omit if empty |
| `budget` | number | ≥ 0; omit if not set |
| `event_date` | string | `YYYY-MM-DD`; omit if unknown |
| `expected_gathering` | string | e.g. `"250"` or `"100-300"` (max 64 chars) |

Multiple categories create **multiple deals** (one per category). The first updates the initialized deal; extras create new rows linked to the same phone.

---

## Complete JavaScript example

```javascript
const DEALS_BASE = import.meta.env.VITE_DEALS_API_URL; // e.g. http://localhost:8080/api/deals

const jsonHeaders = {
  'Content-Type': 'application/json',
  Accept: 'application/json',
};

async function initDeal(contactNumber) {
  const res = await fetch(`${DEALS_BASE}/init`, {
    method: 'POST',
    headers: jsonHeaders,
    body: JSON.stringify({ contact_number: contactNumber }),
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(`Deal init failed (${res.status}): ${text || res.statusText}`);
  }

  const data = await res.json();
  const dealId = data.id ?? data.deal_id ?? data.dealId;
  if (dealId == null) throw new Error('Init response missing deal id');
  return dealId;
}

async function submitDealDetails(dealId, name) {
  const res = await fetch(`${DEALS_BASE}/${dealId}/details`, {
    method: 'PUT',
    headers: jsonHeaders,
    body: JSON.stringify({ name }),
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(`Deal details failed (${res.status}): ${text || res.statusText}`);
  }

  return res.json();
}

/** Full flow: phone first, then name on submit */
async function createDeal(contactNumber, name) {
  const dealId = await initDeal(contactNumber);
  return submitDealDetails(dealId, name);
}
```

### Typical UI wiring

1. **After OTP / phone verified** → call `initDeal(phone)` and store `dealId`.
2. **On form submit** → call `submitDealDetails(dealId, name)`.
3. If the user submits **without** a prior init (no `dealId`), call `initDeal(phone)` first, then `submitDealDetails`.

---

## TypeScript types

```typescript
export interface DealInitPayload {
  contact_number: string;
}

export interface DealInitResponse {
  deal_id: number;
  dealId: number;
  id: number;
  message: string;
}

/** Current simplified form — name only */
export interface DealDetailsPayload {
  name: string;
  categories?: DealCategoryPayload[];
}

export type LeadCategoryName =
  | 'Photography'
  | 'Wedding Photography'
  | 'Makeup'
  | 'Planning and Decor'
  | 'Planning & Decor'
  | 'Planning';

export interface DealCategoryPayload {
  name: LeadCategoryName;
  venue?: string;
  budget?: number;
  event_date?: string; // YYYY-MM-DD
  expected_gathering?: string;
}

export interface DealResponse {
  id: number;
  userName: string;
  contactNumber: string;
  categoryId: number;
  eventDate: string | null;
  venue: string | null;
  budget: string | null;
  expectedGathering: string | null;
  createdAt: string;
  updatedAt: string;
}
```

---

## Legacy single-request endpoint

`POST /api/deals` still exists for older clients. It accepts `name`, `contact_number`, and optional `categories` in one call.

For the **current frontend**, prefer **`POST /init` + `PUT /{id}/details`**. The legacy endpoint is not required for the name + phone flow.

---

## CORS

If the browser calls the API from a different origin (e.g. Vite on `localhost:5173`), ensure the backend allows that origin. See `CORS-FIX-LOCALHOST-5173.md` and `CORS-TROUBLESHOOTING.md` in this repo if preflight requests fail.

---

## Quick checklist

- [ ] Call `POST /init` when phone is available
- [ ] Persist `dealId` until form submit
- [ ] Call `PUT /{dealId}/details` with `{ name }` only
- [ ] Do not send `categories` unless the user selects services
- [ ] Do not send `contact_number` in step 2
- [ ] Handle `400` if step 2 is called twice on the same deal
