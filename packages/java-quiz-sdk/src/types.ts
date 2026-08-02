export interface Category {
  slug: string
  name: string
}

export interface CategoryList {
  items: Category[]
}

export type QuizDifficulty = 'BEGINNER' | 'INTERMEDIATE' | 'ADVANCED'
export type QuizAttemptStatus = 'IN_PROGRESS' | 'COMPLETED'

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
  status: 'IN_PROGRESS' | 'COMPLETED'
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

export interface QuizCategoryPerformance {
  category: string
  correct: number
  total: number
}

export interface CompletedQuizQuestion {
  id: string
  position: number
  selectedAlternativeId: string
  correctAlternativeId: string
  correct: boolean
  explanation: string
}

export interface CompletedQuizAttempt {
  attemptId: string
  status: 'COMPLETED'
  totalQuestions: number
  correctAnswers: number
  score: number
  startedAt: string
  completedAt: string
  performanceByCategory: QuizCategoryPerformance[]
  questions: CompletedQuizQuestion[]
}

export interface QuizAttemptHistoryItem {
  id: string
  status: QuizAttemptStatus
  difficulty: QuizDifficulty
  categories: string[]
  score: number | null
  startedAt: string
  completedAt: string | null
}

export interface QuizAttemptHistory {
  items: QuizAttemptHistoryItem[]
  page: number
  size: number
  totalItems: number
  totalPages: number
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

export interface CompleteQuizAttemptOptions {
  signal?: AbortSignal
}

export interface ListQuizAttemptsOptions {
  page?: number
  size?: number
  status?: QuizAttemptStatus
  signal?: AbortSignal
}

export interface JavaQuizClient {
  categories: {
    list(options?: ListCategoriesOptions): Promise<CategoryList>
  }
  attempts: {
    list(options?: ListQuizAttemptsOptions): Promise<QuizAttemptHistory>
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
    complete(
      attemptId: string,
      options?: CompleteQuizAttemptOptions,
    ): Promise<CompletedQuizAttempt>
  }
}
