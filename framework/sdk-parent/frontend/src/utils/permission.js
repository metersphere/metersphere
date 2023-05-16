import {LicenseKey, SUPER_GROUP} from "./constants";
import {getCurrentProjectID, getCurrentUser, getCurrentWorkspaceId} from "./token";

export function hasPermission(permission) {
  let user = getCurrentUser();
  if (!user || !user.groups) {
    return false;
  }
  let index = user.groups.findIndex(g => g.id === SUPER_GROUP);
  if (index !== -1) {
    return true;
  }

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

export function hasPermissionForProjectId(permission, projectId) {
  let user = getCurrentUser();
  if (!user || !user.groups) {
    return false;
  }
  let index = user.groups.findIndex(g => g.id === SUPER_GROUP);
  if (index !== -1) {
    return true;
  }

  user.userGroups.forEach(ug => {
    user.groupPermissions.forEach(gp => {
      if (gp.group.id === ug.groupId) {
        ug.userGroupPermissions = gp.userGroupPermissions;
        ug.group = gp.group;
      }
    });
  });

  let currentProjectPermissions = user.userGroups.filter(ug => ug.group && ug.group.type === 'PROJECT')
    .filter(ug => ug.sourceId === projectId)
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
  return false;
}

export function hasPermissions(...permissions) {
  for (let p of permissions) {
    if (hasPermission(p)) {
      return true;
    }
  }
  return false;
}

export function hasLicense() {
  let v = localStorage.getItem(LicenseKey);
  return v && v === 'valid';
}


export function removeLicense() {
  localStorage.removeItem(LicenseKey);
}

export function saveLicense(data) {
  // 保存License
  localStorage.setItem(LicenseKey, data);
}


export function validateAndSetLicense(data) {
  if (data === null || data.status !== 'valid') {
    removeLicense();
  } else {
    saveLicense(data.status);
  }
}
