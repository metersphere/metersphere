/* eslint-disable no-restricted-syntax */
import {
  type AppContext,
  type Component,
  type ComponentPublicInstance,
  createVNode,
  getCurrentInstance,
  render,
  type VNode,
} from 'vue';

export type Components = Component;

export interface Options {
  visible?: boolean;
  onClose?: () => void;
  appendTo?: HTMLElement | string;
  [key: string]: unknown;
}

export interface CommandComponent1 {
  (options: Options): VNode;
  close: () => void;
}

const getAppendToElement = (props: Options): HTMLElement => {
  let appendTo: HTMLElement | null = document.body;
  if (props.appendTo) {
    if (typeof props.appendTo === 'string') {
      appendTo = document.querySelector<HTMLElement>(props.appendTo);
    }
    if (props.appendTo instanceof HTMLElement) {
      appendTo = props.appendTo;
    }
    if (!(appendTo instanceof HTMLElement)) {
      appendTo = document.body;
    }
  }
  return appendTo;
};

const initInstance = <T extends Components>(
  // eslint-disable-next-line no-shadow
  Component: T,
  props: Options,
  container: HTMLElement,
  appContext: AppContext | null = null
) => {
  const vNode = createVNode(Component, props);
  vNode.appContext = appContext;
  render(vNode, container);

  getAppendToElement(props).appendChild(container);
  return vNode;
};

// eslint-disable-next-line no-shadow
export const useCommandComponent = <T extends Components>(Component: T): CommandComponent1 => {
  const appContext = getCurrentInstance()?.appContext;
  if (appContext) {
    const currentProvides = (getCurrentInstance() as any)?.provides;
    Reflect.set(appContext, 'provides', { ...appContext.provides, ...currentProvides });
  }

  const container = document.createElement('div');

  const close = () => {
    render(null, container);
    container.parentNode?.removeChild(container);
  };

  const CommandComponent = (options: Options): VNode => {
    if (!Reflect.has(options, 'visible')) {
      options.visible = true;
    }
    if (typeof options.onClose !== 'function') {
      options.onClose = close;
    } else {
      const originOnClose = options.onClose;
      options.onClose = () => {
        originOnClose();
        close();
      };
    }
    const vNode = initInstance<T>(Component, options, container, appContext);
    const vm = vNode.component?.proxy as ComponentPublicInstance<Options>;
    for (const prop in options) {
      if (Reflect.has(options, prop) && !Reflect.has(vm.$props, prop)) {
        vm[prop as keyof ComponentPublicInstance] = options[prop];
      }
    }
    return vNode;
  };

  CommandComponent.close = close;

  return CommandComponent;
};

export default useCommandComponent;
