import type { LineType } from '@arco-design/web-vue/es/timeline/interface';

export interface MsTimeLineListItem {
  id?: string;
  label?: string;
  time: string;
  dotColor?: string;
  lineType?: LineType;
  isLast?: boolean; // 是否最后一条列表项，最后一个列表项展示的是 loading 或者暂无更多数据，因为列表支持滚动加载，所以最后一个 item 需要特殊处理
}
