import {getCurrentProjectID, getCurrentUser, humpToLine} from "@/common/js/utils";

export function _handleSelectAll(component, selection, tableData, selectRows, condition) {
  if (selection.length > 0) {
    if (selection.length === 1) {
      selection.hashTree = [];
      selectRows.add(selection[0]);
    } else {
      tableData.forEach(item => {
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
  }
  if (selectRows.size < 1 && condition) {
    condition.selectAll = false;
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
  column.prop = humpToLine(column.prop);
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
    if (order.name === column.prop) {
      order.type = column.order;
      hasProp = true;
    }
  });
  if (!hasProp) {
    condition.orders.push({name: column.prop, type: column.order});
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


export function buildBatchParam(vueObj) {
  let param = {};
  param.ids = Array.from(vueObj.selectRows).map(row => row.id);
  param.projectId = getCurrentProjectID();
  param.condition = vueObj.condition;
  return param;
}


