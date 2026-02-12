# Push notifications when app is closed (FCM)

To receive chat notifications when the app is **quit** on the receiving device:

## 1. Firebase Console

1. Go to [Firebase Console](https://console.firebase.google.com/) and create or use a project.
2. Add an Android app with package name **`com.jammit`**.
3. Download **`google-services.json`** and replace `app/google-services.json` in this project.
4. In Project settings → Cloud Messaging, ensure **Cloud Messaging API (Legacy)** or **Firebase Cloud Messaging API (V1)** is enabled.
5. For the backend: Project settings → Service accounts → Generate new private key. Save the JSON file somewhere safe.

## 2. Backend

1. Set the environment variable to the **absolute path** of the service account JSON file:
   - **`GOOGLE_APPLICATION_CREDENTIALS=/path/to/your-service-account.json`**
2. Restart the backend. When a new message is sent, the server will send an FCM notification to the recipient’s device if they have registered a token (e.g. after opening the app once while logged in).

## 3. Android

- The app already registers the FCM token with the backend when the user is logged in (on opening the app).
- No extra code is required once `google-services.json` is replaced with your Firebase project file.

If `GOOGLE_APPLICATION_CREDENTIALS` is not set, the backend still works; it just won’t send push notifications when the receiving app is closed.
