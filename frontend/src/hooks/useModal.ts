import { Modal } from '@arco-design/web-vue';
import type { ModalConfig } from '@arco-design/web-vue';
import { useI18n } from '@/hooks/useI18n';
import { ref } from 'vue';

export type ModalType = 'info' | 'success' | 'warning' | 'error';

export type ModalSize = 'small' | 'medium' | 'large' | 'full';

export type ModalMode = 'default' | 'weak';

export interface ModalOptions extends ModalConfig {
  mode?: ModalMode;
  type: ModalType;
  size?: ModalSize;
  onBeforeOk: () => Promise<void>;
}

export interface DeleteModalOptions extends ModalConfig {
  title: string;
  content: string;
}
const { t } = useI18n();
export default function useModal() {
  const enableLoading = ref<boolean>(false);
  return {
    openModal: (options: ModalOptions) =>
      // error 使用 warning的感叹号图标
      Modal[options.type === 'error' ? 'warning' : options.type]({
        // 默认设置按钮属性，也可通过options传入覆盖
        okButtonProps: {
          type: options.mode === 'weak' ? 'text' : 'primary',
        },
        cancelButtonProps: {
          type: options.mode === 'weak' ? 'text' : 'secondary',
          disabled: enableLoading.value,
        },
        simple: false,
        okLoading: enableLoading.value,
        ...options,
        onBeforeOk: (done: (closed: boolean) => void) => {
          enableLoading.value = true;
          options?.onBeforeOk().finally(() => {
            enableLoading.value = false;
            done(true);
          });
        },
        onBeforeCancel: () => {
          return !enableLoading.value;
        },
        titleAlign: 'start',
        modalClass: `ms-usemodal ms-usemodal-${options.mode || 'default'} ms-usemodal-${
          options.size || 'small'
        } ms-usemodal-${options.type}`,
      }),
    openDeleteModal: (options: DeleteModalOptions) =>
      Modal.warning({
        okText: t('common.confirmDelete'),
        cancelText: t('common.cancel'),
        hideCancel: false,
        okButtonProps: { status: 'danger' },
        titleAlign: 'start',
        modalClass: `ms-usemodal ms-usemodal-warning ms-usemodal-small`,
        ...options,
      }),
  };
}
