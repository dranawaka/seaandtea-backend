# Image Upload API – UI Integration

Base URL: **`/api/v1/upload`**  
Example: `http://localhost:8080/api/v1/upload`

---

## Authentication

All upload and delete endpoints require a valid JWT. Send it in the header:

```http
Authorization: Bearer <your_jwt_token>
```

Role requirements are listed per endpoint below.

---

## File constraints (all uploads)

| Constraint | Value |
|------------|--------|
| **Allowed types** | JPEG, JPG, PNG, WebP (`image/jpeg`, `image/jpg`, `image/png`, `image/webp`) |
| **Max file size** | **10 MB** |
| **Request param name** | `file` (required for all uploads) |

- **400** is returned if the file is empty, has an unsupported type, or exceeds 10 MB.
- **413 Payload Too Large** may be returned by the server for very large requests.

---

## Overview of endpoints

| Method | Path | Use case | Allowed roles |
|--------|------|----------|----------------|
| **POST** | `/tour/{tourId}/image` | Add an image to a tour | GUIDE, ADMIN |
| **POST** | `/product/{productId}/image` | Add an image to a product | ADMIN |
| **POST** | `/profile-picture` | Set the authenticated user’s profile picture | USER, GUIDE, ADMIN |
| **POST** | `/guide-profile-picture` | Set profile picture for guide profile | GUIDE, ADMIN |
| **DELETE** | `/image` | Remove an image from storage by URL | GUIDE, ADMIN |
| **GET** | `/homepage-slider` | List homepage slider images (public) | — (no auth) |
| **POST** | `/homepage-slider` | Add image to homepage slider | ADMIN |
| **DELETE** | `/homepage-slider/{id}` | Remove image from homepage slider | ADMIN |

---

# 1. Upload tour image

**POST** `/api/v1/upload/tour/{tourId}/image`

Uploads an image for a specific tour. The image is stored in Cloudinary and attached to the tour. Returns the full **tour** object (with updated `images` list).

**Path parameter**

| Name | Type | Description |
|------|------|-------------|
| `tourId` | number | ID of the tour to add the image to. |

**Request (multipart/form-data)**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `file` | file | Yes | Image file (JPEG, PNG, WebP; max 10 MB). |
| `isPrimary` | boolean | No | If `true`, this image is set as the tour’s primary image. Default: `false`. |
| `altText` | string | No | Alt text for the image. |

### Example request (cURL)

```bash
curl -X POST "http://localhost:8080/api/v1/upload/tour/42/image" \
  -H "Authorization: Bearer <your_jwt_token>" \
  -F "file=@/path/to/photo.jpg" \
  -F "isPrimary=true" \
  -F "altText=Sunset over the tea plantation"
```

### Example request (JavaScript fetch)

```javascript
const formData = new FormData();
formData.append('file', fileInput.files[0]);
formData.append('isPrimary', 'true');
formData.append('altText', 'Sunset over the tea plantation');

const response = await fetch('http://localhost:8080/api/v1/upload/tour/42/image', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
  },
  body: formData,
});
```

### Response: 200 OK

Returns the full **TourResponse** (tour with updated `images` array). Structure matches the tour object used elsewhere (e.g. `id`, `title`, `guide`, `images`, etc.). The new image appears in `images` with `imageUrl`, `isPrimary`, `altText`, `id`, `createdAt`.

### Error responses

| Status | Meaning |
|--------|--------|
| **400** | Invalid or empty file, unsupported type, or file too large. |
| **401** | Missing or invalid JWT. |
| **403** | User is not GUIDE/ADMIN or is not allowed to modify this tour. |
| **404** | Tour not found. |
| **413** | File too large. |

---

# 2. Upload product image

**POST** `/api/v1/upload/product/{productId}/image`

Uploads an image for a product and adds it to the product’s image list (max 10 images per product). The image is stored in Cloudinary. Returns the full **product** object with updated `images` list.

**Path parameter**

| Name | Type | Description |
|------|------|-------------|
| `productId` | number | ID of the product to add the image to. |

**Request (multipart/form-data)**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `file` | file | Yes | Image file (JPEG, PNG, WebP; max 10 MB). |
| `isPrimary` | boolean | No | If `true`, this image is set as the product’s primary image. Default: `false`. |
| `altText` | string | No | Alt text for the image. |

### Example request (cURL)

```bash
curl -X POST "http://localhost:8080/api/v1/upload/product/5/image" \
  -H "Authorization: Bearer <your_jwt_token>" \
  -F "file=@/path/to/product.jpg" \
  -F "isPrimary=true" \
  -F "altText=Main product view"
```

### Response: 200 OK

Returns the full **ProductResponse** (product with updated `images` array). Each image has `id`, `imageUrl`, `isPrimary`, `altText`, `sortOrder`, `createdAt`.

