<template>
  <div :class="['ms-thumbnail-card', `ms-thumbnail-card--${props.mode}`]" @click="handleCardClick">
    <div class="ms-thumbnail-card-content">
      <div class="ms-thumbnail-card-more">
        <MsTableMoreAction v-if="props.moreActions" :list="props.moreActions" @select="handleMoreActionSelect" />
      </div>
      <img v-if="fileType === 'image'" :src="props.url" class="absolute top-0" />
      <MsIcon v-else :type="FileIconMap[fileType][UploadStatus.done]" class="absolute top-0 h-full w-full p-[24px]" />
      <div class="ms-thumbnail-card-footer">
        {{ props.footerText }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { computed } from 'vue';

  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import { FileIconMap, getFileEnum } from '@/components/pure/ms-upload/iconMap';

  import { UploadStatus } from '@/enums/uploadEnum';

  const props = withDefaults(
    defineProps<{
      mode?: 'default' | 'hover'; // 默认模式和hover模式，hover 模式时会在 hover 卡片时显示 footer；default 则是一直显示 footer
      type: string;
      url?: string;
      footerText?: string;
      moreActions?: ActionsItem[];
    }>(),
    {
      mode: 'default',
    }
  );
  const emit = defineEmits<{
    (e: 'click'): void;
    (e: 'actionSelect', item: ActionsItem): void;
  }>();

  const fileType = computed(() => {
    if (props.type) {
      return getFileEnum(`/${props.type.toLowerCase()}`);
    }
    return 'unknown';
  });

  function handleCardClick() {
    emit('click');
  }

  function handleMoreActionSelect(item: ActionsItem) {
    emit('actionSelect', item);
  }
</script>

<style lang="less" scoped>
  .ms-thumbnail-card {
    @apply relative w-full;

    aspect-ratio: 1/1;
    .ms-thumbnail-card-content {
      @apply absolute bottom-0 left-0 right-0 top-0 inline-block flex-grow cursor-pointer overflow-hidden;

      min-width: 102px;
      border-radius: var(--border-radius-small);
      background-color: var(--color-text-n9);
      transition: all 0.2s;
      &::before {
        content: '';
        display: block;
        padding-bottom: 100%; /* 高度与宽度 1:1 */
      }
    }
    &:hover {
      .ms-thumbnail-card-more {
        @apply visible;
      }
    }
    .ms-thumbnail-card-more {
      @apply invisible absolute bg-white;

      top: 4px;
      right: 4px;
      z-index: 1;
      padding: 4px;
      border-radius: var(--border-radius-small);
      &:hover {
        color: rgb(var(--primary-5));
        background-color: rgb(var(--primary-1));
      }
    }
    .ms-thumbnail-card-footer {
      @apply absolute w-full text-center;

      bottom: 0;
      padding: 2px 0;
      font-size: 12px;
      font-weight: 500;
      color: #ffffff;
      background-color: #00000050;
    }
  }
  .ms-thumbnail-card--default {
    &:hover {
      background-color: rgb(50 50 51 / 10%);
    }
    .ms-thumbnail-card-footer {
      @apply visible;

      background-color: #32323330;
    }
  }
  .ms-thumbnail-card--hover {
    &:hover {
      .ms-thumbnail-card-footer {
        @apply visible;
      }
    }
    .ms-thumbnail-card-footer {
      @apply invisible;
    }
  }
</style>
