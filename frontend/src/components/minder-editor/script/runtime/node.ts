import { isDisableNode, markDeleteNode, isDeleteDisableNode } from '../tool/utils';
import useLocaleNotVue from '../tool/useLocaleNotVue';

const tran = useLocaleNotVue;

const buttons = [
  `minder.menu.move.forward:Alt+Up:ArrangeUp`,
  `minder.menu.insert._down:Tab|Insert:AppendChildNode`,
  `minder.menu.insert._same:Enter:AppendSiblingNode`,
  `minder.menu.move.backward:Alt+Down:ArrangeDown`,
  `minder.commons.delete:Delete|Backspace:RemoveNode`,
  `minder.menu.insert._up:Shift+Tab|Shift+Insert:AppendParentNode`,
];

export default function NodeRuntime(this: { minder: any; hotbox: any; editText: () => any }) {
  const { minder, hotbox } = this;
  // eslint-disable-next-line @typescript-eslint/no-this-alias
  const runtime = this;

  const main = hotbox.state('main');

  let AppendLock = 0;

  buttons.forEach((button: string) => {
    const parts = button.split(':');
    const label = parts.shift();
    const key = parts.shift();
    const command = parts.shift() || '';
    main.button({
      position: 'ring',
      label: tran(label || ''),
      key,
      action() {
        if (command?.indexOf('Append') === 0) {
          AppendLock++;
          minder.execCommand(command, tran('minder.main.subject.branch'));

          const afterAppend = () => {
            if (!--AppendLock) {
              runtime.editText();
            }
            minder.off('layoutallfinish', afterAppend);
          };
          minder.on('layoutallfinish', afterAppend);
        } else {
          if (command.indexOf('RemoveNode') > -1) {
            if (window.minderProps?.delConfirm) {
              // 如果有删除确认，不删除，调用确认方法
              window.minderProps.delConfirm();
              return;
            }
            markDeleteNode(minder);
          }
          minder.execCommand(command);
        }
      },
      enable() {
        if (command.indexOf('RemoveNode') > -1) {
          if (
            isDeleteDisableNode(minder) &&
            command.indexOf('AppendChildNode') < 0 &&
            command.indexOf('AppendSiblingNode') < 0
          ) {
            return false;
          }
        } else if (command.indexOf('ArrangeUp') > 0 || command.indexOf('ArrangeDown') > 0) {
          if (!minder.moveEnable) {
            return false;
          }
        } else if (command.indexOf('AppendChildNode') < 0 && command.indexOf('AppendSiblingNode') < 0) {
          if (isDisableNode(minder)) return false;
        }
        const node = minder.getSelectedNode();
        if (node && node.parent === null && command.indexOf('AppendSiblingNode') > -1) {
          return false;
        }
        return minder.queryCommandState(command) !== -1;
      },
      beforeShow() {
        this.$button.children[0].innerHTML = tran(label || '');
      },
    });
  });

  main.button({
    position: 'ring',
    key: '/',
    action() {
      if (!minder.queryCommandState('expand')) {
        minder.execCommand('expand');
      } else if (!minder.queryCommandState('collapse')) {
        minder.execCommand('collapse');
      }
    },
    enable() {
      return minder.queryCommandState('expand') !== -1 || minder.queryCommandState('collapse') !== -1;
    },
    beforeShow() {
      if (!minder.queryCommandState('expand')) {
        this.$button.children[0].innerHTML = tran('minder.menu.expand.expand');
      } else {
        this.$button.children[0].innerHTML = tran('minder.menu.expand.folding');
      }
    },
  });
}
