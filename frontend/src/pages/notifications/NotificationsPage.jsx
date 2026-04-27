import { useNavigate } from 'react-router-dom'
import { useQuery, useQueryClient } from '@tanstack/react-query'
import { ZQ } from '../../utils/colors'
import BackHeader from '../../components/layout/BackHeader'
import { fetchNotificationsMe, markNotificationRead } from '../../api/notifications'

function groupByDate(items) {
  const today = new Date(); today.setHours(0, 0, 0, 0)
  const yesterday = new Date(today); yesterday.setDate(yesterday.getDate() - 1)
  const groups = {}
  for (const item of items) {
    const d = new Date(item.createdAt); d.setHours(0, 0, 0, 0)
    const label = d >= today ? '오늘' : d >= yesterday ? '어제'
      : `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`
    if (!groups[label]) groups[label] = []
    groups[label].push(item)
  }
  return groups
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

function TypeIcon({ type }) {
  if (type === 'NOISE_WARNING') return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#FF6B35" strokeWidth="2">
      <path d="M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z" />
      <line x1="12" y1="9" x2="12" y2="13" /><line x1="12" y1="17" x2="12.01" y2="17" />
    </svg>
  )
  if (type === 'GRADE_UPDATED') return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#16A57E" strokeWidth="2">
      <rect x="1" y="4" width="22" height="16" rx="2" />
      <line x1="1" y1="10" x2="23" y2="10" />
    </svg>
  )
  return (
    <svg width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#2783DE" strokeWidth="2">
      <circle cx="12" cy="12" r="10" />
      <polyline points="12 6 12 12 16 14" />
    </svg>
  )
}

function NotifRow({ n, onRead }) {
  return (
    <div
      onClick={() => onRead(n.id)}
      style={{
        position: 'relative', display: 'flex', alignItems: 'center', gap: 12,
        padding: '12px 16px 12px 22px', borderBottom: `0.5px solid ${ZQ.border}`,
        cursor: 'pointer', background: ZQ.card,
      }}
    >
      {!n.isRead && (
        <span style={{
          position: 'absolute', left: 8, top: '50%', transform: 'translateY(-50%)',
          width: 6, height: 6, borderRadius: '50%', background: ZQ.blue,
        }} />
      )}
      <div style={{
        width: 32, height: 32, borderRadius: '50%', background: '#f5f5f3',
        display: 'flex', alignItems: 'center', justifyContent: 'center', flexShrink: 0,
      }}>
        <TypeIcon type={n.type} />
      </div>
      <div style={{ flex: 1, minWidth: 0 }}>
        <div style={{ display: 'flex', alignItems: 'center' }}>
          <span style={{
            fontSize: 13, fontWeight: n.isRead ? 400 : 600, color: ZQ.text2,
            flex: 1, minWidth: 0, fontFamily: "'NanumSquare_ac', sans-serif",
          }}>{n.title}</span>
          <span style={{ fontSize: 11, color: ZQ.textMute, marginLeft: 8, flexShrink: 0, fontFamily: "'NanumSquare_ac', sans-serif" }}>
            {formatTime(n.createdAt)}
          </span>
        </div>
        <div style={{ fontSize: 12, color: ZQ.textSec, marginTop: 2, fontFamily: "'NanumSquare_ac', sans-serif" }}>{n.body}</div>
      </div>
    </div>
  )
}

export default function NotificationsPage() {
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const { data } = useQuery({ queryKey: ['notifications'], queryFn: fetchNotificationsMe })
  const items = Array.isArray(data) ? data : (data?.items ?? [])

  const markRead = (id) => {
    markNotificationRead(id).then(() => queryClient.invalidateQueries({ queryKey: ['notifications'] }))
  }

  const groups = groupByDate(items)

  return (
    <div style={{ background: ZQ.bg, minHeight: '100vh' }}>
      <BackHeader title="알림함" onBack={() => navigate(-1)} />
      {Object.entries(groups).map(([label, notifs]) => (
        <div key={label} style={{ background: ZQ.card, marginTop: 12 }}>
          <div style={{ padding: '10px 16px 4px', fontSize: 12, fontWeight: 600, color: ZQ.textSec, fontFamily: "'NanumSquare_ac', sans-serif" }}>
            {label}
          </div>
          {notifs.map(n => <NotifRow key={n.id} n={n} onRead={markRead} />)}
        </div>
      ))}
      {items.length === 0 && (
        <div style={{ textAlign: 'center', padding: '48px 16px', color: ZQ.textMute, fontSize: 14, fontFamily: "'NanumSquare_ac', sans-serif" }}>
          알림이 없습니다
        </div>
      )}
    </div>
  )
}