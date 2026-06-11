package com.thecoderscorner.menu.editorui.generator.core;

import com.thecoderscorner.menu.domain.MenuItem;
import com.thecoderscorner.menu.domain.state.MenuTree;
import com.thecoderscorner.menu.editorui.generator.arduino.CallbackRequirement;

import java.util.List;
import java.util.Map;

/**
 * Represents a code generation interface for building a menu tree structure
 * based on a provided MenuTree, which generates C++ code for both header
 * and source files. Implementations of this interface perform tasks such as
 * initializing menu trees, generating the C++ menu structure, and associated
 * setup declarations.
 *
 * These are short lived objects with no expectation of thread safety. Therefore
 * they should be created for each conversion, and any implementation lightweight
 * enough for such repeated creations.
 */
public interface MenuTreeCodeGenerator {
    /**
     * Initialise the generator with the given menu tree. MUST be called before any other method.
     * @param tree the tree
     * @throws TcMenuConversionException if the tree cannot be processed.
     */
    void initialise(MenuTree tree) throws TcMenuConversionException;

    /**
     * Gets a list of headers that need to be generated.
     * @return the list of headers
     */
    List<HeaderDefinition> headersToGenerate();

    /**
     * Get the root menu code that will be added to the menu CPP file (or sketch file).
     * @param callbackRequirements the callbacks that are already identified.
     * @param extractor the variable extractor to use for variable generation.
     * @return the code to be added to the file.
     */
    String getRootMenuCode(Map<MenuItem, CallbackRequirement> callbackRequirements, CodeVariableExtractor extractor);

    /**
     * Gets the code that needs to be added to the header file.
     * @param extractor the variable extractor to use for variable generation.
     * @return the code to be added to the header file.
     */
    String getHeaderMenuCode(CodeVariableExtractor extractor);

    /**
     * Gets any code that's needed in setupMenu() in order for the menu to function.
     * @param extractor the variable extractor to use for variable generation.
     * @return the code to be added to setupMenu().
     */
    String generateSetupDeclaration(CodeVariableExtractor extractor);

    /**
     * Gets the first menu item (either by variable or calling getMenuById
     * @return the first item in the menu.
     */
    String getFirstMenuVariable();

    String getMenuItemAccessor(MenuItem item);
}
