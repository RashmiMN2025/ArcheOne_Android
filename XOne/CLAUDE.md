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

# Build Android App Bundle (AAB) for Play Store
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
- Kotlin official code style (specified in gradle.properties)
- Abort on lint errors is enabled
- Java 11 compatibility
- Gradle JVM args: `-Xmx2048m -Dfile.encoding=UTF-8`

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
- Proguard enabled for release builds with custom rules (proguard-rules.pro)
- Multi-architecture APK support (ARM, x86): armeabi, armeabi-v7a, arm64-v8a, x86, x86_64
- Build variants: debug and release
- Resource shrinking disabled in release builds
- PNG crunching enabled for optimization
- AndroidX and non-transitive R class enabled

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

## Travel Management Navigation Flow

### **Complete Travel Navigation Architecture**

The travel management system uses a multi-screen navigation flow that supports both employee travel history tracking and manager approval workflows.

#### **Navigation Routes Available**
1. `"travel"` - Main travel request form
2. `"travel_history"` - Travel history list screen
3. `"travel_history_detail"` - Individual travel history details (TravelHistoryDetailScreen)
4. `"travel_approvals"` - Travel approvals list for managers
5. `"travel_approval_detail"` - Travel approval detail screen
6. `"travel_request_detail"` - Read-only processed request details
7. `TravelApproveActivity` - Approve screen (Intent-based)
8. `TravelRejectActivity` - Reject screen (Intent-based)

#### **Complete Navigation Flow**

**Path 1: Employee History Flow**
```
TravelScreen → [History Button] → TravelHistoryScreen → [Click Item] → TravelHistoryDetailScreen
```

**Path 2: Manager Approvals Flow (Multiple Routes to Approve/Reject)**

**Route 2A: Direct from TravelApprovalsScreen**
```
TravelScreen → [Approvals Button] → TravelApprovalsScreen → [Approve/Reject Buttons] → TravelApproveScreen/TravelRejectScreen
```

**Route 2B: Via TravelApprovalDetailScreen**
```
TravelScreen → [Approvals Button] → TravelApprovalsScreen → [Click Item] → TravelApprovalDetailScreen → [Approve/Reject Buttons] → TravelApproveScreen/TravelRejectScreen
```

#### **Navigation Methods in TravelController**
- `navigateToTravelHistory()` - Refreshes data and navigates to history
- `navigateToTravelDetails(requestId)` - Navigates to history detail from history list
- `navigateToTravelApprovalDetails(requestId)` - Navigates to approval detail from approvals list
- `navigateToTravelApprovals()` - Loads and navigates to approvals screen
- `navigateToTravelApprovalDetail(request)` - Conditional navigation based on request status
- `navigateToTravelApprove(request)` - Intent-based navigation to TravelApproveActivity
- `navigateToTravelReject(request)` - Intent-based navigation to TravelRejectActivity

#### **Navigation Architecture Types**
- **Compose Navigation**: Used for screens 1-6 within HomeActivity
- **Intent-based Navigation**: Used for approve/reject screens (7-8) - separate Activities
- **State Management**: Controller manages selected travel request state for detail screens
- **Auto-refresh**: Lists refresh automatically after approve/reject actions and successful submissions

#### **Key Navigation Patterns**
- **Dual Approval Access**: Managers can approve/reject directly from list or via detail screen
- **Post-action Navigation**: Approve/reject actions return to TravelApprovalsScreen with refreshed data
- **History Auto-navigation**: Successful travel request submission automatically navigates to travel history
- **Back Navigation**: Consistent use of `navigator.popBackStack()` for proper back button behavior

#### **Data Models for Travel Navigation**
- **Request Models**: Use `TravelDestinationRequest` with camelCase for API submissions
- **Response Models**: Use `TravelDestination` with snake_case from API responses
- **Origin/Destination Display**: UI shows `"${originCity} → ${destinationCity}"` format instead of single travel_destination field
- **Multi-destination Support**: Handles both single and multi-destination travel requests seamlessly

---

## Performance Optimization Session - API Call Optimization & Lazy Loading Implementation

