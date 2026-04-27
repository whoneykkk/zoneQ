import { useNavigate, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { ZQ } from '../../utils/colors'
import BackHeader from '../../components/layout/BackHeader'
import { fetchNotice } from '../../api/notices'

function formatDate(createdAt) {
  if (!createdAt) return ''
  const d = new Date(createdAt)
  return `${d.getFullYear()}.${String(d.getMonth() + 1).padStart(2, '0')}.${String(d.getDate()).padStart(2, '0')}`
}

export default function NoticeDetailPage() {
  const navigate = useNavigate()
  const { id } = useParams()
  const { data: notice, isLoading } = useQuery({
    queryKey: ['notices', id],
    queryFn: () => fetchNotice(id),
  })

  if (isLoading) return (
    <div style={{ background: ZQ.bg, minHeight: '100vh' }}>
      <BackHeader title="공지사항" onBack={() => navigate('/notices')} />
    </div>
  )

  return (
    <div style={{ background: ZQ.bg, minHeight: '100vh' }}>
      <BackHeader title="공지사항" onBack={() => navigate('/notices')} />
      <div style={{ background: '#fff', margin: '12px 0' }}>
        <div style={{ padding: 16, borderBottom: `0.5px solid ${ZQ.border}` }}>
          {notice?.isPinned && (
            <div style={{ marginBottom: 8 }}>
              <span style={{ fontSize: 11, color: '#854F0B', fontWeight: 600, fontFamily: "'NanumSquare_ac', sans-serif" }}>⭐ 고정 공지</span>
            </div>
          )}
          <div style={{ fontSize: 17, fontWeight: 600, color: ZQ.text2, marginBottom: 6, lineHeight: 1.4, fontFamily: "'NanumSquare_ac', sans-serif" }}>
            {notice?.title}
          </div>
          <span style={{ fontSize: 11, color: ZQ.textMute, fontFamily: "'NanumSquare_ac', sans-serif" }}>
            관리자 · {formatDate(notice?.createdAt)}
          </span>
        </div>
        <div style={{ padding: 16, fontSize: 14, color: ZQ.text2, lineHeight: 1.8, whiteSpace: 'pre-line', fontFamily: "'NanumSquare_ac', sans-serif" }}>
          {notice?.body}
        </div>
      </div>
    </div>
  )
}