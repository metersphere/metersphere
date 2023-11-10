export interface MinderNodePosition {
  x: number;
  y: number;
}

export interface MinderEvent {
  name: string;
  timestamp: number;
  nodePosition: MinderNodePosition;
  nodeDom?: HTMLElement;
  nodeData?: Record<string, any>;
}

export interface MinderState {
  event: MinderEvent;
  mold: number;
}
