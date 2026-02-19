# Create New Tour API – UI Integration

Base URL: **`/api/v1/tours`**  
Example: `http://localhost:8080/api/v1/tours`

---

## Authentication

Creating a tour requires an authenticated user with role **GUIDE** or **ADMIN**. Send the JWT in the header:

```http
Authorization: Bearer <your_jwt_token>
```

- **GUIDE**: The tour is created for the authenticated user’s guide profile. The user must have a guide profile; otherwise the request fails.
- **ADMIN**: Can create tours on behalf of a guide (implementation may tie to a specific guide; confirm backend behavior for admin-created tours).

---

## Overview

- **Guides** create tours (title, description, category, duration, price, options, images, etc.).
- The created tour is **active** by default and appears in public listing (e.g. verified tours) according to backend rules.
- Images can be provided as a list of URLs; upload images first (e.g. via your file-upload API), then pass the returned URLs in `imageUrls`. One image can be marked as primary via `primaryImageIndex`.

---

# Create New Tour

## 1. Create a tour

**POST** `/api/v1/tours`

Creates a new tour for the authenticated guide (or admin). Request body must be valid JSON; validation errors return **400**.

### Request body

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `title` | string | Yes | Tour title. Length **3–200** characters. |
| `description` | string | Yes | Full description. Length **10–2000** characters. |
| `category` | string | Yes | One of: `TEA_TOURS`, `BEACH_TOURS`, `CULTURAL_TOURS`, `ADVENTURE_TOURS`, `FOOD_TOURS`, `WILDLIFE_TOURS`. |
| `durationHours` | number | Yes | Duration in hours. **1–168** (1 week max). |
| `maxGroupSize` | number | No | Max group size. Default **10**. **1–50**. |
| `pricePerPerson` | number | Yes | Price per person. **0.01–10000**. |
| `instantBooking` | boolean | No | Whether instant booking is allowed. Default **false**. |
| `securePayment` | boolean | No | Whether secure payment is offered. Default **true**. |
| `languages` | string[] | No | List of languages (max **10**). |
| `highlights` | string[] | No | List of highlights (max **20**). |
| `includedItems` | string[] | No | What’s included (max **30**). |
| `excludedItems` | string[] | No | What’s excluded (max **30**). |
| `meetingPoint` | string | No | Meeting point description (max **500**). |
| `cancellationPolicy` | string | No | Cancellation policy text (max **1000**). |
| `imageUrls` | string[] | No | List of image URLs (max **10**). Upload images first, then pass URLs here. |
| `primaryImageIndex` | number | No | Index in `imageUrls` for the primary image (0-based). Default **0**. |

### Example request

```http
POST /api/v1/tours
Authorization: Bearer <your_jwt_token>
Content-Type: application/json

{
  "title": "Sunset Sail & Tea Tasting",
  "description": "A relaxing evening sail along the coast followed by a curated tea tasting with local snacks. Perfect for small groups.",
  "category": "TEA_TOURS",
  "durationHours": 4,
  "maxGroupSize": 8,
  "pricePerPerson": 65.00,
  "instantBooking": true,
  "securePayment": true,
  "languages": ["English", "Spanish"],
  "highlights": ["Sunset views", "Tea tasting", "Small group"],
  "includedItems": ["Tea and snacks", "Life jackets", "Guide"],
  "excludedItems": ["Transport to harbour"],
  "meetingPoint": "Harbour Pier 2, next to the blue kiosk",
  "cancellationPolicy": "Free cancellation up to 24 hours before start.",
  "imageUrls": [
    "https://your-cdn.com/tours/sunset-sail-1.jpg",
    "https://your-cdn.com/tours/tea-tasting-1.jpg"
  ],
  "primaryImageIndex": 0
}
```

### Response: 201 Created

Returns the created tour as `TourResponse`:

