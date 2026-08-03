export type AuthMode = 'local' | 'oidc'

export interface LocalAuthConfig {
  mode: 'local'
}

export interface OidcAuthConfig {
  clientId: string
  cognitoDomain: string
  issuerUri: string
  mode: 'oidc'
  postLogoutRedirectUri: string
  redirectUri: string
}

export type AuthConfig = LocalAuthConfig | OidcAuthConfig

interface AuthEnvironment {
  VITE_AUTH_MODE?: string
  VITE_COGNITO_CLIENT_ID?: string
  VITE_COGNITO_DOMAIN?: string
  VITE_COGNITO_ISSUER_URI?: string
  VITE_COGNITO_POST_LOGOUT_REDIRECT_URI?: string
  VITE_COGNITO_REDIRECT_URI?: string
}

export function readAuthConfig(
  environment: AuthEnvironment,
  origin: string,
): AuthConfig {
  const mode = environment.VITE_AUTH_MODE ?? 'local'
  if (mode === 'local') {
    return { mode }
  }

  if (mode !== 'oidc') {
    throw new Error(
      `VITE_AUTH_MODE must be "local" or "oidc", received "${mode}".`,
    )
  }

  return {
    mode,
    clientId: required(
      environment.VITE_COGNITO_CLIENT_ID,
      'VITE_COGNITO_CLIENT_ID',
    ),
    cognitoDomain: normalizeUrl(
      required(environment.VITE_COGNITO_DOMAIN, 'VITE_COGNITO_DOMAIN'),
    ),
    issuerUri: normalizeUrl(
      required(environment.VITE_COGNITO_ISSUER_URI, 'VITE_COGNITO_ISSUER_URI'),
    ),
    redirectUri:
      environment.VITE_COGNITO_REDIRECT_URI ?? `${origin}/auth/callback`,
    postLogoutRedirectUri:
      environment.VITE_COGNITO_POST_LOGOUT_REDIRECT_URI ?? origin,
  }
}

function required(value: string | undefined, name: string): string {
  if (!value?.trim()) {
    throw new Error(`${name} is required when VITE_AUTH_MODE is "oidc".`)
  }
  return value.trim()
}

function normalizeUrl(value: string): string {
  return value.replace(/\/$/, '')
}
