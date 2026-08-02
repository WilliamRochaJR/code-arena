import { useQuery } from '@tanstack/react-query'
import { useNavigate, useSearchParams } from 'react-router-dom'
import type { JavaQuizClient } from '@code-arena/java-quiz-sdk'

import { createSetupSearchParams, readCategories } from './setupSearchParams'

interface CategorySelectionPageProps {
  client: JavaQuizClient
}

export function CategorySelectionPage({ client }: CategorySelectionPageProps) {
  const navigate = useNavigate()
  const [searchParams, setSearchParams] = useSearchParams()
  const selectedCategories = readCategories(searchParams)
  const categoriesQuery = useQuery({
    queryKey: ['categories'],
    queryFn: ({ signal }) => client.categories.list({ signal }),
  })

  function toggleCategory(slug: string) {
    const nextCategories = selectedCategories.includes(slug)
      ? selectedCategories.filter((category) => category !== slug)
      : [...selectedCategories, slug]
    setSearchParams(createSetupSearchParams(nextCategories), { replace: true })
  }

  function continueToDifficulty() {
    navigate({
      pathname: '/quiz/difficulty',
      search: createSetupSearchParams(selectedCategories).toString(),
    })
  }

  return (
    <main className="main-content">
      <section className="intro" aria-labelledby="page-title">
        <span className="eyebrow">Desafio Java</span>
        <h1 id="page-title">Quais temas você quer praticar?</h1>
        <p>
          Escolha uma ou mais categorias. Vamos preparar dez questões para o seu
          próximo treino.
        </p>
      </section>

      <section className="category-panel" aria-labelledby="category-title">
        <div className="panel-heading">
          <div>
            <span className="step-number">01</span>
            <h2 id="category-title">Selecione as categorias</h2>
          </div>
          <span className="selection-count" aria-live="polite">
            {selectedCategories.length}{' '}
            {selectedCategories.length === 1 ? 'selecionada' : 'selecionadas'}
          </span>
        </div>

        {categoriesQuery.isPending && (
          <div className="status-card" role="status">
            <span className="spinner" aria-hidden="true" />
            <div>
              <strong>Carregando categorias</strong>
              <p>Consultando os temas disponíveis na arena...</p>
            </div>
          </div>
        )}

        {categoriesQuery.isError && (
          <div className="status-card status-card--error" role="alert">
            <span className="status-icon" aria-hidden="true">
              !
            </span>
            <div>
              <strong>Não foi possível carregar as categorias</strong>
              <p>Confira se a API está em execução e tente novamente.</p>
              <button
                className="text-button"
                type="button"
                onClick={() => void categoriesQuery.refetch()}
              >
                Tentar novamente
              </button>
            </div>
          </div>
        )}

        {categoriesQuery.isSuccess &&
          categoriesQuery.data.items.length === 0 && (
            <div className="status-card" role="status">
              <span className="status-icon" aria-hidden="true">
                —
              </span>
              <div>
                <strong>Nenhuma categoria disponível</strong>
                <p>Novos temas serão publicados em breve.</p>
              </div>
            </div>
          )}

        {categoriesQuery.isSuccess && categoriesQuery.data.items.length > 0 && (
          <fieldset className="category-grid">
            <legend className="sr-only">Categorias disponíveis</legend>
            {categoriesQuery.data.items.map((category, index) => {
              const selected = selectedCategories.includes(category.slug)
              return (
                <label
                  className={`category-card${selected ? ' category-card--selected' : ''}`}
                  key={category.slug}
                >
                  <input
                    checked={selected}
                    onChange={() => toggleCategory(category.slug)}
                    type="checkbox"
                    value={category.slug}
                  />
                  <span className="category-index" aria-hidden="true">
                    {String(index + 1).padStart(2, '0')}
                  </span>
                  <span className="category-copy">
                    <strong>{category.name}</strong>
                    <small>{category.slug}</small>
                  </span>
                  <span className="check-mark" aria-hidden="true">
                    ✓
                  </span>
                </label>
              )
            })}
          </fieldset>
        )}

        <div className="panel-footer">
          <p>Você poderá ajustar a dificuldade no próximo passo.</p>
          <button
            className="primary-button"
            type="button"
            disabled={selectedCategories.length === 0}
            onClick={continueToDifficulty}
          >
            Continuar
            <span aria-hidden="true">→</span>
          </button>
        </div>
      </section>
    </main>
  )
}
