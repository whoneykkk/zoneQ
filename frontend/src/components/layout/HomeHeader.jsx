import { ZQ } from '../../utils/colors'

export default function HomeHeader({ userName, onGoProfile, onGoMessages, onGoNotifications, onGoDashboard, isAdmin = false, hasUnreadMessages = false, hasUnreadNotifications = false }) {
  return (
    <div style={{ padding: '14px 16px 12px', display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
      <button onClick={onGoProfile} style={{ background: 'none', border: 'none', padding: 0, cursor: 'pointer', display: 'flex', alignItems: 'center', gap: 10 }}>
        <div style={{ width: 30, height: 30, borderRadius: '50%', background: '#E8E8E6', display: 'flex', alignItems: 'center', justifyContent: 'center', border: '2px solid #fff', boxShadow: '0 2px 4px rgba(0,0,0,0.1)' }}>
          <svg width="16" height="16" viewBox="0 0 24 24" fill="#A19E99">
            <circle cx="12" cy="8" r="4"/>
            <path d="M4 20c0-4 3.6-7 8-7s8 3 8 7"/>
          </svg>
        </div>
        <span style={{ fontSize: 14, fontWeight: 800, color: '#1A1A1A', fontFamily: "'NanumSquare_ac', sans-serif" }}>
          {userName} 님
        </span>
      </button>

      <div style={{ display: 'flex', gap: 10 }}>
        {isAdmin && (
          <button onClick={onGoDashboard} style={{ background: 'none', border: 'none', cursor: 'pointer', padding: 0, width: 20, height: 20, display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#1A1A1A" strokeWidth="1.8">
              <rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/><rect x="3" y="14" width="7" height="7"/>
            </svg>
          </button>
        )}
        <button onClick={onGoMessages} style={{ background: 'none', border: 'none', cursor: 'pointer', padding: 0, position: 'relative', width: 20, height: 20 }}>
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#1A1A1A" strokeWidth="1.8">
            <path d="M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2z"/>
            <polyline points="22,6 12,13 2,6"/>
          </svg>
          {hasUnreadMessages && (
            <span style={{ position: 'absolute', top: -3, right: -3, width: 7, height: 7, borderRadius: '50%', background: ZQ.blue, border: '1.5px solid #fff' }} />
          )}
        </button>

        <button onClick={onGoNotifications} style={{ background: 'none', border: 'none', cursor: 'pointer', padding: 0, position: 'relative', width: 20, height: 20 }}>
          <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="#1A1A1A" strokeWidth="1.8">
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
            <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
          </svg>
          {hasUnreadNotifications && (
            <span style={{ position: 'absolute', top: -3, right: -3, width: 7, height: 7, borderRadius: '50%', background: ZQ.C, border: '1.5px solid #fff' }} />
          )}
        </button>
      </div>
    </div>
  )
}
