#
# This build file should work of all platforms, although presently tested on MacOSX and Windows. On Windows it requires
# that you have built the FreeType package yourself. On Windows and macOS it is assumed that the statically linked
# FreeType library is available in the packaged directory for the platform.
#

cmake_minimum_required(VERSION 3.27)
project(tcMenuNative)
set(CMAKE_CXX_STANDARD 17)
SET(CMAKE_CXX_FLAGS  "${CMAKE_CXX_FLAGS} -DLIBRARY_EXPORTS=1")

if(UNIX AND NOT APPLE)
    find_package(Freetype REQUIRED)
    find_package(PNG REQUIRED)
    find_package(ZLIB REQUIRED)
endif()

# Add our source to the library that we'll build.
add_library(tcMenuNative SHARED library.cpp FreeFontHandler.cpp)

# Pull in the free type headers.
if(APPLE)
    target_include_directories(tcMenuNative PRIVATE ${FREETYPE_DIR})
elseif(UNIX)
    target_include_directories(tcMenuNative PRIVATE ${FREETYPE_INCLUDE_DIRS})
else ()
    target_include_directories(tcMenuNative PRIVATE ${FREETYPE_DIR})
endif ()

if(APPLE)
    target_link_libraries(tcMenuNative "${CMAKE_CURRENT_SOURCE_DIR}/packaged/mac/libfreetype.a")
elseif(UNIX)
    # On unix we need to link against the three libraries needed by freetype, these were found above using find_package
    target_link_libraries(tcMenuNative
            ${FREETYPE_LIBRARIES}
            ${PNG_LIBRARIES}
            ${ZLIB_LIBRARIES})
else ()
    # on windows however, we need to build freetype from source in release mode as a static library. Consult the freetype
    # docs if you don't want to use the tcMenuPackaged one
    target_link_libraries(tcMenuNative "${CMAKE_CURRENT_SOURCE_DIR}/packaged/win/freetype.lib")
endif()

# and include the test project
add_subdirectory(fontTest)
