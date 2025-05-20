import { useClipboard } from '@vueuse/core';
import { Message } from '@arco-design/web-vue';

import { useI18n } from '@/hooks/useI18n';
import useMinderStore from '@/store/modules/components/minder-editor';
import { getGenerateId } from '@/utils';

import { MinderEventName } from '@/enums/minderEnum';

import type { MinderJsonNode } from '../props';
import { markDeleteNode, resetNodes, setPriorityView } from '../script/tool/utils';

interface IData {
  getRegisterProtocol(protocol: string): {
    encode: (nodes: Array<MinderJsonNode>) => Array<MinderJsonNode>;
    decode: (nodes: Array<MinderJsonNode>) => Array<MinderJsonNode>;
  };
}

export interface MinderOperationProps {
  insertNode?: (node: MinderJsonNode, command: string, value?: string) => void;
  customBatchExpand?: (node: MinderJsonNode) => void;
  disabled?: boolean;
  canShowMoreMenu?: boolean;
  canShowMoreMenuNodeOperation?: boolean;
  canShowPasteMenu?: boolean;
  canShowDeleteMenu?: boolean;
  customPriority?: boolean;
  priorityStartWithZero?: boolean;
  priorityPrefix?: string;
  canShowBatchCut?: boolean;
  canShowBatchCopy?: boolean;
  canShowBatchDelete?: boolean;
}

let isCopyFinished = true;

