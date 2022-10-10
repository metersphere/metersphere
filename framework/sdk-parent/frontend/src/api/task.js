import {get, post, socket} from "../plugins/request";
import {getCurrentProjectID, getCurrentUser} from "../utils/token";
// 获取使用当前js模块的package.json，不要修改引入路径
import packageInfo from '@/../package.json'

const currentModuleName = packageInfo.name;

export function getTaskSocket() {
  return socket("/websocket/task/running/" + getCurrentProjectID() + "/" + getCurrentUser().id)
}

export function getCaseData(id) {
  return get(`/task/center/case/${id}`)
}

export function getScenarioData(id) {
  return get(`/task/center/scenario/${id}`)
}

export function getTaskList(data, goPage, pageSize) {
  return post(`/task/center/list/${goPage}/${pageSize}`, data)
}

export function stopTask(data) {
  if (currentModuleName === 'api' && data.type === 'API') {
    // 停止API测试任务
    return post(`/api/automation/stop/batch`, [data])
  } else if (currentModuleName === 'performance' && data.type === 'PERFORMANCE') {
    // 停止性能测试任务
    return post(`/performance/stop/batch`, data)
  } else {
    // 来自其他模块的停止任务请求
    return post(`/task/center/stop/batch`, [data])
  }
}

export function stopBatchTask(dataArray) {
  if (dataArray && dataArray.length > 0) {
    if (currentModuleName === 'api') {
      // 调用性能测试服务，停止对应测试任务
      post(`/task/center/stop/perf`, dataArray).then(r => {
      })
      // 停止API测试任务
      return post(`/api/automation/stop/batch`, dataArray)
    } else if (currentModuleName === 'performance') {
      // 停止性能测试任务
      let perfArray = dataArray.filter(p => p.type === "PERFORMANCE");
      if (perfArray && perfArray.length > 0) {
        post(`/performance/stop/batch`, perfArray[0]).then(r => {
        })
      }
      return post(`/task/center/stop/api`, dataArray)
    } else {
      return post(`/task/center/stop/batch`, dataArray)
    }
  }
}
