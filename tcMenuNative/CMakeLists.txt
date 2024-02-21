cmake_minimum_required(VERSION 3.27)
project(tcMenuNative)

set(CMAKE_CXX_STANDARD 17)
SET(CMAKE_CXX_FLAGS  "${CMAKE_CXX_FLAGS} -DLIBRARY_EXPORTS=1")

add_library(tcMenuNative SHARED library.cpp FreeFontHandler.cpp)

target_include_directories(tcMenuNative PRIVATE ${FREETYPE_DIR}/include)

target_link_libraries(tcMenuNative "${PROJECT_SOURCE_DIR}/freetyped.lib")

add_subdirectory(fontTest)