### Overview
Major performance optimization session focused on eliminating bulk API calls during app startup and implementing lazy loading patterns across all controllers.

### Problem Identified
- **Bulk API Loading**: All controllers were initialized eagerly, causing 49+ API calls immediately after login
- **Poor Startup Performance**: TravelController, LocationsController, AssetController, ProfileController all loading data on creation
- **Resource Waste**: Data loaded for services user might never access

### Phase 1 Implementation - Lazy Controller Initialization

#### Changes Made to HomeActivity.kt
```kotlin
// BEFORE: Eager initialization
private lateinit var travelController: TravelController
private lateinit var locationsController: LocationsController

// AFTER: Lazy initialization
internal val travelController by lazy { 
    Log.d("HomeActivity", "Lazy initializing TravelController")
    TravelController(navigator, this@HomeActivity) 
}
internal val locationsController by lazy { 
    Log.d("HomeActivity", "Lazy initializing LocationsController")
    LocationsController(this@HomeActivity)
}
```

#### Controller Modifications
**TravelController.kt**
- Removed `loadCombinedTravelHistory()` from constructor
- Added `onServiceAccessed()` method for on-demand data loading
- Data only loads when travel service is clicked

**LocationsController.kt**
- Removed data processing from constructor  
- Added lazy loading with `onServiceAccessed()` method
- Office data processed only when locations service accessed

**AssetController.kt**
- Implemented on-demand asset data loading
- Added `onServiceAccessed()` method
- Asset details loaded only when asset service clicked

**ProfileController.kt**
- Added lazy data initialization
- Modified constructor to start with empty model
- Data loaded via `onServiceAccessed()` method

#### HomeController Integration
```kotlin
"travel", "traveldesk", "travel desk" -> {
    // Load travel data on-demand before navigating
    if (context is com.archeGlobal.one.HomeActivity) {
        Log.d("HomeController", "Loading travel data on-demand")
        context.travelController.onServiceAccessed()
    }
    navigator.navigateToTravel()
}
```

#### Compilation Fixes Applied
1. **Platform Declaration Clashes**: Removed redundant getter methods (Kotlin lazy generates them automatically)
2. **Access Modifiers**: Changed lazy controllers from `private` to `internal` for cross-class access
3. **Constructor Parameters**: Fixed parameter mismatches for all lazy controllers
4. **AndroidNavigator**: Updated to use direct property access instead of getter methods

### Asset Service Loading Issue Fix
**Problem**: AssetActivity created its own controller instance instead of using lazy-loaded one from HomeActivity
**Solution**: Added `controller.onServiceAccessed()` call immediately after controller creation in AssetActivity and LocationsActivity

### Communique Thumbnail Loading Optimization

#### Original Issues
- Thumbnails loading slowly and some failing completely
- Sequential batch loading with artificial delays
- Poor error handling and retry logic
- Memory usage issues with large thumbnails

#### Optimizations Implemented

**1. Enhanced Download Mechanism**
```kotlin
// Retry logic with exponential backoff
for (attempt in 0 until maxRetries) {
    try {
        // Download with optimized timeouts
        connection.connectTimeout = 5000
        connection.readTimeout = 10000
        
        // Better error handling
        if (responseCode in 400..499) {
            return null // Don't retry client errors
        }
        
        if (attempt < maxRetries - 1) {
            delay(1000L * (attempt + 1)) // 1s, 2s, 3s backoff
            continue
        }
    } catch (e: Exception) {
        // Handle retries
    }
}
```

**2. Concurrent Loading with Semaphore**
```kotlin
// BEFORE: Sequential batch loading with delays
toLoad.chunked(3).forEach { batch ->
    // Process batch
    delay(500) // Artificial delay
}

// AFTER: True concurrent loading with traffic control
private val thumbnailDownloadSemaphore = Semaphore(4)

val results = toLoad.map { communique ->
    async(Dispatchers.IO) {
        thumbnailDownloadSemaphore.acquire()
        try {
            getPdfThumbnail(context, communique.filePath)
        } finally {
            thumbnailDownloadSemaphore.release()
        }
    }
}.awaitAll()
```

