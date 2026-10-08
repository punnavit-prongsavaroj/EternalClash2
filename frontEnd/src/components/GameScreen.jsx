import { useState, useEffect, useRef, useCallback } from 'react'
import { submitPlacement, submitAction, resolveTurn, fetchEvents, fetchBattles, EVENT_THAI_NAMES, PLAYER_COLORS } from '../api'

// Coordinate mappers to keep nodes away from UI elements
const mapX = (val) => 10 + (val * 0.8); // 10% to 90%
const mapY = (val) => 25 + (val * 0.65); // 25% to 90%

const SEASON_BGS = {
    summer: { day: '/Map/sunny.jpg', night: '/Map/Summer-night.jpg' },
    rainy:  { day: '/Map/rainy-day.jpg', night: '/Map/rainy-night.jpg' },
    winter: { day: '/Map/Snow-day.jpg', night: '/Map/Snow-night.jpg' },
};

function EventAnnouncement({ events, onDone }) {
    useEffect(() => {
        const t = setTimeout(onDone, 3500);
        return () => clearTimeout(t);
    }, []); // Run only on mount so polling doesn't reset the timer
    return (
        <div style={{position:'fixed', top:'50%', left:'50%', transform:'translate(-50%,-50%)', zIndex:100000, pointerEvents:'none', textAlign:'center', fontFamily:'"Kanit", sans-serif'}}>
            <h1 style={{fontSize:'5rem', margin:0, color:'#ff4757', fontWeight:900, textShadow:'0 5px 15px rgba(0,0,0,0.8), 0 0 20px #e74c3c'}}>⚠️ เกิดเหตุการณ์!</h1>
            {events.map((e,i) => <div key={i} style={{fontSize:'3.5rem', color:'#f1c40f', fontWeight:'bold', marginTop:'10px', textShadow:'0 5px 15px rgba(0,0,0,0.8)'}}>{e}</div>)}
        </div>
    );
}

