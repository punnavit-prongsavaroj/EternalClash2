import { useEffect, useState } from 'react';

export default function CloudTransition({ onMiddle }) {
    const [phase, setPhase] = useState('start'); // start -> middle -> end
    
    // Create random cloud configs once on mount
    const [clouds] = useState(() => {
        const arr = [];
        for (let i = 0; i < 30; i++) {
            const size = Math.random() * 400 + 300;
            const top = Math.random() * 120 - 10;
            const initialLeft = 120;
            let targetLeft = Math.random() * 80;
            if (targetLeft > 60) targetLeft = 50;
            arr.push({ id: i, size, top, initialLeft, targetLeft });
        }
        return arr;
    });

    useEffect(() => {
        // Trigger move to middle
        const t1 = setTimeout(() => {
            setPhase('middle');
        }, 50);

        // Call callback when clouds cover screen
        const t2 = setTimeout(() => {
            if (onMiddle) onMiddle();
            setPhase('end');
        }, 900);

        return () => { clearTimeout(t1); clearTimeout(t2); };
    }, [onMiddle]);

    return (
        <div style={{
            position: 'fixed',
            top: 0,
            left: 0,
            width: '100vw',
            height: '100vh',
            zIndex: 99999,
            pointerEvents: 'none',
            overflow: 'hidden',
            backgroundColor: phase === 'middle' ? 'rgba(255,255,255,0.9)' : 'transparent',
            transition: 'background-color 0.5s ease-in-out'
        }}>
            {clouds.map(c => {
                let left = c.initialLeft + 'vw';
                if (phase === 'middle') left = c.targetLeft + 'vw';
                else if (phase === 'end') left = '-150vw';

                return (
                    <div key={c.id} style={{
                        position: 'absolute',
                        width: c.size + 'px',
                        height: (c.size * 0.7) + 'px',
                        backgroundColor: '#ffffff',
                        borderRadius: '50%',
                        opacity: 1,
                        top: c.top + 'vh',
                        left: left,
                        transition: 'left 0.8s ease-in-out'
                    }} />
                );
            })}
        </div>
    );
}
