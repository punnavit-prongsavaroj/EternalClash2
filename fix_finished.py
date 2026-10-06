import re

with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

pattern = r"\} else if \(snapshot\.status === 'FINISHED'\) \{[\s\S]*?(?=\}\n\}\n// ----- GAME PHASE -----)"

new_finished = '''} else if (snapshot.status === 'FINISHED') {
        if (lastStatus !== 'FINISHED') {
            lastStatus = 'FINISHED';
            hideAllScreens();
            document.getElementById('game-over-screen').style.display = 'block'; // เปลี่ยนจาก flex เป็น block เพื่อให้ UI ข้างในจัดการกันเอง
            document.getElementById('win-ui').style.display = 'none'; // ซ่อน UI
            
            if(pollingInterval) clearInterval(pollingInterval);
            
            // ปิดเพลง
            const bgMusic = document.getElementById('bg-music');
            if (bgMusic) bgMusic.pause();
            
            const winner = snapshot.players.find(p => p.alive);
            if (winner) {
                document.getElementById('winner-name-display').innerText = winner.name;
                document.getElementById('winner-marshal-display').innerText = winner.marshalName || 'ไม่ได้เลือก';
                
                const imgName = MARSHAL_IMGS[winner.marshalName] || 'Jo-Sho.jpg';
                document.getElementById('winner-marshal-img').src = '/Marshal/' + imgName;
                document.getElementById('winner-marshal-img').style.display = 'block';
                
                // เล่นวิดีโอ
                const winVideo = document.getElementById('win-video');
                const videoFile = MARSHAL_WIN_VIDEOS[winner.marshalName];
                if (videoFile && winVideo) {
                    winVideo.src = '/SkillAction/win%20animation/' + videoFile;
                    winVideo.muted = false; // เปิดเสียงถ้าได้
                    
                    winVideo.onended = () => {
                        document.getElementById('win-ui').style.display = 'flex';
                    };
                    
                    winVideo.onerror = () => { document.getElementById('win-ui').style.display = 'flex'; };
                    
                    winVideo.play().catch(e => {
                        // ถ้าติด autoplay policy ให้ mute แล้วลองเพลย์ใหม่
                        winVideo.muted = true;
                        winVideo.play().then(() => {
                            // เล่นได้แล้วแบบไม่มีเสียง
                        }).catch(e2 => {
                            // ถ้ายังไม่ได้อีก ก็ข้ามวิดีโอไปเลย
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
    '''

js = re.sub(pattern, new_finished, js)

with open('code/src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)

print("Game Over video logic fixed")
