with open('code/src/main/resources/static/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

# Remove the event-sfx audio tag
html = html.replace('<audio id="event-sfx"><source src="/sound/event.mp3" type="audio/mpeg"></audio>\\n', '')

# Remove duplicates
html = html.replace('''    <audio id="farm-sfx"><source src="/sound/farm.mp3" type="audio/mpeg"></audio>
    <audio id="recruit-sfx"><source src="/sound/recruit.mp3" type="audio/mpeg"></audio>
    <audio id="attack-sfx"><source src="/sound/attack.mp3" type="audio/mpeg"></audio>
''', '', 1)

with open('code/src/main/resources/static/index.html', 'w', encoding='utf-8') as f:
    f.write(html)
