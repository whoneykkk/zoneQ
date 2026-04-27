import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ZQ } from '../../utils/colors'
import BackHeader from '../../components/layout/BackHeader'
import { fetchMessagesInbox } from '../../api/messages'

export default function MessagesPage() {
  const navigate = useNavigate()
  const { data: messages = [] } = useQuery({ queryKey: ['messages', 'inbox'], queryFn: fetchMessagesInbox })

  return (
    <div style={{ background: '#fff', minHeight: '100vh', position: 'relative' }}>
      <BackHeader title="쪽지함" onBack={() => navigate(-1)} />
      {messages.map((m) => (
        <div
          key={m.id}
          onClick={() => navigate(`/messages/${m.id}`)}
          style={{
            display: 'flex', alignItems: 'flex-start', gap: 10,
            padding: '12px 16px 12px 22px',
            borderBottom: `0.5px solid ${ZQ.border}`,
            cursor: 'pointer', position: 'relative',
          }}
        >
          {!m.isRead && (
            <div style={{
              position: 'absolute', left: 8, top: '50%', transform: 'translateY(-50%)',
              width: 6, height: 6, borderRadius: '50%', background: ZQ.blue,
            }} />
          )}
          <div style={{
            width: 28, height: 28, borderRadius: '50%', background: ZQ.bg,
            display: 'flex', alignItems: 'center', justifyContent: 'center',
            flexShrink: 0, fontSize: 11, color: ZQ.textSec, fontWeight: 600,
            border: `0.5px solid ${ZQ.border}`,
          }}>
            {m.senderLabel ? m.senderLabel[0] : '?'}
          </div>
          <div style={{ flex: 1, minWidth: 0 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 2 }}>
              <span style={{ fontSize: 12, color: ZQ.textSec, fontFamily: "'NanumSquare_ac', sans-serif" }}>{m.senderLabel || '익명'}</span>
              <span style={{ fontSize: 11, color: ZQ.textMute, fontFamily: "'NanumSquare_ac', sans-serif" }}>{m.createdAt ? formatTime(m.createdAt) : ''}</span>
            </div>
            <p style={{
              margin: 0, fontSize: 13,
              fontWeight: m.isRead ? 400 : 600,
              color: m.isRead ? ZQ.textSec : ZQ.text2,
              whiteSpace: 'nowrap', overflow: 'hidden', textOverflow: 'ellipsis',
              fontFamily: "'NanumSquare_ac', sans-serif",
            }}>{m.body}</p>
          </div>
        </div>
      ))}
      <button
        onClick={() => navigate('/messages/compose')}
        style={{
          position: 'fixed', bottom: 28, right: 20,
          background: '#888780', border: 'none', cursor: 'pointer',
          borderRadius: 12, padding: '8px 14px',
          display: 'flex', flexDirection: 'column', alignItems: 'center', gap: 2,
          boxShadow: '0 2px 12px rgba(0,0,0,0.2)',
        }}
      >
        <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="white" strokeWidth="2">
          <path d="M11 4H4a2 2 0 00-2 2v14a2 2 0 002 2h14a2 2 0 002-2v-7" />
          <path d="M18.5 2.5a2.1 2.1 0 013 3L12 15l-4 1 1-4z" />
        </svg>
        <span style={{ fontSize: 10, color: 'white', fontWeight: 600, fontFamily: "'NanumSquare_ac', sans-serif" }}>새 쪽지</span>
      </button>
    </div>
  )
}

function formatTime(createdAt) {
  const diff = Date.now() - new Date(createdAt).getTime()
  const mins = Math.floor(diff / 60000)
  if (mins < 60) return `${mins}분 전`
  if (mins < 1440) return `${Math.floor(mins / 60)}시간 전`
  const d = new Date(createdAt)
  const h = d.getHours(), m = d.getMinutes()
  return `${h >= 12 ? '오후' : '오전'} ${h > 12 ? h - 12 : h}:${String(m).padStart(2, '0')}`
}