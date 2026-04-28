import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { useAuthStore } from '../../stores/authStore'
import { fetchSeats } from '../../api/seats'
import { fetchMessagesInbox } from '../../api/messages'
import { fetchNotificationsMe } from '../../api/notifications'
import HomeHeader from '../../components/layout/HomeHeader'
import SeatMap from '../../components/ui/SeatMap'
import { ZQ } from '../../utils/colors'
import { useNoiseMeasurement } from '../../hooks/useNoiseMeasurement'

const MOCK_SEATS = {
  S: Array.from({ length: 14 }, (_, i) => ({ seatNumber: i + 1, isOccupied: [1,3,4,6,7,8,10,11,14].includes(i+1) })),
  A: Array.from({ length: 14 }, (_, i) => ({ seatNumber: i + 1, isOccupied: [1,2,4,5,6,8,9,10,11,12].includes(i+1) })),
  B: Array.from({ length: 14 }, (_, i) => ({ seatNumber: i + 1, isOccupied: [2,3,5,6,7,9,11,12,13].includes(i+1) })),
  C: Array.from({ length: 14 }, (_, i) => ({ seatNumber: i + 1, isOccupied: [1,4,5,6,8,10,11,13,14].includes(i+1) })),
}

function normalizeSeatData(apiData) {
  if (!apiData) return MOCK_SEATS
  const grouped = { S: [], A: [], B: [], C: [] }
  for (const seat of apiData) {
    if (grouped[seat.zone]) grouped[seat.zone].push(seat)
  }
  const allEmpty = Object.values(grouped).every((arr) => arr.length === 0)
  return allEmpty ? MOCK_SEATS : grouped
}

function getNoiseColor(db) {
  if (db === null) return ZQ.text
  if (db < 40) return ZQ.S
  if (db < 50) return ZQ.A
  if (db < 60) return ZQ.B
  return ZQ.C
}

function getNoiseBg(db) {
  if (db === null) return ZQ.Sbg
  if (db < 40) return ZQ.Sbg
  if (db < 50) return ZQ.Abg
  if (db < 60) return ZQ.Bbg
  return ZQ.Cbg
}

function formatCountdown(assignedAt) {
  const MAX_MS = 5 * 60 * 60 * 1000
  const remaining = Math.max(0, MAX_MS - (Date.now() - new Date(assignedAt).getTime()))
  const h = Math.floor(remaining / 3_600_000)
  const m = Math.floor((remaining % 3_600_000) / 60_000)
  const s = Math.floor((remaining % 60_000) / 1_000)
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

export default function HomePage() {
  const user = useAuthStore((s) => s.user)
  const navigate = useNavigate()
  const { db, status } = useNoiseMeasurement()
  const [countdown, setCountdown] = useState('--:--:--')

  useEffect(() => {
    if (!user?.seat || !user?.seatAssignedAt) {
      setCountdown('--:--:--')
      return
    }
    setCountdown(formatCountdown(user.seatAssignedAt))
    const timer = setInterval(() => setCountdown(formatCountdown(user.seatAssignedAt)), 1_000)
    return () => clearInterval(timer)
  }, [user?.seat, user?.seatAssignedAt])

  const { data: seatData } = useQuery({
    queryKey: ['seats'],
    queryFn: fetchSeats,
    select: normalizeSeatData,
  })

  const { data: messages } = useQuery({
    queryKey: ['messages', 'inbox'],
    queryFn: fetchMessagesInbox,
  })

  const { data: notifications } = useQuery({
    queryKey: ['notifications'],
    queryFn: fetchNotificationsMe,
  })

  const seats = seatData || MOCK_SEATS
  const mySeat = user?.seat ? { zone: user.seat.zone, seatNumber: user.seat.seatNumber } : null
  const hasUnreadMessages = Array.isArray(messages) ? messages.some((m) => !m.isRead) : false
  const hasUnreadNotifications = notifications?.unreadCount > 0

  const handleSeatClick = (zone, seatNumber) => {
    navigate(`/messages/compose?zone=${zone}&seatNumber=${seatNumber}`)
  }

  const noiseColor = getNoiseColor(db)
  const noiseBg = getNoiseBg(db)

  return (
    <div style={{ background: '#fff', minHeight: '100vh' }}>
      <HomeHeader
        userName={user?.name || ''}
        onGoProfile={() => navigate('/profile')}
        onGoMessages={() => navigate('/messages')}
        onGoNotifications={() => navigate('/notifications')}
        hasUnreadMessages={hasUnreadMessages}
        hasUnreadNotifications={hasUnreadNotifications}
      />

      <div style={{ padding: '0 16px' }}>
        <div style={{ marginBottom: 16 }}>
          <div style={{ fontSize: 20, fontWeight: 800, color: ZQ.text, lineHeight: '32px', fontFamily: "'NanumSquare_ac', sans-serif" }}>
            오늘도 좋은 하루 되세요
          </div>
          <div style={{ fontSize: 14, color: ZQ.textMute, fontFamily: "'NanumSquare_ac', sans-serif" }}>
            {mySeat
              ? <>{`현재 이용 중인 좌석은 `}<strong style={{ color: ZQ.text2 }}>{mySeat.zone}-{mySeat.seatNumber}번</strong>{`입니다.`}</>
              : '현재 배정된 좌석이 없습니다.'}
          </div>
        </div>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 15, marginBottom: 16 }}>
          {/* 잔여 이용 시간 */}
          <div style={{ background: '#fff', border: `1px solid ${ZQ.border}`, borderRadius: 12, padding: 15, boxShadow: ZQ.shadow }}>
            <div style={{ fontSize: 10, color: '#1A1A1A', letterSpacing: 0.5, textTransform: 'uppercase', marginBottom: 5, fontFamily: "'NanumSquare_ac', sans-serif" }}>잔여 이용 시간</div>
            <div style={{ fontSize: 24, fontWeight: 800, color: ZQ.text, fontFamily: "'NanumSquare_ac', sans-serif" }}>{countdown}</div>
          </div>

          {/* 현재 내 소음 */}
          <div style={{ background: noiseBg, border: `1px solid ${ZQ.border}`, borderRadius: 12, padding: 15, boxShadow: ZQ.shadow }}>
            <div style={{ fontSize: 10, color: '#1A1A1A', letterSpacing: 0.5, textTransform: 'uppercase', marginBottom: 5, fontFamily: "'NanumSquare_ac', sans-serif" }}>현재 내 소음</div>
            {status === 'denied' ? (
              <div style={{ fontSize: 12, color: ZQ.textMute, fontFamily: "'NanumSquare_ac', sans-serif", lineHeight: 1.6 }}>마이크 권한 필요</div>
            ) : status === 'error' ? (
              <div style={{ fontSize: 12, color: ZQ.C, fontFamily: "'NanumSquare_ac', sans-serif" }}>측정 오류</div>
            ) : (
              <div style={{ fontSize: 24, fontWeight: 800, color: noiseColor, fontFamily: "'NanumSquare_ac', sans-serif" }}>
                {db !== null ? db : '--'} <span style={{ fontSize: 13, fontWeight: 400 }}>dB</span>
              </div>
            )}
          </div>
        </div>

        <div style={{ background: '#fff', border: `1px solid ${ZQ.border}`, borderRadius: 10, padding: '10px 10px 14px', marginBottom: 20, boxShadow: ZQ.shadow }}>
          <SeatMap seats={seats} mySeat={mySeat} mode="home" onSeatClick={handleSeatClick} />
        </div>
      </div>
    </div>
  )
}