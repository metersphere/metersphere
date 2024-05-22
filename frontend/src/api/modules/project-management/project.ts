import MSR from '@/api/http/index';
import { ProjectListUrl, ProjectSwitchUrl } from '@/api/requrls/project-management/project';

import type { ProjectListItem } from '@/models/setting/project';

export function getProjectList(organizationId: string) {
  return MSR.get<ProjectListItem[]>({ url: ProjectListUrl, params: organizationId }, { ignoreCancelToken: true });
}

export function switchProject(data: { projectId: string; userId: string }) {
  return MSR.post({ url: ProjectSwitchUrl, data });
}

export function getProjectInfo(projectId: string) {
  return MSR.get<ProjectListItem>({ url: `/project/get/${projectId}` });
}

export function getProjectListByOrgAndModule(orgId: string, module: string) {
  return MSR.get<ProjectListItem[]>({ url: `${ProjectListUrl}/${orgId}/${module}` });
}
