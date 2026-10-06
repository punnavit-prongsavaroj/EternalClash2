import re

# 1. Update index.html
with open('code/src/main/resources/static/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

audio_html = '''
    <!-- ระบบ BGM -->
    <audio id="bg-music" loop>
        <source src="/sound/bgm.mp3" type="audio/mpeg">
    </audio>
    <button id="music-toggle" onclick="toggleMusic()" style="position: fixed; top: 20px; right: 20px; z-index: 1000; background: rgba(0,0,0,0.7); color: white; border: 2px solid #f39c12; width: 50px; height: 50px; border-radius: 50%; cursor: pointer; font-size: 1.5rem; display: flex; justify-content: center; align-items: center; box-shadow: 0 4px 10px rgba(0,0,0,0.5);">
        🔇
    </button>
'''
if 'id="bg-music"' not in html:
    html = html.replace('<body>', '<body>\n' + audio_html)
    with open('code/src/main/resources/static/index.html', 'w', encoding='utf-8') as f:
        f.write(html)

# 2. Update app.js
with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

js_music = '''
// --- ระบบเพลง BGM ---
let isMusicPlaying = false;
function toggleMusic() {
    const bgMusic = document.getElementById('bg-music');
    const toggleBtn = document.getElementById('music-toggle');
    if (isMusicPlaying) {
        bgMusic.pause();
        toggleBtn.innerText = '🔇';
        isMusicPlaying = false;
    } else {
        let playPromise = bgMusic.play();
        if (playPromise !== undefined) {
            playPromise.then(_ => {
                toggleBtn.innerText = '🔊';
                isMusicPlaying = true;
            }).catch(error => {
                console.warn("เบราว์เซอร์บล็อกการเล่นเพลงอัตโนมัติ");
            });
        }
    }
}
'''
if 'toggleMusic()' not in js:
    js = js + '\n' + js_music
    with open('code/src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
        f.write(js)

# 3. Create sound directory
import os
os.makedirs('code/src/main/resources/static/sound', exist_ok=True)

print("Audio UI added")
