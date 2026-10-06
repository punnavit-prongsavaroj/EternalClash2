import re

with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

# Update mapping just in case
old_map = "'โจโฉ': 'sungon.mp4',"
new_map = "'โจโฉ': 'josho.mp4',"
js = js.replace(old_map, new_map)

pattern = r"\} else if \(snapshot\.status === 'FINISHED'\) \{[\s\S]*?(?=// ----- DRAFT PHASE -----)"

new_finished = '''} else if (snapshot.status === 'FINISHED') {
        if (lastStatus !== 'FINISHED') {
            lastStatus = 'FINISHED';
            hideAllScreens();
            document.getElementById('game-over-screen').style.display = 'block'; // เปลี่ยนจาก flex เป็น block เพื่อให้ UI ข้างในจัดการกันเอง
            document.getElementById('win-ui').style.display = 'none'; // ซ่อน UI
            
            if(pollingInterval) clearInterval(pollingInterval);
            
            // ปิดเพลงฉาก
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

'''

js = re.sub(pattern, new_finished, js)

with open('code/src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)

print("Game Over video logic fixed for real")
