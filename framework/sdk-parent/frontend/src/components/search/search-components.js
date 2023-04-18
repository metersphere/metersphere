import MsTableSearchInput from "./MsTableSearchInput";
import MsTableSearchDateTimePicker from "./MsTableSearchDateTimePicker";
import MsTableSearchDatePicker from "./MsTableSearchDatePicker";
import MsTableSearchSelect from "./MsTableSearchSelect";
import MsTableSearchInputNumber from "./MsTableSearchInputNumber";
import MsTableSearchMix from "./MsTableSearchMix";
import MsTableSearchNodeTree from "./MsTableSearchNodeTree";


export default {
  MsTableSearchInput,
  MsTableSearchDatePicker,
  MsTableSearchDateTimePicker,
  MsTableSearchSelect,
  MsTableSearchInputNumber,
  MsTableSearchMix,
  MsTableSearchNodeTree
}

export const OPERATORS = {
  LIKE: {
    label: "commons.adv_search.operators.like",
    value: "like"
  },
  NOT_LIKE: {
    label: "commons.adv_search.operators.not_like",
    value: "not like"
  },
  IN: {
    label: "commons.adv_search.operators.in",
    value: "in"
  },
  NOT_IN: {
    label: "commons.adv_search.operators.not_in",
    value: "not in"
  },
  GT: {
    label: "commons.adv_search.operators.gt",
    value: "gt"
  },
  GE: {
    label: "commons.adv_search.operators.ge",
    value: "ge"
  },
  LT: {
    label: "commons.adv_search.operators.lt",
    value: "lt"
  },
  LE: {
    label: "commons.adv_search.operators.le",
    value: "le"
  },
  EQ: {
    label: "commons.adv_search.operators.equals",
    value: "eq"
  },
  BETWEEN: {
    label: "commons.adv_search.operators.between",
    value: "between"
  },
  CURRENT_USER: {
    label: "commons.adv_search.operators.current_user",
    value: "current user"
  },
}

const MS_USER_OPTIONS = { // 获取当前工作空间的用户列表
  url: "/user/ws/current/member/list",
  labelKey: "name",
  valueKey: "id",
  showLabel: option => {
    return option.label + "(" + option.value + ")";
  }
}

// 获取当前项目的用户列表
const MS_PROJECT_USER_OPTIONS = {
  url: "/user/project/member/list",
  labelKey: "name",
  valueKey: "id",
  showLabel: option => {
    return option.label + "(" + option.value + ")";
  }
}

// 名称
export const NAME = {
  key: "name", // 返回结果Map的key
  name: 'MsTableSearchInput', // Vue控件名称
  label: 'commons.name', // 显示名称
  operator: { // 运算符设置
    value: OPERATORS.LIKE.value, // 如果未设置value初始值，则value初始值为options[0]
    options: [OPERATORS.LIKE, OPERATORS.NOT_LIKE] // 运算符候选项
  },
}
// 更新时间
export const UPDATE_TIME = {
  key: "updateTime",
  name: 'MsTableSearchDateTimePicker',
  label: 'commons.update_time',
  operator: {
    options: [OPERATORS.BETWEEN, OPERATORS.GT, OPERATORS.LT]
  },
}
// 所属项目
export const PROJECT_NAME = {
  key: "projectName",
  name: 'MsTableSearchInput',
  label: 'commons.adv_search.project',
  operator: {
    options: [OPERATORS.LIKE, OPERATORS.NOT_LIKE]
  },
}
// 所属测试
export const TEST_NAME = {
  key: "testName",
  name: 'MsTableSearchInput',
  label: 'commons.adv_search.test',
  operator: {
    options: [OPERATORS.LIKE, OPERATORS.NOT_LIKE]
  },
}
// 测试计划
export const TEST_PLAN_NAME = {
  key: "testPlanName",
  name: 'MsTableSearchInput',
  label: 'test_track.report.list.test_plan',
  operator: {
    options: [OPERATORS.LIKE, OPERATORS.NOT_LIKE]
  },
}
// 创建时间
export const CREATE_TIME = {
  key: "createTime",
  name: 'MsTableSearchDateTimePicker',
  label: 'commons.create_time',
  operator: {
    options: [OPERATORS.BETWEEN, OPERATORS.GT, OPERATORS.LT]
  },
}
// 报告状态
export const STATUS = {
  key: "status",
  name: 'MsTableSearchSelect',
  label: 'commons.status',
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    { label: 'Pending', value: 'PENDING' },
    { label: 'Running', value: 'RUNNING' },
    { label: 'Rerunning', value: 'RERUNNING' },
    { label: 'Success', value: 'SUCCESS' },
    { label: 'Error', value: 'ERROR' },
    { label: 'FakeError', value: 'FAKE_ERROR' },
    { label: 'Stopped', value: 'STOPPED' },

  ],
  props: { // 尾部控件的props，一般为element ui控件的props
    multiple: true
  }
}

