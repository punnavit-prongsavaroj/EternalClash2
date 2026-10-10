import { useState } from 'react'

function CommandPanel({ gameState, playerId, selectedCityId, onClose, onSubmitAction, isSubmitting }) {
    const [actionType, setActionType] = useState('PRODUCE_FOOD');
    const [targetCityId, setTargetCityId] = useState('');

    const city = gameState.nodes.find(n => n.nodeId === selectedCityId);
    
    // Only show targets that aren't this city
    const targets = gameState.nodes.filter(n => n.nodeId !== selectedCityId);

    return (
        <div id="command-panel" className="ui-panel" style={{display: 'flex', flexDirection: 'column'}}>
            <h3>ออกคำสั่ง: {city?.name}</h3>
            <p>ทหาร: {city?.soldiers} | อาหาร: {city?.food}</p>
            
            <select value={actionType} onChange={e => setActionType(e.target.value)} disabled={isSubmitting}>
                <option value="PRODUCE_FOOD">🌾 ผลิตอาหาร (+20)</option>
                <option value="RECRUIT_SOLDIERS">⚔️ เกณฑ์ทหาร (+20)</option>
                <option value="SEND_ARMY">🚀 ส่งกองทัพโจมตี</option>
            </select>

            {actionType === 'SEND_ARMY' && (
                <div style={{marginTop: '10px'}}>
                    <p>เป้าหมายโจมตี:</p>
                    <select value={targetCityId} onChange={e => setTargetCityId(e.target.value)} disabled={isSubmitting}>
                        <option value="">-- เลือกเมืองเป้าหมาย --</option>
                        {targets.map(t => (
                            <option key={t.nodeId} value={t.nodeId}>{t.name} (ห่าง {
                                // Simplified distance logic for UI
                                1
                            } turn)</option>
                        ))}
                    </select>
                </div>
            )}

            <div style={{marginTop: '20px', display: 'flex', justifyContent: 'space-between'}}>
                <button 
                    onClick={() => onSubmitAction(actionType, actionType === 'SEND_ARMY' ? parseInt(targetCityId) : null)}
                    disabled={isSubmitting || (actionType === 'SEND_ARMY' && !targetCityId)}
                    style={{background: '#2ecc71'}}
                >
                    ยืนยันคำสั่ง
                </button>
                <button onClick={onClose} disabled={isSubmitting} style={{background: '#e74c3c'}}>
                    ยกเลิก
                </button>
            </div>
        </div>
    )
}

export default CommandPanel
