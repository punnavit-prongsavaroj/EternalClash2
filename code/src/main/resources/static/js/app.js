const API_BASE_URL = '/api';

const EVENT_THAI_NAMES = {
    'SINKHOLE': 'หลุมยุบ',
    'SUN_GLARE': 'แสงแดดแสบตา',
    'LIGHTNING': 'พายุฟ้าผ่า',
    'AVALANCHE': 'หิมะถล่ม',
    'FOOD_SPOILAGE': 'อาหารเน่าเสีย',
    'INSECT_DAMAGE': 'แมลงศัตรูพืชบุก',
    'FROSTBITE': 'อากาศหนาวจัด (Frostbite)',
    'EPIDEMIC': 'โรคระบาด',
    'SLOW': 'ติดพายุ (เดินทางล่าช้า)',
    'FLOOD': 'น้ำท่วมใหญ่',
    'SUNBURN': 'แดดเผา',
    'SNOW_COVER': 'พายุหิมะปกคลุม',
    'REBELLION': 'กบฏชาวบ้านลุกฮือ'
};

const MARSHAL_IMGS = {
    'ขงเบ้ง': 'kong-beng.jpg',
    'จูล่ง': 'Ju-long.jpg',
    'จิวยี่': 'Jilyi.jpg',
    'โจโฉ': 'Jo-Sho.jpg',
    'เล่าปี่': 'lao-pi.jpg',
    'ลิโป้': 'lu-bu.jpg',
    'ซุนกวน': 'SunGuan.jpg'
};

let pollingInterval = null;
let lastTurn = -1;
let lastStatus = '';
let mySubmitted = false;

document.addEventListener("DOMContentLoaded", () => {
    checkLoginState();
});

function startPolling() {
    if(pollingInterval) clearInterval(pollingInterval);
    pollingInterval = setInterval(fetchGameState, 2000);
}

function checkLoginState() {
    const savedName = localStorage.getItem("eternalClashPlayerName");
    const savedGameId = localStorage.getItem("eternalClashGameId");
    const savedRoomCode = localStorage.getItem("eternalClashRoomCode") || savedGameId;
    
    if (savedGameId) {
        showRoom(savedGameId, savedRoomCode, savedName);
        startPolling();
    } else if (savedName && savedName.trim() !== "") {
        showLobby(savedName);
    } else {
        showLogin();
    }
}

function joinLobby() {
    const nameInput = document.getElementById("player-name-input").value.trim();
    if (nameInput === "") {
        alert("ท่านขุนพล! โปรดระบุชื่อของท่านก่อนเข้าสู่สนามรบ");
        return;
    }
    localStorage.setItem("eternalClashPlayerName", nameInput);
    showLobby(nameInput);
}

function logout() {
    localStorage.removeItem("eternalClashPlayerName");
    localStorage.removeItem("eternalClashGameId");
    localStorage.removeItem("eternalClashPlayerId");
    if(pollingInterval) clearInterval(pollingInterval);
    document.getElementById("player-name-input").value = "";
    showLogin();
}

function leaveRoom() {
    localStorage.removeItem("eternalClashGameId");
    localStorage.removeItem("eternalClashPlayerId");
    if(pollingInterval) clearInterval(pollingInterval);
    checkLoginState();
}

function hideAllScreens() {
    document.getElementById("login-screen").style.display = "none";
    document.getElementById("lobby-screen").style.display = "none";
    document.getElementById("room-screen").style.display = "none";
    document.getElementById("draft-screen").style.display = "none";
    document.getElementById("game-screen").style.display = "none";
    document.getElementById("game-over-screen").style.display = "none";
}

function showLogin() { hideAllScreens(); document.getElementById("login-screen").style.display = "flex"; }
function showLobby(playerName) { hideAllScreens(); document.getElementById("lobby-screen").style.display = "flex"; document.getElementById("display-name").innerText = playerName; }
function showRoom(gameId, roomCode, playerName) { hideAllScreens(); document.getElementById("room-screen").style.display = "flex"; document.getElementById("current-room-id").innerText = roomCode; document.getElementById("current-player-name").innerText = playerName; }
function showJoinPopup() { document.getElementById('join-popup').style.display = 'flex'; }
function closeJoinPopup() { document.getElementById('join-popup').style.display = 'none'; document.getElementById('room-code-input').value = ''; }

async function createRoom() {
    try {
        const response = await fetch(API_BASE_URL + '/games', { method: 'POST' });
        if (!response.ok) throw new Error("สร้างห้องไม่สำเร็จ");
        const game = await response.json();
        await joinGameApi(game.id, game.roomCode);
    } catch (error) { alert(error.message); }
}

