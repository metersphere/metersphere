function ContainerRuntime(this: { selector: string; container?: HTMLElement }) {
  let container: HTMLElement | null;

  if (typeof this.selector === 'string') {
    container = document.querySelector(this.selector);
  } else {
    container = this.selector;
  }

  if (!container) throw new Error(`Invalid selector: ${this.selector}`);

  // 这个类名用于给编辑器添加样式
  container.classList.add('km-editor');

  // 暴露容器给其他运行时使用
  this.container = container;
}

export default ContainerRuntime;
