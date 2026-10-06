import re

with open('src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

# Replace handleSnapshot section for DRAFT
old_handle_draft = '''    if (snapshot.status === 'MARSHAL_SELECTION') {
        if(lastStatus !== 'MARSHAL_SELECTION') {
            lastStatus = 'MARSHAL_SELECTION';
            hideAllScreens();
            document.getElementById('draft-screen').style.display = 'flex';
            fetchCurrentDraft();
        }
    } else if'''

new_handle_draft = '''    if (snapshot.status === 'MARSHAL_SELECTION') {
        if(lastStatus !== 'MARSHAL_SELECTION') {
            lastStatus = 'MARSHAL_SELECTION';
            hideAllScreens();
            document.getElementById('draft-screen').style.display = 'flex';
        }
        updateDraftUI(snapshot);
    } else if'''

js = js.replace(old_handle_draft, new_handle_draft)

# Replace Draft functions
old_draft_funcs = '''// ----- DRAFT PHASE -----
async function fetchCurrentDraft() {
    const playerId = localStorage.getItem('eternalClashPlayerId');
    const res = await fetch(API_BASE_URL + '/players/' + playerId + '/marshal-candidates');
    if(!res.ok) return;
    const list = await res.json();
    if(list.length > 0) renderDraftCard(list[0].marshal);
}

async function rerollMarshal() {
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
        const buttons = document.querySelectorAll('#draft-screen button');
        buttons.forEach(btn => btn.style.display = 'none');
        document.getElementById('draft-waiting').style.display = 'block';
    }
}'''

new_draft_funcs = '''// ----- DRAFT PHASE -----
async function updateDraftUI(snapshot) {
    const playerId = parseInt(localStorage.getItem('eternalClashPlayerId'));
    const me = snapshot.players.find(p => p.playerId === playerId);
    
    // เรียงตาม ID หรือดูจากใครที่ยังไม่มี marshalName เป็นคนแรก
    const currentDraftPlayer = snapshot.players.find(p => !p.marshalName);
    
    const card = document.getElementById('marshal-single-card');
    const btnWrapper = document.getElementById('draft-buttons-wrapper');
    const waitMsg = document.getElementById('draft-waiting');
    
    if (me.marshalName) {
        card.style.display = 'none';
        btnWrapper.style.display = 'none';
        waitMsg.style.display = 'block';
        waitMsg.innerHTML = 'ท่านเลือก <strong style="color:white">' + me.marshalName + '</strong> แล้ว<br>กำลังรอผู้เล่นอื่น...';
        return;
    }
    
    if (currentDraftPlayer && currentDraftPlayer.playerId === playerId) {
        // ตาของฉัน!
        card.style.display = 'block';
        btnWrapper.style.display = 'flex';
        waitMsg.style.display = 'none';
        
        // โหลดข้อมูลถ้ายังไม่แสดง
        if (!card.innerHTML.includes('จุดเด่น')) {
            const res = await fetch(API_BASE_URL + '/players/' + playerId + '/marshal-candidates');
            if(res.ok) {
                const list = await res.json();
                if(list.length > 0) renderDraftCard(list[0].marshal);
            }
        }
    } else if (currentDraftPlayer) {
        // ตาคนอื่น
        card.style.display = 'none';
        btnWrapper.style.display = 'none';
        waitMsg.style.display = 'block';
        waitMsg.innerHTML = '<h3 style="color:#f39c12">รอผู้เล่น ' + currentDraftPlayer.name + ' เลือกแม่ทัพก่อน...</h3>';
    }
}

async function rerollMarshal() {
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

js = js.replace(old_draft_funcs, new_draft_funcs)

with open('src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)
