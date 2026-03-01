# Add project specific ProGuard rules here.

# Room entities — keep all fields for reflection-based column mapping
-keep class com.dasurv.data.local.entity.** { *; }

# Room DAO — keep abstract methods
-keep interface com.dasurv.data.local.dao.** { *; }

# Keep Compose @Stable/@Immutable annotations (used for recomposition skipping)
-keep @androidx.compose.runtime.Stable class * { *; }
-keep @androidx.compose.runtime.Immutable class * { *; }

# ML Kit face detection
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# Data model classes used in state flows
-keep class com.dasurv.data.model.** { *; }

# Hilt — consumer rules are bundled, but keep generated components
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Kotlin serialization / coroutines
-dontwarn kotlinx.coroutines.**
-keep class kotlinx.coroutines.** { *; }

# ExifInterface
-dontwarn androidx.exifinterface.**
