# ============================================================================
# ProGuard / R8 rules for Spot the Shade
# ============================================================================

# --- Crash stack traces: keep source file + line numbers ---
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# --- Kotlin Metadata (needed for reflection) ---
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
-keep class kotlin.Metadata { *; }

# --- Kotlin Coroutines ---
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.flow.**

# --- Hilt / Dagger ---
-dontwarn dagger.hilt.internal.**
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }
-keepclasseswithmembers class * {
    @dagger.* <methods>;
}
-keepclasseswithmembers class * {
    @javax.inject.* <fields>;
}
-keepclasseswithmembers class * {
    @javax.inject.* <init>(...);
}

# --- DataStore / Preferences ---
-keep class androidx.datastore.** { *; }
-keepclassmembers class * extends androidx.datastore.preferences.protobuf.GeneratedMessageLite {
    <fields>;
}

# --- Jetpack Compose ---
-dontwarn androidx.compose.**
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }

# --- Navigation Compose ---
-keep class * extends androidx.navigation.Navigator { *; }

# --- Enum classes (keep values/valueOf) ---
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# --- App data classes used in StateFlow ---
-keep class app.krafted.spottheshade.data.model.GameState { *; }
-keep class app.krafted.spottheshade.data.model.GridItem { *; }
-keep class app.krafted.spottheshade.data.model.HSLColor { *; }
-keep class app.krafted.spottheshade.data.model.ThemeType { *; }
-keep class app.krafted.spottheshade.data.model.GameResult { *; }
-keep class app.krafted.spottheshade.data.model.UserPreferences { *; }

# --- Android standard keeps ---
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# --- Keep Hilt-generated Application class ---
-keep class app.krafted.spottheshade.SpotTheShadeApplication { *; }
