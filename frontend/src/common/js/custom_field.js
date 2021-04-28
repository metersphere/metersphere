import i18n from '../../i18n/i18n'
import {SYSTEM_FIELD_NAME_MAP} from "@/common/js/table-constants";

/**
 * 设置默认值，添加自定义校验
 * @param data 原表单值
 * @param template 模板
 * @param customFieldForm 用于自定义表单的校验
 * @param rules 自定义表单的校验规则
 * @param oldFields 用于兼容旧版本数据
 */
export function parseCustomField(data, template, customFieldForm, rules, oldFields) {
  let hasOldData = false;
  if (!data.customFields) {
    // 旧数据
    hasOldData = true;
    data.customFields = {};
  }
  if (!(data.customFields instanceof Object) && !(data.customFields instanceof Array)) {
    data.customFields = JSON.parse(data.customFields);
  }

  // 设置页面显示的默认值
  template.customFields.forEach(item => {

    if (item.defaultValue && !item.hasParse) {
      item.defaultValue = JSON.parse(item.defaultValue);
      item.hasParse = true; // 多次调用不执行这部分
    }

    // 添加自定义字段必填校验
    if (item.required) {
      let msg = (item.system ? i18n.t(SYSTEM_FIELD_NAME_MAP[item.name]) : item.name) + i18n.t('commons.cannot_be_null');
      if (rules) {
        rules[item.name] = [
          {required: true,  message: msg,  trigger: 'blur'}
        ];
      }
    }

    if (hasOldData && oldFields) {
      // 兼容旧数据
      for (const key of oldFields.keys()) {
        if (item.name === key) {
          if (oldFields.get(key)) {
            item.defaultValue = oldFields.get(key);
          }
        }
      }
    }

    // 将保存的值赋值给template
    if (data.customFields instanceof Array) {
      for (let i = 0; i < data.customFields.length; i++) {
        let customField = data.customFields[i];
        if (customField.id === item.id) {
          item.defaultValue = customField.value;
          break;
        }
      }
    } else if (data.customFields instanceof Object) {
      // 兼容旧的存储方式
      for (const key in data.customFields) {
        if (item.name === key) {
          if (data.customFields[key]) {
            item.defaultValue = JSON.parse(data.customFields[key]);
          }
        }
      }
    }

    if (customFieldForm) {
      customFieldForm[item.name] = item.defaultValue;
    }
  });
}

// 将template的属性值设置给customFields
export function buildCustomFields(data, param, template) {
  if (template.customFields) {
    if (!(data.customFields instanceof Array)) {
      data.customFields = [];
    }
    let customFields = data.customFields;
    template.customFields.forEach(item => {
      let hasField = false;
      for (const index in customFields) {
        if (customFields[index].id === item.id) {
          hasField = true;
          customFields[index].name = item.name;
          customFields[index].value = item.defaultValue;
          break;
        }
      }
      if (!hasField) {
        let customField = {
          id: item.id,
          name: item.name,
          value: item.defaultValue
        };
        customFields.push(customField);
      }
    });
    param.customFields = JSON.stringify(customFields);
  }
}


export function getTemplate(baseUrl, vueObj) {
  return new Promise((resolve) => {
    let template = {};
    vueObj.$get(baseUrl + vueObj.projectId, (response) => {
      template = response.data;
      if (template.customFields) {
        template.customFields.forEach(item => {
          if (item.options) {
            item.options = JSON.parse(item.options);
          }
        });
      }
      resolve(template);
    });
  });
}

// 兼容旧字段
export function buildTestCaseOldFields(testCase) {
  let oldFields = new Map();
  oldFields.set('用例状态', testCase.status);
  oldFields.set('责任人', testCase.maintainer);
  oldFields.set('用例等级', testCase.priority);
  return oldFields;
}
