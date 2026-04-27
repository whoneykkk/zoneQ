import { useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { useQuery, useMutation } from '@tanstack/react-query'
import { ZQ } from '../../utils/colors'
import BackHeader from '../../components/layout/BackHeader'
import { fetchMessage, replyMessage } from '../../api/messages'

export default function MessageDetailPage() {
  const navigate = useNavigate()
  const { id } = useParams()
  const [reply, setReply] = useState('')
  const [sent, setSent] = useState(false)

  const { data: message, isLoading } = useQuery({
    queryKey: ['messages', id],
    queryFn: () => fetchMessage(id),
  })

  const mutation = useMutation({
    mutationFn: () => replyMessage(id, { body: reply }),
    onSuccess: () => setSent(true),
  })

  if (isLoading) return (
    <div style={{ background: ZQ.bg, minHeight: '100vh' }}>
      <BackHeader title="쪽지 상세" onBack={() => navigate('/messages')} />
    </div>
  )

  return (
    <div style={{ background: ZQ.bg, minHeight: '100vh' }}>
      <BackHeader title="쪽지 상세" onBack={() => navigate('/messages')} />

      <div style={{ background: '#fff', marginTop: 12 }}>
        <div style={{ padding: '12px 16px', borderBottom: `0.5px solid ${ZQ.border}`, display: 'flex', justifyContent: 'space-between' }}>
          <span style={{ fontSize: 13, fontWeight: 500, color: ZQ.textSec, fontFamily: "'NanumSquare_ac', sans-serif" }}>
            {message?.senderLabel || '익명'}
          </span>
          <span style={{ fontSize: 11, color: ZQ.textMute, fontFamily: "'NanumSquare_ac', sans-serif" }}>
            {message?.createdAt || ''}
          </span>
        </div>
        <div style={{ padding: 16, lineHeight: 1.7, fontSize: 14, color: ZQ.text2, fontFamily: "'NanumSquare_ac', sans-serif" }}>
          {message?.body}
        </div>
      </div>

      {message?.canReply ? (
        <div style={{ background: '#fff', marginTop: 12, padding: '14px 16px' }}>
          <div style={{ fontSize: 12, color: ZQ.textSec, marginBottom: 8, fontFamily: "'NanumSquare_ac', sans-serif" }}>답장</div>
          {sent ? (
            <div style={{ padding: 12, background: ZQ.Sbg, borderRadius: 10, textAlign: 'center', fontSize: 13, color: ZQ.S, fontWeight: 600, fontFamily: "'NanumSquare_ac', sans-serif" }}>
              ✓ 답장을 보냈습니다
            </div>
          ) : (
            <>
              <textarea
                value={reply}
                onChange={(e) => setReply(e.target.value)}
                placeholder="답장 내용을 입력하세요"
                rows={3}
                style={{
                  width: '100%', padding: '10px 12px', border: `1px solid ${ZQ.border}`,
                  borderRadius: 10, fontSize: 13, outline: 'none', resize: 'none',
                  fontFamily: "'NanumSquare_ac', sans-serif", boxSizing: 'border-box', color: ZQ.text2,
                }}
              />
              <div style={{ display: 'flex', justifyContent: 'flex-end', marginTop: 8 }}>
                <button
                  onClick={() => reply.trim() && mutation.mutate()}
                  style={{
                    padding: '8px 20px',
                    background: reply.trim() ? ZQ.blue : ZQ.textMute,
                    color: '#fff', border: 'none', borderRadius: 8,
                    fontSize: 13, fontWeight: 600, cursor: 'pointer',
                    fontFamily: "'NanumSquare_ac', sans-serif",
                  }}
                >답장 보내기</button>
              </div>
            </>
          )}
        </div>
      ) : (
        <div style={{ background: '#fff', marginTop: 12, padding: '14px 16px' }}>
          <div style={{ padding: '10px 14px', background: ZQ.bg, borderRadius: 10 }}>
            <span style={{ fontSize: 12, color: ZQ.textMute, fontFamily: "'NanumSquare_ac', sans-serif" }}>
              익명 쪽지는 답장할 수 없습니다.
            </span>
          </div>
        </div>
      )}
    </div>
  )
}