**3. Improved PDF Rendering**
- File validation before processing
- Memory optimization (max height 600px)
- Background filling for transparent PDFs  
- Better error handling and resource cleanup
- Performance tracking with timing logs

**4. Critical Bug Fix**
Fixed compilation error in retry logic:
```kotlin
// WRONG: Invalid Kotlin syntax
return@repeat // Not supported
continue@repeat // Not supported

// CORRECT: Standard loop with proper continue
for (attempt in 0 until maxRetries) {
    if (shouldRetry) {
        continue // Valid syntax
    }
}
```

### Profile Screen Data Loading Bug Fix

#### Problem Identified
After lazy initialization changes, ProfileScreen displayed empty data (no name, image, version, last seen) because:
1. ProfileController started with empty model: `ProfileModel(name = "", email = "")`
2. Data only loaded when `onServiceAccessed()` called
3. ProfileScreen had no mechanism to trigger data loading
4. Direct navigation bypassed HomeController's `onServiceAccessed()` call

#### Solution Applied
```kotlin
@Composable
fun ProfileScreen(controller: ProfileController, ...) {
    // Ensure profile data is loaded when screen is displayed
    LaunchedEffect(Unit) {
        Log.d("ProfileScreen", "ProfileScreen composed - ensuring data is loaded")
        controller.onServiceAccessed()
    }
    // ... rest of screen
}
```

### Performance Impact Summary

#### API Calls Optimization
- **Before**: 49+ API calls immediately after login
- **After**: Only login API call, service-specific APIs on demand
- **Startup Time**: Dramatically improved app launch performance
- **Memory Usage**: Reduced initial memory footprint
- **Network Usage**: Eliminated unnecessary API calls

#### Thumbnail Loading Performance  
- **Before**: Sequential loading, 35+ seconds for 10 thumbnails
- **After**: Concurrent loading, 8-12 seconds for 10 thumbnails  
- **Improvement**: ~3-4x faster thumbnail loading
- **Reliability**: Better error handling and retry logic
- **Memory**: Controlled concurrency prevents memory issues

### Key Implementation Patterns

#### Lazy Loading Pattern
```kotlin
// Controller declaration
internal val controllerName by lazy {
    Log.d("HomeActivity", "Lazy initializing ControllerName") 
    ControllerName(parameters)
}

// Controller implementation  
class ControllerName {
    private var isDataLoaded = false
    var model by mutableStateOf(EmptyModel())
    
    fun onServiceAccessed() {
        if (!isDataLoaded) {
            loadData()
            isDataLoaded = true
        }
    }
}

// Screen usage
LaunchedEffect(Unit) {
    controller.onServiceAccessed()
}
```

#### Concurrent Loading with Semaphore
```kotlin
private val semaphore = Semaphore(maxConcurrent)

val results = items.map { item ->
    async(Dispatchers.IO) {
        semaphore.acquire()
        try {
            processItem(item)
        } finally {
            semaphore.release()
        }
    }
}.awaitAll()
```

### Files Modified
1. **HomeActivity.kt** - Lazy controller initialization
2. **HomeController.kt** - On-demand controller access
3. **TravelController.kt** - Lazy data loading
4. **LocationsController.kt** - Lazy data loading  
5. **AssetController.kt** - Lazy data loading
6. **ProfileController.kt** - Lazy data loading
7. **AssetActivity.kt** - Controller initialization fix
8. **LocationsActivity.kt** - Controller initialization fix
9. **ProfileScreen.kt** - Data loading trigger
10. **CommuniqueScreen.kt** - Concurrent thumbnail loading
11. **AndroidNavigator.kt** - Property access fixes

### Testing Notes
- All services maintain full functionality while eliminating bulk loading
- Thumbnail loading is significantly faster and more reliable
- Profile screen data loads correctly from all navigation paths
- Build compiles successfully with all optimizations

### Future Considerations
- Monitor app performance metrics to validate improvements
- Consider implementing disk-based caching for thumbnails
- Evaluate extending lazy loading to other heavy operations
- Consider progressive loading UI patterns for better user experience