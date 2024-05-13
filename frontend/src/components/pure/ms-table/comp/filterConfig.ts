import { getProjectMemberOptions } from '@/api/modules/project-management/projectMember';

import { FilterRemoteMethodsEnum } from '@/enums/tableFilterEnum';

export function initRemoteOptionsFunc(remoteMethod: string, params: Record<string, any>) {
  switch (remoteMethod) {
    case FilterRemoteMethodsEnum.PROJECT_PERMISSION_MEMBER:
      return getProjectMemberOptions(params.projectId, params.keyword);
    default:
      break;
  }
}

export default {};
