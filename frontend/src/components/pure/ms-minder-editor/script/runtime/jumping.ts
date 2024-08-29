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

function JumpingRuntime(this: IJumpingRuntime): void {
  const { fsm, receiver } = this;

  // input => normal
  receiver.listen('input', (e: KeyboardEvent) => {
    receiver.enable();
    if (e.type === 'keydown') {
      if (e.code === 'Enter') {
        e.preventDefault();
        return setTimeout(() => {
          fsm.jump('normal', 'input-commit');
        }, 0);
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
