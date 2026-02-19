# Reviews & Ratings API – UI Integration Guide

Base URL: **`/api/v1/reviews`**  
Example: `http://localhost:8080/api/v1/reviews`

---

## Authentication

| Endpoint | Required |
|----------|----------|
| **POST** – Submit a review | Yes – authenticated user (tourist of the booking) |
| **GET** – List reviews by tour or guide | No (public) |
| **GET** `/rating` – Overall rating for tour or guide | No (public) |

Send the JWT in the header for protected endpoints:

```http
Authorization: Bearer <your_jwt_token>
```

---

## Overview

Reviews are tied to **completed bookings** (tours). Only the **tourist** who completed a booking can submit one review per booking. Reviews apply to both the **tour** and the **guide**; you can list reviews and get overall ratings by `tourId` or by `guideId`.

---

# Reviews API

## 1. Submit a review

**POST** `/api/v1/reviews`

Creates a rating and optional comment for a completed booking. Only the tourist of that booking can submit; one review per booking.

### Request body

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `bookingId` | number | Yes | ID of the **completed** booking. |
| `rating` | number | Yes | Integer 1–5 (1 = worst, 5 = best). |
| `comment` | string | No | Optional text; max **2000** characters. |

### Example request

```http
POST /api/v1/reviews
Authorization: Bearer <your_jwt_token>
Content-Type: application/json

{
  "bookingId": 42,
  "rating": 5,
  "comment": "Amazing tour and guide. Highly recommend!"
}
```

### Response: 201 Created

```json
{
  "id": 101,
  "rating": 5,
  "comment": "Amazing tour and guide. Highly recommend!",
  "touristName": "Jane Doe",
  "isVerified": false,
  "createdAt": "2026-02-18T14:30:00",
  "tourId": 7,
  "guideId": 3
}
```

**Response fields:**

| Field | Type | Description |
|-------|------|-------------|
| `id` | number | Review ID. |
| `rating` | number | Rating (1–5). |
| `comment` | string \| null | Review text. |
| `touristName` | string | Display name of the reviewer (e.g. "First Last"). |
| `isVerified` | boolean | Whether the review is verified (e.g. confirmed booking). |
| `createdAt` | string | ISO-8601 date-time. |
| `tourId` | number | Tour this review is for. |
| `guideId` | number | Guide this review is for. |

### Error responses

| Status | Meaning |
|--------|---------|
| **400** | Invalid input (e.g. missing `bookingId`/`rating`, rating not 1–5, comment &gt; 2000 chars). |
| **409** | User has already submitted a review for this booking. |
| **401** | Not authenticated. |
| **403** | Not the tourist for this booking, or booking not found / not completed. |

---

## 2. List reviews (by tour or guide)

**GET** `/api/v1/reviews`

Returns paginated reviews. You must provide **either** `tourId` **or** `guideId` (not both required at once; one is required).

### Query parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `tourId` | number | One of tourId/guideId | Filter by tour ID. |
| `guideId` | number | One of tourId/guideId | Filter by guide ID. |
| `page` | number | No | Page index (0-based). Default: `0`. |
| `size` | number | No | Page size. Default: `10`. |

Reviews are sorted by **newest first** (`createdAt` descending).

### Example requests

```http
GET /api/v1/reviews?tourId=7&page=0&size=10
```

```http
GET /api/v1/reviews?guideId=3&page=0&size=20
```

### Response: 200 OK

Paginated response (Spring Page):

```json
{
  "content": [
    {
      "id": 101,
      "rating": 5,
      "comment": "Amazing tour and guide. Highly recommend!",
      "touristName": "Jane Doe",
      "isVerified": false,
      "createdAt": "2026-02-18T14:30:00",
      "tourId": 7,
      "guideId": 3
    },
    {
      "id": 99,
      "rating": 4,
      "comment": "Great experience.",
      "touristName": "John Smith",
      "isVerified": true,
      "createdAt": "2026-02-15T09:00:00",
      "tourId": 7,
      "guideId": 3
    }
  ],
  "pageable": { "pageNumber": 0, "pageSize": 10, "sort": { "sorted": true, "unsorted": false, "empty": false }, "offset": 0, "paged": true, "unpaged": false },
  "totalPages": 1,
  "totalElements": 2,
  "last": true,
  "first": true,
  "size": 10,
  "number": 0,
  "numberOfElements": 2,
  "empty": false
}
```

**Content item fields:** same as in [Submit a review](#1-submit-a-review) response.

### Error responses

| Status | Meaning |
|--------|---------|
| **400** | Neither `tourId` nor `guideId` provided (or invalid query). |

---

## 3. Get overall rating (tour or guide)

**GET** `/api/v1/reviews/rating`

Returns the **overall** average rating and total count for a tour or a guide, plus an optional star breakdown. You must provide **either** `tourId` **or** `guideId`.

### Query parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `tourId` | number | One of tourId/guideId | Get rating for this tour. |
| `guideId` | number | One of tourId/guideId | Get rating for this guide. |

### Example requests

```http
GET /api/v1/reviews/rating?tourId=7
```

```http
GET /api/v1/reviews/rating?guideId=3
```

### Response: 200 OK

```json
{
  "averageRating": 4.65,
  "totalCount": 23,
  "ratingBreakdown": {
    "1": 0,
    "2": 1,
    "3": 2,
    "4": 8,
    "5": 12
  }
}
```

**Response fields:**

| Field | Type | Description |
|-------|------|-------------|
| `averageRating` | number | Average rating (1–5), rounded to 2 decimal places. `0` if no reviews. |
| `totalCount` | number | Total number of reviews. |
| `ratingBreakdown` | object | Count per star: keys `1`–`5`, values are counts. Use for star-bars or distribution UI. |

### Error responses

| Status | Meaning |
|--------|---------|
| **400** | Neither `tourId` nor `guideId` provided. |

---

# UI integration notes

- **Tour detail page:** Use `GET /reviews?tourId={id}` for the review list and `GET /reviews/rating?tourId={id}` for the summary (average + count + breakdown).
- **Guide profile page:** Use `GET /reviews?guideId={id}` and `GET /reviews/rating?guideId={id}` the same way.
- **Submit review:** Show the “Write a review” form only for the tourist of a **completed** booking; send `POST /reviews` with `bookingId`, `rating`, and optional `comment`. Handle **409** by showing “You have already reviewed this booking.”
- **Display:** Use `touristName` for “Reviewed by …”, `createdAt` for date, `rating` for stars, and `ratingBreakdown` from the rating endpoint for a 1–5 star distribution chart or bars.
