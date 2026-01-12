# Jammit - Musician Connection App

An Android app for connecting musicians built with Jetpack Compose, Kotlin, and Material 3.

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Navigation**: Navigation Compose
- **Material Design**: Material 3
- **Dependency Injection**: ViewModel factory (can be extended with Hilt/Dagger)

## Project Structure

```
app/src/main/java/com/jammit/
├── data/
│   └── model/
│       ├── User.kt
│       ├── Instrument.kt
│       ├── Chat.kt
│       ├── Message.kt
│       └── MusicianLevel.kt (enum)
├── navigation/
│   ├── NavRoutes.kt (sealed classes for routes)
│   └── Navigation.kt (navigation graph and bottom nav)
├── ui/
│   ├── auth/
│   │   ├── AuthViewModel.kt
│   │   ├── LoginScreen.kt
│   │   └── RegisterScreen.kt
│   ├── profile/
│   │   ├── ProfileViewModel.kt
│   │   └── ProfileScreen.kt
│   ├── explore/
│   │   ├── ExploreViewModel.kt
│   │   └── ExploreScreen.kt
│   ├── chats/
│   │   ├── ChatsViewModel.kt
│   │   └── ChatsScreen.kt
│   ├── chatdetail/
│   │   ├── ChatDetailViewModel.kt
│   │   └── ChatDetailScreen.kt
│   ├── userprofile/
│   │   ├── UserProfileViewModel.kt
│   │   └── UserProfileScreen.kt
│   └── theme/
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── MainActivity.kt
└── JammitApplication.kt
```

## Features

### Authentication Flow
- Login screen with email/password and Google Sign-In options
- Registration screen
- Mock authentication (no real backend)

### Main Screen with Bottom Navigation
- **Profile Tab**: Edit user profile (username, musician level, instruments)
- **Explore Tab**: Browse nearby musicians with filters
- **Chats Tab**: View and manage conversations

### Explore Tab Features
- List of nearby musicians (mock data)
- Filter by:
  - Instrument(s)
  - Musician level
  - Search radius (slider, 1-50 km)
- User profile preview with distance

### Chat Features
- List of recent chats
- One-on-one chat screen
- Message list with timestamps
- Text input and send functionality

### Profile Features
- View and edit own profile
- Update username
- Select musician level (BEGINNER, INTERMEDIATE, ADVANCED, PROFESSIONAL)
- Multi-select instruments from predefined list

## Current Status

This is a **UI-only implementation** with mock data. The following are not implemented:
- Real authentication (Google Sign-In, Firebase, etc.)
- Backend integration
- Real location services and permissions
- Real-time messaging
- Image loading (uses placeholder icons)

## Setup Instructions

1. Open the project in Android Studio
2. Sync Gradle files
3. Generate launcher icons using Android Studio (right-click res folder → New → Image Asset)
4. Build and run the app
5. Use any email/password combination on login/register screens (authentication is mocked)

**Note**: The project references launcher icons (`ic_launcher` and `ic_launcher_round`) in the manifest. You'll need to generate these using Android Studio's Image Asset Studio, or the app will use default Android icons.

## Next Steps

To make this a production-ready app, you would need to:
1. Integrate Firebase Authentication for Google Sign-In and email/password
2. Set up a backend (Firebase, custom API, etc.)
3. Implement real location services with permissions
4. Add real-time messaging (Firebase Firestore, WebSockets, etc.)
5. Implement image upload and loading (Cloud Storage, Coil/Glide)
6. Add dependency injection (Hilt/Dagger)
7. Add unit and UI tests
8. Implement error handling and loading states
9. Add data persistence (Room, DataStore)