async function joinRoom() {
    const code = document.getElementById('room-code-input').value.trim();
    if(!code) { alert('กรุณากรอกรหัสห้อง'); return; }
    try {
        const res = await fetch(API_BASE_URL + '/games/code/' + code);
        if (!res.ok) throw new Error("ไม่พบห้องนี้");
        const game = await res.json();
        await joinGameApi(game.id, game.roomCode);
        closeJoinPopup();
    } catch (error) { alert("ไม่พบห้องนี้ หรือเข้าห้องไม่สำเร็จ"); }
}

async function joinGameApi(gameId, roomCode) {
    const playerName = localStorage.getItem("eternalClashPlayerName");
    const response = await fetch(API_BASE_URL + '/games/' + gameId + '/players', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name: playerName })
    });
    if (!response.ok) throw new Error("ไม่สามารถเข้าร่วมห้องได้");
    const player = await response.json();
    localStorage.setItem("eternalClashGameId", gameId);
    localStorage.setItem("eternalClashPlayerId", player.id);
    localStorage.setItem("eternalClashRoomCode", roomCode);
    showRoom(gameId, roomCode, playerName);
    startPolling();
}

async function fetchGameState() {
    const gameId = localStorage.getItem('eternalClashGameId');
    const playerId = localStorage.getItem('eternalClashPlayerId');
    if (!gameId || !playerId) return;

    try {
        // อัปเดตรายชื่อคนในห้องรอ (ถ้าอยู่หน้าห้อง)
        if(lastStatus === '' || lastStatus === 'WAITING') {
            const pRes = await fetch(API_BASE_URL + '/games/' + gameId + '/players');
            if (pRes.ok) {
                const players = await pRes.json();
                const listDiv = document.getElementById("room-players-list");
                listDiv.innerHTML = "<h3>ผู้เล่นในห้อง (" + players.length + " คน):</h3>";
                players.forEach(p => { listDiv.innerHTML += "<div>- " + p.name + "</div>"; });
            }
        }

        const res = await fetch(API_BASE_URL + '/games/' + gameId + '/snapshot?viewerPlayerId=' + playerId);
        if(!res.ok) return;
        const snapshot = await res.json();
        handleSnapshot(snapshot);
    } catch(e) {}
}

async function startGame() {
    const gameId = localStorage.getItem('eternalClashGameId');
    try {
        await fetch(API_BASE_URL + '/games/' + gameId + '/start', { method: 'POST' });
    } catch (error) { alert("เกิดข้อผิดพลาดในการเริ่มเกม"); }
}

function handleSnapshot(snapshot) {
    if (snapshot.status === 'MARSHAL_SELECTION') {
        if(lastStatus !== 'MARSHAL_SELECTION') {
            lastStatus = 'MARSHAL_SELECTION';
            hideAllScreens();
            document.getElementById('draft-screen').style.display = 'flex';
        }
        updateDraftUI(snapshot);
    } else if (snapshot.status === 'IN_PROGRESS') {
        if(lastStatus !== 'IN_PROGRESS') {
            lastStatus = 'IN_PROGRESS';
            hideAllScreens();
            document.getElementById('game-screen').style.display = 'flex';
        }
        
        if (snapshot.currentTurn !== lastTurn) {
            mySubmitted = false;
            document.getElementById('command-panel').style.display = 'flex';
            
            if (lastTurn > 0) {
                updateLog(snapshot);
            }
            lastTurn = snapshot.currentTurn;
        }
        updateGameUI(snapshot);
    } else if (snapshot.status === 'FINISHED') {
        if (lastStatus !== 'FINISHED') {
            lastStatus = 'FINISHED';
            hideAllScreens();
            document.getElementById('game-over-screen').style.display = 'flex';
            
            // ปิดเพลงหรือเปลี่ยนเพลงตอนจบ (ถ้าต้องการ)
            // document.getElementById('bg-music').pause();
            if(pollingInterval) clearInterval(pollingInterval);
            
            const winner = snapshot.players.find(p => p.alive);
            if (winner) {
                document.getElementById('winner-name-display').innerText = winner.name;
                document.getElementById('winner-marshal-display').innerText = winner.marshalName || 'ไม่ได้เลือก';
                
                const imgName = MARSHAL_IMGS[winner.marshalName] || 'Jo-Sho.jpg';
                document.getElementById('winner-marshal-img').src = '/Marshal/' + imgName;
                document.getElementById('winner-marshal-img').style.display = 'block';
            } else {
                document.getElementById('winner-name-display').innerText = 'ไม่มีผู้รอดชีวิต (เสมอ)';
                document.getElementById('winner-name-display').style.color = '#e74c3c';
                document.getElementById('winner-marshal-display').innerText = '-';
            }
        }
    }
}

