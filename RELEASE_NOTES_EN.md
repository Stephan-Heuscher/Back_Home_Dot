# Release Notes - Assistive Tap

## Version v0.2.1-beta (2025-10-27)

### Beta Release

We are excited to announce the beta release of **Assistive Tap** (German: **AssistiPunkt**) - your accessible Android app for intuitive navigation!

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
