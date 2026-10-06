import re

with open('src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

# Replace the season logic
old_logic = "const seasons = ['spring', 'summer', 'autumn', 'winter'];"
new_logic = "const seasons = [{id: 'summer', th: 'ฤดูร้อน'}, {id: 'rainy', th: 'ฤดูฝน'}, {id: 'winter', th: 'ฤดูหนาว'}];"
js = js.replace(old_logic, new_logic)

# Replace the text assignment
old_text = "document.getElementById('current-turn-display').innerText = 'เทิร์นที่: ' + turnCounter + ' (' + s + ' - ' + t + ')';"
new_text = "document.getElementById('current-turn-display').innerText = 'เทิร์นที่: ' + turnCounter + ' (' + s.th + ' - ' + t + ')';"
js = js.replace(old_text, new_text)

old_text_start = "document.getElementById('current-turn-display').innerText = 'เทิร์นที่: 1 (' + randomSeason + ' - ' + randomTime + ')';"
new_text_start = "document.getElementById('current-turn-display').innerText = 'เทิร์นที่: 1 (' + randomSeason.th + ' - ' + randomTime + ')';"
js = js.replace(old_text_start, new_text_start)

# Update filter logic to check s.id
js = js.replace("randomSeason === 'winter'", "randomSeason.id === 'winter'")
js = js.replace("s === 'winter'", "s.id === 'winter'")

with open('src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)
