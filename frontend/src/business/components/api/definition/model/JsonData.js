/* 用例等级 */
export const PRIORITY = [
  {name: 'P0', id: 'P0'},
  {name: 'P1', id: 'P1'},
  {name: 'P2', id: 'P2'},
  {name: 'P3', id: 'P3'}
];

export const CASE_ORDER = [
  {label: 'api_test.definition.request.grade_order_asc', name: 'priority', type: 'desc', id: 'grade_order_asc'},
  {label: 'api_test.definition.request.grade_order_desc', name: 'priority', type: 'asc', id: 'grade_order_desc'},
  {
    label: 'api_test.definition.request.create_time_order_asc',
    name: 'create_time',
    type: 'asc',
    id: 'create_time_order_asc'
  },
  {
    label: 'api_test.definition.request.create_time_order_desc',
    name: 'create_time',
    type: 'desc',
    id: 'create_time_order_desc'
  },
  {
    label: 'api_test.definition.request.update_time_order_asc',
    name: 'update_time',
    type: 'asc',
    id: 'update_time_order_asc'
  },
  {
    label: 'api_test.definition.request.update_time_order_desc',
    name: 'update_time',
    type: 'desc',
    id: 'update_time_order_desc'
  }
];

export const OPTIONS = [
  {value: 'HTTP', name: 'HTTP'},
  {value: 'TCP', name: 'TCP'},
  {value: 'SQL', name: 'SQL'},
  {value: 'DUBBO', name: 'DUBBO'}
];

export const DEFAULT_DATA = [{
  "id": "gc",
  "name": "回收站",
  "level": 1,
  "children": [],
}, {
  "id": "root",
  "name": "全部模块",
  "level": 0,
  "children": [],
}];

export const REQ_METHOD = [
  {id: 'GET', label: 'GET'},
  {id: 'POST', label: 'POST'},
  {id: 'PUT', label: 'PUT'},
  {id: 'PATCH', label: 'PATCH'},
  {id: 'DELETE', label: 'DELETE'},
  {id: 'OPTIONS', label: 'OPTIONS'},
  {id: 'HEAD', label: 'HEAD'},
  {id: 'CONNECT', label: 'CONNECT'}
];
export const TCP_METHOD = [
  {id: 'TCP', label: 'TCP'}
];
export const SQL_METHOD = [
  {id: 'SQL', label: 'SQL'}
];
export const DUBBO_METHOD = [
  {id: 'dubbo://', label: 'dubbo://'},
];

export const CASE_PRIORITY = [
  {id: 'P0', label: 'P0'},
  {id: 'P1', label: 'P1'},
  {id: 'P2', label: 'P2'},
  {id: 'P3', label: 'P3'}
];

export const REVIEW_STATUS = [
  {id: 'Prepare', label: '未评审'},
  {id: 'Pass', label: '通过'},
  {id: 'UnPass', label: '未通过'}
];
export const API_STATUS = [
  {id: 'Prepare', label: 'test_track.plan.plan_status_prepare'},
  {id: 'Underway', label: 'test_track.plan.plan_status_running'},
  {id: 'Completed', label: 'test_track.plan.plan_status_completed'}
];
export const TEST = [
  {id: 'performance', name: '性能测试', module: 'performance'},
  {id: 'testcase', name: '接口用例', module: 'api'},
  {id: 'automation', name: '场景测试', module: 'api'}
];
export const TEST_CASE = [
  {value: 'HTTP', label: 'HTTP', leaf: true},
  {value: 'TCP', label: 'TCP', leaf: true},
  {value: 'DUBBO', label: 'DUBBO', leaf: true},
  {value: 'SQL', label: 'SQL', leaf: true}
];

export const API_METHOD_COLOUR = [
  ['GET', "#61AFFE"], ['POST', '#49CC90'], ['PUT', '#fca130'],
  ['PATCH', '#E2EE11'], ['DELETE', '#f93e3d'], ['OPTIONS', '#0EF5DA'],
  ['HEAD', '#8E58E7'], ['CONNECT', '#90AFAE'],
  ['DUBBO', '#C36EEF'], ['dubbo://', '#C36EEF'], ['SQL', '#0AEAD4'], ['TCP', '#0A52DF'],
];

export const REQUIRED = [
  {name: '必填', id: true},
  {name: '非必填', id: false}
];

export const RESULT_MAP = new Map([
  ['success', '通过'],
  ['error', '未通过'],
  ['default', '未执行']
]);

