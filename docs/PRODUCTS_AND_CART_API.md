# Products & Shopping Cart API – UI Integration Guide

Base URL: **`/api/v1`**  
Example: `http://localhost:8080/api/v1`

---

## Authentication

| Context | Required |
|--------|----------|
| **Products – list, get by ID, best-sellers, by category** | No (public) |
| **Products – create, update, delete** | Yes – **ADMIN** only |
| **Cart – all endpoints** | Yes – **USER**, **GUIDE**, or **ADMIN** |

Send the JWT in the header:

```http
Authorization: Bearer <your_jwt_token>
```

---

## Product categories (slugs)

Use these **lowercase** values in query params and request bodies.

| Slug | Display name |
|------|----------------|
| `all` | All Products (filter only – omit or use `all` for no category filter) |
| `sea` | Sea & Beach Wears and Handy Crafts |
| `tea` | Premium Tea |
| `spices` | Spices & Food |
| `clothing` | Clothing & Textiles |
| `souvenirs` | Souvenirs & Crafts |
| `beauty` | Beauty & Wellness |
| `other` | Other |

---

# Products API

## 1. List products (paginated)

**GET** `/products`

Public. Optional filters and pagination.

### Query parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `category` | string | — | Filter by slug: `sea`, `tea`, `spices`, `clothing`, `souvenirs`, `beauty`, `other`. Use `all` or omit for no filter. |
| `searchTerm` | string | — | Search in name and description. |
| `page` | number | `0` | Page index (0-based). |
| `size` | number | `10` | Page size. |
| `sortBy` | string | `createdAt` | Sort field. |
| `sortDirection` | string | `desc` | `asc` or `desc`. |

### Example request

```http
GET /api/v1/products?category=sea&page=0&size=12&sortBy=createdAt&sortDirection=desc
```

### Response: 200 OK

Paginated response (Spring Page):

```json
{
  "content": [
    {
      "id": 1,
      "name": "Beach Sarong Collection",
      "description": "Lightweight sarongs perfect for beach days.",
      "imageUrls": [
        "https://example.com/img1.jpg",
        "https://example.com/img2.jpg"
      ],
      "currentPrice": 22.00,
      "originalPrice": 30.00,
      "discountPercentage": 27,
      "category": "sea",
      "rating": 4.6,
      "reviewCount": 203,
      "isBestSeller": true
    }
  ],
  "pageable": { "pageNumber": 0, "pageSize": 12, "sort": { "sorted": true, "unsorted": false, "empty": false }, "offset": 0, "paged": true, "unpaged": false },
  "totalPages": 1,
  "totalElements": 1,
  "last": true,
  "first": true,
  "size": 12,
  "number": 0,
  "numberOfElements": 1,
  "empty": false
}
```

**List item fields:**

| Field | Type | Description |
|-------|------|-------------|
| `id` | number | Product ID. |
| `name` | string | Product name. |
| `description` | string | Short description. |
| `imageUrls` | string[] | Image URLs (primary first). Use `imageUrls[0]` for card thumbnail. |
| `currentPrice` | number | Selling price. |
| `originalPrice` | number \| null | Original price (if discounted). |
| `discountPercentage` | number \| null | Discount (e.g. 27 for 27% OFF). |
| `category` | string | Category slug. |
| `rating` | number \| null | Average rating (0–5). |
| `reviewCount` | number | Number of reviews. |
| `isBestSeller` | boolean | Best-seller badge. |

---

## 2. Get product by ID

**GET** `/products/{id}`

Public. Returns a single active product with full image list.

### Example request

```http
GET /api/v1/products/1
```

### Response: 200 OK

```json
{
  "id": 1,
  "name": "Beach Sarong Collection",
  "description": "Lightweight sarongs perfect for beach days, handwoven with ocean motifs.",
  "images": [
    {
      "id": 10,
      "imageUrl": "https://example.com/img1.jpg",
      "isPrimary": true,
      "altText": null,
      "sortOrder": 0,
      "createdAt": "2026-02-18T10:00:00"
    },
    {
      "id": 11,
      "imageUrl": "https://example.com/img2.jpg",
      "isPrimary": false,
      "altText": null,
      "sortOrder": 1,
      "createdAt": "2026-02-18T10:00:00"
    }
  ],
  "currentPrice": 22.00,
  "originalPrice": 30.00,
  "discountPercentage": 27,
  "category": "sea",
  "rating": 4.6,
  "reviewCount": 203,
  "isBestSeller": true,
  "isActive": true,
  "createdAt": "2026-02-18T10:00:00",
  "updatedAt": "2026-02-18T10:00:00"
}
```

**Detail image object:**

| Field | Type | Description |
|-------|------|-------------|
| `id` | number | Image ID. |
| `imageUrl` | string | Image URL. |
| `isPrimary` | boolean | Primary/thumbnail. |
| `altText` | string \| null | Alt text. |
| `sortOrder` | number | Display order. |
| `createdAt` | string (ISO-8601) | Created time. |

