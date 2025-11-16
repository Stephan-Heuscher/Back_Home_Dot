# Privacy Policy for Assistive Tap (AssistiPunkt)

**Last Updated: November 16, 2025**
**Version: 3.0.0**

## Introduction

Assistive Tap ("we," "our," or "the app") is committed to protecting your privacy. This Privacy Policy explains how we handle information when you use our Android application.

## Developer Information

- **App Name**: Assistive Tap (AssistiPunkt)
- **Developer**: Stephan Heuscher
- **Contact Email**: stv.heuscher@gmail.com
- **GitHub**: https://github.com/Stephan-Heuscher/Back_Home_Dot

## Data Collection and Usage

### Information We DO NOT Collect

Assistive Tap itself **does not collect, store, or transmit** any personal information or user data. The app:

- Does not collect your name, email, or contact information
- Does not track your location
- Does not record your screen or app usage
- Does not access your contacts, photos, or other personal files
- Does not monitor which apps you use or how you use them
- **Does not use advertising ID** - We explicitly exclude advertising identifiers
- Does not contain any advertising or analytics libraries
- Does not share any data with third parties

### Information Stored Locally

The app stores the following settings **only on your device**:

- Position of the floating navigation dot (as screen percentage)
- Selected color preferences (RGB values)
- Opacity/transparency settings (0-255 alpha value)
- Activation status (on/off)
- Tap behavior mode (Safe Home or Navi)
- Keyboard avoidance preference (on/off)
- Recent apps switch timeout (milliseconds)
- Screen dimensions and rotation state

This data:
- Never leaves your device
- Is not transmitted to any server
- Is only accessible by the app itself
- Is stored in Android's secure SharedPreferences
- Is automatically deleted when you uninstall the app
- Cannot be accessed by other apps

## Permissions Explained

### 1. Display Over Other Apps (SYSTEM_ALERT_WINDOW)

**Purpose**: Allows the app to display the floating navigation dot on top of other apps.

**What We Access**: Only the ability to draw a small circular button on your screen.

**What We DON'T Do**: We do not read, record, or access content from other apps. We only display our navigation dot.

### 2. Accessibility Service

**Purpose**: Enables navigation actions (back, home, recent apps, app switching).

**What We Access**: Minimal system functions needed for navigation gestures only.

**What We DON'T Do**: 
- We do NOT read or record screen content
- We do NOT log your activities
- We do NOT access text from other apps
- We do NOT monitor which apps you use
- We do NOT transmit any accessibility data

**Important**: The accessibility service is used exclusively for executing navigation commands when you tap the floating dot. It has no ability to collect or transmit data.

### 3. Run at Startup (RECEIVE_BOOT_COMPLETED)

**Purpose**: Allows the app to start automatically after device restart (if you had it enabled).

**What We Access**: Notification when the device boots up.

**What We DON'T Do**: We do not collect any boot-related data or device information.

### 4. Foreground Service

**Purpose**: Keeps the floating navigation dot visible and responsive while you use other apps.

**What We Access**: Permission to run a persistent service with a notification.

**What We DON'T Do**: The service only manages the overlay button and does not collect, monitor, or transmit any data.

**Note**: A persistent notification is required by Android when the app is active. This is a system requirement, not a tracking mechanism.

### 5. Post Notifications (Android 13+)

**Purpose**: Display the required foreground service notification.

**What We Access**: Ability to show a notification that the app is running.

**What We DON'T Do**: We do not send promotional notifications, reminders, or any other messages. The notification only indicates the app is active.

## Children's Privacy

Assistive Tap does not knowingly collect any personal information from children. The app is designed to be safe for users of all ages.

## Data Security

- All app settings are stored locally using Android's secure SharedPreferences
- No data is transmitted from the app to external servers
- The accessibility service operates in a sandboxed environment with minimal permissions
- No advertising ID is generated or used by this app
- No analytics or tracking SDKs are included in the app
- The app does not require internet permission and cannot transmit data

## Your Rights and Choices

You have the right to:
- Disable the app at any time
- Revoke permissions in Android Settings → Apps → Assistive Tap → Permissions
- Disable the accessibility service in Settings → Accessibility
- Uninstall the app (which automatically deletes all local data)

## Changes to This Privacy Policy

We may update this Privacy Policy from time to time. Changes will be posted on this page with an updated "Last Updated" date. Continued use of the app after changes constitutes acceptance of the updated policy.

## Data Retention

- **App Settings**: Stored locally until you uninstall the app

## Legal Basis for Processing (GDPR)

If you are in the European Economic Area (EEA):
- **App Settings**: Processed based on your consent and our legitimate interest in providing app functionality

## Your GDPR Rights

If you are in the EEA, you have rights under the General Data Protection Regulation (GDPR):
- Right to access your data
- Right to rectification
- Right to erasure ("right to be forgotten")
- Right to restrict processing
- Right to data portability
- Right to object to processing
- Right to withdraw consent

For app-related questions, contact us using the information below.

## California Privacy Rights

If you are a California resident, you have rights under the California Consumer Privacy Act (CCPA). Since we do not collect or sell personal information, there is minimal data to request or delete.

## Contact Us

If you have questions, concerns, or requests regarding this Privacy Policy:

- **Email**: s.heuscher@gmail.com
- **GitHub Issues**: https://github.com/Stephan-Heuscher/Back_Home_Dot/issues

## Third-Party Services

**This app does not use any third-party services, SDKs, or libraries that collect data.**

The app uses only standard Android framework libraries that are part of the Android operating system:
- AndroidX libraries (UI components - no data collection)
- Material Design components (UI styling - no data collection)
- Android Accessibility Services (system API - no data transmission)

No third-party analytics, advertising, or tracking services are integrated.

## Transparency Commitment

We believe in transparency. This app is open source, and you can review the code at:
https://github.com/Stephan-Heuscher/Back_Home_Dot

---

## Play Store Data Safety Declaration

For Google Play Store compliance, we declare:

- **Advertising ID**: NOT USED - App explicitly excludes advertising identifiers
- **Location Data**: NOT COLLECTED
- **Personal Information**: NOT COLLECTED
- **Financial Information**: NOT COLLECTED
- **App Activity**: NOT COLLECTED
- **Device or Other IDs**: NOT COLLECTED
- **Data Sharing**: NO DATA SHARED with third parties
- **Data Encryption**: Settings stored in Android's encrypted SharedPreferences
- **Data Deletion**: All data deleted upon app uninstall

---

**Summary**: Assistive Tap collects **ZERO** personal data. No advertising ID, no tracking, no analytics, no data transmission. Settings are stored only on your device using Android's secure storage. You have full control over permissions and can disable features or uninstall at any time. This app is completely privacy-focused and designed for accessibility, not data collection.
