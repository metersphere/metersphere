import {OPERATORS} from "@/business/components/common/components/search/search-components";

export function getAdvSearchCustomField(componentArr, fields) {
  const components = [];
  for (let field of fields) {
    let index = componentArr.findIndex(a => a.key === field.id);
    if (index > -1) {
      continue;
    }
    const componentType = getComponentName(field.type);
    let component = {
      key: field.id,
      name: componentType,
      label: field.name,
      operator: getComponentOperator(componentType, field.type, false), // 自定义字段可以异步获取选项？
      options: getComponentOptions(field),
      custom: true,
    }
    // 作为搜索条件时，可以多选
    if (componentType === 'MsTableSearchSelect') {
      component['props'] = {
        multiple: true
      }
    }
    components.push(component);
  }
  return components;
}

function getComponentOptions(field) {
  const fieldOptions = field.options;
  let type = field.type;
  let options = [];
  if (fieldOptions.length === 0 && field.type !== 'member' && field.type !== 'multipleMember') {
    return options;
  }

  if (type === 'member' || type === 'multipleMember') {
    options = { // 异步获取候选项
      url: "/user/list",
      labelKey: "name",
      valueKey: "id",
      showLabel: option => {
        return option.label + "(" + option.value + ")";
      }
    }
  }

  for (let option of fieldOptions) {
    let temp = {
      value: option.value,
      label: option.text
    }
    options.push(temp);
  }
  return options;
}

function getComponentName(type) {
  switch (type) {
    case 'input':
    case 'textarea':
    case 'richText':
      return 'MsTableSearchInput';
    case 'select':
    case 'multipleSelect':
    case 'radio':
    case 'checkbox':
    case 'member':
    case 'multipleMember':
      return 'MsTableSearchSelect';
    case 'date':
      return 'MsTableSearchDatePicker';
    case 'datetime':
      return 'MsTableSearchDateTimePicker';
    case 'int':
    case 'float':
    case 'multipleInput':
      return 'MsTableSearchInput'; // todo 创建对应组件
    default:
      return 'MsTableSearchInput';
  }
}

function getComponentOperator(componentType, fieldType, async) {
  let operator = {};
  switch (componentType) {
    case 'MsTableSearchInput':
      operator = { // 运算符设置
        value: OPERATORS.LIKE.value, // 如果未设置value初始值，则value初始值为options[0]
        options: [OPERATORS.LIKE, OPERATORS.NOT_LIKE] // 运算符候选项
      }
      break;
    case 'MsTableSearchSelect':
      if (async || fieldType === 'member' || fieldType === 'multipleMember') {
        operator = {
          options: [OPERATORS.IN, OPERATORS.NOT_IN, OPERATORS.CURRENT_USER],
          //todo
          change: function (component, value) { // 运算符change事件
            if (value === OPERATORS.CURRENT_USER.value) {
              component.value = value;
            }
          }
        }
      } else {
        operator = {
          options: [OPERATORS.IN, OPERATORS.NOT_IN]
        }
      }
      break;
    case 'MsTableSearchDatePicker':
    case 'MsTableSearchDateTimePicker':
      operator = {
        options: [OPERATORS.BETWEEN, OPERATORS.GT, OPERATORS.GE, OPERATORS.LT, OPERATORS.LE, OPERATORS.EQ]
      }
      break;
  }
  return operator;
}

export function _findByKey(components, key) {
  return components.find(co => co.key === key);
}

export function _findIndexByKey(components, key) {
  return components.findIndex(co => co.key === key);
}
