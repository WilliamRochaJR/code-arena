import { Link, useLocation, useParams } from 'react-router-dom'
import type { QuizAttemptSummary } from '@code-arena/java-quiz-sdk'

interface AttemptLocationState {
  attempt?: QuizAttemptSummary
}

export function AttemptCreatedPage() {
  const { attemptId } = useParams()
  const location = useLocation()
  const state = location.state as AttemptLocationState | null
  const attempt = state?.attempt

  return (
    <main className="main-content compact-content">
      <section className="success-panel" aria-labelledby="page-title">
        <span className="success-mark" aria-hidden="true">
          ✓
        </span>
        <span className="eyebrow">Tentativa criada</span>
        <h1 id="page-title">Seu quiz está pronto.</h1>
        <p>
          A tentativa foi criada com sucesso. A execução das perguntas será o
          próximo incremento do Code Arena.
        </p>

        <dl className="attempt-summary">
          <div>
            <dt>Identificador</dt>
            <dd>{attemptId}</dd>
          </div>
          {attempt && (
            <>
              <div>
                <dt>Dificuldade</dt>
                <dd>{attempt.difficulty}</dd>
              </div>
              <div>
                <dt>Questões</dt>
                <dd>{attempt.totalQuestions}</dd>
              </div>
            </>
          )}
        </dl>

        <Link className="secondary-button" to="/quiz/categories">
          Configurar outro quiz
        </Link>
      </section>
    </main>
  )
}
