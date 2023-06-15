export interface UserGroupListItem {
  name: string;
  id: number;
  title?: string;
  authScope: string;
}
export interface UserOption {
  id: number;
  name: string;
  email: string;
}
export interface CustomMoreActionItem {
  eventKey: string;
  name: string;
}
export interface PopVisibleItem {
  [key: number]: boolean;
}

export type RenameType = 'rename' | 'auth';
