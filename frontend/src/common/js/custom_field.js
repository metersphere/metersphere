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
  let hasOldData = false
  if (!data.customFields) {
    // 旧数据
    hasOldData = true;
    data.customFields = {};
  }
  if (!(data.customFields instanceof Object)) {
    data.customFields = JSON.parse(data.customFields);
  }
  // 设置页面显示的默认值
  template.customFields.forEach(item => {
    if (item.defaultValue) {
      item.defaultValue = JSON.parse(item.defaultValue);
    }

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

    for (const key in data.customFields) {
      if (item.name === key) {
        if (data.customFields[key]) {
          item.defaultValue = JSON.parse(data.customFields[key]);
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
    let customFields = data.customFields;
    template.customFields.forEach(item => {
      if (item.defaultValue) {
        customFields[item.name] = JSON.stringify(item.defaultValue);
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

// 兼容旧版本的步骤
export function compatibleTestCaseStep(testCase, tmp) {
  if(testCase.expectedResult !== null) { //  改成富文本后加入的新数据 或 经过兼容的旧数据
    tmp.stepResult = testCase.expectedResult + '<br>';
  } else {  //  如果是旧数据
    if(tmp.steps !== null) {
      tmp.stepResult = '';
      tmp.steps.forEach(item => {
        tmp.stepResult += item.num + ': ' + item.result + '<br>';
      });
    }
  }
  if(testCase.stepDescription !== null) {
    tmp.stepDesc = testCase.stepDescription + '<br>';
  } else {
    if(tmp.steps !== null) {
      tmp.stepDesc = '';
      tmp.steps.forEach(item => {
        tmp.stepDesc += item.num + ': ' + item.desc + '<br>';
      });
    }
  }
  if(!testCase.actualResult) {
    if(tmp.results) {
      tmp.actualResult = '';
      tmp.results.forEach(item => {
        if (item.actualResult) {
          tmp.actualResult += item.actualResult + '<br>';
        }
      });
    }
  }
}

// 兼容旧字段
export function buildTestCaseOldFields(testCase) {
  let oldFields = new Map();
  oldFields.set('i43sf4_testCaseStatus', testCase.status);
  oldFields.set('i43sf4_testCaseMaintainer', testCase.maintainer);
  oldFields.set('i43sf4_testCasePriority', testCase.priority);
  return oldFields;
}
