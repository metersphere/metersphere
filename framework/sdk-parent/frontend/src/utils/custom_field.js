import i18n from "../i18n";
import {getCurrentProjectID, getCurrentUser} from "../utils/token";
import { SYSTEM_FIELD_NAME_MAP } from "../utils/table-constants";
import {OPTION_LABEL_PREFIX} from "../utils/tableUtils";

function setDefaultValue(item, value) {
  item.defaultValue = value;
  item.hasParse = true; // 多次调用不执行这部分
}

/**
 * 设置默认值，添加自定义校验
 * @param data 原表单值
 * @param template 模板
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
  template.customFields.forEach((item) => {
    if (item.defaultValue && !item.hasParse) {
      let val = item.defaultValue;
      try {
        val = JSON.parse(item.defaultValue);
      } catch (e) {
        //
      }
      if (
        item.name === "责任人" &&
        item.system &&
        val &&
        val === "CURRENT_USER"
      ) {
        val = '';
        const {id, userGroups} = getCurrentUser();
        if (userGroups) {
          // CURRENT_USER是否是当前项目下的成员
          let index = userGroups.findIndex(ug => ug.sourceId === getCurrentProjectID());
          if (index !== -1) {
            val = id;
          }
        }
      }
      setDefaultValue(item, val);
    }

    // 添加自定义字段必填校验
    if (item.required) {
      let msg =
        (item.system ? i18n.t(SYSTEM_FIELD_NAME_MAP[item.name]) : item.name) +
        i18n.t("commons.cannot_be_null");
      if (rules) {
        rules[item.name] = [{ required: true, message: msg, trigger: "blur" }];
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
            if (item.type === "textarea" || item.type === "richText") {
              setDefaultValue(item, "\"" + customField.textValue + "\"");
            } else {
              setDefaultValue(item, customField.value);
            }
            if (customField.textValue && customField.textValue.startsWith(OPTION_LABEL_PREFIX))  {
              // 处理 jira 的 sprint 字段，没有选项，则添加对应选项
              if (item.options && item.options.filter(i => i.value === customField.value).length < 1) {
                item.options.push({
                  'text': customField.textValue.substring(OPTION_LABEL_PREFIX.length),
                  'value': customField.value
                });
              }
            }
            item.isEdit = true;
          } catch (e) {
            console.error("JSON parse custom field value error.", e);
          }
          break;
        }
      }
    } else if (data.fields instanceof Object) {
      // todo
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
    let requestFields = [];

    template.customFields.forEach((item) => {
      let customField = { fieldId: item.id };
      if (["richText", "textarea"].indexOf(item.type) > -1) {
        customField["textValue"] = item.defaultValue;
      } else {
        customField["value"] = item.defaultValue
          ? JSON.stringify(item.defaultValue)
          : "";
        // 处理 jira 的 sprint 字段
        if (item.optionLabel) {
          customField["textValue"] = item.optionLabel;
        }
      }
      if (item.isEdit) {
        editFields.push(customField);
      } else {
        addFields.push(customField);
      }
      let fieldValue =
        item.defaultValue instanceof Array && item.type !== "multipleInput"
          ? JSON.stringify(item.defaultValue)
          : item.defaultValue || "";
      let requestField = {
        id: item.id,
        name: item.name,
        customData: item.customData,
        type: item.type,
        value: fieldValue,
      };
      requestFields.push(requestField);
    });
    param.addFields = addFields;
    param.editFields = editFields;
    param.requestFields = requestFields;
  }
}

export function getTemplate(baseUrl, vueObj) {
  return new Promise((resolve) => {
    let template = {};
    vueObj.$get(baseUrl + vueObj.projectId, (response) => {
      template = response.data;
      if (template.customFields) {
        template.customFields.forEach((item) => {
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
  oldFields.set("用例状态", testCase.status);
  oldFields.set("责任人", testCase.maintainer);
  oldFields.set("用例等级", testCase.priority);
  return oldFields;
}

export function sortCustomFields(customFields) {
  let total = 0; //定义total用于控制循环结束
  for (let i = 0; total < customFields.length; total++) {
    if (
      typeof customFields[i].defaultValue === "string" ||
      customFields[i].defaultValue instanceof String
    ) {
      try {
        customFields[i].defaultValue = JSON.parse(customFields[i].defaultValue);
      } catch (e) {
        // nothing
      }
    }
    if (customFields[i].type === "richText") {
      //循环到是0的位置就删除该元素0并且在arr末尾push进这个元素0，由于splice删除了该位置元素，所以i不用+1，下次循环仍然检查i位置的元素
      customFields.push(customFields.splice(i, 1)[0]);
    } else {
      i++; //循环到不是0的位置就继续往后循环
    }
  }
  return customFields;
}

export function getApiParamsConfigFields(_this) {
  return [
    { value: "MIX_LENGTH", text: _this.$t("schema.minLength") },
    { value: "MAX_LENGTH", text: _this.$t("schema.maxLength") },
    { value: "ENCODE", text: _this.$t("commons.encode") },
    { value: "DESCRIPTION", text: _this.$t("commons.description") },
  ];
}

export function getApiJsonSchemaConfigFields(_this) {
  return [
    { value: "MIX_LENGTH", text: _this.$t("schema.minLength") },
    { value: "MAX_LENGTH", text: _this.$t("schema.maxLength") },
    { value: "DEFAULT", text: _this.$t("commons.default") },
    { value: "ENUM", text: _this.$t("schema.enum") },
    { value: "PATTERN", text: _this.$t("schema.pattern") },
    { value: "FORMAT", text: _this.$t("schema.format") },
    { value: "DESCRIPTION", text: _this.$t("commons.description") },
  ];
}

export function getShowFields(key) {
  let apiParamsShowFields = localStorage.getItem(key);
  if (apiParamsShowFields) {
    try {
      return JSON.parse(apiParamsShowFields);
    } catch (e) {
      return [];
    }
  }
  return [];
}
