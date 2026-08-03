/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_URL?: string
  readonly VITE_AUTH_MODE?: 'local' | 'oidc'
  readonly VITE_COGNITO_CLIENT_ID?: string
  readonly VITE_COGNITO_DOMAIN?: string
  readonly VITE_COGNITO_ISSUER_URI?: string
  readonly VITE_COGNITO_POST_LOGOUT_REDIRECT_URI?: string
  readonly VITE_COGNITO_REDIRECT_URI?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