```json
{
  "id": 42,
  "title": "Sunset Sail & Tea Tasting",
  "description": "A relaxing evening sail along the coast...",
  "category": "TEA_TOURS",
  "durationHours": 4,
  "maxGroupSize": 8,
  "pricePerPerson": 65.00,
  "instantBooking": true,
  "securePayment": true,
  "languages": ["English", "Spanish"],
  "highlights": ["Sunset views", "Tea tasting", "Small group"],
  "includedItems": ["Tea and snacks", "Life jackets", "Guide"],
  "excludedItems": ["Transport to harbour"],
  "meetingPoint": "Harbour Pier 2, next to the blue kiosk",
  "cancellationPolicy": "Free cancellation up to 24 hours before start.",
  "isActive": true,
  "createdAt": "2026-02-19T10:00:00",
  "updatedAt": "2026-02-19T10:00:00",
  "guide": {
    "id": 5,
    "firstName": "Jane",
    "lastName": "Guide",
    "profilePictureUrl": "https://...",
    "averageRating": 4.8,
    "totalTours": 3,
    "isVerified": true
  },
  "images": [
    {
      "id": 101,
      "imageUrl": "https://your-cdn.com/tours/sunset-sail-1.jpg",
      "isPrimary": true,
      "altText": null,
      "createdAt": "2026-02-19T10:00:00"
    },
    {
      "id": 102,
      "imageUrl": "https://your-cdn.com/tours/tea-tasting-1.jpg",
      "isPrimary": false,
      "altText": null,
      "createdAt": "2026-02-19T10:00:00"
    }
  ],
  "totalBookings": 0,
  "averageRating": null,
  "totalReviews": 0
}
```

**Response fields (summary):**

| Field | Type | Description |
|-------|------|-------------|
| `id` | number | Tour ID (use for update, delete, or add/remove images). |
| `title`, `description`, `category`, etc. | — | Echo of created data. |
| `isActive` | boolean | Always `true` for newly created tours. |
| `createdAt`, `updatedAt` | string | ISO-8601 date-time. |
| `guide` | object | Guide id, firstName, lastName, profilePictureUrl, averageRating, totalTours, isVerified. |
| `images` | array | List of image objects (id, imageUrl, isPrimary, altText, createdAt). |
| `totalBookings`, `averageRating`, `totalReviews` | number | Statistics (0 for new tours). |

### Error responses

| Status | Meaning |
|--------|--------|
| **400** | Invalid input (e.g. validation: title/description length, category, duration/price bounds). |
| **401** | Missing or invalid JWT. |
| **403** | Forbidden – user is not GUIDE or ADMIN, or has no guide profile (for GUIDE). |
| **404** | Not typically returned on create; backend may return if “Guide profile not found”. |

---

## Tour categories (enum)

Use exactly these values for `category`:

| Value | Description |
|-------|-------------|
| `TEA_TOURS` | Tea-focused experiences |
| `BEACH_TOURS` | Beach and coastal tours |
| `CULTURAL_TOURS` | Cultural and heritage |
| `ADVENTURE_TOURS` | Adventure activities |
| `FOOD_TOURS` | Food and dining |
| `WILDLIFE_TOURS` | Wildlife and nature |

---

## UI integration notes

1. **Auth**: Ensure the user is logged in as GUIDE (or ADMIN) and send `Authorization: Bearer <token>` on every request.
2. **Images**: If your app allows tour images:
   - Upload each image via your file-upload API first.
   - Collect the returned URLs and send them in `imageUrls`.
   - Set `primaryImageIndex` to the index of the cover image (0-based).
3. **Validation**: Implement client-side checks for title/description length, category, duration (1–168), price (0.01–10000), and list size limits to avoid 400 errors.
4. **After create**: On **201**, use the response `id` to navigate to the tour detail or edit screen, or to add more images via `POST /api/v1/tours/{id}/images` if needed.
5. **Optional fields**: You can omit optional fields or send `null`/empty arrays; defaults (e.g. `maxGroupSize: 10`, `instantBooking: false`, `securePayment: true`) are applied by the backend.
