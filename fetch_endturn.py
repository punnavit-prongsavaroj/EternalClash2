with open('code/src/main/resources/static/index.html', 'r', encoding='utf-8') as f:
    lines = f.readlines()
for i, line in enumerate(lines):
    if 'จบเทิร์น' in line:
        print(line.strip())
