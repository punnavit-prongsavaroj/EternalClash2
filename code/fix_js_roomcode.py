import re

with open('src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

# Modify checkLoginState
js = js.replace('''    const savedGameId = localStorage.getItem("eternalClashGameId");
    
    if (savedGameId) {
        showRoom(savedGameId, savedName);''', 
'''    const savedGameId = localStorage.getItem("eternalClashGameId");
    const savedRoomCode = localStorage.getItem("eternalClashRoomCode") || savedGameId;
    
    if (savedGameId) {
        showRoom(savedGameId, savedRoomCode, savedName);''')

# Modify showRoom
js = js.replace('function showRoom(gameId, playerName) { hideAllScreens(); document.getElementById("room-screen").style.display = "flex"; document.getElementById("current-room-id").innerText = gameId; document.getElementById("current-player-name").innerText = playerName; }',
'function showRoom(gameId, roomCode, playerName) { hideAllScreens(); document.getElementById("room-screen").style.display = "flex"; document.getElementById("current-room-id").innerText = roomCode; document.getElementById("current-player-name").innerText = playerName; }')

# Modify joinGameApi
js = js.replace('async function joinGameApi(gameId) {', 'async function joinGameApi(gameId, roomCode) {')
js = js.replace('localStorage.setItem("eternalClashPlayerId", player.id);\n    showRoom(gameId, playerName);', 'localStorage.setItem("eternalClashPlayerId", player.id);\n    localStorage.setItem("eternalClashRoomCode", roomCode);\n    showRoom(gameId, roomCode, playerName);')

# Modify createRoom
js = js.replace('await joinGameApi(game.id);', 'await joinGameApi(game.id, game.roomCode);')

# Modify joinRoom
old_join = '''async function joinRoom() {
    const code = document.getElementById('room-code-input').value.trim();
    if(!code) { alert('กรุณากรอกรหัสห้อง'); return; }
    try {
        await joinGameApi(code);
        closeJoinPopup();
    } catch (error) { alert("ไม่พบห้องนี้ หรือเข้าห้องไม่สำเร็จ"); }
}'''

new_join = '''async function joinRoom() {
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
js = js.replace(old_join, new_join)

with open('src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)
