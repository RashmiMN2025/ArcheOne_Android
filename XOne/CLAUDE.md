# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

XOne is a comprehensive enterprise Android application developed by Arche Global that serves as an employee portal. The app provides various HR and operational services including travel management, document handling, asset tracking, SOS features, and internal communications.

## Build & Development Commands

### Building the Project
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK 
./gradlew assembleRelease

# Build Android App Bundle (AAB)
./gradlew bundleRelease

# Clean build
./gradlew clean

# Clean and rebuild everything
./gradlew clean assembleDebug
```

### Code Quality & Linting
```bash
# Run Ktlint formatting check
./gradlew ktlintCheck

# Auto-format code with Ktlint
./gradlew ktlintFormat

# Run Android Lint
./gradlew lint
```

### Testing
```bash
# Run unit tests
./gradlew testDebugUnitTest

# Run instrumented tests
./gradlew connectedDebugAndroidTest

# Run all tests
./gradlew test
```

### Installation & Deployment
```bash
# Install debug APK on connected device
./gradlew installDebug

# Install release APK on connected device
./gradlew installRelease
```

## Architecture Overview

### Tech Stack
- **Language**: Kotlin (100%)
- **UI Framework**: Jetpack Compose with Material Design 3
- **Architecture**: MVC/MVP pattern with some MVVM elements
- **Min SDK**: 26 (Android 8.0), Target SDK: 35 (Android 15)
- **Networking**: Retrofit 2.9.0 with OkHttp 4.11.0
- **Image Loading**: Coil 2.4.0
- **Animations**: Lottie 6.0.0
- **Security**: Biometric authentication, AES/RSA encryption

### Project Structure
```
com.archeGlobal.one/
├── controller/          # Business logic controllers (MVC pattern)
├── model/              # Data models and API responses
├── network/            # API service interfaces and Retrofit client
├── repository/         # Data repository layer
├── ui/
│   ├── activities/     # Activity classes
│   ├── components/     # Reusable Compose components
│   ├── screens/        # Screen composables
│   └── theme/          # App theming and styling
├── navigation/         # Navigation setup
└── utils/              # Utility classes and helpers
```

### Key Components

#### Authentication & Security
- Multi-factor authentication (OTP, MPIN, Biometric)
- Microsoft SSO integration
- AES/RSA encryption for sensitive data
- Certificate pinning for API security
- Secure storage with Android Security Crypto

#### Core Features
- **Home Dashboard**: Central navigation hub
- **Travel Management**: Multi-destination booking and approvals
- **Document Management**: Upload, view, and manage documents
- **Chat Support**: AI-powered chat with typing indicators
- **SOS System**: Emergency contact and safety features
- **Asset Management**: IT and office asset tracking
- **Business Cards**: Digital business cards with QR codes

#### Network Architecture
- `RetrofitClient.kt`: Centralized API client with authentication
- `AuthInterceptor`: Automatic token management and 401 handling
- `EncryptedAPIService.kt`: Secure API communications
- SSL certificate pinning for arche.global domain

#### UI Architecture
- **Compose-first approach**: Modern declarative UI
- **Custom Graphik font family**: 20 font variations
- **Material Design 3**: Dynamic theming support
- **Consistent font scaling**: Forced 1.0f scale across devices
- **Modular components**: Reusable UI elements

## Development Guidelines

### Code Style
- Uses Ktlint for code formatting (configured in build.gradle.kts)
- Kotlin official code style
- Abort on lint errors is enabled
- Java 11 compatibility

### Security Practices
- Never commit sensitive data (API keys, certificates are in assets/)
- Use EncryptedAPIService for sensitive API calls
- Implement proper certificate pinning
- Use secure storage for user data

### Testing
- Unit tests: JUnit 4 framework
- UI tests: Compose testing framework
- Instrumented tests: AndroidJUnit and Espresso

### Key Files to Understand
- `XOneApplication.kt`: Application initialization and lifecycle
- `MainActivity.kt`: Main navigation and activity setup
- `RetrofitClient.kt`: API client configuration
- `PreferencesManager.kt`: SharedPreferences wrapper
- `UserDataManager.kt`: User session management

### Navigation
- Uses Navigation Compose for screen navigation
- `AndroidNavigator.kt`: Custom navigation wrapper
- Activity-based routing with deep linking support

### State Management
- StateFlow/LiveData for reactive state
- Coroutines for asynchronous operations
- Repository pattern for data management

### Build Configuration
- Uses Gradle Version Catalog (libs.versions.toml)
- Proguard enabled for release builds
- Multi-architecture APK support (ARM, x86)
- Build variants: debug and release

## API Integration

The app communicates with Arche Global's backend services through encrypted REST APIs. Key endpoints include:
- Authentication and user management
- Travel booking and approvals
- Document upload and management
- Chat and communication services
- Asset and inventory tracking

All API communications use SSL certificate pinning and encrypted payloads for security.

## Common Development Patterns

### Controllers
Controllers handle business logic and API interactions following MVC pattern. They manage state and coordinate between models and views.

### Models
Data classes representing API responses and business entities. Use proper serialization annotations for JSON parsing.

### Compose Screens
Screen composables follow consistent patterns with state management, error handling, and loading states.

### Repository Pattern
Repositories abstract data sources and provide clean interfaces for controllers to interact with both local and remote data.

This codebase represents a mature, production-ready enterprise application with strong security practices, modern Android development patterns, and comprehensive feature coverage for employee needs.

## Cross-Screen Navigation Architecture

### HomeActivity Navigation Hub
The app uses a centralized navigation system where `HomeActivity` serves as the main navigation hub containing most screens as composables within a `NavHost`. Key architectural patterns:

#### Intent-Based Cross-Activity Navigation
For navigating from separate activities (like `AssetActivity`) back to HomeActivity screens:
```kotlin
val intent = Intent(context, HomeActivity::class.java).apply {
    putExtra("navigateTo", "track_tickets")
    putExtra("ticketCategory", "Asset Related Issue")
    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
}
```

#### Intent Processing in HomeActivity
HomeActivity processes navigation intents via:
- `onCreate()` for fresh activity starts
- `onNewIntent()` for existing activity instances
- `LaunchedEffect` dependency tracking for intent parameter changes

#### Category-Specific Data Loading
Controllers support category-specific data loading:
```kotlin
fun navigateToTrackTickets(category: String = "Helpdesk") {
    loadTicketsData(category) // Loads filtered data
    navigate("track_tickets")
}
```

### Help Desk & Ticketing System
Integrated ticketing system with category-based filtering:
- **API Endpoint**: `POST /tickets` with `{email, category}` request format
- **Categories**: "Helpdesk", "Asset Related Issue" 
- **Cross-screen access**: Track Tickets buttons in HelpDeskScreen and AssetScreen
- **Data Models**: `TicketsRequest`, `TicketsResponse`, `SupportTicket`

## UI Consistency Patterns

### TopAppBar Header Centering
Standardized header centering pattern across all screens accounts for navigation and action elements:

**For screens WITH actions (buttons on right side):**
```kotlin
Text(
    modifier = Modifier.offset(x = 24.dp), // Positive offset
    text = "Screen Title"
)
```
Used in: HelpDeskScreen, AssetScreen, TravelScreen

**For screens WITHOUT actions (only back button):**
```kotlin
Text(
    modifier = Modifier.offset(x = (-24).dp), // Negative offset  
    text = "Screen Title"
)
```
Used in: TicketTrackingScreen, FAQDetailScreen

This ensures visual centering regardless of TopAppBar button configuration.

### Icon Integration Patterns
Consistent icon usage across screens:
- **Custom drawable icons**: `query.png`, `solution.png`, `description.png`, `helpq.png`
- **Red tinting**: `ColorFilter.tint(Color(0xFFD32F2F))` for thematic consistency
- **Centered alignment**: `contentAlignment = Alignment.Center` in icon containers

## Travel Management System - Key Implementation Details

### Travel Request Submission
The travel system supports both single and multi-destination travel requests with the following architecture:

#### Request Models (for API submissions)
- **`TravelRequestSubmission`**: Main request model using camelCase serialization
- **`TravelDestinationRequest`**: Individual destination model for requests (camelCase format)
- **Helper functions**: `createSingleDestinationRequest()` and `createMultiDestinationRequest()`

#### Response Models (for API responses)
- **`TravelHistoryItem`**: Individual travel history record (snake_case format)
- **`TravelDestination`**: Destination data from API responses (snake_case format)
- **`TravelRequest`**: UI model for displaying travel requests in history

#### Key API Format Requirements
**Request Format (camelCase):**
```json
{
  "employeeName": "John Doe",
  "destinations": [
    {
      "travelDestination": "New York",
      "departureDate": "2025-07-20",
      "arrivalDate": "2025-07-25",
      "flightTimePreference": "Morning"
    }
  ]
}
```

**Response Format (snake_case):**
```json
{
  "status": 200,
  "order_history": [
    {
      "request_id": "TRV202500045",
      "employee_name": "John Doe",
      "stay_required": true,
      "Travel Details": [
        {
          "travel_destination": "New York",
          "departure_date": "2025-07-20",
          "arrival_date": "2025-07-25",
          "flight_time": "Morning"
        }
      ]
    }
  ]
}
```

#### Controller Implementation
- **`TravelController.submitTravelRequest()`**: Handles both single and multi-destination submissions
- **Navigation**: After successful submission, navigates to travel history screen (not home)
- **Error Handling**: Intelligent error suppression for response parsing issues while maintaining genuine error reporting

#### Data Flow
1. **Request Creation**: Uses `TravelDestinationRequest` with camelCase for API requests
2. **Response Parsing**: Uses `TravelDestination` with snake_case for API responses  
3. **History Display**: UI uses `TravelRequest` model with `getAllDestinations()` method
4. **State Management**: Controller manages submission state and error handling

#### Navigation Routes
- `"travel"`: Travel request form
- `"travel_history"`: Travel history list screen
- `"travel_history_detail"`: Individual request details
- `"travel_approvals"`: Manager approval screen

#### Error Handling Patterns
- **JSON Parsing Errors**: Gracefully handled for `stay_required` field type mismatches
- **Network Errors**: Proper distinction between genuine network issues and parsing failures
- **Success Flow**: Always refreshes travel history data before navigation

#### Important Data Type Fixes
- **`stay_required` field**: Updated from `Int?` to `Boolean?` in response models
- **Dual serialization support**: `TravelRequestResponse` supports both camelCase and snake_case formats

#### Testing Considerations
- Mock navigator implementations must include `navigateToTravelHistory()` method
- Response parsing is resilient to backend format changes
- History functionality remains intact with request format changes

This travel system demonstrates robust API integration with format flexibility, comprehensive error handling, and seamless user experience from submission to history tracking.

## SOS Features
- Quick emergency contact activation 
- Location sharing with predefined emergency contacts
- One-tap SOS alert mechanism
- Background service for continuous location tracking during emergency
- Encrypted communication of emergency details

## Login Flow

### **Authentication Methods**
- **OTP Login**: Email/Mobile/EmployeeID + OTP verification
- **MPIN Login**: 4-digit PIN for returning users
- **Biometric Login**: Fingerprint/Face authentication
- **MFA/SSO Login**: Microsoft SSO integration
- **Session Recovery**: Automatic token refresh after logout/expiry

### **Complete Login Flow Documentation**

#### **Scenario 1: First Time Login (Fresh Install)**
**Initial State:** No stored data, `firstTimeLogin = true`
**Flow:**
1. LoginScreen shows OTP form only
2. User enters credentials → `POST /send-otp` → OTP sent
3. Navigate to OTP screen → User enters OTP
4. `POST /otpVerify` with OTP → **Returns Token #1**
5. `POST /login` with Token #1 → Returns user data + saves `last_user_*` credentials
6. Navigate to MPIN setup (first-time users)

**Tokens Generated:** 1 new token

#### **Scenario 2: Normal Logout → Return**
**Initial State:** User data preserved in `last_user_*` preferences
**Flow:**
1. LoginScreen loads → **Background Auto-Refresh:**
   - `POST /otpVerify` with `{isBiometric: true, backgroundRefresh: true}` → **Token #1**
   - `POST /login` with Token #1 → Fresh data loaded silently, NO navigation
2. User sees "Welcome, [Name]" + Quick login options (MPIN/Biometric/MFA)
3. User clicks authentication method:
   - `POST /otpVerify` with `{isBiometric: true, backgroundRefresh: false}` → **Token #2**
   - `POST /login` with Token #2 → Navigate to Home

**Tokens Generated:** 2 tokens (background refresh + manual login)

#### **Scenario 3: Session Expiry (401 Error)**
**Flow:**
1. Any API call returns 401 → AuthInterceptor detects
2. `preferencesManager.clearSessionData()` + `userDataManager.clearSessionData()`
3. Preserve user data in `session_expired_*` preferences + preserve MPIN/biometric
4. Navigate to LoginActivity with `session_expired=true`
5. **Identical flow to Scenario 2** (background refresh + manual login)

**Tokens Generated:** 2 tokens (same as logout scenario)

#### **Scenario 4: MPIN Login**
**Flow:**
1. Background refresh happens (if applicable)
2. User clicks MPIN → Enters 4-digit PIN
3. Local MPIN validation: `mpinController.validateMpin()`
4. If valid: `POST /otpVerify` with `{isBiometric: true}` → **New Token**
5. `POST /login` with new token → Navigate to Home

**Tokens Generated:** 1 token (+ background refresh if applicable)

#### **Scenario 5: Biometric Login**
**Flow:**
1. Background refresh happens (if applicable)
2. User clicks Fingerprint → Biometric prompt → Authentication succeeds
3. `POST /otpVerify` with `{isBiometric: true}` → **New Token**
4. `POST /login` with new token → Navigate to Home

**Tokens Generated:** 1 token (+ background refresh if applicable)

#### **Scenario 6: MFA/SSO Login**
**Flow:**
1. User clicks "Login with MFA" → Terms dialog → Microsoft SSO WebView
2. User completes Microsoft authentication → Returns token from Microsoft
3. `POST /login` with Microsoft token → Navigate to Home/MPIN setup

**Tokens Generated:** 1 token (from Microsoft SSO)

#### **Scenario 7: "Login as Different User"**
**Flow:**
1. User clicks "Log in as different user" link
2. Clear ALL data: `UserDataManager.clearUserData()`, `MpinManager.clearAllMpinData()`, `BiometricHelper.disableBiometric()`, clear `last_user_*`
3. Reset to fresh state → Same as Scenario 1

**Tokens Generated:** 1 token (after fresh OTP login)

### **API Endpoints & Token Generation**

| **Endpoint** | **Purpose** | **When Called** | **Auth Header** | **Returns Token** |
|--------------|-------------|-----------------|-----------------|-------------------|
| `POST /send-otp` | Send OTP to user | First-time login, OTP method | No | No |
| `POST /otpVerify` | Verify OTP/Generate token | After OTP entry, MPIN/Biometric login, Background refresh | No | **YES** |
| `POST /login` | Get user data and establish session | After token received | **YES** (Bearer token) | No |

### **Key Implementation Details**

#### **Credential Preservation System**
- **Always Preserved:** `last_user_email`, `last_user_mobile`, `last_user_employee_id`, `last_user_name`
- **Session Expiry:** Additional `session_expired_*` preservation
- **Background Refresh:** `backgroundRefresh=true` parameter prevents auto-navigation
- **Token Storage:** Temporary token storage in PreferencesManager for API calls

#### **Session Management**
- **AuthInterceptor:** Detects 401 responses and triggers `handleTokenExpiration()`
- **clearSessionData():** Preserves re-authentication credentials
- **clearUserData():** Complete reset for different user login

#### **Security Features**
- Local MPIN validation before server calls
- Biometric authentication before token generation
- Encrypted API calls via `EncryptedAPIHelper`
- Certificate pinning for arche.global domain
- Automatic token refresh ensures fresh data

#### **Navigation Logic**
- **Background Refresh:** Updates data without navigation
- **Manual Authentication:** Always navigates to Home after success
- **MPIN Setup:** First-time users go to MPIN setup, returning users go to Home

### **Benefits of Current Implementation**
✅ **Always Fresh Data** - New tokens generated frequently
✅ **Seamless UX** - Logout and session expiry provide identical experience
✅ **Security** - No token reuse, always generate fresh authentication
✅ **Convenience** - Quick re-authentication with preserved credentials
✅ **Fallbacks** - Multiple authentication methods available

## Authentication State Management

### **Critical Implementation Detail**
The app uses a dual-check authentication system in `MainActivity.kt`:

```kotlin
val hasAuthToken = preferencesManager.getAuthToken() != null
val isLoggedInState = preferencesManager.getBoolean("isLoggedIn", false)  
val isUserLoggedIn = hasAuthToken && isLoggedInState
```

**Why this matters:**
- **Token alone is insufficient** - Background authentication can generate tokens without user interaction
- **Login state is crucial** - Only set to `true` during actual user login actions
- **Prevents logout bypass** - After logout, `isLoggedIn=false` even if background auth creates new tokens
- **Key preference keys:** `"auth_token"` and `"isLoggedIn"` (note: no underscore in isLoggedIn)

### **Logout Implementation**
Located in `ProfileController.proceedWithLocalLogout()`:
1. Sets `userDataManager.setIsLoggedIn(false)` ✅
2. Clears auth token via `clearAuthToken()` ✅  
3. Preserves user credentials for quick re-login ✅
4. Navigates to LoginActivity ✅

### **Login Success Implementation**
Both `OtpVerificationController.loginWithToken()` and `LoginController.loginWithToken()` properly call:
```kotlin
userDataManager.setIsLoggedIn(true)  // Critical for MainActivity auth check
userDataManager.setHasLoggedIn(true) // For returning user experience
```

## Claude Code Development Rules

When working on this codebase, follow these essential rules:

1. **Use TodoWrite Tool**: Always use the TodoWrite tool to plan and track tasks throughout development
2. **UI Consistency**: Follow established TopAppBar header centering patterns based on action presence
3. **Navigation Patterns**: Use intent-based navigation for cross-activity flows, preserve navigation state
4. **API Integration**: Maintain format flexibility for camelCase requests and snake_case responses
5. **Security First**: Use EncryptedAPIService for sensitive operations, never commit credentials
6. **Error Handling**: Implement graceful degradation for parsing errors while preserving genuine error reporting

### **Task Management Workflow**
- Use TodoWrite tool to create and track tasks
- Mark tasks as `pending`, `in_progress`, or `completed`
- Always provide clear, actionable task descriptions
- Break complex features into smaller, manageable steps
- Document any blockers or issues encountered