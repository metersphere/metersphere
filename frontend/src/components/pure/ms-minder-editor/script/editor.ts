import '@7polo/kity/dist/kity';
import '@7polo/kityminder-core';
import clipboardMimetype from './runtime/clipboard-mimetype';
import container from './runtime/container';
import drag from './runtime/drag';
import exportsRuntime from './runtime/exports';
import fsm from './runtime/fsm';
import history from './runtime/history';
import input from './runtime/input';
import jumping from './runtime/jumping';
import minder from './runtime/minder';
import receiver from './runtime/receiver';

type EditMenuProps = {
  sequenceEnable: boolean;
  tagEnable: boolean;
  progressEnable: boolean;
  moveEnable: boolean;
  [key: string]: any;
};

type Runtime = {
  name: string;
  // eslint-disable-next-line no-use-before-define
  call: (thisArg: KMEditor, editor: KMEditor) => void;
};

const runtimes: Runtime[] = [];

function assemble(runtime: Runtime) {
  runtimes.push(runtime);
}

function isEnable(editMenuProps: EditMenuProps, runtime: Runtime) {
  switch (runtime.name) {
    case 'PriorityRuntime':
      return editMenuProps.sequenceEnable === true;
    case 'TagRuntime':
      return editMenuProps.tagEnable === true;
    case 'ProgressRuntime':
      return editMenuProps.progressEnable === true;
    default:
      return true;
  }
}

class KMEditor {
  public selector: HTMLDivElement | null | string;

  public editMenuProps: EditMenuProps;

  constructor(selector: HTMLDivElement | null | string, editMenuPropsC: EditMenuProps) {
    this.selector = selector;
    this.editMenuProps = editMenuPropsC;
    this.init();
  }

  public init() {
    for (let i = 0; i < runtimes.length; i++) {
      if (typeof runtimes[i].call === 'function' && isEnable(this.editMenuProps, runtimes[i])) {
        runtimes[i].call(this, this);
      }
    }
  }
}

assemble(container);
assemble(fsm);
assemble(minder);
assemble(receiver);
assemble(input);
assemble(clipboardMimetype);
assemble(drag);
assemble(history);
assemble(jumping);
assemble(exportsRuntime);

window.kityminder.Editor = KMEditor;

export default KMEditor;
