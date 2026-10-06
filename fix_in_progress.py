import re

with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

if 'let lastSeason = null;' not in js:
    js = js.replace('let lastTurn = 0;', 'let lastTurn = 0;\nlet lastSeason = null;\nlet lastDaytime = null;')

old_block = '''    } else if (snapshot.status === 'IN_PROGRESS') {
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
    } else if (snapshot.status === 'FINISHED') {'''

new_block = '''    } else if (snapshot.status === 'IN_PROGRESS') {
        // ฟังก์ชั่นสำหรับอัปเดต UI ภายในเกม
        const doGameUpdate = () => {
            if (snapshot.currentTurn !== lastTurn) {
                mySubmitted = false;
                document.getElementById('command-panel').style.display = 'flex';
                
                if (lastTurn > 0) {
                    updateLog(snapshot);
                }
                lastTurn = snapshot.currentTurn;
            }
            lastSeason = snapshot.season;
            lastDaytime = snapshot.daytime;
            updateGameUI(snapshot);
        };

        if(lastStatus !== 'IN_PROGRESS') {
            lastStatus = 'IN_PROGRESS';
            playCloudTransition(() => {
                hideAllScreens();
                document.getElementById('game-screen').style.display = 'flex';
                
                // เปลี่ยนเพลงเป็นเพลงในเกม
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
        
        // ถ้าเป็นระหว่างเกม และมีการเปลี่ยนกลางวัน/กลางคืน หรือ ฤดู
        if (lastSeason !== null && lastDaytime !== null && 
            (lastSeason !== snapshot.season || lastDaytime !== snapshot.daytime)) {
            playCloudTransition(() => {
                doGameUpdate();
            });
            return;
        }
        
        // อัปเดตปกติ (ไม่มีเมฆ)
        doGameUpdate();

    } else if (snapshot.status === 'FINISHED') {'''

if "lastSeason !== null" not in js:
    js = js.replace(old_block, new_block)
    with open('code/src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
        f.write(js)

print("IN_PROGRESS block updated")
