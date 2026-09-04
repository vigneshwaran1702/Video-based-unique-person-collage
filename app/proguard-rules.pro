# Add project specific ProGuard rules here.

-keep class com.vignesh.uniquepersoncollage.data.model.** { *; }
-keep class com.google.mlkit.vision.face.** { *; }
-keep class org.tensorflow.lite.** { *; }

# Keep Compose runtime
-keep class androidx.compose.** { *; }
