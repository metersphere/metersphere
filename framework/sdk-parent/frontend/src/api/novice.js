import {get, post} from "../plugins/request"
import {TASK_DATA} from "../utils/constants";


export function getSideTask() {
  return post(`/novice/info`);
}

export function saveStep() {
  return post(`/novice/save/step`,{'guideStep': localStorage.getItem('step'), 'dataOption': JSON.stringify(TASK_DATA)});
}

export function updateStatus(status) {
  return post(`/novice/status`,{'status': status, 'dataOption': JSON.stringify(TASK_DATA)});
}

export function saveTask(data) {
  return post(`/novice/save/task`,{'dataOption': JSON.stringify(data)});
}

export function initTaskData(url){
  getSideTask().then(res=>{
    let taskData = TASK_DATA
    if(res.data.length > 0 && res.data[0].dataOption){
      taskData = JSON.parse(res.data[0].dataOption)
    }
    if(taskData.length > 0){
      taskData.forEach(item=>{
        let index = item.taskData.findIndex(function(res) {
          return res.status === 0 && res.api.includes(url);
        });
        if(index > -1){
          item.taskData[index].status = 1
          item.rate += 1
          item.percentage = Math.floor(item.rate / item.taskData.length * 100)
          if(item.percentage === 100){
            item.status = 1
          }else if(100 > item.percentage && item.percentage > 0){
            item.status = 2
          }
        }
      })
      // 入库
      saveTask(taskData).then(res => {
      }).catch(error => {
        // 错误的信息
        this.$error({
          message: error.response.data.message
        })
      })
    }
  })
}
