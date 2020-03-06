import {TokenKey} from "./constants";

export function hasRole(role) {
  let user = JSON.parse(localStorage.getItem(TokenKey));
  let roles = user.roles.map(r => r.id);
  return roles.indexOf(role) > -1;
}

export function hasRoles(...roles) {
  let user = JSON.parse(localStorage.getItem(TokenKey));
  let rs = user.roles.map(r => r.id);
  for (let item of roles) {
    if (rs.indexOf(item) > -1) {
      return true;
    }
  }
  return false;
}
