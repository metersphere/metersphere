/* eslint-disable no-unused-expressions */
/* eslint-disable camelcase */
/**
 * @fileOverview
 *
 * 文本输入支持
 *
 * @author: techird
 * @copyright: Baidu FEX, 2014
 */
import type { MinderJsonNode, MinderJsonNodeData } from '../../props';
import Debug from '../tool/debug';
import { isDisableNode, markChangeNode } from '../tool/utils';

if (!('innerText' in document.createElement('a')) && 'getSelection' in window) {
  Object.defineProperty(HTMLElement.prototype, 'innerText', {
    get() {
      const selection = window.getSelection();
      const ranges = [];
      let str;
      let i;
      if (selection) {
        for (i = 0; i < selection.rangeCount; i++) {
          // @ts-ignore
          ranges[i] = selection.getRangeAt(i);
        }

        selection.removeAllRanges();
        selection.selectAllChildren(this);
        str = selection.toString();
        selection.removeAllRanges();

        for (i = 0; i < ranges.length; i++) {
          selection.addRange(ranges[i]);
        }

        return str;
      }
      return '';
    },

    set(text) {
      this.innerHTML = (text || '').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/\n/g, '<br>');
    },
  });
}

const debug = new Debug('input') as any;

function InputRuntime(this: any) {
  this.receiverElement = this.receiver.element;
  this.isGecko = window.kity.Browser.gecko;

  const updatePosition = (): void => {
    let timer: null | NodeJS.Timeout = null;
    const focusNode = this.minder.getSelectedNode();
    if (!focusNode) return;

    if (!timer) {
      timer = setTimeout(() => {
        const box = focusNode.getRenderBox('TextRenderer');
        this.receiverElement.style.left = `${Math.round(box.x)}px`;
        this.receiverElement.style.top = `${debug.flagged ? Math.round(box.bottom + 30) : Math.round(box.y)}px`;
        timer = null;
      });
    }
  };

  // let the receiver follow the current selected node position
  // setup everything to go
  const setupReciverElement = () => {
    if (debug.flagged) {
      this.receiverElement.classList.add('debug');
    }

    if (this.receiverElement) {
      this.receiverElement.onmousedown = (e: any) => {
        e.stopPropagation();
      };
    }
    if (this.minder.on) {
      this.minder.on('layoutallfinish viewchange viewchanged selectionchange', (e: any) => {
        // viewchange event is too frequenced, lazy it
        if (e.type === 'viewchange' && this.fsm.state() !== 'input') return;

        updatePosition();
      });
    }
    updatePosition();
  };

  setupReciverElement();

  /**
   * 增加对字体的鉴别，以保证用户在编辑状态ctrl/cmd + b/i所触发的加粗斜体与显示一致
   * @editor Naixor
   * @Date 2015-12-2
   */
  // edit for the selected node
  const editText = (): void => {
    const node = this.minder.getSelectedNode();
    if (!node) {
      return;
    }

    markChangeNode(node);

    let textContainer = this.receiverElement;
    this.receiverElement.innerText = '';
    if (node.getData('font-weight') === 'bold') {
      const b = document.createElement('b');
      textContainer.appendChild(b);
      textContainer = b;
    }
    if (node.getData('font-style') === 'italic') {
      const i = document.createElement('i');
      textContainer.appendChild(i);
      textContainer = i;
    }
    textContainer.innerText = this.minder.queryCommandValue('text');

    if (this.isGecko) {
      this.receiver.fixFFCaretDisappeared();
    }
    this.fsm.jump('input', 'input-request');
    this.receiver.selectAll();
  };

  // expose editText()
  this.editText = editText.bind(this);

  /**
   * 增加对字体的鉴别，以保证用户在编辑状态ctrl/cmd + b/i所触发的加粗斜体与显示一致
   * @editor Naixor
   * @Date 2015-12-2
   */
  const enterInputMode = (): void => {
    const node = this.minder.getSelectedNode();
    if (node) {
      const fontSize = node.getData('font-size') || node.getStyle('font-size');
      this.receiverElement.style.fontSize = `${fontSize}px`;
      this.receiverElement.style.minWidth = '0';
      this.receiverElement.style.minWidth = `${this.receiverElement.clientWidth}px`;
      this.receiverElement.style.fontWeight = node.getData('font-weight') || '';
      this.receiverElement.style.fontStyle = node.getData('font-style') || '';
      this.receiverElement.classList.add('input');
      this.receiverElement.focus();
    }
  };

  const exitInputMode = (): void => {
    this.receiverElement.classList.remove('input');
    this.receiver.selectAll();
  };

  /**
   * 按照文本提交操作处理
   * @Desc: 从其他节点复制文字到另一个节点时部分浏览器(chrome)会自动包裹一个span标签，这样试用一下逻辑出来的就不是text节点二是span节点因此导致undefined的情况发生
   * @Warning: 下方代码使用[].slice.call来将HTMLDomCollection处理成为Array，ie8及以下会有问题
   * @Editor: Naixor
   * @Date: 2015.9.16
   */
  const commitInputText = (textNodes: any): string => {
    let text = '';
    const TAB_CHAR = '\t';
    const ENTER_CHAR = '\n';
    const STR_CHECK = /\S/;
    const SPACE_CHAR = '\u0020';
    // 针对FF,SG,BD,LB,IE等浏览器下SPACE的charCode存在为32和160的情况做处理
    const SPACE_CHAR_REGEXP = new RegExp(`(\u0020|${String.fromCharCode(160)})`);
    const BR = document.createElement('br');
    let isBold = false;
    let isItalic = false;

    // eslint-disable-next-line no-underscore-dangle, camelcase
    for (let str: any | string, _divChildNodes, space_l, i = 0, l = textNodes.length; i < l; i++) {
      str = textNodes[i];

      switch (Object.prototype.toString.call(str)) {
        // 正常情况处理
        case '[object HTMLBRElement]': {
          text += ENTER_CHAR;
          break;
        }
        case '[object Text]': {
          // SG下会莫名其妙的加上&nbsp;影响后续判断，干掉！
          /**
           * FF下的wholeText会导致如下问题：
           *     |123| -> 在一个节点中输入一段字符，此时TextNode为[#Text 123]
           *     提交并重新编辑，在后面追加几个字符
           *     |123abc| -> 此时123为一个TextNode为[#Text 123, #Text abc]，但是对这两个任意取值wholeText均为全部内容123abc
           * 上述BUG仅存在在FF中，故将wholeText更改为textContent
           */
          str = str.textContent?.replace('&nbsp;', ' ');

          if (!STR_CHECK.test(str)) {
            space_l = str.length;
            while (space_l--) {
              if (SPACE_CHAR_REGEXP.test(str[space_l])) {
                text += SPACE_CHAR;
              } else if (str[space_l] === TAB_CHAR) {
                text += TAB_CHAR;
              }
            }
          } else {
            text += str;
          }
          break;
        }
        // ctrl + b/i 会给字体加上<b>/<i>标签来实现黑体和斜体
        case '[object HTMLElement]': {
          switch (str.nodeName) {
            case 'B': {
              isBold = true;
              break;
            }
            case 'I': {
              isItalic = true;
              break;
            }
            default:
          }
          [].splice.apply(textNodes, [i, 1, ...[].slice.call(str.childNodes)]);
          l = textNodes.length;
          i--;
          break;
        }
        // 被增加span标签的情况会被处理成正常情况并会推交给上面处理
        case '[object HTMLSpanElement]': {
          [].splice.apply(textNodes, [i, 1, ...[].slice.call(str.childNodes)]);
          l = textNodes.length;
          i--;
          break;
        }
        // 若标签为image标签，则判断是否为合法url，是将其加载进来
        case '[object HTMLImageElement]': {
          if (str.src) {
            if (/http(|s):\/\//.test(str.src)) {
              this.minder.execCommand('Image', str.src, str.alt);
            } else {
              // data:image协议情况
            }
          }
          break;
        }
        // 被增加div标签的情况会被处理成正常情况并会推交给上面处理
        case '[object HTMLDivElement]': {
          _divChildNodes = [];
          for (let di = 0; di < l; di++) {
            _divChildNodes.push(str.childNodes[di]);
          }
          _divChildNodes.push(BR);
          [].splice.apply(textNodes, [i, 1, ...[].slice.call(_divChildNodes)]);
          l = textNodes.length;
          i--;
          break;
        }
        default: {
          if (str && str.childNodes.length) {
            _divChildNodes = [];
            for (let di = 0; di < l; di++) {
              _divChildNodes.push(str.childNodes[di]);
            }
            _divChildNodes.push(BR);
            [].splice.apply(textNodes, [i, 1, ...[].slice.call(_divChildNodes)]);
            l = textNodes.length;
            i--;
          } else if (str && str.textContent !== undefined) {
            text += str.textContent;
          } else {
            text += '';
          }
          // // 其他带有样式的节点被粘贴进来，则直接取textContent，若取不出来则置空
        }
      }
    }

    text = text.replace(/^\n*|\n*$/g, '').replace(/<\/?p\b[^>]*>/gi, ''); // 去除富文本内p标签
    text = text.replace(new RegExp(`(\n|\r|\n\r)(\u0020|${String.fromCharCode(160)}){4}`, 'g'), '$1\t');
    this.minder.getSelectedNode().setText(text);
    if (isBold) {
      this.minder.queryCommandState('bold') || this.minder.execCommand('bold');
    } else {
      this.minder.queryCommandState('bold') && this.minder.execCommand('bold');
    }

    if (isItalic) {
      this.minder.queryCommandState('italic') || this.minder.execCommand('italic');
    } else {
      this.minder.queryCommandState('italic') && this.minder.execCommand('italic');
    }
    exitInputMode();
    return text;
  };

  /**
   * 判断节点的文本信息是否是
   * @Desc: 从其他节点复制文字到另一个节点时部分浏览器(chrome)会自动包裹一个span标签，这样使用以下逻辑出来的就不是text节点二是span节点因此导致undefined的情况发生
   * @Notice: 此处逻辑应该拆分到 kityminder-core/core/data中去，单独增加一个对某个节点importJson的事件
   * @Editor: Naixor
   * @Date: 2015.9.16
   */
  const commitInputNode = (node: MinderJsonNode, text: string) => {
    try {
      this.minder.decodeData('text', text).then((json: MinderJsonNodeData) => {
        function importText(nodeT: MinderJsonNode, jsonT: MinderJsonNodeData, minder: any) {
          const { data } = jsonT;
          nodeT.setText(data.text || '');
          const childrenTreeData = jsonT.children || [];
          for (let i = 0; i < childrenTreeData.length; i++) {
            const childNode = minder.createNode(null, nodeT);
            importText(childNode, childrenTreeData[i], minder);
          }
          return nodeT;
        }
        importText(node, json, this.minder);
        this.minder.fire('contentchange');
        if (node.parent) {
          node.parent.renderTree();
          node.parent.layout();
        } else {
          this.minder.getRoot().renderTree();
          this.minder.layout(300);
        }
      });
    } catch (e: any) {
      this.minder.fire('contentchange');
      this.minder.getRoot().renderTree();
      // 无法被转换成脑图节点则不处理
      if (e.toString() !== 'Error: Invalid local format') {
        throw e;
      }
    }
  };

  const commitInputResult = () => {
    /**
     * @desc 进行如下处理：
     *       根据用户的输入判断是否生成新的节点
     *       fix #83 https://github.com/fex-team/kityminder-editor/issues/83
     * @editor Naixor
     * @date 2015.9.16
     */
    const textNodes = Array.from(this.receiverElement.childNodes);

    /**
     * @desc 增加setTimeout的原因：ie下receiverElement.innerHTML=""会导致后
     *       面commitInputText中使用textContent报错，不要问我什么原因！
     * @editor Naixor
     * @date 2015.12.14
     */
    setTimeout(() => {
      // 解决过大内容导致SVG窜位问题
      this.receiverElement.innerHTML = '';
    }, 0);

    const node = this.minder.getSelectedNode();
    const processedTextNodes = commitInputText(textNodes as any);

    commitInputNode(node, processedTextNodes as any);

    if (node.type === 'root') {
      const rootText = this.minder.getRoot().getText();
      this.minder.fire('initChangeRoot', {
        text: rootText,
      });
    }
  };
  // listen the fsm changes, make action.
  const setupFsm = () => {
    // when jumped to input mode, enter
    this.fsm.when('* -> input', enterInputMode.bind(this));

    // when exited, commit or exit depends on the exit reason
    this.fsm.when('input -> *', (exit: any, enter: any, reason: string) => {
      switch (reason) {
        case 'input-cancel':
          return exitInputMode();
        case 'input-commit':
        default:
          return commitInputResult();
      }
    });

    // lost focus to commit
    this.receiver.onblur(() => {
      if (this.fsm.state() === 'input') {
        this.fsm.jump('normal', 'input-commit');
      }
    });

    this.minder.on('beforemousedown', () => {
      if (this.fsm.state() === 'input') {
        this.fsm.jump('normal', 'input-commit');
      }
    });

    this.minder.on('dblclick', () => {
      // eslint-disable-next-line no-underscore-dangle
      if (this.minder.getSelectedNode() && this.minder._status !== 'readonly' && !isDisableNode(this.minder)) {
        this.editText();
      }
    });
  };

  setupFsm();
}

export default InputRuntime;
