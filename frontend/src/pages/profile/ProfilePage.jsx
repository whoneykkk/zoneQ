import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '../../stores/authStore'
import BackHeader from '../../components/layout/BackHeader'
import GradeBadge from '../../components/ui/GradeBadge'
import { ZQ } from '../../utils/colors'

const MOCK_CHART_DATA = {
  days: ['05.10','05.11','05.12','05.13','05.14','05.15','오늘'],
  vals: [38, 36, 40, 35, 37, 34, 32],
}

const MOCK_CAUSES = [
  { label: '대화',   pct: 12 },
  { label: '키보드', pct: 64 },
  { label: '기침',   pct: 8  },
  { label: '기타',   pct: 16 },
]

const MOCK_VISITS = [
  { date: '2024년 5월 16일', avg: 28.4, grade: 'S' },
  { date: '2024년 5월 15일', avg: 28.4, grade: 'S' },
  { date: '2024년 5월 14일', avg: 31.2, grade: 'S' },
  { date: '2024년 5월 13일', avg: 29.8, grade: 'S' },
]

function NoiseChart({ days, vals }) {
  const W = 308, H = 90, padY = 8
  const minV = 28, maxV = 48
  const px = (i) => (i / (vals.length - 1)) * W
  const py = (v) => H - padY - ((v - minV) / (maxV - minV)) * (H - padY * 2)
  const d = vals.map((v, i) => `${i === 0 ? 'M' : 'L'}${px(i).toFixed(1)},${py(v).toFixed(1)}`).join(' ')
  const lastX = px(vals.length - 1)
  const lastY = py(vals[vals.length - 1])

  return (
    <div style={{ overflowX: 'auto' }}>
      <svg width={W} height={H + 24} style={{ display: 'block', margin: '0 auto' }}>
        <path d={d} fill="none" stroke={ZQ.blue} strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
        {vals.map((v, i) => <circle key={i} cx={px(i)} cy={py(v)} r="3" fill={ZQ.blue}/>)}
        <rect x={lastX - 9} y={lastY - 20} width={18} height={14} rx={3} fill={ZQ.S}/>
        <text x={lastX} y={lastY - 9} textAnchor="middle" fill="white" fontSize="9" fontWeight="700">S</text>
        {days.map((lb, i) => (
          <text key={i} x={px(i)} y={H + 18} textAnchor="middle" fontSize="10" fontFamily="monospace" fill={ZQ.textMute}>{lb}</text>
        ))}
      </svg>
    </div>
  )
}

function NoiseBar({ label, pct }) {
  return (
    <div style={{ marginBottom: 14 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 6 }}>
        <span style={{ fontSize: 12, color: ZQ.text2, fontFamily: 'NanumSquare_ac, sans-serif' }}>{label}</span>
        <span style={{ fontSize: 12, color: ZQ.text2, fontFamily: 'monospace', fontWeight: 500 }}>{pct}%</span>
      </div>
      <div style={{ height: 8, background: '#EEEEEC', borderRadius: 9999, overflow: 'hidden' }}>
        <div style={{ width: `${pct}%`, height: '100%', background: ZQ.chartBar, borderRadius: 9999 }} />
      </div>
    </div>
  )
}

