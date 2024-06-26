/* eslint-disable no-multi-assign */
import * as key from '../tool/key';

// eslint-disable-next-line no-unused-vars
function ReceiverRuntime(this: any) {
  // 接收事件的 div
  const element = document.createElement('div');
  element.contentEditable = 'true';
  element.setAttribute('tabindex', '-1');
  element.classList.add('receiver');

  // 侦听器，接收到的事件会派发给所有侦听器
  // eslint-disable-next-line no-unused-vars
  const listeners: (((event: KeyboardEvent) => boolean) | string)[] = [];

  interface Receiver {
    element: HTMLDivElement;
    selectAll(): void;
    enable(): void;
    disable(): void;
    fixFFCaretDisappeared(): void;
    // eslint-disable-next-line no-unused-vars
    onblur(handler: (event: FocusEvent) => void): void;
    // eslint-disable-next-line no-unused-vars
    listen?(state: string, listener: ((event: KeyboardEvent) => boolean) | string): void;
  }

  // receiver 对象
  const receiver: Receiver = {
    element,
    selectAll() {
      // 保证有被选中的
      if (!element.innerHTML) element.innerHTML = '&nbsp;';
      const range = document.createRange();
      const selection = window.getSelection();
      range.selectNodeContents(element);
      selection?.removeAllRanges();
      selection?.addRange(range);
      element.focus();
    },
    enable() {
      element.setAttribute('contenteditable', 'true');
    },
    disable() {
      element.setAttribute('contenteditable', 'false');
    },
    fixFFCaretDisappeared() {
      element.removeAttribute('contenteditable');
      element.setAttribute('contenteditable', 'true');
      element.blur();
      element.focus();
    },
    onblur(handler: (event: FocusEvent) => void) {
      element.onblur = handler;
    },
  };

  // 侦听指定状态下的事件，如果不传 state，侦听所有状态
  receiver.listen = (...args) => {
    let [state, listener] = args;
    if (args.length <= 1) {
      listener = state;
      state = '*';
    }
    (listener as any).notifyState = state;
    listeners.push(listener as any);
  };

  const dispatchKeyEvent = (e: KeyboardEvent) => {
    (e as any).is = (keyExpression: string) => {
      const subs = keyExpression.split('|');
      for (let i = 0; i < subs.length; i++) {
        if (key.is(this, subs[i])) return true;
      }
      return false;
    };
    for (let i = 0; i < listeners.length; i++) {
      const listener = listeners[i];
      // 忽略不在侦听状态的侦听器
      if ((listener as any).notifyState !== '*' && (listener as any).notifyState !== this.fsm.state()) {
        // eslint-disable-next-line no-continue
        continue;
      }

      if (typeof listener === 'function' && listener.call(null, e)) {
        return;
      }
    }
  };

  element.onkeydown = element.onkeypress = element.onkeyup = dispatchKeyEvent;
  this.container.appendChild(element);

  receiver.selectAll();
  this.minder.on('beforemousedown', receiver.selectAll);
  this.minder.on('receiverfocus', receiver.selectAll);
  this.minder.on('readonly', () => {
    // 屏蔽 minder 的事件接受，删除 receiver
    this.minder.disable();
    window.editor.receiver.element.parentElement.removeChild(window.editor.receiver.element);
  });

  this.receiver = receiver as any;
}

export default ReceiverRuntime;
