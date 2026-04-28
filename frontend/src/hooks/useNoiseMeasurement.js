import { useEffect, useRef, useState, useCallback } from 'react'
import client from '../api/client'

export function useNoiseMeasurement() {
  const [db, setDb] = useState(null)
  const [status, setStatus] = useState('idle')

  const audioCtxRef = useRef(null)
  const streamRef = useRef(null)
  const rafIdRef = useRef(null)
  const intervalRef = useRef(null)
  const samplesRef = useRef([])

  const stop = useCallback(() => {
    cancelAnimationFrame(rafIdRef.current)
    clearInterval(intervalRef.current)
    streamRef.current?.getTracks().forEach((t) => t.stop())
    audioCtxRef.current?.close()
    audioCtxRef.current = null
    streamRef.current = null
    samplesRef.current = []
  }, [])

  useEffect(() => {
    let cancelled = false

    async function start() {
      try {
        const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
        if (cancelled) {
          stream.getTracks().forEach((t) => t.stop())
          return
        }
        streamRef.current = stream

        const ctx = new AudioContext()
        audioCtxRef.current = ctx
        const source = ctx.createMediaStreamSource(stream)
        const analyser = ctx.createAnalyser()
        analyser.fftSize = 2048
        source.connect(analyser)

        const buf = new Float32Array(analyser.fftSize)

        function sample() {
          if (cancelled) return
          analyser.getFloatTimeDomainData(buf)
          const rms = Math.sqrt(buf.reduce((s, v) => s + v * v, 0) / buf.length)
          // 94dBSPL 기준 보정 (브라우저 마이크 정규화된 신호 기준)
          const currentDb = rms > 0 ? 20 * Math.log10(rms) + 94 : 0
          samplesRef.current.push(currentDb)
          setDb(Math.round(currentDb))
          rafIdRef.current = requestAnimationFrame(sample)
        }
        sample()
        setStatus('measuring')

        // 30초마다 Leq 계산 후 서버 전송
        intervalRef.current = setInterval(async () => {
          const samples = samplesRef.current.splice(0)
          if (!samples.length) return

          // Leq = 10 * log10(mean(10^(dB/10)))
          const leq = 10 * Math.log10(
            samples.reduce((s, v) => s + Math.pow(10, v / 10), 0) / samples.length
          )

          try {
            await client.post('/noise/measurements', { db: +leq.toFixed(1) })
          } catch (_) {
            // 전송 실패는 무시 — 측정은 계속 진행
          }
        }, 30_000)
      } catch (err) {
        if (cancelled) return
        if (err.name === 'NotAllowedError' || err.name === 'PermissionDeniedError') {
          setStatus('denied')
        } else {
          setStatus('error')
        }
      }
    }

    start()

    return () => {
      cancelled = true
      stop()
    }
  }, [stop])

  return { db, status }
}