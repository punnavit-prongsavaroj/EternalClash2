export const API_BASE_URL = '/api';

export const EVENT_THAI_NAMES = {
    'SINKHOLE': 'หลุมยุบ', 'SUN_GLARE': 'แสงแดดแสบตา', 'LIGHTNING': 'พายุฟ้าผ่า',
    'AVALANCHE': 'หิมะถล่ม', 'FOOD_SPOILAGE': 'อาหารเน่าเสีย', 'INSECT_DAMAGE': 'แมลงศัตรูพืชบุก',
    'FROSTBITE': 'อากาศหนาวจัด (Frostbite)', 'EPIDEMIC': 'โรคระบาด', 'SLOW': 'ติดพายุ (เดินทางล่าช้า)',
    'FLOOD': 'น้ำท่วมใหญ่', 'SUNBURN': 'แดดเผา', 'SNOW_COVER': 'พายุหิมะปกคลุม', 
    'REBELLION': 'กบฏชาวบ้านลุกฮือ', 'STARVATION': 'ขาดแคลนเสบียง (ทหารอดตาย)'
};

export const MARSHAL_WIN_VIDEOS = {
    'ขงเบ้ง': 'kongming.mp4', 'จูล่ง': 'julong.mp4', 'จิวยี่': 'jilyi.mp4',
    'โจโฉ': 'josho.mp4', 'เล่าปี่': 'laopi.mp4', 'ลิโป้': 'Lubu.mp4', 'ซุนกวน': 'songun.mp4'
};

export const MARSHAL_IMGS = {
    'ขงเบ้ง': 'kong-beng.jpg', 'จูล่ง': 'Ju-long.jpg', 'จิวยี่': 'Jilyi.jpg',
    'โจโฉ': 'Jo-Sho.jpg', 'เล่าปี่': 'lao-pi.jpg', 'ลิโป้': 'lu-bu.jpg', 'ซุนกวน': 'SunGuan.jpg'
};

export const PLAYER_COLORS = ['#e74c3c','#3498db','#2ecc71','#9b59b6','#e67e22','#1abc9c','#34495e'];

async function api(path, options = {}) {
    const res = await fetch(API_BASE_URL + path, options);
    const text = await res.text();
    if (!res.ok) {
        let msg = 'Request failed';
        try { if(text) { const j = JSON.parse(text); msg = j.message || msg; } } catch {}
        throw new Error(msg);
    }
    return text ? JSON.parse(text) : null;
}

export const createGame = () => api('/games', { method: 'POST' });
export const fetchGameByCode = (code) => api('/games/code/' + code);
export const joinGame = (gameId, name) => api('/games/' + gameId + '/players', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify({ name }) });
export const fetchPlayers = (gameId) => api('/games/' + gameId + '/players');
export const fetchGameSnapshot = (gameId, playerId) => api('/games/' + gameId + '/snapshot?viewerPlayerId=' + playerId);
export const startGame = (gameId) => api('/games/' + gameId + '/start', { method:'POST' });
export const submitPlacement = (gameId, playerId, cityId) => api('/games/' + gameId + '/placement?playerId=' + playerId + '&cityId=' + cityId, { method:'POST' });
export const getMarshalCandidates = (playerId) => api('/players/' + playerId + '/marshal-candidates');
export const rerollMarshal = (playerId) => api('/players/' + playerId + '/marshal-candidates/reroll', { method:'POST' });
export const confirmMarshal = (playerId) => api('/players/' + playerId + '/marshal-candidates/choose', { method:'POST' });
export const submitAction = (gameId, playerId, body) => api('/games/' + gameId + '/players/' + playerId + '/actions', { method:'POST', headers:{'Content-Type':'application/json'}, body: JSON.stringify(body) });
export const resolveTurn = (gameId) => fetch(API_BASE_URL + '/games/' + gameId + '/resolve-turn', { method:'POST' }).catch(() => {});
export const fetchEvents = (gameId, turnNumber) => api('/games/' + gameId + '/events?turnNumber=' + turnNumber);
export const fetchBattles = (gameId, turnNumber) => api('/games/' + gameId + '/battles?turnNumber=' + turnNumber);
