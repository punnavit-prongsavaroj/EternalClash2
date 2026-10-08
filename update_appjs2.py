import re

with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()
    
start_idx = js.find('function handleSnapshot(snapshot)')
end_idx = js.find('async function updateLog(snapshot)')

old_block = js[start_idx:end_idx]

new_block = '''function handleSnapshot(snapshot) {
    if (snapshot.status === 'MARSHAL_SELECTION') {
        if(lastStatus !== 'MARSHAL_SELECTION') {
            lastStatus = 'MARSHAL_SELECTION';
            hideAllScreens();
            document.getElementById('draft-screen').style.display = 'flex';
        }
        updateDraftUI(snapshot);
    } else if (snapshot.status === 'PLACEMENT') {
        if (lastStatus !== 'PLACEMENT') {
            lastStatus = 'PLACEMENT';
            playCloudTransition(() => {
                hideAllScreens();
                document.getElementById('game-screen').style.display = 'flex';
                document.getElementById('placement-panel').style.display = 'flex';
                document.getElementById('command-panel').style.display = 'none';
                
                const bgMusic = document.getElementById('bg-music');
                if (bgMusic && bgMusic.src && bgMusic.src.includes('menu_bgm.mp3')) {
                    bgMusic.src = '/sound/game_bgm.mp3';
                    if (typeof isMusicPlaying !== 'undefined' && isMusicPlaying) {
                        bgMusic.play().catch(e => console.log(e));
                    }
                }
                updateGameUI(snapshot);
            });
        } else {
            updateGameUI(snapshot);
        }
    } else if (snapshot.status === 'IN_PROGRESS') {
        const doGameUpdate = () => {
            if (snapshot.currentTurn !== lastTurn) {
                mySubmitted = false;
                document.getElementById('command-panel').style.display = 'flex';
                const pp = document.getElementById('placement-panel');
                if (pp) pp.style.display = 'none';
                
                if (lastTurn > 0) {
                    updateLog(snapshot);
                }
                lastTurn = snapshot.currentTurn;
            }
            updateGameUI(snapshot);
        };

        if (lastStatus !== 'IN_PROGRESS') {
            lastStatus = 'IN_PROGRESS';
            playCloudTransition(() => {
                hideAllScreens();
                document.getElementById('game-screen').style.display = 'flex';
                const pp = document.getElementById('placement-panel');
                if (pp) pp.style.display = 'none';
                document.getElementById('command-panel').style.display = 'flex';
                
                const bgMusic = document.getElementById('bg-music');
                if (bgMusic && bgMusic.src && bgMusic.src.includes('menu_bgm.mp3')) {
                    bgMusic.src = '/sound/game_bgm.mp3';
                    if (typeof isMusicPlaying !== 'undefined' && isMusicPlaying) {
                        bgMusic.play().catch(e => console.log(e));
                    }
                }
                
                doGameUpdate();
            });
            return;
        }
        
        if (lastTurn > 0 && snapshot.currentTurn !== lastTurn) {
            playCloudTransition(() => {
                doGameUpdate();
            });
            return;
        }
        
        doGameUpdate();

    } else if (snapshot.status === 'FINISHED') {
        if (lastStatus !== 'FINISHED') {
            lastStatus = 'FINISHED';
            hideAllScreens();
            document.getElementById('game-over-screen').style.display = 'block';
            document.getElementById('win-ui').style.display = 'none';
            
            if(pollingInterval) clearInterval(pollingInterval);
            
            const bgMusic = document.getElementById('bg-music');
            if (bgMusic) bgMusic.pause();
            
            const winner = snapshot.players.find(p => p.alive);
            if (winner) {
                document.getElementById('winner-name-display').innerText = winner.name;
                document.getElementById('winner-marshal-display').innerText = winner.marshalName || 'ไม่ได้เลือก';
                
                const winVideo = document.getElementById('win-video');
                const videoFile = MARSHAL_WIN_VIDEOS[winner.marshalName];
                if (videoFile && winVideo) {
                    winVideo.src = encodeURI('/SkillAction/win animation/' + videoFile);
                    winVideo.load();
                    
                    const fallbackTimeout = setTimeout(() => {
                        if (winVideo.currentTime === 0) {
                            document.getElementById('win-ui').style.display = 'flex';
                        }
                    }, 2000);

                    winVideo.onended = () => {
                        clearTimeout(fallbackTimeout);
                        document.getElementById('win-ui').style.display = 'flex';
                    };
                    
                    winVideo.onerror = () => { 
                        clearTimeout(fallbackTimeout);
                        document.getElementById('win-ui').style.display = 'flex'; 
                    };
                    
                    winVideo.muted = !(typeof isMusicPlaying !== 'undefined' && isMusicPlaying);
                    
                    winVideo.play().then(() => {
                        clearTimeout(fallbackTimeout);
                    }).catch(e => {
                        winVideo.muted = true;
                        winVideo.play().then(() => {
                            clearTimeout(fallbackTimeout);
                        }).catch(e2 => {
                            clearTimeout(fallbackTimeout);
                            document.getElementById('win-ui').style.display = 'flex';
                        });
                    });
                } else {
                    document.getElementById('win-ui').style.display = 'flex';
                }
            } else {
                document.getElementById('winner-name-display').innerText = 'ไม่มีผู้รอดชีวิต (เสมอ)';
                document.getElementById('winner-name-display').style.color = '#e74c3c';
                document.getElementById('winner-marshal-display').innerText = '-';
                document.getElementById('win-ui').style.display = 'flex';
            }
        }
    }
}

// ----- DRAFT PHASE -----
async function updateDraftUI(snapshot) {
    const playerId = parseInt(localStorage.getItem('eternalClashPlayerId'));
    const me = snapshot.players.find(p => p.playerId === playerId);
    
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
        card.style.display = 'block';
        btnWrapper.style.display = 'flex';
        waitMsg.style.display = 'none';
        
        if (!card.innerHTML.includes('จุดเด่น')) {
            const res = await fetch(API_BASE_URL + '/players/' + playerId + '/marshal-candidates');
            if(res.ok) {
                const list = await res.json();
                if(list.length > 0) renderDraftCard(list[0].marshal);
            }
        }
    } else if (currentDraftPlayer) {
        card.style.display = 'none';
        btnWrapper.style.display = 'none';
        waitMsg.style.display = 'block';
        waitMsg.innerHTML = '<h3 style="color:#f39c12">รอผู้เล่น ' + currentDraftPlayer.name + ' เลือกแม่ทัพก่อน...</h3>';
    }
}

async function rerollMarshal() {
    const btnWrapper = document.getElementById('draft-buttons-wrapper');
    if (btnWrapper) btnWrapper.style.display = 'none';

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
}

async function confirmMarshal() {
    const card = document.getElementById('marshal-single-card');
    const wrapper = document.getElementById('draft-buttons-wrapper');
    const waiting = document.getElementById('draft-waiting');
    
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
}

function renderDraftCard(m) {
    const card = document.getElementById('marshal-single-card');
    const imgName = MARSHAL_IMGS[m.name] || 'Jo-Sho.jpg';
    
    card.innerHTML = 
        '<img src="/Marshal/' + imgName + '" style="position: absolute; top: 0; left: 0; width: 100%; height: 100%; object-fit: cover; object-position: top; z-index: 1;">' +
        '<div style="position: absolute; top: 0; left: 0; width: 100%; padding: 25px 15px 40px; background: linear-gradient(to bottom, rgba(0,0,0,0.9) 10%, rgba(0,0,0,0.5) 60%, transparent); z-index: 2; box-sizing: border-box; text-align: center;"></div>' +
        '<div style="position: absolute; bottom: 0; left: 0; width: 100%; padding: 50px 20px 20px; background: linear-gradient(to top, rgba(0,0,0,1) 10%, rgba(0,0,0,0.85) 60%, transparent); z-index: 2; box-sizing: border-box; text-align: center;">' +
            '<h3 style="font-size: 2.8rem; margin-bottom: 12px; color: #f39c12; text-shadow: 2px 2px 4px #000; letter-spacing: 2px;">' + m.name + '</h3>' +
            '<p style="font-size: 1.05rem; color: #2ecc71; text-shadow: 1px 1px 3px #000; margin-bottom: 6px;">✅ จุดเด่น: ' + (m.abilityDescription || 'ไม่มี') + '</p>' +
            '<p style="font-size: 1.05rem; color: #e74c3c; text-shadow: 1px 1px 3px #000; margin: 0;">⚠️ ข้อเสีย: ' + (m.disadvantageDescription || 'ไม่มี') + '</p>' +
        '</div>';
}

// ----- GAME PHASE -----
let lastActionMap = {};
let selectedCityIdForAction = null; // Store which city is currently selected for taking an action

function updateGameUI(snapshot) {
    const bgMap = document.getElementById('dynamic-map-bg');
    let bgUrl = '';
    const seasonId = snapshot.season ? snapshot.season.toLowerCase() : 'summer';
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
    
    // Update player top bar stats (aggregate of all their cities)
    const myPlayerId = parseInt(localStorage.getItem('eternalClashPlayerId'));
    const me = snapshot.players.find(p => p.playerId === myPlayerId);
    if (me) {
        document.getElementById('ui-marshal').innerText = me.marshalName || '-';
        document.getElementById('ui-city-name').innerText = me.name;
        
        let totalFood = 0;
        let totalSoldiers = 0;
        let myNodes = snapshot.nodes.filter(n => n.ownerId === myPlayerId);
        myNodes.forEach(n => {
            if (n.food != null) totalFood += n.food;
            if (n.soldiers != null) totalSoldiers += n.soldiers;
        });
        
        document.getElementById('ui-food').innerText = totalFood;
        document.getElementById('ui-soldiers').innerText = totalSoldiers;
        
        if (snapshot.status === 'IN_PROGRESS') {
            if (selectedCityIdForAction) {
                const selectedNode = snapshot.nodes.find(n => n.nodeId === selectedCityIdForAction);
                if (!selectedNode || selectedNode.actionUsedThisTurn || selectedNode.ownerId !== myPlayerId) {
                    selectedCityIdForAction = null;
                    document.getElementById('command-panel').style.display = 'none';
                } else {
                    document.getElementById('command-panel').style.display = 'flex';
                }
            } else {
                document.getElementById('command-panel').style.display = 'none';
            }
        }
    }
    
    // Render Edges First
    const edgesContainer = document.getElementById('edges-container');
    if (edgesContainer) {
        edgesContainer.innerHTML = '';
        snapshot.edges.forEach(edge => {
            const n1 = snapshot.nodes.find(n => n.nodeId === edge.node1Id);
            const n2 = snapshot.nodes.find(n => n.nodeId === edge.node2Id);
            
            if (n1 && n2) {
                const line = document.createElementNS('http://www.w3.org/2000/svg', 'line');
                line.setAttribute('x1', n1.x + '%');
                line.setAttribute('y1', n1.y + '%');
                line.setAttribute('x2', n2.x + '%');
                line.setAttribute('y2', n2.y + '%');
                line.setAttribute('stroke', 'rgba(255, 255, 255, 0.4)');
                line.setAttribute('stroke-width', '4');
                line.setAttribute('stroke-dasharray', '8 8');
                edgesContainer.appendChild(line);
            }
        });
    }
    
    // Render Nodes
    const container = document.getElementById('castles-container');
    const targetSelect = document.getElementById('attack-target');
    container.innerHTML = '';
    targetSelect.innerHTML = '';
    
    snapshot.nodes.forEach(node => {
        const isMine = node.ownerId === myPlayerId;
        const owner = snapshot.players.find(p => p.playerId === node.ownerId);
        
        const nodeDiv = document.createElement('div');
        nodeDiv.className = 'castle-node' + (isMine ? ' my-castle' : '');
        nodeDiv.style.left = node.x + '%';
        nodeDiv.style.top = node.y + '%';
        
        if (node.actionUsedThisTurn) {
            nodeDiv.style.opacity = '0.5';
            nodeDiv.style.filter = 'grayscale(50%)';
        }
        
        let nodeIcon = owner ? '🏰' : '⚪';
        let nodeName = owner ? owner.name : 'ป่าเถื่อน';
        
        let clickHandler = '';
        if (snapshot.status === 'PLACEMENT') {
            if (!owner) {
                clickHandler = `submitPlacement(${node.nodeId})`;
                nodeDiv.style.cursor = 'pointer';
            }
        } else if (snapshot.status === 'IN_PROGRESS') {
            if (isMine && !node.actionUsedThisTurn) {
                clickHandler = `selectCityForAction(${node.nodeId})`;
                nodeDiv.style.cursor = 'pointer';
                if (selectedCityIdForAction === node.nodeId) {
                    nodeDiv.style.boxShadow = '0 0 20px 10px #f1c40f';
                    nodeDiv.style.borderRadius = '50%';
                }
            } else if (!isMine) {
                clickHandler = `openAttackModalForTarget(${node.nodeId})`;
                nodeDiv.style.cursor = 'crosshair';
            }
        }
        
        let radarHTML = '';
        if (isDay || isMine) {
            let armies = snapshot.visibleArmies.filter(a => a.visibleTargetCityId === node.nodeId);
            let hasT0 = armies.some(a => (a.arrivalTurn - snapshot.currentTurn) <= 0);
            let hasT1 = armies.some(a => (a.arrivalTurn - snapshot.currentTurn) === 1);
            
            if (hasT0 || hasT1) {
                let dot1 = hasT0 ? '<div class="dot red"></div>' : '<div class="dot white"></div>';
                let dot2 = hasT1 ? '<div class="dot red"></div>' : '<div class="dot white"></div>';
                radarHTML = `<div class="radar-container" style="transform: translateY(-50%);">${dot1}${dot2}</div>`;
            }
        }
        
        let badgeHTML = '';
        if (node.soldiers != null) {
            badgeHTML = `<div style="position:absolute; top:-10px; right:-10px; background:red; color:white; font-size:0.8rem; font-weight:bold; padding:2px 6px; border-radius:10px; border:2px solid white; pointer-events:none; z-index:10;">${node.soldiers} ⚔️</div>`;
        }
        
        nodeDiv.innerHTML = `
            ${radarHTML}
            ${badgeHTML}
            <div class="castle-icon" style="pointer-events: auto;" onclick="${clickHandler}">${nodeIcon}</div>
            <div class="castle-name">${nodeName}</div>
        `;
                         
        container.appendChild(nodeDiv);
        
        if (snapshot.status === 'IN_PROGRESS') {
            if (node.nodeId !== selectedCityIdForAction) {
                targetSelect.innerHTML += `<option value="${node.nodeId}">${nodeIcon} ${nodeName}</option>`;
            }
        }
    });
}

async function submitPlacement(cityId) {
    const gameId = localStorage.getItem('eternalClashGameId');
    const playerId = localStorage.getItem('eternalClashPlayerId');
    
    const waitMsg = document.getElementById('placement-waiting-msg');
    if (waitMsg) waitMsg.style.display = 'block';
    
    try {
        await fetch(API_BASE_URL + '/games/' + gameId + '/placement?playerId=' + playerId + '&cityId=' + cityId, { method: 'POST' });
    } catch(e) { 
        alert(e.message); 
        if (waitMsg) waitMsg.style.display = 'none';
    }
}

function selectCityForAction(cityId) {
    selectedCityIdForAction = cityId;
    fetchGameState(); 
}

function openAttackModalForTarget(targetNodeId) {
    if (!selectedCityIdForAction) {
        alert("กรุณาคลิกเลือกเมืองของท่านก่อนที่จะสั่งโจมตีเมืองอื่น");
        return;
    }
    document.getElementById('attack-target').value = targetNodeId;
    openAttackModal();
}

async function submitAction(actionType) {
    const gameId = localStorage.getItem('eternalClashGameId');
    const playerId = localStorage.getItem('eternalClashPlayerId');
    let target = null;
    let soldiers = null;
    
    if (!selectedCityIdForAction) {
        alert("กรุณาเลือกเมืองก่อนออกคำสั่ง!");
        return;
    }
    
    if (actionType === 'SEND_ARMY') {
        target = parseInt(document.getElementById('attack-target').value);
        soldiers = parseInt(document.getElementById('attack-soldiers').value);
    }
    
    document.getElementById('command-panel').style.display = 'none';

    try {
        const res = await fetch(API_BASE_URL + '/games/' + gameId + '/players/' + playerId + '/actions', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ cityId: selectedCityIdForAction, actionType: actionType, targetCityId: target, soldierCount: soldiers })
        });
        
        if (!res.ok) {
            const err = await res.json();
            alert(err.message || 'คำสั่งล้มเหลว');
            document.getElementById('command-panel').style.display = 'flex';
            return;
        }
        
        selectedCityIdForAction = null; 
        
        await fetch(API_BASE_URL + '/games/' + gameId + '/resolve-turn', { method: 'POST' });
    } catch(e) { alert(e.message); }
}
'''

js = js.replace(old_block, new_block)

with open('code/src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)
