import re

with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()
    
match = re.search(r'function handleSnapshot\(snapshot\) \{.*?// ----- DRAFT PHASE -----', js, re.DOTALL)
if match:
    with open('handleSnapshot.js', 'w', encoding='utf-8') as out:
        out.write(match.group(0))
