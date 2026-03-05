# Odyzen

## Project Overview
A native Android application built with Kotlin and Jetpack Compose that helps you reclaim focus through social accountability. Block distracting apps and require a friend's TOTP code to unlock — inspired by Odysseus tying himself to the mast.

## How to Run (Mac Silicon / M1 / M2)

### 1. Open in Android Studio
1.  Launch **Android Studio**.
2.  Select **Open**.
3.  Navigate to this directory: `/Users/shikher/Vibe coding/accountability/app`.
4.  Standard Android Studio synchronization should start. Wait for the `Gradle Sync` to finish.

### 2. Set up an Emulator (AVD)
Since you are on an M2 Mac, you need an **ARM64** system image.
1.  Go to **Device Manager** (icon in top right, or Tools -> Device Manager).
2.  Click **Create Device**.
3.  Choose a hardware profile (e.g., **Pixel 7**). Click Next.
4.  **System Image**:
    -   Select a Release (e.g., **Tiramisu** (API 33) or **UpsideDownCake** (API 34)).
    -   **Important**: Ensure the ABI column says **arm64-v8a**. It usually defaults to this on M2 Macs.
    -   If needed, click the "Other Images" tab to specifically find arm64 images.
5.  Finish creating the device.

### 3. Build and Run
1.  Make sure your new Emulator is selected in the toolbar dropdown.
2.  Click the green **Run** (Play) button.
3.  The emulator should launch, and the app will install and open.

### 4. CRITICAL: Enable Accessibility
The app *will not block anything* until you grant it permission.
1.  On the Emulator, go to **Settings** -> **Accessibility**.
2.  Find **Odyzen** in the list of downloaded services.
3.  Tap it and switch it to **On**.
4.  Allow the permission.
5.  Now, go back to the app and create your keys/blocks.

## Troubleshooting
-   **Gradle Errors**: If you see Java version errors, go to *Settings -> Build, Execution, Deployment -> Build Tools -> Gradle* and ensure "Gradle JDK" is set to Java 17 or newer (usually "jbr-17" or similar embedded JDK).

---

## v2 Roadmap

| Feature | Description |
|---|---|
| 🔒 **Block Uninstalls** | Prevent the user from uninstalling Odyzen without a TOTP code, closing the most obvious bypass loophole. |
| 📊 **Usage Dashboard** | Visual daily/weekly usage charts per blocked app — see trends, streaks, and how your screen time changes over time. |
| 📬 **Accountability Pings** | Automatically notify your accountability partner (via email or push) about your usage patterns, keeping the social pressure loop active even passively. |
| 🔑 **Single/Multi-Use Keys** | Mark TOTP keys as single-use (burn after one unlock) or multi-use, giving accountability partners finer-grained control over access. |
| 🚫 **Hard Block** | Force-close (or redirect away from) the blocked app entirely instead of just overlaying the TOTP screen. Prevents users from pressing Back to sneak past the lock and continue using the app underneath. |
