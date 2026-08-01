import { createJavaQuizClient } from '@code-arena/java-quiz-sdk'

export const javaQuizClient = createJavaQuizClient({
  baseUrl: import.meta.env.VITE_API_URL ?? window.location.origin,
})
