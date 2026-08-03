import { describe, expect, it } from 'vitest'

import { readAuthConfig } from './config'

describe('readAuthConfig', () => {
  it('uses local mode by default', () => {
    expect(readAuthConfig({}, 'http://localhost:5173')).toEqual({
      mode: 'local',
    })
  })

  it('reads OIDC configuration and supplies local redirect defaults', () => {
    expect(
      readAuthConfig(
        {
          VITE_AUTH_MODE: 'oidc',
          VITE_COGNITO_CLIENT_ID: 'web-client',
          VITE_COGNITO_DOMAIN: 'https://auth.example.com/',
          VITE_COGNITO_ISSUER_URI: 'https://issuer.example.com/',
        },
        'http://localhost:5173',
      ),
    ).toEqual({
      mode: 'oidc',
      clientId: 'web-client',
      cognitoDomain: 'https://auth.example.com',
      issuerUri: 'https://issuer.example.com',
      redirectUri: 'http://localhost:5173/auth/callback',
      postLogoutRedirectUri: 'http://localhost:5173',
    })
  })

  it('rejects an incomplete OIDC configuration', () => {
    expect(() =>
      readAuthConfig({ VITE_AUTH_MODE: 'oidc' }, 'http://localhost:5173'),
    ).toThrow('VITE_COGNITO_CLIENT_ID is required')
  })

  it('rejects an unknown authentication mode', () => {
    expect(() =>
      readAuthConfig({ VITE_AUTH_MODE: 'implicit' }, 'http://localhost:5173'),
    ).toThrow('VITE_AUTH_MODE must be "local" or "oidc"')
  })
})
