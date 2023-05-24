import { markDeleteNode, resetNodes } from '../tool/utils';

interface INode {
  getLevel(): number;
  isAncestorOf(node: INode): boolean;
  appendChild(node: INode): INode;
}

interface IData {
  getRegisterProtocol(protocol: string): {
    encode: (nodes: Array<INode>) => Array<INode>;
    decode: (nodes: Array<INode>) => Array<INode>;
  };
}

interface ICliboardEvent extends ClipboardEvent {
  clipboardData: DataTransfer;
}

export default function ClipboardRuntime(this: any) {
  const { minder } = this;
  const { receiver } = this;
  const Data: IData = window.kityminder.data;

  if (!minder.supportClipboardEvent || window.kity.Browser.gecko) {
    return;
  }

  const kmencode = this.MimeType.getMimeTypeProtocol('application/km');
  const { decode } = Data.getRegisterProtocol('json');
  let _selectedNodes: Array<INode> = [];

  function encode(nodes: Array<INode>): string {
    const _nodes = [];
    for (let i = 0, l = nodes.length; i < l; i++) {
      _nodes.push(minder.exportNode(nodes[i]));
    }
    return kmencode(Data.getRegisterProtocol('json').encode(_nodes));
  }

  const beforeCopy = (e: ICliboardEvent) => {
    if (document.activeElement === receiver.element) {
      const clipBoardEvent = e;
      const state = this.fsm.state();

      switch (state) {
        case 'input': {
          break;
        }
        case 'normal': {
          const nodes = [...minder.getSelectedNodes()];
          if (nodes.length) {
            if (nodes.length > 1) {
              let targetLevel;
              nodes.sort((a: any, b: any) => {
                return a.getLevel() - b.getLevel();
              });
              // eslint-disable-next-line prefer-const
              targetLevel = nodes[0].getLevel();
              if (targetLevel !== nodes[nodes.length - 1].getLevel()) {
                let pnode;
                let idx = 0;
                const l = nodes.length;
                let pidx = l - 1;

                pnode = nodes[pidx];

                while (pnode.getLevel() !== targetLevel) {
                  idx = 0;
                  while (idx < l && nodes[idx].getLevel() === targetLevel) {
                    if (nodes[idx].isAncestorOf(pnode)) {
                      nodes.splice(pidx, 1);
                      break;
                    }
                    idx++;
                  }
                  pidx--;
                  pnode = nodes[pidx];
                }
              }
            }
            const str = encode(nodes);
            clipBoardEvent.clipboardData.setData('text/plain', str);
          }
          e.preventDefault();
          break;
        }
        default:
      }
    }
  };

  const beforeCut = (e: ClipboardEvent) => {
    const { activeElement } = document;
    if (activeElement === receiver.element) {
      if (minder.getStatus() !== 'normal') {
        e.preventDefault();
        return;
      }

      const clipBoardEvent = e;
      const state = this.fsm.state();

      switch (state) {
        case 'input': {
          break;
        }
        case 'normal': {
          markDeleteNode(minder);
          const nodes = minder.getSelectedNodes();
          if (nodes.length) {
            clipBoardEvent.clipboardData?.setData('text/plain', encode(nodes));
            minder.execCommand('removenode');
          }
          e.preventDefault();
          break;
        }
        default:
      }
    }
  };

  const beforePaste = (e: ClipboardEvent) => {
    if (document.activeElement === receiver.element) {
      if (minder.getStatus() !== 'normal') {
        e.preventDefault();
        return;
      }

      const clipBoardEvent = e;
      const state = this.fsm.state();
      const textData = clipBoardEvent.clipboardData?.getData('text/plain');

      switch (state) {
        case 'input': {
          // input状态下如果格式为application/km则不进行paste操作
          if (!this.MimeType.isPureText(textData)) {
            e.preventDefault();
            return;
          }
          break;
        }
        case 'normal': {
          /*
           * 针对normal状态下通过对选中节点粘贴导入子节点文本进行单独处理
           */
          const sNodes = minder.getSelectedNodes();

          if (this.MimeType.whichMimeType(textData) === 'application/km') {
            const nodes = decode(this.MimeType.getPureText(textData));
            resetNodes(nodes);
            let _node;
            sNodes.forEach((node: INode) => {
              // 由于粘贴逻辑中为了排除子节点重新排序导致逆序，因此复制的时候倒过来
              for (let i = nodes.length - 1; i >= 0; i--) {
                _node = minder.createNode(null, node);
                minder.importNode(_node, nodes[i]);
                _selectedNodes.push(_node);
                node.appendChild(_node);
              }
            });
            minder.select(_selectedNodes, true);
            _selectedNodes = [];

            minder.refresh();
          } else if (clipBoardEvent.clipboardData && clipBoardEvent.clipboardData.items[0].type.indexOf('image') > -1) {
            const imageFile = clipBoardEvent.clipboardData.items[0].getAsFile();
            const serverService = window.angular.element(document.body).injector().get('server');

            return serverService.uploadImage(imageFile).then((json: Record<string, any>) => {
              const resp = json.data;
              if (resp.errno === 0) {
                minder.execCommand('image', resp.data.url);
              }
            });
          } else {
            sNodes.forEach((node: INode) => {
              minder.Text2Children(node, textData);
            });
          }
          e.preventDefault();
          break;
        }
        default:
      }
      // 触发命令监听
      minder.execCommand('paste');
    }
  };

  /**
   * 由editor的receiver统一处理全部事件，包括clipboard事件
   * @Editor: Naixor
   * @Date: 2015.9.24
   */
  document.addEventListener('copy', () => beforeCopy);
  document.addEventListener('cut', () => beforeCut);
  document.addEventListener('paste', () => beforePaste);
}
