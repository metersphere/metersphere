import {getCurrentProjectID, getCurrentUser, humpToLine} from "@/common/js/utils";
import {CUSTOM_TABLE_HEADER} from "@/common/js/default-table-header";
import {updateCustomFieldTemplate} from "@/network/custom-field-template";
import i18n from "@/i18n/i18n";

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
  } else {
    column.order = 'asc';
  }
  if (!condition.orders) {
    condition.orders = [];
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


export function buildBatchParam(vueObj, selectIds) {
  let param = {};
  if (vueObj.selectRows) {
    param.ids = selectIds ? selectIds: Array.from(vueObj.selectRows).map(row => row.id);
  } else {
    param.ids = selectIds;
  }
  param.projectId = getCurrentProjectID();
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
export function getTableHeaderWithCustomFields(key, customFields) {
  let fieldSetting = [...CUSTOM_TABLE_HEADER[key]]; // 复制
  let keys = getCustomFieldsKeys(customFields);
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
  });
  return getCustomTableHeaderByFiledSetting(key, fieldSetting);
}

/**
 * 获取所有字段
 * @param key
 * @param customFields
 * @returns {*[]}
 */
export function getAllFieldWithCustomFields(key, customFields) {
  let fieldSetting = [...CUSTOM_TABLE_HEADER[key]];
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
    let key =  customFieldKeys[i];
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
  localStorage.setItem(key+"_SORT", result);
}

export function getLastTableSortField(key) {
  let fieldStr = localStorage.getItem(key+"_SORT");
  return fieldStr;
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
  if (row.customFields) {
    for (let i = 0; i < row.customFields.length; i++) {
      let item = row.customFields[i];
      if (item.name === field.name) {
        if (field.type === 'member' || field.type === 'multipleMember') {
          for (let j = 0; j < members.length; j++) {
            let member = members[j];
            if (member.id === item.value) {
              return member.name;
            }
          }
        } else if (['radio', 'select', 'multipleSelect', 'checkbox'].indexOf(field.type) > -1) {
          for (let j = 0; j < field.options.length; j++) {
            let option = field.options[j];
            if (option.value === item.value) {
              return field.system ? i18n.t(option.text) : option.text;
            }
          }
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
        id: item.name,
        name: item.name,
        uuid: item.id
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
