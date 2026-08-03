import { expect, test } from '@playwright/test'

test('completes a quiz and shows the attempt in history', async ({ page }) => {
  await page.goto('/')

  await expect(
    page.getByRole('heading', { name: 'Quais temas você quer praticar?' }),
  ).toBeVisible()
  await page.getByRole('checkbox', { name: /orientacao a objetos/i }).click()
  await expect(page).toHaveURL(/categories=OOP/)
  await page.getByRole('button', { name: /continuar/i }).click()

  await expect(
    page.getByRole('heading', { name: 'Qual será a intensidade?' }),
  ).toBeVisible()
  await page.getByRole('radio', { name: /iniciante/i }).click()
  await expect(page).toHaveURL(/difficulty=BEGINNER/)

  const startQuizButton = page.getByRole('button', { name: 'Iniciar quiz' })
  await expect(startQuizButton).toBeEnabled()
  await startQuizButton.click()

  await expect(page).toHaveURL(/\/quiz-attempts\/[^/]+\/questions\/1$/)
  const attemptId = new URL(page.url()).pathname.split('/')[2]
  expect(attemptId).toBeTruthy()

  const progress = page.getByText(/Questão 1 de \d+/)
  await expect(progress).toBeVisible()
  const progressText = await progress.textContent()
  const totalQuestions = Number(progressText?.match(/de (\d+)/)?.[1])
  expect(totalQuestions).toBeGreaterThan(0)

  for (let position = 1; position <= totalQuestions; position += 1) {
    await expect(
      page.getByText(`Questão ${position} de ${totalQuestions}`),
    ).toBeVisible()
    await page.getByRole('radio').first().click()

    if (position < totalQuestions) {
      await page.getByRole('button', { name: 'Salvar e avançar' }).click()
      await expect(page).toHaveURL(
        new RegExp(`/quiz-attempts/${attemptId}/questions/${position + 1}$`),
      )
      continue
    }

    await page.getByRole('button', { name: 'Salvar resposta' }).click()
  }

  await expect(
    page.getByText('Respostas prontas para conclusão.'),
  ).toBeVisible()
  await page.getByRole('button', { name: 'Concluir quiz' }).click()
  await expect(page.getByRole('alertdialog')).toContainText(
    'Concluir esta tentativa?',
  )
  await page.getByRole('button', { name: 'Confirmar conclusão' }).click()

  await expect(page).toHaveURL(`/quiz-attempts/${attemptId}/result`)
  await expect(page.getByText('Quiz concluído')).toBeVisible()
  await expect(page.getByRole('heading', { name: /^\d+%$/ })).toBeVisible()
  await expect(
    page.getByRole('heading', { name: 'Revisão das respostas' }),
  ).toBeVisible()

  await page.getByRole('link', { name: 'Minhas tentativas' }).click()
  await expect(
    page.getByRole('heading', { name: 'Minhas tentativas' }),
  ).toBeVisible()
  await page.getByRole('button', { name: 'Concluídas' }).click()

  const latestResultLink = page
    .getByRole('link', { name: 'Ver resultado' })
    .first()
  await expect(latestResultLink).toHaveAttribute(
    'href',
    `/quiz-attempts/${attemptId}/result`,
  )
})
