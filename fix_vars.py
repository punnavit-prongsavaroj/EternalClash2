import re

with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

if 'let lastSeason = null;' not in js:
    js = js.replace('let lastTurn = -1;', 'let lastTurn = -1;\nlet lastSeason = null;\nlet lastDaytime = null;')
    
with open('code/src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)

print("Variables injected")
