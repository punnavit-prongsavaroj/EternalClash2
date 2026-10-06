import re

# 1. Update index.html
with open('code/src/main/resources/static/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

sfx_html_new = '''    <audio id="click-sfx"><source src="/sound/click.mp3" type="audio/mpeg"></audio>
    <audio id="farm-sfx"><source src="/sound/farm.mp3" type="audio/mpeg"></audio>
    <audio id="recruit-sfx"><source src="/sound/recruit.mp3" type="audio/mpeg"></audio>
    <audio id="attack-sfx"><source src="/sound/attack.mp3" type="audio/mpeg"></audio>
'''
html = re.sub(r'    <audio id="click-sfx">.*?</audio>\n', sfx_html_new, html, flags=re.DOTALL)

with open('code/src/main/resources/static/index.html', 'w', encoding='utf-8') as f:
    f.write(html)

# 2. Update app.js
with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

old_js_sfx = '''function playClickSound() {
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
});'''

new_js_sfx = '''function playSound(soundId) {
    const sfx = document.getElementById(soundId);
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

// ผูกระบบเสียงให้ทำงานตามประเภทของปุ่ม
document.addEventListener('click', function(e) {
    const btn = e.target.closest('button');
    if (btn && btn.id !== 'music-toggle') {
        if (btn.classList.contains('produce-btn')) {
            playSound('farm-sfx');
        } else if (btn.classList.contains('recruit-btn')) {
            playSound('recruit-sfx');
        } else if (btn.classList.contains('attack-btn')) {
            playSound('attack-sfx');
        } else {
            playSound('click-sfx'); // ปุ่มอื่นๆ ทั่วไปใช้เสียงคลิกธรรมดา
        }
    }
});'''

js = js.replace(old_js_sfx, new_js_sfx)

with open('code/src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)

print("Multiple SFX logic added")
