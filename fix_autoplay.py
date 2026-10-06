import re

with open('code/src/main/resources/static/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

html = html.replace('>🔇</button>', '>🔊</button>')
html = html.replace('>&#128263;</button>', '>🔊</button>') # just in case
html = html.replace('🔇\n    </button>', '🔊\n    </button>')
html = html.replace('🔇', '🔊')

with open('code/src/main/resources/static/index.html', 'w', encoding='utf-8') as f:
    f.write(html)

with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

old_bgm = '''let isMusicPlaying = false;
function toggleMusic() {'''

new_bgm = '''let isMusicPlaying = true;
function toggleMusic() {'''

js = js.replace(old_bgm, new_bgm)

# Add auto-play attempt and click listener for overcoming autoplay policy
autoplay_logic = '''
// พยายามเล่นเพลงตอนเริ่ม และผูก Event กับการคลิกเพื่อให้เล่นเพลงเมื่อเบราว์เซอร์บล็อก
document.addEventListener('DOMContentLoaded', () => {
    const bgMusic = document.getElementById('bg-music');
    if (bgMusic && isMusicPlaying) {
        bgMusic.play().catch(e => console.warn("รอผู้เล่นคลิกเพื่อเล่นเพลง..."));
    }
});
document.addEventListener('click', () => {
    const bgMusic = document.getElementById('bg-music');
    if (bgMusic && isMusicPlaying && bgMusic.paused) {
        bgMusic.play().catch(e => {});
    }
}, { once: false }); // แอบพยายามเล่นเมื่อคลิกที่ไหนก็ได้บนจอ (จนกว่าจะดัง)
'''

if 'รอผู้เล่นคลิกเพื่อเล่นเพลง' not in js:
    js = js.replace('// --- ระบบเพลง BGM ---', '// --- ระบบเพลง BGM ---\n' + autoplay_logic)

with open('code/src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)

print("Autoplay enabled")
