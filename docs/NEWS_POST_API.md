# News & Posts API – UI Integration

Base URL: **`/api/v1/news`**  
Example: `http://localhost:8080/api/v1/news`

---

## Authentication

- **Public endpoints** (list published posts, get post by ID, get comments): No auth required. If the user is logged in, the response can include `likedByCurrentUser` for posts.
- **Admin endpoints** (create, update, delete posts, list all posts): Require **ADMIN** role. Send JWT:
  ```http
  Authorization: Bearer <your_jwt_token>
  ```
- **Like / Comment endpoints**: Require any authenticated user (USER, GUIDE, or ADMIN):
  ```http
  Authorization: Bearer <your_jwt_token>
  ```

---

## Overview

- **Admins** create and manage news posts (title, body, published flag).
- **Customers** (USER) and **Guides** (GUIDE) can view published posts, like/unlike, and comment.
- Only **published** posts appear in the public list; unpublished posts are visible only to admins (e.g. in admin list or when opening by ID as admin).

---

# News & Posts API

## 1. List published posts (feed)

**GET** `/api/v1/news`

Returns a paginated list of **published** posts, newest first. Optional auth: if the user is logged in, each post includes `likedByCurrentUser`.

### Query parameters

| Parameter | Type   | Required | Description                          |
|-----------|--------|----------|--------------------------------------|
| `page`    | number | No       | Page index (0-based). Default: `0`.  |
| `size`    | number | No       | Page size. Default: `10`.           |

### Example request

```http
GET /api/v1/news?page=0&size=10
Authorization: Bearer <your_jwt_token>
```

(Header optional for public feed; include it if you want `likedByCurrentUser`.)

### Response: 200 OK

Paginated response (Spring Page format):

```json
{
  "content": [
    {
      "id": 1,
      "title": "New Tour: Sunset Sail",
      "bodySummary": "We're excited to announce...",
      "authorId": 2,
      "authorDisplayName": "Sea & Tea Team",
      "isPublished": true,
      "createdAt": "2026-02-18T10:00:00",
      "likeCount": 12,
      "commentCount": 5,
      "likedByCurrentUser": false
    }
  ],
  "pageable": { "pageNumber": 0, "pageSize": 10, "offset": 0, "paged": true, "unpaged": false },
  "totalElements": 25,
  "totalPages": 3,
  "size": 10,
  "number": 0,
  "first": true,
  "last": false,
  "numberOfElements": 10,
  "empty": false
}
```

**Response fields (per post in `content`):**

| Field                 | Type    | Description                                              |
|-----------------------|---------|----------------------------------------------------------|
| `id`                  | number  | Post ID.                                                 |
| `title`               | string  | Post title.                                              |
| `bodySummary`         | string  | Short summary of the body (for list/cards).              |
| `authorId`            | number  | Author user ID.                                         |
| `authorDisplayName`   | string  | Author display name.                                     |
| `isPublished`         | boolean | Whether the post is published.                           |
| `createdAt`           | string  | ISO-8601 date-time.                                      |
| `likeCount`           | number  | Total likes.                                             |
| `commentCount`        | number  | Total comments.                                          |
| `likedByCurrentUser`  | boolean | Whether the current user liked this post (null if not logged in). |

**Pagination fields (top level):** `totalElements`, `totalPages`, `size`, `number`, `first`, `last`, `numberOfElements`, `empty`.

---

## 2. Get post by ID (single post / detail page)

**GET** `/api/v1/news/{id}`

Returns a single post with full body and embedded comments. Published posts are visible to everyone; unpublished posts only to **ADMIN**.

### Path parameters

| Parameter | Type   | Description |
|-----------|--------|-------------|
| `id`      | number | Post ID.    |

### Example request

```http
GET /api/v1/news/1
Authorization: Bearer <your_jwt_token>
```

### Response: 200 OK

```json
{
  "id": 1,
  "title": "New Tour: Sunset Sail",
  "body": "Full post content with markdown or HTML...",
  "authorId": 2,
  "authorDisplayName": "Sea & Tea Team",
  "isPublished": true,
  "createdAt": "2026-02-18T10:00:00",
  "updatedAt": "2026-02-18T11:30:00",
  "likeCount": 12,
  "commentCount": 5,
  "likedByCurrentUser": false,
  "comments": [
    {
      "id": 101,
      "postId": 1,
      "userId": 3,
      "userDisplayName": "Jane Doe",
      "text": "Can't wait to try this!",
      "createdAt": "2026-02-18T12:00:00"
    }
  ]
}
```

**Response fields:**

