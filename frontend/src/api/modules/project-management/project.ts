import MSR from '@/api/http/index';
import { ProjectListUrl } from '@/api/requrls/project-management/project';

import type { ProjectListItem } from '@/models/setting/project';

export function getProjectList(organizationId: string) {
  return MSR.get<ProjectListItem[]>({ url: ProjectListUrl, params: organizationId });
}

export default {};
