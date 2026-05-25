import type { Metadata } from 'next'
import './globals.css'

export const metadata: Metadata = {
  title: 'Field Agents Tracking',
  description: 'Sistema de rastreamento de agentes de campo',
}

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="pt-BR">
      <body>{children}</body>
    </html>
  )
}
