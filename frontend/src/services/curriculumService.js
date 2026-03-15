import apiClient from './api.js';

export async function generateCurriculum(payload) {
  const response = await apiClient.post('/curriculum/generate', payload);
  return response.data;
}

export async function generateMockCurriculum(studentId) {
  const response = await apiClient.post('/curriculum/generate-mock', { studentId });
  return response.data;
}

export async function refineCurriculum(payload) {
  const response = await apiClient.post('/curriculum/refine', payload);
  return response.data;
}

export async function getCurriculumByStudent(studentId) {
  const response = await apiClient.get(`/curriculum/${studentId}`);
  return response.data;
}

export async function saveAsTopics(curriculumId, studentId, curriculumName) {
  const response = await apiClient.post(`/curriculum/${curriculumId}/save-topics`, {
    studentId,
    curriculumName,
  });
  return response.data;
}
