import { useQuery } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { ZQ, ZONE_COLOR } from '../../utils/colors'
import { fetchDashboardStats, fetchDashboardRealtime } from '../../api/dashboard'
import BackHeader from '../../components/layout/BackHeader'

function StatCard({ label, value, unit }) {
  return (
    <div style={{ background: '#fff', border: `1px solid ${ZQ.border}`, borderRadius: 12, padding: '14px 16px', boxShadow: ZQ.shadow }}>
      <div style={{ fontSize: 10, color: ZQ.textMute, letterSpacing: 0.5, textTransform: 'uppercase', marginBottom: 6, fontFamily: 'NanumSquare_ac, sans-serif' }}>{label}</div>
      <div style={{ fontSize: 22, fontWeight: 800, color: ZQ.text, fontFamily: 'NanumSquare_ac, sans-serif' }}>
        {value ?? '--'}
        {unit && <span style={{ fontSize: 12, fontWeight: 400, marginLeft: 4 }}>{unit}</span>}
      </div>
    </div>
  )
}

function GradeBar({ grade, count, total }) {
  const pct = total > 0 ? Math.round((count / total) * 100) : 0
  return (
    <div style={{ display: 'flex', alignItems: 'center', gap: 10, marginBottom: 8 }}>
      <span style={{ width: 16, fontSize: 12, fontWeight: 700, color: ZONE_COLOR[grade], fontFamily: 'monospace' }}>{grade}</span>
      <div style={{ flex: 1, height: 8, background: '#EEEEEC', borderRadius: 9999, overflow: 'hidden' }}>
        <div style={{ width: `${pct}%`, height: '100%', background: ZONE_COLOR[grade], borderRadius: 9999 }} />
      </div>
      <span style={{ width: 32, fontSize: 11, color: ZQ.textMute, textAlign: 'right', fontFamily: 'monospace' }}>{count ?? 0}</span>
    </div>
  )
}

export default function DashboardPage() {
  const navigate = useNavigate()

  const { data: stats } = useQuery({
    queryKey: ['dashboard', 'stats'],
    queryFn: fetchDashboardStats,
    refetchInterval: 10_000,
  })

  const { data: realtime } = useQuery({
    queryKey: ['dashboard', 'realtime'],
    queryFn: fetchDashboardRealtime,
    refetchInterval: 4_000,
  })

  const gradeTotal = stats?.gradeDistribution
    ? Object.values(stats.gradeDistribution).reduce((s, v) => s + (v ?? 0), 0)
    : 0

  const realtimeSeats = realtime?.seats ?? []

  return (
    <div style={{ background: ZQ.bg, minHeight: '100vh' }}>
      <BackHeader title="관리자 대시보드" onBack={() => navigate('/')} />

      <div style={{ padding: '16px 15px', display: 'flex', flexDirection: 'column', gap: 16 }}>
        {/* 통계 카드 */}
        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 12 }}>
          <StatCard label="총 좌석" value={stats?.totalSeats} />
          <StatCard label="현재 재실" value={stats?.occupiedSeats} />
          <StatCard label="평균 소음" value={stats?.avgDb} unit="dB" />
          <StatCard label="경고 발생" value={stats?.warningCount} />
        </div>

        {/* 등급 분포 */}
        {stats?.gradeDistribution && (
          <div style={{ background: '#fff', border: `1px solid ${ZQ.border}`, borderRadius: 12, padding: '16px 20px', boxShadow: ZQ.shadow }}>
            <div style={{ fontSize: 12, fontWeight: 700, color: ZQ.text, letterSpacing: 1.2, textTransform: 'uppercase', marginBottom: 14, fontFamily: 'NanumSquare_ac, sans-serif' }}>등급 분포</div>
            {['S', 'A', 'B', 'C'].map((g) => (
              <GradeBar key={g} grade={g} count={stats.gradeDistribution[g]} total={gradeTotal} />
            ))}
          </div>
        )}

        {/* 실시간 좌석 dB */}
        <div style={{ background: '#fff', border: `1px solid ${ZQ.border}`, borderRadius: 12, padding: '16px 20px', boxShadow: ZQ.shadow }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 12 }}>
            <span style={{ fontSize: 12, fontWeight: 700, color: ZQ.text, letterSpacing: 1.2, textTransform: 'uppercase', fontFamily: 'NanumSquare_ac, sans-serif' }}>실시간 좌석 현황</span>
            <span style={{ fontSize: 10, color: ZQ.textMute, fontFamily: 'monospace' }}>4초 갱신</span>
          </div>

          {realtimeSeats.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '24px 0', color: ZQ.textMute, fontSize: 13, fontFamily: 'NanumSquare_ac, sans-serif' }}>데이터 없음</div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: 8 }}>
              {realtimeSeats.map((seat) => (
                <div key={seat.seatId} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '8px 12px', background: seat.isOccupied ? ZQ.bg : '#F9F9F9', borderRadius: 8, border: `1px solid ${ZQ.border}` }}>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    <span style={{ fontSize: 11, fontWeight: 700, color: ZONE_COLOR[seat.zone] ?? ZQ.textMute, fontFamily: 'monospace' }}>{seat.zone}</span>
                    <span style={{ fontSize: 13, fontWeight: 600, color: ZQ.text, fontFamily: 'NanumSquare_ac, sans-serif' }}>{seat.seatNumber}번</span>
                  </div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: 8 }}>
                    {seat.isOccupied ? (
                      <span style={{ fontSize: 14, fontWeight: 700, color: ZQ.text, fontFamily: 'monospace' }}>
                        {seat.currentDb != null ? `${seat.currentDb} dB` : '-- dB'}
                      </span>
                    ) : (
                      <span style={{ fontSize: 12, color: ZQ.textMute, fontFamily: 'NanumSquare_ac, sans-serif' }}>비어있음</span>
                    )}
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}