// ----- DRAFT PHASE -----
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
}

function renderDraftCard(m) {
    const card = document.getElementById('marshal-single-card');
    const imgName = MARSHAL_IMGS[m.name] || 'Jo-Sho.jpg'; // fallback
    
    card.innerHTML = 
        '<img src="/Marshal/' + imgName + '" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; object-fit: cover; object-position: top; z-index: 1;">' +
        '<div style="position: absolute; top: 0; left: 0; width: 100%; padding: 25px 15px 40px; background: linear-gradient(to bottom, rgba(0,0,0,0.9) 10%, rgba(0,0,0,0.5) 60%, transparent); z-index: 2; box-sizing: border-box; text-align: center;">' +
        '</div>' +
        '<div style="position: absolute; bottom: 0; left: 0; width: 100%; padding: 50px 20px 20px; background: linear-gradient(to top, rgba(0,0,0,1) 10%, rgba(0,0,0,0.85) 60%, transparent); z-index: 2; box-sizing: border-box; text-align: center;">' +
            '<h3 style="font-size: 2.8rem; margin-bottom: 12px; color: #f39c12; text-shadow: 2px 2px 4px #000; letter-spacing: 2px;">' + m.name + '</h3>' +
            '<p style="font-size: 1.05rem; color: #2ecc71; text-shadow: 1px 1px 3px #000; margin-bottom: 6px;">✅ จุดเด่น: ' + (m.abilityDescription || 'ไม่มี') + '</p>' +
            '<p style="font-size: 1.05rem; color: #e74c3c; text-shadow: 1px 1px 3px #000; margin: 0;">⚠️ ข้อเสีย: ' + (m.disadvantageDescription || 'ไม่มี') + '</p>' +
        '</div>';
}

// ----- GAME PHASE -----
let lastActionMap = {};

function updateGameUI(snapshot) {
    // ฤดูกาล / กลางวันกลางคืน
    const bgMap = document.getElementById('dynamic-map-bg');
    let bgUrl = '';
    const seasonId = snapshot.season.toLowerCase();
    const isDay = snapshot.daytime;
    
    if(seasonId === 'summer') {
        bgUrl = isDay ? '/Map/sunny.jpg' : '/Map/Summer-night.jpg';
    } else if(seasonId === 'rainy') {
        bgUrl = isDay ? '/Map/rainy-day.jpg' : '/Map/rainy-night.jpg';
    } else if(seasonId === 'winter') {
        bgUrl = isDay ? '/Map/Snow-day.jpg' : '/Map/Snow-night.jpg';
    } else {
        bgUrl = '/Map/sunny.jpg';
    }
    bgMap.style.backgroundImage = 'url("' + bgUrl + '")';
    bgMap.style.filter = 'none';
    
    let seasonTh = seasonId === 'summer' ? 'ฤดูร้อน' : (seasonId === 'rainy' ? 'ฤดูฝน' : 'ฤดูหนาว');
    document.getElementById('current-turn-display').innerText = 'เทิร์นที่: ' + snapshot.currentTurn + ' (' + seasonTh + ' - ' + (isDay ? 'กลางวัน' : 'กลางคืน') + ')';
    
    // อัปเดตเมืองผู้เล่น
    const container = document.getElementById('castles-container');
    const targetSelect = document.getElementById('attack-target');
    container.innerHTML = '';
    targetSelect.innerHTML = '';
    
    // จำลองตำแหน่งบนแผนที่ คงที่ตาม ID ผู้เล่น
    const positions = [
        { x: 50, y: 80 }, { x: 20, y: 30 }, { x: 80, y: 40 }, { x: 20, y: 70 }, { x: 80, y: 70 }
    ];
    
    snapshot.players.filter(p => p.alive).forEach((player, index) => {
        if(player.isViewer) {
            document.getElementById('ui-marshal').innerText = player.marshalName || '-';
            document.getElementById('ui-city-name').innerText = player.name;
            document.getElementById('ui-food').innerText = player.food || 0;
            document.getElementById('ui-soldiers').innerText = player.citySoldiers || 0;
        }
        
        const pos = positions[index % positions.length];
        const node = document.createElement('div');
        node.className = 'castle-node' + (player.isViewer ? ' my-castle' : '');
        node.style.left = pos.x + '%';
        node.style.top = pos.y + '%';
        
        const dx = 50 - pos.x;
        const dy = 50 - pos.y;
        let angle = Math.atan2(dy, dx) * (180 / Math.PI);
        
        let radarHTML = '';
        let showRadar = true;
        if (!isDay && !player.isViewer) {
            showRadar = false;
        }
        if (showRadar) {
            // หา incoming armies ไปหา player นี้
            let armies = snapshot.visibleArmies.filter(a => a.visibleTargetPlayerId === player.playerId);
            let hasT1 = armies.some(a => (a.arrivalTurn - snapshot.currentTurn) === 1);
            let hasT2 = armies.some(a => (a.arrivalTurn - snapshot.currentTurn) === 2);
            // กรณีศัตรูประชิดถึงเทิร์นนี้เลย
            let hasT0 = armies.some(a => (a.arrivalTurn - snapshot.currentTurn) <= 0);
            if(hasT0) hasT1 = true;
            
            let dot1 = hasT1 ? '<div class="dot red"></div>' : '<div class="dot white"></div>';
            let dot2 = hasT2 ? '<div class="dot red"></div>' : '<div class="dot white"></div>';
            
            radarHTML = '<div class="radar-container" style="transform: translateY(-50%) rotate(' + angle + 'deg);">' + dot1 + dot2 + '</div>';
        }
        
        node.innerHTML = radarHTML +
            '<div class="castle-icon" style="pointer-events: auto;" onclick="if(!'+player.isViewer+') { document.getElementById(\'attack-target\').value=\''+player.playerId+'\'; openAttackModal(); }">🏰</div>' +
            '<div class="castle-name">' + player.name + '</div>';
                         
        container.appendChild(node);
        if (!player.isViewer) {
            targetSelect.innerHTML += '<option value="' + player.playerId + '">ตีเมือง: ' + player.name + '</option>';
        }
    });
}

