export interface TagProps {
  title: string;
  name: string;
  fullPath: string;
  query?: any;
  ignoreCache?: boolean;
}

export interface TabBarState {
  tabList: TagProps[];
  cacheTabList: Set<string>;
}
