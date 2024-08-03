#
# This build file should work of all platforms, although presently tested on MacOSX and Windows. On Windows it requires
# that you have built the FreeType package yourself, whereas on MacOS this package would normally be available if you
# have C++ developer tools installed. You may need to set FREETYPE_DIR yourself and on Windows it is assumed that the
# statically linked library is available there.
#

cmake_minimum_required(VERSION 3.27)
project(tcMenuNative)
set(CMAKE_CXX_STANDARD 17)
SET(CMAKE_CXX_FLAGS  "${CMAKE_CXX_FLAGS} -DLIBRARY_EXPORTS=1")

# On unix systems freetype will generally be available when developer tools are installed. We find it.
if(UNIX)
    find_package(Freetype REQUIRED)
    find_package(PNG REQUIRED)
    find_package(ZLIB REQUIRED)
endif()

# Add our source to the library that we'll build.
add_library(tcMenuNative SHARED library.cpp FreeFontHandler.cpp)

# Pull in the free type headers.
if(UNIX AND NOT APPLE)
#    I don't like this but for now we need to do this as the find_package doesn't
#    correctly locate the includes.
    target_include_directories(tcMenuNative PRIVATE /usr/include/freetype2)
else ()
    target_include_directories(tcMenuNative PRIVATE ${FREETYPE_DIR})
endif ()

if(UNIX)
    # On unix we need to link against the three libraries needed by freetype, these were found above using find_package
    target_link_libraries(tcMenuNative
            ${FREETYPE_LIBRARY}
            ${PNG_LIBRARY}
            ${ZLIB_LIBRARY})
else ()
    # on windows however, we need to build freetype from source in release mode as a static library. Consult the freetype
    # docs if you don't want to use the tcMenuPackaged one
    target_link_libraries(tcMenuNative "${CMAKE_CURRENT_SOURCE_DIR}/packaged/win/freetype.lib")
endif()

# and include the test project
add_subdirectory(fontTest)
