export interface Category {
  slug: string
  name: string
}

export interface CategoryList {
  items: Category[]
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

export interface JavaQuizClient {
  categories: {
    list(options?: ListCategoriesOptions): Promise<CategoryList>
  }
}