async function submitAction(actionType) {
    const gameId = localStorage.getItem('eternalClashGameId');
    const playerId = localStorage.getItem('eternalClashPlayerId');
    let target = null;
    let soldiers = null;
    
    if (actionType === 'SEND_ARMY') {
        target = parseInt(document.getElementById('attack-target').value);
        soldiers = parseInt(document.getElementById('attack-soldiers').value);
    }
    
    try {
        const res = await fetch(API_BASE_URL + '/games/' + gameId + '/players/' + playerId + '/actions', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ actionType: actionType, targetPlayerId: target, soldierCount: soldiers })
        });
        
        if (!res.ok) {
            const err = await res.json();
            alert(err.message || 'คำสั่งล้มเหลว');
            return;
        }
        
        mySubmitted = true;
        document.getElementById('command-panel').style.display = 'none';
        
        // เราก็บอก Backend ให้ประมวลผลเลยถ้าทุกคนส่งครบ
        // (ปกติอาจจะให้ server จัดการเอง หรือลองเรียก resolve-turn)
        await fetch(API_BASE_URL + '/games/' + gameId + '/resolve-turn', { method: 'POST' });
    } catch(e) { alert(e.message); }
}

async function updateLog(snapshot) {
    const logContent = document.getElementById('log-content');
    if(logContent.innerHTML.includes('ยังไม่มีเหตุการณ์')) logContent.innerHTML = '';
    
    const gameId = localStorage.getItem('eternalClashGameId');
    const prevTurn = lastTurn - 1;
    
    let extraLines = '';
    if (prevTurn > 0) {
        try {
            const resE = await fetch(API_BASE_URL + '/games/' + gameId + '/events?turnNumber=' + prevTurn);
            if (resE.ok) {
                const events = await resE.json();
                events.forEach(ev => {
                    let evName = EVENT_THAI_NAMES[ev.eventType] || ev.eventType;
                    let targetName = "";
                    if (ev.affectedPlayerId) {
                        const p = snapshot.players.find(p => p.playerId === ev.affectedPlayerId);
                        targetName = p ? "เมืองของ " + p.name : "เมืองปริศนา";
                    } else if (ev.affectedArmyId) {
                        targetName = "กองทัพที่กำลังเดินทาง";
                    }
                    
                    let impactStr = [];
                    if (ev.foodImpact && ev.foodImpact !== 0) impactStr.push("เสบียง " + ev.foodImpact);
                    if (ev.soldierImpact && ev.soldierImpact !== 0) impactStr.push("ทหาร " + ev.soldierImpact);
                    if (ev.extraTravelTurns && ev.extraTravelTurns !== 0) impactStr.push("ดีเลย์ " + ev.extraTravelTurns + " เทิร์น");
                    
                    let detail = impactStr.length > 0 ? " (ผลกระทบ: " + impactStr.join(", ") + ")" : "";
                    
                    extraLines += "<span style='color:#9b59b6'>⚡ <b>อีเวนต์: [" + evName + "]</b> เกิดขึ้นที่ " + targetName + detail + "</span><br>";
                });
            }
            const resB = await fetch(API_BASE_URL + '/games/' + gameId + '/battles?turnNumber=' + prevTurn);
            if (resB.ok) {
                const battles = await resB.json();
                battles.forEach(b => {
                    const atk = snapshot.players.find(p => p.playerId === b.attackerPlayerId);
                    const def = snapshot.players.find(p => p.playerId === b.defenderPlayerId);
                    const atkName = atk ? atk.name : 'ศัตรู';
                    const defName = def ? def.name : 'ศัตรู';
                    
                    extraLines += "⚔️ <b style='color:#e74c3c'>" + atkName + " ปะทะ " + defName + "</b> (สูญเสีย: รุก " + b.attackerCasualties + ", รับ " + b.defenderCasualties + ")<br>";
                    if (b.cityDestroyed) {
                        extraLines += "💥 <b style='color:#c0392b; font-size:1.1rem;'>เมืองของ " + defName + " แตกพ่าย!</b><br>";
                    }
                });
            }
        } catch(e) {}
    }
    
    let actionLines = '';
    snapshot.visibleActions.forEach(act => {
        let actStr = act.actionType;
        if(actStr === 'PRODUCE_FOOD') actStr = '<span style="color:#2ecc71">ทำฟาร์ม</span>';
        else if(actStr === 'RECRUIT_SOLDIERS') actStr = '<span style="color:#3498db">เกณฑ์ทหาร</span>';
        else if(actStr === 'SEND_ARMY') actStr = '<span style="color:#e74c3c">ส่งกองทัพโจมตี</span>';
        else actStr = 'พักผ่อน';
        
        const p = snapshot.players.find(p => p.playerId === act.playerId);
        const name = p ? p.name : 'ศัตรู';
        actionLines += "🏰 " + name + " เลือก: " + actStr + "<br>";
    });
    
    if(actionLines === '' && extraLines === '') actionLines = '<i>ไม่มีความเคลื่อนไหวที่มองเห็นได้</i><br>';
    
    const newLog = "<div style='margin-bottom: 10px; line-height: 1.5;'>" +
                   "<strong style='color:#f39c12'>[สรุปเหตุการณ์ เทิร์น " + prevTurn + "]</strong><br>" +
                   actionLines + extraLines +
                   "</div><hr style='border-color: #7f8c8d; margin: 10px 0;'>";
                   
    logContent.innerHTML = newLog + logContent.innerHTML;
    document.getElementById('log-panel').style.display = 'flex';
}

