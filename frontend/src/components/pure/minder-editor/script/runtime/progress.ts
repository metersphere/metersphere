import { useI18n } from '@/hooks/useI18n';

import { isDisableNode } from '../tool/utils';

const { t } = useI18n();

export default function ProgressRuntime(this: any) {
  const { minder, hotbox } = this;

  const main = hotbox.state('main');

  main.button({
    position: 'top',
    label: t('minder.menu.progress.progress'),
    key: 'G',
    next: 'progress',
    enable() {
      if (isDisableNode(minder)) {
        return false;
      }
      return minder.queryCommandState('progress') !== -1;
    },
  });

  const progress = hotbox.state('progress');
  '012345678'.replace(/./g, (p) => {
    progress.button({
      position: 'ring',
      label: `G${p}`,
      key: p,
      action() {
        minder.execCommand('Progress', parseInt(p, 10) + 1);
      },
    });
    return p;
  });

  progress.button({
    position: 'center',
    label: t('minder.commons.remove'),
    key: 'Del',
    action() {
      minder.execCommand('Progress', 0);
    },
  });

  progress.button({
    position: 'top',
    label: t('minder.commons.return'),
    key: 'esc',
    next: 'back',
  });
}
