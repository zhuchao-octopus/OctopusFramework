@echo off
"F:\\Android\\SDK\\cmake\\3.18.1\\bin\\cmake.exe" ^
  "-HF:\\StudioProjects\\OctopusFramework\\libJOSUtils" ^
  "-DCMAKE_SYSTEM_NAME=Android" ^
  "-DCMAKE_EXPORT_COMPILE_COMMANDS=ON" ^
  "-DCMAKE_SYSTEM_VERSION=24" ^
  "-DANDROID_PLATFORM=android-24" ^
  "-DANDROID_ABI=x86_64" ^
  "-DCMAKE_ANDROID_ARCH_ABI=x86_64" ^
  "-DANDROID_NDK=F:\\Android\\SDK\\ndk\\21.1.6352462" ^
  "-DCMAKE_ANDROID_NDK=F:\\Android\\SDK\\ndk\\21.1.6352462" ^
  "-DCMAKE_TOOLCHAIN_FILE=F:\\Android\\SDK\\ndk\\21.1.6352462\\build\\cmake\\android.toolchain.cmake" ^
  "-DCMAKE_MAKE_PROGRAM=F:\\Android\\SDK\\cmake\\3.18.1\\bin\\ninja.exe" ^
  "-DCMAKE_CXX_FLAGS=-frtti -fexceptions" ^
  "-DCMAKE_LIBRARY_OUTPUT_DIRECTORY=F:\\StudioProjects\\OctopusFramework\\libJOSUtils\\build\\intermediates\\cxx\\RelWithDebInfo\\5e4w41x4\\obj\\x86_64" ^
  "-DCMAKE_RUNTIME_OUTPUT_DIRECTORY=F:\\StudioProjects\\OctopusFramework\\libJOSUtils\\build\\intermediates\\cxx\\RelWithDebInfo\\5e4w41x4\\obj\\x86_64" ^
  "-DCMAKE_BUILD_TYPE=RelWithDebInfo" ^
  "-BF:\\StudioProjects\\OctopusFramework\\libJOSUtils\\.cxx\\RelWithDebInfo\\5e4w41x4\\x86_64" ^
  -GNinja
