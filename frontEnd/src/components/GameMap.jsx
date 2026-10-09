import { useEffect, useRef } from 'react'
import { Network } from 'vis-network'

function GameMap({ gameState, playerId, selectedCityId, onCityClick }) {
    const containerRef = useRef(null);
    const networkRef = useRef(null);

    useEffect(() => {
        if (!containerRef.current) return;

        const nodes = gameState.nodes.map(n => {
            const isMine = n.ownerId === playerId;
            let color = n.ownerId ? (isMine ? '#2ecc71' : '#e74c3c') : '#95a5a6';
            if (n.nodeId === selectedCityId) {
                color = '#f1c40f'; // selected highlight
            }
            if (n.actionUsedThisTurn) {
                color = '#7f8c8d'; // grayed out
            }

            let label = n.name;
            if (n.soldiers != null) {
                label += `\n⚔️ ${n.soldiers}`;
                if (isMine && n.food != null) {
                    label += `\n🌾 ${n.food}`;
                }
            }

            return {
                id: n.nodeId,
                label: label,
                x: n.x,
                y: n.y,
                color: color,
                shape: 'dot',
                size: isMine ? 25 : 15,
                font: { color: 'white', strokeWidth: 2, strokeColor: '#000' }
            };
        });

        const edges = gameState.edges.map(e => ({
            from: e.sourceId,
            to: e.targetId,
            color: { color: 'rgba(255,255,255,0.2)' },
            width: 2
        }));

        // Render armies as moving nodes or just labels on edges for now
        // For a true SPA, you can animate them, but let's stick to simple edges/nodes
        gameState.visibleArmies.forEach(a => {
            const s = gameState.nodes.find(n => n.nodeId === a.sourceCityId);
            const t = gameState.nodes.find(n => n.nodeId === a.visibleTargetCityId);
            if (s && t) {
                let ratio = 0.5;
                const rem = a.arrivalTurn - gameState.currentTurn;
                if (rem === 2) ratio = 0.33;
                if (rem === 1) ratio = 0.66;
                if (rem <= 0) ratio = 0.9;
                
                nodes.push({
                    id: 'army_' + a.armyId,
                    label: `⚔️ ${a.soldiers} (ถึงใน ${rem} turn)`,
                    x: s.x + (t.x - s.x) * ratio,
                    y: s.y + (t.y - s.y) * ratio,
                    color: a.ownerId === playerId ? '#3498db' : '#c0392b',
                    shape: 'triangle',
                    size: 15,
                    font: { color: 'yellow', strokeWidth: 2, strokeColor: '#000' }
                });
            }
        });

        const data = { nodes, edges };
        const options = {
            physics: false,
            interaction: {
                dragNodes: false,
                zoomView: true,
                dragView: true
            }
        };

        if (!networkRef.current) {
            networkRef.current = new Network(containerRef.current, data, options);
            networkRef.current.on('click', (params) => {
                if (params.nodes.length > 0) {
                    const id = params.nodes[0];
                    if (typeof id === 'number') { // not an army
                        onCityClick(id);
                    }
                }
            });
        } else {
            networkRef.current.setData(data);
        }

    }, [gameState, playerId, selectedCityId, onCityClick]);

    return (
        <div 
            ref={containerRef} 
            style={{width: '100vw', height: '100vh', background: '#34495e'}} 
        />
    );
}

export default GameMap
