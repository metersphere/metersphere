export interface ReportBugItem {
  id: string;
  num: number;
  title: string;
  status: string;
  handleUserName: string;
  relationCaseCount: number;
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
  requestTime?: number;
}
