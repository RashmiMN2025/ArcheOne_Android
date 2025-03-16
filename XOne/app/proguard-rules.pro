# Retrofit and OkHttp
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-dontwarn retrofit2.**

# Keep your API interfaces
-keep interface com.archeGlobal.one.network.** { *; }

# Keep data classes used in API responses
-keep class com.archeGlobal.one.model.** { *; }

# Keep Gson-related classes if you're using Gson
-keep class com.google.gson.** { *; }
-keepattributes Signature
-keepattributes *Annotation*
