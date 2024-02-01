import { Ref, ref } from 'vue';
import { Modal } from '@arco-design/web-vue';

import { useI18n } from '@/hooks/useI18n';

import type { ModalConfig } from '@arco-design/web-vue';

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
  const beforeOkHandler = (options: any, done: (closed: boolean) => void, loading: Ref) => {
    loading.value = true;
    options?.onBeforeOk().finally(() => {
      loading.value = false;
      done(true);
    });
  };
  return {
    openModal: (options: ModalOptions) => {
      const okHandlerLoading = ref<boolean>(false);
      Modal[options.type]({
        // 默认设置按钮属性，也可通过options传入覆盖
        okButtonProps: {
          type: options.mode === 'weak' ? 'text' : 'primary',
        },
        cancelButtonProps: {
          type: options.mode === 'weak' ? 'text' : 'secondary',
          disabled: okHandlerLoading.value,
        },
        simple: false,
        okLoading: okHandlerLoading.value,
        ...options,
        onBeforeOk: (done: (closed: boolean) => void) => {
          beforeOkHandler(options, done, okHandlerLoading);
        },
        onBeforeCancel: () => {
          return !okHandlerLoading.value;
        },
        titleAlign: 'start',
        modalClass: `ms-usemodal ms-usemodal-${options.mode || 'default'} ms-usemodal-${
          options.size || 'small'
        } ms-usemodal-${options.type}`,
      });
    },
    openDeleteModal: (options: DeleteModalOptions) => {
      const deleteLoading = ref<boolean>(false);
      Modal.error({
        okText: t('common.confirmDelete'),
        cancelText: t('common.cancel'),
        hideCancel: false,
        simple: false,
        okButtonProps: { status: 'danger' },
        titleAlign: 'start',
        modalClass: `ms-usemodal ms-usemodal-warning ms-usemodal-small`,
        ...options,
        okLoading: deleteLoading.value,
        cancelButtonProps: {
          disabled: deleteLoading.value,
        },
        onBeforeOk: (done: (closed: boolean) => void) => {
          beforeOkHandler(options, done, deleteLoading);
        },
        onBeforeCancel: () => {
          return !deleteLoading.value;
        },
      });
    },
  };
}
