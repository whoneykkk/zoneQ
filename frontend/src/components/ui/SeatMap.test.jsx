import { render, screen, fireEvent } from '@testing-library/react'
import { describe, it, expect, vi } from 'vitest'
import SeatMap from './SeatMap'

const seats = {
  S: Array.from({ length: 14 }, (_, i) => ({ seatNumber: i + 1, isOccupied: i < 5 })),
  A: Array.from({ length: 14 }, (_, i) => ({ seatNumber: i + 1, isOccupied: i < 3 })),
  B: Array.from({ length: 14 }, (_, i) => ({ seatNumber: i + 1, isOccupied: false })),
  C: Array.from({ length: 14 }, (_, i) => ({ seatNumber: i + 1, isOccupied: false })),
}

describe('SeatMap', () => {
  it('renders all 4 zone headers', () => {
    render(<SeatMap seats={seats} mySeat={{ zone: 'S', seatNumber: 13 }} />)
    expect(screen.getByText('S ZONE')).toBeInTheDocument()
    expect(screen.getByText('A ZONE')).toBeInTheDocument()
    expect(screen.getByText('B ZONE')).toBeInTheDocument()
    expect(screen.getByText('C ZONE')).toBeInTheDocument()
  })

  it('calls onSeatClick with zone and seatNumber for occupied seats in home mode', () => {
    const handler = vi.fn()
    render(<SeatMap seats={seats} mySeat={{ zone: 'S', seatNumber: 13 }} mode="home" onSeatClick={handler} />)
    fireEvent.click(screen.getAllByText('사용중')[0])
    expect(handler).toHaveBeenCalledWith('S', 1)
  })
})
