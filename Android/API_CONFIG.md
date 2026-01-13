# API Configuration

## Base URL

The app is configured to connect to the backend API. The base URL is set in `RetrofitClient.kt`:

- **Android Emulator**: `http://10.0.2.2:3000/` (this is the default)
- **Physical Device**: Change to your computer's IP address, e.g., `http://192.168.1.100:3000/`

### How to find your computer's IP address:

**macOS/Linux:**
```bash
ifconfig | grep "inet " | grep -v 127.0.0.1
```

**Windows:**
```bash
ipconfig
```

Look for your local network IP (usually starts with 192.168.x.x or 10.0.x.x)

### To change the base URL:

Edit `app/src/main/java/com/jammit/network/RetrofitClient.kt`:

```kotlin
private const val BASE_URL = "http://YOUR_IP_ADDRESS:3000/"
```

## Network Security

The app allows cleartext HTTP traffic (configured in `AndroidManifest.xml`) for development. For production, you should:

1. Use HTTPS
2. Remove `android:usesCleartextTraffic="true"` from the manifest
3. Configure network security config

## Testing

1. Make sure the backend is running on port 3000
2. Make sure the backend and Android device/emulator are on the same network
3. Check backend logs for incoming requests
4. Check Android Logcat for network errors
