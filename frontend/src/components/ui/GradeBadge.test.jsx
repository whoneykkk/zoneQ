import { render, screen } from '@testing-library/react'
import { describe, it, expect } from 'vitest'
import GradeBadge from './GradeBadge'

describe('GradeBadge', () => {
  it('renders grade S', () => {
    render(<GradeBadge grade="S" />)
    expect(screen.getByText('GRADE S')).toBeInTheDocument()
  })

  it('renders grade C', () => {
    render(<GradeBadge grade="C" />)
    expect(screen.getByText('GRADE C')).toBeInTheDocument()
  })

  it('renders 미부여 when grade is null', () => {
    render(<GradeBadge grade={null} />)
    expect(screen.getByText('미부여')).toBeInTheDocument()
  })
})