function toggleLog() {
    const panel = document.getElementById('log-panel');
    panel.style.display = (panel.style.display === 'none' || panel.style.display === '') ? 'flex' : 'none';
}

function openAttackModal() { document.getElementById('attack-modal').style.display = 'flex'; }
function closeAttackModal() { document.getElementById('attack-modal').style.display = 'none'; }
function confirmAttack() { closeAttackModal(); submitAction('SEND_ARMY'); }
function returnToLobby() {
    localStorage.removeItem("eternalClashGameId");
    localStorage.removeItem("eternalClashPlayerId");
    if(pollingInterval) clearInterval(pollingInterval);
    checkLoginState();
}


// --- ระบบเพลง BGM ---
let isMusicPlaying = false;
function toggleMusic() {
    const bgMusic = document.getElementById('bg-music');
    const toggleBtn = document.getElementById('music-toggle');
    if (isMusicPlaying) {
        bgMusic.pause();
        toggleBtn.innerText = '🔇';
        isMusicPlaying = false;
    } else {
        let playPromise = bgMusic.play();
        if (playPromise !== undefined) {
            playPromise.then(_ => {
                toggleBtn.innerText = '🔊';
                isMusicPlaying = true;
            }).catch(error => {
                console.warn("เบราว์เซอร์บล็อกการเล่นเพลงอัตโนมัติ");
            });
        }
    }
}
