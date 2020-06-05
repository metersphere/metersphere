import {
  REFRESH_SESSION_USER_URL,
  ROLE_ORG_ADMIN,
  ROLE_TEST_MANAGER,
  ROLE_TEST_USER,
  ROLE_TEST_VIEWER,
  TokenKey
} from "./constants";
import axios from "axios";

export function hasRole(role) {
  let user = getCurrentUser();
  let roles = user.roles.map(r => r.id);
  return roles.indexOf(role) > -1;
}

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

export function checkoutCurrentOrganization() {
  let user = getCurrentUser();
  // 查看当前用户是否是 lastOrganizationId 的组织管理员
  return user.userRoles.filter(ur => hasRole(ROLE_ORG_ADMIN) && user.lastOrganizationId === ur.sourceId).length > 0;
}

export function checkoutCurrentWorkspace() {
  let user = getCurrentUser();
  // 查看当前用户是否是 lastWorkspaceId 的工作空间用户
  return user.userRoles.filter(ur => hasRoles(ROLE_TEST_MANAGER, ROLE_TEST_USER, ROLE_TEST_VIEWER) && user.lastWorkspaceId === ur.sourceId).length > 0;
}

export function checkoutTestManagerOrTestUser() {
  let user = getCurrentUser();
  return user.userRoles.filter(ur => hasRoles(ROLE_TEST_MANAGER, ROLE_TEST_USER) && user.lastWorkspaceId === ur.sourceId).length > 0;
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
  for(let filter in filters) {
    if (filters[filter] && filters[filter].length > 0) {
      condition.filters[filter] = filters[filter];
    } else {
      condition.filters[filter] = null;
    }
  }
}

//表格数据排序
export function _sort(column, condition) {
  column.prop = humpToLine(column.prop);
  if (column.order == 'descending') {
    column.order = 'desc';
  } else {
    column.order = 'asc';
  }
  if (!condition.orders) {
    condition.orders = [];
  }
  let hasProp = false;
  condition.orders.forEach(order => {
    if (order.name == column.prop) {
      order.type = column.order;
      hasProp = true;
      return;
    }
  });
  if (!hasProp) {
    condition.orders.push({name: column.prop, type: column.order});
  }
}
