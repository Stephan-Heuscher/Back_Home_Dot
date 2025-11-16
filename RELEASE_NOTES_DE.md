# Release Notes - AssistiPunkt

## Version 3.0.0 (2025-11-16)

### Interaktiver Tooltip & Touch-Verbesserungen

Wir freuen uns, **AssistiPunkt v3.0.0** vorzustellen, das einen **interaktiven Tooltip** einführt, um neuen Nutzern Gesten zu zeigen, und die Touch-Verarbeitung deutlich verbessert!

#### 🎓 Interaktiver Tooltip
- **Hilfe beim ersten Touch**: Tooltip erscheint bei erster Interaktion und zeigt verfügbare Gesten
- **Intelligente Positionierung**: Tooltip positioniert sich unter oder über dem Button um Überlappung zu vermeiden
- **Auto-Ausblenden**: Verschwindet nach 2,5 Sekunden um nicht zu stören
- **Modus-bewusst**: Zeigt Gesten passend zum aktuellen Modus (Safe-Home oder Navi)
- **Visuelle Anleitung**: Klare Anweisungen für Tippen, langes Drücken und Ziehen
- **Nicht störend**: Separate Fensterebene die niemals Button-Interaktion blockiert

#### 👆 Touch & Gesten-Verbesserungen
- **Touch-Durchleitung korrigiert**: Touches werden nun korrekt an Apps unter dem Button weitergegeben
- **Kein Flackern**: Button flackert nicht mehr während der Interaktion
- **Bessere Z-Ordnung**: Button bleibt immer visuell über dem Tooltip
- **Verbesserte Reaktionsfähigkeit**: Touch-Events werden zuverlässiger erkannt
- **Flüssige Animationen**: Alle Übergänge sind sanft und reaktionsschnell

#### 📍 Positions- & Layout-Verbesserungen
- **Begrenzungs-Sicherheit**: Button bleibt im sichtbaren Bildschirmbereich
- **Navigationsleisten-Bewusstsein**: Hält sicheren Abstand zur System-Navigationsleiste
- **Fenster-Größenoptimierung**: Optimierte Fenstergröße reduziert Speicherverbrauch
- **Rand-Zugriff**: Button kann alle Bildschirmränder erreichen ohne zu springen
- **Rotations-Stabilität**: Position bleibt stabil während Bildschirm-Rotation

#### ✨ Benutzererfahrung
- **Vereinfachte Anweisungen**: Hauptbildschirm-Text ist klarer und prägnanter
- **Sofortiges Feedback**: Tooltip erscheint sofort beim ersten Touch
- **Weniger Verwirrung**: Neue Nutzer verstehen Gesten sofort
- **Besseres Onboarding**: Keine Notwendigkeit alle Gesten im Voraus zu merken

#### 🔧 Technische Änderungen
- Separates Fenster für Tooltip mit `FLAG_NOT_TOUCHABLE` implementiert
- `bringToFront()`-Mechanismus entfernt der Touch-Unterbrechungen verursachte
- Race Conditions bei Drag-End-Positionierung behoben
- Verbesserte ViewGroup Z-Order-Verwaltung
- Optimierte Window-Layout-Parameter für bessere Touch-Verarbeitung
- Umfassendes Tooltip-Lifecycle-Management hinzugefügt

#### 🎯 Vorteile für Nutzer
- **Einfacheres Lernen**: Neue Nutzer entdecken Funktionen durch Tooltip
- **Besserer Touch**: Zuverlässigere Button-Interaktion
- **Sauberere Oberfläche**: Tooltip erscheint nur bei Bedarf
- **Keine Verwirrung**: Klare visuelle Anleitung für alle Gesten

### Fehlerbehebungen
- Touch-Events wurden durch Overlay-Fenster blockiert - behoben
- Button-Flackern bei schnellen Tipps - behoben
- Tooltip überlappte Button in manchen Bildschirmpositionen - behoben
- Z-Order-Probleme die visuelle Schichtungs-Probleme verursachten - behoben
- Randfälle bei Fenster-Größenberechnung die Touch-Verlust verursachten - behoben

---

## Version 2.1.0 (2025-11-11)

### Safe-Home-Modus als Standard & Verbesserter Farbwähler

Wir freuen uns, **AssistiPunkt v2.1.0** vorzustellen, das die Benutzerfreundlichkeit mit **Safe-Home als Standard-Modus** und einem **intuitiven Farbwähler** weiter verbessert!

