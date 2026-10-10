import { useState, useEffect } from 'react'
import { fetchPlayers, startGame } from '../api'
import ManualModal from './ManualModal'

export default function RoomScreen({ gameId, playerId, gameState, onLeave }) {
    const [players, setPlayers] = useState([]);
    const [loading, setLoading] = useState(false);
    const [showManual, setShowManual] = useState(false);

    useEffect(() => {
        if (!gameId) return;
        const load = async () => {
            try { const p = await fetchPlayers(gameId); setPlayers(p); } catch {}
        };
        load();
        const interval = setInterval(load, 2000);
        return () => clearInterval(interval);
    }, [gameId]);

    const handleStart = async () => {
        setLoading(true);
        try { await startGame(gameId); } catch (e) { alert(e.message); setLoading(false); }
    };

    const roomCode = localStorage.getItem('eternalClashRoomCode') || gameId;
    const isLeader = players.length > 0 && players[0].id === playerId;

    return (
        <div id="room-screen" className="screen">
            <div className="lobby-bg"></div>
            <button className="logout-btn" onClick={onLeave}>ออกจากห้อง</button>
            <div className="glass-panel" style={{padding:'40px', textAlign:'center', borderRadius:'12px', marginTop:'100px'}}>
                <h2 style={{color:'#f39c12', fontSize:'2rem', marginBottom:'20px'}}>ห้องรหัส: <span>{roomCode}</span></h2>
                <div style={{color:'white', textAlign:'left', marginBottom:'20px', fontSize:'1.1rem'}}>
                    <h3>ผู้เล่นในห้อง ({players.length} คน):</h3>
                    {players.map((p, index) => <div key={p.id}>- {p.name} {index === 0 && <span style={{color:'#f1c40f'}}>(หัวหน้าห้อง)</span>}</div>)}
                </div>
                {isLeader ? (
                    <button className="action-btn create-btn" onClick={handleStart} disabled={loading || players.length < 1}>
                        {loading ? 'กำลังเริ่ม...' : 'เริ่มเกม'}
                    </button>
                ) : (
                    <p style={{color:'#2ecc71', fontSize:'1.2rem', fontWeight:'bold', marginTop:'20px'}}>กำลังรอหัวหน้าห้องเริ่มเกม...</p>
                )}
            </div>

            <button className="action-btn" style={{position:'absolute', bottom:'20px', right:'20px', zIndex:15, background:'#2980b9', padding:'10px 20px', fontSize:'1.1rem', width:'auto', boxShadow:'0 5px 15px rgba(0,0,0,0.5)'}} onClick={() => setShowManual(v => !v)}>📖 คู่มือการเล่น</button>
            
            {showManual && <ManualModal onClose={() => setShowManual(false)} />}
        </div>
    );
}