// 报告状态
export const PERFORMANCE_REPORT_STATUS = {
  key: "status",
  name: 'MsTableSearchSelect',
  label: 'commons.status',
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    {label: 'Starting', value: 'Starting'},
    {label: 'Running', value: 'Running'},
    {label: 'Reporting', value: 'Reporting'},
    {label: 'Completed', value: 'Completed'},
    {label: 'Error', value: 'Error'}
  ],
  props: { // 尾部控件的props，一般为element ui控件的props
    multiple: true
  }
}

// ui 报告状态
export const UI_REPORT_STATUS = {
  key: "status",
  name: 'MsTableSearchSelect',
  label: 'commons.status',
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    {label: "Running", value: "RUNNING"},
    {label: "Error", value: "ERROR"},
    {label: "Success", value: "SUCCESS"},
    {label: 'Stopped', value: 'STOPPED'},
    {label: "Pending", value: "PENDING"},
    {label: "Timeout", value: "TIMEOUT"},
  ],
  props: { // 尾部控件的props，一般为element ui控件的props
    multiple: true
  }
}
// api 状态
export const API_STATUS = {
  key: "status",
  name: 'MsTableSearchSelect',
  label: 'commons.status',
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    {value: 'Prepare', label: 'test_track.plan.plan_status_prepare'},
    {value: 'Underway', label: 'test_track.plan.plan_status_running'},
    {value: 'Completed', label: 'test_track.plan.plan_status_completed'}
  ],
  props: { // 尾部控件的props，一般为element ui控件的props
    multiple: true
  }
}

export const CASE_STATUS = {
  key: "case_status",
  name: 'MsTableSearchSelect',
  label: 'commons.status',
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    {value: 'Prepare', label: 'test_track.plan.plan_status_prepare'},
    {value: 'Underway', label: 'test_track.plan.plan_status_running'},
    {value: 'Completed', label: 'test_track.plan.plan_status_completed'}
  ],
  props: { // 尾部控件的props，一般为element ui控件的props
    multiple: true
  }
}

export const API_STATUS_TRASH = {
  key: "status",
  name: 'MsTableSearchSelect',
  label: 'commons.status',
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [{value: 'Trash', label: 'test_track.plan.plan_status_trash'}],
  props: { // 尾部控件的props，一般为element ui控件的props
    multiple: true
  }
}

// 用例执行结果
export const API_CASE_RESULT = {
  key: "exec_result",
  name: 'MsTableSearchSelect',
  label: 'test_track.plan_view.execute_result',
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    {text: 'Pending', value: 'PENDING'},
    {text: 'Running', value: 'RUNNING'},
    {text: 'Rerunning', value: 'RERUNNING'},
    {text: 'Success', value: 'SUCCESS'},
    {text: 'Error', value: 'ERROR'},
    {text: "FakeError", value: 'FAKE_ERROR'},
    {text: 'Stopped', value: 'STOPPED'},
  ],
  props: { // 尾部控件的props，一般为element ui控件的props
    multiple: true
  }
}
// 场景执行结果
export const API_SCENARIO_RESULT = {
  key: "lastResult",
  name: 'MsTableSearchSelect',
  label: 'test_track.plan_view.execute_result',
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    {text: 'Pending', value: 'PENDING'},
    {text: 'Running', value: 'RUNNING'},
    {text: 'Rerunning', value: 'RERUNNING'},
    {text: 'Success', value: 'SUCCESS'},
    {text: 'Error', value: 'ERROR'},
    {text: "FakeError", value: 'FAKE_ERROR'},
    {text: 'Stopped', value: 'STOPPED'},
  ],
  props: { // 尾部控件的props，一般为element ui控件的props
    multiple: true
  }
}
// 场景执行结果
export const UI_SCENARIO_RESULT = {
  key: "lastResult",
  name: 'MsTableSearchSelect',
  label: 'test_track.plan_view.execute_result',
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    {label: "Running", value: "RUNNING"},
    {label: "Error", value: "ERROR"},
    {label: "Success", value: "SUCCESS"},
    {label: 'Stopped', value: 'STOPPED'},
    {label: "Pending", value: "PENDING"},
    {label: "Timeout", value: "TIMEOUT"},
  ],
  props: { // 尾部控件的props，一般为element ui控件的props
    multiple: true
  }
}

