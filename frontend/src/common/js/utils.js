import {
  REFRESH_SESSION_USER_URL,
  ROLE_ORG_ADMIN,
  ROLE_ADMIN,
  ROLE_TEST_MANAGER,
  ROLE_TEST_USER,
  ROLE_TEST_VIEWER,
  TokenKey,
  LicenseKey
} from "./constants";
import axios from "axios";
import {jsPDF} from "jspdf";

export function hasRole(role) {
  let user = getCurrentUser();
  let roles = user.roles.map(r => r.id);
  return roles.indexOf(role) > -1;
}

// 是否含有某个角色
export function hasRoles(...roles) {
  let user = getCurrentUser();
  let rs = user.roles.map(r => r.id);
  for (let item of roles) {
    if (rs.indexOf(item) > -1) {
      return true;
    }
  }
  return false;
}

export function hasRolePermission(role) {
  let user = getCurrentUser();
  for (let ur of user.userRoles) {
    if (role === ur.roleId) {
      if (ur.roleId === ROLE_ADMIN) {
        return true;
      } else if (ur.roleId === ROLE_ORG_ADMIN && user.lastOrganizationId === ur.sourceId) {
        return true;
      } else if (user.lastWorkspaceId === ur.sourceId) {
        return true;
      }
    }
  }
  return false
}

//是否含有对应组织或工作空间的角色
export function hasRolePermissions(...roles) {
  for (let role of roles) {
    if (hasRolePermission(role)) {
      return true;
    }
  }
  return false;
}

export function checkoutCurrentOrganization() {
  // 查看当前用户是否是 lastOrganizationId 的组织管理员
  return hasRolePermissions(ROLE_ORG_ADMIN);
}

export function checkoutCurrentWorkspace() {
  // 查看当前用户是否是 lastWorkspaceId 的工作空间用户
  return hasRolePermissions(ROLE_TEST_MANAGER, ROLE_TEST_USER, ROLE_TEST_VIEWER);
}

export function checkoutTestManagerOrTestUser() {
  return hasRolePermissions(ROLE_TEST_MANAGER, ROLE_TEST_USER);
}

export function getCurrentOrganizationId() {
  let user = getCurrentUser();
  return user.lastOrganizationId;
}

export function getCurrentWorkspaceId() {
  let user = getCurrentUser();
  return user.lastWorkspaceId;
}

export function getCurrentUser() {
  return JSON.parse(localStorage.getItem(TokenKey));
}

export function saveLocalStorage(response) {
  // 登录信息保存 cookie
  localStorage.setItem(TokenKey, JSON.stringify(response.data));
  let rolesArray = response.data.roles;
  let roles = rolesArray.map(r => r.id);
  // 保存角色
  localStorage.setItem("roles", roles);
}

export function saveLicense(data) {
  // 保存License
  localStorage.setItem(LicenseKey, data);
}


export function refreshSessionAndCookies(sign, sourceId) {
  axios.post(REFRESH_SESSION_USER_URL + "/" + sign + "/" + sourceId).then(r => {
    saveLocalStorage(r.data);
    window.location.reload();
  })
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

export function downloadFile(name, content) {
  const blob = new Blob([content]);
  if ("download" in document.createElement("a")) {
    // 非IE下载
    //  chrome/firefox
    let aTag = document.createElement('a');
    aTag.download = name;
    aTag.href = URL.createObjectURL(blob);
    aTag.click();
    URL.revokeObjectURL(aTag.href)
  } else {
    // IE10+下载
    navigator.msSaveBlob(blob, name)
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
      let imgHeight = a4Width/contentWidth * contentHeight;

      let pageData = canvas.toDataURL('image/jpeg', 1.0);

      // 当前图片的剩余高度
      let leftHeight = imgHeight;

      // 当前页面的剩余高度
      let blankHeight = a4Height - currentHeight;

      if (leftHeight > blankHeight) {
        //页面偏移
        let position = 0;
        while(leftHeight > 0) {
          // 本次添加占用的高度
          let occupation = a4Height - currentHeight;
          pdf.addImage(pageData, 'JPEG', 0, position + currentHeight, imgWidth, imgHeight);
          currentHeight = leftHeight;
          leftHeight -= occupation;
          position -= occupation;
          //避免添加空白页
          if(leftHeight > 0) {
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