### Error: 404 Not Found

Product not found or inactive.

---

## 3. Best sellers

**GET** `/products/best-sellers`

Public. Paginated list of products with `isBestSeller: true`.

### Query parameters

| Parameter | Type | Default |
|-----------|------|---------|
| `page` | number | `0` |
| `size` | number | `10` |

### Example request

```http
GET /api/v1/products/best-sellers?page=0&size=8
```

### Response: 200 OK

Same paginated shape as **List products**; each item has the same list-item fields (including `imageUrls`).

---

## 4. Products by category

**GET** `/products/category/{category}`

Public. Category path uses the slug: `sea`, `tea`, `spices`, `clothing`, `souvenirs`, `beauty`, `other`.

### Query parameters

| Parameter | Type | Default |
|-----------|------|---------|
| `page` | number | `0` |
| `size` | number | `10` |

### Example request

```http
GET /api/v1/products/category/tea?page=0&size=12
```

### Response: 200 OK

Same paginated shape as **List products**.

---

## 5. Create product (Admin)

**POST** `/products`

**Auth:** Bearer token, **ADMIN** role.

### Request body

```json
{
  "name": "Beach Sarong Collection",
  "description": "Lightweight sarongs perfect for beach days.",
  "imageUrls": [
    "https://example.com/img1.jpg",
    "https://example.com/img2.jpg"
  ],
  "primaryImageIndex": 0,
  "currentPrice": 22.00,
  "originalPrice": 30.00,
  "category": "sea",
  "rating": 4.6,
  "reviewCount": 0,
  "isBestSeller": true,
  "isActive": true
}
```

| Field | Type | Required | Rules |
|-------|------|----------|--------|
| `name` | string | Yes | 2–200 chars. |
| `description` | string | No | Max 2000. |
| `imageUrls` | string[] | No | Max 10 URLs, each max 500 chars. |
| `primaryImageIndex` | number | No | 0-based index in `imageUrls`; default 0. |
| `currentPrice` | number | Yes | 0.01–99999.99. |
| `originalPrice` | number | No | For discount display. |
| `category` | string | Yes | One of: `sea`, `tea`, `spices`, `clothing`, `souvenirs`, `beauty`, `other` (not `all`). |
| `rating` | number | No | 0–5. |
| `reviewCount` | number | No | Default 0. |
| `isBestSeller` | boolean | No | Default false. |
| `isActive` | boolean | No | Default true. |

### Response: 201 Created

Full product object (same shape as **Get product by ID**), including `images` and timestamps.

### Errors

- **400** – Validation error (e.g. invalid category, category `all`).
- **401** – Missing or invalid token.
- **403** – Not ADMIN.

---

## 6. Update product (Admin)

**PUT** `/products/{id}`

**Auth:** Bearer token, **ADMIN** role.

### Request body

All fields optional; only sent fields are updated.

```json
{
  "name": "Updated name",
  "description": "Updated description.",
  "imageUrls": ["https://example.com/new1.jpg"],
  "primaryImageIndex": 0,
  "currentPrice": 20.00,
  "originalPrice": 28.00,
  "category": "sea",
  "rating": 4.7,
  "reviewCount": 250,
  "isBestSeller": false,
  "isActive": true
}
```

If `imageUrls` is provided, it **replaces** all existing images. Use `primaryImageIndex` to set which entry is primary.

### Response: 200 OK

Full product object (same as **Get product by ID**).

### Errors

- **400** – Invalid input (e.g. category `all`).
- **401** – Unauthorized.
- **403** – Forbidden.
- **404** – Product not found.

---

## 7. Delete product (Admin)

**DELETE** `/products/{id}`

**Auth:** Bearer token, **ADMIN** role.

### Example request

```http
DELETE /api/v1/products/1
Authorization: Bearer <token>
```

### Response: 204 No Content

No body.

### Errors

- **401** – Unauthorized.
- **403** – Forbidden.
- **404** – Product not found.

---

# Shopping Cart API

All cart endpoints require a logged-in user (Bearer token, role USER, GUIDE, or ADMIN). Each user has one cart.

---

## 1. Get my cart

**GET** `/cart`

Returns the current user’s cart with all items and totals.

### Example request

```http
GET /api/v1/cart
Authorization: Bearer <token>
```

### Response: 200 OK

```json
{
  "cartId": 5,
  "items": [
    {
      "cartItemId": 12,
      "productId": 1,
      "productName": "Beach Sarong Collection",
      "imageUrl": "https://example.com/img1.jpg",
      "unitPrice": 22.00,
      "quantity": 2,
      "lineTotal": 44.00
    }
  ],
  "itemCount": 2,
  "totalAmount": 44.00
}
```

