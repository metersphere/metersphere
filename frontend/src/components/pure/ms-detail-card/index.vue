<template>
  <div class="ms-detail-card">
    <div class="ms-detail-card-title">
      <div class="flex flex-1 items-center gap-[8px]">
        <slot name="titlePrefix"></slot>
        <a-tooltip :content="t(props.title)">
          <div class="one-line-text max-w-[300px] font-medium text-[var(--color-text-1)]">
            {{ t(props.title) }}
          </div>
        </a-tooltip>
        <slot name="titleAppend"></slot>
      </div>
      <div v-if="$slots.titleRight" class="flex items-center overflow-hidden">
        <slot name="titleRight"></slot>
      </div>
    </div>
    <div v-if="showingDescription.length > 0" class="ms-detail-card-desc">
      <div
        v-for="item of showingDescription"
        :key="item.key"
        :class="[
          `ms-detail-card-desc-item ${
            Array.isArray(item.value) && item.value.length > 0 ? 'ms-detail-card-desc-tag' : ''
          } flex w-[calc(100%/3)] items-center gap-[8px]`,
        ]"
        :style="{ width: item.width }"
      >
        <div class="whitespace-nowrap text-[var(--color-text-4)]">
          {{ t(item.locale) }}
        </div>
        <div v-if="Array.isArray(item.value)" class="pr-[24px]">
          <MsTagGroup :tag-list="item.value" size="small" is-string-tag />
        </div>
        <slot v-else :name="item.key" :value="item.value">
          <a-tooltip :content="item.value" :disabled="isEmpty(item.value)" :position="item.tooltipPosition">
            <div
              class="text-ov overflow-hidden overflow-ellipsis whitespace-nowrap pr-[24px] text-[var(--color-text-1)]"
              >{{ item.value || '-' }}</div
            >
          </a-tooltip>
        </slot>
      </div>
    </div>
    <MsButton
      v-if="props.simpleShowCount !== undefined && props.simpleShowCount > 0"
      type="text"
      class="more-btn"
      @click="toggleExpand"
    >
      <div v-if="isExpand" class="flex items-center gap-[4px]">
        {{ t('msDetailCard.collapse') }}
        <icon-up :size="14" />
      </div>
      <div v-else class="flex items-center gap-[4px]">
        {{ t('msDetailCard.more') }}
        <icon-down :size="14" />
      </div>
    </MsButton>
  </div>
</template>

<script setup lang="ts">
  import { isEmpty } from 'lodash-es';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsTagGroup from '@/components/pure/ms-tag/ms-tag-group.vue';

  import { useI18n } from '@/hooks/useI18n';

  export interface Description {
    key: string;
    locale: string;
    value: string | string[];
    width?: string;
    tooltipPosition?: 'top' | 'tl' | 'tr' | 'bottom' | 'bl' | 'br' | 'left' | 'lt' | 'lb' | 'right' | 'rt' | 'rb';
  }

  const props = defineProps<{
    title: string;
    description: Description[];
    simpleShowCount?: number; // 简单展示的数量，超过的内容隐藏，并展示更多按钮
  }>();

  const { t } = useI18n();

  const isExpand = ref(false);

  function toggleExpand() {
    isExpand.value = !isExpand.value;
  }

  const showingDescription = computed(() => {
    if (isExpand.value) {
      return props.description;
    }
    if (props.simpleShowCount && props.description.length > props.simpleShowCount) {
      return props.description.slice(0, props.simpleShowCount);
    }
    return props.description;
  });
</script>

<style lang="less" scoped>
  .ms-detail-card {
    @apply relative flex flex-col;

    border-radius: var(--border-radius-small);
    gap: 8px;
    .ms-detail-card-title {
      @apply flex items-center justify-between;

      gap: 16px;
    }
    .ms-detail-card-desc {
      @apply flex flex-wrap overflow-hidden; // TODO:过渡动画
    }
    .more-btn {
      @apply absolute;

      bottom: 2px;
      left: 50%;
      font-size: 12px;
      transform: translateX(-50%);
      line-height: 16px;
    }
  }
</style>