### Error responses

| Status | Meaning |
|--------|--------|
| **400** | Invalid or empty file, unsupported type, file too large, or product already has 10 images. |
| **401** | Missing or invalid JWT. |
| **403** | Not ADMIN. |
| **404** | Product not found. |
| **413** | File too large. |

---

## Product image management (Admin)

Besides uploading via the endpoint above, admins can manage product images on the **Products API**:

- **GET** `/api/v1/products` and **GET** `/api/v1/products/{id}` — list/view products (images in response).
- **DELETE** `/api/v1/products/{productId}/images/{imageId}` — remove one image from a product (also deletes from storage). Returns updated product.
- **PATCH** `/api/v1/products/{productId}/images/{imageId}` — update an image: set `isPrimary`, `sortOrder`, or `altText`. Request body: `{ "isPrimary": true, "sortOrder": 0, "altText": "..." }` (all fields optional). Returns updated product.

---

# 3. Upload profile picture

**POST** `/api/v1/upload/profile-picture`

Sets the **authenticated user’s** profile picture. The user’s profile is updated with the new image URL.

**Request (multipart/form-data)**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `file` | file | Yes | Image file (JPEG, PNG, WebP; max 10 MB). |

### Example request (JavaScript fetch)

```javascript
const formData = new FormData();
formData.append('file', fileInput.files[0]);

const response = await fetch('http://localhost:8080/api/v1/upload/profile-picture', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
  },
  body: formData,
});
const user = await response.json(); // Updated user with profilePictureUrl
```

### Response: 200 OK

Returns the updated **UserDto** (current user with new `profilePictureUrl`):

```json
{
  "id": 1,
  "email": "user@example.com",
  "firstName": "Jane",
  "lastName": "Doe",
  "phone": "+1234567890",
  "dateOfBirth": "1990-05-15",
  "nationality": "US",
  "profilePictureUrl": "https://res.cloudinary.com/.../profiles/profile_xxx.webp",
  "isVerified": false,
  "isActive": true,
  "role": "USER",
  "createdAt": "2026-01-01T00:00:00",
  "updatedAt": "2026-02-19T12:00:00"
}
```

### Error responses

| Status | Meaning |
|--------|--------|
| **400** | Invalid or empty file, unsupported type, or file too large. |
| **401** | Missing or invalid JWT. |
| **413** | File too large. |

---

# 4. Upload guide profile picture

**POST** `/api/v1/upload/guide-profile-picture`

Same as profile picture, but intended for **guide** profiles. Updates both the user and the guide profile with the new image. Only **GUIDE** and **ADMIN** may call this.

**Request (multipart/form-data)**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `file` | file | Yes | Image file (JPEG, PNG, WebP; max 10 MB). |

### Example request (JavaScript fetch)

```javascript
const formData = new FormData();
formData.append('file', fileInput.files[0]);

const response = await fetch('http://localhost:8080/api/v1/upload/guide-profile-picture', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${token}`,
  },
  body: formData,
});
const user = await response.json();
```

### Response: 200 OK

Same as **Upload profile picture**: returns the updated **UserDto** with the new `profilePictureUrl`.

### Error responses

| Status | Meaning |
|--------|--------|
| **400** | Invalid or empty file, unsupported type, or file too large. |
| **401** | Missing or invalid JWT. |
| **403** | User is not GUIDE or ADMIN. |
| **404** | Guide profile not found. |
| **413** | File too large. |

---

# 5. Delete image

**DELETE** `/api/v1/upload/image`

Deletes an image from Cloudinary storage by its full URL. Use this only for images that were uploaded via this API (Cloudinary URLs). **GUIDE** or **ADMIN** only.

**Query parameter**

| Name | Type | Required | Description |
|------|------|----------|-------------|
| `imageUrl` | string | Yes | Full URL of the image to delete (e.g. Cloudinary `secure_url`). |

### Example request (cURL)

```bash
curl -X DELETE "http://localhost:8080/api/v1/upload/image?imageUrl=https%3A%2F%2Fres.cloudinary.com%2F...%2Fimage.webp" \
  -H "Authorization: Bearer <your_jwt_token>"
