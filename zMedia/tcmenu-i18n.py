import sys
import os
from pathlib import Path

# Default output directory to current directory if not provided
output_dir = sys.argv[1] if len(sys.argv) > 1 else "."

def load_properties(props_file):
    props_dict = {}
    if os.path.exists(props_file):
        with open(props_file, 'r', encoding='utf-8') as f:
            for line in f:
                line = line.strip()
                # Skip empty lines and comments
                if line and not line.startswith('#') and not line.startswith('!'):
                    # Split on first '=' or ':'
                    if '=' in line:
                        key, value = line.split('=', 1)
                        props_dict[key.strip()] = value.strip()
                    elif ':' in line:
                        key, value = line.split(':', 1)
                        props_dict[key.strip()] = value.strip()
    return props_dict

# Check if i18n directory exists
i18n_dir = os.path.join(".", "i18n")
if not os.path.exists(i18n_dir):
    print("Usage: requires an i18n directory. Structure:")
    print("  ./i18n/project-lang.properties")
    print("see: https://www.thecoderscorner.com/products/arduino-libraries/tc-menu/multi-language-locale-menu/")
    sys.exit(1)

print("""
    _____       __  __                  
   / / / |_ ___|  \\/  | ___ _ __  _   _ 
  / / /| __/ __| |\\/| |/ _ \\ '_ \\| | | |
 / / / | || (__| |  | |  __/ | | | |_| |
/_/_/   \\__\\___|_|  |_|\\___|_| |_|\\__,_|        
Properties file to header generator
""")

def_props = os.path.join(i18n_dir, "project-lang.properties")
def_props_dict = load_properties(def_props)

# Load all locale properties files into a dictionary of dictionaries
locale_props_dict = {}
for filename in os.listdir(i18n_dir):
    if filename.endswith('.properties') and filename != 'project-lang.properties':
        props_file = os.path.join(i18n_dir, filename)
        locale_props_dict[filename] = load_properties(props_file)
        print(f"Loaded {filename} with {len(locale_props_dict[filename])} entries")

print("Processing i18n files into " + output_dir)

# Extract locale names from filenames
locale_list = []
for filename in locale_props_dict.keys():
    # Extract locale code from filename (project-lang_XX.properties -> XX)
    if '_' in filename:
        locale_code = filename.split('_')[1].split('.')[0]
        locale_list.append(locale_code)

def to_upper_define_element(key):
    result = []
    current_word = []

    for char in key:
        if char.isalnum():
            current_word.append(char.upper())
        else:
            if current_word:
                result.append(''.join(current_word))
                current_word = []

    if current_word:
        result.append(''.join(current_word))

    return '_'.join(result)


# Get current directory name
current_dir_name = Path.cwd().name

# Construct filename and open for writing
lang_select_filename = f"{current_dir_name}_langSelect.h"
lang_select_path = os.path.join(output_dir, lang_select_filename)

with open(lang_select_path, 'w', encoding='utf-8') as lang_select_file:
    lang_select_file.write("// TcMenu Generated locale header file containing all locale definitions.\n")
    lang_select_file.write("// To enable a particular language set build flag TC_LOCALE_<LANG>\n\n\n")
    processed_something = False
    for locale in locale_list:
        print(f"Processing locale: {locale}")
        lang_select_file.write(f"// Definitions for locale {locale}\n")
        locale_props = locale_props_dict[f"project-lang_{locale}.properties"]
        if processed_something:
            lang_select_file.write(f"\n#elif defined(TC_LOCALE_{locale.upper()})\n")
        else:
            lang_select_file.write(f"#if defined(TC_LOCALE_{locale.upper()})\n")
            processed_something = True

        for key, value in def_props_dict.items():
            actual_val = ""
            if key not in locale_props:
                actual_val = def_props_dict.get(key, None)
            else:
                actual_val = locale_props.get(key, "")
            lang_select_file.write(f"#define TC_I18N_{to_upper_define_element(key)} \"{actual_val}\"\n")

        lang_select_file.write("\n#else // default locale\n")
        for key, value in def_props_dict.items():
            lang_select_file.write(f"#define TC_I18N_{to_upper_define_element(key)} \"{value}\"\n")
        lang_select_file.write("\n#endif // locale definitions\n\n")
        lang_select_file.write("\n// Its always better to use getTcLocaleString(string_id) method.\n")
        lang_select_file.write("#define getTcLocaleString(x) (x)\n")
