export interface moduleKeysType {
  all?: string;
  selectAll: boolean;
  selectIds: Set<string>;
  excludeIds: Set<string>;
  count: number;
}

export interface saveParams {
  selectAll: boolean;
  selectIds: string[];
  excludeIds: string[];
  count: number;
}
