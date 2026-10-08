with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    lines = f.readlines()
for i, line in enumerate(lines):
    if 'function openAttackModal()' in line:
        for j in range(i, min(i+10, len(lines))):
            print(lines[j], end='')
        break
