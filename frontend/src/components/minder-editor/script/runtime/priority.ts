import { isDisableNode } from '../tool/utils';
import useLocaleNotVue from '../tool/useLocaleNotVue';

const tran = useLocaleNotVue;

export default function PriorityRuntime(this: any): void {
  const { minder, hotbox } = this;

  const main = hotbox.state('main');

  main.button({
    position: 'top',
    label: tran('minder.main.priority'),
    key: 'P',
    next: 'priority',
    enable() {
      if (isDisableNode(minder)) {
        return false;
      }
      return minder.queryCommandState('priority') !== -1;
    },
    beforeShow() {
      this.$button.children[0].innerHTML = tran('minder.main.priority');
    },
  });

  const priority = hotbox.state('priority');

  priority.button({
    position: 'center',
    label: tran('minder.commons.remove'),
    key: 'Del',
    action() {
      minder.execCommand('Priority', 0);
    },
    beforeShow() {
      this.$button.children[0].innerHTML = tran('minder.commons.remove');
    },
  });

  priority.button({
    position: 'top',
    label: tran('minder.commons.return'),
    key: 'esc',
    beforeShow() {
      this.$button.children[0].innerHTML = tran('minder.commons.return');
    },
    next: 'back',
  });
}