// 请求类型
export const API_METHOD = {
  key: "method",
  name: 'MsTableSearchSelect',
  label: 'api_test.definition.api_type',
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    {value: 'GET', label: 'GET'},
    {value: 'POST', label: 'POST'},
    {value: 'PUT', label: 'PUT'},
    {value: 'PATCH', label: 'PATCH'},
    {value: 'DELETE', label: 'DELETE'},
    {value: 'OPTIONS', label: 'OPTIONS'},
    {value: 'HEAD', label: 'HEAD'},
    {value: 'CONNECT', label: 'CONNECT'},
    {value: 'DUBBO', label: 'DUBBO'},
    {value: 'dubbo://', label: 'dubbo://'},
    {value: 'SQL', label: 'SQL'},
    {value: 'TCP', label: 'TCP'}
  ],
  props: { // 尾部控件的props，一般为element ui控件的props
    multiple: true
  }
}
// api 路径
export const API_PATH = {
  key: "path", // 返回结果Map的key
  name: 'MsTableSearchInput', // Vue控件名称
  label: 'api_test.definition.api_path', // 显示名称
  operator: { // 运算符设置
    value: OPERATORS.LIKE.value, // 如果未设置value初始值，则value初始值为options[0]
    options: [OPERATORS.LIKE, OPERATORS.NOT_LIKE] // 运算符候选项
  },
}
// 标签
export const TAGS = {
  key: "tags", // 返回结果Map的key
  name: 'MsTableSearchInput', // Vue控件名称
  label: 'commons.tag', // 显示名称
  operator: { // 运算符设置
    value: OPERATORS.LIKE.value, // 如果未设置value初始值，则value初始值为options[0]
    options: [OPERATORS.LIKE, OPERATORS.NOT_LIKE] // 运算符候选项
  },
}
// 创建人
export const CREATOR = {
  key: "creator",
  name: 'MsTableSearchSelect',
  label: 'api_test.creator',
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN, OPERATORS.CURRENT_USER],
    change: function (component, value) { // 运算符change事件
      if (value === OPERATORS.CURRENT_USER.value) {
        component.value = value;
      }
    }
  },
  options: MS_PROJECT_USER_OPTIONS,
  props: {
    multiple: true
  },
  isShow: operator => {
    return operator !== OPERATORS.CURRENT_USER.value;
  }
}

// 创建人(仅当前项目)
export const PROJECT_CREATOR = {
  key: "creator",
  name: 'MsTableSearchSelect',
  label: 'api_test.creator',
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN, OPERATORS.CURRENT_USER],
    change: function (component, value) { // 运算符change事件
      if (value === OPERATORS.CURRENT_USER.value) {
        component.value = value;
      }
    }
  },
  options: MS_PROJECT_USER_OPTIONS,
  props: {
    multiple: true
  },
  isShow: operator => {
    return operator !== OPERATORS.CURRENT_USER.value;
  }
}

