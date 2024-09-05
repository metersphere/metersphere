import Debug from '../tool/debug';

function handlerConditionMatch(condition: any, when: string, exit: string, enter: string): boolean {
  if (condition.when !== when) {
    return false;
  }
  if (condition.enter !== '*' && condition.enter !== enter) {
    return false;
  }
  if (condition.exit !== '*' && condition.exit !== exit) {
    return false;
  }
  return true;
}

class FSM {
  private currentState: string;

  private readonly BEFORE_ARROW: string = ' - ';

  private readonly AFTER_ARROW: string = ' -> ';

  private handlers: any[] = [];

  private debug: Debug;

  constructor(defaultState: string) {
    this.currentState = defaultState;
    this.debug = new Debug('fsm');
  }

  /**
   * 状态跳转
   *
   * 会通知所有的状态跳转监视器
   *
   * @param  {string} newState  新状态名称
   * @param  {any} reason 跳转的原因，可以作为参数传递给跳转监视器
   */
  public jump(newState: string, reason: any, ...args: any): string {
    if (!reason) {
      throw new Error('Please tell fsm the reason to jump');
    }

    const oldState = this.currentState;
    const notify = [oldState, newState].concat([reason, args[0]]);
    let i;
    let handler;

    // 跳转前
    for (i = 0; i < this.handlers.length; i++) {
      handler = this.handlers[i];
      if (handlerConditionMatch(handler.condition, 'before', oldState, newState)) {
        if (handler.apply(null, [...notify])) {
          return newState;
        }
      }
    }

    this.currentState = newState;
    this.debug.log('[{0}] {1} -> {2}', reason, oldState, newState);
    // 跳转后
    for (i = 0; i < this.handlers.length; i++) {
      handler = this.handlers[i];
      if (handlerConditionMatch(handler.condition, 'after', oldState, newState)) {
        handler.apply(null, [...notify]);
      }
    }
    return newState;
  }

  /**
   * 返回当前状态
   * @return {string}
   */
  public state(): string {
    return this.currentState;
  }

  /**
   * 添加状态跳转监视器
   *
   * @param {string} condition
   *     监视的时机
   *         "* => *" （默认）
   *
   * @param  {Handler} handler
   *     监视函数，当状态跳转的时候，会接收三个参数
   *         * from - 跳转前的状态
   *         * to - 跳转后的状态
   *         * reason - 跳转的原因
   */
  public when(...args: any[]): void {
    let [condition, handler] = args;
    if (args.length === 1) {
      handler = condition;
      condition = '* -> *';
    }

    let whenVar = '';
    let resolved: string[];
    let exit: string;
    let enter: string;

    resolved = condition.split(this.BEFORE_ARROW);
    if (resolved.length === 2) {
      whenVar = 'before';
    } else {
      resolved = condition.split(this.AFTER_ARROW);
      if (resolved.length === 2) {
        whenVar = 'after';
      }
    }
    if (!whenVar) {
      throw new Error(`Illegal fsm condition: ${condition}`);
    }

    // eslint-disable-next-line prefer-destructuring, prefer-const
    exit = resolved[0];
    // eslint-disable-next-line prefer-destructuring, prefer-const
    enter = resolved[1];

    (handler as any).condition = {
      when: whenVar,
      exit,
      enter,
    };

    this.handlers.push(handler);
  }
}

function FSMRuntime(this: any) {
  this.fsm = new FSM('normal');
  this.fsm.when('normal -> normal', (exit: any, enter: any, reason: string, e: KeyboardEvent) => {
    const arrowKey = ['ArrowRight', 'ArrowLeft', 'ArrowUp', 'ArrowDown'];
    if (reason === 'shortcut-handle' && arrowKey.includes(e.code)) {
      this.minder.dispatchKeyEvent(e); // 触发脑图本身的方向快捷键事件
    }
  });
}

export default FSMRuntime;
