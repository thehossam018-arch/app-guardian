# App Guardian ProGuard rules.
# Keep kotlinx.serialization models used for DataStore persistence.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclasseswithmembers class com.hossam.appguardian.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.hossam.appguardian.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.hossam.appguardian.model.**$$serializer { *; }
-keepclassmembers class com.hossam.appguardian.model.** {
    ** Companion;
}

# Never strip logging removal in release (we avoid Log calls with sensitive data anyway).
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
}
