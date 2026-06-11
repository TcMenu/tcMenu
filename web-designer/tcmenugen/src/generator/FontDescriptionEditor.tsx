import React, {useState, useEffect} from 'react';
import {FontDescription, FontMode, prettyPrintFont} from "./TcCodeGeneration";
import {IntegerEditor} from "../menuedit/IntegerEditor";

interface FontDescriptionEditorProps {
    initialFont: FontDescription;
    displaySupportedFonts: FontMode[];
    onSave: (font: FontDescription) => void;
}

interface FontAvailable {
    description: string;
    fontDef: string;
}

const AVAILABLE_FONTS: { [key in FontMode]?: FontAvailable[] } = {
    [FontMode.DEFAULT_FONT]: [],
    [FontMode.ADAFRUIT]: [
        { description: "FreeMono12pt7b", fontDef: "FreeMono12pt7b" },
        { description: "FreeMono18pt7b", fontDef: "FreeMono18pt7b" },
        { description: "FreeMono24pt7b", fontDef: "FreeMono24pt7b" },
        { description: "FreeMono9pt7b", fontDef: "FreeMono9pt7b" },
        { description: "FreeMonoBold12pt7b", fontDef: "FreeMonoBold12pt7b" },
        { description: "FreeMonoBold18pt7b", fontDef: "FreeMonoBold18pt7b" },
        { description: "FreeMonoBold24pt7b", fontDef: "FreeMonoBold24pt7b" },
        { description: "FreeMonoBold9pt7b", fontDef: "FreeMonoBold9pt7b" },
        { description: "FreeMonoBoldOblique12pt7b", fontDef: "FreeMonoBoldOblique12pt7b" },
        { description: "FreeMonoBoldOblique18pt7b", fontDef: "FreeMonoBoldOblique18pt7b" },
        { description: "FreeMonoBoldOblique24pt7b", fontDef: "FreeMonoBoldOblique24pt7b" },
        { description: "FreeMonoBoldOblique9pt7b", fontDef: "FreeMonoBoldOblique9pt7b" },
        { description: "FreeMonoOblique12pt7b", fontDef: "FreeMonoOblique12pt7b" },
        { description: "FreeMonoOblique18pt7b", fontDef: "FreeMonoOblique18pt7b" },
        { description: "FreeMonoOblique24pt7b", fontDef: "FreeMonoOblique24pt7b" },
        { description: "FreeMonoOblique9pt7b", fontDef: "FreeMonoOblique9pt7b" },
        { description: "FreeSans12pt7b", fontDef: "FreeSans12pt7b" },
        { description: "FreeSans18pt7b", fontDef: "FreeSans18pt7b" },
        { description: "FreeSans24pt7b", fontDef: "FreeSans24pt7b" },
        { description: "FreeSans9pt7b", fontDef: "FreeSans9pt7b" },
        { description: "FreeSansBold12pt7b", fontDef: "FreeSansBold12pt7b" },
        { description: "FreeSansBold18pt7b", fontDef: "FreeSansBold18pt7b" },
        { description: "FreeSansBold24pt7b", fontDef: "FreeSansBold24pt7b" },
        { description: "FreeSansBold9pt7b", fontDef: "FreeSansBold9pt7b" },
        { description: "FreeSansBoldOblique12pt7b", fontDef: "FreeSansBoldOblique12pt7b" },
        { description: "FreeSansBoldOblique18pt7b", fontDef: "FreeSansBoldOblique18pt7b" },
        { description: "FreeSansBoldOblique24pt7b", fontDef: "FreeSansBoldOblique24pt7b" },
        { description: "FreeSansBoldOblique9pt7b", fontDef: "FreeSansBoldOblique9pt7b" },
        { description: "FreeSansOblique12pt7b", fontDef: "FreeSansOblique12pt7b" },
        { description: "FreeSansOblique18pt7b", fontDef: "FreeSansOblique18pt7b" },
        { description: "FreeSansOblique24pt7b", fontDef: "FreeSansOblique24pt7b" },
        { description: "FreeSansOblique9pt7b", fontDef: "FreeSansOblique9pt7b" },
        { description: "FreeSerif12pt7b", fontDef: "FreeSerif12pt7b" },
        { description: "FreeSerif18pt7b", fontDef: "FreeSerif18pt7b" },
        { description: "FreeSerif24pt7b", fontDef: "FreeSerif24pt7b" },
        { description: "FreeSerif9pt7b", fontDef: "FreeSerif9pt7b" },
        { description: "FreeSerifBold12pt7b", fontDef: "FreeSerifBold12pt7b" },
        { description: "FreeSerifBold18pt7b", fontDef: "FreeSerifBold18pt7b" },
        { description: "FreeSerifBold24pt7b", fontDef: "FreeSerifBold24pt7b" },
        { description: "FreeSerifBold9pt7b", fontDef: "FreeSerifBold9pt7b" },
        { description: "FreeSerifBoldItalic12pt7b", fontDef: "FreeSerifBoldItalic12pt7b" },
        { description: "FreeSerifBoldItalic18pt7b", fontDef: "FreeSerifBoldItalic18pt7b" },
        { description: "FreeSerifBoldItalic24pt7b", fontDef: "FreeSerifBoldItalic24pt7b" },
        { description: "FreeSerifBoldItalic9pt7b", fontDef: "FreeSerifBoldItalic9pt7b" },
        { description: "FreeSerifItalic12pt7b", fontDef: "FreeSerifItalic12pt7b" },
        { description: "FreeSerifItalic18pt7b", fontDef: "FreeSerifItalic18pt7b" },
        { description: "FreeSerifItalic24pt7b", fontDef: "FreeSerifItalic24pt7b" },
        { description: "FreeSerifItalic9pt7b", fontDef: "FreeSerifItalic9pt7b" }

    ],
    [FontMode.ADAFRUIT_LOCAL]: [],
    [FontMode.TCUNICODE]: [
        { description: "B612Regular8pt", fontDef: "B612Regular8pt" },
        { description: "OpenSansCyrillicLatin12", fontDef: "OpenSansCyrillicLatin12" },
        { description: "OpenSansCyrillicLatin14", fontDef: "OpenSansCyrillicLatin14" },
        { description: "OpenSansCyrillicLatin18", fontDef: "OpenSansCyrillicLatin18" },
        { description: "OpenSansRegular10pt", fontDef: "OpenSansRegular10pt" },
        { description: "OpenSansRegular12pt", fontDef: "OpenSansRegular12pt" },
        { description: "OpenSansRegular14pt", fontDef: "OpenSansRegular14pt" },
        { description: "OpenSansRegular16pt", fontDef: "OpenSansRegular16pt" },
        { description: "OpenSansRegular18pt", fontDef: "OpenSansRegular18pt" },
        { description: "OpenSansRegular7pt", fontDef: "OpenSansRegular7pt" },
        { description: "OpenSansRegular8pt", fontDef: "OpenSansRegular8pt" },
        { description: "RobotoMedium24", fontDef: "RobotoMedium24" },
        { description: "RobotoRegular12pt", fontDef: "RobotoRegular12pt" },
        { description: "RobotoRegular14pt", fontDef: "RobotoRegular14pt" },
        { description: "RobotoRegular16pt", fontDef: "RobotoRegular16pt" },
        { description: "RobotoRegular18pt", fontDef: "RobotoRegular18pt" }
    ],
    [FontMode.TCUNICODE_LOCAL]: [
    ],
    [FontMode.U8G2]: [
        { description: "u8g2_font_6x10_tf", fontDef: "u8g2_font_6x10_tf" },
        { description: "u8g2_font_6x12_tf", fontDef: "u8g2_font_6x12_tf" },
        { description: "u8g2_font_7x14_tf", fontDef: "u8g2_font_7x14_tf" },
        { description: "u8g2_font_8x13_tf", fontDef: "u8g2_font_8x13_tf" },
        { description: "u8g2_font_9x15_tf", fontDef: "u8g2_font_9x15_tf" },
        { description: "u8g2_font_10x20_tf", fontDef: "u8g2_font_10x20_tf" },
        { description: "u8g2_font_tom_thumb_4x6_tf", fontDef: "u8g2_font_tom_thumb_4x6_tf" },
        { description: "u8g2_font_ncenB14_tr", fontDef: "u8g2_font_ncenB14_tr" },
        { description: "u8g2_font_boutique_bitmap_7x7_tf", fontDef: "u8g2_font_boutique_bitmap_7x7_tf" },
        { description: "u8g2_font_boutique_bitmap_9x9_tf", fontDef: "u8g2_font_boutique_bitmap_9x9_tf" },
        { description: "u8g2_font_6x10_tf", fontDef: "u8g2_font_6x10_tf" }
    ],
    [FontMode.NUMBERED]: [
    ]
};

