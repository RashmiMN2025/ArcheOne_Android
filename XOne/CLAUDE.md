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

- Supports multiple authentication methods (MPIN, Biometric, SSO)
- Implements secure token-based authentication
- Handles Microsoft SSO integration for enterprise login
- Manages session persistence and automatic token refresh
- Implements multi-factor authentication with OTP verification
- Provides fallback mechanisms for authentication failures
- Encrypts and securely stores login credentials
- Supports offline authentication with cached credentials