with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

js = js.replace('const events = await resE.json();\\n', 'const events = await resE.json();\\n')
# Wait, it's literal \n
js = js.replace('const events = await resE.json();\\\\n', 'const events = await resE.json();\\n')

# Actually, let's just use regex to remove literal \n if it's there
import re
js = re.sub(r'const events = await resE\.json\(\);\s*\\n\s*', 'const events = await resE.json();\\n', js)
js = re.sub(r'const battles = await resB\.json\(\);\s*\\n\s*', 'const battles = await resB.json();\\n', js)

with open('code/src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)
