import re

with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

# Update mapping
old_map = "'โจโฉ': 'sungon.mp4',"
new_map = "'โจโฉ': 'josho.mp4',"
js = js.replace(old_map, new_map)

# Fix video play logic
old_play = '''winVideo.src = '/SkillAction/win%20animation/' + videoFile;
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
                    });'''

new_play = '''// ใช้ URL ที่ถูกต้อง (Browser จะจัดการเรื่องเว้นวรรคเองถ้าใช้ encodeURI)
                    winVideo.src = encodeURI('/SkillAction/win animation/' + videoFile);
                    winVideo.load(); // บังคับให้โหลดข้อมูลใหม่
                    
                    // ตั้งเวลาเผื่อวิดีโอค้างหรือไม่ยอมเล่น (เช่น 404 หรือโหลดนานเกินไป)
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
                    
                    // ตั้งค่าเสียง
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
                    });'''

js = js.replace(old_play, new_play)

with open('code/src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)

print("Video logic improved")
