# Release Notes - Assistive Tap

## Version 3.0.0 (2025-11-16)

### Interactive Tooltip & Touch Improvements

We are excited to announce **Assistive Tap v3.0.0**, which introduces an **interactive tooltip** to help new users discover gestures and significantly improves touch handling!

#### 🎓 Interactive Tooltip
- **First-Touch Help**: Tooltip appears on first interaction showing available gestures
- **Smart Positioning**: Tooltip positions itself below or above button to avoid overlap
- **Auto-Hide**: Disappears after 2.5 seconds to avoid clutter
- **Mode-Aware**: Shows gestures appropriate for current mode (Safe-Home or Navi)
- **Visual Guidance**: Clear instructions for tap, long-press, and drag gestures
- **Non-Intrusive**: Separate window layer that never blocks button interaction

#### 👆 Touch & Gesture Improvements
- **Fixed Touch Pass-Through**: Touches now properly pass through to apps below the button
- **Eliminated Flicker**: Button no longer flickers during interaction
- **Better Z-Order**: Button always stays visually above tooltip
- **Improved Responsiveness**: Touch events register more reliably
- **Smooth Animations**: All transitions are fluid and responsive

#### 📍 Position & Layout Enhancements
- **Boundary Safety**: Button stays within visible screen bounds
- **Navigation Bar Awareness**: Maintains safe distance from system navigation bar
- **Window Sizing**: Optimized window size reduces memory footprint
- **Edge Access**: Button can reach all screen edges without jumping
- **Rotation Stability**: Position remains stable during screen rotation

#### ✨ User Experience
- **Simplified Instructions**: Main screen text is clearer and more concise
- **Immediate Feedback**: Tooltip shows instantly on first touch
- **Reduced Confusion**: New users understand gestures right away
- **Better Onboarding**: No need to remember all gestures upfront

#### 🔧 Technical Changes
- Implemented separate window for tooltip with `FLAG_NOT_TOUCHABLE`
- Removed `bringToFront()` mechanism that caused touch interruptions
- Fixed race conditions in drag end positioning
- Improved ViewGroup Z-order management
- Optimized window layout parameters for better touch handling
- Added comprehensive tooltip lifecycle management

#### 🎯 Benefits for Users
- **Easier Learning**: New users discover features through tooltip
- **Better Touch**: More reliable button interaction
- **Cleaner Interface**: Tooltip only appears when needed
- **No Confusion**: Clear visual guidance for all gestures

### Bug Fixes
- Fixed touch events being blocked by overlay window
- Fixed button flicker during rapid taps
- Fixed tooltip overlapping button in some screen positions
- Fixed Z-order issues causing visual layering problems
- Fixed edge cases in window sizing causing touch loss

---

## Version 2.1.0 (2025-11-11)

### Safe-Home Mode as Default & Improved Color Picker

We are excited to announce **Assistive Tap v2.1.0**, which further enhances user experience with **Safe-Home as the default mode** and an **intuitive color picker**!

#### 🏠 Safe-Home Mode Now Default
- **Safety First**: Safe-Home is now the default mode for all users
- **Standard Mode Removed**: The old Standard mode has been removed from the UI (kept in code for backward compatibility)
- **Automatic Migration**: Users with the old Standard mode will be automatically migrated to Safe-Home
- **Simplified Selection**: Only two modes to choose from: Safe-Home (default) and Navi

#### 🎨 Improved Color Picker
- **HSV Instead of RGB**: Intuitive color selection with Hue, Saturation, and Brightness
- **Larger Preview**: 120dp tall color preview with elevation for better visibility
- **Better Labels**:
  - "Color Type" instead of "Red, Green, Blue"
  - "Color Intensity" for saturation
  - "Brightness" for value
- **Senior-Friendly**: No more technical RGB values
- **Real-Time Preview**: Color updates live as sliders are adjusted
- **Accessible Touch Targets**: All sliders with 48dp minimum height

#### 🔧 Technical Changes
- Changed `AppConstants.DEFAULT_TAP_BEHAVIOR` to "SAFE_HOME"
- `SettingsActivity` now uses `Color.HSVToColor()` and `Color.colorToHSV()`
- Completely redesigned `color_picker_dialog.xml` layout
- Removed Standard mode radio button from `activity_settings.xml`
- Added migration logic in SettingsActivity

#### 🎯 Benefits for Users
- **More Safety**: Default mode ensures users always return home
- **Easier Customization**: Color picker is more intuitive and visual
- **Less Confusion**: Fewer modes to choose from
- **Better Accessibility**: Larger touch targets and clearer labeling

