# Messaging API – Internal Chat (Customers, Guides, Admins)

Base URL: **`/api/v1/messages`**  
Example: `http://localhost:8080/api/v1/messages`

---

## Authentication

All messaging endpoints require an authenticated user. Send the JWT in the header:

```http
Authorization: Bearer <your_jwt_token>
```

Any logged-in user (customer **USER**, guide **GUIDE**, or **ADMIN**) can send and receive messages. Users only see their own conversations.

---

## Overview

The messaging service provides internal chat between:

- **Customers** (role `USER`) – can message guides and admins
- **Guides** (role `GUIDE`) – can message customers and admins
- **Admins** (role `ADMIN`) – can message customers and guides

Messages are one-to-one (user to user). Optionally, a message can be linked to a **booking** (e.g. chat about a specific tour).

---

# Messaging API

## 1. Send a message

**POST** `/api/v1/messages`

Send a message to another user.

### Request body

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `receiverId` | number | Yes | User ID of the recipient. |
| `message` | string | Yes | Message text; max **5000** characters. |
| `bookingId` | number | No | Optional booking ID to link the message to (e.g. tour discussion). |

### Example request

```http
POST /api/v1/messages
Authorization: Bearer <your_jwt_token>
Content-Type: application/json

{
  "receiverId": 5,
  "message": "Hi, I'd like to ask about the tour schedule.",
  "bookingId": 12
}
```

### Response: 201 Created

```json
{
  "id": 42,
  "senderId": 1,
  "senderName": "Jane Doe",
  "receiverId": 5,
  "receiverName": "John Guide",
  "bookingId": 12,
  "message": "Hi, I'd like to ask about the tour schedule.",
  "isRead": false,
  "createdAt": "2026-02-18T14:30:00"
}
```

**Response fields:**

| Field | Type | Description |
|-------|------|-------------|
| `id` | number | Message ID. |
| `senderId` | number | Sender user ID. |
| `senderName` | string | Sender display name. |
| `receiverId` | number | Receiver user ID. |
| `receiverName` | string | Receiver display name. |
| `bookingId` | number \| null | Linked booking ID, if any. |
| `message` | string | Message content. |
| `isRead` | boolean | Whether the message has been read by the receiver. |
| `createdAt` | string | ISO-8601 date-time. |

### Error responses

| Status | Meaning |
|--------|---------|
| **400** | Invalid input, or trying to send a message to yourself. |
| **404** | Receiver or booking (if provided) not found. |
| **401** | Not authenticated. |

---

## 2. List my conversations

**GET** `/api/v1/messages/conversations`

Returns all conversations for the current user. Each item represents one other user (partner) with a last-message preview and unread count.

### Example request

```http
GET /api/v1/messages/conversations
Authorization: Bearer <your_jwt_token>
```

### Response: 200 OK

```json
[
  {
    "partnerId": 5,
    "partnerName": "John Guide",
    "partnerEmail": "john@example.com",
    "partnerRole": "GUIDE",
    "lastMessagePreview": "Sure, we can do 2pm on Saturday.",
    "lastMessageAt": "2026-02-18T14:35:00",
    "unreadCount": 2
  },
  {
    "partnerId": 3,
    "partnerName": "Admin User",
    "partnerEmail": "admin@seaandtea.com",
    "partnerRole": "ADMIN",
    "lastMessagePreview": "Your booking has been confirmed.",
    "lastMessageAt": "2026-02-17T10:00:00",
    "unreadCount": 0
  }
]
```

**Response fields (per conversation):**

| Field | Type | Description |
|-------|------|-------------|
| `partnerId` | number | Other user's ID. |
| `partnerName` | string | Partner display name. |
| `partnerEmail` | string | Partner email. |
| `partnerRole` | string | `USER`, `GUIDE`, or `ADMIN`. |
| `lastMessagePreview` | string | Snippet of the last message (max 100 chars). |
| `lastMessageAt` | string | ISO-8601 date-time of last message. |
| `unreadCount` | number | Unread messages from this partner to the current user. |

---

## 3. Get messages with a user (conversation thread)

**GET** `/api/v1/messages/conversations/{partnerId}`

Returns paginated messages between the current user and the given partner. Newest first.

### Path parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `partnerId` | number | The other user's ID in the conversation. |

### Query parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `page` | number | No | Page index (0-based). Default: `0`. |
| `size` | number | No | Page size. Default: `20`. |

### Example request

```http
GET /api/v1/messages/conversations/5?page=0&size=20
Authorization: Bearer <your_jwt_token>
```

### Response: 200 OK

```json
{
  "content": [
    {
      "id": 43,
      "senderId": 5,
      "senderName": "John Guide",
      "receiverId": 1,
      "receiverName": "Jane Doe",
      "bookingId": null,
      "message": "Sure, we can do 2pm on Saturday.",
      "isRead": true,
      "createdAt": "2026-02-18T14:35:00"
    },
    {
      "id": 42,
      "senderId": 1,
      "senderName": "Jane Doe",
      "receiverId": 5,
      "receiverName": "John Guide",
      "bookingId": 12,
      "message": "Hi, I'd like to ask about the tour schedule.",
      "isRead": true,
      "createdAt": "2026-02-18T14:30:00"
    }
  ],
  "pageable": { ... },
  "totalElements": 15,
  "totalPages": 1,
  "size": 20,
  "number": 0,
  "first": true,
  "last": true,
  "numberOfElements": 2,
  "empty": false
}
```

### Error responses

| Status | Meaning |
|--------|---------|
| **400** | Invalid partner (e.g. using your own user ID). |
| **404** | Partner user not found. |
| **401** | Not authenticated. |

---

## 4. Mark conversation as read

**PUT** `/api/v1/messages/conversations/{partnerId}/read`

Marks all messages **from** the given partner **to** the current user as read.

### Path parameters

| Parameter | Type | Description |
|-----------|------|-------------|
| `partnerId` | number | User ID of the partner whose messages to mark as read. |

### Example request

```http
PUT /api/v1/messages/conversations/5/read
Authorization: Bearer <your_jwt_token>
```

### Response: 204 No Content

No body.

### Error responses

| Status | Meaning |
|--------|---------|
| **404** | Partner not found. |
| **401** | Not authenticated. |

---

## 5. Get unread message count

**GET** `/api/v1/messages/unread-count`

Returns the total number of unread messages for the current user (across all conversations).

### Example request

```http
GET /api/v1/messages/unread-count
Authorization: Bearer <your_jwt_token>
```

### Response: 200 OK

```json
{
  "unreadCount": 3
}
```

---

## Summary

| Method | Endpoint | Description |
|--------|----------|-------------|
| **POST** | `/api/v1/messages` | Send a message |
| **GET** | `/api/v1/messages/conversations` | List my conversations |
| **GET** | `/api/v1/messages/conversations/{partnerId}` | Get messages with a user (paginated) |
| **PUT** | `/api/v1/messages/conversations/{partnerId}/read` | Mark conversation as read |
| **GET** | `/api/v1/messages/unread-count` | Get total unread count |

All endpoints require **Authentication** (JWT). Users only access their own conversations.
