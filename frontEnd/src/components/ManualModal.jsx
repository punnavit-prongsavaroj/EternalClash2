import React, { useState } from 'react';

export default function ManualModal({ onClose }) {
    const [manualPage, setManualPage] = useState(1);
    const TOTAL_PAGES = 13;
    
    return (
        <div className="popup-overlay" style={{display:'flex', zIndex:1000, pointerEvents:'auto'}}>
            <div className="glass-panel" style={{display:'flex', position:'relative', width:'90vw', height:'90vh', flexDirection:'column', padding:'25px', background:'rgba(20,25,30,0.95)', border:'1px solid rgba(243, 156, 18, 0.5)', boxShadow:'0 0 40px rgba(0,0,0,0.8)', borderRadius:'16px'}}>
                <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '15px'}}>
                    <div style={{width: '35px'}}></div>
                    <h3 style={{color:'#f39c12', margin:0, fontSize:'2rem', textShadow:'0 2px 5px rgba(0,0,0,0.5)'}}>คู่มือการเล่น (หน้า {manualPage}/{TOTAL_PAGES})</h3>
                    <button onClick={onClose} style={{background:'rgba(255,255,255,0.1)', border:'none', color:'white', width:'35px', height:'35px', borderRadius:'50%', fontSize:'1.2rem', cursor:'pointer', display:'flex', justifyContent:'center', alignItems:'center', transition:'0.2s', padding: 0}} onMouseOver={(e)=>e.currentTarget.style.background='rgba(231, 76, 60, 0.8)'} onMouseOut={(e)=>e.currentTarget.style.background='rgba(255,255,255,0.1)'}>✖</button>
                </div>
                <div style={{flex:1, position:'relative', background:'rgba(0,0,0,0.6)', borderRadius:'12px', padding:'20px', display:'flex', alignItems:'center', justifyContent:'center', overflow:'hidden', boxShadow:'inset 0 0 20px rgba(0,0,0,0.8)'}}>
                    <button onClick={() => setManualPage(p => Math.max(1, p-1))} style={{position:'absolute', left:'10px', background:'rgba(0,0,0,0.5)', border:'none', color: manualPage === 1 ? 'rgba(255,255,255,0.1)' : 'white', fontSize:'3rem', cursor: manualPage === 1 ? 'default' : 'pointer', padding:'20px 15px', width:'auto', borderRadius:'8px', transition:'0.3s', zIndex:10}} onMouseOver={(e)=>manualPage !== 1 && (e.currentTarget.style.background='rgba(243, 156, 18, 0.8)')} onMouseOut={(e)=>manualPage !== 1 && (e.currentTarget.style.background='rgba(0,0,0,0.5)')}>❮</button>
                    
                    <img src={'/img/manual/page_' + manualPage + '.jpg'} style={{maxWidth:'100%', maxHeight:'100%', objectFit:'contain', borderRadius:'8px', boxShadow:'0 5px 25px rgba(0,0,0,0.5)'}} alt={`คู่มือหน้า ${manualPage}`} />
                    
                    <button onClick={() => setManualPage(p => Math.min(TOTAL_PAGES, p+1))} style={{position:'absolute', right:'10px', background:'rgba(0,0,0,0.5)', border:'none', color: manualPage === TOTAL_PAGES ? 'rgba(255,255,255,0.1)' : 'white', fontSize:'3rem', cursor: manualPage === TOTAL_PAGES ? 'default' : 'pointer', padding:'20px 15px', width:'auto', borderRadius:'8px', transition:'0.3s', zIndex:10}} onMouseOver={(e)=>manualPage !== TOTAL_PAGES && (e.currentTarget.style.background='rgba(243, 156, 18, 0.8)')} onMouseOut={(e)=>manualPage !== TOTAL_PAGES && (e.currentTarget.style.background='rgba(0,0,0,0.5)')}>❯</button>
                </div>
            </div>
        </div>
    );
}