---

## Version 2.0.0 (2025-11-05)

### Major Refactoring - Clean Architecture & Safe-Home Mode

We are excited to announce **Assistive Tap v2.0.0**, a complete architectural overhaul with the new **Safe-Home Mode** that significantly improves code quality, maintainability, and user experience!

#### 🏠 Safe-Home Mode (New!)
- **Always Home**: All taps lead to home screen - maximum safety for users who need simplicity
- **Square Design**: Button becomes a rounded square (8dp radius) - like Android navigation buttons
- **Protected Dragging**: Button can only be moved after 500ms long-press to prevent accidental movement
- **Minimal Drag Threshold**: Short drags trigger Home, substantial drags move the button
- **Mode-based Design**: Circle (Standard/Navi) vs. Square (Safe-Home)

#### 🏗️ Architecture Improvements
- **Component Extraction**: Extracted specialized components from monolithic OverlayService
  - **KeyboardManager** (273 lines): Complete keyboard avoidance management with debouncing
  - **PositionAnimator** (86 lines): Smooth position animations using ValueAnimator
  - **OrientationHandler** (97 lines): Mathematical rotation transformations for all orientations
- **Code Reduction**: OverlayService reduced from 670 to ~459 lines (31% reduction)
- **Clean Architecture**: Strict separation between Domain, Data, and Presentation layers
- **Testability**: All components are now independently testable
- **Dependency Injection**: ServiceLocator pattern ready for Hilt migration

#### 🔄 Rotation Handling - Zero Jump
- **Hide During Rotation**: Dot is now hidden during screen rotation to eliminate visible jumping
- **Smart Detection**: Dynamic 16ms polling detects dimension changes immediately
- **Perfect Positioning**: Dot reappears at mathematically correct position after rotation
- **Mathematical Transformation**: Accurate center-point transformation for all rotation angles (0°, 90°, 180°, 270°)

#### ⌨️ Keyboard Avoidance Improvements
- **Fully Extracted**: Complete keyboard management in dedicated KeyboardManager class
- **Smart Margin**: 1.5x dot diameter distance from keyboard
- **Debouncing**: Prevents position flickering during keyboard transitions
- **Snapshot/Restore**: Position memory for keyboard appearance/disappearance cycles

#### 🎨 User Experience
- **Eliminated Jumping**: Dot no longer jumps during rotation
- **Faster Response**: Intelligent detection provides optimal timing (~16ms on most devices)
- **Seamless Transitions**: All animations and position updates are smooth

#### 🔧 Technical Details
- **Repository Pattern**: Migrated BackHomeAccessibilityService to repository pattern
- **Reactive Data**: Kotlin Flows for real-time settings updates
- **Component Composition**: Composition over inheritance pattern throughout
- **ServiceLocator**: Factory methods for all specialized components

### Breaking Changes
- None - all changes are internal architecture improvements

### Bug Fixes
- Fixed dot jumping during screen rotation
- Improved keyboard avoidance reliability
- Better position calculation accuracy

---

## Version 1.1.1 (2025-11-03)

### New Features

We are excited to announce **Assistive Tap v1.1.1** with major enhancements to accessibility, navigation customization, and user experience!

#### 🎯 Tap Behavior Modes
- **Two Behavior Options**: Choose between STANDARD and BACK tap behavior modes
- **STANDARD Mode** (recommended): 1 tap = Home, 2 taps = Back (intuitive Android navigation)
- **BACK Mode**: 1 tap = Back, 2 taps = Switch to previous app (traditional behavior)
- **Always Available**: 3 taps = All open apps, 4 taps = Open app, long press = Home screen

#### ⌨️ Keyboard Avoidance
- **Automatic Positioning**: Floating dot automatically moves up when keyboard appears
- **Smart Detection**: Detects keyboard visibility and adjusts position accordingly
- **Seamless Experience**: No manual repositioning needed when typing

#### 🎨 Dynamic User Interface
- **Context-Aware Instructions**: Main screen instructions automatically update based on selected tap behavior
- **Settings Optimization**: Tap behavior selection moved to top of settings for better discoverability
- **Enhanced Accessibility**: Updated strings and descriptions for improved usability

#### 🔧 Technical Improvements
- **Automatic Versioning**: Release builds now automatically increment version numbers
- **Build Process Enhancement**: Version code and patch version increment automatically for each release
- **Improved Build Scripts**: Gradle build process optimized with dynamic versioning

