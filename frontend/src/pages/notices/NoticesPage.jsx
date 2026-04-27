import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ZQ } from '../../utils/colors'
import BackHeader from '../../components/layout/BackHeader'
import { fetchNotices } from '../../api/notices'

export default function NoticesPage() {
  const navigate = useNavigate()
  const { data } = useQuery({ queryKey: ['notices'], queryFn: fetchNotices })
  const notices = Array.isArray(data) ? data : (data?.content ?? [])

  return (
    <div style={{ background: '#fff', minHeight: '100vh' }}>
      <BackHeader title="공지사항" onBack={() => navigate(-1)} />
      {notices.map((n, i) => (
        <div
          key={n.id}
          onClick={() => navigate(`/notices/${n.id}`)}
          style={{
            padding: '13px 16px',
            borderBottom: i < notices.length - 1 ? `0.5px solid ${ZQ.border}` : 'none',
            cursor: 'pointer',
            background: n.isPinned ? '#FAEEDA' : '#fff',
          }}
        >
          {n.isPinned ? (
            <>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 4 }}>
                <span style={{ fontSize: 10, color: '#BA7517', fontWeight: 600, fontFamily: "'NanumSquare_ac', sans-serif" }}>⭐ 고정</span>
                <span style={{ fontSize: 10, color: '#BA7517', fontFamily: "'NanumSquare_ac', sans-serif" }}>{formatDate(n.createdAt)}</span>
              </div>
              <div style={{ fontSize: 13, fontWeight: 600, color: '#633806', marginBottom: 3, fontFamily: "'NanumSquare_ac', sans-serif" }}>{n.title}</div>
              <div style={{ fontSize: 12, color: '#854F0B', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', fontFamily: "'NanumSquare_ac', sans-serif" }}>
                {n.preview || (n.body || '').slice(0, 80)}
              </div>
            </>
          ) : (
            <>
              <div style={{ fontSize: 10, color: ZQ.textMute, textAlign: 'right', marginBottom: 4, fontFamily: "'NanumSquare_ac', sans-serif" }}>{formatDate(n.createdAt)}</div>
              <div style={{ fontSize: 13, fontWeight: 600, color: ZQ.text2, marginBottom: 3, fontFamily: "'NanumSquare_ac', sans-serif" }}>{n.title}</div>
              <div style={{ fontSize: 12, color: ZQ.textSec, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', fontFamily: "'NanumSquare_ac', sans-serif" }}>
                {n.preview || (n.body || '').slice(0, 80)}
              </div>
            </>
          )}
        </div>
      ))}
    </div>
  )
}

function formatDate(createdAt) {
  if (!createdAt) return ''
  const d = new Date(createdAt)
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`
}