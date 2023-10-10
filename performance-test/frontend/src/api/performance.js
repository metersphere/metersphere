import {get, post, request} from "metersphere-frontend/src/plugins/request"

export function getDashboardHeatmap() {
  return get('/performance/dashboard/tests')
}

export function getRecentTests(condition) {
  return post('/performance/recent/5', condition)
}

export function getRecentReports(condition) {
  return post('/performance/report/recent/5', condition)
}

export function getReportCount(testId) {
  return get(`/performance/test/report-count/${testId}`)
}

export function getPerformanceVersions(testId) {
  return get(`/performance/versions/${testId}`)
}

export function deleteCurrentVersionTest(test) {
  return get(`performance/delete/${test.versionId}/${test.refId}`);
}

export function searchTests(goPage, pageSize, condition) {
  return post(`/performance/list/${goPage}/${pageSize}`, condition)
}

export function copyTest(test) {
  return post(`/performance/copy`, {id: test.id})
}

export function runTest(test) {
  return post('/performance/run', {id: test.id, triggerMode: 'MANUAL'})
}

export function deleteTest(data) {
  return post(`/performance/delete`, data)
}

export function deleteTestBatch(data) {
  return post(`/performance/delete/batch`, data)
}

export function getTest(id) {
  return get(`/performance/get/${id}`)
}

export function getFollows(id) {
  return get(`/performance/test/follow/${id}`)
}

export function getTestVersionHistory(id) {
  return get(`/performance/versions/${id}`)
}

export function getTestByVersion(versionId, refId) {
  return get(`/performance/get/${versionId}/${refId}`)
}

export function syncScenario(param) {
  return post(`/performance/sync/scenario`, param)
}

export function saveSchedule(param) {
  let url = '/performance/schedule/create';
  if (param.id) {
    url = '/performance/schedule/update';
  }
  return post(url, param);
}

export function saveTest(test, formData) {
  let savePath = "/performance/save"
  let editPath = "/performance/edit"

  let url = test.id ? editPath : savePath;

  let config = {
    method: 'POST',
    url: url,
    data: formData,
    headers: {
      'Content-Type': undefined
    }
  }
  return request(config);
}

export function saveFollows(testId, param) {
  return post(`/performance/test/update/follows/${testId}`, param)
}


export function getFiles(id) {
  return get(`/performance/file/metadata/${id}`)
}

export function getMetadataById(id) {
  return get(`/performance/file/getMetadataById/${id}`)
}

export function getResourcePools(isShare) {
  let url = '/testresourcepool/list/quota/valid';
  if (isShare) {
    url = '/share/testresourcepool/list/quota/valid';
  }
  return get(url);
}

export function getLoadConfig(testId, reportId, isShare) {
  let url = '';
  if (testId) {
    url = '/performance/get-load-config/' + testId;
  }
  if (reportId) {
    url = '/performance/report/get-load-config/' + reportId;
  }
  if (!url) {
    return;
  }
  if (isShare) {
    url = '/share/performance/report/get-load-config/' + reportId;
  }

  return get(url);
}

export function getJmxContent(testId, reportId, isShare) {
  let url = '';
  if (testId) {
    url = '/performance/get-jmx-content/' + testId;
  }
  if (reportId) {
    url = '/performance/report/get-jmx-content/' + reportId;
  }
  if (!url) {
    return;
  }
  if (isShare) {
    url = '/share/performance/report/get-jmx-content/' + reportId;
  }
  return get(url)
}

export function getAdvancedConfig(type, testId, reportId, isShare, shareId) {
  let url = '/performance/get-advanced-config/' + testId;
  if (type) {
    url = '/performance/report/get-advanced-config/' + reportId;
  }
  if (isShare) {
    url = '/share/performance/report/get-advanced-config/' + shareId + '/' + reportId;
  }

  return get(url);
}

export function getJmxContents(jmxIds) {
  return post('/performance/export/jmx', jmxIds)
}

export function downloadFile(url, file) {
  let data = {
    name: file.name,
    id: file.id,
  };
  let config = {
    url: url,
    method: 'post',
    data: data,
    responseType: 'blob'
  };
  return request(config);
}

export function getProjectFiles(type, projectId, currentPage, pageSize, condition) {
  return post(`/performance/project/${type}/${projectId}/${currentPage}/${pageSize}`, condition)
}

export function getNoticeTasks(testId) {
  return get(`/notice/search/message/${testId}`);
}

export function getProjectFileByName(projectId, data) {
  return post(`/performance/file/${projectId}/getMetadataByName`, data)
}

export function uploadFiles(projectId, formData) {
  let url = `/project/upload/files/${projectId}`;
  let options = {
    method: 'POST',
    url: url,
    data: formData,
    headers: {
      'Content-Type': undefined
    }
  };
  return request(options);
}

export function updateFile(fileId, formData) {
  let url = `/project/update/file/${fileId}`;
  let options = {
    method: 'POST',
    url: url,
    data: formData,
    headers: {
      'Content-Type': undefined
    }
  };
  return request(options);
}

export function deleteFile(fileId) {
  return get(`/project/delete/file/${fileId}`)
}

export function checkFileIsRelated(fileId) {
  return get(`/performance/check-file-is-related/${fileId}`)
}

export function listSchedule(data) {
  return post('/performance/list/schedule', data)
}

export function updateSchedule(schedule) {
  return post('/performance/schedule/update', schedule);
}


export function exportScenarioJmx(condition) {
  return post('/api/automation/export/jmx', condition)
}

export function searchScenarioList(goPage, pageSize, condition) {
  return post(`/api/automation/list/${goPage}/${pageSize}`, condition)
}

export function getNodeOperationInfo(request) {
  return post(`/prometheus/query/node-operation-info`, request)
}
