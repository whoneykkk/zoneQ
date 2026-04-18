import { ZQ, ZONE_COLOR, ZONE_BG } from '../../utils/colors'

const SIZE = {
  sm: { fontSize: 9,  padding: '1px 7px' },
  md: { fontSize: 10, padding: '4px 12px' },
  lg: { fontSize: 12, padding: '4px 12px' },
}

export default function GradeBadge({ grade, size = 'md' }) {
  const s = SIZE[size] || SIZE.md
  const bg = grade ? ZONE_BG[grade] : ZQ.bg
  const color = grade ? ZONE_COLOR[grade] : ZQ.textMute

  return (
    <span style={{
      display: 'inline-flex', alignItems: 'center', justifyContent: 'center',
      background: bg, color,
      borderRadius: 9999, fontFamily: 'Manrope, sans-serif', fontWeight: 800,
      fontSize: s.fontSize, padding: s.padding, letterSpacing: 0.2, whiteSpace: 'nowrap',
    }}>
      {grade ? `GRADE ${grade}` : '미부여'}
    </span>
  )
}
