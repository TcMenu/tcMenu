## Using applicability definitions in plugins

Within plugins you may need to change the includes, source files, variables or functions called depending on the state of a property.

## Property expansion

Within nearly all places, you can use variable expansion, this works in nearly everywhere. To expand a property we simply put the property within `${PROPERTY_ID}` and it will be expanded if possible.

There is even an advanced mode for property expansion where you can use a regular expression to define how to extract the first capture group of the property, for example:

    Property Value: ada:font123,4
    ${ITEM_FONT/.*:([\w_]*),.*/}
    Becomes: font123

## Applicability definitions

Nearly all entries can have applicability applied to them. When applicability is applied to an item, the item with the condition is only included when the condition is true. We recommend you use an editor supporting XML schema, then you'll get auto compelete of these applicabilites.

Firsrly, we can reference a global applicability (see below) by adding `applicabilityRef`. For example:

    <GlobalVariable ... applicabilityRef="globallyAvailableRef"/>

We can check when a variable either is the value exactly, or is not the value

    <SourceFile ... whenProperty="PROP1" isValue="5" />
    <IncludeFile ... whenProperty="PROP1" isNotValue="5" />

We can also match on a regular expression, using Java regex compiler:

    <SetupFunction ... whenProperty="PROP1" matches="regex" /> 

## Global Applicability definitions

At the top level of the plugin we can define an `ApplicabilityDef`, this can then be referred to throughout the document. In the case below we show a very complex nested applicability where INTERRUPT_SWITCHES is true and either INT_PROP is 10 or (ROOT is test and INT_PROP is 20). You can build very complex arrangements with this. However, it should be avoided when possible, preferring simple inline applicability shown above.

    <ApplicabilityDefs>
        <ApplicabilityDef key="globallyAvailableRef" mode="and">
            <Applicability whenProperty="INTERRUPT_SWITCHES" isValue="true"/>
            <ApplicabilityDef mode="or">
                <Applicability whenProperty="INT_PROP" isValue="10"/>
                <ApplicabilityDef>
                    <Applicability whenProperty="ROOT" isValue="test"/>
                    <Applicability whenProperty="INT_PROP" isValue="20"/>
                </ApplicabilityDef>
            </ApplicabilityDef>
        </ApplicabilityDef>
    </ApplicabilityDefs>