import i18n from '../../i18n/i18n'
import {SYSTEM_FIELD_NAME_MAP} from "@/common/js/table-constants";

function setDefaultValue(item, value) {
  item.defaultValue = value;
  item.hasParse = true; // 多次调用不执行这部分
}

/**
 * 设置默认值，添加自定义校验
 * @param data 原表单值
 * @param template 模板
 * @param customFieldForm 用于自定义表单的校验
 * @param rules 自定义表单的校验规则
 * @param oldFields 用于兼容旧版本数据
 */
export function parseCustomField(data, template, rules, oldFields) {
  let hasOldData = false;
  if (!data.fields) {
    // 旧数据
    hasOldData = true;
    data.fields = [];
  }

  let customFieldForm = {};

  // 设置页面显示的默认值
  template.customFields.forEach(item => {

    if (item.defaultValue && !item.hasParse) {
      setDefaultValue(item, JSON.parse(item.defaultValue));
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
            setDefaultValue(item, oldFields.get(key));
          }
        }
      }
    }

    // 将保存的值赋值给template
    if (data.fields instanceof Array) {
      for (let i = 0; i < data.fields.length; i++) {
        let customField = data.fields[i];
        if (customField.id === item.id) {
          try {
            setDefaultValue(item, JSON.parse(customField.value));
            item.isEdit = true;
          } catch (e) {
            console.log("JSON parse custom field value error.");
          }
          break;
        }
      }
    } else if (data.fields instanceof Object) { // todo
      // 兼容旧的存储方式
      for (const key in data.fields) {
        if (item.name === key) {
          if (data.fields[key]) {
            setDefaultValue(item, JSON.parse(data.fields[key]));
          }
        }
      }
    }

    customFieldForm[item.name] = item.defaultValue;
  });

  return customFieldForm;
}

// 将template的属性值设置给customFields
export function buildCustomFields(data, param, template) {
  if (template.customFields) {
    if (!(data.fields instanceof Array)) {
      data.fields = [];
    }

    let addFields = [];
    let editFields = [];

    template.customFields.forEach(item => {
      let customField = {
        fieldId: item.id,
        value: JSON.stringify(item.defaultValue),
      };
      if (item.isEdit) {
        editFields.push(customField);
      } else {
        addFields.push(customField);
      }
    });
    param.addFields = addFields;
    param.editFields = editFields;
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
