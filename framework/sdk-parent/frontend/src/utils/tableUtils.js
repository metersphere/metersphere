import {getCurrentProjectID, getCurrentUser} from "./token";
import {CUSTOM_TABLE_HEADER} from "./default-table-header";
import {updateCustomFieldTemplate} from "../api/custom-field-template";
import i18n from "../i18n";
import Sortable from 'sortablejs'
import {datetimeFormat} from "fit2cloud-ui/src/filters/time";
import {hasLicense} from "../utils/permission";
import {getUUID, humpToLine} from "./index";
import {CUSTOM_FIELD_TYPE_OPTION} from "./table-constants";

export function _handleSelectAll(component, selection, tableData, selectRows, condition) {
  if (selection.length > 0) {
    if (selection.length === 1) {
      selection.hashTree = [];
      tableData.forEach((item) => {
        component.$set(item, "showMore", true);
        selectRows.add(item);
      });
    } else {
      tableData.forEach((item) => {
        item.hashTree = [];
        component.$set(item, "showMore", true);
        selectRows.add(item);
      });
    }
  } else {
    selectRows.clear();
    tableData.forEach(item => {
      component.$set(item, "showMore", false);
    });
    if (condition) {
      condition.selectAll = false;
    }
  }
}

export function _handleSelect(component, selection, row, selectRows) {
  row.hashTree = [];
  if (selectRows.has(row)) {
    component.$set(row, "showMore", false);
    selectRows.delete(row);
  } else {
    component.$set(row, "showMore", true);
    selectRows.add(row);
  }
  let arr = Array.from(selectRows);
  arr.forEach(row => {
    component.$set(row, "showMore", true);
  });
}

// 设置 unSelectIds 查询条件，返回当前选中的条数
export function setUnSelectIds(tableData, condition, selectRows) {
  let ids = Array.from(selectRows).map(o => o.id);
  let allIDs = tableData.map(o => o.id);
  let thisUnSelectIds = allIDs.filter(function (val) {
    return ids.indexOf(val) === -1;
  });
  if (condition.unSelectIds) {
    //首先将选择的ID从unSelectIds中排除
    condition.unSelectIds = condition.unSelectIds.filter(function (val) {
      return ids.indexOf(val) === -1;
    });
    //去掉unselectIds中存在的ID
    let needPushIds = thisUnSelectIds.filter(function (val) {
      return condition.unSelectIds.indexOf(val) === -1;
    });
    needPushIds.forEach(id => {
      condition.unSelectIds.push(id);
    });
  }
}

export function getSelectDataCounts(condition, total, selectRows) {
  if (condition.selectAll) {
    return total - condition.unSelectIds.length;
  } else {
    return selectRows.size;
  }
}

// 全选操作
export function toggleAllSelection(table, tableData, selectRows) {
  //如果已经全选，不需要再操作了
  if (selectRows.size != tableData.length) {
    table.toggleAllSelection(true);
  }
}

//检查表格每一行是否应该选择(使用场景：全选数据时进行翻页操作)
export function checkTableRowIsSelect(component, condition, tableData, table, selectRows) {
  //如果默认全选的话，则选中应该选中的行
  if (condition.selectAll) {
    let unSelectIds = condition.unSelectIds;
    tableData.forEach(row => {
      if (unSelectIds.indexOf(row.id) < 0) {
        table.toggleRowSelection(row, true);

        //默认全选，需要把选中对行添加到selectRows中。不然会影响到勾选函数统计
        if (!selectRows.has(row)) {
          component.$set(row, "showMore", true);
          selectRows.add(row);
        }
      } else {
        //不勾选的行，也要判断是否被加入了selectRow中。加入了的话就去除。
        if (selectRows.has(row)) {
          component.$set(row, "showMore", false);
          selectRows.delete(row);
        }
      }
    });
  }
}

// nexttick:表格加载完成之后触发。判断是否需要勾选行
export function checkTableRowIsSelected(veuObj, table) {
  veuObj.$nextTick(function () {
    if (table) {
      table.checkTableRowIsSelect();
      table.doLayout();
    }
  });
}

//表格数据过滤
export function _filter(filters, condition) {
  if (!condition.filters) {
    condition.filters = {};
  }
  for (let filter in filters) {
    if (filters.hasOwnProperty(filter)) {
      if (filters[filter] && filters[filter].length > 0) {
        condition.filters[humpToLine(filter)] = filters[filter];
      } else {
        condition.filters[humpToLine(filter)] = null;
      }
    }
  }
}

