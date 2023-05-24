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
  const listeners: ((event: KeyboardEvent) => boolean)[] = [];

  const dispatchKeyEvent = (e: KeyboardEvent) => {
    (e as any).is = (keyExpression: string) => {
      const subs = keyExpression.split('|');
      for (let i = 0; i < subs.length; i++) {
        if (key.is(this, subs[i])) return true;
      }
      return false;
    };
    let listener;
    for (let i = 0; i < listeners.length; i++) {
      listener = listeners[i];
      // 忽略不在侦听状态的侦听器
      if ((listener as any).notifyState !== '*' && (listener as any).notifyState !== this.fsm.state()) {
        // eslint-disable-next-line no-continue
        continue;
      }

      if (listener.call(null, e)) {
        return;
      }
    }
  };

  element.onkeydown = element.onkeypress = element.onkeyup = dispatchKeyEvent;
  this.container.appendChild(element);

  interface Receiver {
    element: HTMLDivElement;
    selectAll(): void;
    enable(): void;
    disable(): void;
    fixFFCaretDisappeared(): void;
    // eslint-disable-next-line no-unused-vars
    onblur(handler: (event: FocusEvent) => void): void;
    // eslint-disable-next-line no-unused-vars
    listen?(state: string, listener: (event: KeyboardEvent) => boolean): void;
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
    // eslint-disable-next-line no-unused-vars
    onblur(handler: (event: FocusEvent) => void) {
      element.onblur = handler;
    },
  };
  receiver.selectAll();
  this.minder.on('beforemousedown', receiver.selectAll);
  this.minder.on('receiverfocus', receiver.selectAll);
  this.minder.on('readonly', () => {
    // 屏蔽 minder 的事件接受，删除 receiver 和 hotbox
    this.minder.disable();
    window.editor.receiver.element.parentElement.removeChild(window.editor.receiver.element);
    window.editor.hotbox.$container.removeChild(window.editor.hotbox.$element);
  });

  // 侦听指定状态下的事件，如果不传 state，侦听所有状态
  // eslint-disable-next-line no-unused-vars
  receiver.listen = (state: string, listener: ((event: KeyboardEvent) => boolean) | string) => {
    if (arguments.length === 1) {
      // eslint-disable-next-line no-param-reassign
      listener = state;
      // eslint-disable-next-line no-param-reassign
      state = '*';
    }
    // eslint-disable-next-line no-param-reassign
    if (typeof listener === 'function') {
      (listener as any).notifyState = state;
    }
    listeners.push(listener as any);
  };

  this.receiver = receiver as any;
}

export default ReceiverRuntime;
