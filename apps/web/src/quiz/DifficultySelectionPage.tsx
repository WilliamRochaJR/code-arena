import { useMutation } from '@tanstack/react-query'
import { Link, useNavigate, useSearchParams } from 'react-router-dom'
import type { JavaQuizClient, QuizDifficulty } from '@code-arena/java-quiz-sdk'

import {
  createSetupSearchParams,
  readCategories,
  readDifficulty,
} from './setupSearchParams'

interface DifficultySelectionPageProps {
  client: JavaQuizClient
}

const DIFFICULTIES: Array<{
  value: QuizDifficulty
  title: string
  description: string
  detail: string
}> = [
  {
    value: 'BEGINNER',
    title: 'Iniciante',
    description: 'Conceitos essenciais e fundamentos da linguagem.',
    detail: 'Base sólida',
  },
  {
    value: 'INTERMEDIATE',
    title: 'Intermediário',
    description: 'Aplicação prática, APIs e decisões do dia a dia.',
    detail: 'Desafio equilibrado',
  },
  {
    value: 'ADVANCED',
    title: 'Avançado',
    description: 'Cenários complexos, detalhes e boas práticas.',
    detail: 'Alta intensidade',
  },
]

export function DifficultySelectionPage({
  client,
}: DifficultySelectionPageProps) {
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()
  const categories = readCategories(searchParams)
  const selectedDifficulty = readDifficulty(searchParams)
  const categoriesSearch = createSetupSearchParams(categories).toString()
  const createAttempt = useMutation({
    mutationFn: (difficulty: QuizDifficulty) =>
      client.attempts.create({ difficulty, categories }),
    onSuccess: (attempt) => {
      navigate(`/quiz-attempts/${attempt.id}/questions/1`, { replace: true })
    },
  })

  function selectDifficulty(difficulty: QuizDifficulty) {
    createAttempt.reset()
    setSearchParams(createSetupSearchParams(categories, difficulty), {
      replace: true,
    })
  }

  function submitAttempt() {
    if (selectedDifficulty && !createAttempt.isPending) {
      createAttempt.mutate(selectedDifficulty)
    }
  }

  if (categories.length === 0) {
    return (
      <main className="main-content compact-content">
        <section className="status-card missing-setup" role="alert">
          <span className="status-icon" aria-hidden="true">
            !
          </span>
          <div>
            <strong>Escolha ao menos uma categoria</strong>
            <p>
              A dificuldade depende dos temas selecionados na etapa anterior.
            </p>
            <Link className="text-link" to="/quiz/categories">
              Voltar para categorias
            </Link>
          </div>
        </section>
      </main>
    )
  }

  return (
    <main className="main-content">
      <section className="intro" aria-labelledby="page-title">
        <span className="eyebrow">Desafio Java</span>
        <h1 id="page-title">Qual será a intensidade?</h1>
        <p>
          Defina o nível das dez questões. Você poderá tentar outra dificuldade
          em um novo treino.
        </p>
      </section>

      <section className="category-panel" aria-labelledby="difficulty-title">
        <div className="panel-heading">
          <div>
            <span className="step-number">02</span>
            <h2 id="difficulty-title">Selecione a dificuldade</h2>
          </div>
          <span className="selection-count">{categories.length} temas</span>
        </div>

        <fieldset className="difficulty-grid">
          <legend className="sr-only">Dificuldades disponíveis</legend>
          {DIFFICULTIES.map((difficulty) => {
            const selected = selectedDifficulty === difficulty.value
            return (
              <label
                className={`difficulty-card${selected ? ' difficulty-card--selected' : ''}`}
                key={difficulty.value}
              >
                <input
                  checked={selected}
                  name="difficulty"
                  onChange={() => selectDifficulty(difficulty.value)}
                  type="radio"
                  value={difficulty.value}
                />
                <span className="difficulty-level">{difficulty.detail}</span>
                <strong>{difficulty.title}</strong>
                <p>{difficulty.description}</p>
                <span className="radio-mark" aria-hidden="true" />
              </label>
            )
          })}
        </fieldset>

        {createAttempt.isError && (
          <div className="inline-error" role="alert">
            <strong>Não foi possível iniciar o quiz.</strong>
            <span> Revise a configuração e tente novamente.</span>
          </div>
        )}

        <div className="panel-footer">
          <Link
            className="secondary-button"
            to={{ pathname: '/quiz/categories', search: categoriesSearch }}
          >
            <span aria-hidden="true">←</span>
            Voltar
          </Link>
          <button
            className="primary-button"
            type="button"
            disabled={!selectedDifficulty || createAttempt.isPending}
            onClick={submitAttempt}
          >
            {createAttempt.isPending ? 'Preparando quiz...' : 'Iniciar quiz'}
            {!createAttempt.isPending && <span aria-hidden="true">→</span>}
          </button>
        </div>
      </section>
    </main>
  )
}
