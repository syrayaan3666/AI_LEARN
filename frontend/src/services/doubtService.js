import apiClient from './api.js';

export async function askDoubt(payload) {
  const response = await apiClient.post('/doubts/ask', payload);
  return response.data;
}

export async function getDoubtHistory(studentId) {
  const response = await apiClient.get(`/doubts/history/${studentId}`);
  return response.data;
}
