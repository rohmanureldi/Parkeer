cmake_minimum_required(VERSION 3.22)
project(parkeer_crypto)

add_library(parkeer_crypto SHARED
        parkeer_crypto.cpp
)

target_link_libraries(parkeer_crypto
        android
        log
)

# 16KB page size alignment (required for Android 15+)
target_link_options(parkeer_crypto PRIVATE "-Wl,-z,max-page-size=16384")

# Strip symbols in release
set(CMAKE_C_FLAGS_RELEASE "${CMAKE_C_FLAGS_RELEASE} -s -O2")
set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS_RELEASE} -s -O2")
