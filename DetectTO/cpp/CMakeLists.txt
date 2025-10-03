cmake_minimum_required(VERSION 3.18.1)  # match Android Studio CMake
project(native-lib)

# OpenCV path
set(OpenCV_DIR "${CMAKE_CURRENT_SOURCE_DIR}/OpenCV-android-sdk/sdk/native/jni")
find_package(OpenCV REQUIRED)

add_library(native-lib SHARED draw_box.cpp nmsfilter.cpp)

include_directories(${OpenCV_INCLUDE_DIRS})

find_library(log-lib log)

target_link_libraries(native-lib ${OpenCV_LIBS} ${log-lib})