### Changes

#### ✅ Enhanced Features
- **Tap Behavior Selection**: Radio buttons in settings for easy mode switching
- **Real-time Instructions**: Main screen text updates immediately when behavior changes
- **Settings Reorganization**: Most important setting (tap behavior) moved to top
- **Keyboard Avoidance**: Automatic dot repositioning when typing

#### ✅ Technical Updates
- **Version Properties**: New `version.properties` file for automatic version management
- **Build Automation**: Release builds automatically increment version numbers
- **Documentation Updates**: README and technical docs updated to reflect changes

---

## Version 1.0.0 (2025-10-27)

### Initial Release

We are excited to announce the first public release of **Assistive Tap** (German: **AssistiPunkt**) - your accessible Android app for intuitive navigation!

---

## Key Features

### Gesture-Based Navigation
- **Single Tap**: Navigate back
- **Double Tap**: Switch to last app
- **Triple Tap**: Show all open apps
- **Quadruple Tap**: Open Assistive Tap app
- **Long Press**: Go to home screen

### Customization Options
- **Color Selection**: Choose your preferred color for the floating dot
- **Transparency**: Adjust transparency level (0-100%)
- **Free Positioning**: Move the dot anywhere on screen
- **Switch Speed**: Configurable delay for app switching (50-300ms)

### Accessibility
- **WCAG 2.1 Level AA Compliant**: Built following international accessibility standards
- **TalkBack Support**: Full screen reader compatibility
- **Simple Language**: Clear, easy-to-understand interface text
- **Large Text**: Optimized text sizes (16-28sp) for better readability
- **Dark Mode**: Automatic adaptation to system theme
- **Touch Targets**: Minimum 48dp touch target sizes

### Design & User Experience
- **Design Gallery**: Color and design inspiration in settings
- **New App Icon**: Modern, recognizable icon
- **Material Design 3**: Contemporary, intuitive user interface
- **Smart Permission Display**: Clear guidance when permissions are missing

### Internationalization
- **Multilingual**: Full support for German and English
- **Auto-Detection**: App adapts to system language

### Support Development
- **Rewarded Ads**: Voluntary ads to support app development
- **No In-App Purchases**: All features available for free
- **Open Source**: Full source code available on GitHub

---

## Technical Details

### System Requirements
- **Android Version**: 8.0 (API Level 26) or higher
- **Permissions**:
  - Overlay permission (for floating dot)
  - Accessibility service (for navigation actions)
  - Usage access (for direct app switching)

### Architecture
- **Language**: Kotlin
- **Target SDK**: 36
- **UI Framework**: Material Design 3
- **Services**: OverlayService + AccessibilityService
- **Code Optimization**: ProGuard for release builds

---

## Fixed Issues

### Rotation & Positioning
- Dot stays at same physical position during screen rotation
- Correct rotation transformation (counter-clockwise)
- Dot no longer disappears at screen edges
- Center-point transformation fixes diameter shift

### Stability
- App exit reliably stops services without status change
- Quadruple tap reliably opens app
- Overlay visibility guaranteed
- More robust error handling

### User Interface
- Dark mode works correctly
- Switches toggle reliably
- Dialogs use consistent "Assistive Tap" naming
- Optimized button texts and descriptions

---

## Known Limitations

- **Overlay over System UI**: From Android 8.0, Google does not allow overlays over system settings for security reasons
- **Battery Optimization**: Aggressive battery optimization may terminate the service

---

## Installation

1. Download APK from [GitHub Releases](https://github.com/Stephan-Heuscher/Back_Home_Dot/releases)
2. Install APK on your device
3. Open app and follow instructions
4. Grant required permissions

---

## Feedback & Support

We appreciate your feedback!

- **GitHub Issues**: [Report a problem](https://github.com/Stephan-Heuscher/Back_Home_Dot/issues)
- **Feature Requests**: [Suggest an enhancement](https://github.com/Stephan-Heuscher/Back_Home_Dot/issues/new)
- **Discussions**: [Join the discussion](https://github.com/Stephan-Heuscher/Back_Home_Dot/discussions)

---

## Credits

- **Developed by**: Stephan Heuscher
- **With support from**: Claude (Anthropic)
- **Icons**: Material Design
- **Inspired by**: iOS AssistiveTouch

---

**Note**: This app is an assistive tool and does not replace professional advice or therapy for motor impairments.

Made with ❤️ for accessibility
