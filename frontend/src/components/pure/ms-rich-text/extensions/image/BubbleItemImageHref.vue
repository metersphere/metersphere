<script setup lang="ts">
  import { type Component, computed } from 'vue';

  import { useI18n } from '@/hooks/useI18n';

  import Image from './index';
  import type { Editor } from '@halo-dev/richtext-editor';

  const { t } = useI18n();

  const props = defineProps<{
    editor: Editor;
    isActive: ({ editor }: { editor: Editor }) => boolean;
    visible?: ({ editor }: { editor: Editor }) => boolean;
    icon?: Component;
    title?: string;
    action?: ({ editor }: { editor: Editor }) => void;
  }>();

  const hrefSrc = computed({
    get: () => {
      return props.editor.getAttributes(Image.name).href;
    },
    set: (href: string) => {
      props.editor
        .chain()
        .updateAttributes(Image.name, { href })
        .setNodeSelection(props.editor.state.selection.from)
        .focus()
        .run();
    },
  });
</script>

<template>
  <input
    v-model.lazy="hrefSrc"
    :placeholder="t('editor.placeholder.alt_input')"
    class="block w-full rounded-md border border-gray-300 bg-gray-50 px-2 py-1.5 text-sm text-gray-900 hover:bg-gray-100 focus:border-blue-500 focus:ring-blue-500"
  />
</template>
