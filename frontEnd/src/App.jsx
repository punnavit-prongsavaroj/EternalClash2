import { useState, useEffect, useRef, useCallback } from 'react'
import LoginScreen from './components/LoginScreen'
import LobbyScreen from './components/LobbyScreen'
import RoomScreen from './components/RoomScreen'
import DraftScreen from './components/DraftScreen'
import GameScreen from './components/GameScreen'
import GameOverScreen from './components/GameOverScreen'
import CloudTransition from './components/CloudTransition'
import { fetchGameSnapshot, fetchPlayers } from './api'
import './index.css'

export default function App() {
  const [playerName, setPlayerName] = useState(() => localStorage.getItem('eternalClashPlayerName') || null);
  const [playerId, setPlayerId] = useState(() => {
    const v = localStorage.getItem('eternalClashPlayerId'); return v ? parseInt(v) : null;
  });
  const [gameId, setGameId] = useState(() => {
    const v = localStorage.getItem('eternalClashGameId'); return v ? parseInt(v) : null;
  });
  const [gameState, setGameState] = useState(null);
  const [screen, setScreen] = useState('login'); // login | lobby | room | draft | game | gameover
  const [transitionTask, setTransitionTask] = useState(null);
  const [isMusicPlaying, setIsMusicPlaying] = useState(true);
  const [isPortrait, setIsPortrait] = useState(false);
  const bgMusicRef = useRef(null);
  const lastStatusRef = useRef('');
  const lastTurnRef = useRef(-1);

  // Portrait lock
  useEffect(() => {
    const mq = window.matchMedia('(orientation: portrait)');
    const handler = (e) => setIsPortrait(e.matches);
    setIsPortrait(mq.matches);
    mq.addEventListener('change', handler);
    return () => mq.removeEventListener('change', handler);
  }, []);

  // Global click sound
  useEffect(() => {
    const playClickSound = (e) => {
        const target = e.target.closest('button, select, input[type="button"], input[type="submit"], .castle-icon');
        if (target) {
            const audio = new Audio('/sound/click.mp3');
            audio.volume = 0.5;
            audio.play().catch(() => {});
        }
    };
    document.addEventListener('click', playClickSound);
    return () => document.removeEventListener('click', playClickSound);
  }, []);

  // Music
  useEffect(() => {
    const audio = bgMusicRef.current;
    if (!audio) return;
    const tryPlay = () => { if (isMusicPlaying && audio.paused) audio.play().catch(() => {}); };
    document.addEventListener('click', tryPlay);
    return () => document.removeEventListener('click', tryPlay);
  }, [isMusicPlaying]);

  const switchBgm = useCallback((src) => {
    const audio = bgMusicRef.current;
    if (!audio || audio.src.includes(src)) return;
    audio.src = '/sound/' + src;
    if (isMusicPlaying) audio.play().catch(() => {});
  }, [isMusicPlaying]);

  const toggleMusic = () => {
    const audio = bgMusicRef.current;
    if (!audio) return;
    if (isMusicPlaying) { audio.pause(); setIsMusicPlaying(false); }
    else { audio.play().catch(() => {}); setIsMusicPlaying(true); }
  };

  const transition = useCallback((callback) => {
    setTransitionTask({ callback });
    setTimeout(() => {
        setTransitionTask(null);
    }, 1500); // 1.5s total animation time before resetting
  }, []);

  // Restore session
  useEffect(() => {
    if (gameId && playerId) { setScreen('room'); }
    else if (playerName) { setScreen('lobby'); }
  }, []);

  // Polling
  useEffect(() => {
    if (!gameId || !playerId) return;
    let isCancelled = false;
    const poll = async () => {
      try {
        const snapshot = await fetchGameSnapshot(gameId, playerId);
        if (isCancelled) return;
        setGameState(snapshot);
        const status = snapshot.status;

        if (status === 'MARSHAL_SELECTION' && lastStatusRef.current !== 'MARSHAL_SELECTION') {
          lastStatusRef.current = 'MARSHAL_SELECTION';
          transition(() => setScreen('draft'));
        } else if (status === 'PLACEMENT' && lastStatusRef.current !== 'PLACEMENT') {
          lastStatusRef.current = 'PLACEMENT';
          switchBgm('game_bgm.mp3');
          transition(() => setScreen('game'));
        } else if (status === 'IN_PROGRESS' && lastStatusRef.current !== 'IN_PROGRESS') {
          lastStatusRef.current = 'IN_PROGRESS';
          switchBgm('game_bgm.mp3');
          transition(() => setScreen('game'));
        } else if (status === 'FINISHED' && lastStatusRef.current !== 'FINISHED') {
          lastStatusRef.current = 'FINISHED';
          if (bgMusicRef.current) bgMusicRef.current.pause();
          transition(() => setScreen('gameover'));
        }

        if (status === 'IN_PROGRESS' && snapshot.currentTurn !== lastTurnRef.current) {
          lastTurnRef.current = snapshot.currentTurn;
        }
      } catch (e) {}
    };
    poll();
    const interval = setInterval(poll, 2000);
    return () => { isCancelled = true; clearInterval(interval); };
  }, [gameId, playerId, transition, switchBgm]);

  const handleJoined = (gId, pId, pName) => {
    localStorage.setItem('eternalClashGameId', gId);
    localStorage.setItem('eternalClashPlayerId', pId);
    localStorage.setItem('eternalClashPlayerName', pName);
    setGameId(gId); setPlayerId(pId); setPlayerName(pName);
    transition(() => setScreen('room'));
  };

  const handleLeaveRoom = () => {
    localStorage.removeItem('eternalClashGameId');
    localStorage.removeItem('eternalClashPlayerId');
    setGameId(null); setPlayerId(null);
    lastStatusRef.current = '';
    transition(() => setScreen('lobby'));
  };

  const handleLogout = () => {
    localStorage.clear();
    setGameId(null); setPlayerId(null); setPlayerName(null);
    lastStatusRef.current = '';
    setScreen('login');
  };

  const handleReturnToLobby = () => {
    localStorage.removeItem('eternalClashGameId');
    localStorage.removeItem('eternalClashPlayerId');
    setGameId(null); setPlayerId(null);
    lastStatusRef.current = '';
    transition(() => setScreen('lobby'));
  };

  const renderScreen = () => {
    switch (screen) {
      case 'login': return <LoginScreen onLoggedIn={(name) => { localStorage.setItem('eternalClashPlayerName', name); setPlayerName(name); transition(() => setScreen('lobby')); }} />;
      case 'lobby': return <LobbyScreen playerName={playerName} onJoined={handleJoined} onLogout={handleLogout} />;
      case 'room': return <RoomScreen gameId={gameId} playerId={playerId} gameState={gameState} onLeave={handleLeaveRoom} />;
      case 'draft': return <DraftScreen gameState={gameState} gameId={gameId} playerId={playerId} />;
      case 'game': return <GameScreen gameState={gameState} gameId={gameId} playerId={playerId} isMusicPlaying={isMusicPlaying} />;
      case 'gameover': return <GameOverScreen gameState={gameState} onReturnToLobby={handleReturnToLobby} onLogout={handleLogout} />;
      default: return null;
    }
  };

  return (
    <>
      {isPortrait && (
        <div style={{display:'flex', position:'fixed', top:0, left:0, width:'100vw', height:'100vh', background:'#2c3e50', zIndex:9999, flexDirection:'column', alignItems:'center', justifyContent:'center', color:'white', textAlign:'center', padding:'20px'}}>
          <div style={{fontSize:'5rem', marginBottom:'20px'}}>🔄</div>
          <h1 style={{color:'#f39c12', marginBottom:'10px'}}>กรุณาหมุนจอเป็นแนวนอน</h1>
          <p style={{fontSize:'1.2rem'}}>เกมนี้ออกแบบมาเพื่อเล่นในแนวนอนเท่านั้น (Landscape)</p>
        </div>
      )}
      {transitionTask && (
        <CloudTransition onMiddle={transitionTask.callback} />
      )}
      <audio ref={bgMusicRef} src="/sound/menu_bgm.mp3" loop />
      <button onClick={toggleMusic} style={{position:'fixed', top:'20px', right:'20px', zIndex:1000, background:'rgba(0,0,0,0.7)', color:'white', border:'2px solid #f39c12', width:'50px', height:'50px', borderRadius:'50%', cursor:'pointer', fontSize:'1.5rem', display:'flex', justifyContent:'center', alignItems:'center', boxShadow:'0 4px 10px rgba(0,0,0,0.5)'}}>
        {isMusicPlaying ? '🔊' : '🔇'}
      </button>
      {renderScreen()}
    </>
  );
}
