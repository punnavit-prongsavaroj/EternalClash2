import re

with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

# startGame
old_startGame = '''async function startGame() {
    const gameId = localStorage.getItem('eternalClashGameId');
    try {
        await fetch(API_BASE_URL + '/games/' + gameId + '/start', { method: 'POST' });
    } catch (error) { alert("เกิดข้อผิดพลาดในการเริ่มเกม"); }
}'''
new_startGame = '''async function startGame() {
    const gameId = localStorage.getItem('eternalClashGameId');
    const btn = document.querySelector('button[onclick="startGame()"]');
    if (btn) btn.style.display = 'none'; // Optimistic Update

    try {
        await fetch(API_BASE_URL + '/games/' + gameId + '/start', { method: 'POST' });
    } catch (error) { 
        alert("เกิดข้อผิดพลาดในการเริ่มเกม"); 
        if (btn) btn.style.display = 'inline-block';
    }
}'''
js = js.replace(old_startGame, new_startGame)


# rerollMarshal
old_reroll = '''async function rerollMarshal() {
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
}'''
new_reroll = '''async function rerollMarshal() {
    const btnWrapper = document.getElementById('draft-buttons-wrapper');
    if (btnWrapper) btnWrapper.style.display = 'none'; // Optimistic Update

    try {
        const playerId = localStorage.getItem('eternalClashPlayerId');
        const res = await fetch(API_BASE_URL + '/players/' + playerId + '/marshal-candidates/reroll', { method: 'POST' });
        if(res.ok) {
            const c = await res.json();
            renderDraftCard(c.marshal);
            if (btnWrapper) btnWrapper.style.display = 'flex';
        } else {
            alert("คุณสุ่มใหม่ครบจำนวนแล้ว!");
            if (btnWrapper) btnWrapper.style.display = 'flex';
        }
    } catch(e) {
        console.warn("เซิร์ฟเวอร์กำลังรีสตาร์ท กรุณารอสักครู่...");
        if (btnWrapper) btnWrapper.style.display = 'flex';
    }
}'''
js = js.replace(old_reroll, new_reroll)


# confirmMarshal
old_confirm = '''async function confirmMarshal() {
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
new_confirm = '''async function confirmMarshal() {
    const card = document.getElementById('marshal-single-card');
    const wrapper = document.getElementById('draft-buttons-wrapper');
    const waiting = document.getElementById('draft-waiting');
    
    // Optimistic Update
    if (card) card.style.display = 'none';
    if (wrapper) wrapper.style.display = 'none';
    if (waiting) {
        waiting.style.display = 'block';
        waiting.innerHTML = 'ส่งคำสั่งเลือกขุนพลแล้ว...<br>กำลังรอระบบยืนยัน...';
    }

    try {
        const playerId = localStorage.getItem('eternalClashPlayerId');
        const res = await fetch(API_BASE_URL + '/players/' + playerId + '/marshal-candidates/choose', { method: 'POST' });
        if(res.ok) {
            const m = await res.json();
            document.getElementById('ui-marshal').innerText = m.name;
            if (waiting) {
                waiting.innerHTML = 'ท่านเลือก <strong style="color:white">' + m.name + '</strong> แล้ว<br>กำลังรอผู้เล่นอื่น...';
            }
        } else {
            // Revert if failed
            if (card) card.style.display = 'block';
            if (wrapper) wrapper.style.display = 'flex';
            if (waiting) waiting.style.display = 'none';
            alert('เลือกขุนพลไม่สำเร็จ');
        }
    } catch(e) {
        console.warn("เซิร์ฟเวอร์กำลังรีสตาร์ท กรุณารอสักครู่...");
        if (card) card.style.display = 'block';
        if (wrapper) wrapper.style.display = 'flex';
        if (waiting) waiting.style.display = 'none';
    }
}'''
js = js.replace(old_confirm, new_confirm)


with open('code/src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)

print("All buttons optimistic UI added")
