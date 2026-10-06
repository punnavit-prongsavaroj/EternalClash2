import re

with open('src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

# Add bg filter update to simulateNextTurn
old_code = "document.getElementById('current-turn-display').innerText = 'เทิร์นที่: ' + turnCounter + ' (' + s.th + ' - ' + t + ')';"
new_code = '''
    const bgMap = document.getElementById('dynamic-map-bg');
    if(s.id === 'winter') bgMap.style.filter = 'grayscale(0.6) brightness(1.2)';
    else if(t === 'night') bgMap.style.filter = 'brightness(0.4)';
    else bgMap.style.filter = 'none';
    
    document.getElementById('current-turn-display').innerText = 'เทิร์นที่: ' + turnCounter + ' (' + s.th + ' - ' + (t==='day'?'กลางวัน':'กลางคืน') + ')';
'''
js = js.replace(old_code, new_code)

# Replace the text day/night in renderMap as well
js = js.replace("randomTime + ')'", "(randomTime==='day'?'กลางวัน':'กลางคืน') + ')'")

with open('src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)
