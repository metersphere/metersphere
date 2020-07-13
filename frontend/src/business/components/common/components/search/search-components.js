import MsTableSearchInput from "./MsTableSearchInput";
import MsTableSearchDateTimePicker from "./MsTableSearchDateTimePicker";
import MsTableSearchDatePicker from "./MsTableSearchDatePicker";
import MsTableSearchSelect from "./MsTableSearchSelect";
import i18n from "../../../../../i18n/i18n";

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
    value: ">"
  },
  GE: {
    label: i18n.t("commons.adv_search.operators.ge"),
    value: ">="
  },
  LT: {
    label: i18n.t("commons.adv_search.operators.lt"),
    value: "<"
  },
  LE: {
    label: i18n.t("commons.adv_search.operators.le"),
    value: "<="
  },
  EQ: {
    label: i18n.t("commons.adv_search.operators.equals"),
    value: "=="
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
  key: "name",
  name: 'MsTableSearchInput',
  label: i18n.t('commons.name'),
  operators: [OPERATORS.LIKE, OPERATORS.NOT_LIKE],
}

export const UPDATE_TIME = {
  key: "updateTime",
  name: 'MsTableSearchDateTimePicker',
  label: i18n.t('commons.update_time'),
  operators: [OPERATORS.BETWEEN, OPERATORS.GT, OPERATORS.GE, OPERATORS.LT, OPERATORS.LE, OPERATORS.EQ],
}
export const PROJECT_NAME = {
  key: "projectName",
  name: 'MsTableSearchInput',
  label: i18n.t('commons.adv_search.project'),
  operators: [OPERATORS.LIKE, OPERATORS.NOT_LIKE],
}
export const TEST_NAME = {
  key: "testName",
  name: 'MsTableSearchInput',
  label: i18n.t('commons.adv_search.test'),
  operators: [OPERATORS.LIKE, OPERATORS.NOT_LIKE],
}
export const CREATE_TIME = {
  key: "createTime",
  name: 'MsTableSearchDateTimePicker',
  label: i18n.t('commons.create_time'),
  operators: [OPERATORS.BETWEEN, OPERATORS.GT, OPERATORS.GE, OPERATORS.LT, OPERATORS.LE, OPERATORS.EQ],
}

export const STATUS = {
  key: "status",
  name: 'MsTableSearchSelect',
  label: i18n.t('commons.status'),
  operators: [OPERATORS.IN, OPERATORS.NOT_IN],
  options: [
    {label: "Saved", value: "Saved"}, {label: "Starting", value: "Starting"},
    {label: "Running", value: "Running"}, {label: "Reporting", value: "Reporting"},
    {label: "Completed", value: "Completed"}, {label: "Error", value: "Error"}
  ],
  props: {
    multiple: true
  }
}

export const CREATOR = {
  key: "creator",
  name: 'MsTableSearchSelect',
  label: i18n.t('api_test.creator'),
  operators: [OPERATORS.IN, OPERATORS.NOT_IN, OPERATORS.CURRENT_USER],
  options: {
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
  showContent: operator => {
    return operator !== OPERATORS.CURRENT_USER.value;
  }
}

export const TRIGGER_MODE = {
  key: "triggerMode",
  name: 'MsTableSearchSelect',
  label: i18n.t('commons.trigger_mode.name'),
  operators: [OPERATORS.IN, OPERATORS.NOT_IN],
  options: [
    {label: i18n.t("commons.trigger_mode.manual"), value: "MANUAL"},
    {label: i18n.t("commons.trigger_mode.schedule"), value: "SCHEDULE"},
    {label: i18n.t("commons.trigger_mode.api"), value: "API"}
  ],
  props: {
    multiple: true
  }
}

export const TEST_CONFIGS = [NAME, UPDATE_TIME, PROJECT_NAME, CREATE_TIME, STATUS, CREATOR]

export const REPORT_CONFIGS = [NAME, TEST_NAME, PROJECT_NAME, CREATE_TIME, STATUS, CREATOR, TRIGGER_MODE]