export default function useMinderOperation(options: MinderOperationProps) {
  const minderStore = useMinderStore();
  const { t } = useI18n();

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
  const minderCopy = async (e?: ClipboardEvent) => {
    if (!isCopyFinished) {
      return;
    }
    isCopyFinished = false;
    const { editor } = window;
    const { minder, fsm } = editor;
    const selectedNodes: MinderJsonNode[] = minder.getSelectedNodes();
    const state = fsm.state();
    switch (state) {
      case 'input': {
        break;
      }
      case 'normal': {
        if (
          !options.canShowMoreMenu ||
          !options.canShowMoreMenuNodeOperation ||
          (selectedNodes.length > 1 && options.canShowBatchCopy === false)
        ) {
          e?.preventDefault();
          return;
        }
        minderStore.dispatchEvent(MinderEventName.COPY_NODE, undefined, undefined, undefined, selectedNodes);
        if (e?.clipboardData) {
          e.clipboardData.setData('text/plain', encode(selectedNodes));
        } else {
          const { copy } = useClipboard();
          await copy(encode(selectedNodes));
        }
        minder.execCommand('Copy');
        e?.preventDefault();
        Message.success(t('common.copySuccess'));
        setTimeout(() => {
          isCopyFinished = true;
        }, 0);
        break;
      }
      default:
    }
  };

  /**
   * 执行剪切
   */
  const minderCut = async (e?: ClipboardEvent) => {
    const { editor } = window;
    const { minder, fsm } = editor;
    const selectedNodes: MinderJsonNode[] = minder.getSelectedNodes();
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
        if (
          options.disabled ||
          !options.canShowMoreMenu ||
          !options.canShowMoreMenuNodeOperation ||
          (selectedNodes.length > 1 && options.canShowBatchCut === false)
        ) {
          e?.preventDefault();
          return;
        }
        markDeleteNode(minder);
        if (selectedNodes.length) {
          const newNodes = selectedNodes.map((node) => ({
            ...node,
            parent: node.parent, // 保存父节点信息，因为剪切节点后 parent 会被置空
          }));
          minderStore.dispatchEvent(MinderEventName.CUT_NODE, undefined, undefined, undefined, newNodes);
          if (e?.clipboardData) {
            e.clipboardData.setData('text/plain', encode(selectedNodes));
          } else {
            const { copy } = useClipboard();
            await copy(encode(selectedNodes));
          }
          minder.execCommand('Cut');
          e?.preventDefault();
          Message.success(t('common.cutSuccess'));
        }
        break;
      }
      default:
    }
  };

  /**
   * 执行粘贴
   */
  const minderPaste = async (e?: ClipboardEvent) => {
    const { editor } = window;
    const { minder, fsm, MimeType } = editor;
    const Data: IData = window.kityminder.data;
    const { decode } = Data.getRegisterProtocol('json');
    if (minder.getStatus() !== 'normal') {
      e?.preventDefault();
      return;
    }
    const state = fsm.state();
    if (!navigator.clipboard && !e?.clipboardData) {
      // 不支持剪贴板操作
      Message.warning(t('minder.notSupportClipboard'));
      return;
    }
    const textData = e?.clipboardData ? e.clipboardData.getData('text/plain') : await navigator.clipboard.readText();
    switch (state) {
      case 'input': {
        // input状态下如果格式为application/km则不进行paste操作
        if (!MimeType.isPureText(textData)) {
          e?.preventDefault();
        }
        break;
      }
      case 'normal': {
        if (options.disabled || !options.canShowPasteMenu) {
          e?.preventDefault();
          return;
        }
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
              _node.setData({ isNew: true });
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
  const execInsertCommand = (command: string, selectedNodes: MinderJsonNode[], value?: string) => {
    if (selectedNodes.length > 1) {
      return; // TODO: 批量操作暂不支持插入
    }
    if (options.insertNode) {
      options.insertNode(selectedNodes[0], command, value);
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
    execInsertCommand('AppendChildNode', selectedNodes, value);
    minderStore.dispatchEvent(MinderEventName.INSERT_CHILD, value, undefined, undefined, selectedNodes);
  };

  /**
   * 插入兄弟节点
   * @param selectedNodes 当前选中的节点集合
   * @param value 携带的参数
   */
  const appendSiblingNode = (selectedNodes: MinderJsonNode[], value?: string) => {
    execInsertCommand('AppendSiblingNode', selectedNodes, value);
    minderStore.dispatchEvent(MinderEventName.INSERT_SIBLING, value, undefined, undefined, selectedNodes);
  };

  /**
   * 脑图展开
   * @param selectedNodes 当前选中的节点集合
   */
  const minderExpand = (selectedNodes: MinderJsonNode[]) => {
    if (selectedNodes.every((node) => node.isExpanded())) {
      // 选中的节点集合全部展开，则全部收起
      selectedNodes.forEach((node) => {
        node.collapse();
        node.renderTree();
      });
      if (!options.customPriority) {
        // 展开后，需要设置一次优先级展示，避免展开后优先级显示成脑图内置文案；如果设置了自定义优先级，则不在此设置，由外部自行处理
        setPriorityView(!!options.priorityStartWithZero, options.priorityPrefix || '');
      }
      window.minder.refresh();
      minderStore.dispatchEvent(MinderEventName.COLLAPSE, undefined, undefined, undefined, selectedNodes);
    } else {
      // 选中的节点集合中有一个节点未展开，则全部展开
      selectedNodes.forEach((node) => {
        if (selectedNodes.length > 1 && options.customBatchExpand) {
          // 批量操作节点才执行customBatchExpand
          options.customBatchExpand(node);
        } else {
          node.expand();
          node.renderTree();
        }
      });
      if (!options.customPriority) {
        // 展开后，需要设置一次优先级展示，避免展开后优先级显示成脑图内置文案；如果设置了自定义优先级，则不在此设置，由外部自行处理
        setPriorityView(!!options.priorityStartWithZero, options.priorityPrefix || '');
      }
      window.minder.refresh();
      minderStore.dispatchEvent(MinderEventName.EXPAND, undefined, undefined, undefined, selectedNodes);
    }
  };

  /**
   * 删除节点
   * @param selectedNodes 当前选中的节点集合
   */
  const minderDelete = (selectedNodes: MinderJsonNode[]) => {
    if (selectedNodes.length > 1 && (!options.canShowBatchDelete || options.disabled)) {
      // 批量操作节点时，如果不允许批量删除或者当前操作被禁用，则不执行删除操作
      return;
    }
    if (
      selectedNodes.length >= 1 &&
      (options.canShowDeleteMenu || (options.canShowMoreMenu && options.canShowMoreMenuNodeOperation)) &&
      !options.disabled
    ) {
      // 如果允许删除操作，则执行删除操作
      const newNodes = selectedNodes.map((node) => ({
        ...node,
        parent: node.parent, // 保存父节点信息，因为删除节点后 parent 会被置空
      }));
      minderStore.dispatchEvent(MinderEventName.DELETE_NODE, undefined, undefined, undefined, newNodes);
      window.minder.execCommand('RemoveNode');
    }
  };

  return {
    minderCopy,
    minderCut,
    minderPaste,
    appendChildNode,
    appendSiblingNode,
    minderDelete,
    minderExpand,
  };
}
