# Assistive Tap - Claude Development Guide

## Project Overview

**Assistive Tap** (AssistiPunkt) is an Android accessibility application that provides a floating navigation dot for users with limited mobility, especially elderly users. It enables one-handed navigation through customizable gesture controls.

**Current Version:** 2.0.0 (Code: 8)
**Target Users:** Elderly users and people with motor impairments
**Language:** Kotlin (100%)
**Min SDK:** 26 (Android 8.0) | **Target SDK:** 36 (Android 15)

---

## Tech Stack

### Core Technologies
- **Language:** Kotlin 2.0.21 with Java 11 target
- **Build System:** Gradle 8.13.0 with Kotlin DSL
- **Architecture:** Clean Architecture (Domain → Data → Presentation)
- **Async:** Kotlin Coroutines & Flows
- **UI:** Material Design 3 with AndroidX libraries

### Key Android APIs Used
- **WindowManager Overlay API** - Floating dot rendering (`TYPE_APPLICATION_OVERLAY`)
- **Accessibility Service API** - System navigation actions (Home, Back, Recents)
- **WindowInsets API** - Keyboard detection and avoidance
- **SharedPreferences** - Settings persistence with reactive updates

### Dependencies
```
androidx.core:core-ktx:1.17.0
androidx.appcompat:appcompat:1.7.1
com.google.android.material:material:1.13.0
androidx.activity:activity:1.11.0
androidx.constraintlayout:constraintlayout:2.2.1
```

---

## Architecture & Design Patterns

### Clean Architecture Layers
```
Domain Layer (business logic)
  ├── model/ (TapBehavior, DotPosition, OverlaySettings, Gesture)
  ├── repository/ (SettingsRepository interface)
  └── usecase/ (OverlayUseCases)
       ↓
Data Layer (persistence)
  ├── local/ (SharedPreferencesDataSource)
  └── repository/ (SettingsRepositoryImpl)
       ↓
Presentation Layer (UI & services)
  ├── ui/ (MainActivity, SettingsActivity)
  └── service/overlay/ (OverlayService, KeyboardManager, etc.)
```

### Key Patterns
- **Repository Pattern** - Abstract data access
- **Service Locator** - Manual DI (preparing for Hilt migration)
- **Observer Pattern** - Kotlin Flows for reactive settings
- **Composition Over Inheritance** - Specialized components

---

## Critical Components

### 1. OverlayService (459 lines)
**Location:** `app/src/main/java/ch/heuscher/back_home_dot/service/overlay/OverlayService.kt`

Main service orchestrating all overlay functionality:
- Manages foreground service lifecycle
- Coordinates all sub-components
- Handles settings changes via Flows
- Manages broadcast receivers

### 2. KeyboardManager (273 lines)
**Location:** `app/src/main/java/ch/heuscher/back_home_dot/service/overlay/KeyboardManager.kt`

Smart keyboard avoidance system:
- Detects keyboard via WindowInsets API (Android R+)
- Estimates keyboard height on older devices
- Moves dot above keyboard with 1.5x diameter margin
- Restores position when keyboard closes

### 3. GestureDetector
**Location:** `app/src/main/java/ch/heuscher/back_home_dot/service/overlay/GestureDetector.kt`

Gesture recognition engine:
- Tap counting with timeout logic
- Long-press detection (500ms threshold)
- Drag detection with touch slop
- System-conformant gesture timeouts

### 4. OrientationHandler (97 lines)
**Location:** `app/src/main/java/ch/heuscher/back_home_dot/service/overlay/OrientationHandler.kt`

Rotation handling:
- Detects screen rotation (0°, 90°, 180°, 270°)
- Transforms position mathematically for all rotations
- Prevents visual "jumping" during rotation

### 5. BackHomeAccessibilityService
**Location:** `app/src/main/java/ch/heuscher/back_home_dot/BackHomeAccessibilityService.kt`

System navigation actions:
- Performs Home, Back, Recents actions via Accessibility API
- Detects foreground package for Home screen logic
- Applies action delays (100-150ms) for reliability

---

## Key Features & Behavior Modes

### Two Tap Behavior Modes

| Mode | 1x Tap | 2x Tap | 3x Tap | 4+ Tap | Long Press |
|------|--------|--------|--------|--------|------------|
| **SAFE_HOME** (Default) | Home | Home | Home | Open app | Home |
| **NAVI** | Back | Previous app | Recents | Open app | Home |

**Note:** The STANDARD mode has been replaced with SAFE_HOME as the default mode for improved safety and simplicity for elderly users. Existing users with STANDARD mode will be automatically migrated to SAFE_HOME.

