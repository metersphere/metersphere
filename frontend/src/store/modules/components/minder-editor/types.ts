import type { MinderJsonNode } from '@/components/pure/ms-minder-editor/props';

import type { MinderEventName } from '@/enums/minderEnum';

export interface MinderNodePosition {
  x: number;
  y: number;
  cx: number;
  cy: number;
  height: number;
  left: number;
  right: number;
  top: number;
  width: number;
  bottom: number;
}

export interface MinderCustomEvent {
  name: MinderEventName;
  eventId: string;
  params?: any;
  nodePosition?: MinderNodePosition;
  nodeDom?: HTMLElement;
  nodes?: MinderJsonNode[];
}

export interface MinderState {
  event: MinderCustomEvent;
  mold: number;
  clipboard: MinderJsonNode[]; // 剪切板
  minderUnsaved: boolean; // 脑图是否有未保存的内容
}
