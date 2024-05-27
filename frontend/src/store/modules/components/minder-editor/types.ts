import type { MinderJsonNode } from '@/components/pure/ms-minder-editor/props';

import type { MinderEventName } from '@/enums/minderEnum';

export interface MinderNodePosition {
  x: number;
  y: number;
}

export interface MinderEvent {
  name: MinderEventName;
  timestamp: number;
  nodePosition?: MinderNodePosition;
  nodeDom?: HTMLElement;
  node?: MinderJsonNode;
}

export interface MinderState {
  event: MinderEvent;
  mold: number;
}
