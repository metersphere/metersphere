
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