// 执行人
export const EXECUTOR = {
  key: "executor",
  name: 'MsTableSearchSelect',
  label: 'test_track.plan_view.executor',
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN, OPERATORS.CURRENT_USER],
    change: function (component, value) { // 运算符change事件
      if (value === OPERATORS.CURRENT_USER.value) {
        component.value = value;
      }
    }
  },
  options: MS_USER_OPTIONS,
  props: {
    multiple: true
  },
  isShow: operator => {
    return operator !== OPERATORS.CURRENT_USER.value;
  }
}
// 关注人
export const FOLLOW_PEOPLE = {
  key: "followPeople",
  name: 'MsTableSearchSelect',
  label: 'commons.follow_people',
  operator: {
    options: [OPERATORS.IN, OPERATORS.CURRENT_USER],
    change: function (component, value) { // 运算符change事件
      if (value === OPERATORS.CURRENT_USER.value) {
        component.value = value;
      }
    }
  },
  options: MS_USER_OPTIONS,
  props: {
    multiple: true
  },
  isShow: operator => {
    return operator !== OPERATORS.CURRENT_USER.value;
  }
}
// 引用
export const ISREFERENCE = {
  key: "isReference",
  name: 'MsTableSearchSelect',
  label: 'api_test.scenario.reference',
  operator: {
    options: [OPERATORS.IN]
  },
  options: [
    {value: '', label: 'commons.default'},
    {value: 'true', label: 'commons.yes'},
    {value: 'false', label: 'commons.no'}
  ],
  props: { // 尾部控件的props，一般为element ui控件的props
    multiple: false
  }
}
// 触发方式
export const TRIGGER_MODE = {
  key: "triggerMode",
  name: 'MsTableSearchSelect',
  label: 'commons.trigger_mode.name',
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    {label: 'test_track.report.trigger_mode.manual', value: 'manual'},
    {label: 'commons.trigger_mode.schedule', value: 'SCHEDULE'},
    {label: 'commons.trigger_mode.api', value: 'API'},
    {label: 'api_test.automation.batch_execute', value: 'BATCH'}
  ],
  props: {
    multiple: true
  }
}
// 触发方式
export const UI_TRIGGER_MODE = {
  key: "triggerMode",
  name: 'MsTableSearchSelect',
  label: 'commons.trigger_mode.name',
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    {label: "commons.trigger_mode.manual", value: "MANUAL"},
    {label: "api_test.automation.batch_execute", value: 'BATCH'},
    {label: 'commons.trigger_mode.schedule', value: 'SCHEDULE'},
    {label: 'commons.trigger_mode.api', value: 'API'},
  ],
  props: {
    multiple: true
  }
}
// 优先级
export const PRIORITY = {
  key: "priority",
  name: 'MsTableSearchSelect',
  label: "test_track.case.priority",
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    {label: "P0", value: "P0"},
    {label: "P1", value: "P1"},
    {label: "P2", value: "P2"},
    {label: "P3", value: "P3"},
  ],
  props: {
    multiple: true
  }
}
// 测试用例类型
export const TYPE = {
  key: "type",
  name: 'MsTableSearchSelect',
  label: "test_track.case.type",
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    {label: 'commons.functional', value: 'functional'},
    {label: 'commons.performance', value: 'performance'},
    {label: 'commons.api', value: 'api'}
  ],
  props: {
    multiple: true
  }
}
// 所属模块
export const MODULE = {
  key: "module",
  name: 'MsTableSearchInput',
  label: "test_track.case.module",
  operator: {
    value: OPERATORS.LIKE.value,
    options: [OPERATORS.LIKE, OPERATORS.NOT_LIKE]
  },
}
// 责任人
export const PRINCIPAL = {
  key: "principal",
  name: 'MsTableSearchSelect',
  label: 'test_track.plan.plan_principal',
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN, OPERATORS.CURRENT_USER],
    change: function (component, value) { // 运算符change事件
      if (value === OPERATORS.CURRENT_USER.value) {
        component.value = value;
      }
    }
  },
  options: MS_PROJECT_USER_OPTIONS,
  props: {
    multiple: true
  },
  isShow: operator => {
    return operator !== OPERATORS.CURRENT_USER.value;
  }
};

// 评审人
export const REVIEWER = {
  key: "reviewer",
  name: 'MsTableSearchSelect',
  label: 'test_track.review.reviewer',
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN, OPERATORS.CURRENT_USER],
    change: function (component, value) { // 运算符change事件
      if (value === OPERATORS.CURRENT_USER.value) {
        component.value = value;
      }
    }
  },
  options: MS_PROJECT_USER_OPTIONS,
  props: {
    multiple: true
  },
  isShow: operator => {
    return operator !== OPERATORS.CURRENT_USER.value;
  }
};

// 责任人
export const MAINTAINER = {
  key: "maintainer",
  name: 'MsTableSearchSelect',
  label: 'test_track.plan.plan_principal',
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN, OPERATORS.CURRENT_USER],
    change: function (component, value) { // 运算符change事件
      if (value === OPERATORS.CURRENT_USER.value) {
        component.value = value;
      }
    }
  },
  options: MS_PROJECT_USER_OPTIONS,
  props: {
    multiple: true
  },
  isShow: operator => {
    return operator !== OPERATORS.CURRENT_USER.value;
  }
};

