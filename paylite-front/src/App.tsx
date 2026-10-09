import type { FormEvent, ReactNode } from 'react'
import { useEffect, useState } from 'react'
import { Link, Navigate, Route, Routes, useNavigate } from 'react-router-dom'
import type { CardResponse, CardType } from './api'
import { api } from './api'
import keycloak from './auth'

function Layout({ children }: { children: ReactNode }) {
  return (
    <div className='app-shell'>
      <header className='topbar'>
        <Link
          to='/'
          className='brand'
        >
          PayLite
        </Link>

        <div className='user-area'>
          <span>{String(keycloak.tokenParsed?.preferred_username ?? 'User')}</span>

          <button
            className='secondary'
            onClick={() =>
              void keycloak.logout({
                redirectUri: window.location.origin,
              })
            }
          >
            Log out
          </button>
        </div>
      </header>

      <main>{children}</main>
    </div>
  )
}

function PageTitle({ title, subtitle }: { title: string; subtitle: string }) {
  const navigate = useNavigate()

  return (
    <section className='page-heading'>
      <button
        type='button'
        className='back-button'
        onClick={() => navigate('/')}
      >
        ← Dashboard
      </button>

      <h1>{title}</h1>
      <p>{subtitle}</p>
    </section>
  )
}

function Feedback({ message, error = false }: { message: string; error?: boolean }) {
  if (!message) return null

  return <div className={error ? 'feedback error' : 'feedback success'}>{message}</div>
}

function Dashboard() {
  const actions = [
    {
      title: 'Create card',
      description: 'Issue a UZCARD or HUMO card',
      path: '/cards/new',
      icon: '💳',
    },
    {
      title: 'Card Information',
      description: 'View card details and live balance',
      path: '/cards/info',
      icon: '💳',
    },
    {
      title: 'Deposit',
      description: 'Add funds to a card',
      path: '/deposit',
      icon: '💰',
    },
    {
      title: 'Withdraw',
      description: 'Withdraw funds from a card',
      path: '/withdraw',
      icon: '↗',
    },
    {
      title: 'P2P transfer',
      description: 'Transfer money between cards',
      path: '/p2p',
      icon: '⇄',
    },
  ]

  return (
    <Layout>
      <section className='page-heading'>
        <p className='eyebrow'>PAYLITE / DASHBOARD</p>
        <h1>Welcome to PayLite</h1>
        <p>Manage your cards and transfer money.</p>
      </section>

      <section className='action-grid'>
        {actions.map((action) => (
          <Link
            className='action-card'
            key={action.path}
            to={action.path}
          >
            <span className='action-icon'>{action.icon}</span>
            <h2>{action.title}</h2>
            <p>{action.description}</p>
            <span className='action-link'>Open →</span>
          </Link>
        ))}
      </section>
    </Layout>
  )
}