//表格数据排序
export function _sort(column, condition) {
  let field = humpToLine(column.column.columnKey ? column.column.columnKey : column.prop);
  if (column.order === 'descending') {
    column.order = 'desc';
  } else if (column.order === 'ascending') {
    column.order = 'asc';
  }
  if (!condition.orders) {
    condition.orders = [];
  }
  if (column.order == null) {
    return;
  }
  let hasProp = false;
  condition.orders.forEach(order => {
    if (order.name === field) {
      order.type = column.order;
      hasProp = true;
    }
  });
  /* if (column.prop === 'case_passing_rate' || column.prop === 'case_total') {
     hasProp = true;
   }*/
  if (!hasProp) {
    condition.orders.push({name: field, type: column.order});
  }
}

export function initCondition(condition, isSelectAll) {
  if (!isSelectAll) {
    condition.selectAll = false;
    condition.unSelectIds = [];
  }
}

export function getLabel(vueObj, type) {
  let param = {};
  param.userId = getCurrentUser().id;
  param.type = type;
  vueObj.result = vueObj.$post('/system/header/info', param, response => {
    if (response.data != null) {
      vueObj.tableLabel = eval(response.data.props);
    } else {
      let param = {};
      param.type = type;
      vueObj.result = vueObj.$post('/system/system/header', param, response => {
        if (response.data != null) {
          vueObj.tableLabel = eval(response.data.props);
        }
      });
    }
  });
}


export function buildBatchParam(vueObj, selectIds, projectId) {
  let param = {};
  if (vueObj.selectRows) {
    param.ids = selectIds ? selectIds : Array.from(vueObj.selectRows).map(row => row.id);
  } else {
    param.ids = selectIds;
  }
  param.projectId = projectId ? projectId : getCurrentProjectID();
  param.condition = vueObj.condition;
  return param;
}

// 深拷贝
export function deepClone(source) {
  if (!source && typeof source !== 'object') {
    throw new Error('error arguments', 'deepClone');
  }
  const targetObj = source.constructor === Array ? [] : {};
  Object.keys(source).forEach(keys => {
    if (source[keys] && typeof source[keys] === 'object') {
      targetObj[keys] = deepClone(source[keys]);
    } else {
      targetObj[keys] = source[keys];
    }
  });
  return targetObj;
}

export function getPageInfo(condition) {
  return {
    total: 0,
    pageSize: 10,
    currentPage: 1,
    result: {},
    data: [],
    condition: condition ? condition : {},
    loading: false
  }
}

export function buildPagePath(path, page) {
  return path + "/" + page.currentPage + "/" + page.pageSize;
}

export function getPageDate(response, page) {
  let data = response.data;
  page.total = data.itemCount;
  page.data = data.listObject;
}

/**
 * 获取自定义表头
 * 如果 localStorage 没有，获取默认表头
 * @param key
 * @returns {[]|*}
 */
export function getCustomTableHeader(key, customFields) {
  let fieldSetting = getAllFieldWithCustomFields(key, customFields);
  return getCustomTableHeaderByFiledSetting(key, fieldSetting);
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

/**
 * 获取带自定义字段的表头
 * @param key
 * @param customFields
 * @returns {[]|*}
 */
export function getTableHeaderWithCustomFields(key, customFields, projectMembers = []) {
  let fieldSetting = [...CUSTOM_TABLE_HEADER[key]];
  fieldSetting = JSON.parse(JSON.stringify(fieldSetting)); // 复制，国际化
  translateLabel(fieldSetting);
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
      updateCustomFieldTemplate({id: item.id, key: item.key});
    }
    let field = {
      id: item.name,
      key: item.key,
      label: item.name,
      isCustom: true
    }
    fieldSetting.push(field);
    if ((item.type === 'member' || item.type === 'multipleMember') && projectMembers && projectMembers.length > 0) {
      item.options = projectMembers;
    }
  });
  return getCustomTableHeaderByFiledSetting(key, fieldSetting);
}

export function translateLabel(fieldSetting) {
  if (fieldSetting) {
    fieldSetting.forEach(item => {
      if (item.label) {
        item.label = i18n.t(item.label);
      }
    });
  }
}