export const API_PRINCIPAL = {};
Object.assign(API_PRINCIPAL, PRINCIPAL);
API_PRINCIPAL.key = 'creator';

// 测试阶段
export const STAGE = {
  key: "stage",
  name: 'MsTableSearchSelect',
  label: "test_track.plan.plan_stage",
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    {label: 'test_track.plan.smoke_test', value: 'smoke'},
    {label: 'test_track.plan.regression_test', value: 'regression'},
    {label: 'test_track.plan.system_test', value: 'system'}
  ],
  props: {
    multiple: true
  }
};
// 测试计划状态
export const TEST_PLAN_STATUS = {
  key: "status",
  name: 'MsTableSearchSelect',
  label: "test_track.plan.plan_status",
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    {label: 'test_track.plan.plan_status_prepare', value: 'Prepare'},
    {label: 'test_track.plan.plan_status_running', value: 'Underway'},
    {label: 'test_track.plan.plan_status_completed', value: 'Completed'},
    {label: 'test_track.plan.plan_status_finished', value: 'Finished'},
    {label: 'test_track.plan.plan_status_archived', value: 'Archived'}
  ],
  props: {
    multiple: true
  }
};
// 测试计划报告状态
export const TEST_PLAN_REPORT_STATUS = {
  key: "status",
  name: 'MsTableSearchSelect',
  label: "test_track.plan.plan_status",
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    {label: 'Starting', value: 'Starting'},
    {label: 'Running', value: 'Underway'},
    {label: 'Completed', value: 'Completed'}
  ],
  props: {
    multiple: true
  }
};
// 测试计划报告触发方式
export const TEST_PLAN_TRIGGER_MODE = {
  key: "triggerMode",
  name: 'MsTableSearchSelect',
  label: "test_track.report.list.trigger_mode",
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    {label: 'test_track.report.trigger_mode.manual', value: 'manual'},
    {label: 'commons.trigger_mode.schedule', value: 'SCHEDULE'},
    {label: 'commons.trigger_mode.api', value: 'API'},
    {label: 'api_test.automation.batch_execute', value: 'BATCH'}

  ],
  props: {
    multiple: true
  }
};

export const CASE_REVIEW_STATUS = {
  key: "reviewStatus",
  name: 'MsTableSearchSelect',
  label: "test_track.review_view.execute_result",
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    {label: "test_track.review.prepare", value: "Prepare"},
    {label: "test_track.review.pass", value: "Pass"},
    {label: "test_track.review.un_pass", value: "UnPass"},
    {label: "test_track.review.again", value: "Again"},
    {label: 'test_track.review.underway', value: 'Underway'},
  ],
  props: {
    multiple: true
  }
}

export const TEST_CASE_STATUS = {
  key: "status",
  name: 'MsTableSearchSelect',
  label: "custom_field.case_status",
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [],
  props: {
    multiple: true
  }
}

export const TEST_CASE_MAINTAINER = {
  key: "maintainer",
  name: 'MsTableSearchSelect',
  label: "custom_field.case_maintainer",
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: MS_PROJECT_USER_OPTIONS,
  props: {
    multiple: true
  }
}

export const TEST_CASE_PRIORITY = {
  key: "priority",
  name: 'MsTableSearchSelect',
  label: "custom_field.case_priority",
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [],
  props: {
    multiple: true
  }
}

export const PLAN_CASE_STATUS = {
  key: "planCaseStatus",
  name: 'MsTableSearchSelect',
  label: "test_track.plan_view.execute_result",
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    {label: "api_test.home_page.detail_card.unexecute", value: "Prepare"},
    {label: "test_track.plan_view.pass", value: "Pass"},
    {label: "test_track.plan_view.failure", value: "Failure"},
    {label: "test_track.plan_view.blocking", value: "Blocking"},
    {label: "test_track.plan_view.skip", value: "Skip"},
  ],
  props: {
    multiple: true
  }
}
// 缺陷所属平台
export const PLATFORM = {
  key: "platform",
  name: 'MsTableSearchSelect',
  label: "所属平台",
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [],
  props: {
    multiple: true
  }
}

export const STEP_COUNT = {
  key: "stepCount",
  name: 'MsTableSearchInputNumber',
  label: 'api_test.automation.step',
  operator: {
    options: [OPERATORS.GT, OPERATORS.GE, OPERATORS.LT, OPERATORS.LE, OPERATORS.EQ]
  },
}

