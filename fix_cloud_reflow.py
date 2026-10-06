import re

with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

old_transition = '''    // สั่ง Reflow ให้เบราว์เซอร์รับรู้ค่าเริ่มต้นก่อน animate
    void overlay.offsetWidth;

    // Phase 1: เมฆลอยเข้ามาบังเต็มจอ
    clouds.forEach(c => {
        c.style.left = (Math.random() * 100 - 10) + 'vw';
    });'''

new_transition = '''    // ใช้ setTimeout เพื่อกระตุ้นให้ CSS Transition ทำงานแน่นอน 100%
    setTimeout(() => {
        // Phase 1: เมฆลอยเข้ามาบังเต็มจอ
        clouds.forEach(c => {
            c.style.left = (Math.random() * 100 - 10) + 'vw';
        });
    }, 50);'''

if "setTimeout(() => {\n        // Phase 1" not in js:
    js = js.replace(old_transition, new_transition)

# Change cloud color to pure white for better visibility
js = js.replace("cloud.style.backgroundColor = '#ecf0f1';", "cloud.style.backgroundColor = '#ffffff';")

with open('code/src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)

print("Cloud reflow logic fixed")
