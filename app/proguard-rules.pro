# Parkeer ProGuard Rules

# Keep crypto classes — reflection/JCE usage breaks if renamed
-keep class com.kdx.parkeer.core.crypto.** { *; }

# Keep card protocol — binary serialization relies on class structure
-keep class com.kdx.parkeer.core.cardprotocol.** { *; }

# Keep model classes — used in serialization and enum lookup by ordinal/code
-keep class com.kdx.parkeer.core.model.** { *; }

# Keep NFC reader — instantiated via Hilt with reflection
-keep class com.kdx.parkeer.core.nfc.NtagCardReader { *; }