| Field                 | Type    | Description                                              |
|-----------------------|---------|----------------------------------------------------------|
| `id`                  | number  | Post ID.                                                 |
| `title`               | string  | Post title.                                              |
| `body`                | string  | Full post body.                                          |
| `authorId`            | number  | Author user ID.                                         |
| `authorDisplayName`   | string  | Author display name.                                     |
| `isPublished`         | boolean | Whether the post is published.                           |
| `createdAt`           | string  | ISO-8601 date-time.                                      |
| `updatedAt`           | string  | ISO-8601 date-time.                                      |
| `likeCount`           | number  | Total likes.                                             |
| `commentCount`        | number  | Total comments.                                         |
| `likedByCurrentUser`  | boolean | Whether the current user liked this post.                |
| `comments`            | array   | List of comment objects (see comment fields below).      |

**Comment object fields:**

| Field             | Type   | Description        |
|-------------------|--------|--------------------|
| `id`              | number | Comment ID.        |
| `postId`          | number | Post ID.           |
| `userId`          | number | Commenter user ID. |
| `userDisplayName` | string | Commenter name.    |
| `text`            | string | Comment text.      |
| `createdAt`       | string | ISO-8601 date-time.|

### Error responses

| Status | Meaning |
|--------|---------|
| **404** | Post not found, or (for non-admin) post exists but is unpublished. |
| **401** | Not required for published posts; only relevant if you expect admin view. |

---

## 3. Get comments for a post (paginated)

**GET** `/api/v1/news/{id}/comments`

Returns paginated comments for a post, oldest first.

### Path parameters

| Parameter | Type   | Description |
|-----------|--------|-------------|
| `id`      | number | Post ID.    |

### Query parameters

| Parameter | Type   | Required | Description                          |
|-----------|--------|----------|--------------------------------------|
| `page`    | number | No       | Page index (0-based). Default: `0`.  |
| `size`    | number | No       | Page size. Default: `20`.            |

### Example request

```http
GET /api/v1/news/1/comments?page=0&size=20
```

### Response: 200 OK

Same Spring Page structure as in section 1. `content` is an array of comment objects with fields: `id`, `postId`, `userId`, `userDisplayName`, `text`, `createdAt`.

### Error responses

| Status | Meaning |
|--------|---------|
| **404** | Post not found. |

---

## 4. Create post (Admin)

**POST** `/api/v1/news`

Creates a new news post. **ADMIN** only.

### Request body

| Field         | Type    | Required | Description                                      |
|---------------|---------|----------|--------------------------------------------------|
| `title`       | string  | Yes      | Post title; max **500** characters.              |
| `body`        | string  | Yes      | Post body; max **50,000** characters.            |
| `isPublished` | boolean | No       | Default: `true`. Set `false` for draft.          |

### Example request

```http
POST /api/v1/news
Authorization: Bearer <your_jwt_token>
Content-Type: application/json

{
  "title": "New Tour: Sunset Sail",
  "body": "We're excited to announce our new sunset sailing experience...",
  "isPublished": true
}
```

### Response: 201 Created

Same shape as **Get post by ID** (full `NewsPostResponse` with `comments` typically empty for a new post).

### Error responses

| Status | Meaning |
|--------|---------|
| **400** | Invalid input (e.g. blank title/body or over length). |
| **401** | Not authenticated. |
| **403** | Forbidden – Admin only. |

---

## 5. Update post (Admin)

**PUT** `/api/v1/news/{id}`

Updates an existing post. **ADMIN** only. Send only fields you want to change.

### Path parameters

| Parameter | Type   | Description |
|-----------|--------|-------------|
| `id`      | number | Post ID.    |

### Request body

| Field         | Type    | Required | Description                                      |
|---------------|---------|----------|--------------------------------------------------|
| `title`       | string  | No       | New title; max **500** characters.               |
| `body`        | string  | No       | New body; max **50,000** characters.             |
| `isPublished` | boolean | No       | Update published state.                           |

### Example request

```http
PUT /api/v1/news/1
Authorization: Bearer <your_jwt_token>
Content-Type: application/json

{
  "title": "Updated: Sunset Sail 2026",
  "isPublished": true
}
```

### Response: 200 OK

Full `NewsPostResponse` (same as Get post by ID).

### Error responses

| Status | Meaning |
|--------|---------|
| **400** | Invalid input. |
| **401** | Not authenticated. |
| **403** | Forbidden – Admin only. |
| **404** | Post not found. |

---

## 6. Delete post (Admin)

**DELETE** `/api/v1/news/{id}`

Permanently deletes a post and its likes and comments. **ADMIN** only.

### Path parameters

| Parameter | Type   | Description |
|-----------|--------|-------------|
| `id`      | number | Post ID.    |

### Example request

```http
DELETE /api/v1/news/1
Authorization: Bearer <your_jwt_token>
```

### Response: 204 No Content

No body.

### Error responses

| Status | Meaning |
|--------|---------|
| **401** | Not authenticated. |
| **403** | Forbidden – Admin only. |
| **404** | Post not found. |

---

## 7. List all posts – Admin

**GET** `/api/v1/news/admin/all`

Returns paginated list of **all** posts (published and unpublished), newest first. **ADMIN** only.

### Query parameters

