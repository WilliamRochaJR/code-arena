import type { User } from 'oidc-client-ts'
import { describe, expect, it, vi } from 'vitest'

import { getValidAccessToken, type TokenRenewalState } from './access-token'

describe('getValidAccessToken', () => {
  it('returns the current access token while it is valid', async () => {
    const renew = vi.fn()

    await expect(
      getValidAccessToken(user(false, 'current-token'), renew, renewalState()),
    ).resolves.toBe('current-token')
    expect(renew).not.toHaveBeenCalled()
  })

  it('shares one silent renewal between concurrent requests', async () => {
    const renewal = renewalState()
    const renew = vi
      .fn<() => Promise<User | null>>()
      .mockResolvedValue(user(false, 'renewed-token'))

    await expect(
      Promise.all([
        getValidAccessToken(user(true, 'expired-token'), renew, renewal),
        getValidAccessToken(user(true, 'expired-token'), renew, renewal),
      ]),
    ).resolves.toEqual(['renewed-token', 'renewed-token'])
    expect(renew).toHaveBeenCalledOnce()
    expect(renewal.current).toBeNull()
  })
})

function renewalState(): TokenRenewalState {
  return { current: null }
}

function user(expired: boolean, accessToken: string): User {
  return { expired, access_token: accessToken } as User
}
