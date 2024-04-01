import { CSSProperties } from 'vue';
import { SelectProps } from '@arco-design/web-vue';

import { Size } from './types';

export const PAGE_ITEM_TYPES = ['page', 'more', 'previous', 'next', 'jumper'] as const;

export type PageItemType = (typeof PAGE_ITEM_TYPES)[number];

export interface PaginationProps {
  total?: number;
  current?: number;
  defaultCurrent?: number;
  pageSize?: number;
  defaultPageSize?: number;
  disabled?: boolean;
  hideOnSinglePage?: boolean;
  simple?: boolean;
  showTotal?: boolean;
  showMore?: boolean;
  showJumper?: boolean;
  showPageSize?: boolean;
  pageSizeOptions?: number[];
  pageSizeProps?: SelectProps;
  size?: Size;
  pageItemStyle?: CSSProperties;
  activePageItemStyle?: CSSProperties;
  baseSize?: number;
  bufferSize?: number;
}