| Parameter | Type   | Required | Description                          |
|-----------|--------|----------|--------------------------------------|
| `page`    | number | No       | Page index (0-based). Default: `0`.  |
| `size`    | number | No       | Page size. Default: `10`.           |

### Example request

```http
GET /api/v1/news/admin/all?page=0&size=10
Authorization: Bearer <your_jwt_token>
```

### Response: 200 OK

Same paginated structure as **List published posts**; items have the same fields (including `isPublished` so UI can show draft/published state).

### Error responses

| Status | Meaning |
|--------|---------|
| **401** | Not authenticated. |
| **403** | Forbidden – Admin only. |

---

## 8. Like a post

**POST** `/api/v1/news/{id}/like`

Adds a like from the current user. One like per user per post. Any authenticated user (USER, GUIDE, ADMIN).

### Path parameters

| Parameter | Type   | Description |
|-----------|--------|-------------|
| `id`      | number | Post ID.    |

### Example request

```http
POST /api/v1/news/1/like
Authorization: Bearer <your_jwt_token>
```

### Response: 204 No Content

No body.

### Error responses

| Status | Meaning |
|--------|---------|
| **404** | Post not found. |
| **409** | Already liked this post. |
| **401** | Not authenticated. |

---

## 9. Unlike a post

**DELETE** `/api/v1/news/{id}/like`

Removes the current user's like. Any authenticated user.

### Path parameters

| Parameter | Type   | Description |
|-----------|--------|-------------|
| `id`      | number | Post ID.    |

### Example request

```http
DELETE /api/v1/news/1/like
Authorization: Bearer <your_jwt_token>
```

### Response: 204 No Content

No body.

### Error responses

| Status | Meaning |
|--------|---------|
| **404** | Post not found. |
| **409** | You have not liked this post. |
| **401** | Not authenticated. |

---

## 10. Add a comment

**POST** `/api/v1/news/{id}/comments`

Adds a comment to a published post. Any authenticated user.

### Path parameters

| Parameter | Type   | Description |
|-----------|--------|-------------|
| `id`      | number | Post ID.    |

### Request body

| Field  | Type   | Required | Description                           |
|--------|--------|----------|---------------------------------------|
| `text` | string | Yes      | Comment text; max **2000** characters.|

### Example request

```http
POST /api/v1/news/1/comments
Authorization: Bearer <your_jwt_token>
Content-Type: application/json

{
  "text": "Can't wait to try this!"
}
```

### Response: 201 Created

```json
{
  "id": 101,
  "postId": 1,
  "userId": 3,
  "userDisplayName": "Jane Doe",
  "text": "Can't wait to try this!",
  "createdAt": "2026-02-18T12:00:00"
}
```

### Error responses

| Status | Meaning |
|--------|---------|
| **400** | Invalid input (e.g. blank or over length). |
| **404** | Post not found. |
| **401** | Not authenticated. |

---

## 11. Delete a comment

**DELETE** `/api/v1/news/{id}/comments/{commentId}`

Deletes a comment. Users can delete **their own** comment; **ADMIN** can delete any.

### Path parameters

| Parameter   | Type   | Description |
|-------------|--------|-------------|
| `id`        | number | Post ID.    |
| `commentId` | number | Comment ID. |

### Example request

```http
DELETE /api/v1/news/1/comments/101
Authorization: Bearer <your_jwt_token>
```

### Response: 204 No Content

No body.

### Error responses

| Status | Meaning |
|--------|---------|
| **403** | Can only delete your own comment (and you're not admin). |
| **404** | Post or comment not found. |
| **401** | Not authenticated. |

---

## Summary

| Method   | Endpoint                          | Auth        | Description                    |
|----------|-----------------------------------|------------|--------------------------------|
| **GET**  | `/api/v1/news`                    | Optional   | List published posts (feed)    |
| **GET**  | `/api/v1/news/{id}`               | Optional   | Get post by ID (detail)        |
| **GET**  | `/api/v1/news/{id}/comments`      | No         | Get comments (paginated)       |
| **POST** | `/api/v1/news`                    | Admin      | Create post                    |
| **PUT**  | `/api/v1/news/{id}`               | Admin      | Update post                    |
| **DELETE** | `/api/v1/news/{id}`             | Admin      | Delete post                    |
| **GET**  | `/api/v1/news/admin/all`         | Admin      | List all posts (incl. drafts)  |
| **POST** | `/api/v1/news/{id}/like`         | User/Guide/Admin | Like post              |
| **DELETE** | `/api/v1/news/{id}/like`       | User/Guide/Admin | Unlike post            |
| **POST** | `/api/v1/news/{id}/comments`     | User/Guide/Admin | Add comment             |
| **DELETE** | `/api/v1/news/{id}/comments/{commentId}` | User/Guide/Admin | Delete comment |

- **Optional auth**: Use for feed/detail when you want `likedByCurrentUser`.
- **Admin**: JWT with role **ADMIN** required.
- **User/Guide/Admin**: Any authenticated user can like, unlike, comment, and delete own comment.
