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

  import useLocale from '@/locale/useLocale';

  import '@halo-dev/richtext-editor/dist/style.css';
  import suggestion from './extensions/mention/suggestion';
  import {
    Editor,
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
  } from '@halo-dev/richtext-editor';
  import Mention from '@tiptap/extension-mention';

  const props = withDefaults(
    defineProps<{
      raw?: string;
      uploadImage?: (file: File) => Promise<any>;
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

  // debounce OnUpdate
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
  });

  const { currentLocale } = useLocale();
  const locale = computed(() => currentLocale.value as 'zh-CN' | 'en-US');

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

  onBeforeUnmount(() => {
    editor.value?.destroy();
  });
</script>

<template>
  <div class="rich-wrapper flex w-full">
    <RichTextEditor v-if="editor" :editor="editor" :locale="locale" />
  </div>
</template>

<style scoped lang="less">
  .rich-wrapper {
    position: relative;
    border: 1px solid var(--color-text-n8);
    :deep(.halo-rich-text-editor .ProseMirror) {
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
  // 修改滚动条
  :deep(.editor-header + div > div) {
    &::-webkit-scrollbar {
      width: 6px !important;
      height: 4px !important;
    }
    &::-webkit-scrollbar-thumb {
      border-radius: 8px !important;
      background: var(--color-text-input-border) !important;
    }
    &::-webkit-scrollbar-thumb:hover {
      background: #a1a7b0 !important;
    }
    &&::-webkit-scrollbar-track {
      @apply bg-white !important;
    }
  }
</style>
