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
  import { useDebounceFn, useLocalStorage, useVModel } from '@vueuse/core';

  import type { MsFileItem } from '@/components/pure/ms-upload/types';
  import AttachmentSelectorModal from './attachmentSelectorModal.vue';

  import { editorUploadFile } from '@/api/modules/case-management/featureCase';
  import { PreviewEditorImageUrl } from '@/api/requrls/case-management/featureCase';
  import { useI18n } from '@/hooks/useI18n';
  import useLocale from '@/locale/useLocale';
  import { useAppStore } from '@/store';

  import '@halo-dev/richtext-editor/dist/style.css';
  import ExtensionImage from './extensions/image/index';
  import suggestion from './extensions/mention/suggestion';
  import {
    DecorationSet,
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
    Plugin,
    PluginKey,
    RichTextEditor,
    ToolbarItem,
    ToolboxItem,
  } from '@halo-dev/richtext-editor';
  import Mention from '@tiptap/extension-mention';
  import type { queueAsPromised } from 'fastq';
  import * as fastq from 'fastq';

  const appStore = useAppStore();
  const { t } = useI18n();

  // image drag and paste upload
  type Task = {
    file: File;
    process: (permalink: string, fileId: string) => void;
  };

  const props = withDefaults(
    defineProps<{
      raw?: string;
      uploadImage?: (file: File) => Promise<any>;
      maxHeight?: string;
      filedIds?: string[];
      commentIds?: string[];
      wrapperClass?: string;
    }>(),
    {
      raw: '',
      uploadImage: undefined,
    }
  );

  const editor = shallowRef<Editor>();

  const emit = defineEmits<{
    (event: 'update:raw', value: string): void;
    (event: 'update:filedIds', value: string[]): void;
    (event: 'update', value: string): void;
    (event: 'update:commentIds', value: string): void;
  }>();

  const imagesNodesIds = useVModel(props, 'filedIds', emit);
  const commentNodeIds = useVModel(props, 'commentIds', emit);

  async function asyncWorker(arg: Task): Promise<void> {
    if (!props.uploadImage) {
      return;
    }
    const uploadFileId = await props.uploadImage(arg.file);
    if (uploadFileId) {
      const permanentUrl = `${PreviewEditorImageUrl}/${appStore.currentProjectId}/${uploadFileId}/${true}`;
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
    },
    {
      immediate: true,
    }
  );

  const showSidebar = useLocalStorage('halo:editor:show-sidebar', true);

  const attachmentSelectorModal = ref(false);
  const selectedimagesNode = ref<string>();
  const selectedCommentNode = ref<string>();

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
                    if (!selectedimagesNode.value) {
                      // eslint-disable-next-line prefer-destructuring
                      selectedimagesNode.value = images[0];
                    }
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
    &:hover {
      border-color: rgb(var(--primary-5));
    }
    @apply relative overflow-hidden;
    :deep(.halo-rich-text-editor .ProseMirror) {
      padding: 16px 24px !important;
      p:first-child {
        margin-top: 0;
      }
    }
    :deep(.halo-rich-text-editor) {
      padding: 16px 24px !important;
      .editor-header {
        .ms-scroll-bar();

        justify-content: start !important;
      }
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
