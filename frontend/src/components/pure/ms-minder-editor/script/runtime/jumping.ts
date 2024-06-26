/**
 * @Desc: 下方使用receiver.enable()和receiver.disable()通过
 *        修改div contenteditable属性的hack来解决开启热核后依然无法屏蔽浏览器输入的bug;
 *        特别: win下FF对于此种情况必须要先blur在focus才能解决，但是由于这样做会导致用户
 *             输入法状态丢失，因此对FF暂不做处理
 * @Editor: Naixor
 * @Date: 2015.09.14
 */
interface IReceiver {
  element: HTMLElement;
  disable(): void;
  enable(): void;
  listen(type: string, callback: (e: KeyboardEvent) => void): void;
}

interface IMinder {
  getSelectedNode(): any;
}

interface IFsm {
  state(): string;
  jump(state: string, event: string, data?: any): void;
}

interface IJumpingRuntime {
  fsm: IFsm;
  minder: IMinder;
  receiver: IReceiver;
  container: HTMLElement;
}

// Nice: http://unixpapa.com/js/key.html
function isIntendToInput(e: KeyboardEvent): boolean {
  if (e.ctrlKey || e.metaKey || e.altKey) return false;

  // a-zA-Z
  if (e.keyCode >= 65 && e.keyCode <= 90) return true;

  // 0-9 以及其上面的符号
  if (e.keyCode >= 48 && e.keyCode <= 57) return true;

  // 小键盘区域 (除回车外)
  if (e.keyCode !== 108 && e.keyCode >= 96 && e.keyCode <= 111) return true;

  // 小键盘区域 (除回车外)
  // @yinheli from pull request
  if (e.keyCode !== 108 && e.keyCode >= 96 && e.keyCode <= 111) return true;

  // 输入法
  if (e.keyCode === 229 || e.keyCode === 0) return true;

  return false;
}

function JumpingRuntime(this: IJumpingRuntime): void {
  const { fsm, minder, receiver } = this;
  const receiverElement = receiver.element;
  // normal -> *
  receiver.listen('normal', (e: KeyboardEvent) => {
    // 为了防止处理进入edit模式而丢失处理的首字母,此时receiver必须为enable
    receiver.enable();
    /**
     * check
     * @editor Naixor
     * @Date 2015-12-2
     */
    switch (e.type) {
      case 'keydown': {
        if (minder.getSelectedNode()) {
          if (isIntendToInput(e)) {
            return fsm.jump('input', 'user-input');
          }
        } else {
          receiverElement.innerHTML = '';
        }
        // normal -> normal shortcut
        // fsm.jump('normal', 'shortcut-handle', e); TODO: 未来需要支持自定义快捷键处理逻辑
        break;
      }
      case 'keyup': {
        break;
      }
      default:
    }
  });

  // input => normal
  receiver.listen('input', (e: KeyboardEvent) => {
    receiver.enable();
    if (e.type === 'keydown') {
      if (e.code === 'Enter') {
        e.preventDefault();
        return fsm.jump('normal', 'input-commit');
      }
      if (e.code === 'Escape') {
        e.preventDefault();
        return fsm.jump('normal', 'input-cancel');
      }
      if (e.code === 'Tab' || (e.shiftKey && e.code === 'Tab')) {
        e.preventDefault();
      }
    } else if (e.type === 'keyup' && e.code === 'Escape') {
      e.preventDefault();
      return fsm.jump('normal', 'input-cancel');
    }
  });
}

export default JumpingRuntime;
