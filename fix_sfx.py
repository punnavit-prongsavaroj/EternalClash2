import re

# 1. Update index.html
with open('code/src/main/resources/static/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

sfx_html = '''    <audio id="click-sfx">
        <source src="/sound/click.mp3" type="audio/mpeg">
    </audio>
'''
if 'id="click-sfx"' not in html:
    html = html.replace('id="bg-music" loop>', 'id="bg-music" loop>\n' + sfx_html)
    with open('code/src/main/resources/static/index.html', 'w', encoding='utf-8') as f:
        f.write(html)

# 2. Update app.js
with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

js_sfx = '''
// --- ระบบเสียง SFX ตอนกดปุ่ม ---
function playClickSound() {
    const sfx = document.getElementById('click-sfx');
    if (sfx) {
        sfx.currentTime = 0;
        let playPromise = sfx.play();
        if (playPromise !== undefined) {
            playPromise.catch(error => {
                // Ignore autoplay block for clicks
            });
        }
    }
}

// ผูกระบบเสียงกับปุ่มทั้งหมดในจออัตโนมัติ
document.addEventListener('click', function(e) {
    if (e.target.tagName === 'BUTTON' || e.target.closest('button')) {
        const btn = e.target.closest('button');
        if (btn && btn.id !== 'music-toggle') {
            playClickSound();
        }
    }
});
'''
if 'playClickSound()' not in js:
    js = js + '\n' + js_sfx
    with open('code/src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
        f.write(js)

print("SFX logic added")
