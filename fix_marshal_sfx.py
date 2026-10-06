import re

with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

old_click_logic = '''// ผูกระบบเสียงให้ทำงานตามประเภทของปุ่ม
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

new_click_logic = '''// ผูกระบบเสียงให้ทำงานตามประเภทของปุ่ม
document.addEventListener('click', function(e) {
    const btn = e.target.closest('button');
    if (btn && btn.id !== 'music-toggle') {
        // เช็คก่อนว่าเป็นปุ่มเลือกขุนพลหรือไม่ (ใช้คลาสสีซ้ำกับปุ่มในเกม)
        const onclickAttr = btn.getAttribute('onclick') || '';
        if (onclickAttr.includes('confirmMarshal') || onclickAttr.includes('rerollMarshal')) {
            playSound('click-sfx');
        } else if (btn.classList.contains('produce-btn')) {
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

if "onclickAttr.includes('confirmMarshal')" not in js:
    js = js.replace(old_click_logic, new_click_logic)
    with open('code/src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
        f.write(js)

print("Button click sound logic fixed for marshal draft")
