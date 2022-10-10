import {get, post} from 'metersphere-frontend/src/plugins/request';

export function getResourcePoolPages(goPage, pageSize, param) {
  return post(`/testresourcepool/list/${goPage}/${pageSize}`, param);
}

export function delResourcePoolById(poolId) {
  return get(`/testresourcepool/delete/${poolId}`);
}

export function createResourcePool(pool) {
  return post('/testresourcepool/add', pool);
}

export function modifyResourcePool(pool) {
  return post('/testresourcepool/update', pool);
}

export function checkResourcePoolUse(poolId) {
  return get(`/testresourcepool/check/use/${poolId}`);
}

export function modifyResourcePoolStatus(poolId, poolStatus) {
  return get(`/testresourcepool/update/${poolId}/${poolStatus}`);
}