// 测试计划 实际开始时间
export const ACTUAL_START_TIME = {
  key: "actualStartTime",
  name: 'MsTableSearchDateTimePicker',
  label: 'test_track.plan.actual_start_time',
  operator: {
    options: [OPERATORS.BETWEEN, OPERATORS.GT, OPERATORS.GE, OPERATORS.LT, OPERATORS.LE]
  },
}
// 测试计划 实际结束时间
export const ACTUAL_END_TIME = {
  key: "actualEndTime",
  name: 'MsTableSearchDateTimePicker',
  label: 'test_track.plan.actual_end_time',
  operator: {
    options: [OPERATORS.BETWEEN, OPERATORS.GT, OPERATORS.GE, OPERATORS.LT, OPERATORS.LE]
  },
}

// 测试计划 计划开始时间
export const PLAN_START_TIME = {
  key: "planStartTime",
  name: 'MsTableSearchDateTimePicker',
  label: 'test_track.plan.planned_start_time',
  operator: {
    options: [OPERATORS.BETWEEN, OPERATORS.GT, OPERATORS.GE, OPERATORS.LT, OPERATORS.LE]
  },
}

// 测试计划 计划结束时间
export const PLAN_END_TIME = {
  key: "planEndTime",
  name: 'MsTableSearchDateTimePicker',
  label: 'test_track.plan.planned_end_time',
  operator: {
    options: [OPERATORS.BETWEEN, OPERATORS.GT, OPERATORS.GE, OPERATORS.LT, OPERATORS.LE]
  },
}
// 测试评审 截止时间
export const END_TIME = {
  key: "endTime",
  name: 'MsTableSearchDateTimePicker',
  label: 'test_track.review.end_time',
  operator: {
    options: [OPERATORS.BETWEEN, OPERATORS.GT, OPERATORS.GE, OPERATORS.LT, OPERATORS.LE]
  },
}

// 用例需求
export const CASE_DEMAND = {
  key: "demand",
  name: "MsTableSearchMix",
  label: "test_track.related_requirements",
  operator: {
    options: [{
      label: "test_track.demand.third_platform_demand",
      value: "third_platform"
    },
      {
        label: "test_track.demand.other_demand",
        value: "other_platform"
      }],
    change: function (component, value) { // 运算符change事件
      component.showInput = value === 'other_platform';
      component.value = "";
    }
  },
  reset() { // 重置搜索时执行
    this.showInput = false;
  },
  options: {
    url: "/issues/demand/list",
    labelKey: "name",
    valueKey: "id",
    showLabel: option => {
      return option.label;
    }
  },
  props: {
    multiple: true,
    'collapse-tags': true
  }
}

export const ID = {
  key: "id",
  name: 'MsTableSearchInput',
  label: 'ID',
  operator: {
    options: [OPERATORS.LIKE, OPERATORS.NOT_LIKE]
  }
}

// 平台状态
export const PLATFORM_STATUS = {
  key: "platformStatus",
  name: 'MsTableSearchSelect',
  label: "平台状态",
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [],
  props: {
    multiple: true
  }
}

function _getModuleTree(options) {
  return {
    key: "moduleIds",
    name: 'MsTableSearchNodeTree',
    label: "test_track.case.module",
    operator: {
      value: OPERATORS.IN.value,
      options: [OPERATORS.IN, OPERATORS.NOT_IN]
    },
    options: options,
    init: undefined // 高级搜索框非首次打开时会执行该函数，在组件首次created时给其赋值
  }
}

export const TEST_CASE_MODULE_TREE = _getModuleTree({
  url: "/case/node/list",
  type: "POST",
  params: {} // 赋值时注意顺序
})

export const API_MODULE_TREE = _getModuleTree({
  url: "/api/module/list",
  type: "GET",
  params: {}
})

export const SCENARIO_MODULE_TREE = _getModuleTree({
  url: "/api/automation/module/list",
  type: "GET",
  params: {}
})

export const UI_MODULE_TREE = _getModuleTree({
  url: "/ui/scenario/module/list",
  type: "GET",
  params: {}
})

export const UI_TRASH_MODULE_TREE = _getModuleTree({
  url: "/ui/scenario/module/trash/list",
  type: "GET",
  params: {}
})

