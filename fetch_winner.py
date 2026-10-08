with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    lines = f.readlines()
for i, line in enumerate(lines):
    if 'WINNER' in line or 'winner' in line.lower() or 'video' in line.lower() or 'FINISHED' in line:
        print(f"{i}: {line.strip()}")
