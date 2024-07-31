import useMinderStore from '@/store/modules/components/minder-editor';
import { getGenerateId } from '@/utils';

import { MinderEventName } from '@/enums/minderEnum';

import type { MinderJsonNode } from '../props';
import { markDeleteNode, resetNodes } from '../script/tool/utils';

interface IData {
  getRegisterProtocol(protocol: string): {
    encode: (nodes: Array<MinderJsonNode>) => Array<MinderJsonNode>;
    decode: (nodes: Array<MinderJsonNode>) => Array<MinderJsonNode>;
  };
}

export interface MinderOperationProps {
  insertNode?: (node: MinderJsonNode, command: string, value?: string) => void;
  canShowMoreMenuNodeOperation?: boolean;
  canShowPasteMenu?: boolean;
}

export default function useMinderOperation({
  insertNode,
  canShowMoreMenuNodeOperation,
  canShowPasteMenu,
}: MinderOperationProps) {
  const minderStore = useMinderStore();

  function encode(nodes: Array<MinderJsonNode>): string {
    const { editor } = window;
    const { minder, MimeType } = editor;
    const Data: IData = window.kityminder.data;
    const kmencode = MimeType.getMimeTypeProtocol('application/km');
    const _nodes = [];
    for (let i = 0, l = nodes.length; i < l; i++) {
      // @ts-ignore
      _nodes.push(minder.exportNode(nodes[i]));
    }
    return kmencode(Data.getRegisterProtocol('json').encode(_nodes));
  }

  /**
   * 执行复制
   */
  const minderCopy = (e?: ClipboardEvent) => {
    if (!canShowMoreMenuNodeOperation) {
      e?.preventDefault();
      return;
    }
    const { editor } = window;
    const { minder, fsm } = editor;
    const state = fsm.state();
    switch (state) {
      case 'input': {
        break;
      }
      case 'normal': {
        const selectedNodes = minder.getSelectedNodes();
        minderStore.dispatchEvent(MinderEventName.COPY_NODE, undefined, undefined, undefined, selectedNodes);
        minder.execCommand('Copy');
        e?.preventDefault();
        break;
      }
      default:
    }
  };

  /**
   * 执行剪切
   */
  const minderCut = (e?: ClipboardEvent) => {
    if (!canShowMoreMenuNodeOperation) {
      e?.preventDefault();
      return;
    }
    const { editor } = window;
    const { minder, fsm } = editor;
    if (minder.getStatus() !== 'normal') {
      e?.preventDefault();
      return;
    }

    const state = fsm.state();

    switch (state) {
      case 'input': {
        break;
      }
      case 'normal': {
        markDeleteNode(minder);
        const selectedNodes = minder.getSelectedNodes();
        if (selectedNodes.length) {
          e?.clipboardData?.setData('text/plain', encode(selectedNodes));
          minder.execCommand('Cut');
        }
        e?.preventDefault();
        minderStore.dispatchEvent(MinderEventName.CUT_NODE, undefined, undefined, undefined, selectedNodes);
        break;
      }
      default:
    }
  };

  /**
   * 执行粘贴
   */
  const minderPaste = (e?: ClipboardEvent) => {
    if (!canShowMoreMenuNodeOperation && !canShowPasteMenu) {
      e?.preventDefault();
      return;
    }
    const { editor } = window;
    const { minder, fsm, MimeType } = editor;
    const Data: IData = window.kityminder.data;
    const { decode } = Data.getRegisterProtocol('json');
    if (minder.getStatus() !== 'normal') {
      e?.preventDefault();
      return;
    }

    const state = fsm.state();
    const textData = e?.clipboardData?.getData('text/plain');

    switch (state) {
      case 'input': {
        // input状态下如果格式为application/km则不进行paste操作
        if (!MimeType.isPureText(textData)) {
          e?.preventDefault();
        }
        break;
      }
      case 'normal': {
        /*
         * 针对normal状态下通过对选中节点粘贴导入子节点文本进行单独处理
         */
        const sNodes = minder.getSelectedNodes();
        const selectedNodes: MinderJsonNode[] = [];
        minderStore.dispatchEvent(MinderEventName.PASTE_NODE, undefined, undefined, undefined, selectedNodes);
        if (MimeType.whichMimeType(textData) === 'application/km') {
          const nodes = decode(MimeType.getPureText(textData));
          sNodes.forEach((node: MinderJsonNode) => {
            const noFakeNodeTree = resetNodes(nodes); // 每次粘贴复制的节点集合到新的节点之前，都需要重置节点集合的信息
            let _node;
            // 由于粘贴逻辑中为了排除子节点重新排序导致逆序，因此复制的时候倒过来
            for (let i = noFakeNodeTree.length - 1; i >= 0; i--) {
              _node = minder.createNode(null, node);
              minder.importNode(_node, noFakeNodeTree[i]);
              selectedNodes.push(_node);
              node.appendChild(_node);
            }
          });
          window.minder.execCommand('Expand'); // 展开当前所有选中的节点，避免无子节点的节点在粘贴后还是折叠状态
          minder.select(selectedNodes, true);
          minder.refresh();
        } else if (e?.clipboardData && e.clipboardData.items[0].type.indexOf('image') > -1) {
          const imageFile = e?.clipboardData.items[0].getAsFile();
          const serverService = window.angular.element(document.body).injector().get('server');

          return serverService.uploadImage(imageFile).then((json: Record<string, any>) => {
            const resp = json.data;
            if (resp.errno === 0) {
              minder.execCommand('image', resp.data.url);
            }
          });
        } else {
          sNodes.forEach((node: MinderJsonNode) => {
            minder.Text2Children(node, textData);
          });
        }
        e?.preventDefault();
        break;
      }
      default:
    }
  };

  /**
   * 执行插入
   * @param command 插入命令
   * @param value 携带的参数
   */
  const execInsertCommand = (command: string, value?: string) => {
    const node: MinderJsonNode = window.minder.getSelectedNode();
    if (insertNode) {
      insertNode(node, command, value);
      return;
    }
    if (window.minder.queryCommandState(command) !== -1) {
      window.minder.execCommand(command);
      nextTick(() => {
        const newNode: MinderJsonNode = window.minder.getSelectedNode();
        if (!newNode.data) {
          newNode.data = {
            id: getGenerateId(),
            text: '',
          };
        }
        newNode.data.isNew = true; // 新建的节点标记为新建
      });
    }
  };

  /**
   * 插入子节点
   * @param selectedNodes 当前选中的节点集合
   * @param value 携带的参数
   */
  const appendChildNode = (selectedNodes: MinderJsonNode[], value?: string) => {
    execInsertCommand('AppendChildNode', value);
    minderStore.dispatchEvent(MinderEventName.INSERT_CHILD, value, undefined, undefined, selectedNodes);
  };

  /**
   * 插入兄弟节点
   * @param selectedNodes 当前选中的节点集合
   * @param value 携带的参数
   */
  const appendSiblingNode = (selectedNodes: MinderJsonNode[], value?: string) => {
    execInsertCommand('AppendSiblingNode', value);
    minderStore.dispatchEvent(MinderEventName.INSERT_SIBLING, value, undefined, undefined, selectedNodes);
  };

  /**
   * 删除节点
   * @param selectedNodes 当前选中的节点集合
   */
  const minderDelete = (selectedNodes: MinderJsonNode[]) => {
    minderStore.dispatchEvent(MinderEventName.DELETE_NODE, undefined, undefined, undefined, selectedNodes);
    window.minder.execCommand('RemoveNode');
  };

  return {
    minderCopy,
    minderCut,
    minderPaste,
    appendChildNode,
    appendSiblingNode,
    minderDelete,
  };
}
