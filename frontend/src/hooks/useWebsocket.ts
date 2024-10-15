import { getSocket } from '@/api/modules/project-management/commonScript';

export interface WebsocketParams {
  reportId: string | number;
  socketUrl?: string;
  host?: string;
  onMessage?: (event: MessageEvent) => void;
}

export default function useWebsocket(options: WebsocketParams) {
  const websocket = ref<WebSocket>();
  function createSocket() {
    return new Promise((resolve) => {
      websocket.value = getSocket(options.reportId, options.socketUrl, options.host);
      websocket.value.addEventListener('message', (event) => {
        if (options.onMessage) {
          options.onMessage(event);
        }
      });
      websocket.value.addEventListener('open', () => {
        resolve(true);
      });
    });
  }

  return {
    websocket,
    createSocket,
  };
}