/**
 * 获取所有字段
 * @param key
 * @param customFields
 * @returns {*[]}
 */
export function getAllFieldWithCustomFields(key, customFields) {
  let fieldSetting = [...CUSTOM_TABLE_HEADER[key]];
  // 如果没有 license, 排除 xpack
  if (!hasLicense()) {
    fieldSetting = fieldSetting.filter(v => !v.xpack);
  }
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

export function generateTableHeaderKey(keys) {
  let customFieldKeys = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ';
  for (let i = 0; i < customFieldKeys.length; i++) {
    let key = customFieldKeys[i];
    if (keys.has(key)) {
      continue;
    }
    keys.add(key);
    return key;
  }
  return '';
}

export function getCustomFieldsKeys(customFields) {
  let keys = new Set();
  customFields.forEach(item => {
    if (item.key) {
      keys.add(item.key);
    }
  });
  return keys;
}

/**
 * 将自定义表头存在 localStorage
 * 格式简化，减小占用
 * @param key
 * @param fields
 */
export function saveCustomTableHeader(key, fields) {
  let result = '';
  if (fields) {
    fields.forEach(item => {
      result += item.key;
    });
  }
  localStorage.setItem(key, result);
}

/**
 * 将上一次的表格排序字段存在 localStorage
 * @param key
 * @param fields
 */
export function saveLastTableSortField(key, field) {
  let result = field;
  localStorage.setItem(key + "_SORT", result);
}

export function getLastTableSortField(key) {
  let orderJsonStr = localStorage.getItem(key + "_SORT");
  if (orderJsonStr) {
    try {
      return JSON.parse(orderJsonStr);
    } catch (e) {
      return [];
    }
  }
  return [];
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

/**
 * 存储表格的列宽
 * @param key
 * @param fieldKey
 * @param colWith
 */
export function saveCustomTableWidth(key, fieldKey, colWith) {
  let fields = getCustomTableWidth(key);
  fields[fieldKey] = colWith + '';
  localStorage.setItem(key + '_WITH', JSON.stringify(fields));
}

/**
 * 获取列表的自定义字段的显示值
 * @param row
 * @param field
 * @param members
 * @returns {VueI18n.TranslateResult|*}
 */
export function getCustomFieldValue(row, field, members) {
  if (row.fields) {
    for (let i = 0; i < row.fields.length; i++) {
      let item = row.fields[i];
      if (item.id === field.id) {
        if (!item.value) return '';
        if (field.type === 'member') {
          for (let j = 0; j < members.length; j++) {
            let member = members[j];
            if (member.id === item.value) {
              return member.name;
            }
          }
        } else if (field.type === 'multipleMember') {
          let values = '';
          if (item.value.length > 0) {
            item.value.forEach(v => {
              for (let j = 0; j < members.length; j++) {
                let member = members[j];
                if (member.id === v) {
                  values += member.name;
                  values += " ";
                  break;
                }
              }
            });
          }
          return values;
        } else if (['radio', 'select'].indexOf(field.type) > -1) {
          if (field.options) {
            for (let j = 0; j < field.options.length; j++) {
              let option = field.options[j];
              if (option.value === item.value) {
                return field.system ? i18n.t(option.text) : option.text;
              }
            }
          }
        } else if (['multipleSelect', 'checkbox'].indexOf(field.type) > -1) {
          let values = '';
          try {
            if (field.type === 'multipleSelect') {
              if (typeof (item.value) === 'string' || item.value instanceof String) {
                item.value = JSON.parse(item.value);
              }
            }
            item.value.forEach(v => {
              for (let j = 0; j < field.options.length; j++) {
                let option = field.options[j];
                if (option.value === v) {
                  values += (field.system ? i18n.t(option.text) : option.text);
                  values += " ";
                  break;
                }
              }
            });
          } catch (e) {
            values = '';
          }
          return values;
        } else if (field.type === 'cascadingSelect') {
          let val = '';
          let options = field.options;
          for (const v of item.value) {
            if (!options) break;
            for (const o of options) {
              if (o.value === v) {
                val = o.text;
                options = o.children;
                break;
              }
            }
          }
          return val;
        } else if (field.type === 'multipleInput') {
          let val = '';
          item.value.forEach(i => {
            val += i + ' ';
          });
          return val;
        } else if (field.type === 'datetime' || field.type === 'date') {
          return datetimeFormat(item.value);
        } else if (['richText', 'textarea'].indexOf(field.type) > -1) {
          return item.textValue;
        }
        return item.value;
      }
    }
  }
}

/**
 * 获取批量编辑的自定义字段选项
 * @param customFields
 * @param typeArr
 * @param valueArr
 * @param members
 */
export function getCustomFieldBatchEditOption(customFields, typeArr, valueArr, members) {

  customFields.forEach(item => {
    if (item.options) {
      typeArr.push({
        id: item.id,
        name: item.name,
        uuid: item.id,
        custom: "custom" + item.id
      });

      let options = [];
      if (['multipleMember', 'member'].indexOf(item.type) > -1) {
        members.forEach(member => {
          options.push({
            id: member.id,
            name: member.name
          });
        });
      } else {
        item.options.forEach((option) => {
          options.push({
            id: option.value,
            name: option.system ? i18n.t(option.text) : option.text
          });
        });
      }
      valueArr[item.name] = options;
    }
  });
}

export function parseCustomFilesForList(data) {
  data.forEach(item => {
    if (item.fields) {
      item.fields.forEach(i => {
        parseCustomFilesForItem(i);
      });
    }
  });
}

export function parseCustomFilesForItem(data) {
  if (data.value) {
    data.value = JSON.parse(data.value);
  }
  if (data.textValue) {
    data.value = data.textValue;
  }
}

// 多个监听共享变量
// 否则切换 pageSize 等刷新操作会导致部分行的回调函数中 data 数据不一致
let shareDragParam = {};

// 清除 shareDragParam ，减少内存占用
export function clearShareDragParam() {
  shareDragParam.data = null;
}

export function handleRowDrop(data, callback, msTableKey) {
  setTimeout(() => {
    const tbody = document.querySelector(`#${msTableKey} .el-table__body-wrapper tbody`);
    if (!tbody) {
      return;
    }
    const dropBars = tbody.getElementsByClassName('table-row-drop-bar');

    const msTable = document.getElementsByClassName('ms-table');

    // 每次调用生成一个class
    // 避免增删列表数据时，回调函数中的 data 与实际 data 不一致
    let dropClass = 'table-row-drop-bar-random' + '_' + getUUID();

    for (let i = 0; i < dropBars.length; i++) {
      dropBars[i].classList.add(dropClass);
    }

    shareDragParam.data = data;

    Sortable.create(tbody, {
      handle: "." + dropClass,
      animation: 100,
      onStart: function (/**Event*/evt) {
        // 解决拖拽时高亮阴影停留在原位置的问题
        if (msTable) {
          for (let i = 0; i < msTable.length; i++) {
            msTable[i].classList.add('disable-hover');
          }
        }
      },
      onEnd({newIndex, oldIndex}) {
        let param = {};
        param.moveId = shareDragParam.data[oldIndex].id;
        if (newIndex === 0) {
          param.moveMode = 'BEFORE';
          param.targetId = shareDragParam.data[0].id;
        } else {
          // 默认从后面添加
          param.moveMode = 'AFTER';
          if (newIndex < oldIndex) {
            // 如果往前拖拽，则添加到当前下标的前一个元素后面
            param.targetId = shareDragParam.data[newIndex - 1].id;
          } else {
            // 如果往后拖拽，则添加到当前下标的元素后面
            param.targetId = shareDragParam.data[newIndex].id;
          }
        }
        if (shareDragParam.data && shareDragParam.data.length > 1 && newIndex !== oldIndex) {
          const currRow = shareDragParam.data.splice(oldIndex, 1)[0];
          shareDragParam.data.splice(newIndex, 0, currRow);
          if (callback) {
            callback(param);
          }
        }

        for (let i = 0; i < msTable.length; i++) {
          msTable[i].classList.remove('disable-hover');
        }
      }
    });
  }, 100);
}

export function getCustomFieldFilter(field, userFilter) {
  if (field.type === 'multipleMember') {
    return null;
  }
  if (field.type === 'member' && userFilter) {
    return userFilter;
  }

  let optionTypes = CUSTOM_FIELD_TYPE_OPTION
    .filter(x => x.hasOption)
    .map(x => x.value);

  return optionTypes.indexOf(field.type) > -1 && Array.isArray(field.options) ?
    (field.options.length > 0 ? field.options : null) : null;
}

