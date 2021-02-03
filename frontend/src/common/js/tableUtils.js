
export function _handleSelectAll(component, selection, tableData, selectRows) {
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
    })
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
  })
}

// 设置 unSelectIds 查询条件，返回当前选中的条数
export function setUnSelectIds(tableData, condition, selectRows) {
  let ids = Array.from(selectRows).map(o => o.id);
  let allIDs = tableData.map(o => o.id);
  condition.unSelectIds = allIDs.filter(function (val) {
    return ids.indexOf(val) === -1
  });

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



