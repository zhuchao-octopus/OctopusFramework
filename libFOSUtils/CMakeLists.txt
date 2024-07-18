# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)
project(MyJniProjects)

Set(CMAKE VERBOSE MAKEFILE On)

# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add.library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.


SET(BT_PATH ${CMAKE_CURRENT_SOURCE_DIR}/src/main/cpp/bt)
SET(MCU_PATH ${CMAKE_CURRENT_SOURCE_DIR}/src/main/cpp/mcu)
SET(UART_PATH ${CMAKE_CURRENT_SOURCE_DIR}/src/main/cpp/uart)
SET(DVD_PATH ${CMAKE_CURRENT_SOURCE_DIR}/src/main/cpp/dvd)
SET(SETTING_PATH ${CMAKE_CURRENT_SOURCE_DIR}/src/main/cpp/setting)
SET(UPDATE_PATH ${CMAKE_CURRENT_SOURCE_DIR}/src/main/cpp/update)

include_directories(BT_PATH)
include_directories(MCU_PATH)
include_directories(UART_PATH)
include_directories(DVD_PATH)
include_directories(SETTING_PATH)
include_directories(UPDATE_PATH)

ADD_SUBDIRECTORY(${BT_PATH})
ADD_SUBDIRECTORY(${MCU_PATH})
ADD_SUBDIRECTORY(${UART_PATH})
ADD_SUBDIRECTORY(${DVD_PATH})
ADD_SUBDIRECTORY(${SETTING_PATH})
ADD_SUBDIRECTORY(${UPDATE_PATH})