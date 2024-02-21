cmake_minimum_required(VERSION 3.27)
project(fontGlyphTest)

set(CMAKE_CXX_STANDARD 17)

add_executable(fontGlyphTest  main.cpp)

target_include_directories(fontGlyphTest PRIVATE ..)

target_link_libraries(fontGlyphTest tcMenuNative)
