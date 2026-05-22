# Parkeer ProGuard Rules — Aggressive Hardening

# ─── Suppress warnings for desktop-only classes ───
-dontwarn java.awt.**
-dontwarn com.sun.jna.**

# ─── R8 Aggressive Obfuscation ───
-allowaccessmodification
-repackageclasses ''
-overloadaggressively

# Remove all logging
-assumenosideeffects class android.util.Log {
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
}

# ─── Keep Rules ───

# Hilt
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager { *; }
-keep,allowobfuscation,allowshrinking class * extends dagger.internal.Factory

# NFC system classes
-keep class android.nfc.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }
-keepattributes *Annotation*

# Compose
-keep class androidx.compose.** { *; }

# Dexterity
-keep class com.telkomsel.dexterity.** { *; }
-keep class com.telkomsel.analytics.** { *; }

# Model classes (serialized to NFC)
-keep class com.eldirohmanur.parkeer.core.model.** { *; }

# Card protocol (binary serialization)
-keep class com.eldirohmanur.parkeer.core.cardprotocol.** { *; }

# Native JNI bridge
-keep class com.eldirohmanur.parkeer.core.crypto.NativeCipher { *; }