export const UI_CUSTOM_COMMAND_MODULE_TREE = _getModuleTree({
  url: "/ui/scenario/module/custom/list",
  type: "GET",
  params: {}
})

export const UI_CUSTOM_COMMAND_TRASH_MODULE_TREE = _getModuleTree({
  url: "/ui/scenario/module/custom/trash/list",
  type: "GET",
  params: {}
})


export const TEST_CONFIGS = [ID, NAME, UPDATE_TIME, CREATE_TIME, STATUS, CREATOR, FOLLOW_PEOPLE];

export const PROJECT_CONFIGS = [NAME, UPDATE_TIME, CREATE_TIME, CREATOR];

export const REPORT_SCENARIO_CONFIGS = [NAME, TEST_NAME, CREATE_TIME, STATUS, CREATOR, TRIGGER_MODE];

export const REPORT_CONFIGS = [NAME, TEST_NAME, CREATE_TIME, PERFORMANCE_REPORT_STATUS, CREATOR, TRIGGER_MODE];

export const REPORT_CASE_CONFIGS = [NAME, CREATE_TIME, STATUS, CREATOR, TRIGGER_MODE];

export const UI_REPORT_CONFIGS = [NAME, TEST_NAME, CREATE_TIME, UI_REPORT_STATUS, PROJECT_CREATOR, UI_TRIGGER_MODE];

export const UI_SCENARIO_CONFIGS = [NAME, CREATE_TIME, API_STATUS, PROJECT_CREATOR, UI_MODULE_TREE];
export const UI_SCENARIO_CONFIGS_TRASH = [NAME, CREATE_TIME, API_STATUS_TRASH, PROJECT_CREATOR, UI_TRASH_MODULE_TREE];

export const UI_CUSTOM_COMMAND_CONFIGS = [NAME, CREATE_TIME, PROJECT_CREATOR, UI_CUSTOM_COMMAND_MODULE_TREE];
export const UI_CUSTOM_COMMAND_CONFIGS_TRASH = [NAME, CREATE_TIME, PROJECT_CREATOR, UI_CUSTOM_COMMAND_TRASH_MODULE_TREE];

// 测试跟踪-测试用例 列表
export const TEST_CASE_CONFIGS = [ID, NAME, TAGS, TEST_CASE_MODULE_TREE, CREATE_TIME, UPDATE_TIME, CREATOR, CASE_REVIEW_STATUS, FOLLOW_PEOPLE, CASE_DEMAND, TEST_CASE_STATUS, TEST_CASE_PRIORITY, TEST_CASE_MAINTAINER];

export const TEST_PLAN_CONFIGS = [NAME, UPDATE_TIME, CREATE_TIME, PRINCIPAL, TEST_PLAN_STATUS, STAGE, TAGS, FOLLOW_PEOPLE, ACTUAL_START_TIME, ACTUAL_END_TIME, PLAN_START_TIME, PLAN_END_TIME];

// 测试跟踪 测试评审列表
export const TEST_REVIEW = [NAME, CREATOR, TAGS, TEST_PLAN_STATUS, FOLLOW_PEOPLE, CREATE_TIME, UPDATE_TIME, END_TIME];
export const TEST_REVIEW_CASE = [NAME, REVIEWER, MAINTAINER];
export const TEST_REVIEW_RELEVANCE_CASE_CONFIGS = [ID, NAME, TAGS, TEST_CASE_MODULE_TREE, CREATE_TIME, UPDATE_TIME, CREATOR, CASE_REVIEW_STATUS, FOLLOW_PEOPLE, CASE_DEMAND, TEST_CASE_STATUS, TEST_CASE_PRIORITY, TEST_CASE_MAINTAINER];

export const API_DEFINITION_CONFIGS = [ID, NAME, API_METHOD, API_PATH, API_STATUS, TAGS, UPDATE_TIME, CREATE_TIME, API_PRINCIPAL, API_MODULE_TREE, FOLLOW_PEOPLE];
export const API_DEFINITION_CONFIGS_TRASH = [ID, NAME, API_METHOD, API_PATH, API_STATUS_TRASH, TAGS, UPDATE_TIME, CREATE_TIME, API_PRINCIPAL, API_MODULE_TREE, FOLLOW_PEOPLE];

