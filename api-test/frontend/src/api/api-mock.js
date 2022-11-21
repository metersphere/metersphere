import { get, post } from 'metersphere-frontend/src/plugins/request';
import { fileUpload } from '@/api/base-network';

export function getMockApiParams(id) {
  return get('/mock/config/get-api-params/' + id);
}

export function updateMockExpectConfigStatus(mockParam) {
  return post('/mock/config/update/expect', mockParam);
}

export function mockExpectConfig(id) {
  return get('/mock/config/get-expect/' + id);
}

export function delMock(id) {
  return get('/mock/config/delete/' + id);
}

export function createMockConfig(mockParam) {
  return post('/mock/config/gen', mockParam);
}

export function getMockApiResponse(id) {
  return get('/mock/config/get-api-response/' + id);
}

export function getTcpMockTestData(mockParam) {
  return post('/mock/config/get-tcp-test-data', mockParam);
}

export function getMockTestData(mockParam) {
  return post('/mock/config/test-data', mockParam);
}

export function updateMockExpectConfig(mockParam, file, files) {
  return fileUpload('/mock/config/update/form', file, files, mockParam);
}

export function getTcpMockInfo(projectId) {
  return get('/mock/config/get-details/' + projectId);
}
