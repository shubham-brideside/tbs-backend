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

Deal creation is a **two-step** process, but step 2 is **conditional**:

| Step | Endpoint | When to call | Frontend sends |
|------|----------|--------------|----------------|
| 1 | `POST /init` | When phone is captured (e.g. after OTP) | `contact_number` |
| 2 | `PUT /{dealId}/details` | **Only if** `requires_details === true` | `name` |

You do **not** need to send `categories`, `event_date`, `venue`, `budget`, or `expected_gathering`. If omitted, the backend defaults the deal to category **Planning** (`category_id = 4`).

### Decision rule (important)

After `POST /init`, read `requires_details` from the response:

| `requires_details` | What to do |
|--------------------|------------|
| `true` | User still needs to submit their name → call `PUT /{dealId}/details` on form submit |
| `false` | Deal already has a real name for this phone → **skip** `/details`, show success / redirect |

```
┌──────────────────┐   POST /init              ┌─────────────┐
│ Phone capture    │ ────────────────────────► │ Backend     │
│ (OTP / widget)   │   { contact_number }      │ deal + flags│
└────────┬─────────┘                           └──────┬──────┘
         │                                            │
         │  Store dealId + requires_details           │
         ▼                                            │
┌──────────────────┐                                  │
│ Lead form        │                                  │
└────────┬─────────┘                                  │
         │                                            │
         │  requires_details === true?                │
         ├──── yes ──► PUT /{dealId}/details { name } │
         │                                            │
         └──── no  ──► Skip details; deal is ready    ▼
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

**New deal (placeholder name `"TBS"`):**

```json
{
  "id": 42,
  "deal_id": 42,
  "dealId": 42,
  "is_new_deal": true,
  "already_configured": false,
  "requires_details": true,
  "message": "Deal initialized successfully with contact number: +919876543210"
}
```

**Existing deal reused — name already submitted (skip step 2):**

```json
{
  "id": 43765,
  "deal_id": 43765,
  "dealId": 43765,
  "is_new_deal": false,
  "already_configured": true,
  "requires_details": false,
  "message": "Existing deal reused for contact number: +919876543210. Details step not required."
}
```

**Existing deal reused — still placeholder `"TBS"` (call step 2):**

```json
{
  "id": 5,
  "deal_id": 5,
  "dealId": 5,
  "is_new_deal": false,
  "already_configured": false,
  "requires_details": true,
  "message": "Existing deal reused for contact number: +919876543210. Submit name via PUT /details."
}
```

### Response fields

| Field | Type | Meaning |
|-------|------|---------|
| `id` / `deal_id` / `dealId` | number | Same deal id — use any alias |
| `is_new_deal` | boolean | `true` if a new deal row was created |
| `already_configured` | boolean | `true` if deal name is not the placeholder `"TBS"` |
| `requires_details` | boolean | **`true` → call `/details`; `false` → skip** |
| `message` | string | Human-readable status |

**Parse the deal id:**

```javascript
const dealId = data.id ?? data.deal_id ?? data.dealId;
const needsDetails = data.requires_details === true;
```

Store `dealId` and `requires_details` until form submit.

### Behaviour notes

- Reuses the latest deal for the same phone when it is **active landing-page** (`IN_PROGRESS`, `LANDING_PAGE`, not deleted).
- Otherwise creates a new deal (e.g. previous deal was deleted, WON/LOST, or from WhatsApp).
- New deals start with placeholder name `"TBS"` until step 2 runs.

### Errors

| Status | Cause |
|--------|--------|
| `400` | Missing or blank `contact_number` |
| `500` | Server error |

---

## Step 2 — Submit name (`PUT /{dealId}/details`)

Call this **only when** `requires_details === true` from step 1.

If the user returns with the same phone and already submitted their name before, init returns `requires_details: false` — **do not call this endpoint** (avoids unnecessary requests and errors).

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
| `400` | Deal is not an active landing-page deal (wrong status, sub-source, or deleted) |
| `400` | Validation failed (e.g. blank `name`) |
| `500` | Server error |

Active landing-page deals (`IN_PROGRESS` + `LANDING_PAGE`) **can** be updated even if the name is no longer `"TBS"` (e.g. user resubmits the form). Prefer skipping `/details` when init says `requires_details: false`.

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

  return {
    dealId,
    isNewDeal: data.is_new_deal === true,
    alreadyConfigured: data.already_configured === true,
    requiresDetails: data.requires_details === true,
    message: data.message,
  };
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

/** Full flow: phone first, then name on submit (skips details when not required) */
async function createDeal(contactNumber, name) {
  const init = await initDeal(contactNumber);

  if (!init.requiresDetails) {
    return { dealId: init.dealId, skippedDetails: true, message: init.message };
  }

  const deal = await submitDealDetails(init.dealId, name);
  return { dealId: init.dealId, skippedDetails: false, deal };
}
```

### Typical UI wiring

1. **After OTP / phone verified** → call `initDeal(phone)` and store `{ dealId, requiresDetails }`.
2. **On form submit**:
   - If `requiresDetails === false` → show success / redirect (no API call).
   - If `requiresDetails === true` → call `submitDealDetails(dealId, name)`.
3. If the user submits **without** a prior init (no stored state), call `initDeal(phone)` first, then follow step 2.

### Example: guard before calling `/details`

```javascript
async function onFormSubmit(phone, name, storedInit) {
  let init = storedInit;

  if (!init?.dealId) {
    init = await initDeal(phone);
  }

  if (!init.requiresDetails) {
    // Returning user — deal already has their name
    return { success: true, dealId: init.dealId };
  }

  await submitDealDetails(init.dealId, name);
  return { success: true, dealId: init.dealId };
}
```

---

## TypeScript types

```typescript
export interface DealInitPayload {
  contact_number: string;
}

export interface DealInitResponse {
  id: number;
  deal_id: number;
  dealId: number;
  is_new_deal: boolean;
  already_configured: boolean;
  requires_details: boolean;
  message: string;
}

/** Parsed init result for app state */
export interface DealInitResult {
  dealId: number;
  isNewDeal: boolean;
  alreadyConfigured: boolean;
  requiresDetails: boolean;
  message?: string;
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

For the **current frontend**, prefer **`POST /init` + conditional `PUT /{id}/details`**. The legacy endpoint is not required for the name + phone flow.

---

## CORS

If the browser calls the API from a different origin (e.g. Vite on `localhost:5173`), ensure the backend allows that origin. See `CORS-FIX-LOCALHOST-5173.md` and `CORS-TROUBLESHOOTING.md` in this repo if preflight requests fail.

---

## Quick checklist

- [ ] Call `POST /init` when phone is available
- [ ] Persist `dealId` and `requires_details` until form submit
- [ ] Call `PUT /{dealId}/details` **only when** `requires_details === true`
- [ ] Send `{ name }` only in step 2 (no `categories` unless user selects services)
- [ ] Do not send `contact_number` in step 2
- [ ] When `requires_details === false`, show success without calling `/details`
