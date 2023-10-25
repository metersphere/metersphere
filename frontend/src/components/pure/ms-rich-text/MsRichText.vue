<script lang="ts" setup>
  // import { unified } from 'unified';
  // import rehypeParse from 'rehype-parse';
  // import rehypeFormat from 'rehype-format';
  // import rehypeStringify from 'rehype-stringify';
  import { useLocalStorage } from '@vueuse/core';

  import useLocale from '@/locale/useLocale';

  import '@halo-dev/richtext-editor/dist/style.css';
  import {
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
    useEditor,
  } from '@halo-dev/richtext-editor';

  const content = useLocalStorage('content', '');

  const editor = useEditor({
    content: content.value,
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
      ExtensionCommands,
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
    ],
    onUpdate: () => {
      content.value = `${editor.value?.getHTML()}`;
      console.log(content.value);
    },
  });

  // const formatContent = computed(() => {
  //   return unified().use(rehypeParse).use(rehypeFormat).use(rehypeStringify).processSync(content.value);
  // });

  // watchEffect(() => {
  //   console.log(String(formatContent.value));
  // });
  const { currentLocale } = useLocale();

  // const locale = useLocalStorage('locale', 'zh-CN');
  const locale = computed(() => currentLocale.value as 'zh-CN' | 'en-US');
</script>

<template>
  <div style="height: 140px" class="rich-wrapper flex w-full">
    <RichTextEditor v-if="editor" :editor="editor" :locale="locale" />
  </div>
</template>

<style scoped lang="less">
  .rich-wrapper {
    border: 1px solid var(--color-text-n8);
  }
</style>
