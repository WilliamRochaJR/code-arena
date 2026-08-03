import { useMemo } from 'react'
import { createJavaQuizClient } from '@code-arena/java-quiz-sdk'

import App from '../App'
import { useAuthSession } from './AuthContext'

export function AuthenticatedApplication() {
  const session = useAuthSession()
  const client = useMemo(
    () =>
      createJavaQuizClient({
        baseUrl: import.meta.env.VITE_API_URL ?? window.location.origin,
        onUnauthorized: session.handleUnauthorized,
        tokenProvider: session.getAccessToken,
      }),
    [session.getAccessToken, session.handleUnauthorized],
  )

  if (session.isLoading) {
    return <AuthenticationStatus message="Restaurando sua sessão..." />
  }

  if (session.error) {
    return (
      <AuthenticationStatus
        error
        message="Não foi possível restaurar sua sessão."
        actionLabel="Tentar entrar novamente"
        onAction={session.signIn}
      />
    )
  }

  if (!session.isAuthenticated) {
    return (
      <AuthenticationStatus
        message="Entre com sua conta Google para acessar seus questionários."
        actionLabel="Entrar com Google"
        onAction={session.signIn}
      />
    )
  }

  return <App client={client} session={session} />
}

function AuthenticationStatus({
  actionLabel,
  error = false,
  message,
  onAction,
}: {
  actionLabel?: string
  error?: boolean
  message: string
  onAction?: () => Promise<void>
}) {
  return (
    <main className="auth-page">
      <section
        className={`status-card${error ? ' status-card--error' : ''}`}
        role={error ? 'alert' : 'status'}
      >
        <span
          className={actionLabel ? 'status-icon' : 'spinner'}
          aria-hidden="true"
        >
          {actionLabel ? '→' : ''}
        </span>
        <div>
          <strong>Code Arena</strong>
          <p>{message}</p>
          {actionLabel && onAction && (
            <button
              className="text-button"
              type="button"
              onClick={() => void onAction()}
            >
              {actionLabel}
            </button>
          )}
        </div>
      </section>
    </main>
  )
}
