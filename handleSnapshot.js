function handleSnapshot(snapshot) {
    if (snapshot.status === 'MARSHAL_SELECTION') {
        if(lastStatus !== 'MARSHAL_SELECTION') {
            lastStatus = 'MARSHAL_SELECTION';
            hideAllScreens();
            document.getElementById('draft-screen').style.display = 'flex';
        }
        updateDraftUI(snapshot);
    } else if (snapshot.status === 'IN_PROGRESS') {
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

    } else if (snapshot.status === 'FINISHED') {
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

// ----- DRAFT PHASE -----