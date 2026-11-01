# Attributes needed by Retrofit and kotlinx.serialization
-keepattributes Signature, InnerClasses, EnclosingMethod, RuntimeVisibleAnnotations, AnnotationDefault, *Annotation*

# Safety net for the crash you hit
-keep class kotlin.sequences.** { *; }
-dontwarn kotlin.sequences.**

# Retrofit
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-keep interface com.example.salesforce.** { @retrofit2.http.* <methods>; }

# kotlinx.serialization
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class com.example.salesforce.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep @kotlinx.serialization.Serializable class * { *; }
-keepclassmembers class * {
    *** Companion;
}

# OkHttp / Okio
-dontwarn okhttp3.**
-dontwarn okio.**