export const FontDescriptionEditor: React.FC<FontDescriptionEditorProps> = ({ initialFont, displaySupportedFonts, onSave }) => {
    const [isEditing, setIsEditing] = useState<boolean>(false);
    const [fontType, setFontType] = useState<FontMode>(initialFont.fontType);
    const [fontName, setFontName] = useState<string>(initialFont.fontName);
    const [fontMag, setFontMag] = useState<number>(initialFont.fontMag);

    const isUnicode = fontType === FontMode.TCUNICODE || fontType === FontMode.TCUNICODE_LOCAL;
    const isAdafruit = fontType === FontMode.ADAFRUIT || fontType === FontMode.ADAFRUIT_LOCAL;

    useEffect(() => {
        setFontType(initialFont.fontType);
        setFontName(initialFont.fontName);
        setFontMag(initialFont.fontMag);
    }, [initialFont]);

    useEffect(() => {
        if (isUnicode) {
            setFontMag(0);
        } else if (isAdafruit) {
            if (fontMag < 1 || fontMag > 8) {
                setFontMag(1);
            }
        }
    }, [fontType, isUnicode, isAdafruit, fontMag]);

    const handleSave = () => {
        onSave(new FontDescription(fontType, fontName, fontMag));
        setIsEditing(false);
    };

    const handleCancel = () => {
        setFontType(initialFont.fontType);
        setFontName(initialFont.fontName);
        setFontMag(initialFont.fontMag);
        setIsEditing(false);
    };

    if (!isEditing) {
        return (
            <div className="font-description-collapsed" onClick={() => setIsEditing(true)}>
                <span>{initialFont.toString()}</span>
                <button type="button" className="edit-font-button">Edit</button>
            </div>
        );
    }

    return (
        <div className="font-description-editor">
            <div className="form-group">
                <label htmlFor="fontType">Font Type</label>
                <select
                    id="fontType"
                    value={fontType}
                    onChange={(e) => setFontType(parseInt(e.target.value) as FontMode)}
                >
                    {displaySupportedFonts.map(mode => (
                        <option key={mode} value={mode}>
                            {prettyPrintFont(mode)}
                        </option>
                    ))}
                </select>
            </div>

            <div className="form-group">
                <label htmlFor="fontName">Font Name</label>
                <input
                    type="text"
                    id="fontName"
                    list="fontNameList"
                    value={fontName}
                    onChange={(e) => setFontName(e.target.value)}
                    placeholder="Enter or select font name"
                />
                <datalist id="fontNameList">
                    {(AVAILABLE_FONTS[fontType] || []).map(f => (
                        <option key={f.fontDef} value={f.fontDef}>{f.description}</option>
                    ))}
                </datalist>
            </div>

            <div className="form-group">
                <label htmlFor="fontMag">Font Magnitude</label>
                <IntegerEditor
                    id="fontMag"
                    initialValue={fontMag}
                    min={isUnicode ? 0 : 1}
                    max={isAdafruit ? 8 : 100}
                    onChange={(val) => setFontMag(val)}
                    readOnly={isUnicode}
                />
            </div>

            <div className="font-editor-actions">
                <button type="button" onClick={handleSave}>Save Font</button>
                <button type="button" onClick={handleCancel}>Cancel</button>
            </div>
        </div>
    );
};
