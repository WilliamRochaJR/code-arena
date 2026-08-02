import { Link, Navigate, Outlet, Route, Routes } from 'react-router-dom'
import type { JavaQuizClient } from '@code-arena/java-quiz-sdk'

import { javaQuizClient } from './api/java-quiz-client'
import { CategorySelectionPage } from './quiz/CategorySelectionPage'
import { DifficultySelectionPage } from './quiz/DifficultySelectionPage'
import { QuizAttemptPage } from './quiz/QuizAttemptPage'
import { QuizResultPage } from './quiz/QuizResultPage'
import './App.css'

interface AppProps {
  client?: JavaQuizClient
}

function App({ client = javaQuizClient }: AppProps) {
  return (
    <Routes>
      <Route element={<AppLayout />}>
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

function AppLayout() {
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
        <span className="step-label">Treino Java</span>
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
