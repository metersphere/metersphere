import MsTableSearchInput from "./MsTableSearchInput";
import MsTableSearchDateTimePicker from "./MsTableSearchDateTimePicker";
import MsTableSearchDatePicker from "./MsTableSearchDatePicker";
import MsTableSearchSelect from "./MsTableSearchSelect";

export default {
  MsTableSearchInput, MsTableSearchDatePicker, MsTableSearchDateTimePicker, MsTableSearchSelect
}

export const LOGIC = {
  AND: {
    label: "commons.adv_search.and",
    value: "and"
  },
  OR: {
    label: "commons.adv_search.or",
    value: "or"
  },
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
    value: ">"
  },
  GE: {
    label: "commons.adv_search.operators.ge",
    value: ">="
  },
  LT: {
    label: "commons.adv_search.operators.lt",
    value: "<"
  },
  LE: {
    label: "commons.adv_search.operators.le",
    value: "<="
  },
  EQ: {
    label: "commons.adv_search.operators.equals",
    value: "=="
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

export const TEST_NAME = {
  key: "name",
  name: 'MsTableSearchInput',
  labelI18n: 'commons.name',
  operators: [OPERATORS.LIKE, OPERATORS.NOT_LIKE],
}

export const UPDATE_TIME = {
  key: "updateTime",
  name: 'MsTableSearchDateTimePicker',
  labelI18n: 'commons.update_time',
  operators: [OPERATORS.BETWEEN, OPERATORS.GT, OPERATORS.GE, OPERATORS.LT, OPERATORS.LE, OPERATORS.EQ],
}
export const PROJECT_NAME = {
  key: "projectName",
  name: 'MsTableSearchInput',
  labelI18n: 'load_test.project_name',
  operators: [OPERATORS.LIKE, OPERATORS.NOT_LIKE],
}
export const CREATE_TIME = {
  key: "createTime",
  name: 'MsTableSearchDateTimePicker',
  labelI18n: 'commons.create_time',
  operators: [OPERATORS.BETWEEN, OPERATORS.GT, OPERATORS.GE, OPERATORS.LT, OPERATORS.LE, OPERATORS.EQ],
}

export const STATUS = {
  key: "status",
  name: 'MsTableSearchSelect',
  labelI18n: 'commons.status',
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
  labelI18n: 'api_test.creator',
  operators: [OPERATORS.IN, OPERATORS.NOT_IN, OPERATORS.CURRENT_USER],
  options: {
    url: "/user/list",
    label: "name",
    value: "id",
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
  labelI18n: 'commons.trigger_mode.name',
  operators: [OPERATORS.IN, OPERATORS.NOT_IN],
  options: [
    {label: "commons.trigger_mode.manual", value: "MANUAL"},
    {label: "commons.trigger_mode.schedule", value: "SCHEDULE"},
    {label: "commons.trigger_mode.api", value: "API"}
  ],
  props: {
    multiple: true
  }
}

export const TEST_CONFIGS = [TEST_NAME, UPDATE_TIME, PROJECT_NAME, CREATE_TIME, STATUS, CREATOR]

export const REPORT_CONFIGS = [TEST_NAME, UPDATE_TIME, PROJECT_NAME, CREATE_TIME, STATUS, CREATOR, TRIGGER_MODE]
