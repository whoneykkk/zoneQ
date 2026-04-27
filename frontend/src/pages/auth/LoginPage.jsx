import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '../../stores/authStore'
import { fetchLogin } from '../../api/auth'
import { fetchProfileMe } from '../../api/profile'
import { ZQ } from '../../utils/colors'

export default function LoginPage() {
  const [email, setEmail] = useState('')
  const [pw, setPw] = useState('')
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const { setAuth, setUser } = useAuthStore()
  const navigate = useNavigate()
  const ok = email.length > 0 && pw.length > 0

  const handleLogin = async () => {
    if (!ok || loading) return
    setLoading(true)
    setError('')
    try {
      const { accessToken, refreshToken } = await fetchLogin(email, pw)
      setAuth(accessToken, null, refreshToken)
      const profile = await fetchProfileMe()
      setUser(profile)
      navigate('/')
    } catch (e) {
      setError(e.response?.data?.message || '로그인에 실패했습니다.')
    } finally {
      setLoading(false)
    }
  }

  const handleKeyDown = (e) => { if (e.key === 'Enter') handleLogin() }

  return (
    <div style={{ height: '100%', minHeight: '100vh', display: 'flex', flexDirection: 'column', justifyContent: 'center', padding: '0 32px', background: '#fff' }}>
      <div style={{ textAlign: 'center', marginBottom: 48 }}>
        <div style={{ width: 64, height: 64, borderRadius: 16, background: '#1a1c1b', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 16px' }}>
          <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
            <rect x="4" y="4" width="10" height="10" rx="2" fill="white"/>
            <rect x="18" y="4" width="10" height="10" rx="2" fill="white"/>
            <rect x="4" y="18" width="10" height="10" rx="2" fill="white"/>
            <rect x="18" y="18" width="10" height="10" rx="2" fill="white"/>
          </svg>
        </div>
        <div style={{ fontSize: 24, fontWeight: 800, color: ZQ.text2, letterSpacing: -0.5, fontFamily: "'NanumSquare_ac', sans-serif" }}>ZoneQ</div>
        <div style={{ fontSize: 13, color: ZQ.textSec, marginTop: 4, fontFamily: "'NanumSquare_ac', sans-serif" }}>스터디카페 소음 관리 시스템</div>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: 12 }}>
        <div>
          <div style={{ fontSize: 12, color: ZQ.textSec, marginBottom: 6, fontFamily: "'NanumSquare_ac', sans-serif" }}>이메일</div>
          <input
            type="email" value={email} onChange={(e) => setEmail(e.target.value)} onKeyDown={handleKeyDown}
            placeholder="name@example.com"
            style={{ width: '100%', padding: '12px 14px', border: `1px solid ${ZQ.border}`, borderRadius: 10, fontSize: 14, outline: 'none', boxSizing: 'border-box', color: ZQ.text2, fontFamily: "'NanumSquare_ac', sans-serif" }}
          />
        </div>
        <div>
          <div style={{ fontSize: 12, color: ZQ.textSec, marginBottom: 6, fontFamily: "'NanumSquare_ac', sans-serif" }}>비밀번호</div>
          <input
            type="password" value={pw} onChange={(e) => setPw(e.target.value)} onKeyDown={handleKeyDown}
            placeholder="••••••••"
            style={{ width: '100%', padding: '12px 14px', border: `1px solid ${ZQ.border}`, borderRadius: 10, fontSize: 14, outline: 'none', boxSizing: 'border-box', color: ZQ.text2, fontFamily: "'NanumSquare_ac', sans-serif" }}
          />
        </div>

        {error && (
          <div style={{ fontSize: 12, color: ZQ.C, fontFamily: "'NanumSquare_ac', sans-serif" }}>{error}</div>
        )}

        <button onClick={handleLogin} disabled={!ok || loading} style={{
          marginTop: 8, padding: '14px', borderRadius: 12,
          background: ok && !loading ? '#1A1C1B' : '#ccc',
          color: '#fff', border: 'none', fontSize: 15, fontWeight: 800,
          cursor: ok && !loading ? 'pointer' : 'default',
          fontFamily: "'NanumSquare_ac', sans-serif",
        }}>
          {loading ? '로그인 중...' : '로그인'}
        </button>
      </div>

      <div style={{ textAlign: 'center', marginTop: 24, fontSize: 13, color: ZQ.textSec, fontFamily: "'NanumSquare_ac', sans-serif" }}>
        계정이 없으신가요? <span style={{ color: ZQ.blue, cursor: 'pointer', fontWeight: 500 }}>회원가입</span>
      </div>
    </div>
  )
}
