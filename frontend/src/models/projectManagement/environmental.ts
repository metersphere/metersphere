export interface EnvListItem {
  name: string;
  id: string;
}

export interface EnvGroupProjectListItem {
  name: string;
  env: string;
  host: string;
  description: string;
}
export interface EnvGroupListItem {
  name: string;
  id: string;
  projectList: EnvGroupProjectListItem[];
}
