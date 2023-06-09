import { Modal } from '@arco-design/web-vue';
import type { ModalConfig } from '@arco-design/web-vue';

export type ModalType = 'info' | 'success' | 'warning' | 'error';

export type ModalSize = 'small' | 'medium' | 'large' | 'full';

export type ModalMode = 'default' | 'weak';

export interface ModalOptions extends ModalConfig {
  mode?: ModalMode;
  type: ModalType;
  size?: ModalSize;
}

export default function useModal() {
  return {
    openModal: (options: ModalOptions) =>
      Modal[options.type]({
        ...options,
        titleAlign: 'start',
        modalClass: `ms-modal-${options.mode || 'default'} ms-modal-${options.size || 'medium'}`,
      }),
  };
}
