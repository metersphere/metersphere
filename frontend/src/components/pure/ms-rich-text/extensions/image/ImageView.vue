<script lang="ts" setup>
  import { computed, onMounted, ref } from 'vue';

  import { useI18n } from '@/hooks/useI18n';

  import Image from './index';
  import type { Decoration, Editor, Node, Node as ProseMirrorNode } from '@halo-dev/richtext-editor';
  import { NodeViewWrapper } from '@halo-dev/richtext-editor';

  const { t } = useI18n();
  const props = defineProps<{
    editor: Editor;
    node: any;
    decorations: Decoration[];
    selected: boolean;
    extension: Node<any, any>;
    getPos: () => number;
    updateAttributes: (attributes: Record<string, any>) => void;
    deleteNode: () => void;
  }>();

  const src = computed({
    get: () => {
      return props.node?.attrs.src;
    },
    set: (newSrc: string) => {
      props.updateAttributes({
        src: newSrc,
      });
    },
  });

  const alt = computed({
    get: () => {
      return props.node?.attrs.alt;
    },
    set: (newAlt: string) => {
      props.updateAttributes({ alt: newAlt });
    },
  });

  const href = computed({
    get: () => {
      return props.node?.attrs.href;
    },
    set: (newHref: string) => {
      props.updateAttributes({ href: newHref });
    },
  });

  // 请求成功的文件id
  const fileId = computed({
    get: () => {
      return props.node?.attrs.fileId;
    },
    set: (newFileId: string) => {
      props.updateAttributes({ fileId: newFileId });
    },
  });

  function handleSetFocus() {
    props.editor.commands.setNodeSelection(props.getPos());
  }

  const aspectRatio = ref<number>(0);
  const inputRef = ref();
  const resizeRef = ref<HTMLDivElement>();

  function onImageLoaded() {
    if (!resizeRef.value) return;
    aspectRatio.value = resizeRef.value.clientWidth / resizeRef.value.clientHeight;
  }

  onMounted(() => {
    if (!src.value) {
      inputRef.value.focus();
      return;
    }

    if (!resizeRef.value) return;

    let startX: number;
    let startWidth: number;

    function doDrag(e: MouseEvent) {
      if (!resizeRef.value) return;

      const newWidth = Math.min(startWidth + e.clientX - startX, resizeRef.value.parentElement?.clientWidth || 0);

      const width = `${newWidth.toFixed(0)}px`;
      const height = `${(newWidth / aspectRatio.value).toFixed(0)}px`;
      props.editor
        .chain()
        .updateAttributes(Image.name, { width, height })
        .setNodeSelection(props.getPos())
        .focus()
        .run();
    }

    function stopDrag() {
      document.documentElement.removeEventListener('mousemove', doDrag, false);
      document.documentElement.removeEventListener('mouseup', stopDrag, false);
    }

    resizeRef.value.addEventListener('mousedown', function (e) {
      startX = e.clientX;
      startWidth = resizeRef.value?.clientWidth || 1;
      document.documentElement.addEventListener('mousemove', doDrag, false);
      document.documentElement.addEventListener('mouseup', stopDrag, false);
    });
  });
</script>

<template>
  <node-view-wrapper as="div" class="inline-block w-full">
    <div v-if="!src" class="w-full p-1.5">
      <input
        ref="inputRef"
        v-model.lazy="src"
        class="block w-full rounded-md border border-gray-300 bg-gray-50 px-2 py-1.5 text-sm text-gray-900 focus:border-blue-500 focus:ring-blue-500"
        :placeholder="t('editor.placeholder.alt_input')"
        tabindex="-1"
        @focus="handleSetFocus"
      />
    </div>
    <div
      v-else
      ref="resizeRef"
      class="relative inline-block max-w-full resize-x overflow-hidden rounded-md text-center"
      :class="{
        'rounded ring-2': selected,
      }"
      :style="{
        width: node.attrs.width,
        height: node.attrs.height,
      }"
    >
      <img :src="src" :title="node.attrs.title" :alt="alt" :href="href" class="h-full w-full" @load="onImageLoaded" />
    </div>
  </node-view-wrapper>
</template>
