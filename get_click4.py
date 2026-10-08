with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    lines = f.readlines()

for i in range(max(0, 705-20), min(705+10, len(lines))):
    print(lines[i], end='')
