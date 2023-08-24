import { ref, onUnmounted } from 'vue';
import useModal from '@/hooks/useModal';
import { onBeforeRouteLeave } from 'vue-router';
import { useI18n } from '@/hooks/useI18n';

// 离开页面确认提示
export default function useLeaveUnSaveTip() {
  const isSave = ref(false);
  const { openModal } = useModal();
  const { t } = useI18n();

  const setSaveStatus = (status: boolean) => {
    isSave.value = status;
  };

  onBeforeRouteLeave((to, from, next) => {
    if (to.path === from.path) {
      return;
    }
    // 如果已保存
    if (isSave.value) {
      next();
    } else {
      openModal({
        type: 'error',
        title: t('common.unSaveLeaveTitle'),
        content: t('common.unSaveLeaveContent'),
        okText: t('common.leave'),
        cancelText: t('common.cancel'),
        okButtonProps: {
          status: 'normal',
        },
        onBeforeOk: async () => {
          setSaveStatus(true);
          next();
        },
        hideCancel: false,
      });
    }
  });
  onUnmounted(() => {
    isSave.value = false;
  });

  return {
    setSaveStatus,
  };
}
