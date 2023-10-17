import MSR from '@/api/http/index';
import { ProjectBasicInfoUrl, UpdateProjectUrl } from '@/api/requrls/project-management/basicInfo';

import type { ProjectBasicInfoModel, UpdateProject } from '@/models/projectManagement/basicInfo';

// 获取项目详情
export function getProjectInfo(id: string) {
  return MSR.get<ProjectBasicInfoModel>({ url: ProjectBasicInfoUrl, params: id });
}

// 更新项目
export function updateProject(data: UpdateProject) {
  return MSR.post({ url: UpdateProjectUrl, data });
}
