import {COUNT_NUMBER, COUNT_NUMBER_SHALLOW, ORIGIN_COLOR, ORIGIN_COLOR_SHALLOW, PRIMARY_COLOR} from "./constants";
import i18n from '../i18n'
import {jsPDF} from "jspdf";
import {MessageBox} from "element-ui";


export function setCustomizeColor(color) {
  // 自定义主题风格
  document.body.style.setProperty('--aside_color', color || '#783887');
  document.body.style.setProperty('--font_color', "#fff");
  document.body.style.setProperty('--font_light_color', "#fff");
}

export function setDefaultTheme() {
  setColor(ORIGIN_COLOR, ORIGIN_COLOR_SHALLOW, COUNT_NUMBER, COUNT_NUMBER_SHALLOW, PRIMARY_COLOR);
}


export function setColor(a, b, c, d, e) {
  // 顶部菜单背景色
  document.body.style.setProperty('--color', a);
  document.body.style.setProperty('--color_shallow', b);
  // 首页颜色
  document.body.style.setProperty('--count_number', c);
  document.body.style.setProperty('--count_number_shallow', d);
  // 主颜色
  document.body.style.setProperty('--primary_color', e);
}


export function setAsideColor() {
  // 自定义主题风格
  document.body.style.setProperty('--aside_color', "#783887");
  document.body.style.setProperty('--font_light_color', "#fff");
  document.body.style.setProperty('--font_color', "#fff");
}

export function setLightColor() {
  // 自定义主题风格
  document.body.style.setProperty('--aside_color', "#fff");
  document.body.style.setProperty('--font_color', "#505266");
  document.body.style.setProperty('--font_light_color', "#783887");
}


export function getUUID() {
  function S4() {
    return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
  }

  return (S4() + S4() + "-" + S4() + "-" + S4() + "-" + S4() + "-" + S4() + S4() + S4());
}

export function listenGoBack(callback) {
  //监听浏览器返回操作，关闭该对话框
  if (window.history && window.history.pushState) {
    history.pushState(null, null, document.URL);
    window.addEventListener('popstate', callback);
  }
}

export function removeGoBackListener(callback) {
  window.removeEventListener('popstate', callback);
}

// 驼峰转换下划线
export function humpToLine(name) {
  return name.replace(/([A-Z])/g, "_$1").toLowerCase();
}

// 下划线转换驼峰
export function lineToHump(name) {
  return name.replace(/\_(\w)/g, function (all, letter) {
    return letter.toUpperCase();
  });
}

export function fullScreenLoading(component) {
  return component.$loading({
    lock: true,
    text: '资源切换中...',
    spinner: 'el-icon-loading',
    background: 'rgba(218,218,218,0.6)',
    customClass: 'ms-full-loading'
  });
}

export function stopFullScreenLoading(loading, timeout) {
  timeout = timeout ? timeout : 2000;
  setTimeout(() => {
    loading.close();
  }, timeout);
}


export function strMapToObj(strMap) {
  if (strMap) {
    let obj = Object.create(null);
    for (let [k, v] of strMap) {
      obj[k] = v;
    }
    return obj;
  }
  return null;
}

export function objToStrMap(obj) {
  let strMap = new Map();
  for (let k of Object.keys(obj)) {
    strMap.set(k, obj[k]);
  }
  return strMap;
}

export function downloadFile(name, content) {
  const blob = new Blob([content]);
  if ("download" in document.createElement("a")) {
    // 非IE下载
    //  chrome/firefox
    let aTag = document.createElement('a');
    aTag.download = name;
    aTag.href = URL.createObjectURL(blob);
    aTag.click();
    URL.revokeObjectURL(aTag.href);
  } else {
    // IE10+下载
    navigator.msSaveBlob(blob, name);
  }
}

export function getTranslateOptions(data) {
  let options = [];
  data.forEach(i => {
    let option = {};
    Object.assign(option, i);
    option.text = i18n.t(option.text);
    options.push(option);
  });
  return options;
}

