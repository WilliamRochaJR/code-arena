import type { User } from 'oidc-client-ts'

export interface TokenRenewalState {
  current: Promise<User | null> | null
}

export async function getValidAccessToken(
  user: User | null | undefined,
  renew: () => Promise<User | null>,
  renewal: TokenRenewalState,
): Promise<string | null> {
  if (!user) return null
  if (!user.expired) return user.access_token

  renewal.current ??= renew().finally(() => {
    renewal.current = null
  })
  const renewedUser = await renewal.current
  return renewedUser?.access_token ?? null
}
