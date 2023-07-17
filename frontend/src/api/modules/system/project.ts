import MSR from '@/api/http/index';
import { ProjectListUrl } from '@/api/requrls/system/project';
import type { ProjectListItem } from '@/models/system/project';

export function getProjectList(organizationId: string) {
  return MSR.get<ProjectListItem[]>({ url: ProjectListUrl, params: organizationId });
}

export default {};
