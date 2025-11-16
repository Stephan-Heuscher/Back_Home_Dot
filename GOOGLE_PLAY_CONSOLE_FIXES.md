# Google Play Console Fixes

## Summary of Changes

This document explains how to fix the errors you're seeing in Google Play Console for Assistive Tap.

---

## 1. Advertising ID Declaration Fix ✅

### What was fixed in the code:
- Added explicit declaration in `AndroidManifest.xml` that the app does NOT use advertising ID
- Line added: `<uses-permission android:name="com.google.android.gms.permission.AD_ID" tools:node="remove" />`

### What you need to do in Play Console:

1. Go to **Play Console** → **Policy** → **App content**
2. Find **Advertising ID** section
3. Click **Start** or **Manage**
4. Select: **"No, my app does not collect or use advertising ID"**
5. Save and submit

**Why?** Your app doesn't use any advertising libraries (no Google Ads, no analytics with advertising). The manifest now explicitly declares this.

---

## 2. Foreground Service Declaration Fix 📋

### What's already correct in the code:
- `AndroidManifest.xml` correctly declares foreground service type as `specialUse`
- Service includes the required property explaining its purpose: "Provides overlay button for navigation"

### What you need to do in Play Console:

1. Go to **Play Console** → **Policy** → **App content**
2. Find **Foreground Service** section
3. Click **Start** or **Manage**
4. Select the foreground service type your app uses:
   - ✅ **Special Use** (already declared in manifest)
5. Provide justification for special use:
   ```
   This app provides an accessibility overlay button that helps users with
   motor impairments navigate their device. The foreground service is required
   to maintain the floating button on screen while the user interacts with
   other apps. This is essential for the app's core accessibility functionality.
   ```
6. Save and submit

**Why?** Your `OverlayService` runs as a foreground service with type `specialUse` because it provides accessibility features that don't fit standard foreground service categories.

---

## 3. After Making These Changes

### In Google Play Console:
1. Update **Advertising ID** declaration → Set to "No"
2. Update **Foreground Service** declaration → Set to "Special Use"
3. Save all changes

### Build and Upload New Version:
```bash
# Build the release APK with the updated manifest
./gradlew assembleRelease

# Or build App Bundle (recommended for Play Store)
./gradlew bundleRelease
```

### Upload to Play Console:
1. Go to **Production** → **Create new release**
2. Upload the new APK/AAB from:
   - APK: `app/build/outputs/apk/release/app-release.apk`
   - AAB: `app/build/outputs/bundle/release/app-release.aab`
3. Add release notes from `PLAYSTORE_RELEASE_NOTES_EN.txt` and `PLAYSTORE_RELEASE_NOTES_DE.txt`
4. Review and roll out

---

## 4. Verification Checklist

After uploading the new build and updating declarations:

- [ ] Advertising ID error should disappear (may take a few hours)
- [ ] Foreground Service error should disappear
- [ ] App builds successfully with no manifest merger errors
- [ ] Release notes are added from prepared files
- [ ] Screenshots and store listing are up to date

---

## Technical Details

### Manifest Changes:
```xml
<!-- This tells Android and Play Store that app does NOT use advertising ID -->
<uses-permission android:name="com.google.android.gms.permission.AD_ID" tools:node="remove" />
```

### Foreground Service Type (Already Present):
```xml
<service
    android:name=".service.overlay.OverlayService"
    android:foregroundServiceType="specialUse">
    <property
        android:name="android.app.PROPERTY_SPECIAL_USE_FGS_SUBTYPE"
        android:value="Provides overlay button for navigation" />
</service>
```

---

## Need Help?

If errors persist after making these changes:
1. Wait 2-4 hours for Play Console to process the changes
2. Check that you uploaded the new APK/AAB with updated manifest
3. Verify both Play Console declarations are saved correctly
4. Contact Play Console support if issues continue

---

## Additional Resources

- [Advertising ID Best Practices](https://support.google.com/googleplay/android-developer/answer/6048248)
- [Foreground Services Guide](https://developer.android.com/develop/background-work/services/foreground-services)
- [Special Use Foreground Services](https://developer.android.com/develop/background-work/services/fg-service-types#special-use)
