import re

with open('src/main/resources/static/js/app.js', 'r', encoding='utf-8') as f:
    js = f.read()

# Add EVENT_THAI_NAMES at the top of the file if not exists
if 'EVENT_THAI_NAMES' not in js:
    js = js.replace("const MARSHAL_IMGS = {", "const EVENT_THAI_NAMES = {\n    'SINKHOLE': 'หลุมยุบ',\n    'SUN_GLARE': 'แสงแดดแสบตา',\n    'LIGHTNING': 'พายุฟ้าผ่า',\n    'AVALANCHE': 'หิมะถล่ม',\n    'FOOD_SPOILAGE': 'อาหารเน่าเสีย',\n    'INSECT_DAMAGE': 'แมลงศัตรูพืชบุก',\n    'FROSTBITE': 'อากาศหนาวจัด (Frostbite)',\n    'EPIDEMIC': 'โรคระบาด',\n    'SLOW': 'ติดพายุ (เดินทางล่าช้า)',\n    'FLOOD': 'น้ำท่วมใหญ่',\n    'SUNBURN': 'แดดเผา',\n    'SNOW_COVER': 'พายุหิมะปกคลุม',\n    'REBELLION': 'กบฏชาวบ้านลุกฮือ'\n};\n\nconst MARSHAL_IMGS = {")

# Modify event parsing in updateLog
old_event_loop = '''                events.forEach(ev => {
                    extraLines += "<span style='color:#9b59b6'>⚡ อีเวนต์: " + (ev.description || "เกิดเหตุการณ์ลึกลับ") + "</span><br>";
                });'''

new_event_loop = '''                events.forEach(ev => {
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
                });'''

js = js.replace(old_event_loop, new_event_loop)

with open('src/main/resources/static/js/app.js', 'w', encoding='utf-8') as f:
    f.write(js)
