with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    lines = f.readlines()

for i, line in enumerate(lines):
    if 'onclick' in line or 'addEventListener(\'click\'' in line:
        print(f"{i+1}: {line.strip()}")
