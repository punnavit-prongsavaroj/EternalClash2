import React, { useEffect, useRef, useState } from 'react'
import { MARSHAL_WIN_VIDEOS } from '../api'

export default function GameOverScreen({ gameState, onReturnToLobby, onLogout }) {
    const videoRef = useRef(null);
    const [showUI, setShowUI] = useState(false);

    const winner = gameState?.players?.find(p => p.alive || p.isAlive);
    
    useEffect(() => {
        const videoFile = winner?.marshalName && MARSHAL_WIN_VIDEOS[winner.marshalName];
        const video = videoRef.current;
        if (videoFile && video) {
            video.src = encodeURI('/SkillAction/win animation/' + videoFile);
            video.load();
            const fallback = setTimeout(() => setShowUI(true), 2000);
            video.onended = () => { clearTimeout(fallback); setShowUI(true); };
            video.onerror = () => { clearTimeout(fallback); setShowUI(true); };
            video.play().then(() => clearTimeout(fallback)).catch(() => { video.muted = true; video.play().catch(() => { clearTimeout(fallback); setShowUI(true); }); });
        } else {
            setShowUI(true);
        }
    }, [winner?.marshalName]);

    return (
        <div style={{position:'fixed', top:0, left:0, width:'100vw', height:'100vh', background:'black', zIndex:100}}>
            <video ref={videoRef} style={{position:'absolute', top:0, left:0, width:'100vw', height:'100vh', objectFit:'cover', zIndex:1}} />
            {showUI && (
                <div style={{display:'flex', position:'relative', zIndex:2, width:'100%', height:'100%', flexDirection:'column', alignItems:'center', justifyContent:'center', background:'rgba(0,0,0,0.4)'}}>
                    <h1 style={{color:'#f1c40f', fontSize:'5rem', textShadow:'0 0 30px #f39c12', marginBottom:'20px'}}>จบสงคราม!</h1>
                    <h2 style={{color:'white', fontSize:'2.5rem', marginBottom:'10px'}}>ผู้ชนะรอดชีวิต: <span style={{color:'#2ecc71'}}>{winner?.name || 'ไม่มีผู้รอดชีวิต (เสมอ)'}</span></h2>
                    <h3 style={{color:'#f39c12', fontSize:'2rem', marginBottom:'40px'}}>ขุนพลคู่กาย: <span style={{color:'white'}}>{winner?.marshalName || '-'}</span></h3>
                    <div style={{display:'flex', gap:'20px'}}>
                        <button className="action-btn recruit-btn" style={{fontSize:'1.2rem', padding:'15px 30px'}} onClick={onReturnToLobby}>🏠 กลับล็อบบี้ (หาห้องใหม่)</button>
                        <button className="action-btn produce-btn" style={{fontSize:'1.2rem', padding:'15px 30px'}} onClick={onLogout}>🚪 กลับหน้าเมนูหลัก</button>
                    </div>
                </div>
            )}
        </div>
    );
}
