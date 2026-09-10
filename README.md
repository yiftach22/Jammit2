# Jammit — Location-Based Musician Matching App

[![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack_Compose-7F52FF?logo=kotlin&logoColor=white)](https://developer.android.com/jetpack/compose)
[![NestJS](https://img.shields.io/badge/NestJS-11-E0234E?logo=nestjs&logoColor=white)](https://nestjs.com/)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-TypeORM-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Socket.IO](https://img.shields.io/badge/Socket.IO-Real--time_Chat-010101?logo=socket.io&logoColor=white)](https://socket.io/)
[![FCM](https://img.shields.io/badge/FCM-Push_Notifications-FFCA28?logo=firebase&logoColor=black)](https://firebase.google.com/docs/cloud-messaging)

Jammit connects musicians who want to jam together. Users set up a profile (instruments + skill level), browse nearby musicians filtered by instrument/level/distance, and chat in real time once they connect.

**Jammit2** is a full rebuild of [Jammit v1](https://github.com/yiftach22/Jammit-post-pc-course-project-), moving off an all-Firebase stack to a self-designed backend — built as a deliberate exercise in owning the server side end-to-end. Built with use of AI tooling (Cursor) for both design and implementation.

---

## Why a rebuild

v1 used Firebase for everything — auth, database, and realtime. v2 keeps Firebase only where it's the right tool (Cloud Messaging for push) and replaces the rest with a real backend: a NestJS REST API, a PostgreSQL database via TypeORM, and a Socket.IO layer for real-time chat. The Android client was also rebuilt on more modern Jetpack Compose with an MVVM architecture, replacing the original View-based UI.

---

## Features

- **Auth** — email/password registration and login (bcrypt + JWT). Username availability check during signup.
- **Profile** — set username, musician level (Beginner / Intermediate / Advanced / Professional), and a multi-select list of instruments with per-instrument level.
- **Explore** — browse nearby musicians, filtered by instrument, level, and search radius; distance computed server-side with the Haversine formula.
- **Real-time chat** — one-on-one conversations over a Socket.IO gateway (room-per-chat), with message history persisted in Postgres.
- **Push notifications** — when a recipient isn't connected to the chat socket, a push notification is sent via Firebase Cloud Messaging instead.

**Not yet implemented:** Google Sign-In (the endpoint exists and explicitly throws "not implemented" — email/password is the only working auth path).

---

## Architecture

```
┌────────────────────────────────┐
│  Android Client (Kotlin)        │
│  Jetpack Compose · MVVM         │
│  Retrofit (REST) + Socket.IO    │
└──────────────┬───────────────────┘
               │ REST (HTTP) + WebSocket (/ws)
               ▼
┌────────────────────────────────┐
│  NestJS API                     │
│  Auth · Users · Explore · Chats │
└──────────────┬───────────────────┘
               │ TypeORM
      ┌────────┴────────┐
      ▼                 ▼
┌────────────┐   ┌───────────────┐
│ PostgreSQL  │   │ Firebase Admin│
│ Users/Chats │   │ FCM push      │
└────────────┘   └───────────────┘
```

---

## Tech Stack

| Layer          | Technology                                                        |
|----------------|--------------------------------------------------------------------|
| Android Client | Kotlin, Jetpack Compose, Material 3, Navigation Compose, MVVM      |
| Networking     | Retrofit + OkHttp (REST), socket.io-client (chat)                  |
| Location/Auth  | Google Play Services (Location, Auth)                              |
| Backend        | NestJS 11, TypeScript                                              |
| Database       | PostgreSQL, TypeORM                                                 |
| Real-time      | Socket.IO (NestJS WebSocket gateway)                                |
| Auth           | JWT + bcrypt                                                        |
| Notifications  | Firebase Cloud Messaging (firebase-admin on the backend)            |
| Testing        | Jest (backend unit + e2e)                                           |

---

## Project Structure

```
├── Android/
│   └── app/src/main/java/com/jammit/
│       ├── data/            # models, session, unread-message store
│       ├── network/         # ApiService (Retrofit), RetrofitClient, chat notifications
│       ├── repository/      # Auth, Chats, Explore, Instruments, User repositories
│       ├── navigation/      # NavRoutes, nav graph
│       └── ui/               # auth, profile, explore, chats, chatdetail, userprofile, theme
│
└── backend/
    └── src/
        ├── entities/         # User, Instrument, InstrumentWithLevel, Chat, Message
        ├── modules/
        │   ├── auth/         # register, login, JWT
        │   ├── users/        # CRUD + push.service.ts (FCM)
        │   ├── instruments/  # instrument catalog
        │   ├── explore/      # nearby-user search (Haversine + filters)
        │   └── chats/        # REST history + chat.gateway.ts (Socket.IO)
        ├── dto/
        ├── config/           # TypeORM data source
        └── scripts/          # seed-instruments, seed-dummy-users
```

---

## Getting Started

### Backend

```bash
cd backend

# 1. Start Postgres (Docker — recommended)
docker-compose up -d

# 2. Install deps
npm install

# 3. Copy the example env file and fill in your own values
cp .env.example .env

# 4. Run
npm run start:dev
```

API runs at `http://localhost:3000`, WebSocket gateway at `/ws`. See `backend/SETUP.md` for a local (non-Docker) Postgres install and troubleshooting.

### Android

1. Open `Android/` in Android Studio and sync Gradle.
2. Add your own Firebase project's `google-services.json` under `Android/app/` (see `Android/FCM_SETUP.md`).
3. Set the API base URL in `RetrofitClient.kt` — `10.0.2.2:3000` works out of the box for the emulator; for a physical device, use your machine's LAN IP (see `Android/API_CONFIG.md`).
4. Run on an emulator or device.

### Tests

```bash
cd backend
npm run test       # unit tests
npm run test:e2e   # e2e tests
```

---

## Status

Actively developed. v1 remains on GitHub for reference but is superseded by this repo.

**Note for contributors:** `backend/README.md` and `Android/README.md` currently describe an earlier milestone (mocked auth, no real-time messaging) that predates the current implementation — the feature list above reflects the actual code as of this rebuild. Worth reconciling those two docs before relying on them.
