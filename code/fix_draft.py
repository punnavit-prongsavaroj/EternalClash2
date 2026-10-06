import re

# Update HTML card style (Remove old headers)
with open('src/main/resources/static/index.html', 'r', encoding='utf-8') as f:
    html = f.read()

old_headers = '''<h2 style="color: #f39c12; font-size: 2.5rem; margin-bottom: 20px;">เลือกขุนพลคู่กาย</h2>
            <p style="color: white; font-size: 1.2rem; margin-bottom: 30px;">โปรดพิจารณาขุนพลที่จะนำทัพของคุณ</p>'''
html = html.replace(old_headers, '')

with open('src/main/resources/static/index.html', 'w', encoding='utf-8') as f:
    f.write(html)

# Update JS render function & MARSHAL_POOL
with open('src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

old_pool = '''const MARSHAL_POOL = [
    { id: 1, name: 'ขงเบ้ง', skill: 'รอดพ้นการทำลายเมือง 50%', img: 'kong-beng.jpg' },
    { id: 2, name: 'จูล่ง', skill: 'ลดเวลาเดินทัพ 1 เทิร์น', img: 'Ju-long.jpg' },
    { id: 3, name: 'จิวยี่', skill: 'กินเสบียง x1.25', img: 'Jilyi.jpg' },
    { id: 4, name: 'โจโฉ', skill: 'กบฏลดทรัพยากรลงครึ่งเดียว', img: 'Jo-Sho.jpg' }
];'''

new_pool = '''const MARSHAL_POOL = [
    { id: 1, name: 'ขงเบ้ง', skill: 'รอดพ้นการทำลายเมือง 50%', drawback: 'โอกาส 20% เดินทัพล้มเหลว (เสียแอคชัน)', img: 'kong-beng.jpg' },
    { id: 2, name: 'จูล่ง', skill: 'ลดเวลาเดินทัพ 1 เทิร์น', drawback: 'ไม่มีข้อเสียพิเศษ', img: 'Ju-long.jpg' },
    { id: 3, name: 'จิวยี่', skill: 'ความสามารถรบสูง', drawback: 'สิ้นเปลืองเสบียง x1.25', img: 'Jilyi.jpg' },
    { id: 4, name: 'โจโฉ', skill: 'ความเป็นผู้นำสูง', drawback: 'โอกาส 5% เกิดกบฏ (เสบียงและทหารลดครึ่ง)', img: 'Jo-Sho.jpg' }
];'''
js = js.replace(old_pool, new_pool)

old_render = '''function renderCurrentDraft() {
    const card = document.getElementById('marshal-single-card');
    const m = MARSHAL_POOL[currentDraftIndex];
    card.innerHTML = '<img src="/Marshal/' + m.img + '" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; object-fit: cover; object-position: top; z-index: 1;">' +
                     '<div style="position: absolute; bottom: 0; left: 0; width: 100%; padding: 40px 20px 20px; background: linear-gradient(to top, rgba(0,0,0,1) 10%, rgba(0,0,0,0.7) 60%, transparent); z-index: 2; box-sizing: border-box; text-align: center;">' +
                     '<h3 style="font-size: 2.8rem; margin-bottom: 5px; color: #f39c12; text-shadow: 2px 2px 4px #000; letter-spacing: 2px;">' + m.name + '</h3>' +
                     '<p style="font-size: 1.3rem; color: #ecf0f1; text-shadow: 1px 1px 3px #000;">สกิล: ' + m.skill + '</p>' +
                     '</div>';
}'''

new_render = '''function renderCurrentDraft() {
    const card = document.getElementById('marshal-single-card');
    const m = MARSHAL_POOL[currentDraftIndex];
    card.innerHTML = 
        '<img src="/Marshal/' + m.img + '" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; object-fit: cover; object-position: top; z-index: 1;">' +
        '<div style="position: absolute; top: 0; left: 0; width: 100%; padding: 25px 15px 40px; background: linear-gradient(to bottom, rgba(0,0,0,0.9) 10%, rgba(0,0,0,0.5) 60%, transparent); z-index: 2; box-sizing: border-box; text-align: center;">' +
            '<h2 style="color: #f39c12; font-size: 2rem; margin: 0 0 5px 0; text-shadow: 2px 2px 4px #000;">เลือกขุนพลคู่กาย</h2>' +
            '<p style="color: white; font-size: 1rem; margin: 0; text-shadow: 1px 1px 2px #000;">โปรดพิจารณาขุนพลที่จะนำทัพของคุณ</p>' +
        '</div>' +
        '<div style="position: absolute; bottom: 0; left: 0; width: 100%; padding: 50px 20px 20px; background: linear-gradient(to top, rgba(0,0,0,1) 10%, rgba(0,0,0,0.85) 60%, transparent); z-index: 2; box-sizing: border-box; text-align: center;">' +
            '<h3 style="font-size: 2.8rem; margin-bottom: 12px; color: #f39c12; text-shadow: 2px 2px 4px #000; letter-spacing: 2px;">' + m.name + '</h3>' +
            '<p style="font-size: 1.05rem; color: #2ecc71; text-shadow: 1px 1px 3px #000; margin-bottom: 6px;">✅ จุดเด่น: ' + m.skill + '</p>' +
            '<p style="font-size: 1.05rem; color: #e74c3c; text-shadow: 1px 1px 3px #000; margin: 0;">⚠️ ข้อเสีย: ' + m.drawback + '</p>' +
        '</div>';
}'''
js = js.replace(old_render, new_render)

with open('src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)
