import type { QuizDifficulty } from '@code-arena/java-quiz-sdk'

export function readCategories(searchParams: URLSearchParams): string[] {
  const categories = searchParams.get('categories')
  if (!categories) {
    return []
  }
  return [...new Set(categories.split(',').filter(Boolean))]
}

export function readDifficulty(
  searchParams: URLSearchParams,
): QuizDifficulty | undefined {
  const difficulty = searchParams.get('difficulty')
  return isQuizDifficulty(difficulty) ? difficulty : undefined
}

export function createSetupSearchParams(
  categories: string[],
  difficulty?: QuizDifficulty,
): URLSearchParams {
  const searchParams = new URLSearchParams()
  if (categories.length > 0) {
    searchParams.set('categories', categories.join(','))
  }
  if (difficulty) {
    searchParams.set('difficulty', difficulty)
  }
  return searchParams
}

function isQuizDifficulty(value: string | null): value is QuizDifficulty {
  return (
    value === 'BEGINNER' || value === 'INTERMEDIATE' || value === 'ADVANCED'
  )
}
