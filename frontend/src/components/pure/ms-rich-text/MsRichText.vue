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

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import AttachmentSelectorModal from './attachmentSelectorModal.vue';

  import { useI18n } from '@/hooks/useI18n';
  import useLocale from '@/locale/useLocale';

  import '@halo-dev/richtext-editor/dist/style.css';
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
    ExtensionImage,
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
    process: (permalink: string) => void;
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

  async function asyncWorker(arg: Task): Promise<void> {
    if (!props.uploadImage) {
      return;
    }
    const attachmentData = await props.uploadImage(arg.file);
    if (attachmentData.status?.permalink) {
      arg.process(attachmentData.status.permalink);
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
          allowBase64: false,
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
                      // icon: () => {
                      //   return defineComponent({
                      //     template: "<MsIcon type='icon-icon_link-copy_outlined' size='16' />",
                      //   });
                      // },
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
                    // icon: markRaw(RiLayoutRightLine),
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
          debugger;
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
              process: (url: string) => {
                editor.value
                  ?.chain()
                  .focus()
                  .insertContent([
                    {
                      type: 'image',
                      attrs: {
                        src: url,
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
</script>

<template>
  <div class="rich-wrapper flex w-full">
    <AttachmentSelectorModal v-model:visible="attachmentSelectorModal" />
    <RichTextEditor v-if="editor" :editor="editor" :content-styles="contentStyles" :locale="currentLocale" />
  </div>
</template>

<style scoped lang="less">
  .rich-wrapper {
    @apply relative overflow-hidden;

    border: 1px solid var(--color-text-n8);
    border-radius: var(--border-radius-small);
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