### Smart Features
- **Keyboard Avoidance** - Auto-moves dot above keyboard
- **Position Persistence** - Saves position as percentage (device-independent)
- **Orientation Handling** - Maintains relative position across rotations
- **Customization** - Color (with user-friendly HSV picker), transparency, timeout settings

---

## Important Constants & Configuration

**Location:** `app/src/main/java/ch/heuscher/back_home_dot/util/AppConstants.kt`

```kotlin
OVERLAY_SIZE = 48 (dp)              // Dot size (accessibility minimum)
TOUCH_SLOP = 10 (px)                // Drag detection threshold
CLICK_TIMEOUT = 300 (ms)            // Multi-tap timeout
LONG_PRESS_TIMEOUT = 500 (ms)       // Long-press threshold
RECENTS_DEFAULT_TIMEOUT = 100 (ms)  // Delay before Recents action
DEFAULT_COLOR = Color.BLACK         // Default dot color
DEFAULT_ALPHA = 255                 // Default opacity (0-255)
KEYBOARD_MARGIN_MULTIPLIER = 1.5f   // Margin above keyboard
```

---

## Data Models

### OverlaySettings
**Location:** `app/src/main/java/ch/heuscher/back_home_dot/domain/model/OverlaySettings.kt`

```kotlin
data class OverlaySettings(
    val isEnabled: Boolean,
    val color: Int,                           // ARGB color
    val alpha: Int,                           // 0-255
    val position: DotPosition,                // Current pixels
    val positionPercent: DotPositionPercent,  // Device-independent %
    val recentsTimeout: Long,                 // ms
    val keyboardAvoidanceEnabled: Boolean,
    val tapBehavior: String,                  // "STANDARD", "NAVI", "SAFE_HOME"
    val screenWidth: Int,
    val screenHeight: Int,
    val rotation: Int                         // 0, 90, 180, 270
)
```

### Position Conversion
- **Pixel Position** → **Percent Position** (for persistence)
- Ensures dot appears in same relative location across devices/orientations
- Conversion happens in `SettingsRepository` before saving

---

## Required Permissions

### AndroidManifest.xml
1. `SYSTEM_ALERT_WINDOW` - Draw overlay window
2. `RECEIVE_BOOT_COMPLETED` - Auto-start on boot
3. `FOREGROUND_SERVICE` - Run service in foreground
4. `FOREGROUND_SERVICE_SPECIAL_USE` - Special service type
5. `POST_NOTIFICATIONS` - Notifications (Android 13+)

### Runtime Permissions
1. **Overlay Permission** - `Settings.canDrawOverlays()`
2. **Accessibility Service** - Enabled in system settings

**Permission Flow:** MainActivity handles permission checks and guides user through enabling required permissions.

---

## Build & Development

