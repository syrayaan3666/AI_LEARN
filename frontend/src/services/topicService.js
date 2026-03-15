import apiClient from './api.js';

export async function getTopics() {
  const response = await apiClient.get('/topics');
  return response.data;
}

export async function getLearningByTopic(topicId, options = {}) {
  const response = await apiClient.get(`/learning/${topicId}`, {
    params: {
      generate: options.forceGenerate || undefined,
    },
  });
  return response.data;
}

export async function generateNotesForTopic(topicId) {
  const response = await apiClient.post(`/learning/${topicId}/generate-notes`);
  return response.data;
}
