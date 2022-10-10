/**
 * 同一行的多个文本框高度保持一致
 * 同时支持 autosize 的功能
 * @param size 同一行中文本框的个数
 * @param index 编辑行的下标
 * 如果编辑某一行，则只调整某一行，提升效率
 */
import calcTextareaHeight from "element-ui/packages/input/src/calcTextareaHeight";

export function resizeTextarea(size = 2, index) {
  let textareaList = document.querySelectorAll('.sync-textarea .el-textarea__inner');
  if (index || index === 0) {
    _resizeTextarea(index * size, size, textareaList);
  } else {
    for (let i = 0; i < textareaList.length; i += size) {
      _resizeTextarea(i, size, textareaList);
    }
  }
}

function _resizeTextarea(i, size, textareaList) {
  // 查询同一行中文本框的最大高度
  let maxHeight = 0;
  for (let j = 0; j < size; j++) {
    let cur = textareaList[i + j];
    let curHeight = parseFloat(calcTextareaHeight(cur).height);
    maxHeight = Math.max(curHeight, maxHeight);
  }

  // 将同一行中的文本框设置为最大高度
  for (let k = 0; k < size; k++) {
    let cur = textareaList[i + k];
    if (cur.clientHeight !== maxHeight) {
      cur.style.height = maxHeight + 'px';
    }
  }
}

export const uuid = function () {
  let d = new Date().getTime();
  let d2 = (performance && performance.now && (performance.now() * 1000)) || 0;
  return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function (c) {
    let r = Math.random() * 16;
    if (d > 0) {
      r = (d + r) % 16 | 0;
      d = Math.floor(d / 16);
    } else {
      r = (d2 + r) % 16 | 0;
      d2 = Math.floor(d2 / 16);
    }
    return (c === 'x' ? r : (r & 0x3 | 0x8)).toString(16);
  });
};
