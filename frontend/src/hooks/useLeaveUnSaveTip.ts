import useModal from '@/hooks/useModal';
import { onBeforeRouteLeave } from 'vue-router';
import { useI18n } from '@/hooks/useI18n';

// 离开页面确认提示
export default function useLeaveUnSaveTip() {
  const { openModal } = useModal();
  const { t } = useI18n();

  onBeforeRouteLeave((to, from, next) => {
    if (to.path === from.path) {
      return;
    }
    console.log(from);
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
        next();
      },
      onCancel: () => {
        console.log('取消取消');
        console.log(from);
      },
      hideCancel: false,
    });
  });
}