export const API_CASE_CONFIGS = [ID, NAME, PRIORITY, TAGS, API_CASE_RESULT, UPDATE_TIME, CREATE_TIME, CREATOR, FOLLOW_PEOPLE, API_PATH];

export const API_CASE_CONFIGS_TRASH = [ID, NAME, PRIORITY, TAGS, UPDATE_TIME, CREATE_TIME, CREATOR, FOLLOW_PEOPLE, API_PATH];

export const API_SCENARIO_CONFIGS = [ID, NAME, PRIORITY, TAGS, API_SCENARIO_RESULT, UPDATE_TIME, CREATE_TIME, CREATOR, FOLLOW_PEOPLE, STEP_COUNT, SCENARIO_MODULE_TREE, API_STATUS];
export const API_SCENARIO_CONFIGS_TRASH = [ID, NAME, PRIORITY, TAGS, API_SCENARIO_RESULT, UPDATE_TIME, CREATE_TIME, CREATOR, FOLLOW_PEOPLE, STEP_COUNT, SCENARIO_MODULE_TREE, API_STATUS_TRASH];

export const TEST_PLAN_REPORT_CONFIGS = [NAME, TEST_PLAN_NAME, CREATOR, CREATE_TIME, TEST_PLAN_TRIGGER_MODE, TEST_PLAN_REPORT_STATUS];

// 测试计划 功能用例
export const TEST_PLAN_TEST_CASE_CONFIGS = [NAME, TAGS, MODULE, PRIORITY, CREATE_TIME, UPDATE_TIME, EXECUTOR, CASE_REVIEW_STATUS, PLAN_CASE_STATUS, PRINCIPAL, CASE_DEMAND];
export const TEST_PLAN_API_CASE_CONFIGS = [NAME, CASE_STATUS, CREATE_TIME, UPDATE_TIME];
export const TEST_PLAN_API_SCENARIO_CONFIGS = [NAME, API_STATUS, CREATE_TIME, UPDATE_TIME];

// 测试计划关联页面
export const TEST_PLAN_RELEVANCE_FUNC_CONFIGS = [ID, NAME, TAGS, TEST_CASE_MODULE_TREE, CREATE_TIME, UPDATE_TIME, CREATOR, CASE_REVIEW_STATUS, PLAN_CASE_STATUS, FOLLOW_PEOPLE, CASE_DEMAND, TEST_CASE_STATUS, TEST_CASE_PRIORITY, TEST_CASE_MAINTAINER];
export const TEST_PLAN_RELEVANCE_API_DEFINITION_CONFIGS = [NAME, API_METHOD, API_PATH, TAGS, UPDATE_TIME, CREATE_TIME, CREATOR, API_STATUS];
export const TEST_PLAN_RELEVANCE_API_CASE_CONFIGS = [NAME, PRIORITY, TAGS, UPDATE_TIME, CREATOR, API_PATH, CASE_STATUS];
export const TEST_PLAN_RELEVANCE_API_SCENARIO_CONFIGS = [NAME, PRIORITY, TAGS, API_SCENARIO_RESULT, CREATE_TIME, UPDATE_TIME, CREATOR, API_STATUS];
export const TEST_PLAN_RELEVANCE_UI_SCENARIO_CONFIGS = [NAME, PRIORITY, TAGS, UI_SCENARIO_RESULT, CREATE_TIME, UPDATE_TIME, CREATOR];
export const TEST_PLAN_RELEVANCE_LOAD_CASE = [NAME, STATUS, CREATE_TIME, UPDATE_TIME, CREATOR];

// 测试用例关联测试
export const TEST_CASE_RELEVANCE_API_CASE_CONFIGS = [NAME, PRIORITY, TAGS, CREATOR];
export const TEST_CASE_RELEVANCE_API_SCENARIO_CONFIGS = [NAME, PRIORITY, TAGS, CREATOR];
export const TEST_CASE_RELEVANCE_LOAD_CASE = [NAME, STATUS, CREATE_TIME, UPDATE_TIME, CREATOR];


// 测试跟踪-缺陷管理-缺陷列表
export const TEST_TRACK_ISSUE_LIST = [NAME, PLATFORM, CREATE_TIME, UPDATE_TIME, CREATOR,PLATFORM_STATUS];

// 测试跟踪-测试用例-关联缺陷
export const TEST_CASE_RELEVANCE_ISSUE_LIST = [NAME, PLATFORM, CREATE_TIME, CREATOR];