```

### Example request (JavaScript fetch)

```javascript
const imageUrl = encodeURIComponent('https://res.cloudinary.com/.../image.webp');
const response = await fetch(`http://localhost:8080/api/v1/upload/image?imageUrl=${imageUrl}`, {
  method: 'DELETE',
  headers: {
    'Authorization': `Bearer ${token}`,
  },
});
```

### Response: 200 OK

```json
{
  "message": "Image deleted successfully"
}
```

### Error responses

| Status | Meaning |
|--------|--------|
| **400** | Invalid or unsupported image URL format. |
| **401** | Missing or invalid JWT. |
| **403** | User is not GUIDE or ADMIN. |

---

# 6. Homepage slider (admin)

These endpoints allow **ADMIN** users to manage images shown in the homepage slider. The list endpoint is **public** (no JWT) so the frontend can display the slider without authentication.

---

## 6.1 List homepage slider images

**GET** `/api/v1/upload/homepage-slider`

Returns all slider images in display order. **No authentication required.**

### Response: 200 OK

Array of slider image objects:

```json
[
  {
    "id": 1,
    "imageUrl": "https://res.cloudinary.com/.../homepage/slider/slider_xxx.webp",
    "sortOrder": 0,
    "altText": "Tea plantation view",
    "createdAt": "2026-02-19T10:00:00"
  }
]
```

---

## 6.2 Upload homepage slider image

**POST** `/api/v1/upload/homepage-slider`

Uploads an image for the homepage slider. **ADMIN** only.

**Request (multipart/form-data)**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `file` | file | Yes | Image file (JPEG, PNG, WebP; max 10 MB). |
| `sortOrder` | number | No | Display order (0-based). If omitted, image is appended at the end. |
| `altText` | string | No | Alt text for the image. |

### Example request (JavaScript fetch)

```javascript
const formData = new FormData();
formData.append('file', fileInput.files[0]);
formData.append('sortOrder', '0');
formData.append('altText', 'Welcome to Sea & Tea');

const response = await fetch('http://localhost:8080/api/v1/upload/homepage-slider', {
  method: 'POST',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
  },
  body: formData,
});
const sliderImage = await response.json();
```

### Response: 200 OK

Returns the created slider image:

```json
{
  "id": 1,
  "imageUrl": "https://res.cloudinary.com/.../slider_xxx.webp",
  "sortOrder": 0,
  "altText": "Welcome to Sea & Tea",
  "createdAt": "2026-02-19T10:00:00"
}
```

### Error responses

| Status | Meaning |
|--------|--------|
| **400** | Invalid or empty file, unsupported type, or file too large. |
| **401** | Missing or invalid JWT. |
| **403** | User is not ADMIN. |
| **413** | File too large. |

---

## 6.3 Delete homepage slider image

**DELETE** `/api/v1/upload/homepage-slider/{id}`

Deletes a slider image by ID and removes it from Cloudinary. **ADMIN** only.

**Path parameter**

| Name | Type | Description |
|------|------|-------------|
| `id` | number | ID of the slider image to delete. |

### Example request (JavaScript fetch)

```javascript
const response = await fetch(`http://localhost:8080/api/v1/upload/homepage-slider/1`, {
  method: 'DELETE',
  headers: {
    'Authorization': `Bearer ${adminToken}`,
  },
});
```

### Response: 200 OK

```json
{
  "message": "Homepage slider image deleted successfully"
}
```

### Error responses

| Status | Meaning |
|--------|--------|
| **401** | Missing or invalid JWT. |
| **403** | User is not ADMIN. |
| **404** | Slider image not found. |

---

## UI integration notes

1. **Do not set `Content-Type` for multipart uploads**  
   Let the browser set it (with boundary). Do **not** send `Content-Type: application/json` when sending `FormData`.

2. **Form field name**  
   The file must be sent under the name **`file`**. Example: `formData.append('file', file)`.

3. **Client-side validation**  
   Before uploading, check:
   - File type is JPEG, PNG, or WebP.
   - File size ≤ 10 MB.
   - File is not empty.

4. **Tour images flow**  
   - To add images when **creating** a tour: either use `POST /api/v1/upload/tour/{tourId}/image` after creating the tour (with the returned `id`), or use tour create with pre-uploaded URLs if your flow supports it.
   - To add images to an **existing** tour: use `POST /api/v1/upload/tour/{tourId}/image`; the response is the full tour with the new image in `images`.

5. **Profile picture**  
   - Regular users: `POST /api/v1/upload/profile-picture`.
   - Guides (for their guide profile): `POST /api/v1/upload/guide-profile-picture`. Both return the updated user with `profilePictureUrl`.

6. **Delete**  
   Use `DELETE /api/v1/upload/image?imageUrl=...` only for Cloudinary URLs returned by this API. After deleting, remove the image from your local state or refetch the tour/user.

7. **Homepage slider (admin)**  
   - **List:** `GET /api/v1/upload/homepage-slider` — no auth; use this on the public homepage to render the slider.
   - **Add:** `POST /api/v1/upload/homepage-slider` with `file` (and optional `sortOrder`, `altText`) — ADMIN only.
   - **Remove:** `DELETE /api/v1/upload/homepage-slider/{id}` — ADMIN only; refetch the list after delete.

8. **Base URL**  
   Replace `http://localhost:8080` with your backend base URL in production.
