import apiClient from './api.js';

export async function generateQuiz(payload) {
  const response = await apiClient.post('/quiz/generate', payload);
  return response.data;
}

export async function submitQuiz(payload) {
  const response = await apiClient.post('/quiz/submit', payload);
  return response.data;
}

export async function getQuizHistory(studentId) {
  const response = await apiClient.get(`/quiz/history/${studentId}`);
  return response.data;
}
