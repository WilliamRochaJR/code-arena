import type { ProblemDetail } from './types.js'

export class JavaQuizSdkError extends Error {
  override readonly name: string = 'JavaQuizSdkError'
}

export class JavaQuizHttpError extends JavaQuizSdkError {
  override readonly name = 'JavaQuizHttpError'

  constructor(
    readonly status: number,
    readonly problem?: ProblemDetail,
  ) {
    super(problem?.detail ?? `The API returned HTTP ${status}.`)
  }
}

export class JavaQuizNetworkError extends JavaQuizSdkError {
  override readonly name = 'JavaQuizNetworkError'

  constructor(options?: ErrorOptions) {
    super('The API could not be reached.', options)
  }
}

export class JavaQuizTimeoutError extends JavaQuizSdkError {
  override readonly name = 'JavaQuizTimeoutError'

  constructor(readonly timeoutMs: number) {
    super(`The request exceeded the ${timeoutMs}ms timeout.`)
  }
}

export class JavaQuizRequestCancelledError extends JavaQuizSdkError {
  override readonly name = 'JavaQuizRequestCancelledError'

  constructor() {
    super('The request was cancelled.')
  }
}

export class JavaQuizInvalidResponseError extends JavaQuizSdkError {
  override readonly name = 'JavaQuizInvalidResponseError'

  constructor(options?: ErrorOptions) {
    super('The API returned an invalid response.', options)
  }
}
