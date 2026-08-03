import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { BrowserRouter } from 'react-router-dom'
import './index.css'
import { AuthenticatedApplication } from './auth/AuthenticatedApplication.tsx'
import { AuthenticationProvider } from './auth/AuthenticationProvider.tsx'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 60_000,
    },
  },
})

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <AuthenticationProvider>
          <AuthenticatedApplication />
        </AuthenticationProvider>
      </BrowserRouter>
    </QueryClientProvider>
  </StrictMode>,
)