function CreateCardPage() {
  const [fullName, setFullName] = useState('')
  const [pinfl, setPinfl] = useState('')
  const [phoneNumber, setPhoneNumber] = useState('')
  const [type, setType] = useState<CardType>('UZCARD')
  const [result, setResult] = useState<CardResponse | null>(null)
  const [message, setMessage] = useState('')
  const [error, setError] = useState(false)
  const [loading, setLoading] = useState(false)

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setLoading(true)
    setMessage('')
    setResult(null)
    setError(false)

    try {
      const card = await api.createCard({
        fullName,
        pinfl,
        phoneNumber,
        type,
      })

      setResult(card)
      setMessage('Card created successfully.')
    } catch (e) {
      setError(true)
      setMessage(e instanceof Error ? e.message : 'Card creation failed.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Layout>
      <PageTitle
        title='Create card'
        subtitle='Create a new UZCARD or HUMO card.'
      />

      <form
        className='panel form'
        onSubmit={submit}
      >
        <label>
          Full name
          <input
            required
            value={fullName}
            onChange={(e) => setFullName(e.target.value)}
            placeholder='Test Humo 2'
          />
        </label>

        <label>
          PINFL
          <input
            required
            minLength={14}
            maxLength={14}
            pattern='[0-9]{14}'
            value={pinfl}
            onChange={(e) => setPinfl(e.target.value)}
            placeholder='12345678901203'
          />
        </label>

        <label>
          Phone number
          <input
            required
            value={phoneNumber}
            onChange={(e) => setPhoneNumber(e.target.value)}
            placeholder='+998901234503'
          />
        </label>

        <label>
          Card type
          <select
            value={type}
            onChange={(e) => setType(e.target.value as CardType)}
          >
            <option value='UZCARD'>UZCARD</option>
            <option value='HUMO'>HUMO</option>
          </select>
        </label>

        <button disabled={loading}>{loading ? 'Creating...' : 'Create card'}</button>

        <Feedback
          message={message}
          error={error}
        />

        {result && (
          <div className='result-card'>
            <h3>Card created</h3>
            <p>
              <strong>PAN:</strong> {result.pan}
            </p>
            <p>
              <strong>Type:</strong> {result.type}
            </p>
            <p>
              <strong>Owner:</strong> {result.fullName}
            </p>
            <p>
              <strong>Expires:</strong> {result.expireDate}
            </p>
            <p>
              <strong>Status:</strong> {result.status}
            </p>
          </div>
        )}
      </form>
    </Layout>
  )
}

function BalancePage({ mode }: { mode: 'deposit' | 'withdraw' }) {
  const [pan, setPan] = useState('')
  const [amountSom, setAmountSom] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState(false)
  const [loading, setLoading] = useState(false)
  const [balance, setBalance] = useState<number | null>(null)

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const amount = Number(amountSom)

    if (!Number.isSafeInteger(amount) || amount <= 0) {
      setError(true)
      setMessage("Enter a positive whole amount in so'm.")
      return
    }

    setLoading(true)
    setMessage('')
    setBalance(null)
    setError(false)

    try {
      // Backend stores amounts in tiyin: 1 so'm = 100 tiyin.
      const amountTiyin = amount * 100

      const result = mode === 'deposit' ? await api.deposit(pan.trim(), amountTiyin) : await api.withdraw(pan.trim(), amountTiyin)

      setBalance(result.balance)
      setMessage(mode === 'deposit' ? 'Deposit successful.' : 'Withdrawal successful.')
    } catch (e) {
      setError(true)
      setMessage(e instanceof Error ? e.message : 'Operation failed.')
    } finally {
      setLoading(false)
    }
  }

  return (
    <Layout>
      <PageTitle
        title={mode === 'deposit' ? 'Deposit' : 'Withdraw'}
        subtitle='Enter the card PAN and amount.'
      />

      <form
        className='panel form'
        onSubmit={submit}
      >
        <label>
          Card PAN
          <input
            required
            minLength={16}
            maxLength={16}
            pattern='[0-9]{16}'
            value={pan}
            onChange={(e) => setPan(e.target.value)}
            placeholder='16-digit card number'
          />
        </label>

        <label>
          Amount (so'm)
          <input
            required
            type='number'
            min='1'
            step='1'
            value={amountSom}
            onChange={(e) => setAmountSom(e.target.value)}
            placeholder='100000'
          />
        </label>

        <p className='hint'>Amounts are converted to tiyin before the request is sent.</p>

        <button disabled={loading}>{loading ? 'Processing...' : mode === 'deposit' ? 'Deposit funds' : 'Withdraw funds'}</button>

        <Feedback
          message={message}
          error={error}
        />

        {balance !== null && <p className='balance'>Returned balance: {(balance / 100).toLocaleString()} so'm</p>}
      </form>
    </Layout>
  )
}

function P2PPage() {
  const [fromPan, setFromPan] = useState('')
  const [toPan, setToPan] = useState('')
  const [amountSom, setAmountSom] = useState('')
  const [preview, setPreview] = useState<import('./api').CommissionPreview | null>(null)
  const [previewError, setPreviewError] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState(false)
  const [loading, setLoading] = useState(false)

  const amount = Number(amountSom)
  const validAmount = Number.isSafeInteger(amount) && amount > 0 && Number.isSafeInteger(amount * 100)

  useEffect(() => {
    setPreview(null)
    setPreviewError('')

    const sender = fromPan.trim()
    const recipient = toPan.trim()

    if (!/^\d{16}$/.test(sender) || !/^\d{16}$/.test(recipient) || sender === recipient || !validAmount) {
      return
    }

    let cancelled = false

    const timer = window.setTimeout(async () => {
      try {
        const result = await api.previewCommission({
          fromPan: sender,
          toPan: recipient,
          amount: amount * 100,
        })

        if (!cancelled) {
          setPreview(result)
          setPreviewError('')
        }
      } catch (e) {
        if (!cancelled) {
          setPreview(null)
          setPreviewError(e instanceof Error ? e.message : 'Unable to calculate commission.')
        }
      }
    }, 400)

    return () => {
      cancelled = true
      window.clearTimeout(timer)
    }
  }, [fromPan, toPan, amountSom, validAmount, amount])

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!validAmount) {
      setError(true)
      setMessage("Enter a positive whole amount in so'm.")
      return
    }

    if (fromPan.trim() === toPan.trim()) {
      setError(true)
      setMessage('Sender and recipient must be different cards.')
      return
    }

    setLoading(true)
    setMessage('')
    setError(false)

    try {
      const result = await api.transfer({
        requestId: crypto.randomUUID(),
        amount: amount * 100,
        fromPan: fromPan.trim(),
        toPan: toPan.trim(),
      })

      setMessage('Transfer request completed. Check the result below.')
      setError(false)
      setTransferResult(result)
    } catch (e) {
      setError(true)
      setMessage(e instanceof Error ? e.message : 'P2P transfer failed.')
    } finally {
      setLoading(false)
    }
  }

  const [transferResult, setTransferResult] = useState<unknown>(null)

  return (
    <Layout>
      <PageTitle
        title='P2P transfer'
        subtitle='Review the commission before sending money.'
      />

      <form
        className='panel form'
        onSubmit={submit}
      >
        <label>
          Sender PAN
          <input
            required
            minLength={16}
            maxLength={16}
            pattern='[0-9]{16}'
            value={fromPan}
            onChange={(e) => setFromPan(e.target.value)}
          />
        </label>

        <label>
          Recipient PAN
          <input
            required
            minLength={16}
            maxLength={16}
            pattern='[0-9]{16}'
            value={toPan}
            onChange={(e) => setToPan(e.target.value)}
          />
        </label>

        <label>
          Amount (so'm)
          <input
            required
            type='number'
            min='1'
            step='1'
            value={amountSom}
            onChange={(e) => setAmountSom(e.target.value)}
          />
        </label>

        {preview && (
          <div className='commission-preview'>
            <h3>Transfer summary</h3>

            <div className='summary-row'>
              <span>From</span>
              <strong>{preview.fromType}</strong>
            </div>

            <div className='summary-row'>
              <span>To</span>
              <strong>{preview.toType}</strong>
            </div>

            <div className='summary-row'>
              <span>Transfer amount</span>
              <strong>{(preview.amount / 100).toLocaleString()} so'm</strong>
            </div>

            <div className='summary-row'>
              <span>Commission rate</span>
              <strong>{preview.commissionPercent}%</strong>
            </div>

            <div className='summary-row'>
              <span>Commission</span>
              <strong>{(preview.commissionAmount / 100).toLocaleString()} so'm</strong>
            </div>

            <div className='summary-row summary-total'>
              <span>Total deducted</span>
              <strong>{(preview.totalAmount / 100).toLocaleString()} so'm</strong>
            </div>
          </div>
        )}

        {previewError && (
          <Feedback
            message={previewError}
            error
          />
        )}

        <p className='hint'>The preview does not move money. The commission is calculated again by PayLite when you submit the transfer.</p>

        <button disabled={loading || !preview}>{loading ? 'Transferring...' : 'Confirm transfer'}</button>

        <Feedback
          message={message}
          error={error}
        />

        {transferResult !== null && <pre className='json-result'>{JSON.stringify(transferResult, null, 2)}</pre>}
      </form>
    </Layout>
  )
}

function CardInfoPage() {
  const [pan, setPan] = useState('')
  const [card, setCard] = useState<CardResponse | null>(null)
  const [balance, setBalance] = useState<number | null>(null)
  const [accountStatus, setAccountStatus] = useState('')
  const [message, setMessage] = useState('')
  const [error, setError] = useState(false)
  const [loading, setLoading] = useState(false)

  async function submit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const normalizedPan = pan.trim()

    if (!/^\d{16}$/.test(normalizedPan)) {
      setError(true)
      setMessage('Enter a valid 16-digit card PAN.')
      return
    }

    setLoading(true)
    setMessage('')
    setCard(null)
    setBalance(null)
    setAccountStatus('')
    setError(false)

    try {
      // Load metadata and live balance independently.
      const cardInfo = await api.getCard(normalizedPan)
      const account = await api.getBalance(normalizedPan)

      setCard(cardInfo)
      setBalance(account.balance)
      setAccountStatus(account.status)
      setMessage('Card information loaded successfully.')
    } catch (e) {
      setError(true)
      setMessage(e instanceof Error ? e.message : 'Unable to retrieve card information.')
    } finally {
      setLoading(false)
    }
  }

  const maskedPan = card ? `${card.pan.slice(0, 4)} **** **** ${card.pan.slice(-4)}` : ''

  return (
    <Layout>
      <PageTitle
        title='Card Information'
        subtitle='Find a card and view its details and current balance.'
      />

      <form
        className='panel form'
        onSubmit={submit}
      >
        <label>
          Card PAN
          <input
            required
            minLength={16}
            maxLength={16}
            pattern='[0-9]{16}'
            inputMode='numeric'
            value={pan}
            onChange={(e) => setPan(e.target.value)}
            placeholder='Enter the 16-digit card number'
          />
        </label>

        <button disabled={loading}>{loading ? 'Loading card...' : 'Get card information'}</button>

        <Feedback
          message={message}
          error={error}
        />
      </form>

      {card && balance !== null && (
        <section className='card-details'>
          <div className={`plastic-card ${card.type.toLowerCase()}`}>
            <div className='plastic-card-top'>
              <span className='plastic-brand'>PAYLITE</span>
              <span className='plastic-type'>{card.type}</span>
            </div>

            <div
              className='chip'
              aria-label='Card chip'
            >
              <span />
              <span />
              <span />
            </div>

            <div className='plastic-pan'>{maskedPan}</div>

            <div className='plastic-card-bottom'>
              <div>
                <span className='plastic-label'>CARDHOLDER</span>
                <strong>{card.fullName}</strong>
              </div>

              <div>
                <span className='plastic-label'>EXPIRES</span>
                <strong>{card.expireDate}</strong>
              </div>
            </div>
          </div>

          <div className='panel card-info-panel'>
            <h2>Card details</h2>

            <div className='summary-row'>
              <span>Card number</span>
              <strong>{maskedPan}</strong>
            </div>

            <div className='summary-row'>
              <span>Card type</span>
              <strong>{card.type}</strong>
            </div>

            <div className='summary-row'>
              <span>Cardholder</span>
              <strong>{card.fullName}</strong>
            </div>

            <div className='summary-row'>
              <span>Expiry date</span>
              <strong>{card.expireDate}</strong>
            </div>

            <div className='summary-row'>
              <span>Card status</span>
              <strong>{card.status}</strong>
            </div>

            <div className='summary-row'>
              <span>Account status</span>
              <strong>{accountStatus}</strong>
            </div>

            <div className='balance-panel'>
              <span>Current balance</span>
              <strong>
                {(balance / 100).toLocaleString('en-US', {
                  minimumFractionDigits: 2,
                  maximumFractionDigits: 2,
                })}{' '}
                so'm
              </strong>
              <small>Live balance · displayed in so'm</small>
            </div>
          </div>
        </section>
      )}
    </Layout>
  )
}

export default function App() {
  return (
    <Routes>
      <Route
        path='/'
        element={<Dashboard />}
      />
      <Route
        path='/cards/new'
        element={<CreateCardPage />}
      />
      <Route
        path='/deposit'
        element={<BalancePage mode='deposit' />}
      />
      <Route
        path='/withdraw'
        element={<BalancePage mode='withdraw' />}
      />
      <Route
        path='/p2p'
        element={<P2PPage />}
      />
      <Route
        path='/cards/info'
        element={<CardInfoPage />}
      />
      <Route
        path='*'
        element={
          <Navigate
            to='/'
            replace
          />
        }
      />
    </Routes>
  )
}
