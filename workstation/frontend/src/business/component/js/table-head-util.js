import {X_PACK_TABLE_HEADERS} from "../js/xpack-table-headers";
import {
  generateTableHeaderKey,
  getCustomFieldsKeys, getCustomFieldValue,
  translateLabel
} from "metersphere-frontend/src/utils/tableUtils";
import {updateCustomFieldTemplate} from "metersphere-frontend/src/api/custom-field-template";
import i18n from "@/i18n";


/**
 * 获取所有字段
 * @param key
 * @returns {*[]}
 */
export function getTableHeaderWithCustomFieldsByXpack(key,customFields,projectMembers = []) {
  let fields = [...X_PACK_TABLE_HEADERS[key]];
  fields = JSON.parse(JSON.stringify(fields));
  translateLabel(fields);
  let keys = getCustomFieldsKeys(customFields);
  projectMembers.forEach(member => {
    member['text'] = member.name;
    // 高级搜索使用
    member['label'] = member.name;
    member['value'] = member.id;
    member['showLabel'] = member.name + "(" + member.id + ")";
  })
  customFields.forEach(item => {
    if (!item.key) {
      // 兼容旧版，更新key
      item.key = generateTableHeaderKey(keys, customFields);
      return updateCustomFieldTemplate({id: item.id, key: item.key});
    }
    let field = {
      id: item.name,
      key: item.key,
      label: item.name,
      isCustom: true
    }
    fields.push(field);
    if ((item.type === 'member' || item.type === 'multipleMember') && projectMembers && projectMembers.length > 0) {
      item.options = projectMembers;
    }
  });
  return getCustomTableHeaderByFiledSetting(key, fields);
}

/**
 * 获取 localStorage 的值，过滤
 * @param key
 * @param fieldSetting
 * @returns {[]|*}
 */
function getCustomTableHeaderByFiledSetting(key, fieldSetting) {
  let fieldStr = localStorage.getItem(key);
  if (fieldStr !== null) {
    let fields = [];
    for (let i = 0; i < fieldStr.length; i++) {
      let fieldKey = fieldStr[i];
      for (let j = 0; j < fieldSetting.length; j++) {
        let item = fieldSetting[j];
        if (item.key === fieldKey) {
          fields.push(item);
          break;
        }
      }
    }
    return fields;
  }
  return fieldSetting;
}

export function getAllFieldWithCustomFieldsByXpack(key, customFields) {
  let fieldSetting = [...X_PACK_TABLE_HEADERS[key]];
  fieldSetting = JSON.parse(JSON.stringify(fieldSetting));
  translateLabel(fieldSetting);
  if (customFields) {
    customFields.forEach(item => {
      let field = {
        id: item.name,
        key: item.key,
        label: item.name,
        isCustom: true
      }
      fieldSetting.push(field);
    });
  }
  return fieldSetting;
}
export function getCustomTableHeaderByXpack(key, customFields) {
  let fieldSetting = getAllFieldWithCustomFieldsByXpack(key, customFields);
  return getCustomTableHeaderByFiledSetting(key, fieldSetting);
}

export function getCustomFieldValueForTrack(row, field, members) {
  if (field.name === '用例状态' && field.system) {
    return parseStatus(row, field.options);
  }
  return getCustomFieldValue(row, field, members);
}

function parseStatus(row, options) {
  if (options) {
    for (let option of options) {
      if (option.value === row.status) {
        return option.system ? i18n.t(option.text) : option.text;
      }
    }
  }
  return row.status;
}


/**
 * 获取对应表格的列宽
 * @param key
 * @returns {{}|any}
 */
export function getCustomTableWidth(key) {
  let fieldStr = localStorage.getItem(key + '_WITH');
  if (fieldStr !== null) {
    let fields = JSON.parse(fieldStr);
    return fields;
  }
  return {};
}

