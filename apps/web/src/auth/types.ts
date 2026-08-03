import type { ReactNode } from 'react'

export interface AuthenticatedUser {
  subject: string
  displayName: string | null
  email: string | null
}

export interface AuthSession {
  error: Error | null
  getAccessToken(): Promise<string | null>
  isAuthenticated: boolean
  isLoading: boolean
  signIn(): Promise<void>
  signOut(): Promise<void>
  user: AuthenticatedUser | null
}

export interface AuthenticationProviderProps {
  children: ReactNode
}
