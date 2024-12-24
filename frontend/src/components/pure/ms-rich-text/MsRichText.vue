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
  import { useDebounceFn, useVModel } from '@vueuse/core';

  import type { MsFileItem } from '@/components/pure/ms-upload/types';
  import AttachmentSelectorModal from './attachmentSelectorModal.vue';

  import { editorUploadFile } from '@/api/modules/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useLocale from '@/locale/useLocale';

  import '@halo-dev/richtext-editor/dist/style.css';
  import ExtensionImage from './extensions/image/index';
  import suggestion from './extensions/mention/suggestion';
  import {
    DecorationSet,
    Editor,
    Extension,
    ExtensionBlockquote,
    ExtensionBold,
    ExtensionBulletList,
    ExtensionCode,
    ExtensionCodeBlock,
    ExtensionColor,
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
    lowlight,
    Plugin,
    PluginKey,
    RichTextEditor,
  } from '@halo-dev/richtext-editor';
  import CharacterCount from '@tiptap/extension-character-count';
  import Mention from '@tiptap/extension-mention';
  import type { queueAsPromised } from 'fastq';
  import * as fastq from 'fastq';

  const { t } = useI18n();
  type Task = {
    file: File;
    process: (permalink: string, fileId: string) => void;
  };

  const props = withDefaults(
    defineProps<{
      raw?: string;
      uploadImage?: (file: File) => Promise<any>;
      maxHeight?: string;
      autoHeight?: boolean;
      filedIds?: string[];
      commentIds?: string[];
      wrapperClass?: string;
      placeholder?: string;
      draggable?: boolean;
      previewUrl?: string;
      editable?: boolean;
      limitLength?: number;
      autoFocus?: boolean;
    }>(),
    {
      raw: '',
      uploadImage: undefined,
      placeholder: 'editor.placeholder',
      draggable: false,
      autoHeight: true,
      editable: true,
    }
  );

  const editor = shallowRef<Editor>();

  const emit = defineEmits<{
    (event: 'update:raw', value: string): void;
    (event: 'update:filedIds', value: string[]): void;
    (event: 'update', value: string): void;
    (event: 'update:commentIds', value: string): void;
    (event: 'blur', eveValue: FocusEvent): void;
    (event: 'focus', eveValue: FocusEvent): void;
  }>();

  const imagesNodesIds = useVModel(props, 'filedIds', emit);
  const commentNodeIds = useVModel(props, 'commentIds', emit);

  async function asyncWorker(arg: Task): Promise<void> {
    if (!props.uploadImage) {
      return;
    }
    const uploadFileId = await props.uploadImage(arg.file);
    if (uploadFileId) {
      const permanentUrl = `${props.previewUrl}/${uploadFileId}/${true}`;
      arg.process(permanentUrl, uploadFileId);
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
    }
  );

  watch(
    () => props.editable,
    (val) => {
      // 更新富文本的可编辑配置
      editor.value?.setOptions({ editable: val });
    },
    {
      immediate: true,
    }
  );

  watch(
    () => props.autoFocus,
    (val) => {
      editor.value?.setOptions({ autofocus: val });
      if (val) {
        editor.value?.chain().focus();
      }
    },
    {
      immediate: true,
    }
  );

  const attachmentSelectorModal = ref(false);
  const selectedImagesNode = ref<string>();
  const selectedCommentNode = ref<string>();

  // TODO deletedImagesIds 为解决后台富文本附件删除图片时，后台服务器也删除，目前不确定这个版本要不要替换，等后台协调
  const previousImages = ref<string[]>([]);
  const deletedImagesIds = ref<string[]>([]);
  const addedImages = ref(new Set());
  const removedImages = ref(new Set());

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
        // ExtensionOrderedList,
        ExtensionOrderedList.configure({
          HTMLAttributes: {
            class: 'my-custom-class',
          },
        }),
        ExtensionStrike,
        ExtensionText,
        ExtensionImage.configure({
          inline: true,
          allowBase64: false,
          HTMLAttributes: {
            loading: 'lazy',
          },
        }),
        Extension.create({
          addProseMirrorPlugins() {
            return [
              new Plugin({
                key: new PluginKey('imageBubbleMenu'),
                props: {
                  decorations: (state) => {
                    const images: string[] = [];
                    const { doc } = state;
                    doc.descendants((node) => {
                      if (node.type.name === 'image') {
                        images.push(node.attrs.fileId);
                      }
                    });
                    imagesNodesIds.value = images;
                    if (!selectedImagesNode.value) {
                      // eslint-disable-next-line prefer-destructuring
                      selectedImagesNode.value = images[0];
                    }

                    const currentImagesSet = new Set(images);

                    // 检查被删除的图片
                    const deletedImages = previousImages.value.filter((fileId) => !currentImagesSet.has(fileId));
                    deletedImages.forEach((id) => {
                      if (!deletedImagesIds.value.includes(id)) {
                        deletedImagesIds.value.push(id);
                        removedImages.value.add(id);
                      }
                    });

                    // 检查被添加的图片（包含撤销删除则从删除里边移除掉）
                    const addedImagesNow = (images || []).filter((fileId) => !previousImages.value.includes(fileId));
                    addedImagesNow.forEach((id: string) => {
                      if (deletedImagesIds.value.includes(id)) {
                        const index = deletedImagesIds.value.indexOf(id);
                        if (index > -1) {
                          deletedImagesIds.value.splice(index, 1);
                          removedImages.value.delete(id);
                        }
                      }
                      addedImages.value.add(id);
                    });

                    previousImages.value = images;

                    return DecorationSet.empty;
                  },
                },
              }),
            ];
          },
        }),
        Extension.create({
          addProseMirrorPlugins() {
            return [
              new Plugin({
                key: new PluginKey(Mention.name),
                props: {
                  decorations: (state) => {
                    const commentUsers: string[] = [];
                    const { doc } = state;
                    doc.descendants((node) => {
                      if (node.type.name === 'mention') {
                        commentUsers.push(node.attrs.id);
                      }
                    });
                    commentNodeIds.value = commentUsers;
                    if (!selectedCommentNode.value) {
                      // eslint-disable-next-line prefer-destructuring
                      selectedCommentNode.value = commentUsers[0];
                    }
                    return DecorationSet.empty;
                  },
                },
              }),
            ];
          },
        }),
        ExtensionTaskList,
        ExtensionLink.configure({
          autolink: false,
          openOnClick: false,
          linkOnPaste: true,
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
          placeholder: t(props.placeholder),
        }),
        ExtensionHighlight,
        // ExtensionVideo,
        // ExtensionAudio,
        ExtensionCodeBlock.configure({
          lowlight,
        }),
        // ExtensionIframe,
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
              // getToolboxItems({ editors }: { editors: Editor }) {
              //   return [
              //     {
              //       priority: 0,
              //       component: markRaw(ToolboxItem),
              //       props: {
              //         editor,
              //         icon: markRaw(
              //           defineComponent({
              //             template: "<MsIcon type='icon-icon_link-copy_outlined' size='16' />",
              //           })
              //         ),
              //         title: t('editor.attachment'),
              //         action: () => {
              //           attachmentSelectorModal.value = true;
              //         },
              //       },
              //     },
              //   ];
              // },
              // getToolbarItems({ editors }: { editors: Editor }) {
              //   return {
              //     priority: 1000,
              //     component: markRaw(ToolbarItem),
              //     props: {
              //       editor,
              //       isActive: showSidebar.value,
              //       title: t(''),
              //       action: () => {
              //         showSidebar.value = !showSidebar.value;
              //       },
              //     },
              //   };
              // },
            };
          },
        }),
        ExtensionDraggable,
        // ExtensionColumns,
        // ExtensionColumn,
        ExtensionNodeSelected,
        ExtensionTrailingNode,
        Mention.configure({
          HTMLAttributes: {
            class: 'mention',
          },
          // @ts-ignore
          renderHTML({ options, node }) {
            return [
              'span',
              { class: 'mention-people' },
              `${options.suggestion.char}${node.attrs.label ?? node.attrs.id}`,
            ];
            // return `${options.suggestion.char}${userMap[node.attrs.id]}`;
          },
          suggestion,
        }) as Extension<any, any>,
        CharacterCount.configure({
          limit: props.limitLength || null,
        }) as Extension,
      ],
      autofocus: props.autoFocus,
      editable: props.editable,
      onUpdate: () => {
        debounceOnUpdate();
      },
      onBlur: ({ event }) => {
        // 避免移动到菜单上触发失去焦点切换视图
        if (!event.relatedTarget) {
          emit('blur', event);
        }
      },
      onFocus: ({ event }) => {
        emit('focus', event);
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
              process: (permalink: string, fileId: string) => {
                editor.value
                  ?.chain()
                  .focus()
                  .insertContent([
                    {
                      type: 'image',
                      attrs: {
                        src: permalink,
                        fileId,
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

  function focus() {
    editor.value?.chain().focus();
  }

  const contentStyles = computed(() => {
    return {
      maxHeight: props.autoHeight ? '' : props.maxHeight || '260px',
      overflow: props.autoHeight ? 'hidden' : 'auto',
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

  defineExpose({
    focus,
  });
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
    &:hover {
      border-color: rgb(var(--primary-5));
    }
    @apply relative overflow-hidden;
    :deep(.halo-rich-text-editor .ProseMirror) {
      padding: 16px !important;
      padding-top: 30px !important;
      min-height: 130px;
      > p:first-child {
        margin-top: 0;
      }
    }
    :deep(.halo-rich-text-editor) {
      padding: 2px 16px 16px !important;
      .editor-header {
        .ms-scroll-bar();

        justify-content: start !important;
        border-color: var(--color-text-n8);
        background-color: var(--color-text-fff);
      }
      > p:first-child {
        margin-top: 0;
      }
    }
  }
  :deep(.halo-rich-text-editor .ProseMirror) {
    word-break: break-word;
  }
  :deep(.halo-rich-text-editor .ProseMirror + .draggable) {
    display: none !important;
  }
  :deep(.editor-header) {
    svg {
      color: var(--color-text-3) !important;
    }
    & + div {
      div:first-of-type {
        .ms-scroll-bar();
      }
      & > div {
        overflow: visible;
        .bubble-menu button {
          padding: 3px;
        }
      }
    }
  }
  :deep(.editor-content) {
    .ms-scroll-bar();

    background: var(--color-text-fff);
  }
  :deep(.tableWrapper) {
    .ms-scroll-bar();
    table {
      tr,
      th {
        background: var(--color-text-fff) !important;
      }
      tr {
        th,
        td {
          padding: 8px;
        }
        p {
          margin-top: 0 !important;
        }
      }
    }
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
  .mention-people {
    color: rgb(var(--primary-5)) !important;
  }
</style>
