<script lang="ts" setup>
  /**
   *
   * @name: MsRichText.vue
   * @param {string} raw v-model绑定的值
   * @return {string} 返回编辑器内容
   * @description: 富文本编辑器
   * @example:
   * import { unified } from 'unified';
   * import rehypeParse from 'rehype-parse';
   * import rehypeFormat from 'rehype-format';
   * import rehypeStringify from 'rehype-stringify';
   * return unified().use(rehypeParse).use(rehypeFormat).use(rehypeStringify).processSync(content.value);
   */
  import { useDebounceFn, useLocalStorage } from '@vueuse/core';

  import type { MsFileItem } from '@/components/pure/ms-upload/types';
  import AttachmentSelectorModal from './attachmentSelectorModal.vue';

  import { editorUploadFile } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useLocale from '@/locale/useLocale';

  import '@halo-dev/richtext-editor/dist/style.css';
  import ExtensionImage from './extensions/image/index';
  import suggestion from './extensions/mention/suggestion';
  import {
    type AnyExtension,
    Editor,
    Extension,
    ExtensionAudio,
    ExtensionBlockquote,
    ExtensionBold,
    ExtensionBulletList,
    ExtensionCode,
    ExtensionCodeBlock,
    ExtensionColor,
    ExtensionColumn,
    ExtensionColumns,
    ExtensionCommands,
    ExtensionDocument,
    ExtensionDraggable,
    ExtensionDropcursor,
    ExtensionFontSize,
    ExtensionGapcursor,
    ExtensionHardBreak,
    ExtensionHeading,
    ExtensionHighlight,
    ExtensionHistory,
    ExtensionHorizontalRule,
    ExtensionIframe,
    ExtensionIndent,
    ExtensionItalic,
    ExtensionLink,
    ExtensionNodeSelected,
    ExtensionOrderedList,
    ExtensionPlaceholder,
    ExtensionStrike,
    ExtensionSubscript,
    ExtensionSuperscript,
    ExtensionTable,
    ExtensionTaskList,
    ExtensionText,
    ExtensionTextAlign,
    ExtensionTrailingNode,
    ExtensionUnderline,
    ExtensionVideo,
    lowlight,
    RichTextEditor,
    ToolbarItem,
    ToolboxItem,
  } from '@halo-dev/richtext-editor';
  import Mention from '@tiptap/extension-mention';
  import type { queueAsPromised } from 'fastq';
  import * as fastq from 'fastq';

  const { t } = useI18n();

  // image drag and paste upload
  type Task = {
    file: File;
    process: (compressUrl: string, permalink: string, fileId: string) => void;
  };

  const props = withDefaults(
    defineProps<{
      raw?: string;
      uploadImage?: (file: File) => Promise<any>;
      maxHeight?: string;
    }>(),
    {
      raw: '',
      uploadImage: undefined,
    }
  );

  const editor = shallowRef<Editor>();

  const emit = defineEmits<{
    (event: 'update:raw', value: string): void;
    (event: 'update', value: string): void;
  }>();

  /**
   * 图片压缩
   * @param {*} img 图片对象
   * @param {*} type 图片类型
   * @param {*} maxWidth 图片最大宽度
   * @param {*} flag
   */

  function compress(img, type, maxWidth, flag) {
    let canvas: HTMLCanvasElement | null = document.createElement('canvas');
    let ctx2: any = canvas.getContext('2d');

    const ratio = img.width / img.height;
    let { width } = img;
    let { height } = img;
    // 根据flag判断是否压缩图片
    if (flag && maxWidth <= width) {
      width = maxWidth;
      height = maxWidth / ratio; // 维持图片宽高比
    }
    canvas.width = width;
    canvas.height = height;

    ctx2.fillStyle = '#fff';
    ctx2.fillRect(0, 0, canvas.width, canvas.height);
    ctx2.drawImage(img, 0, 0, width, height);

    let base64Data = canvas.toDataURL(type, 0.75);

    if (type === 'image/gif') {
      const regx = /(?<=data:image).*?(?=;base64)/; // 正则表示时在用于replace时，根据浏览器的不同，有的需要为字符串
      base64Data = base64Data.replace(regx, '/gif');
    }
    canvas = null;
    ctx2 = null;
    return base64Data;
  }

  function handleFile(file: File, callback: any, maxWidth = 600) {
    if (!file || !/\/(?:png|jpg|jpeg|gif)/i.test(file.type)) {
      return;
    }
    const reader = new FileReader();
    // eslint-disable-next-line func-names
    reader.onload = function () {
      const { result } = this;
      let img: HTMLImageElement | null = new Image();
      img.onload = () => {
        const compressedDataUrl = compress(img, file.type, maxWidth, true);
        const url = compress(img, file.type, maxWidth, false);
        img = null;
        callback({
          data: file,
          compressedDataUrl,
          url,
          type: 'image',
        });
      };
      img.src = result as any;
    };
    reader.readAsDataURL(file);
  }

  function onPaste(file: File) {
    return new Promise((resovle, reject) => {
      handleFile(file, (data) => {
        resovle(data);
      });
    });
  }

  const imageMap = {};

  async function asyncWorker(arg: Task): Promise<void> {
    if (!props.uploadImage) {
      return;
    }

    const uploadFileId = await props.uploadImage(arg.file);
    const result: any = await onPaste(arg.file);
    // 如果上传成功
    if (uploadFileId) {
      // eslint-disable-next-line no-prototype-builtins
      if (!imageMap.hasOwnProperty(uploadFileId)) {
        imageMap[uploadFileId] = {
          compressedUrl: result.compressedDataUrl,
          permanentUrl: '',
          fileId: uploadFileId,
        };
      }
      arg.process(result.compressedDataUrl, imageMap[uploadFileId].permanentUrl, uploadFileId);
    }
  }

  const uploadQueue: queueAsPromised<Task> = fastq.promise(asyncWorker, 1);

  const { currentLocale } = useLocale();

  watch(
    () => props.raw,
    () => {
      if (props.raw !== editor.value?.getHTML()) {
        editor.value?.commands.setContent(props.raw);
      }
    },
    {
      immediate: true,
    }
  );

  const showSidebar = useLocalStorage('halo:editor:show-sidebar', true);

  const attachmentSelectorModal = ref(false);

  onMounted(() => {
    const debounceOnUpdate = useDebounceFn(() => {
      const html = `${editor.value?.getHTML()}`;
      emit('update:raw', html);
      emit('update', html);
    }, 250);

    editor.value = new Editor({
      content: props.raw,
      extensions: [
        ExtensionBlockquote,
        ExtensionBold,
        ExtensionBulletList,
        ExtensionCode,
        ExtensionDocument,
        ExtensionDropcursor.configure({
          width: 2,
          class: 'dropcursor',
          color: 'skyblue',
        }),
        ExtensionCommands,
        ExtensionGapcursor,
        ExtensionHardBreak,
        ExtensionHeading,
        ExtensionHistory,
        ExtensionHorizontalRule,
        ExtensionItalic,
        ExtensionOrderedList,
        ExtensionStrike,
        ExtensionText,
        ExtensionImage.configure({
          inline: true,
          allowBase64: true,
          HTMLAttributes: {
            loading: 'lazy',
          },
        }),
        ExtensionTaskList,
        ExtensionLink.configure({
          autolink: false,
          openOnClick: false,
        }),
        ExtensionTextAlign.configure({
          types: ['heading', 'paragraph'],
        }),
        ExtensionUnderline,
        ExtensionTable.configure({
          resizable: true,
        }),
        ExtensionSubscript,
        ExtensionSuperscript,
        ExtensionPlaceholder.configure({
          placeholder: '输入 / 以选择输入类型',
        }),
        ExtensionHighlight,
        ExtensionVideo,
        ExtensionAudio,
        ExtensionCodeBlock.configure({
          lowlight,
        }),
        ExtensionIframe,
        ExtensionColor,
        ExtensionFontSize,
        ExtensionIndent,
        Extension.create({
          addGlobalAttributes() {
            return [
              {
                types: ['heading'],
                attributes: {
                  id: {
                    default: null,
                  },
                },
              },
            ];
          },
        }),
        Extension.create({
          addOptions() {
            return {
              getToolboxItems({ editors }: { editors: Editor }) {
                return [
                  {
                    priority: 0,
                    component: markRaw(ToolboxItem),
                    props: {
                      editor,
                      icon: markRaw(
                        defineComponent({
                          template: "<MsIcon type='icon-icon_link-copy_outlined' size='16' />",
                        })
                      ),
                      title: t('editor.attachment'),
                      action: () => {
                        attachmentSelectorModal.value = true;
                      },
                    },
                  },
                ];
              },
              getToolbarItems({ editors }: { editors: Editor }) {
                return {
                  priority: 1000,
                  component: markRaw(ToolbarItem),
                  props: {
                    editor,
                    isActive: showSidebar.value,
                    title: t(''),
                    action: () => {
                      showSidebar.value = !showSidebar.value;
                    },
                  },
                };
              },
            };
          },
        }),
        ExtensionDraggable,
        ExtensionColumns,
        ExtensionColumn,
        ExtensionNodeSelected,
        ExtensionTrailingNode,
        Mention.configure({
          HTMLAttributes: {
            class: 'mention',
          },
          // TODO第一版本先按照初始化评论的人 不加userMap
          renderLabel({ options, node }) {
            return `${options.suggestion.char}${node.attrs.label ?? node.attrs.id}`;
            // return `${options.suggestion.char}${userMap[node.attrs.id]}`;
          },
          suggestion,
        }),
      ],
      autofocus: 'start',
      onUpdate: () => {
        debounceOnUpdate();
      },
      editorProps: {
        handleDrop: (view, event: DragEvent, _, moved) => {
          if (!moved && event.dataTransfer && event.dataTransfer.files) {
            const images = Array.from(event.dataTransfer.files).filter((file) =>
              file.type.startsWith('image/')
            ) as File[];

            if (images.length === 0) {
              return;
            }

            event.preventDefault();

            images.forEach((file, index) => {
              uploadQueue.push({
                file,
                process: (url: string) => {
                  const { schema } = view.state;
                  const coordinates = view.posAtCoords({
                    left: event.clientX,
                    top: event.clientY,
                  });

                  if (!coordinates) return;

                  const node = schema.nodes.image.create({
                    src: url,
                  });

                  const transaction = view.state.tr.insert(coordinates.pos + index, node);

                  editor.value?.view.dispatch(transaction);
                },
              });
            });

            return true;
          }
          return false;
        },
        handlePaste: (view, event: ClipboardEvent) => {
          const types = Array.from(event.clipboardData?.types || []);

          if (['text/plain', 'text/html'].includes(types[0])) {
            return;
          }

          const images = Array.from(event.clipboardData?.items || [])
            .map((item) => {
              return item.getAsFile();
            })
            .filter((file) => {
              return file && file.type.startsWith('image/');
            }) as File[];

          if (images.length === 0) {
            return;
          }

          event.preventDefault();

          images.forEach((file) => {
            uploadQueue.push({
              file,
              // 压缩url  永久url  文件id
              process: (compressUrl: string, permalink: string, fileId: string) => {
                editor.value
                  ?.chain()
                  .focus()
                  .insertContent([
                    {
                      type: 'image',
                      attrs: {
                        fileId,
                        src: compressUrl,
                        permalinkSrc: permalink,
                      },
                    },
                  ])
                  .run();
              },
            });
          });
        },
      },
    });
  });

  onBeforeUnmount(() => {
    editor.value?.destroy();
  });

  const contentStyles = computed(() => {
    return {
      maxHeight: props.maxHeight || '200px',
      overflow: 'auto',
    };
  });

  // 模拟附件最终插入到富文本TODO 预览接口更新后替换为真实的url
  function getContents(fileList: { id: string; file: File }[]) {
    const contents = fileList
      // eslint-disable-next-line array-callback-return
      .map((item: any) => {
        const { file } = item.file;
        if (file.type.includes('image')) {
          return {
            type: 'image',
            attrs: {
              src: 'https://demo.halo.run/upload/image-xakz.png',
            },
          };
        }
        if (file.type.includes('text/html')) {
          return {
            type: 'text',
            marks: [
              {
                type: 'link',
                attrs: {
                  href: 'https://demo.halo.run/console/posts/editor?name=1210fe82-1d93-4aab-a3a0-9c9820fd4981',
                  fileId: item.id,
                },
              },
            ],
            text: file.name,
          };
        }
      })
      .filter(Boolean);
    editor.value?.chain().focus().insertContent(contents).run();
    attachmentSelectorModal.value = false;
  }

  async function onAttachmentSelect(fileList: MsFileItem[]) {
    try {
      const upFileFileIds: { id: string; file: File }[] = [];

      const uploadPromises = fileList.map(async (item: any) => {
        const fileId = await editorUploadFile({
          fileList: [item.file],
        });
        if (fileId) {
          upFileFileIds.push({
            id: fileId,
            file: item,
          });
        }
      });

      await Promise.all(uploadPromises);

      await getContents(upFileFileIds);
    } catch (error) {
      console.log(error);
    }
  }
</script>

<template>
  <div class="rich-wrapper flex w-full">
    <AttachmentSelectorModal v-model:visible="attachmentSelectorModal" @select="onAttachmentSelect" />
    <RichTextEditor v-if="editor" :editor="editor" :content-styles="contentStyles" :locale="currentLocale" />
  </div>
</template>

<style scoped lang="less">
  .rich-wrapper {
    border: 1px solid var(--color-text-n8);
    border-radius: var(--border-radius-small);
    @apply relative overflow-hidden;
    :deep(.halo-rich-text-editor .ProseMirror) {
      padding: 16px 24px !important;
      p:first-child {
        margin-top: 0;
      }
    }
  }
  :deep(.editor-header) {
    svg {
      color: var(--color-text-3) !important;
    }
  }
  :deep(.editor-content) {
    .ms-scroll-bar();
  }
</style>

<style lang="less">
  .v-popper__popper {
    .v-popper__inner {
      .drop-shadow {
        .ms-scroll-bar();
      }
    }
  }
  .tippy-box {
    .command-items {
      .ms-scroll-bar();
    }
  }
</style>