export function exportPdf(name, canvasList) {

  let pdf = new jsPDF('', 'pt', 'a4');

  // 当前页面的当前高度
  let currentHeight = 0;
  for (let canvas of canvasList) {
    if (canvas) {

      let contentWidth = canvas.width;
      let contentHeight = canvas.height;

      //a4纸的尺寸[595.28,841.89]
      let a4Width = 592.28;
      let a4Height = 841.89;

      // html页面生成的canvas在pdf中图片的宽高
      let imgWidth = a4Width;
      let imgHeight = a4Width / contentWidth * contentHeight;

      let pageData = canvas.toDataURL('image/jpeg', 1.0);

      // 当前图片的剩余高度
      let leftHeight = imgHeight;

      // 当前页面的剩余高度
      let blankHeight = a4Height - currentHeight;

      if (leftHeight > blankHeight) {
        if (blankHeight < 200) {
          pdf.addPage();
          currentHeight = 0;
        }
        //页面偏移
        let position = 0;
        while (leftHeight > 0) {
          // 本次添加占用的高度
          let occupation = a4Height - currentHeight;
          pdf.addImage(pageData, 'JPEG', 0, position + currentHeight, imgWidth, imgHeight);
          currentHeight = leftHeight;
          leftHeight -= occupation;
          position -= occupation;
          //避免添加空白页
          // if (leftHeight > 0) {
          // pdf.addPage();
          // currentHeight = 0;
          // }
        }
      } else {
        pdf.addImage(pageData, 'JPEG', 0, currentHeight, imgWidth, imgHeight);
        currentHeight += imgHeight;
      }
    }
  }

  pdf.save(name.replace(" ", "_") + '.pdf');

}

let confirm = MessageBox.confirm;

export function operationConfirm(tip, success, cancel) {
  if (tip[tip.length - 1] !== '?' && tip[tip.length - 1] !== '？') {
    tip += '?';
  }
  return confirm(tip, '', {
    confirmButtonText: i18n.t('commons.confirm'),
    cancelButtonText: i18n.t('commons.cancel'),
    type: 'warning',
    center: false
  }).then(() => {
    if (success) {
      success();
    }
  }).catch(() => {
    if (cancel) {
      cancel();
    }
  });
}

export function parseTag(data) {
  data.forEach(item => {
    try {
      let tags = JSON.parse(item.tags);
      if (tags instanceof Array) {
        item.tags = tags ? tags : [];
      } else {
        item.tags = tags ? [tags + ''] : [];
      }
    } catch (e) {
      item.tags = [];
    }
  });
}

export function getNodePath(id, moduleOptions) {
  for (let i = 0; i < moduleOptions.length; i++) {
    let item = moduleOptions[i];
    if (id === item.id) {
      return item.path;
    }
  }
  return '';
}

export function windowPrint(id, zoom) {
  //根据div标签ID拿到div中的局部内容
  let bdhtml = window.document.body.innerHTML;
  let el = document.getElementById(id);
  var jubuData = el.innerHTML;
  document.getElementsByTagName('body')[0].style.zoom = zoom;
  //把获取的 局部div内容赋给body标签, 相当于重置了 body里的内容
  window.document.body.innerHTML = jubuData;
  //调用打印功能
  window.print();
  document.getElementsByTagName('body')[0].style.zoom = 1;
  window.document.body.innerHTML = bdhtml;//重新给页面内容赋值；
  return false;
}

export function handleCtrlSEvent(event, func) {
  if (event.keyCode === 83 && event.ctrlKey) {
    func();
    event.preventDefault();
    event.returnValue = false;
    return false;
  }
}

export function byteToSize(bytes) {
  if (bytes === 0) {
    return '0 B';
  }
  let k = 1024,
    sizes = ['B', 'KB', 'MB', 'GB', 'TB', 'PB', 'EB', 'ZB', 'YB'],
    i = Math.floor(Math.log(bytes) / Math.log(k));

  return (bytes / Math.pow(k, i)).toPrecision(3) + ' ' + sizes[i];
}

export function getTypeByFileName(filename) {
  if (filename === '') {
    return '';
  }
  let type = filename.substr(filename.lastIndexOf('.') + 1);
  return type.toUpperCase();
}


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

export function checkMicroMode() {
  return sessionStorage.getItem("MICRO_MODE");
}
