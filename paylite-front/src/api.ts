import keycloak from './auth'

const PAYLITE_API = import.meta.env.VITE_PAYLITE_API
const CARD_BANK_API = import.meta.env.VITE_CARD_BANK_API

export type CardType = 'UZCARD' | 'HUMO'

export interface CardResponse {
  id: number
  pan: string
  type: CardType
  expireDate: string
  fullName: string
  phoneNumber: string
  status: string
  createdAt: string
}

export interface CommissionPreview {
  fromType: CardType
  toType: CardType
  amount: number
  commissionPercent: number
  commissionAmount: number
  totalAmount: number
}

export interface CardAccountResponse {
  pan: string
  balance: number
  status: string
}

async function request<T>(baseUrl: string, path: string, options: RequestInit = {}): Promise<T> {
  if (!keycloak.authenticated) {
    await keycloak.login()
    throw new Error('Login required.')
  }

  await keycloak.updateToken(30)

  const headers = new Headers(options.headers)
  headers.set('Authorization', `Bearer ${keycloak.token}`)
  headers.set('Accept', 'application/json')

  if (options.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json')
  }

  const response = await fetch(`${baseUrl}${path}`, {
    ...options,
    headers,
  })

  if (!response.ok) {
    const body = await response.text()
    let message = body || `HTTP ${response.status}`

    try {
      const problem = JSON.parse(body)
      message = problem.detail || problem.title || message
    } catch {
      // Keep the original response text.
    }

    throw new Error(message)
  }

  if (response.status === 204) {
    return undefined as T
  }

  return response.json() as Promise<T>
}

export const api = {
  createCard: (data: { fullName: string; pinfl: string; phoneNumber: string; type: CardType }) =>
    request<CardResponse>(CARD_BANK_API, '/api/cards', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  deposit: (pan: string, amount: number) =>
    request<CardAccountResponse>(CARD_BANK_API, `/api/cards/${encodeURIComponent(pan)}/deposit`, {
      method: 'POST',
      body: JSON.stringify({ amount }),
    }),

  withdraw: (pan: string, amount: number) =>
    request<CardAccountResponse>(CARD_BANK_API, `/api/cards/${encodeURIComponent(pan)}/withdraw`, {
      method: 'POST',
      body: JSON.stringify({ amount }),
    }),

  transfer: (data: { requestId: string; amount: number; fromPan: string; toPan: string }) =>
    request<unknown>(PAYLITE_API, '/api/p2p', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  previewCommission: (data: { fromPan: string; toPan: string; amount: number }) =>
    request<CommissionPreview>(PAYLITE_API, '/api/p2p/commission-preview', {
      method: 'POST',
      body: JSON.stringify(data),
    }),

  getCard: (pan: string) => request<CardResponse>(CARD_BANK_API, `/api/cards/${encodeURIComponent(pan)}`),

  getBalance: (pan: string) => request<CardAccountResponse>(CARD_BANK_API, `/api/cards/${encodeURIComponent(pan)}/balance`),
}
