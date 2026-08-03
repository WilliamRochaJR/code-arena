import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import type {
  JavaQuizClient,
  QuizAttemptStatus,
} from '@code-arena/java-quiz-sdk'

export function QuizAttemptHistoryPage({ client }: { client: JavaQuizClient }) {
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()
  const pageParam = Number(searchParams.get('page') ?? 0)
  const page = Number.isInteger(pageParam) && pageParam >= 0 ? pageParam : 0
  const statusParam = searchParams.get('status')
  const status: QuizAttemptStatus | undefined =
    statusParam === 'IN_PROGRESS' || statusParam === 'COMPLETED'
      ? statusParam
      : undefined
  const [continuingId, setContinuingId] = useState<string | null>(null)
  const [continueError, setContinueError] = useState<string | null>(null)
  const history = useQuery({
    queryKey: ['quiz-attempts', page, status],
    queryFn: ({ signal }) =>
      client.attempts.list({
        page,
        size: 10,
        ...(status ? { status } : {}),
        signal,
      }),
  })

  function changeStatus(next?: QuizAttemptStatus) {
    const params = new URLSearchParams()
    if (next) params.set('status', next)
    params.set('page', '0')
    setSearchParams(params, { replace: true })
  }

  function changePage(next: number) {
    const params = new URLSearchParams(searchParams)
    params.set('page', String(next))
    setSearchParams(params)
  }

  async function continueAttempt(attemptId: string) {
    setContinuingId(attemptId)
    setContinueError(null)
    try {
      const details = await client.attempts.get(attemptId)
      const question =
        details.questions.find((item) => item.selectedAlternativeId === null) ??
        details.questions[0]
      if (!question) throw new Error('Attempt has no questions.')
      navigate(`/quiz-attempts/${attemptId}/questions/${question.position}`)
    } catch {
      setContinueError(attemptId)
    } finally {
      setContinuingId(null)
    }
  }

  return (
    <main className="main-content history-content">
      <section className="intro">
        <span className="eyebrow">Sua evolução</span>
        <h1>Minhas tentativas</h1>
        <p>
          Continue um treino em andamento ou revise os resultados já concluídos.
        </p>
      </section>
      <section className="history-panel" aria-labelledby="history-title">
        <div className="history-heading">
          <h2 id="history-title">Histórico</h2>
          <div className="history-filters" aria-label="Filtrar tentativas">
            <button
              className={
                !status
                  ? 'history-filter history-filter--active'
                  : 'history-filter'
              }
              type="button"
              onClick={() => changeStatus()}
            >
              Todas
            </button>
            <button
              className={
                status === 'IN_PROGRESS'
                  ? 'history-filter history-filter--active'
                  : 'history-filter'
              }
              type="button"
              onClick={() => changeStatus('IN_PROGRESS')}
            >
              Em andamento
            </button>
            <button
              className={
                status === 'COMPLETED'
                  ? 'history-filter history-filter--active'
                  : 'history-filter'
              }
              type="button"
              onClick={() => changeStatus('COMPLETED')}
            >
              Concluídas
            </button>
          </div>
        </div>
        {history.isPending && (
          <div className="status-card" role="status">
            <span className="spinner" aria-hidden="true" />
            <strong>Carregando tentativas</strong>
          </div>
        )}
        {history.isError && (
          <div className="status-card status-card--error" role="alert">
            <div>
              <strong>Não foi possível carregar o histórico</strong>
              <button
                className="text-button"
                type="button"
                onClick={() => void history.refetch()}
              >
                Tentar novamente
              </button>
            </div>
          </div>
        )}
        {history.isSuccess && history.data.items.length === 0 && (
          <div className="status-card" role="status">
            <div>
              <strong>Nenhuma tentativa encontrada</strong>
              <p>Inicie um novo quiz para acompanhar sua evolução.</p>
            </div>
          </div>
        )}
        {history.isSuccess && history.data.items.length > 0 && (
          <div className="history-list">
            {history.data.items.map((attempt) => (
              <article className="history-card" key={attempt.id}>
                <div>
                  <span
                    className={`attempt-status attempt-status--${attempt.status.toLowerCase()}`}
                  >
                    {attempt.status === 'COMPLETED'
                      ? 'Concluída'
                      : 'Em andamento'}
                  </span>
                  <h3>{attempt.difficulty}</h3>
                  <p>{attempt.categories.join(' · ')}</p>
                  <small>
                    Iniciada em{' '}
                    {new Date(attempt.startedAt).toLocaleDateString('pt-BR')}
                  </small>
                </div>
                <div className="history-result">
                  {attempt.score !== null && <strong>{attempt.score}%</strong>}
                  {attempt.status === 'COMPLETED' ? (
                    <Link
                      className="secondary-button"
                      to={`/quiz-attempts/${attempt.id}/result`}
                    >
                      Ver resultado
                    </Link>
                  ) : (
                    <button
                      className="primary-button"
                      type="button"
                      disabled={continuingId === attempt.id}
                      onClick={() => void continueAttempt(attempt.id)}
                    >
                      {continuingId === attempt.id
                        ? 'Carregando...'
                        : 'Continuar'}
                    </button>
                  )}
                  {continueError === attempt.id && (
                    <span role="alert">Não foi possível abrir.</span>
                  )}
                </div>
              </article>
            ))}
          </div>
        )}
        {history.isSuccess && history.data.totalPages > 1 && (
          <nav
            className="history-pagination"
            aria-label="Paginação do histórico"
          >
            <button
              className="secondary-button"
              type="button"
              disabled={page === 0}
              onClick={() => changePage(page - 1)}
            >
              Anterior
            </button>
            <span>
              Página {page + 1} de {history.data.totalPages}
            </span>
            <button
              className="secondary-button"
              type="button"
              disabled={page + 1 >= history.data.totalPages}
              onClick={() => changePage(page + 1)}
            >
              Próxima
            </button>
          </nav>
        )}
      </section>
    </main>
  )
}
