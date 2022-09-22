import {
  COUNT_NUMBER,
  COUNT_NUMBER_SHALLOW,
  LicenseKey,
  ORIGIN_COLOR,
  ORIGIN_COLOR_SHALLOW,
  PRIMARY_COLOR,
  PROJECT_ID,
  TokenKey,
  WORKSPACE_ID
} from "./constants";
import {jsPDF} from "jspdf";
import JSEncrypt from 'jsencrypt';
import i18n from "@/i18n/i18n";
import calcTextareaHeight from "element-ui/packages/input/src/calcTextareaHeight";
import {MessageBox} from "element-ui";

export function hasRole(role) {
  let user = getCurrentUser();
  let roles = user.roles.map(r => r.id);
  return roles.indexOf(role) > -1;
}

export function hasPermission(permission) {
  let user = getCurrentUser();

  user.userGroups.forEach(ug => {
    user.groupPermissions.forEach(gp => {
      if (gp.group.id === ug.groupId) {
        ug.userGroupPermissions = gp.userGroupPermissions;
        ug.group = gp.group;
      }
    });
  });

  // todo 权限验证
  let currentProjectPermissions = user.userGroups.filter(ug => ug.group && ug.group.type === 'PROJECT')
    .filter(ug => ug.sourceId === getCurrentProjectID())
    .flatMap(ug => ug.userGroupPermissions)
    .map(g => g.permissionId)
    .reduce((total, current) => {
      total.add(current);
      return total;
    }, new Set);
  for (const p of currentProjectPermissions) {
    if (p === permission) {
      return true;
    }
  }

  let currentWorkspacePermissions = user.userGroups.filter(ug => ug.group && ug.group.type === 'WORKSPACE')
    .filter(ug => ug.sourceId === getCurrentWorkspaceId())
    .flatMap(ug => ug.userGroupPermissions)
    .map(g => g.permissionId)
    .reduce((total, current) => {
      total.add(current);
      return total;
    }, new Set);

  for (const p of currentWorkspacePermissions) {
    if (p === permission) {
      return true;
    }
  }

  let systemPermissions = user.userGroups.filter(gp => gp.group && gp.group.type === 'SYSTEM')
    .filter(ug => ug.sourceId === 'system' || ug.sourceId === 'adminSourceId')
    .flatMap(ug => ug.userGroupPermissions)
    .map(g => g.permissionId)
    .reduce((total, current) => {
      total.add(current);
      return total;
    }, new Set);

  for (const p of systemPermissions) {
    if (p === permission) {
      return true;
    }
  }

  return false;
}

export function hasLicense() {
  let v = localStorage.getItem(LicenseKey);
  return v && v === 'valid';
}

export function hasPermissions(...permissions) {
  for (let p of permissions) {
    if (hasPermission(p)) {
      return true;
    }
  }
  return false;
}

export function getCurrentWorkspaceId() {
  let workspaceId = sessionStorage.getItem(WORKSPACE_ID);
  if (workspaceId) {
    return workspaceId;
  }
  return getCurrentUser().lastWorkspaceId;
}

export function getCurrentProjectID() {
  let projectId = sessionStorage.getItem(PROJECT_ID);
  if (projectId) {
    return projectId;
  }
  return getCurrentUser().lastProjectId;
}

export function getCurrentUser() {
  return JSON.parse(localStorage.getItem(TokenKey));
}

export function getCurrentUserId() {
  let user = JSON.parse(localStorage.getItem(TokenKey));
  return user.id;
}

export function enableModules(...modules) {
  for (let module of modules) {
    let moduleStatus = localStorage.getItem('module_' + module);
    if (moduleStatus === 'DISABLE') {
      return false;
    }
  }
  return true;
}

export function saveLocalStorage(response) {
  // 登录信息保存 cookie
  let user = response.data;
  localStorage.setItem(TokenKey, JSON.stringify(user));
  // 校验权限
  user.userGroups.forEach(ug => {
    user.groupPermissions.forEach(gp => {
      if (gp.group.id === ug.groupId) {
        ug.userGroupPermissions = gp.userGroupPermissions;
        ug.group = gp.group;
      }
    });
  });
  // 检查当前项目有没有权限
  let currentProjectId = sessionStorage.getItem(PROJECT_ID);
  if (!currentProjectId) {
    sessionStorage.setItem(PROJECT_ID, user.lastProjectId);
  } else {
    let v = user.userGroups.filter(ug => ug.group && ug.group.type === 'PROJECT')
      .filter(ug => ug.sourceId === currentProjectId);
    if (v.length === 0) {
      sessionStorage.setItem(PROJECT_ID, user.lastProjectId);
    }
  }
  if (!sessionStorage.getItem(WORKSPACE_ID)) {
    sessionStorage.setItem(WORKSPACE_ID, user.lastWorkspaceId);
  }
}

export function saveLicense(data) {
  // 保存License
  localStorage.setItem(LicenseKey, data);
}

export function removeLicense() {
  localStorage.removeItem(LicenseKey);
}

export function jsonToMap(jsonStr) {
  let obj = JSON.parse(jsonStr);
  let strMap = new Map();
  for (let k of Object.keys(obj)) {
    strMap.set(k, obj[k]);
  }
  return strMap;
}