#### 🏠 Safe-Home-Modus Jetzt Standard
- **Sicherheit zuerst**: Safe-Home ist nun der Standard-Modus für alle Nutzer
- **Standard-Modus entfernt**: Der alte Standard-Modus wurde aus der UI entfernt (bleibt im Code für Abwärtskompatibilität)
- **Automatische Migration**: Nutzer mit dem alten Standard-Modus werden automatisch zu Safe-Home migriert
- **Vereinfachte Auswahl**: Nur noch zwei Modi zur Auswahl: Safe-Home (Standard) und Navi

#### 🎨 Verbesserter Farbwähler
- **HSV statt RGB**: Intuitive Farbwahl mit Farbton, Intensität und Helligkeit
- **Größere Vorschau**: 120dp hohe Farbvorschau mit Elevation für bessere Sichtbarkeit
- **Bessere Beschriftungen**:
  - "Farbton" statt "Rot, Grün, Blau"
  - "Farbintensität" für Sättigung
  - "Helligkeit" für Wert
- **Seniorenfreundlich**: Keine technischen RGB-Werte mehr
- **Echtzeit-Vorschau**: Farbe wird während der Anpassung live aktualisiert
- **Barrierefreie Touch-Targets**: Alle Schieberegler mit 48dp Mindesthöhe

#### 🔧 Technische Änderungen
- `AppConstants.DEFAULT_TAP_BEHAVIOR` auf "SAFE_HOME" geändert
- `SettingsActivity` nutzt jetzt `Color.HSVToColor()` und `Color.colorToHSV()`
- `color_picker_dialog.xml` Layout komplett überarbeitet
- Radio-Button für Standard-Modus aus `activity_settings.xml` entfernt
- Migration-Logik in SettingsActivity hinzugefügt

#### 🎯 Vorteile für Nutzer
- **Mehr Sicherheit**: Standard-Modus stellt sicher, dass Nutzer immer nach Hause kommen
- **Einfachere Anpassung**: Farbwähler ist intuitiver und visueller
- **Weniger Verwirrung**: Weniger Modi zur Auswahl
- **Bessere Barrierefreiheit**: Größere Touch-Targets und klarere Beschriftung

---

## Version 2.0.0 (2025-11-05)

### Großes Refactoring - Clean Architecture & Safe-Home-Modus

Wir freuen uns, **AssistiPunkt v2.0.0** vorzustellen, eine vollständige Architektur-Überarbeitung mit dem neuen **Safe-Home-Modus** die Codequalität, Wartbarkeit und Benutzererfahrung signifikant verbessert!

#### 🏠 Safe-Home-Modus (Neu!)
- **Immer nach Hause**: Alle Taps führen zur Startseite - maximale Sicherheit für Nutzer, die Einfachheit benötigen
- **Viereck-Design**: Button wird zum abgerundeten Viereck (8dp Radius) - wie Android-Navigationsbuttons
- **Geschütztes Verschieben**: Button nur nach 500ms langem Drücken verschiebbar um versehentliche Bewegung zu verhindern
- **Minimaler Drag-Threshold**: Kurze Drags lösen Home aus, lange Drags verschieben den Button
- **Modus-basiertes Design**: Kreis (Standard/Navi) vs. Viereck (Safe-Home)

#### 🏗️ Architektur-Verbesserungen
- **Komponenten-Extraktion**: Spezialisierte Komponenten aus monolithischem OverlayService extrahiert
  - **KeyboardManager** (273 Zeilen): Vollständiges Tastatur-Vermeidungs-Management mit Debouncing
  - **PositionAnimator** (86 Zeilen): Sanfte Positions-Animationen mit ValueAnimator
  - **OrientationHandler** (97 Zeilen): Mathematische Rotations-Transformationen für alle Ausrichtungen
- **Code-Reduktion**: OverlayService von 670 auf ~459 Zeilen reduziert (31% Reduktion)
- **Clean Architecture**: Strikte Trennung zwischen Domain, Data und Presentation Layern
- **Testbarkeit**: Alle Komponenten sind nun unabhängig testbar
- **Dependency Injection**: ServiceLocator-Pattern bereit für Hilt-Migration

#### 🔄 Rotations-Handling - Kein Springen
- **Versteckt während Rotation**: Punkt wird während Bildschirm-Rotation versteckt um sichtbares Springen zu eliminieren
- **Intelligente Erkennung**: Dynamisches 16ms-Polling erkennt Dimensionsänderungen sofort
- **Perfekte Positionierung**: Punkt erscheint an mathematisch korrekter Position nach Rotation
- **Mathematische Transformation**: Präzise Mittelpunkt-Transformation für alle Rotationswinkel (0°, 90°, 180°, 270°)

