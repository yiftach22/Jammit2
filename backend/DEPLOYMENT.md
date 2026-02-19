# Deploying the backend online

To make the server available on the internet (so the Android app works from any network):

## 1. Choose a host

Options that work well with NestJS + PostgreSQL:

- **[Render](https://render.com)** – Free tier: Web Service + PostgreSQL. Simple.
- **[Railway](https://railway.app)** – Free tier, good for Node + Postgres.
- **[Fly.io](https://fly.io)** – Free allowance, you add a Postgres app.
- **DigitalOcean / AWS / GCP** – More control, more setup.

## 2. What to deploy

- **Runtime:** Node.js (build: `npm install && npm run build`, start: `node dist/main.js` or `npm run start:prod`).
- **Database:** PostgreSQL. Use the host’s managed Postgres or an external one (e.g. [Neon](https://neon.tech), [Supabase](https://supabase.com)).
- **Environment variables:** Set the same as local (e.g. in the host’s dashboard):
  - `DB_HOST`, `DB_PORT`, `DB_USERNAME`, `DB_PASSWORD`, `DB_NAME`
  - `PORT` (often set automatically by the host, e.g. 3000 or from `process.env.PORT`)
  - `GOOGLE_APPLICATION_CREDENTIALS` or the service account JSON **contents** (see below)
  - Optionally `FRONTEND_URL` for CORS

## 3. FCM (push notifications) in production

The server needs the Firebase service account to send FCM. Two options:

- **Option A (file):** Set `GOOGLE_APPLICATION_CREDENTIALS` to the path of your service account JSON on the server.
- **Option B (env var, recommended for Render/Railway):** Set `FIREBASE_SERVICE_ACCOUNT_JSON` to the **entire contents** of the JSON key (as a single line or escaped string). The backend will use it automatically; no file upload needed.

## 4. CORS

Set `FRONTEND_URL` to your app’s origin if you add a web frontend. For the Android app only, you can keep CORS permissive or set it to `*` for the API (Socket.io and FCM are not browser CORS).

## 5. Android app

In **Android/app/build.gradle.kts**, set your deployed URL:

```kotlin
buildConfigField("String", "BACKEND_BASE_URL", "\"https://your-app-name.onrender.com\"")
```

Use your real URL (no trailing slash). Rebuild the app; it will use this for both REST API and WebSocket (`/ws`).

## 6. HTTPS

Hosts like Render/Railway/Fly provide HTTPS. Use `https://` in `BACKEND_BASE_URL`. The backend runs behind their proxy; you usually don’t need to configure SSL yourself.

## 7. Quick start (Render example)

1. Create a **Web Service** connected to your repo (or deploy from CLI).
2. Build command: `npm install && npm run build`
3. Start command: `npm run start:prod` (or `node dist/main`)
4. Add a **PostgreSQL** database on Render and link it; use the auto-provided `DATABASE_URL` or set `DB_HOST`, `DB_PORT`, `DB_USERNAME`, `DB_PASSWORD`, `DB_NAME`.
5. Set env vars (including DB and `GOOGLE_APPLICATION_CREDENTIALS` or Firebase JSON).
6. Deploy. Copy the service URL (e.g. `https://jammit-api.onrender.com`) into `BACKEND_BASE_URL` in the Android app and rebuild.
