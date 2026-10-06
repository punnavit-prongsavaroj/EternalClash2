import re

with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

old_create = '''async function createRoom() {
    try {
        const response = await fetch(API_BASE_URL + '/games', { method: 'POST' });
        if (!response.ok) throw new Error("สร้างห้องไม่สำเร็จ");
        const game = await response.json();
        await joinGameApi(game.id, game.roomCode);
    } catch (error) { alert(error.message); }
}'''
new_create = '''async function createRoom() {
    const btn = document.querySelector('button[onclick="createRoom()"]');
    const oldText = btn ? btn.innerText : '';
    if (btn) { btn.innerText = 'กำลังสร้าง...'; btn.disabled = true; }
    try {
        const response = await fetch(API_BASE_URL + '/games', { method: 'POST' });
        if (!response.ok) throw new Error("สร้างห้องไม่สำเร็จ");
        const game = await response.json();
        await joinGameApi(game.id, game.roomCode);
    } catch (error) { 
        alert(error.message); 
        if (btn) { btn.innerText = oldText; btn.disabled = false; }
    }
}'''
js = js.replace(old_create, new_create)

old_join = '''async function joinRoom() {
    const code = document.getElementById('room-code-input').value.trim();
    if(!code) { alert('กรุณากรอกรหัสห้อง'); return; }
    try {
        const res = await fetch(API_BASE_URL + '/games/code/' + code);
        if (!res.ok) throw new Error("ไม่พบห้องนี้");
        const game = await res.json();
        await joinGameApi(game.id, game.roomCode);
        closeJoinPopup();
    } catch (error) { alert("ไม่พบห้องนี้ หรือเข้าห้องไม่สำเร็จ"); }
}'''
new_join = '''async function joinRoom() {
    const code = document.getElementById('room-code-input').value.trim();
    if(!code) { alert('กรุณากรอกรหัสห้อง'); return; }
    const btn = document.querySelector('button[onclick="joinRoom()"]');
    const oldText = btn ? btn.innerText : '';
    if (btn) { btn.innerText = 'กำลังเข้า...'; btn.disabled = true; }
    try {
        const res = await fetch(API_BASE_URL + '/games/code/' + code);
        if (!res.ok) throw new Error("ไม่พบห้องนี้");
        const game = await res.json();
        await joinGameApi(game.id, game.roomCode);
        closeJoinPopup();
    } catch (error) { 
        alert("ไม่พบห้องนี้ หรือเข้าห้องไม่สำเร็จ"); 
        if (btn) { btn.innerText = oldText; btn.disabled = false; }
    }
}'''
js = js.replace(old_join, new_join)

with open('code/src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)

print("Create/Join buttons optimistic UI added")
