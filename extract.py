import re

with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()
    
# Find the start and end of updateGameUI
match = re.search(r'function updateGameUI\(snapshot\) \{.*?\nasync function submitAction', js, re.DOTALL)
if match:
    with open('updateGameUI.js', 'w', encoding='utf-8') as out:
        out.write(match.group(0))
