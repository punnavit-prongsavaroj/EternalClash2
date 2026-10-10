import { useState } from 'react'

export default function LoginScreen({ onLoggedIn }) {
    const [name, setName] = useState('');
    const [loading, setLoading] = useState(false);

    const handleJoin = () => {
        if (!name.trim()) { alert('ท่านขุนพล! โปรดระบุชื่อของท่านก่อนเข้าสู่สนามรบ'); return; }
        setLoading(true);
        onLoggedIn(name.trim());
    };

    return (
        <div id="login-screen" className="screen">
            <video autoPlay muted loop playsInline id="bg-video">
                <source src="/gif/1004.mp4" type="video/mp4" />
            </video>
            <h1 className="game-title">EternalClash2</h1>
            <div className="login-container">
                <p>กรุณาตั้งชื่อขุนพลของคุณ</p>
                <input type="text" placeholder="ใส่ชื่อของคุณที่นี่..." value={name} onChange={e => setName(e.target.value)} onKeyDown={e => e.key === 'Enter' && handleJoin()} disabled={loading} autoComplete="off" />
                <button onClick={handleJoin} disabled={loading || !name.trim()}>{loading ? 'กำลังเข้าสู่สนามรบ...' : 'เข้าสู่สนามรบ'}</button>
            </div>
        </div>
    );
}
