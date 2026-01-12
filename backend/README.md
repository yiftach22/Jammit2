# Jammit Backend

NestJS backend for the Jammit musician connection app.

## Tech Stack

- **Framework**: NestJS
- **Language**: TypeScript
- **Database**: PostgreSQL
- **ORM**: TypeORM
- **API**: RESTful

## Setup

### Prerequisites

- Node.js (v18 or higher)
- PostgreSQL (v12 or higher)
- npm or yarn

### Installation

1. Install dependencies:
```bash
npm install
```

2. Create a `.env` file based on `.env.example`:
```bash
cp .env.example .env
```

3. Update the `.env` file with your database credentials:
```
DB_HOST=localhost
DB_PORT=5432
DB_USERNAME=postgres
DB_PASSWORD=postgres
DB_NAME=jammit
PORT=3000
NODE_ENV=development
FRONTEND_URL=http://localhost:8080
```

4. Create the PostgreSQL database:
```bash
createdb jammit
```

5. Run the application:
```bash
npm run start:dev
```

The API will be available at `http://localhost:3000`

## Database Schema

### Entities

- **User**: User profiles with location data
- **Instrument**: Available instruments (Guitar, Piano, etc.)
- **InstrumentWithLevel**: User's instruments with proficiency levels
- **Chat**: Chat conversations between users

### Seeding Instruments

To seed the database with default instruments, you can create a simple script or use the TypeORM CLI. The instruments will be automatically created on first run if they don't exist.

## API Endpoints

### Authentication (Mocked)
- `POST /auth/login` - Login with email/password
- `POST /auth/register` - Register new user
- `POST /auth/google` - Login with Google

### Users
- `GET /users` - Get all users
- `GET /users/:id` - Get user by ID
- `POST /users` - Create new user
- `PATCH /users/:id` - Update user
- `DELETE /users/:id` - Delete user

### Instruments
- `GET /instruments` - Get all instruments
- `GET /instruments/:id` - Get instrument by ID

### Explore
- `GET /explore?currentUserId=:id&instrumentIds=:ids&level=:level&searchRadiusKm=:radius` - Find nearby musicians with filters

### Chats
- `GET /chats/:userId` - Get all chats for a user
- `GET /chats/:userId/:chatId` - Get specific chat
- `POST /chats` - Find or create chat between two users

## Development

```bash
# Development mode with hot reload
npm run start:dev

# Production build
npm run build
npm run start:prod

# Run tests
npm run test
npm run test:e2e
```

## Notes

- Authentication is currently mocked (no real implementation)
- Messages functionality is not implemented yet
- Location-based search uses Haversine formula for distance calculation
- Database schema auto-syncs in development mode (set `NODE_ENV=production` to disable)
