import re

# 1. Update index.html
with open('code/src/main/resources/static/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

html = html.replace('src="/sound/bgm.mp3"', 'src="/sound/menu_bgm.mp3"')

with open('code/src/main/resources/static/index.html', 'w', encoding='utf-8') as f:
    f.write(html)

# 2. Update app.js
with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

game_transition_old = "document.getElementById('game-screen').style.display = 'block';"
game_transition_new = '''document.getElementById('game-screen').style.display = 'block';
            
            // เปลี่ยนเพลงเป็นเพลงในเกม
            const bgMusic = document.getElementById('bg-music');
            if (bgMusic.src.includes('menu_bgm.mp3')) {
                bgMusic.src = '/sound/game_bgm.mp3';
                if (isMusicPlaying) {
                    bgMusic.play().catch(e => console.log(e));
                }
            }'''

if "bgMusic.src = '/sound/game_bgm.mp3';" not in js:
    js = js.replace(game_transition_old, game_transition_new)

game_over_old = "document.getElementById('game-over-screen').style.display = 'flex';"
game_over_new = '''document.getElementById('game-over-screen').style.display = 'flex';
            
            // ปิดเพลงหรือเปลี่ยนเพลงตอนจบ (ถ้าต้องการ)
            // document.getElementById('bg-music').pause();'''

js = js.replace(game_over_old, game_over_new)

with open('code/src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)

print("Music logic updated for two phases")
