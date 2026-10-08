import { useState } from 'react'
import { createGame, fetchGameByCode, joinGame } from '../api'

export default function LobbyScreen({ playerName, onJoined, onLogout }) {
    const [showJoinPopup, setShowJoinPopup] = useState(false);
    const [roomCode, setRoomCode] = useState('');
    const [loading, setLoading] = useState(false);

    const handleCreate = async () => {
        setLoading(true);
        try {
            const game = await createGame();
            const player = await joinGame(game.id, playerName);
            localStorage.setItem('eternalClashRoomCode', game.roomCode || game.id);
            onJoined(game.id, player.id, playerName);
        } catch (e) { alert(e.message); setLoading(false); }
    };

    const handleJoin = async () => {
        if (!roomCode.trim()) { alert('กรุณากรอกรหัสห้อง'); return; }
        setLoading(true);
        try {
            const game = await fetchGameByCode(roomCode.trim());
            const player = await joinGame(game.id, playerName);
            localStorage.setItem('eternalClashRoomCode', game.roomCode || game.id);
            onJoined(game.id, player.id, playerName);
        } catch (e) { alert('ไม่พบห้องนี้ หรือเข้าห้องไม่สำเร็จ'); setLoading(false); }
    };

    return (
        <div id="lobby-screen" className="screen">
            <div className="lobby-bg"></div>
            <div className="lobby-character-container">
                <div className="half-screen left-half"><img src="/gif/jocho.png" className="character-img" alt="Jocho" /></div>
                <div className="half-screen right-half"><img src="/gif/kongming.png" className="character-img" alt="Kongming" /></div>
            </div>
            <button className="logout-btn" onClick={onLogout}>ออกจากระบบ</button>
            <div className="player-name-display">ท่านขุนพล: <span className="highlight">{playerName}</span></div>
            <div className="bottom-actions">
                <button className="action-btn create-btn" onClick={handleCreate} disabled={loading}>สร้างห้อง</button>
                <button className="action-btn join-btn" onClick={() => setShowJoinPopup(true)} disabled={loading}>เข้าร่วมห้อง</button>
            </div>
            {showJoinPopup && (
                <div className="popup-overlay" style={{display:'flex'}}>
                    <div className="popup-box glass-panel">
                        <h2 style={{color:'#f39c12', marginBottom:'20px'}}>เข้าร่วมห้อง</h2>
                        <input type="text" value={roomCode} onChange={e => setRoomCode(e.target.value)} placeholder="กรอกรหัสห้อง..." autoComplete="off" />
                        <div className="popup-buttons">
                            <button className="action-btn join-btn" onClick={handleJoin} disabled={loading}>ยืนยัน</button>
                            <button className="action-btn cancel-btn" onClick={() => setShowJoinPopup(false)}>ยกเลิก</button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
