import {
  getAdminByOrganizationOrProject,
  getAdminByProjectByOrg,
  getUserByOrganizationOrProject,
  getUserByProjectByOrg,
} from '@/api/modules/setting/organizationAndProject';
import { getOrgUserGroupOption, getSystemUserGroupOption } from '@/api/modules/setting/usergroup';

// eslint-disable-next-line no-shadow
export enum UserRequesetTypeEnum {
  SYSTEM_USER_GROUP = 'SYSTEM_USER_GROUP',
  SYSTEM_ORGANIZATION = 'SYSTEM_ORGANIZATION',
  SYSTEM_ORGANIZATION_ADMIN = 'SYSTEM_ORGANIZATION_ADMIN',
  SYSTEM_PROJECT = 'SYSTEM_PROJECT',
  SYSTEM_PROJECT_ADMIN = 'SYSTEM_PROJECT_ADMIN',
  ORGANIZATION_USER_GROUP = 'ORGANIZATION_USER_GROUP',
  ORGANIZATION_USER_GROUP_ADMIN = 'ORGANIZATION_USER_GROUP_ADMIN',
  ORGANIZATION_PROJECT = 'ORGANIZATION_PROJECT',
  ORGANIZATION_PROJECT_ADMIN = 'ORGANIZATION_PROJECT_ADMIN',
}
export default function initOptionsFunc(type: string, params: Record<string, any>) {
  if (type === UserRequesetTypeEnum.SYSTEM_USER_GROUP) {
    // 系统 - 用户组-添加成员-下拉选项
    return getSystemUserGroupOption(params.roleId, params.keyword);
  }
  if (type === UserRequesetTypeEnum.ORGANIZATION_USER_GROUP) {
    // 组织 - 用户组-添加成员-下拉选项
    return getOrgUserGroupOption(params.organizationId, params.roleId, params.keyword);
  }
  if (type === UserRequesetTypeEnum.SYSTEM_ORGANIZATION_ADMIN || type === UserRequesetTypeEnum.SYSTEM_PROJECT_ADMIN) {
    // 系统 - 【组织 或 项目】-添加管理员-下拉选项
    return getAdminByOrganizationOrProject(params.keyword);
  }
  if (type === UserRequesetTypeEnum.SYSTEM_ORGANIZATION || type === UserRequesetTypeEnum.SYSTEM_PROJECT) {
    // 系统 -【组织 或 项目】-添加成员-下拉选项
    return getUserByOrganizationOrProject(params.sourceId, params.keyword);
  }
  if (type === UserRequesetTypeEnum.ORGANIZATION_PROJECT) {
    // 组织 - 项目-添加成员-下拉选项
    return getUserByProjectByOrg(params.organizationId, params.projectId, params.keyword);
  }
  if (type === UserRequesetTypeEnum.ORGANIZATION_PROJECT_ADMIN) {
    // 组织 - 项目-添加管理员-下拉选项
    return getAdminByProjectByOrg(params.organizationId, params.keyword);
  }
}
