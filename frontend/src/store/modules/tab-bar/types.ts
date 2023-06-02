export interface TabProps {
  title: string;
  name: string;
  fullPath: string;
  query?: any;
  ignoreCache?: boolean;
}

export interface TabBarState {
  tabList: TabProps[];
  cacheTabList: Set<string>;
}