export default function ProfilePage() {
  const { user } = useAuthStore()
  const navigate = useNavigate()

  return (
    <div style={{ background: ZQ.bg, minHeight: '100vh' }}>
      <BackHeader title="내 프로필" onBack={() => navigate('/')} />

      <div style={{ padding: '16px 15px', display: 'flex', flexDirection: 'column', gap: 20 }}>
        <div style={{ background: '#fff', border: `1px solid ${ZQ.border}`, borderRadius: 12, padding: 10, display: 'flex', flexDirection: 'row', alignItems: 'center', gap: 20, boxShadow: ZQ.shadow }}>
          <div style={{ position: 'relative', flexShrink: 0, width: 80, height: 80 }}>
            <div style={{ width: 80, height: 80, borderRadius: 9999, background: '#E8E8E6', border: '4px solid #fff', boxShadow: '0px 4px 6px -1px rgba(0,0,0,0.1)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <svg width="36" height="36" viewBox="0 0 24 24" fill="#A19E99">
                <circle cx="12" cy="8" r="4"/>
                <path d="M4 20c0-4 3.6-7 8-7s8 3 8 7"/>
              </svg>
            </div>
            <div style={{ position: 'absolute', bottom: -4, right: -4, background: ZQ.Sbg, border: '2px solid #fff', borderRadius: 9999, padding: '4px 12px', boxShadow: ZQ.shadow }}>
              <GradeBadge grade={user?.grade ?? null} size="sm" />
            </div>
          </div>
          <div style={{ display: 'flex', flexDirection: 'column', gap: 4 }}>
            <div style={{ fontSize: 24, fontWeight: 800, color: '#1A1C1B', fontFamily: 'NanumSquare_ac, sans-serif' }}>{user?.name}</div>
            <div style={{ fontSize: 16, color: ZQ.textSec, fontFamily: 'NanumSquare_ac, sans-serif' }}>{user?.email}</div>
          </div>
        </div>

        <div style={{ display: 'flex', gap: 16 }}>
          {[
            { label: '시간 연장', bg: ZQ.blue, color: '#fff', bordered: false, action: null },
            { label: '공지사항', bg: '#fff', color: ZQ.text, bordered: true, action: () => navigate('/notices') },
            { label: '건의함',   bg: '#fff', color: ZQ.text, bordered: true, action: null },
          ].map((item) => (
            <button key={item.label} onClick={item.action || undefined} style={{
              flex: 1, height: 70, borderRadius: 12, display: 'flex', flexDirection: 'column',
              alignItems: 'center', justifyContent: 'center', gap: 8,
              background: item.bg, border: item.bordered ? `1px solid ${ZQ.border}` : 'none',
              cursor: item.action ? 'pointer' : 'default',
            }}>
              <div style={{ width: 24, height: 24, background: 'rgba(0,0,0,0.08)', borderRadius: 4 }} />
              <span style={{ fontSize: 12, color: item.color, fontFamily: 'NanumSquare_ac, sans-serif', fontWeight: 400 }}>{item.label}</span>
            </button>
          ))}
        </div>

        <div style={{ background: '#fff', border: `1px solid ${ZQ.border}`, borderRadius: 12, padding: 25, boxShadow: ZQ.shadow }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
            <span style={{ fontSize: 14, fontWeight: 700, color: ZQ.text, letterSpacing: 1.4, textTransform: 'uppercase', fontFamily: 'NanumSquare_ac, sans-serif' }}>소음 등급 히스토리</span>
            <span style={{ fontSize: 12, color: ZQ.blue, background: '#E5F2FC', padding: '4px 10px', borderRadius: 4, fontFamily: 'NanumSquare_ac, sans-serif' }}>최근 7일</span>
          </div>
          <NoiseChart days={MOCK_CHART_DATA.days} vals={MOCK_CHART_DATA.vals} />
        </div>

        <div style={{ background: '#fff', border: `1px solid ${ZQ.border}`, borderRadius: 12, padding: 25, boxShadow: ZQ.shadow }}>
          <div style={{ fontSize: 14, fontWeight: 700, color: ZQ.text, letterSpacing: 1.4, textTransform: 'uppercase', marginBottom: 24, fontFamily: 'NanumSquare_ac, sans-serif' }}>소음 원인 분석</div>
          {MOCK_CAUSES.map((c) => <NoiseBar key={c.label} label={c.label} pct={c.pct} />)}
        </div>

        <div style={{ display: 'flex', flexDirection: 'column', gap: 10 }}>
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', padding: '5px 0', borderBottom: `1px solid ${ZQ.border}` }}>
            <span style={{ fontSize: 20, fontWeight: 700, color: '#1A1C1B', fontFamily: 'NanumSquare_ac, sans-serif' }}>방문 기록</span>
            <span style={{ fontSize: 12, color: ZQ.blue, cursor: 'pointer', fontFamily: 'NanumSquare_ac, sans-serif' }}>전체보기</span>
          </div>
          {MOCK_VISITS.map((v, i) => (
            <div key={i} style={{ background: '#fff', border: `1px solid ${ZQ.border}`, borderRadius: 8, padding: '10px 16px', display: 'flex', justifyContent: 'space-between', alignItems: 'center', boxShadow: ZQ.shadow }}>
              <span style={{ fontSize: 14, fontWeight: 800, color: '#1A1C1B', fontFamily: 'NanumSquare_ac, sans-serif' }}>{v.date}</span>
              <div style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
                <div>
                  <div style={{ fontSize: 10, fontWeight: 700, color: '#717783', textAlign: 'right', textTransform: 'uppercase', fontFamily: 'sans-serif' }}>AVERAGE</div>
                  <div style={{ fontSize: 14, fontWeight: 700, color: '#1A1C1B', textAlign: 'right', fontFamily: 'monospace' }}>{v.avg} <span style={{ fontSize: 10, fontWeight: 400 }}>dB</span></div>
                </div>
                <GradeBadge grade={v.grade} size="md" />
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  )
}
