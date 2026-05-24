cmake_minimum_required(VERSION 3.22.1)
project(dualbt)

# Find Oboe via prefab
find_package(oboe REQUIRED CONFIG)

add_library(
    dualbt
    SHARED
    AudioSplitter.cpp
    jni_bridge.cpp
)

target_include_directories(dualbt PRIVATE .)

target_link_libraries(
    dualbt
    oboe::oboe
    android
    log
)
