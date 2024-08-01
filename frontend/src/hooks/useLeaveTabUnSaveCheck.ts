import { onBeforeRouteLeave } from 'vue-router';

import { TabItem } from '@/components/pure/ms-editable-tab/types';

import { hasAnyPermission } from '@/utils/permission';

import { useI18n } from './useI18n';
import useModal from './useModal';

export default function useLeaveTabUnSaveCheck(tabs: TabItem[], permissions?: string[]) {
  const { openModal } = useModal();
  const { t } = useI18n();
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  let isLeaving = false;
  onBeforeRouteLeave((to, from, next) => {
    if (!isLeaving && tabs.some((tab) => tab.unSaved) && hasAnyPermission(permissions || [])) {
      isLeaving = true;
      // 如果有未保存的tab则提示用户
      openModal({
        type: 'warning',
        title: t('common.tip'),
        content: t('apiTestDebug.unsavedLeave'),
        hideCancel: false,
        cancelText: t('common.stay'),
        okText: t('common.leave'),
        onBeforeOk: async () => {
          next();
        },
        onCancel: () => {
          isLeaving = false;
        },
      });
    } else {
      next();
    }
  });

  // 页面有变更时，关闭或刷新页面弹出浏览器的保存提示
  window.onbeforeunload = () => {
    if (!isLeaving && tabs.some((tab) => tab.unSaved) && hasAnyPermission(permissions || [])) {
      return '';
    }
  };
}
