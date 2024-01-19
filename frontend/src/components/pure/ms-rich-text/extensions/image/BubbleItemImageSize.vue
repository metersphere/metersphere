<script setup lang="ts">
  import { type Component, computed, ref } from 'vue';

  import { useI18n } from '@/hooks/useI18n';

  import Image from './index';
  import type { Editor } from '@halo-dev/richtext-editor';
  import { BlockActionButton, BlockActionInput, BlockActionSeparator } from '@halo-dev/richtext-editor';

  const { t } = useI18n();

  const props = defineProps<{
    editor: Editor;
    isActive?: ({ editor }: { editor: Editor }) => boolean;
    visible?: ({ editor }: { editor: Editor }) => boolean;
    icon?: Component;
    title?: string;
    action?: ({ editor }: { editor: Editor }) => void;
  }>();

  function handleSetSize(width?: string, height?: string) {
    props.editor
      .chain()
      .updateAttributes(Image.name, { width, height })
      .setNodeSelection(props.editor.state.selection.from)
      .focus()
      .run();
  }

  const height = computed({
    get: () => {
      return props.editor.getAttributes(Image.name).height;
    },
    set: (value: string) => {
      // eslint-disable-next-line no-use-before-define
      handleSetSize(width.value, value);
    },
  });

  const width = computed({
    get: () => {
      return props.editor.getAttributes(Image.name).width;
    },
    set: (value: string) => {
      handleSetSize(value, height.value);
    },
  });
</script>

<template>
  <BlockActionInput v-model.lazy.trim="width" :tooltip="t('editor.tooltip.custom_width_input')" />

  <BlockActionInput v-model.lazy.trim="height" :tooltip="t('editor.tooltip.custom_height_input')" />

  <BlockActionSeparator />

  <BlockActionButton
    :tooltip="t('editor.extensions.image.small_size')"
    :selected="editor.getAttributes(Image.name).width === '25%'"
    @click="handleSetSize('25%', 'auto')"
  >
    <template #icon>
      <icon-image />
    </template>
  </BlockActionButton>

  <BlockActionButton
    :tooltip="t('editor.extensions.image.medium_size')"
    :selected="editor.getAttributes(Image.name).width === '50%'"
    @click="handleSetSize('50%', 'auto')"
  >
    <template #icon>
      <icon-image />
    </template>
  </BlockActionButton>

  <BlockActionButton
    :tooltip="t('editor.extensions.image.large_size')"
    :selected="editor.getAttributes(Image.name).width === '100%'"
    @click="handleSetSize('100%', '100%')"
  >
    <template #icon>
      <icon-image />
    </template>
  </BlockActionButton>

  <BlockActionButton :tooltip="t('editor.extensions.image.restore_size')" @click="handleSetSize(undefined, undefined)">
    <template #icon>
      <MsIcon type="icon-icon_undo_outlined" class="text-[var(--color-text-3)]" size="16" />
    </template>
  </BlockActionButton>

  <BlockActionSeparator />
</template>
