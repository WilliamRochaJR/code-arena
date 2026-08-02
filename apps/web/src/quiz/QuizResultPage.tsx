import { useQuery } from '@tanstack/react-query'
import { Link, useParams } from 'react-router-dom'
import type { JavaQuizClient } from '@code-arena/java-quiz-sdk'

export function QuizResultPage({ client }: { client: JavaQuizClient }) {
  const { attemptId } = useParams()
  const details = useQuery({
    queryKey: ['quiz-attempt', attemptId],
    queryFn: ({ signal }) => client.attempts.get(attemptId ?? '', { signal }),
    enabled: Boolean(attemptId),
  })
  const result = useQuery({
    queryKey: ['quiz-result', attemptId],
    queryFn: ({ signal }) =>
      client.attempts.complete(attemptId ?? '', { signal }),
    enabled: Boolean(attemptId),
    retry: false,
  })

  if (details.isPending || result.isPending) {
    return (
      <main className="main-content compact-content">
        <section className="status-card quiz-status-card" role="status">
          <span className="spinner" aria-hidden="true" />
          <strong>Carregando resultado</strong>
        </section>
      </main>
    )
  }

  if (details.isError || result.isError) {
    return (
      <main className="main-content compact-content">
        <section
          className="status-card status-card--error quiz-status-card"
          role="alert"
        >
          <div>
            <strong>Não foi possível carregar o resultado</strong>
            <button
              className="text-button"
              type="button"
              onClick={() => {
                void details.refetch()
                void result.refetch()
              }}
            >
              Tentar novamente
            </button>
          </div>
        </section>
      </main>
    )
  }

  return (
    <main className="main-content result-content">
      <section className="result-hero">
        <span className="eyebrow">Quiz concluído</span>
        <h1>{result.data.score}%</h1>
        <p>
          {result.data.correctAnswers} de {result.data.totalQuestions} respostas
          corretas
        </p>
      </section>

      <section
        className="performance-grid"
        aria-label="Desempenho por categoria"
      >
        {result.data.performanceByCategory.map((item) => (
          <div key={item.category}>
            <strong>{item.category}</strong>
            <span>
              {item.correct} de {item.total}
            </span>
          </div>
        ))}
      </section>

      <section className="result-review" aria-labelledby="review-title">
        <h2 id="review-title">Revisão das respostas</h2>
        {result.data.questions.map((correction) => {
          const question = details.data.questions.find(
            (item) => item.id === correction.id,
          )
          const selected = question?.alternatives.find(
            (item) => item.id === correction.selectedAlternativeId,
          )
          const correct = question?.alternatives.find(
            (item) => item.id === correction.correctAlternativeId,
          )
          return (
            <article
              className={`review-card review-card--${correction.correct ? 'correct' : 'incorrect'}`}
              key={correction.id}
            >
              <span>Questão {correction.position}</span>
              <h3>{question?.statement}</h3>
              <p>
                <strong>Sua resposta:</strong> {selected?.text}
              </p>
              {!correction.correct && (
                <p>
                  <strong>Resposta correta:</strong> {correct?.text}
                </p>
              )}
              <p className="explanation">{correction.explanation}</p>
            </article>
          )
        })}
      </section>

      <Link className="primary-button result-action" to="/quiz/categories">
        Fazer outro quiz <span aria-hidden="true">→</span>
      </Link>
    </main>
  )
}
