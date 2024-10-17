import type { GlobalEventNameEnum } from '@/enums/commonEnum';

export interface GlobalStateEvent {
  id: string;
  name: GlobalEventNameEnum;
  params?: Record<string, any>;
}

export interface GlobalState {
  globalEvent?: GlobalStateEvent;
}
