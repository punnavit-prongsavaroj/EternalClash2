import re

with open('src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

old_pool = '''const MARSHAL_POOL = [
    { id: 1, name: 'ขงเบ้ง', skill: 'รอดพ้นการทำลายเมือง 50%' },
    { id: 2, name: 'จูล่ง', skill: 'ลดเวลาเดินทัพ 1 เทิร์น' },
    { id: 3, name: 'จิวยี่', skill: 'กินเสบียง x1.25' },
    { id: 4, name: 'โจโฉ', skill: 'กบฏลดทรัพยากรลงครึ่งเดียว' }
];'''

new_pool = '''const MARSHAL_POOL = [
    { id: 1, name: 'ขงเบ้ง', skill: 'รอดพ้นการทำลายเมือง 50%', img: 'kong-beng.jpg' },
    { id: 2, name: 'จูล่ง', skill: 'ลดเวลาเดินทัพ 1 เทิร์น', img: 'Ju-long.jpg' },
    { id: 3, name: 'จิวยี่', skill: 'กินเสบียง x1.25', img: 'Jilyi.jpg' },
    { id: 4, name: 'โจโฉ', skill: 'กบฏลดทรัพยากรลงครึ่งเดียว', img: 'Jo-Sho.jpg' }
];'''

js = js.replace(old_pool, new_pool)

old_render = '''function renderCurrentDraft() {
    const card = document.getElementById('marshal-single-card');
    const m = MARSHAL_POOL[currentDraftIndex];
    card.innerHTML = '<h3>' + m.name + '</h3><p>สกิล: ' + m.skill + '</p>';
}'''

new_render = '''function renderCurrentDraft() {
    const card = document.getElementById('marshal-single-card');
    const m = MARSHAL_POOL[currentDraftIndex];
    card.innerHTML = '<img src="/Marshal/' + m.img + '" style="width: 100%; height: 250px; object-fit: cover; object-position: top; border-radius: 8px; margin-bottom: 15px; border: 2px solid #34495e;">' +
                     '<h3 style="font-size: 1.8rem;">' + m.name + '</h3><p style="font-size: 1.1rem;">สกิล: ' + m.skill + '</p>';
}'''

js = js.replace(old_render, new_render)

with open('src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)