### Build Commands
```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (auto-increments version)
./gradlew assembleRelease

# Run unit tests
./gradlew testDebugUnitTest

# Install debug build
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Version Management
**File:** `version.properties`
```properties
VERSION_MAJOR=2
VERSION_MINOR=0
VERSION_PATCH=0
VERSION_CODE=8
```
- Auto-increments on release builds
- Version name: `${MAJOR}.${MINOR}.${PATCH}`

### ProGuard Configuration
**File:** `app/proguard-rules.pro`
- Keeps AccessibilityService, OverlayService, Activities
- Preserves Parcelable and Serializable classes
- Retains source file info for crash reports

---

## Project Structure

```
app/src/main/java/ch/heuscher/back_home_dot/
├── domain/                    # Business logic (no Android dependencies)
│   ├── model/                # Data models (TapBehavior, DotPosition, etc.)
│   ├── repository/           # Repository interfaces
│   └── usecase/              # Business use cases
│
├── data/                     # Data access layer
│   ├── local/                # SharedPreferences data source
│   └── repository/           # Repository implementations
│
├── service/overlay/          # Overlay service components
│   ├── OverlayService.kt     # Main service (459 lines)
│   ├── KeyboardManager.kt    # Keyboard avoidance (273 lines)
│   ├── GestureDetector.kt    # Gesture recognition
│   ├── OrientationHandler.kt # Rotation handling (97 lines)
│   ├── PositionAnimator.kt   # Position animations (86 lines)
│   ├── KeyboardDetector.kt   # Keyboard detection
│   └── OverlayViewManager.kt # View creation & positioning
│
├── ui/                       # User interface
│   ├── MainActivity.kt       # Main screen & permissions
│   ├── SettingsActivity.kt   # Settings customization
│   └── ImpressumActivity.kt  # Legal info
│
├── di/                       # Dependency injection
│   └── ServiceLocator.kt     # Manual DI (preparing for Hilt)
│
├── util/                     # Utilities
│   └── AppConstants.kt       # Centralized constants
│
├── BackHomeAccessibilityService.kt  # Accessibility service
├── BootReceiver.kt                  # Auto-start receiver
└── PermissionManager.kt             # Permission helpers
```

---

## Critical Business Logic

### Position Persistence
**Why:** Dot must appear in same relative location across devices and orientations.

**Implementation:**
1. Store position as **percentage** (not pixels)
2. Convert percentage → pixels on app start using current screen dimensions
3. Re-calculate on orientation change
4. Percentage stored in SharedPreferences, pixels used at runtime

**Code Location:** `SettingsRepository.updatePosition()`

### Keyboard Avoidance Flow
1. KeyboardDetector detects keyboard via WindowInsets
2. KeyboardManager calculates target position above keyboard
3. Position includes 1.5x dot diameter margin
4. Snapshot original position for restoration
5. Animate to new position
6. Restore when keyboard closes

**Critical:** Must work on Android 8.0+ (SDK 26), so fallback estimation used when WindowInsets unavailable.

### Gesture Recognition
**Multi-tap Logic:**
- Each tap resets 300ms timeout timer
- Click count increments per tap
- On timeout → action dispatched based on count
- Long press (500ms) → immediate action, no waiting

**Critical:** Must feel responsive while preventing accidental triggers.

### Orientation Changes
**Problem:** Screen rotation changes coordinates system.

**Solution:**
1. Detect rotation change via Display.getRotation()
2. Hide dot during rotation (prevents visual jump)
3. Transform position mathematically for new orientation
4. Poll every 16ms until dimensions change (rotation complete)
5. Show dot at correct position

**Math:** Position transformation handles all 4 rotations (0°, 90°, 180°, 270°)

---

## Accessibility Requirements

**Target:** WCAG 2.1 Level AA compliance for elderly users

### Design Constraints
- **Touch Targets:** Minimum 48dp (accessibility standard)
- **Text Size:** 16-28sp (large, readable)
- **Colors:** High contrast, customizable with user-friendly HSV color picker
- **Language:** Simplified German (target audience)
- **TalkBack:** Full screen reader support

### Simplified UI
- Large buttons with clear labels
- Minimal settings (avoid overwhelming users)
- User-friendly color picker using HSV (Hue, Saturation, Brightness) instead of technical RGB
- Large 120dp color preview with elevation for better visibility
- Direct navigation (no nested menus)
- Safe Home mode as default for maximum safety

---

## Common Development Tasks

### Adding a New Gesture Action
1. Add action to `Gesture` enum (`domain/model/Gesture.kt`)
2. Update `GestureDetector` to recognize gesture
3. Add case in `OverlayService.handleGesture()`
4. Call appropriate `BackHomeAccessibilityService` method
5. Update settings UI if user-configurable

### Modifying Tap Behavior
1. Update `TapBehavior` enum (`domain/model/TapBehavior.kt`)
2. Modify `OverlayService.handleGesture()` switch cases
3. Update SettingsActivity UI for new option
4. Update layout file (`activity_settings.xml`) to add/remove radio buttons
5. Update AppConstants if changing default behavior
6. Update localization strings (`res/values/strings.xml`, `res/values-de/strings.xml`)

### Adding Settings
1. Add property to `OverlaySettings` data class
2. Update `SettingsRepositoryImpl` save/load methods
3. Add SharedPreferences key to `AppConstants`
4. Add UI control to `SettingsActivity`
5. Collect settings change in `OverlayService` Flow

### Debugging Position Issues
**Check:**
- `OverlaySettings.positionPercent` (should be 0.0-1.0)
- `OverlaySettings.screenWidth/screenHeight` (current dimensions)
- `OverlaySettings.rotation` (0, 90, 180, 270)
- `KeyboardManager` logs for keyboard interference
- `OrientationHandler` logs for rotation transforms

---

## Testing Strategy

### Unit Testing
- Domain layer models (pure Kotlin, no Android)
- Repository logic (mock SharedPreferences)
- Position conversion math

### Integration Testing
- Service lifecycle
- Settings persistence
- Permission flows

### Manual Testing Checklist
- [ ] Tap behaviors work in all modes
- [ ] Position persists across app restarts
- [ ] Keyboard avoidance moves/restores correctly
- [ ] Rotation maintains relative position
- [ ] TalkBack announces controls properly
- [ ] Dark mode renders correctly

---

## Known Constraints & Gotchas

### Android API Level Differences
- **WindowInsets API** (keyboard height) only on Android R+ (API 30)
- **Fallback:** Estimate keyboard as 40% of screen height on older devices
- **Code:** `KeyboardDetector.estimateKeyboardHeight()`

### Accessibility Service Delays
- Home/Back/Recents actions need 100-150ms delays
- Without delays, actions may not register reliably
- **Code:** `BackHomeAccessibilityService` action methods

### Position Edge Cases
- Dot must stay within screen bounds after rotation
- Keyboard may push dot off-screen → needs clamping
- Orientation changes while keyboard open → complex state

### Service Lifecycle
- Must be foreground service (Android O+ requirement)
- Notification required for foreground service
- Auto-restart on boot via BootReceiver
- User can disable service → handle gracefully

---

## Localization

**Supported Languages:**
- English (default: `res/values/`)
- German (`res/values-de/`)

**Dark Mode:**
- Colors defined in `res/values-night/`
- Material3.DayNight theme

**Adding Translations:**
1. Add string to `res/values/strings.xml`
2. Add German translation to `res/values-de/strings.xml`
3. Update dark mode colors if needed (`res/values-night/colors.xml`)

---

## Documentation Files

- **README.md** - Comprehensive user and developer guide (380 lines)
- **PRIVACY_POLICY.md** - Privacy policy (German)
- **RELEASE_NOTES_DE.md** - German release notes
- **RELEASE_NOTES_EN.md** - English release notes
- **CLAUDE.md** - This file (AI assistant guide)

---

## Recent Changes (v2.1.0)

### Safe Home Mode Now Default
- **SAFE_HOME** is now the default tap behavior mode for enhanced safety
- STANDARD mode has been removed from the UI (kept in enum for backward compatibility)
- Users upgrading from STANDARD mode will be automatically migrated to SAFE_HOME
- Settings UI now shows SAFE_HOME first, followed by NAVI mode
- Updated default in `AppConstants.DEFAULT_TAP_BEHAVIOR`

### Improved Color Picker
- Replaced technical RGB sliders with intuitive HSV (Hue, Saturation, Brightness) controls
- Larger color preview (120dp height with CardView elevation)
- Better labels: "Color Type", "Color Intensity", "Brightness"
- Simplified interface more suitable for elderly users
- Each slider clearly shows what it controls with 48dp minimum touch target
- Real-time color preview updates as sliders are adjusted

### Technical Changes
- Modified `SettingsActivity.showColorPickerDialog()` to use `Color.HSVToColor()` and `Color.colorToHSV()`
- Updated `color_picker_dialog.xml` layout with new SeekBar IDs
- Removed `tapBehaviorStandard` references from SettingsActivity
- Updated migration logic to convert STANDARD to SAFE_HOME on load

### Benefits for Users
- **Safety First:** Default mode ensures users always return home, reducing confusion
- **Easier Customization:** Color picker is now more intuitive and visual
- **Reduced Complexity:** Fewer mode options to choose from
- **Better Accessibility:** Larger touch targets and clearer labeling

---

## Git Workflow

**Current Branch:** `claude/safe-home-mode-color-picker-011CV2dBsyFQXT1CmbJzqetR`

### Branch Naming Convention
- Feature branches: `claude/<description>-<session-id>`
- Must start with `claude/` and end with session ID for push to work

### Commit Guidelines
- Clear, descriptive messages
- Prefix with type: `feat:`, `fix:`, `refactor:`, `docs:`
- Reference issue numbers if applicable

### Push Process
```bash
# Always use -u flag for new branches
git push -u origin claude/<branch-name>

# Retry on network failures (up to 4x with exponential backoff)
```

---

## Quick Reference

### Most Important Files
1. **OverlayService.kt** - Main orchestration (459 lines)
2. **KeyboardManager.kt** - Keyboard avoidance (273 lines)
3. **AppConstants.kt** - All configuration values
4. **SettingsRepository.kt** - Data contract interface
5. **OverlaySettings.kt** - Settings data model

### Key Configuration Values
- Overlay size: 48dp
- Click timeout: 300ms
- Long press: 500ms
- Recents timeout: 100ms
- Keyboard margin: 1.5x dot diameter

### Essential Commands
```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release (auto-increment version)
adb install -r app/build/...     # Install APK
adb logcat | grep "Assistive Tap"  # View logs
```

---

## Support & Resources

**Project Repository:** https://github.com/Stephan-Heuscher/Back_Home_Dot
**Issue Tracker:** GitHub Issues
**Developer:** Stephan Heuscher

For questions about Claude Code itself: https://docs.claude.com/en/docs/claude-code/
