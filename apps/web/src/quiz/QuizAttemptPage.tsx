import { useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Link, useNavigate, useParams } from 'react-router-dom'
import type {
  JavaQuizClient,
  QuizAttemptDetails,
} from '@code-arena/java-quiz-sdk'

interface QuizAttemptPageProps {
  client: JavaQuizClient
}

export function QuizAttemptPage({ client }: QuizAttemptPageProps) {
  const { attemptId, position: positionParam } = useParams()
  const navigate = useNavigate()
  const queryClient = useQueryClient()
  const position = Number(positionParam)
  const queryKey = ['quiz-attempt', attemptId]
  const attemptQuery = useQuery({
    queryKey,
    queryFn: ({ signal }) => client.attempts.get(attemptId ?? '', { signal }),
    enabled: Boolean(attemptId),
  })
  const questions = attemptQuery.data?.questions ?? []
  const questionIndex = questions.findIndex(
    (question) => question.position === position,
  )
  const question = questions[questionIndex]
  const [localSelections, setLocalSelections] = useState<
    Record<string, string>
  >({})
  const selectedAlternativeId = question
    ? (localSelections[question.id] ?? question.selectedAlternativeId)
    : null

  const saveAnswer = useMutation({
    mutationFn: () => {
      if (!attemptId || !question || !selectedAlternativeId) {
        throw new Error('A question and an alternative are required.')
      }
      return client.attempts.saveAnswer(attemptId, question.id, {
        selectedAlternativeId,
      })
    },
    onSuccess: (savedAnswer) => {
      queryClient.setQueryData<QuizAttemptDetails>(queryKey, (attempt) => {
        if (!attempt) return attempt
        const wasAnswered = attempt.questions.some(
          (item) =>
            item.id === savedAnswer.questionId &&
            item.selectedAlternativeId !== null,
        )
        return {
          ...attempt,
          answeredQuestions: wasAnswered
            ? attempt.answeredQuestions
            : attempt.answeredQuestions + 1,
          questions: attempt.questions.map((item) =>
            item.id === savedAnswer.questionId
              ? {
                  ...item,
                  selectedAlternativeId: savedAnswer.selectedAlternativeId,
                }
              : item,
          ),
        }
      })
      const nextQuestion = questions[questionIndex + 1]
      if (nextQuestion) {
        navigate(
          `/quiz-attempts/${attemptId}/questions/${nextQuestion.position}`,
        )
      }
    },
  })

  if (attemptQuery.isPending) {
    return (
      <main className="main-content compact-content">
        <section className="status-card quiz-status-card" role="status">
          <span className="spinner" aria-hidden="true" />
          <div>
            <strong>Carregando tentativa</strong>
            <p>Preparando as perguntas do seu treino.</p>
          </div>
        </section>
      </main>
    )
  }

  if (attemptQuery.isError) {
    return (
      <main className="main-content compact-content">
        <section
          className="status-card status-card--error quiz-status-card"
          role="alert"
        >
          <span className="status-icon" aria-hidden="true">
            !
          </span>
          <div>
            <strong>Não foi possível carregar a tentativa</strong>
            <p>Confira sua conexão e tente novamente.</p>
            <button
              className="text-button"
              type="button"
              onClick={() => void attemptQuery.refetch()}
            >
              Tentar novamente
            </button>
          </div>
        </section>
      </main>
    )
  }

  if (!attemptId || !question) {
    const firstQuestion = questions[0]
    return (
      <main className="main-content compact-content">
        <section className="status-card quiz-status-card" role="alert">
          <span className="status-icon" aria-hidden="true">
            !
          </span>
          <div>
            <strong>Questão não encontrada</strong>
            <p>A posição informada não pertence a esta tentativa.</p>
            {firstQuestion ? (
              <Link
                className="text-link"
                to={`/quiz-attempts/${attemptId}/questions/${firstQuestion.position}`}
              >
                Ir para a primeira questão
              </Link>
            ) : (
              <Link className="text-link" to="/quiz/categories">
                Configurar outro quiz
              </Link>
            )}
          </div>
        </section>
      </main>
    )
  }

  const previousQuestion = questions[questionIndex - 1]
  const isLastQuestion = questionIndex === questions.length - 1
  const allQuestionsAnswered = questions.every(
    (item) => item.selectedAlternativeId !== null,
  )

  return (
    <main className="main-content quiz-content">
      <section className="quiz-progress" aria-label="Progresso do quiz">
        <div>
          <span className="eyebrow">Desafio Java</span>
          <strong>
            Questão {questionIndex + 1} de {questions.length}
          </strong>
        </div>
        <progress value={questionIndex + 1} max={questions.length}>
          {questionIndex + 1} de {questions.length}
        </progress>
      </section>

      <section className="question-panel" aria-labelledby="question-title">
        <div className="question-heading">
          <div className="question-categories">
            {question.categories.map((category) => (
              <span key={category}>{category}</span>
            ))}
          </div>
          <h1 id="question-title">{question.statement}</h1>
        </div>

        <fieldset className="alternative-list" disabled={saveAnswer.isPending}>
          <legend>Escolha uma alternativa</legend>
          {question.alternatives.map((alternative, index) => {
            const selected = selectedAlternativeId === alternative.id
            return (
              <label
                className={`alternative-card${selected ? ' alternative-card--selected' : ''}`}
                key={alternative.id}
              >
                <input
                  checked={selected}
                  name="alternative"
                  onChange={() => {
                    saveAnswer.reset()
                    setLocalSelections((selections) => ({
                      ...selections,
                      [question.id]: alternative.id,
                    }))
                  }}
                  type="radio"
                  value={alternative.id}
                />
                <span className="alternative-letter" aria-hidden="true">
                  {String.fromCharCode(65 + index)}
                </span>
                <span>{alternative.text}</span>
                <span className="radio-mark" aria-hidden="true" />
              </label>
            )
          })}
        </fieldset>

        {saveAnswer.isError && (
          <div className="inline-error" role="alert">
            <strong>Não foi possível salvar sua resposta.</strong>
            <span> Tente novamente antes de avançar.</span>
          </div>
        )}

        {isLastQuestion && allQuestionsAnswered && (
          <div className="ready-notice" role="status">
            <strong>Respostas prontas para conclusão.</strong>
            <span>
              Revise suas escolhas. A conclusão será adicionada no próximo
              incremento.
            </span>
          </div>
        )}

        <div className="panel-footer question-footer">
          {previousQuestion ? (
            <Link
              className="secondary-button"
              to={`/quiz-attempts/${attemptId}/questions/${previousQuestion.position}`}
            >
              <span aria-hidden="true">←</span>
              Voltar
            </Link>
          ) : (
            <span />
          )}
          <button
            className="primary-button"
            type="button"
            disabled={
              !selectedAlternativeId ||
              saveAnswer.isPending ||
              (isLastQuestion && allQuestionsAnswered)
            }
            onClick={() => saveAnswer.mutate()}
          >
            {saveAnswer.isPending
              ? 'Salvando...'
              : isLastQuestion && allQuestionsAnswered
                ? 'Conclusão em breve'
                : isLastQuestion
                  ? 'Salvar resposta'
                  : 'Salvar e avançar'}
            {!saveAnswer.isPending && !allQuestionsAnswered && (
              <span aria-hidden="true">→</span>
            )}
          </button>
        </div>
      </section>
    </main>
  )
}
