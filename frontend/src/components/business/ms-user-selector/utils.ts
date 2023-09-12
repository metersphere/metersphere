import {
  getAdminByOrganizationOrProject,
  getAdminByProjectByOrg,
  getUserByOrganizationOrProject,
  getUserByProjectByOrg,
} from '@/api/modules/setting/organizationAndProject';
import { getOrgUserGroupOption, getSystemUserGroupOption } from '@/api/modules/setting/usergroup';
import { getUser, getProjectList } from '@/api/modules/setting/member';
import { getProjectMemberOptions } from '@/api/modules/project-management/projectMember';

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
  SYSTEM_ORGANIZATION_PROJECT = 'SYSTEM_ORGANIZATION_PROJECT',
  SYSTEM_ORGANIZATION_MEMBER = 'SYSTEM_ORGANIZATION_MEMBER',
  PROJECT_PERMISSION_MEMBER = 'PROJECT_PERMISSION_MEMBER',
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
  if (type === UserRequesetTypeEnum.SYSTEM_ORGANIZATION_PROJECT) {
    // 系统-组织-组织项目
    return getProjectList(params.organizationId, params.keyword);
  }
  if (type === UserRequesetTypeEnum.SYSTEM_ORGANIZATION_MEMBER) {
    // 系统-组织-组织成员
    return getUser(params.organizationId, params.keyword);
  }
  if (type === UserRequesetTypeEnum.PROJECT_PERMISSION_MEMBER) {
    // 系统-组织-项目成员
    return getProjectMemberOptions(params.projectId, params.keyword);
  }
}