export default function GameScreen({ gameState, gameId, playerId, isMusicPlaying }) {
    const [selectedCityId, setSelectedCityId] = useState(null);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [showAttackModal, setShowAttackModal] = useState(false);
    const [attackTarget, setAttackTarget] = useState('');
    const [attackSoldiers, setAttackSoldiers] = useState(100);
    const [showLog, setShowLog] = useState(false);
    const [showManual, setShowManual] = useState(false);
    const [manualPage, setManualPage] = useState(1);
    const [logEntries, setLogEntries] = useState([]);
    const [announcements, setAnnouncements] = useState([]);
    const lastLogTurnRef = useRef(-1);
    const announcedArmiesRef = useRef([]);

    const me = gameState?.players?.find(p => p.playerId === playerId);
    const myNodes = gameState?.nodes?.filter(n => n.ownerId === playerId) || [];
    const totalFood = myNodes.reduce((a, n) => a + (n.food || 0), 0);
    const totalSoldiers = myNodes.reduce((a, n) => a + (n.soldiers || 0), 0);

    const seasonId = (gameState?.season || 'SUMMER').toLowerCase();
    const isDay = gameState?.daytime;
    const seasonBg = (SEASON_BGS[seasonId] || SEASON_BGS.summer)[isDay ? 'day' : 'night'];
    const seasonTh = seasonId === 'summer' ? 'ฤดูร้อน' : (seasonId === 'rainy' ? 'ฤดูฝน' : 'ฤดูหนาว');

    // Load log on new turn
    useEffect(() => {
        if (!gameState || gameState.status !== 'IN_PROGRESS') return;
        const turn = gameState.currentTurn;
        if (turn <= 0 || turn === lastLogTurnRef.current) return;
        const prevTurn = lastLogTurnRef.current;
        lastLogTurnRef.current = turn;
        if (prevTurn <= 0) return;

        const loadLog = async () => {
            let extraLines = [];
            let popupTexts = [];
            try {
                const events = await fetchEvents(gameId, prevTurn);
                const myId = playerId;
                const incoming = gameState.visibleArmies.filter(a => a.visibleTargetPlayerId === myId && (a.arrivalTurn - turn) <= 1);
                incoming.forEach(a => {
                    if (!announcedArmiesRef.current.includes(a.armyId)) {
                        announcedArmiesRef.current.push(a.armyId);
                        popupTexts.push('⚠️ ข้าศึกบุกประชิดเมือง!');
                        const owner = gameState.players.find(p => p.playerId === a.ownerPlayerId);
                        extraLines.push(<span key={'army'+a.armyId} style={{color:'#e74c3c', fontSize:'1.1em'}}>🚨 <b>เตือนภัย:</b> กองทัพของ {owner?.name || 'ศัตรู'} กำลังมุ่งหน้ามาเมืองของคุณ!</span>);
                        new Audio('/sound/attack.mp3').play().catch(() => {});
                    }
                });
                events.forEach(ev => {
                    let evName = EVENT_THAI_NAMES[ev.eventType] || ev.eventType;
                    if (ev.description?.includes('ขงเบ้ง')) evName = 'กลยุทธ์ขงเบ้งทำงาน!';
                    else if (ev.description?.includes('ทหารไม่ฟัง')) evName = '❌ ทหารขัดขืน!';
                    else if (ev.description?.includes('ประกาศศึก')) evName = '⚔️ ประกาศสงครามอย่างเป็นธรรม!';
                    popupTexts.push(evName);
                    const target = ev.affectedPlayerId ? 'เมืองของ ' + (gameState.players.find(p => p.playerId === ev.affectedPlayerId)?.name || '?') : ev.affectedArmyId ? 'กองทัพที่กำลังเดินทาง' : '';
                    const impacts = [ev.foodImpact && 'เสบียง ' + ev.foodImpact, ev.soldierImpact && 'ทหาร ' + ev.soldierImpact, ev.extraTravelTurns && 'ดีเลย์ ' + ev.extraTravelTurns + ' เทิร์น'].filter(Boolean).join(', ');
                    const desc = ev.description ? ` (${ev.description})` : '';
                    extraLines.push(<span key={'ev'+ev.id} style={{color:'#9b59b6'}}>⚡ <b>อีเวนต์: [{evName}]</b> เกิดขึ้นที่ {target}{impacts ? ' (ผลกระทบ: ' + impacts + ')' : ''} <span style={{color:'#f1c40f'}}>{desc}</span></span>);
                    new Audio('/sound/' + ev.eventType.toLowerCase() + '.mp3').play().catch(() => {});
                });
            } catch {}
            try {
                const battles = await fetchBattles(gameId, prevTurn);
                if (battles.length > 0) new Audio('/sound/battle.mp3').play().catch(() => {});
                battles.forEach(b => {
                    const atk = gameState.players.find(p => p.playerId === b.attackerPlayerId);
                    const def = gameState.players.find(p => p.playerId === b.defenderPlayerId);
                    extraLines.push(<span key={'b'+b.id}>⚔️ <b style={{color:'#e74c3c'}}>{atk?.name || '?'} ปะทะ {def?.name || '?'}</b> (สูญเสีย: รุก {b.attackerCasualties}, รับ {b.defenderCasualties})</span>);
                    if (b.cityDestroyed) extraLines.push(<span key={'bd'+b.id} style={{color:'#c0392b', fontSize:'1.1rem'}}>💥 <b>เมืองของ {def?.name} แตกพ่าย!</b></span>);
                });
            } catch {}
            let actionLines = (gameState.visibleActions || []).map((act, i) => {
                let actStr = act.actionType === 'PRODUCE_FOOD' ? <span style={{color:'#2ecc71'}}>ทำฟาร์ม</span> : act.actionType === 'RECRUIT_SOLDIERS' ? <span style={{color:'#3498db'}}>เกณฑ์ทหาร</span> : act.actionType === 'SEND_ARMY' ? <span style={{color:'#e74c3c'}}>ส่งกองทัพโจมตี</span> : 'พักผ่อน';
                const p = gameState.players.find(p => p.playerId === act.playerId);
                return <div key={'act'+i}>🏰 {p?.name || '?'} เลือก: {actStr}</div>;
            });
            if (popupTexts.length > 0) setAnnouncements(popupTexts);
            if (actionLines.length > 0 || extraLines.length > 0) {
                setLogEntries(prev => [{ turn: prevTurn, actions: actionLines, extras: extraLines }, ...prev]);
                setShowLog(true);
            }
        };
        loadLog();
    }, [gameState?.currentTurn]);

    const handleCityClick = useCallback(async (nodeId) => {
        if (isSubmitting) return;
        const node = gameState?.nodes?.find(n => n.nodeId === nodeId);
        if (!node) return;
        if (gameState.status === 'PLACEMENT') {
            if (!node.ownerId) {
                setIsSubmitting(true);
                try { await submitPlacement(gameId, playerId, nodeId); } catch (e) { alert(e.message); }
                finally { setIsSubmitting(false); }
            }
        } else if (gameState.status === 'IN_PROGRESS') {
            if (node.ownerId === playerId && !node.actionUsedThisTurn) {
                setSelectedCityId(nodeId);
            } else if (node.ownerId !== playerId) {
                if (selectedCityId) {
                    setAttackTarget(String(nodeId));
                    setShowAttackModal(true);
                } else {
                    alert('กรุณาคลิกเลือกเมืองของท่านก่อนที่จะสั่งโจมตีเมืองอื่น');
                }
            }
        }
    }, [gameState, playerId, selectedCityId, isSubmitting, gameId]);

    const doSubmitAction = useCallback(async (actionType, targetCityId = null, soldiers = null) => {
        if (isSubmitting || !selectedCityId) return;
        setIsSubmitting(true);
        setSelectedCityId(null);
        try {
            await submitAction(gameId, playerId, { cityId: selectedCityId, actionType, targetCityId, soldierCount: soldiers });
            await resolveTurn(gameId);
        } catch (e) { alert(e.message); setSelectedCityId(selectedCityId); }
        finally { setIsSubmitting(false); }
    }, [isSubmitting, selectedCityId, gameId, playerId]);

    const allTargets = (gameState?.nodes || []).filter(n => n.nodeId !== selectedCityId);

    return (
        <div id="game-screen" className="screen" style={{display:'flex'}}>
            {/* Dynamic bg */}
            <div id="dynamic-map-bg" className="lobby-bg" style={{backgroundImage:`url("${seasonBg}")`, zIndex:0}}></div>

            {/* SVG Edges */}
            <svg style={{position:'absolute', top:0, left:0, width:'100%', height:'100%', zIndex:4, pointerEvents:'none'}}>
                {(gameState?.edges || []).map(edge => {
                    const n1 = gameState.nodes.find(n => n.nodeId === edge.node1Id);
                    const n2 = gameState.nodes.find(n => n.nodeId === edge.node2Id);
                    if (!n1 || !n2) return null;
                    return <line key={edge.edgeId || `${edge.node1Id}-${edge.node2Id}`} x1={mapX(n1.x)+'%'} y1={mapY(n1.y)+'%'} x2={mapX(n2.x)+'%'} y2={mapY(n2.y)+'%'} stroke="rgba(255,255,255,0.4)" strokeWidth="4" strokeDasharray="8 8" />;
                })}
            </svg>

            {/* Castles + Armies */}
            <div style={{position:'absolute', top:0, left:0, width:'100%', height:'100%', zIndex:5}}>
                {(gameState?.nodes || []).map(node => {
                    const isMine = node.ownerId === playerId;
                    const owner = gameState.players.find(p => p.playerId === node.ownerId);
                    const pIdx = owner ? gameState.players.findIndex(p => p.playerId === owner.playerId) : -1;
                    const badgeColor = pIdx >= 0 ? PLAYER_COLORS[pIdx % PLAYER_COLORS.length] : '#555';
                    const isSelected = selectedCityId === node.nodeId;
                    const nodeIcon = owner ? '🏰' : '⚪';
                    let cursor = 'default';
                    if (gameState.status === 'PLACEMENT' && !owner) cursor = 'pointer';
                    else if (gameState.status === 'IN_PROGRESS' && isMine && !node.actionUsedThisTurn) cursor = 'pointer';
                    else if (gameState.status === 'IN_PROGRESS' && !isMine) cursor = 'crosshair';
                    return (
                        <div key={node.nodeId} className={'castle-node' + (isMine ? ' my-castle' : '')} style={{left: mapX(node.x)+'%', top: mapY(node.y)+'%', opacity: node.actionUsedThisTurn ? 0.5 : 1, filter: node.actionUsedThisTurn ? 'grayscale(50%)' : 'none', boxShadow: isSelected ? '0 0 20px 10px #f1c40f' : undefined, borderRadius: isSelected ? '50%' : undefined}}>
                            {node.soldiers != null && (
                                <div style={{position:'absolute', top:'-10px', right:'-10px', background:badgeColor, color:'white', fontSize:'0.8rem', fontWeight:'bold', padding:'2px 6px', borderRadius:'10px', border:'2px solid white', pointerEvents:'none', zIndex:10, whiteSpace:'nowrap'}}>
                                    {isMine && node.food != null && `🌾${node.food} `}{node.soldiers} ⚔️
                                </div>
                            )}
                            <div className="castle-icon" style={{pointerEvents:'auto', cursor}} onClick={() => handleCityClick(node.nodeId)}>{nodeIcon}</div>
                            <div className="castle-name">{node.name}</div>
                        </div>
                    );
                })}
                {/* Army markers */}
                {(gameState?.visibleArmies || []).map(army => {
                    const s = gameState.nodes.find(n => n.nodeId === army.sourceCityId);
                    const t = gameState.nodes.find(n => n.nodeId === army.visibleTargetCityId);
                    if (!s || !t) return null;
                    const rem = army.arrivalTurn - gameState.currentTurn;
                    let ratio = 0.5;
                    if (rem === 2) ratio = 0.33; else if (rem === 1) ratio = 0.66; else if (rem <= 0) ratio = 0.9;
                    const x = mapX(s.x) + (mapX(t.x) - mapX(s.x)) * ratio;
                    const y = mapY(s.y) + (mapY(t.y) - mapY(s.y)) * ratio;
                    const owner = gameState.players.find(p => p.playerId === army.ownerPlayerId);
                    const pIdx = owner ? gameState.players.findIndex(p => p.playerId === owner.playerId) : 0;
                    const color = PLAYER_COLORS[pIdx % PLAYER_COLORS.length];
                    return (
                        <div key={army.armyId} style={{position:'absolute', left:x+'%', top:y+'%', transform:'translate(-50%,-50%)', background:color, color:'white', padding:'4px 8px', borderRadius:'15px', fontSize:'0.75rem', fontWeight:'bold', border:'2px solid white', zIndex:5, pointerEvents:'none', boxShadow:'0 0 5px rgba(0,0,0,0.5)', whiteSpace:'nowrap'}}>
                            {owner?.name} ({s.name}) ⚔️{army.soldiers}
                        </div>
                    );
                })}
            </div>

            {/* Top bar */}
            <div className="game-dashboard" style={{pointerEvents:'none'}}>
                <div className="top-bar glass-panel" style={{pointerEvents:'auto'}}>
                    <div className="turn-info">
                        <h2 style={{color:'#f39c12', fontSize:'1.8rem', margin:0}}>เทิร์นที่: {gameState?.currentTurn} ({seasonTh} - {isDay ? 'กลางวัน' : 'กลางคืน'})</h2>
                    </div>
                    <div className="player-resources">
                        <div className="resource-item">ขุนพล: <span className="highlight">{me?.marshalName || '-'}</span></div>
                        <div className="resource-item">🏰 เมือง: <span className="highlight">{me?.name || '-'}</span></div>
                        <div className="resource-item">🌾 เสบียง: <span className="highlight">{totalFood}</span></div>
                        <div className="resource-item">⚔️ ทหาร: <span className="highlight">{totalSoldiers}</span></div>
                    </div>
                </div>
            </div>

            {/* Placement panel */}
            {gameState?.status === 'PLACEMENT' && (
                <div className="glass-panel" style={{display:'flex', position:'absolute', top:'10px', left:'50%', transform:'translateX(-50%)', zIndex:25, flexDirection:'column', alignItems:'center', padding:'20px'}}>
                    <h2 style={{color:'#f39c12', marginBottom:'10px', textAlign:'center'}}>ช่วงเตรียมการรบ: เลือกอณาเขตตั้งฐานทัพ</h2>
                    <p style={{color:'white', fontSize:'1.1rem'}}>โปรดคลิกเลือกเมืองที่เป็นอณาเขตว่าง (สีเทา) บนแผนที่เพื่อตั้งเป็นฐานหลักของท่าน</p>
                    {isSubmitting && <p style={{color:'#2ecc71', fontWeight:'bold', marginTop:'10px'}}>ท่านได้เลือกฐานแล้ว รอผู้เล่นท่านอื่น...</p>}
                </div>
            )}

            {/* Command panel */}
            {gameState?.status === 'IN_PROGRESS' && selectedCityId && !isSubmitting && (() => {
                const city = gameState.nodes.find(n => n.nodeId === selectedCityId);
                return (
                    <div className="command-panel glass-panel" style={{pointerEvents:'auto', position:'absolute', top:'50%', left:'50%', transform:'translate(-50%,-50%)', zIndex:20}}>
                        <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '10px'}}>
                            <div style={{width: '30px'}}></div>
                            <h3 style={{color:'#f39c12', textAlign:'center', margin: 0, fontSize:'1.5rem', paddingTop: '5px'}}>ออกคำสั่งประจำเทิร์น</h3>
                            <button onClick={() => setSelectedCityId(null)} style={{background:'transparent', border:'none', color:'white', fontSize:'2rem', cursor:'pointer', padding: 0, lineHeight: 1, width: '30px', height: '30px', display: 'flex', justifyContent: 'center', alignItems: 'center'}}>×</button>
                        </div>
                        <p style={{color:'#bdc3c7', textAlign:'center', marginBottom:'15px'}}>{city?.name} | 🌾{city?.food} ⚔️{city?.soldiers}</p>
                        <div id="action-buttons-container">
                            <button className="action-btn produce-btn" onClick={() => doSubmitAction('PRODUCE_FOOD')}>🌾 ทำฟาร์ม (เพิ่มเสบียง)</button>
                            <button className="action-btn recruit-btn" onClick={() => doSubmitAction('RECRUIT_SOLDIERS')}>⚔️ เกณฑ์ทหาร (ใช้เสบียง)</button>
                            <button className="action-btn attack-btn" onClick={() => setShowAttackModal(true)}>🚀 ส่งกองทัพโจมตี</button>
                        </div>
                    </div>
                );
            })()}

            {/* Attack modal */}
            {showAttackModal && (
                <div className="popup-overlay" style={{display:'flex', zIndex:200, pointerEvents:'auto'}}>
                    <div className="popup-box glass-panel">
                        <h2 style={{color:'#e74c3c', marginBottom:'15px'}}>ส่งกองทัพ</h2>
                        <div style={{margin:'20px 0', textAlign:'left'}}>
                            <label style={{color:'white', display:'block', marginBottom:'10px'}}>เป้าหมาย:</label>
                            <select value={attackTarget} onChange={e => setAttackTarget(e.target.value)} style={{width:'100%', padding:'10px', borderRadius:'8px', fontSize:'1.1rem'}}>
                                <option value="">-- เลือกเมืองเป้าหมาย --</option>
                                {allTargets.map(t => <option key={t.nodeId} value={t.nodeId}>{t.ownerId ? '🏰' : '⚪'} {t.name}</option>)}
                            </select>
                        </div>
                        <div style={{margin:'20px 0', textAlign:'left'}}>
                            <label style={{color:'white', display:'block', marginBottom:'10px'}}>จำนวนทหาร:</label>
                            <input type="number" min="1" value={attackSoldiers} onChange={e => setAttackSoldiers(parseInt(e.target.value))} style={{width:'100%', padding:'10px', borderRadius:'8px', fontSize:'1.1rem'}} />
                        </div>
                        <div className="popup-buttons">
                            <button className="action-btn attack-btn" onClick={() => { setShowAttackModal(false); doSubmitAction('SEND_ARMY', parseInt(attackTarget), attackSoldiers); }} disabled={!attackTarget}>โจมตี!</button>
                            <button className="action-btn cancel-btn" onClick={() => setShowAttackModal(false)}>ยกเลิก</button>
                        </div>
                    </div>
                </div>
            )}

            {/* Log panel */}
            <button className="action-btn" style={{position:'absolute', bottom:'20px', right:'20px', zIndex:15, background:'#34495e', padding:'10px 20px', fontSize:'1.1rem', width:'auto'}} onClick={() => setShowLog(v => !v)}>📜 บันทึกสงคราม</button>
            {showLog && (
                <div className="glass-panel" style={{display:'flex', position:'absolute', bottom:'70px', right:'20px', width:'350px', maxHeight:'400px', zIndex:15, flexDirection:'column', padding:'20px'}}>
                    <h3 style={{color:'#f39c12', marginBottom:'10px', textAlign:'center'}}>เหตุการณ์ล่าสุด</h3>
                    <div style={{flex:1, overflowY:'auto', color:'white', fontSize:'1rem', textAlign:'left', padding:'10px', background:'rgba(0,0,0,0.5)', borderRadius:'8px', minHeight:'200px'}}>
                        {logEntries.length === 0 ? <p style={{color:'#bdc3c7'}}><i>ยังไม่มีเหตุการณ์...</i></p> : logEntries.map((entry, i) => (
                            <div key={i} style={{marginBottom:'10px', lineHeight:1.5}}>
                                <strong style={{color:'#f39c12'}}>[สรุปเหตุการณ์ เทิร์น {entry.turn}]</strong>
                                {entry.actions}
                                {entry.extras.map((e, j) => <div key={j}>{e}</div>)}
                                <hr style={{borderColor:'#7f8c8d', margin:'10px 0'}} />
                            </div>
                        ))}
                    </div>
                </div>
            )}

            {/* Manual */}
            <button className="action-btn" style={{position:'absolute', bottom:'20px', right:'200px', zIndex:15, background:'#2980b9', padding:'10px 20px', fontSize:'1.1rem', width:'auto'}} onClick={() => setShowManual(v => !v)}>📖 คู่มือการเล่น</button>
            {showManual && (
                <div className="popup-overlay" style={{display:'flex', zIndex:1000, pointerEvents:'auto'}}>
                    <div className="glass-panel" style={{display:'flex', position:'relative', width:'90vw', height:'90vh', flexDirection:'column', padding:'25px', background:'rgba(20,25,30,0.95)', border:'1px solid rgba(243, 156, 18, 0.5)', boxShadow:'0 0 40px rgba(0,0,0,0.8)', borderRadius:'16px'}}>
                        <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '15px'}}>
                            <div style={{width: '35px'}}></div>
                            <h3 style={{color:'#f39c12', margin:0, fontSize:'2rem', textShadow:'0 2px 5px rgba(0,0,0,0.5)'}}>คู่มือการเล่น (หน้า {manualPage}/12)</h3>
                            <button onClick={() => setShowManual(false)} style={{background:'rgba(255,255,255,0.1)', border:'none', color:'white', width:'35px', height:'35px', borderRadius:'50%', fontSize:'1.2rem', cursor:'pointer', display:'flex', justifyContent:'center', alignItems:'center', transition:'0.2s', padding: 0}} onMouseOver={(e)=>e.currentTarget.style.background='rgba(231, 76, 60, 0.8)'} onMouseOut={(e)=>e.currentTarget.style.background='rgba(255,255,255,0.1)'}>✖</button>
                        </div>
                        <div style={{flex:1, position:'relative', background:'rgba(0,0,0,0.6)', borderRadius:'12px', padding:'20px', display:'flex', alignItems:'center', justifyContent:'center', overflow:'hidden', boxShadow:'inset 0 0 20px rgba(0,0,0,0.8)'}}>
                            <button onClick={() => setManualPage(p => Math.max(1, p-1))} style={{position:'absolute', left:'10px', background:'rgba(0,0,0,0.5)', border:'none', color: manualPage === 1 ? 'rgba(255,255,255,0.1)' : 'white', fontSize:'3rem', cursor: manualPage === 1 ? 'default' : 'pointer', padding:'20px 15px', width:'auto', borderRadius:'8px', transition:'0.3s', zIndex:10}} onMouseOver={(e)=>manualPage !== 1 && (e.currentTarget.style.background='rgba(243, 156, 18, 0.8)')} onMouseOut={(e)=>manualPage !== 1 && (e.currentTarget.style.background='rgba(0,0,0,0.5)')}>❮</button>
                            
                            <img src={'/img/manual/page_' + manualPage + '.jpg'} style={{maxWidth:'100%', maxHeight:'100%', objectFit:'contain', borderRadius:'8px', boxShadow:'0 5px 25px rgba(0,0,0,0.5)'}} alt={`คู่มือหน้า ${manualPage}`} />
                            
                            <button onClick={() => setManualPage(p => Math.min(12, p+1))} style={{position:'absolute', right:'10px', background:'rgba(0,0,0,0.5)', border:'none', color: manualPage === 12 ? 'rgba(255,255,255,0.1)' : 'white', fontSize:'3rem', cursor: manualPage === 12 ? 'default' : 'pointer', padding:'20px 15px', width:'auto', borderRadius:'8px', transition:'0.3s', zIndex:10}} onMouseOver={(e)=>manualPage !== 12 && (e.currentTarget.style.background='rgba(243, 156, 18, 0.8)')} onMouseOut={(e)=>manualPage !== 12 && (e.currentTarget.style.background='rgba(0,0,0,0.5)')}>❯</button>
                        </div>
                    </div>
                </div>
            )}

            {/* Event announcement */}
            {announcements.length > 0 && <EventAnnouncement events={announcements} onDone={() => setAnnouncements([])} />}
        </div>
    );
}
