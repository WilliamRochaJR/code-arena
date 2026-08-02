export interface Category {
  slug: string
  name: string
}

export interface CategoryList {
  items: Category[]
}

export type QuizDifficulty = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED'

export interface CreateQuizAttemptRequest {
  difficulty: QuizDifficulty
  categories: string[]
}

export interface QuizAttemptSummary {
  id: string
  status: 'IN_PROGRESS'
  difficulty: QuizDifficulty
  categories: string[]
  totalQuestions: number
  answeredQuestions: number
  startedAt: string
}

export interface QuizAlternative {
  id: string
  text: string
}

export interface QuizQuestion {
  id: string
  position: number
  statement: string
  categories: string[]
  alternatives: QuizAlternative[]
  selectedAlternativeId: string | null
}

export interface QuizAttemptDetails {
  id: string
  status: 'IN_PROGRESS'
  difficulty: QuizDifficulty
  totalQuestions: number
  answeredQuestions: number
  startedAt: string
  questions: QuizQuestion[]
}

export interface SaveQuizAnswerRequest {
  selectedAlternativeId: string
}

export interface SavedQuizAnswer {
  questionId: string
  selectedAlternativeId: string
  answeredAt: string
}

export interface ProblemDetail {
  type?: string
  title?: string
  status?: number
  detail?: string
  instance?: string
  [property: string]: unknown
}

export type TokenProvider = () => Promise<string | null> | string | null

export interface JavaQuizClientOptions {
  baseUrl: string
  tokenProvider?: TokenProvider
  timeoutMs?: number
  fetch?: typeof globalThis.fetch
}

export interface ListCategoriesOptions {
  signal?: AbortSignal
}

export interface CreateQuizAttemptOptions {
  signal?: AbortSignal
}

export interface GetQuizAttemptOptions {
  signal?: AbortSignal
}

export interface SaveQuizAnswerOptions {
  signal?: AbortSignal
}

export interface JavaQuizClient {
  categories: {
    list(options?: ListCategoriesOptions): Promise<CategoryList>
  }
  attempts: {
    create(
      request: CreateQuizAttemptRequest,
      options?: CreateQuizAttemptOptions,
    ): Promise<QuizAttemptSummary>
    get(
      attemptId: string,
      options?: GetQuizAttemptOptions,
    ): Promise<QuizAttemptDetails>
    saveAnswer(
      attemptId: string,
      questionId: string,
      request: SaveQuizAnswerRequest,
      options?: SaveQuizAnswerOptions,
    ): Promise<SavedQuizAnswer>
  }
}
