import MsTableSearchInput from "./MsTableSearchInput";
import MsTableSearchDateTimePicker from "./MsTableSearchDateTimePicker";
import MsTableSearchDatePicker from "./MsTableSearchDatePicker";
import MsTableSearchSelect from "./MsTableSearchSelect";
import i18n from "../../../../../i18n/i18n";
import _ from "lodash"

export default {
  MsTableSearchInput, MsTableSearchDatePicker, MsTableSearchDateTimePicker, MsTableSearchSelect
}

export const OPERATORS = {
  LIKE: {
    label: i18n.t("commons.adv_search.operators.like"),
    value: "like"
  },
  NOT_LIKE: {
    label: i18n.t("commons.adv_search.operators.not_like"),
    value: "not like"
  },
  IN: {
    label: i18n.t("commons.adv_search.operators.in"),
    value: "in"
  },
  NOT_IN: {
    label: i18n.t("commons.adv_search.operators.not_in"),
    value: "not in"
  },
  GT: {
    label: i18n.t("commons.adv_search.operators.gt"),
    value: "gt"
  },
  GE: {
    label: i18n.t("commons.adv_search.operators.ge"),
    value: "ge"
  },
  LT: {
    label: i18n.t("commons.adv_search.operators.lt"),
    value: "lt"
  },
  LE: {
    label: i18n.t("commons.adv_search.operators.le"),
    value: "le"
  },
  EQ: {
    label: i18n.t("commons.adv_search.operators.equals"),
    value: "eq"
  },
  BETWEEN: {
    label: i18n.t("commons.adv_search.operators.between"),
    value: "between"
  },
  CURRENT_USER: {
    label: i18n.t("commons.adv_search.operators.current_user"),
    value: "current user"
  },
}

export const NAME = {
  key: "name", // 返回结果Map的key
  name: 'MsTableSearchInput', // Vue控件名称
  label: i18n.t('commons.name'), // 显示名称
  operator: { // 运算符设置
    value: OPERATORS.LIKE.value, // 如果未设置value初始值，则value初始值为options[0]
    options: [OPERATORS.LIKE, OPERATORS.NOT_LIKE] // 运算符候选项
  },
}

export const UPDATE_TIME = {
  key: "updateTime",
  name: 'MsTableSearchDateTimePicker',
  label: i18n.t('commons.update_time'),
  operator: {
    options: [OPERATORS.BETWEEN, OPERATORS.GT, OPERATORS.GE, OPERATORS.LT, OPERATORS.LE, OPERATORS.EQ]
  },
}
export const PROJECT_NAME = {
  key: "projectName",
  name: 'MsTableSearchInput',
  label: i18n.t('commons.adv_search.project'),
  operator: {
    options: [OPERATORS.LIKE, OPERATORS.NOT_LIKE]
  },
}
export const TEST_NAME = {
  key: "testName",
  name: 'MsTableSearchInput',
  label: i18n.t('commons.adv_search.test'),
  operator: {
    options: [OPERATORS.LIKE, OPERATORS.NOT_LIKE]
  },
}
export const CREATE_TIME = {
  key: "createTime",
  name: 'MsTableSearchDateTimePicker',
  label: i18n.t('commons.create_time'),
  operator: {
    options: [OPERATORS.BETWEEN, OPERATORS.GT, OPERATORS.GE, OPERATORS.LT, OPERATORS.LE, OPERATORS.EQ]
  },
}

export const STATUS = {
  key: "status",
  name: 'MsTableSearchSelect',
  label: i18n.t('commons.status'),
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    {label: "Saved", value: "Saved"}, {label: "Starting", value: "Starting"},
    {label: "Running", value: "Running"}, {label: "Reporting", value: "Reporting"},
    {label: "Completed", value: "Completed"}, {label: "Error", value: "Error"}
  ],
  props: { // 尾部控件的props，一般为element ui控件的props
    multiple: true
  }
}

export const CREATOR = {
  key: "creator",
  name: 'MsTableSearchSelect',
  label: i18n.t('api_test.creator'),
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN, OPERATORS.CURRENT_USER],
    change: function (component, value) { // 运算符change事件
      if (value === OPERATORS.CURRENT_USER.value) {
        component.value = value;
      }
    }
  },
  options: { // 异步获取候选项
    url: "/user/list",
    labelKey: "name",
    valueKey: "id",
    showLabel: option => {
      return option.label + "(" + option.value + ")";
    }
  },
  props: {
    multiple: true
  },
  isShow: operator => {
    return operator !== OPERATORS.CURRENT_USER.value;
  }
}

export const TRIGGER_MODE = {
  key: "triggerMode",
  name: 'MsTableSearchSelect',
  label: i18n.t('commons.trigger_mode.name'),
  operator: {
    options: [OPERATORS.IN, OPERATORS.NOT_IN]
  },
  options: [
    {label: i18n.t("commons.trigger_mode.manual"), value: "MANUAL"},
    {label: i18n.t("commons.trigger_mode.schedule"), value: "SCHEDULE"},
    {label: i18n.t("commons.trigger_mode.api"), value: "API"}
  ],
  props: {
    multiple: true
  }
}

export const getTestConfigs = () => {
  return _.cloneDeep([NAME, UPDATE_TIME, PROJECT_NAME, CREATE_TIME, STATUS, CREATOR]);
}

export const getReportConfigs = () => {
  return _.cloneDeep([NAME, TEST_NAME, PROJECT_NAME, CREATE_TIME, STATUS, CREATOR, TRIGGER_MODE]);
}
