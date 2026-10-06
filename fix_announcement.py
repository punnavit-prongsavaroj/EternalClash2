import re

with open('code/src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

# 1. Add showEventAnnouncement function
announcement_func = '''
// --- ระบบประกาศ Event กลางหน้าจอ ---
function showEventAnnouncement(eventsArray) {
    if (!eventsArray || eventsArray.length === 0) return;
    
    const overlay = document.createElement('div');
    overlay.style.position = 'fixed';
    overlay.style.top = '50%';
    overlay.style.left = '50%';
    overlay.style.transform = 'translate(-50%, -50%) scale(1.5)';
    overlay.style.zIndex = '100000';
    overlay.style.pointerEvents = 'none';
    overlay.style.textAlign = 'center';
    overlay.style.textShadow = '0 5px 15px rgba(0,0,0,0.8), 0 0 20px #e74c3c';
    overlay.style.fontFamily = '"Kanit", sans-serif';
    overlay.style.opacity = '0';
    overlay.style.transition = 'all 0.5s cubic-bezier(0.25, 1.5, 0.5, 1)';
    
    let textHtml = "<h1 style='font-size: 5rem; margin: 0; color: #ff4757; font-weight: 900;'>⚠️ เกิดเหตุการณ์!</h1>";
    eventsArray.forEach(evName => {
        textHtml += "<div style='font-size: 3.5rem; color: #f1c40f; font-weight: bold; margin-top: 10px;'>" + evName + "</div>";
    });
    overlay.innerHTML = textHtml;
    
    document.body.appendChild(overlay);
    
    // Animate In (เด้งเข้ามากลางจอ)
    setTimeout(() => {
        overlay.style.opacity = '1';
        overlay.style.transform = 'translate(-50%, -50%) scale(1)';
    }, 50);
    
    // Animate Out (ค้างไว้ 3 วิ แล้วจางหายไป)
    setTimeout(() => {
        overlay.style.transition = 'all 0.5s ease-in';
        overlay.style.opacity = '0';
        overlay.style.transform = 'translate(-50%, -50%) scale(0.5)';
        setTimeout(() => { overlay.remove(); }, 500);
    }, 3000);
}
'''
if 'showEventAnnouncement' not in js:
    js = js.replace('function hideAllScreens()', announcement_func + '\nfunction hideAllScreens()')

# 2. Modify updateLog to collect event names and call showEventAnnouncement
old_event_loop = '''                const events = await resE.json();
                events.forEach(ev => {
                    // เล่นเสียงเฉพาะของแต่ละอีเวนต์'''
                    
new_event_loop = '''                const events = await resE.json();
                let eventNamesForAnnounce = [];
                events.forEach(ev => {
                    let evName = EVENT_THAI_NAMES[ev.eventType] || ev.eventType;
                    eventNamesForAnnounce.push(evName);
                    // เล่นเสียงเฉพาะของแต่ละอีเวนต์'''

if 'eventNamesForAnnounce = []' not in js:
    js = js.replace(old_event_loop, new_event_loop)

old_event_end = '''                        targetName = "กองทัพที่กำลังเดินทาง";
                    }
                    extraLines += <p class="event-text">⚠️ <strong></strong>:  
()</p>;
                });
            }'''
            
new_event_end = '''                        targetName = "กองทัพที่กำลังเดินทาง";
                    }
                    extraLines += <p class="event-text">⚠️ <strong></strong>:  
()</p>;
                });
                
                // โชว์ข้อความกลางจอ
                if (eventNamesForAnnounce.length > 0) {
                    showEventAnnouncement(eventNamesForAnnounce);
                }
            }'''

# Note: The exact spacing in old_event_end might vary, let's use regex
pattern_event_end = r'targetName = "กองทัพที่กำลังเดินทาง";\s*\}\s*extraLines \+\= <p class="event-text">.*?<\/p>;\s*\}\);\s*\}'

replacement_event_end = '''targetName = "กองทัพที่กำลังเดินทาง";
                    }
                    extraLines += <p class="event-text">⚠️ <strong></strong>:  ()</p>;
                });
                
                if (eventNamesForAnnounce.length > 0) {
                    showEventAnnouncement(eventNamesForAnnounce);
                }
            }'''

js = re.sub(pattern_event_end, replacement_event_end, js, flags=re.DOTALL)

with open('code/src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)

print("Event announcement added")
