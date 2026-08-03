import { Link, Navigate, Outlet, Route, Routes } from 'react-router-dom'
import type { JavaQuizClient } from '@code-arena/java-quiz-sdk'

import { javaQuizClient } from './api/java-quiz-client'
import { CategorySelectionPage } from './quiz/CategorySelectionPage'
import { DifficultySelectionPage } from './quiz/DifficultySelectionPage'
import { QuizAttemptPage } from './quiz/QuizAttemptPage'
import { QuizAttemptHistoryPage } from './quiz/QuizAttemptHistoryPage'
import { QuizResultPage } from './quiz/QuizResultPage'
import type { AuthSession } from './auth/types'
import './App.css'

interface AppProps {
  client?: JavaQuizClient
  session?: AuthSession
}

function App({ client = javaQuizClient, session }: AppProps) {
  return (
    <Routes>
      <Route element={<AppLayout session={session} />}>
        <Route index element={<Navigate replace to="/quiz/categories" />} />
        <Route
          path="/quiz/categories"
          element={<CategorySelectionPage client={client} />}
        />
        <Route
          path="/quiz/difficulty"
          element={<DifficultySelectionPage client={client} />}
        />
        <Route
          path="/quiz-attempts"
          element={<QuizAttemptHistoryPage client={client} />}
        />
        <Route
          path="/quiz-attempts/:attemptId/questions/:position"
          element={<QuizAttemptPage client={client} />}
        />
        <Route
          path="/quiz-attempts/:attemptId/result"
          element={<QuizResultPage client={client} />}
        />
        <Route path="*" element={<Navigate replace to="/quiz/categories" />} />
      </Route>
    </Routes>
  )
}

function AppLayout({ session }: { session: AuthSession | undefined }) {
  return (
    <div className="app-shell">
      <header className="topbar">
        <Link
          className="brand"
          to="/quiz/categories"
          aria-label="Code Arena - início"
        >
          <span className="brand-mark" aria-hidden="true">
            {'</>'}
          </span>
          <span>Code Arena</span>
        </Link>
        <nav className="topbar-nav" aria-label="Navegação principal">
          <Link to="/quiz-attempts">Minhas tentativas</Link>
          {session?.user && (
            <span className="step-label">
              {session.user.displayName ?? 'Pessoa autenticada'}
            </span>
          )}
          {session && (
            <button
              className="text-button topbar-signout"
              type="button"
              onClick={() => void session.signOut()}
            >
              Sair
            </button>
          )}
          <span className="step-label">Treino Java</span>
        </nav>
      </header>

      <Outlet />

      <footer className="footer">
        <span>© 2026 Code Arena</span>
        <span>Aprenda. Pratique. Evolua.</span>
      </footer>
    </div>
  )
}

export default App
