export interface ReportBugItem {
  id: string;
  num: number;
  title: string;
  status: string;
  handleUserName: string;
  relationCaseCount: number;
}

export interface FeatureCaseItem {
  id: string;
  num: number;
  name: string;
  moduleName: string;
  priority: string;
  executeResult: string;
  executeUserName: string;
  bugCount: number;
  reportId: string;
}

export interface UpdateReportDetailParams {
  id: string;
  componentId: string;
  componentValue?: string;
  richTextTmpFileIds?: string[];
}

export interface ApiOrScenarioCaseItem {
  id: string;
  num: number;
  name: string;
  moduleName: string;
  priority: string;
  executeResult: string;
  executeUser: string;
  bugCount: number;
  reportId: string;
  projectId: string;
}
