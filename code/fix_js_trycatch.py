import re

with open('src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

old_funcs = '''async function rerollMarshal() {
    const playerId = localStorage.getItem('eternalClashPlayerId');
    const res = await fetch(API_BASE_URL + '/players/' + playerId + '/marshal-candidates/reroll', { method: 'POST' });
    if(res.ok) {
        const c = await res.json();
        renderDraftCard(c.marshal);
    } else {
        alert("คุณสุ่มใหม่ครบจำนวนแล้ว!");
    }
}

async function confirmMarshal() {
    const playerId = localStorage.getItem('eternalClashPlayerId');
    const res = await fetch(API_BASE_URL + '/players/' + playerId + '/marshal-candidates/choose', { method: 'POST' });
    if(res.ok) {
        const m = await res.json();
        document.getElementById('ui-marshal').innerText = m.name;
        document.getElementById('marshal-single-card').style.display = 'none';
        document.getElementById('draft-buttons-wrapper').style.display = 'none';
        document.getElementById('draft-waiting').style.display = 'block';
        document.getElementById('draft-waiting').innerHTML = 'ท่านเลือก <strong style="color:white">' + m.name + '</strong> แล้ว<br>กำลังรอผู้เล่นอื่น...';
    }
}'''

new_funcs = '''async function rerollMarshal() {
    try {
        const playerId = localStorage.getItem('eternalClashPlayerId');
        const res = await fetch(API_BASE_URL + '/players/' + playerId + '/marshal-candidates/reroll', { method: 'POST' });
        if(res.ok) {
            const c = await res.json();
            renderDraftCard(c.marshal);
        } else {
            alert("คุณสุ่มใหม่ครบจำนวนแล้ว!");
        }
    } catch(e) {
        console.warn("เซิร์ฟเวอร์กำลังรีสตาร์ท กรุณารอสักครู่...");
    }
}

async function confirmMarshal() {
    try {
        const playerId = localStorage.getItem('eternalClashPlayerId');
        const res = await fetch(API_BASE_URL + '/players/' + playerId + '/marshal-candidates/choose', { method: 'POST' });
        if(res.ok) {
            const m = await res.json();
            document.getElementById('ui-marshal').innerText = m.name;
            document.getElementById('marshal-single-card').style.display = 'none';
            document.getElementById('draft-buttons-wrapper').style.display = 'none';
            document.getElementById('draft-waiting').style.display = 'block';
            document.getElementById('draft-waiting').innerHTML = 'ท่านเลือก <strong style="color:white">' + m.name + '</strong> แล้ว<br>กำลังรอผู้เล่นอื่น...';
        }
    } catch(e) {
        console.warn("เซิร์ฟเวอร์กำลังรีสตาร์ท กรุณารอสักครู่...");
    }
}'''

js = js.replace(old_funcs, new_funcs)

with open('src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)
