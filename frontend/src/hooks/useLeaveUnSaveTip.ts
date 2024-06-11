import { onBeforeRouteLeave } from 'vue-router';

import { useI18n } from '@/hooks/useI18n';
import type { ModalType } from '@/hooks/useModal';
import useModal from '@/hooks/useModal';

export interface LeaveProps {
  leaveTitle: string;
  leaveContent: string;
  tipType: ModalType;
}

const leaveProps: LeaveProps = {
  leaveTitle: 'common.unSaveLeaveTitle',
  leaveContent: 'common.unSaveLeaveContent',
  tipType: 'warning',
};

// 离开页面确认提示
export default function useLeaveUnSaveTip(leaveProp = leaveProps) {
  const { openModal } = useModal();
  const { t } = useI18n();
  const { leaveTitle, leaveContent, tipType } = leaveProp;

  const isSave = ref(true);

  const setIsSave = (flag: boolean) => {
    isSave.value = flag;
  };
  onBeforeRouteLeave((to, from, next) => {
    if (to.path === from.path) {
      next();
      return;
    }

    if (!isSave.value) {
      openModal({
        type: tipType,
        title: t(leaveTitle),
        content: t(leaveContent),
        okText: t('common.leave'),
        cancelText: t('common.stay'),
        okButtonProps: {
          status: 'normal',
        },
        onBeforeOk: async () => {
          isSave.value = true;
          next();
        },
        hideCancel: false,
      });
    } else {
      next();
    }
  });
  return {
    setIsSave,
  };
}
