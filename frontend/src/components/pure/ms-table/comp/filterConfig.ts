import { getProjectMemberOptions } from '@/api/modules/project-management/projectMember';
import { getProjectList } from '@/api/modules/setting/member';
import { getOrgOptions, getSystemProjectList } from '@/api/modules/system';

import { FilterRemoteMethodsEnum } from '@/enums/tableFilterEnum';

export function initRemoteOptionsFunc(remoteMethod: string, params: Record<string, any>) {
  switch (remoteMethod) {
    case FilterRemoteMethodsEnum.PROJECT_PERMISSION_MEMBER:
      return getProjectMemberOptions(params.projectId, params.keyword);
    case FilterRemoteMethodsEnum.SYSTEM_ORGANIZATION_LIST:
      return getOrgOptions();
    case FilterRemoteMethodsEnum.SYSTEM_PROJECT_LIST:
      return getSystemProjectList(params.keyword);
    case FilterRemoteMethodsEnum.SYSTEM_ORGANIZATION_PROJECT:
      return getProjectList(params.organizationId, params.keyword);
    default:
      break;
  }
}

export default {};
