import { markRaw } from 'vue';

import ImageView from './ImageView.vue';

import { useI18n } from '@/hooks/useI18n';

import type { ExtensionOptions, NodeBubbleMenu } from '../../types';
import { deleteNode } from './delete';
import {
  BlockActionSeparator,
  type Editor,
  EditorState,
  type EditorView,
  isActive,
  mergeAttributes,
  ToolboxItem,
  VueNodeViewRenderer,
} from '@halo-dev/richtext-editor';
import type { ImageOptions } from '@tiptap/extension-image';
import TiptapImage from '@tiptap/extension-image';

const { t } = useI18n();

const Image = TiptapImage.extend<ExtensionOptions & ImageOptions>({
  inline() {
    return true;
  },

  group() {
    return 'inline';
  },

  addAttributes() {
    return {
      ...this.parent?.(),
      width: {
        default: undefined,
        parseHTML: (element) => {
          const width = element.getAttribute('width') || element.style.width || null;
          return width;
        },
        renderHTML: (attributes) => {
          return {
            width: attributes.width,
          };
        },
      },
      height: {
        default: undefined,
        parseHTML: (element) => {
          const height = element.getAttribute('height') || element.style.height || null;
          return height;
        },
        renderHTML: (attributes) => {
          return {
            height: attributes.height,
          };
        },
      },
      href: {
        default: null,
        parseHTML: (element) => {
          const href = element.getAttribute('href') || null;
          return href;
        },
        renderHTML: (attributes) => {
          return {
            href: attributes.href,
          };
        },
      },
      style: {
        renderHTML() {
          return {
            style: 'display: inline-block;ccc:aaa',
          };
        },
      },
      // 上传成功id
      fileId: {
        default: undefined,
        parseHTML: (element) => {
          const fileId = element.getAttribute('fileId') || null;
          return fileId;
        },
        renderHTML: (attributes) => {
          return {
            fileId: attributes.fileId,
          };
        },
      },
      // 永久链接
      permalinkSrc: {
        default: undefined,
        parseHTML: (element) => {
          const permalinkSrc = element.getAttribute('permalinkSrc') || null;
          return permalinkSrc;
        },
        renderHTML: (attributes) => {
          return {
            permalinkSrc: attributes.src,
          };
        },
      },
      // 第三方平台链接, 富文本双向同步时需要
      psrc: {
        default: undefined,
        parseHTML: (element) => {
          return element.getAttribute('psrc') || null;
        },
        renderHTML: (attributes) => {
          return {
            psrc: attributes.psrc,
          };
        },
      },
    };
  },

  addNodeView() {
    return VueNodeViewRenderer(ImageView as Component);
  },

  parseHTML() {
    return [
      {
        tag: this.options.allowBase64 ? 'img[src]' : 'img[src]:not([src^="data:"])',
      },
    ];
  },

  addOptions(): any {
    return {
      ...this.parent?.(),
      getToolboxItems({ editor }: { editor: Editor }) {
        return [
          {
            priority: 10,
            component: markRaw(ToolboxItem),
            props: {
              editor,
              icon: markRaw(
                // eslint-disable-next-line vue/one-component-per-file
                defineComponent({
                  template: "<MsIcon type='icon-icon_file-image_colorful_ash' size='16' />",
                })
              ),
              title: t('editor.image'),
              action: () => {
                editor
                  .chain()
                  .focus()
                  .insertContent([{ type: 'image', attrs: { src: '' } }])
                  .run();
              },
            },
          },
        ];
      },
      getBubbleMenu({ editor }: { editor: Editor }): NodeBubbleMenu {
        return {
          pluginKey: 'imageBubbleMenu',
          shouldShow: ({ state }: { state: EditorState }): boolean => {
            return isActive(state, Image.name);
          },
          defaultAnimation: false,
          items: [
            {
              priority: 80,
              props: {
                icon: markRaw(
                  // eslint-disable-next-line vue/one-component-per-file
                  defineComponent({
                    template: "<MsIcon type='icon-icon_into-item_outlined' size='16' />",
                  })
                ),
                title: t('editor.extensions.image.open_link'),
                action: () => {
                  const newWindow = window.open('');
                  newWindow?.document.write(`<img src="${editor.getAttributes(Image.name).src}">`);
                },
              },
            },
            {
              priority: 110,
              component: markRaw(BlockActionSeparator),
            },
            {
              priority: 120,
              props: {
                icon: markRaw(
                  // eslint-disable-next-line vue/one-component-per-file
                  defineComponent({
                    template: "<MsIcon type='icon-icon_delete-trash_outlined1' size='16' />",
                  })
                ),
                title: t('common.delete'),
                action: ({ editor: editors }) => {
                  deleteNode(Image.name, editors);
                },
              },
            },
          ],
        };
      },
      getDraggable() {
        return {
          getRenderContainer({ dom, view }: { dom: Element; view: EditorView }) {
            let container = dom;
            while (container && container.tagName !== 'P') {
              container = container.parentElement as HTMLElement;
            }
            if (container) {
              container = container.firstElementChild?.firstElementChild as HTMLElement;
            }
            let node;
            if (container.firstElementChild) {
              const pos = view.posAtDOM(container.firstElementChild, 0);
              const $pos = view.state.doc.resolve(pos);
              node = $pos.node();
            }

            return {
              node,
              el: container as HTMLElement,
              dragDomOffset: {
                y: -5,
              },
            };
          },
        };
      },
    };
  },
  renderHTML({ HTMLAttributes }) {
    if (HTMLAttributes.href) {
      return ['a', { href: HTMLAttributes.href }, ['img', mergeAttributes(HTMLAttributes)]];
    }
    return ['img', mergeAttributes(HTMLAttributes)];
  },
});

export default Image;
