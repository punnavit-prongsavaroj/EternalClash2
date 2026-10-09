import { useState, useEffect } from 'react'
import { getMarshalCandidates, rerollMarshal, confirmMarshal } from '../api'
import { MARSHAL_IMGS } from '../api'

export default function DraftScreen({ gameState, gameId, playerId }) {
    const [marshal, setMarshal] = useState(null);
    const [submitting, setSubmitting] = useState(false);
    const [done, setDone] = useState(false);

    const me = gameState?.players?.find(p => p.playerId === playerId);

    useEffect(() => {
        if (me?.marshalName) { setDone(true); return; }
        const currentDrafter = gameState?.players?.find(p => !p.marshalName);
        if (currentDrafter?.playerId === playerId) {
            getMarshalCandidates(playerId).then(list => {
                if (list.length > 0) setMarshal(list[0].marshal);
            }).catch(() => {});
        }
    }, [gameState, playerId, me?.marshalName]);

    const handleReroll = async () => {
        setSubmitting(true);
        try {
            const c = await rerollMarshal(playerId);
            setMarshal(c.marshal);
        } catch (e) { alert(e.message || 'สุ่มใหม่ไม่ได้แล้ว'); }
        finally { setSubmitting(false); }
    };

    const handleConfirm = async () => {
        setSubmitting(true);
        try {
            await confirmMarshal(playerId);
            setDone(true);
        } catch (e) { alert(e.message || 'เลือกไม่สำเร็จ'); }
        finally { setSubmitting(false); }
    };

    const currentDrafter = gameState?.players?.find(p => !p.marshalName);
    const isMyTurn = currentDrafter?.playerId === playerId;

    if (done || (me?.marshalName)) {
        return (
            <div id="draft-screen" className="screen" style={{display:'flex', flexDirection:'column', alignItems:'center', justifyContent:'center'}}>
                <div className="lobby-bg"></div>
                <div className="glass-panel" style={{padding:'40px', textAlign:'center', zIndex:10}}>
                    <h2 style={{color:'#f39c12', fontSize:'2rem'}}>ท่านเลือก <strong style={{color:'white'}}>{me?.marshalName || '...'}</strong> แล้ว</h2>
                    <p style={{color:'#bdc3c7', marginTop:'20px', fontSize:'1.3rem'}}>กำลังรอผู้เล่นอื่น...</p>
                    <div style={{marginTop:'30px', fontSize:'3rem'}}>⏳</div>
                </div>
            </div>
        );
    }

    if (!isMyTurn) {
        return (
            <div id="draft-screen" className="screen" style={{display:'flex', flexDirection:'column', alignItems:'center', justifyContent:'center'}}>
                <div className="lobby-bg"></div>
                <div className="glass-panel" style={{padding:'40px', textAlign:'center', zIndex:10}}>
                    <h3 style={{color:'#f39c12', fontSize:'1.5rem'}}>รอผู้เล่น {currentDrafter?.name || '...'} เลือกแม่ทัพก่อน...</h3>
                </div>
            </div>
        );
    }

    return (
        <div id="draft-screen" className="screen">
            <div className="lobby-bg"></div>
            <div className="glass-panel" style={{padding:'40px', textAlign:'center', borderRadius:'12px', width:'80%', maxWidth:'900px', marginTop:'50px', zIndex:10}}>
                <h2 style={{color:'#f39c12', fontSize:'2.5rem', marginBottom:'25px'}}>เลือกขุนพลคู่กาย</h2>
                {marshal && (
                    <div style={{margin:'0 auto', width:'350px', height:'500px', overflow:'hidden', position:'relative', border:'4px solid #f39c12', borderRadius:'12px', boxShadow:'0 10px 30px rgba(0,0,0,0.8)'}}>
                        <img src={'/Marshal/' + (MARSHAL_IMGS[marshal.name] || 'Jo-Sho.jpg')} style={{position:'absolute', top:0, left:0, width:'100%', height:'100%', objectFit:'cover', objectPosition:'top'}} alt={marshal.name} />
                        <div style={{position:'absolute', top:0, left:0, width:'100%', padding:'25px 15px 40px', background:'linear-gradient(to bottom, rgba(0,0,0,0.9) 10%, rgba(0,0,0,0.5) 60%, transparent)', boxSizing:'border-box'}}></div>
                        <div style={{position:'absolute', bottom:0, left:0, width:'100%', padding:'50px 20px 20px', background:'linear-gradient(to top, rgba(0,0,0,1) 10%, rgba(0,0,0,0.85) 60%, transparent)', boxSizing:'border-box', textAlign:'center'}}>
                            <h3 style={{fontSize:'2.8rem', marginBottom:'12px', color:'#f39c12', textShadow:'2px 2px 4px #000', letterSpacing:'2px'}}>{marshal.name}</h3>
                            <p style={{fontSize:'1.05rem', color:'#2ecc71', textShadow:'1px 1px 3px #000', marginBottom:'6px'}}>✅ จุดเด่น: {marshal.abilityDescription || 'ไม่มี'}</p>
                            <p style={{fontSize:'1.05rem', color:'#e74c3c', textShadow:'1px 1px 3px #000', margin:0}}>⚠️ ข้อเสีย: {marshal.disadvantageDescription || 'ไม่มี'}</p>
                        </div>
                    </div>
                )}
                <div style={{marginTop:'30px', display:'flex', justifyContent:'center', gap:'20px'}}>
                    <button className="action-btn produce-btn" disabled={submitting} onClick={handleConfirm}>✅ เลือกขุนพลคนนี้</button>
                    <button className="action-btn attack-btn" disabled={submitting} onClick={handleReroll}>🔄 สุ่มเปลี่ยนคนใหม่</button>
                </div>
            </div>
        </div>
    );
}
