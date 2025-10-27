# Release Notes - AssistiPunkt

## Version 1.0.0 (2025-10-27)

### Erstveröffentlichung

Wir freuen uns, die erste öffentliche Version von **AssistiPunkt** (international: **Assistive Tap**) vorzustellen - Ihre barrierefreie Android-App für intuitive Navigation!

---

## Hauptfunktionen

### Navigation per Gesten
- **Einfachklick**: Zurück-Navigation
- **Doppelklick**: Zur letzten App wechseln
- **Dreifachklick**: Alle offenen Apps anzeigen
- **Vierfachklick**: AssistiPunkt-App öffnen
- **Langes Drücken**: Zur Startseite

### Anpassungsmöglichkeiten
- **Farbauswahl**: Wählen Sie Ihre bevorzugte Farbe für den Punkt
- **Durchsichtigkeit**: Stellen Sie die Transparenz ein (0-100%)
- **Freie Positionierung**: Verschieben Sie den Punkt an beliebige Stellen
- **Wechsel-Geschwindigkeit**: Konfigurierbare Verzögerung beim App-Wechsel (50-300ms)

### Barrierefreiheit
- **WCAG 2.1 Level AA konform**: Entwickelt nach internationalen Barrierefreiheits-Standards
- **TalkBack-Unterstützung**: Vollständige Screen-Reader-Kompatibilität
- **Einfache Sprache**: Texte in A1-Level Deutsch für maximale Verständlichkeit
- **Große Schrift**: Optimierte Textgrößen (16-28sp) für bessere Lesbarkeit
- **Dark Mode**: Automatische Anpassung an System-Theme
- **Touch-Targets**: Mindestens 48dp große Berührungsflächen

### Design & Benutzerfreundlichkeit
- **Design-Galerie**: Inspirationen für Farben und Designs in den Einstellungen
- **Neues App-Icon**: Modernes, wiedererkennbares Icon
- **Material Design 3**: Zeitgemäße, intuitive Benutzeroberfläche
- **Intelligente Berechtigungsanzeige**: Klare Anleitung bei fehlenden Berechtigungen

### Internationalisierung
- **Mehrsprachig**: Vollständige Unterstützung für Deutsch und Englisch
- **Automatische Spracherkennung**: App passt sich an System-Sprache an

### Unterstützung der Entwicklung
- **Rewarded Ads**: Freiwillige Werbung zur Unterstützung der App-Entwicklung
- **Keine In-App-Käufe**: Alle Funktionen sind kostenlos verfügbar
- **Open Source**: Vollständiger Quellcode auf GitHub verfügbar

---

## Technische Details

### Systemanforderungen
- **Android-Version**: 8.0 (API Level 26) oder höher
- **Berechtigungen**:
  - Overlay-Berechtigung (für schwebenden Punkt)
  - Bedienungshilfe-Zugriff (für Navigationsaktionen)
  - Nutzungszugriff (für direkten App-Wechsel)

### Architektur
- **Sprache**: Kotlin
- **Target SDK**: 36
- **UI-Framework**: Material Design 3
- **Services**: OverlayService + AccessibilityService
- **Code-Optimierung**: ProGuard für Release-Builds

---

## Behobene Probleme

### Rotation & Positionierung
- Punkt bleibt beim Bildschirm-Drehen an gleicher physischer Position
- Korrekte Rotations-Transformation (Gegen den Uhrzeigersinn)
- Punkt verschwindet nicht mehr am Bildschirmrand
- Center-Point Transformation behebt Durchmesser-Verschiebung

### Stabilität
- App-Beenden stoppt Services zuverlässig ohne Status-Änderung
- 4-fach-Klick öffnet App zuverlässig
- Overlay-Sichtbarkeit garantiert
- Robustere Fehlerbehandlung

### Benutzeroberfläche
- Dark Mode funktioniert korrekt
- Switches schalten zuverlässig
- Dialoge verwenden konsistente Bezeichnung "Assistive Tap"
- Optimierte Button-Texte und Beschreibungen

---

## Bekannte Einschränkungen

- **Overlay über System-UI**: Ab Android 8.0 erlaubt Google aus Sicherheitsgründen keine Overlays über System-Einstellungen
- **Akku-Optimierung**: Bei aggressiver Akku-Optimierung kann der Service beendet werden

---

## Installation

1. APK von [GitHub Releases](https://github.com/Stephan-Heuscher/Back_Home_Dot/releases) herunterladen
2. APK auf dem Gerät installieren
3. App öffnen und Anweisungen folgen
4. Erforderliche Berechtigungen erteilen

---

## Feedback & Unterstützung

Wir freuen uns über Ihr Feedback!

- **GitHub Issues**: [Problem melden](https://github.com/Stephan-Heuscher/Back_Home_Dot/issues)
- **Feature-Wünsche**: [Enhancement vorschlagen](https://github.com/Stephan-Heuscher/Back_Home_Dot/issues/new)
- **Diskussionen**: [An Diskussion teilnehmen](https://github.com/Stephan-Heuscher/Back_Home_Dot/discussions)

---

## Credits

- **Entwickelt von**: Stephan Heuscher
- **Mit Unterstützung von**: Claude (Anthropic)
- **Icons**: Material Design
- **Inspiriert von**: iOS AssistiveTouch

---

**Hinweis**: Diese App ist ein Hilfsmittel und ersetzt keine professionelle Beratung oder Therapie bei motorischen Einschränkungen.

Made with ❤️ for accessibility
