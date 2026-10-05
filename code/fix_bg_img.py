import re

with open('src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

bg_logic_old_1 = '''    const bgMap = document.getElementById('dynamic-map-bg');
    if(randomSeason.id === 'winter') bgMap.style.filter = 'grayscale(0.6) brightness(1.2)';
    else if(randomTime === 'night') bgMap.style.filter = 'brightness(0.4)';
    else bgMap.style.filter = 'none';'''

bg_logic_new_1 = '''    const bgMap = document.getElementById('dynamic-map-bg');
    let bgUrl = '';
    if(randomSeason.id === 'summer') {
        bgUrl = randomTime === 'day' ? '/Map/sunny.jpg' : '/Map/Summer-night.jpg';
    } else if(randomSeason.id === 'rainy') {
        bgUrl = randomTime === 'day' ? '/Map/rainy-day.jpg' : '/Map/rainy-night.jpg';
    } else if(randomSeason.id === 'winter') {
        bgUrl = randomTime === 'day' ? '/Map/Snow-day.jpg' : '/Map/Snow-night.jpg';
    }
    bgMap.style.backgroundImage = 'url("' + bgUrl + '")';
    bgMap.style.filter = 'none';'''

js = js.replace(bg_logic_old_1, bg_logic_new_1)

bg_logic_old_2 = '''    const bgMap = document.getElementById('dynamic-map-bg');
    if(s.id === 'winter') bgMap.style.filter = 'grayscale(0.6) brightness(1.2)';
    else if(t === 'night') bgMap.style.filter = 'brightness(0.4)';
    else bgMap.style.filter = 'none';'''

bg_logic_new_2 = '''    const bgMap = document.getElementById('dynamic-map-bg');
    let bgUrl = '';
    if(s.id === 'summer') {
        bgUrl = t === 'day' ? '/Map/sunny.jpg' : '/Map/Summer-night.jpg';
    } else if(s.id === 'rainy') {
        bgUrl = t === 'day' ? '/Map/rainy-day.jpg' : '/Map/rainy-night.jpg';
    } else if(s.id === 'winter') {
        bgUrl = t === 'day' ? '/Map/Snow-day.jpg' : '/Map/Snow-night.jpg';
    }
    bgMap.style.backgroundImage = 'url("' + bgUrl + '")';
    bgMap.style.filter = 'none';'''

js = js.replace(bg_logic_old_2, bg_logic_new_2)

with open('src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)
