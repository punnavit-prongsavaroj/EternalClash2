import re

with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

# Pattern to find the whole IN_PROGRESS block
pattern = r"\} else if \(snapshot\.status === 'IN_PROGRESS'\) \{.*?(?=\} else if \(snapshot\.status === 'FINISHED'\))"

new_block = '''} else if (snapshot.status === 'IN_PROGRESS') {
        const doGameUpdate = () => {
            if (snapshot.currentTurn !== lastTurn) {
                mySubmitted = false;
                document.getElementById('command-panel').style.display = 'flex';
                
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
        
        // ถ้าเป็นการเปลี่ยนเทิร์นใหม่ ให้เรียกเมฆเสมอ!
        if (lastTurn > 0 && snapshot.currentTurn !== lastTurn) {
            playCloudTransition(() => {
                doGameUpdate();
            });
            return;
        }
        
        // อัปเดตปกติเมื่ออยู่ในเทิร์นเดิม
        doGameUpdate();

    '''

js = re.sub(pattern, new_block, js, flags=re.DOTALL)

with open('code/src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)

print("Turn-based cloud transition updated")