| Field | Type | Description |
|-------|------|-------------|
| `cartId` | number | Cart ID. |
| `items` | array | Cart line items. |
| `itemCount` | number | Total quantity across all items. |
| `totalAmount` | number | Sum of all `lineTotal`. |

**Cart item:**

| Field | Type | Description |
|-------|------|-------------|
| `cartItemId` | number | Line item ID (use for update/remove). |
| `productId` | number | Product ID. |
| `productName` | string | Product name. |
| `imageUrl` | string \| null | Primary product image URL. |
| `unitPrice` | number | Price per unit. |
| `quantity` | number | Quantity. |
| `lineTotal` | number | unitPrice × quantity. |

### Error: 401 Unauthorized

Missing or invalid token.

---

## 2. Add to cart

**POST** `/cart/items`

Adds a product to the cart or increases quantity if the product is already in the cart.

### Request body

```json
{
  "productId": 1,
  "quantity": 2
}
```

| Field | Type | Required | Rules |
|-------|------|----------|--------|
| `productId` | number | Yes | Existing active product ID. |
| `quantity` | number | No | Min 1; default 1. |

### Example request

```http
POST /api/v1/cart/items
Authorization: Bearer <token>
Content-Type: application/json

{"productId": 1, "quantity": 2}
```

### Response: 200 OK

Full cart object (same as **Get my cart**).

### Errors

- **400** – Invalid body or product not available.
- **401** – Unauthorized.
- **404** – Product not found.

---

## 3. Update cart item quantity

**PUT** `/cart/items/{itemId}`

Updates the quantity of a specific line item. Use `cartItemId` from the cart response.

### Request body

```json
{
  "quantity": 3
}
```

| Field | Type | Required | Rules |
|-------|------|----------|--------|
| `quantity` | number | Yes | Min 1. |

### Example request

```http
PUT /api/v1/cart/items/12
Authorization: Bearer <token>
Content-Type: application/json

{"quantity": 3}
```

### Response: 200 OK

Full cart object (same as **Get my cart**).

### Errors

- **400** – Invalid quantity.
- **401** – Unauthorized.
- **404** – Cart item not found (or not owned by user).

---

## 4. Remove item from cart

**DELETE** `/cart/items/{itemId}`

Removes one line item from the cart. Use `cartItemId` from the cart response.

### Example request

```http
DELETE /api/v1/cart/items/12
Authorization: Bearer <token>
```

### Response: 200 OK

Full cart object (same as **Get my cart**) after the item is removed.

### Errors

- **401** – Unauthorized.
- **404** – Cart item not found (or not owned by user).

---

# Error response format

Validation and API errors typically return JSON like:

```json
{
  "timestamp": "2026-02-18T12:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Request validation failed",
  "details": { "fieldName": "Error message" },
  "path": "/api/v1/products"
}
```

- **400** – Bad Request (validation, invalid category, etc.).
- **401** – Unauthorized (missing/invalid token).
- **403** – Forbidden (e.g. non-admin on admin product endpoints).
- **404** – Not Found (product, cart item, etc.).
- **500** – Internal Server Error.

---

# Quick reference

| Action | Method | Path | Auth |
|--------|--------|------|------|
| List products | GET | `/products` | — |
| Get product | GET | `/products/{id}` | — |
| Best sellers | GET | `/products/best-sellers` | — |
| By category | GET | `/products/category/{category}` | — |
| Create product | POST | `/products` | ADMIN |
| Update product | PUT | `/products/{id}` | ADMIN |
| Delete product | DELETE | `/products/{id}` | ADMIN |
| Get cart | GET | `/cart` | User |
| Add to cart | POST | `/cart/items` | User |
| Update quantity | PUT | `/cart/items/{itemId}` | User |
| Remove item | DELETE | `/cart/items/{itemId}` | User |

---

# UI integration tips

1. **Product list/cards**  
   Use `GET /products` (or category/best-sellers). Use `imageUrls[0]` for the card image; show `discountPercentage` and `isBestSeller` when set.

2. **Product detail page**  
   Use `GET /products/{id}`. Use `images` for gallery; `images[0]` or the one with `isPrimary: true` as main image.

3. **Add to cart**  
   From product detail or list, call `POST /cart/items` with `productId` and `quantity`. Then refresh cart (e.g. `GET /cart`) or use the returned cart in state.

4. **Cart page**  
   Use `GET /cart` for items, `itemCount`, and `totalAmount`. Use `cartItemId` for update quantity (`PUT /cart/items/{cartItemId}`) and remove (`DELETE /cart/items/{cartItemId}`).

5. **Categories**  
   Use slugs in URLs and filters: `sea`, `tea`, `spices`, `clothing`, `souvenirs`, `beauty`, `other`. Use `all` or no `category` param for “All Products”.

6. **Admin product form**  
   Send `imageUrls` as array of strings; set `primaryImageIndex` (0-based) for the main image. Category must be one of the slugs (not `all`).
