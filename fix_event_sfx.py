import re

# 1. Update index.html
with open('code/src/main/resources/static/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

sfx_html_new = '''    <audio id="click-sfx"><source src="/sound/click.mp3" type="audio/mpeg"></audio>
    <audio id="farm-sfx"><source src="/sound/farm.mp3" type="audio/mpeg"></audio>
    <audio id="recruit-sfx"><source src="/sound/recruit.mp3" type="audio/mpeg"></audio>
    <audio id="attack-sfx"><source src="/sound/attack.mp3" type="audio/mpeg"></audio>
    <audio id="event-sfx"><source src="/sound/event.mp3" type="audio/mpeg"></audio>
    <audio id="battle-sfx"><source src="/sound/battle.mp3" type="audio/mpeg"></audio>
'''
html = re.sub(r'    <audio id="click-sfx">.*?</audio>\n', sfx_html_new, html, flags=re.DOTALL)

with open('code/src/main/resources/static/index.html', 'w', encoding='utf-8') as f:
    f.write(html)


# 2. Update app.js
with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

old_event_fetch = "const events = await resE.json();"
new_event_fetch = "const events = await resE.json();\\n                if(events.length > 0) playSound('event-sfx');"
js = js.replace(old_event_fetch, new_event_fetch.replace('\\\\n', '\\n'))

old_battle_fetch = "const battles = await resB.json();"
new_battle_fetch = "const battles = await resB.json();\\n                if(battles.length > 0) playSound('battle-sfx');"
js = js.replace(old_battle_fetch, new_battle_fetch.replace('\\\\n', '\\n'))

with open('code/src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)

print("Event and Battle SFX logic added")
