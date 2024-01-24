import { useAppStore, useUserStore } from '@/store';
import { SystemScopeType, UserRole, UserRolePermissions } from '@/store/modules/user/types';

export function hasPermission(permission: string, typeList: string[]) {
  const userStore = useUserStore();
  if (userStore.isAdmin) {
    return true;
  }
  const { projectPermissions, orgPermissions, systemPermissions } = userStore.currentRole;

  if (typeList.includes('PROJECT') && projectPermissions.includes(permission)) {
    return true;
  }
  if (typeList.includes('ORGANIZATION') && orgPermissions.includes(permission)) {
    return true;
  }
  if (typeList.includes('SYSTEM') && systemPermissions.includes(permission)) {
    return true;
  }
  return false;
}

// 判断是否有权限
export function hasAnyPermission(permissions: string[], typeList = ['PROJECT', 'ORGANIZATION', 'SYSTEM']) {
  if (!permissions || permissions.length === 0) {
    return true;
  }
  return permissions.some((permission) => hasPermission(permission, typeList));
}

function filterProject(role: UserRole, id: string) {
  return role && role.type === 'PROJECT' && role.scopeId === id;
}
function filterOrganization(role: UserRole, id: string) {
  return role && role.type === 'ORGANIZATION' && role.scopeId === id;
}
function filterSystem(role: UserRole, id: string) {
  return role && role.type === 'SYSTEM' && role.scopeId === id;
}

export function composePermissions(userRolePermissions: UserRolePermissions[], type: SystemScopeType, id: string) {
  let func: (role: UserRole, val: string) => boolean;
  switch (type) {
    case 'PROJECT':
      func = filterProject;
      break;
    case 'ORGANIZATION':
      func = filterOrganization;
      break;
    default:
      func = filterSystem;
      break;
  }
  return userRolePermissions
    .filter((ur) => func(ur.userRole, id))
    .flatMap((role) => role.userRolePermissions)
    .map((g) => g.permissionId);
}

// 判断当前一级菜单是否有权限
export function hasFirstMenuPermission(menuName: string) {
  const userStore = useUserStore();
  const appStore = useAppStore();
  if (userStore.isAdmin || menuName === 'setting' || menuName === 'projectManagement') {
    // 如果是超级管理员，或者是系统设置菜单，或者是项目菜单，都有权限
    return true;
  }
  const { currentMenuConfig } = appStore;
  return currentMenuConfig.includes(menuName);
}