export function mapToJson(strMap) {
  let obj = Object.create(null);
  for (let [k, v] of strMap) {
    obj[k] = v;
  }
  return JSON.stringify(obj);
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

export function getUUID() {
  function S4() {
    return (((1 + Math.random()) * 0x10000) | 0).toString(16).substring(1);
  }

  return (S4() + S4() + "-" + S4() + "-" + S4() + "-" + S4() + "-" + S4() + S4() + S4());
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
        if (blankHeight < 300) {
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
          if (leftHeight > 0) {
            pdf.addPage();
            currentHeight = 0;
          }
        }
      } else {
        pdf.addImage(pageData, 'JPEG', 0, currentHeight, imgWidth, imgHeight);
        currentHeight += imgHeight;
      }
    }
  }

  pdf.save(name.replace(" ", "_") + '.pdf');

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

export function getBodyUploadFiles(obj, runData) {
  let bodyUploadFiles = [];
  obj.bodyUploadIds = [];
  if (runData) {
    if (runData instanceof Array) {
      runData.forEach(request => {
        obj.requestId = request.id;
        _getBodyUploadFiles(request, bodyUploadFiles, obj);
      });
    } else {
      obj.requestId = runData.id;
      _getBodyUploadFiles(runData, bodyUploadFiles, obj);
    }
  }
  return bodyUploadFiles;
}

export function _getBodyUploadFiles(request, bodyUploadFiles, obj) {
  let body = null;
  if (request.hashTree && request.hashTree.length > 0 && request.hashTree[0] && request.hashTree[0].body) {
    obj.requestId = request.hashTree[0].id;
    body = request.hashTree[0].body;
  } else if (request.body) {
    obj.requestId = request.id;
    body = request.body;
  }
  if (body) {
    if (body.kvs) {
      body.kvs.forEach(param => {
        if (param.files) {
          param.files.forEach(item => {
            if (item.file) {
              item.name = item.file.name ? item.file.name : item.name;
              bodyUploadFiles.push(item.file);
            }
          });
        }
      });
    }
    if (body.binary) {
      body.binary.forEach(param => {
        if (param.files) {
          param.files.forEach(item => {
            if (item.file) {
              item.name = item.file.name ? item.file.name : item.name;
              bodyUploadFiles.push(item.file);
            }
          });
        }
      });
    }
  }
}

export function handleCtrlSEvent(event, func) {
  if (event.keyCode === 83 && event.ctrlKey) {
    func();
    event.preventDefault();
    event.returnValue = false;
    return false;
  }
}

export function handleCtrlREvent(event, func) {
  if (event.keyCode === 82 && event.ctrlKey) {
    func();
    event.preventDefault();
    event.returnValue = false;
    return false;
  }
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

export function setCustomizeColor(color) {
  // 自定义主题风格
  document.body.style.setProperty('--aside_color', color || '#783887');
  document.body.style.setProperty('--font_color', "#fff");
  document.body.style.setProperty('--font_light_color', "#fff");
}

export function setDefaultTheme() {
  setColor(ORIGIN_COLOR, ORIGIN_COLOR_SHALLOW, COUNT_NUMBER, COUNT_NUMBER_SHALLOW, PRIMARY_COLOR);
}

export function publicKeyEncrypt(input, publicKey) {

  let jsencrypt = new JSEncrypt({default_key_size: 1024});
  jsencrypt.setPublicKey(publicKey);

  return jsencrypt.encrypt(input);
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

export function getDefaultTableHeight() {
  return document.documentElement.clientHeight - 160;
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

export function getShareId() {
  //let herfUrl = 'http://localhost:8080/sharePlanReport?shareId=bf9496ac-8577-46b4-adf9-9c7e93dd06a8';
  let herfUrl = window.location.href;
  if (herfUrl.indexOf('shareId=') > -1) {
    let shareId = '';
    new URL(herfUrl).searchParams.forEach((value, key) => {
      if (key === 'shareId') {
        shareId = value;
      }
    });
    return shareId;
  } else {
    if (herfUrl.indexOf("?") > 0) {
      let paramArr = herfUrl.split("?");
      if (paramArr.length > 1) {
        let shareId = paramArr[1];
        if (shareId.indexOf("#") > 0) {
          shareId = shareId.split("#")[0];
        }
        return shareId;
      }
    }
  }
  return "";
}

export function setCurTabId(vueObj, tab, ref) {
  vueObj.$nextTick(() => {
    if (vueObj.$refs && vueObj.$refs[ref]) {
      let index = tab.index ? Number.parseInt(tab.index) : vueObj.tabs.length;
      let cutEditTab = vueObj.$refs[ref][index - 1];
      let curTabId = cutEditTab ? cutEditTab.tabId : null;
      vueObj.$store.commit('setCurTabId', curTabId);
    }
  });
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

/**
 * 同一行的多个文本框高度保持一致
 * 同时支持 autosize 的功能
 * @param size 同一行中文本框的个数
 * @param index 编辑行的下标
 * 如果编辑某一行，则只调整某一行，提升效率
 */
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



