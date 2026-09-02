# ========================================================
# Sonexa Music - Production ProGuard / R8 Optimization Rules
# ========================================================

# Preserve line numbers and source file names for production crash stacktraces (e.g. Firebase Crashlytics / Play Console)
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod

# --------------------------------------------------------
# 1. Gson Models & Data Transfer Objects (DTOs)
# --------------------------------------------------------
# Keep all data models so Gson reflection/serialization does not strip or rename fields
-keep class com.sonexa.app.data.model.** { *; }
-keep class com.sonexa.app.data.local.SavedAccount { *; }
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
    @com.google.gson.annotations.Expose <fields>;
}
-keep class com.google.gson.** { *; }

# --------------------------------------------------------
# 2. Retrofit & OkHttp
# --------------------------------------------------------
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keep interface com.sonexa.app.data.api.** { *; }
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }

# --------------------------------------------------------
# 3. AndroidX Media3 & ExoPlayer
# --------------------------------------------------------
-keep class androidx.media3.** { *; }
-dontwarn androidx.media3.**
-keep class com.sonexa.app.audio.** { *; }

# --------------------------------------------------------
# 4. Coil Image Loading
# --------------------------------------------------------
-keep class coil.** { *; }
-dontwarn coil.**

# --------------------------------------------------------
# 5. CameraX & MLKit Face Detection (AI Mood Scanner)
# --------------------------------------------------------
-keep class com.google.mlkit.vision.face.** { *; }
-keep class androidx.camera.** { *; }
-dontwarn androidx.camera.**
-dontwarn com.google.mlkit.**

# --------------------------------------------------------
# 6. Google Sign-In & Credentials Manager
# --------------------------------------------------------
-keep class androidx.credentials.** { *; }
-keep class com.google.android.libraries.identity.googleid.** { *; }
-dontwarn androidx.credentials.**

# --------------------------------------------------------
# 7. Kotlin Coroutines & Serialization
# --------------------------------------------------------
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**