#### ⌨️ Tastatur-Vermeidungs-Verbesserungen
- **Vollständig extrahiert**: Komplettes Tastatur-Management in dedizierter KeyboardManager-Klasse
- **Intelligenter Abstand**: 1.5x Punkt-Durchmesser Abstand zur Tastatur
- **Debouncing**: Verhindert Positions-Flackern während Tastatur-Übergängen
- **Snapshot/Restore**: Positions-Speicher für Tastatur-Erscheinen/Verschwinden-Zyklen

#### 🎨 Benutzererfahrung
- **Kein Springen**: Punkt springt nicht mehr während Rotation
- **Schnellere Reaktion**: Intelligente Erkennung bietet optimales Timing (~16ms auf den meisten Geräten)
- **Nahtlose Übergänge**: Alle Animationen und Positions-Updates sind flüssig

#### 🔧 Technische Details
- **Repository-Pattern**: BackHomeAccessibilityService zu Repository-Pattern migriert
- **Reaktive Daten**: Kotlin Flows für Echtzeit-Einstellungs-Updates
- **Komponenten-Komposition**: Composition over Inheritance Pattern durchgehend
- **ServiceLocator**: Factory-Methoden für alle spezialisierten Komponenten

### Breaking Changes
- Keine - alle Änderungen sind interne Architektur-Verbesserungen

### Fehlerbehebungen
- Punkt-Springen während Bildschirm-Rotation behoben
- Verbesserte Tastatur-Vermeidungs-Zuverlässigkeit
- Bessere Positions-Berechnungs-Genauigkeit

---

## Version 1.1.1 (2025-11-03)

### Neue Funktionen

Wir freuen uns, **AssistiPunkt v1.1.1** mit bedeutenden Verbesserungen der Tipp-Verhaltens-Anpassung und einem optimierten Benutzererlebnis vorzustellen!

#### 🎯 Tipp-Verhaltens-Modi
- **Zwei Verhaltensoptionen**: Wählen Sie zwischen STANDARD und ZURÜCK Tipp-Verhaltens-Modi
- **STANDARD-Modus** (empfohlen): 1x tippen = Home, 2x tippen = Zurück (intuitive Android-Navigation)
- **ZURÜCK-Modus**: 1x tippen = Zurück, 2x tippen = Zu vorheriger App wechseln (traditionelles Verhalten)
- **Immer verfügbar**: 3x tippen = Alle offenen Apps, 4x tippen = App öffnen, langes Drücken = Startseite

#### 🎨 Dynamische Benutzeroberfläche
- **Kontextabhängige Anweisungen**: Hauptbildschirm-Anweisungen aktualisieren sich automatisch basierend auf gewähltem Tipp-Verhalten
- **Einstellungs-Optimierung**: Tipp-Verhaltens-Auswahl an oberste Stelle der Einstellungen verschoben für bessere Auffindbarkeit

#### 🔧 Technische Verbesserungen
- **Automatische Versionierung**: Release-Builds erhöhen nun automatisch Versionsnummern
- **Build-Prozess-Verbesserung**: Versionscode und Patch-Version werden automatisch bei jedem Release erhöht
- **Verbesserte Build-Scripts**: Gradle Build-Prozess mit dynamischer Versionierung optimiert

### Änderungen

#### ✅ Erweiterte Funktionen
- **Tipp-Verhaltens-Auswahl**: Radio-Buttons in Einstellungen für einfache Modus-Umschaltung
- **Echtzeit-Anweisungen**: Hauptbildschirm-Text aktualisiert sich sofort bei Verhaltensänderung
- **Einstellungs-Neustrukturierung**: Wichtigste Einstellung (Tipp-Verhalten) an oberste Stelle verschoben

#### ✅ Technische Aktualisierungen
- **Versions-Eigenschaften**: Neue `version.properties` Datei für automatische Versionsverwaltung
- **Build-Automatisierung**: Release-Builds erhöhen automatisch Versionsnummern
- **Dokumentations-Aktualisierungen**: README und technische Dokumentation aktualisiert

---

## Version 1.1.0 (2025-10-30)

### Neue Funktionen

Wir freuen uns, **AssistiPunkt v1.1.0** mit Tastatur-Vermeidung vorzustellen!

#### ⌨️ Tastatur-Vermeidung
- **Automatische Positionierung**: Schwebender Punkt bewegt sich automatisch nach oben, wenn Tastatur erscheint
- **Intelligente Erkennung**: Erkennt Tastatur-Sichtbarkeit und passt Position entsprechend an
- **Nahtloses Erlebnis**: Kein manuelles Neupositionieren beim Tippen nötig

#### 🎨 Verbesserungen der Benutzerfreundlichkeit
- **Verbesserte Touch-Verarbeitung**: Bessere Gesten-Erkennung
- **Erweiterte Barrierefreiheit**: Aktualisierte Texte und Beschreibungen

---

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
