import os

def strip_bom(filepath):
    try:
        with open(filepath, 'rb') as f:
            content = f.read()
        if content.startswith(b'\xef\xbb\xbf'):
            content = content[3:]
            with open(filepath, 'wb') as f:
                f.write(content)
            print(f"Stripped BOM from {filepath}")
    except Exception as e:
        print(f"Error {filepath}: {e}")

directory = 'code/src/main/java/com/eternalclash2'
for root, _, files in os.walk(directory):
    for file in files:
        if file.endswith('.java'):
            strip_bom(os.path.join(root, file))
