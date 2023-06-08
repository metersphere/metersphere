import { isDisableNode, isTagEnable } from '../tool/utils';
import useLocaleNotVue from '../tool/useLocaleNotVue';

const tran = useLocaleNotVue;

// eslint-disable-next-line no-unused-vars
export default function TagRuntime(this: any) {
  const { minder, hotbox } = this;
  const main = hotbox.state('main');

  main.button({
    position: 'top',
    label: tran('minder.main.tag'),
    key: 'H',
    next: 'tag',
    enable() {
      if (isDisableNode(minder) && !isTagEnable(minder)) {
        return false;
      }
      return minder.queryCommandState('tag') !== -1;
    },
    beforeShow() {
      this.$button.children[0].innerHTML = tran('minder.main.tag');
    },
  });

  const tag = hotbox.state('tag');

  tag.button({
    position: 'center',
    label: tran('minder.commons.remove'),
    key: 'Del',
    action() {
      minder.execCommand('Tag', 0);
    },
    beforeShow() {
      this.$button.children[0].innerHTML = tran('minder.commons.remove');
    },
  });

  tag.button({
    position: 'top',
    label: tran('minder.commons.return'),
    key: 'esc',
    beforeShow() {
      this.$button.children[0].innerHTML = tran('minder.commons.return');
    },
    next: 'back',
  });
}
