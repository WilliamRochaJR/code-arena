import { useCallback, useMemo } from 'react'
import { AuthProvider as OidcProvider, useAuth } from 'react-oidc-context'
import { WebStorageStateStore } from 'oidc-client-ts'

import { AuthSessionContext } from './AuthContext'
import { readAuthConfig } from './config'
import type { AuthenticationProviderProps, AuthSession } from './types'

const config = readAuthConfig(import.meta.env, window.location.origin)

export function AuthenticationProvider({
  children,
}: AuthenticationProviderProps) {
  if (config.mode === 'local') {
    return <LocalAuthenticationProvider>{children}</LocalAuthenticationProvider>
  }

  return (
    <OidcProvider
      authority={config.issuerUri}
      automaticSilentRenew
      client_id={config.clientId}
      loadUserInfo={false}
      onSigninCallback={() => {
        window.history.replaceState({}, document.title, '/quiz/categories')
      }}
      post_logout_redirect_uri={config.postLogoutRedirectUri}
      redirect_uri={config.redirectUri}
      response_type="code"
      scope="openid profile email"
      userStore={new WebStorageStateStore({ store: window.localStorage })}
    >
      <OidcAuthenticationBridge cognitoDomain={config.cognitoDomain}>
        {children}
      </OidcAuthenticationBridge>
    </OidcProvider>
  )
}

function LocalAuthenticationProvider({
  children,
}: AuthenticationProviderProps) {
  const session = useMemo<AuthSession>(
    () => ({
      error: null,
      getAccessToken: async () => null,
      isAuthenticated: true,
      isLoading: false,
      signIn: async () => undefined,
      signOut: async () => undefined,
      user: {
        subject: 'local-development-user',
        displayName: 'Local Developer',
        email: null,
      },
    }),
    [],
  )

  return (
    <AuthSessionContext.Provider value={session}>
      {children}
    </AuthSessionContext.Provider>
  )
}

function OidcAuthenticationBridge({
  children,
  cognitoDomain,
}: AuthenticationProviderProps & { cognitoDomain: string }) {
  const auth = useAuth()
  const signIn = useCallback(async () => {
    await auth.signinRedirect({
      extraQueryParams: { identity_provider: 'Google' },
    })
  }, [auth])
  const signOut = useCallback(async () => {
    await auth.removeUser()
    const logoutUrl = new URL('/logout', cognitoDomain)
    logoutUrl.searchParams.set('client_id', auth.settings.client_id)
    logoutUrl.searchParams.set(
      'logout_uri',
      auth.settings.post_logout_redirect_uri ?? window.location.origin,
    )
    window.location.assign(logoutUrl.toString())
  }, [auth, cognitoDomain])
  const getAccessToken = useCallback(
    async () => auth.user?.access_token ?? null,
    [auth.user?.access_token],
  )
  const session = useMemo<AuthSession>(
    () => ({
      error: auth.error ?? null,
      getAccessToken,
      isAuthenticated: auth.isAuthenticated,
      isLoading: auth.isLoading || Boolean(auth.activeNavigator),
      signIn,
      signOut,
      user: auth.user
        ? {
            subject: auth.user.profile.sub,
            displayName:
              stringClaim(auth.user.profile.name) ??
              stringClaim(auth.user.profile.email),
            email: stringClaim(auth.user.profile.email),
          }
        : null,
    }),
    [auth, getAccessToken, signIn, signOut],
  )

  return (
    <AuthSessionContext.Provider value={session}>
      {children}
    </AuthSessionContext.Provider>
  )
}

function stringClaim(value: unknown): string | null {
  return typeof value === 'string' ? value : null
}
