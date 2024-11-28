<template>
  <MsPagination
    v-if="props.loopTotal > 1"
    v-model:current="currentLoop"
    :page-size="1"
    :total="props.loopTotal"
    :show-jumper="props.loopTotal > 5"
    :base-size="0"
    show-total
    size="mini"
    class="loop-pagination"
    @change="() => emit('change', currentLoop)"
  >
    <template #total="{ total }">
      <div
        class="rounded-[var(--border-radius-small)] bg-[var(--color-text-n8)] p-[2px_6px] leading-[20px] text-[var(--color-text-2)]"
      >
        {{ t('apiScenario.sumLoop', { count: total }) }}
      </div>
    </template>
    <template #jumper-append>
      {{ t('apiScenario.times') }}
    </template>
  </MsPagination>
</template>

<script setup lang="ts">
  import { useI18n } from 'vue-i18n';

  import MsPagination from '@/components/pure/ms-pagination';

  const props = defineProps<{
    loopTotal: number;
  }>();
  const emit = defineEmits<{
    (e: 'change', value: number): void;
  }>();

  const { t } = useI18n();

  const currentLoop = defineModel<number>('currentLoop', {
    required: true,
  });
</script>

<style lang="less">
  .arco-popover,
  .arco-drawer {
    .loop-pagination {
      @apply justify-start;

      gap: 2px;
      margin-bottom: 8px;
      .ms-pagination-list {
        gap: 2px;
        padding: 2px;
        border-radius: var(--border-radius-small);
        background-color: var(--color-text-n8);
        .ms-pagination-item {
          padding: 0 6px;
          min-width: 20px;
          height: 20px;
          border: none;
          border-radius: var(--border-radius-mini);
          color: var(--color-text-1);
          background-color: var(--color-text-fff);
          line-height: 20px;
        }
        .ms-pagination-item-previous {
          margin-left: 0;
        }
        .ms-pagination-item-disabled {
          cursor: not-allowed;
          color: var(--color-text-4);
        }
        .ms-pagination-item-active {
          border: 1px solid rgb(var(--primary-5)) !important;
          color: rgb(var(--primary-5)) !important;
          background-color: var(--color-text-fff) !important;
        }
      }
      .ms-pagination-jumper {
        gap: 4px;
        padding: 2px 6px;
        border-radius: var(--border-radius-mini);
        color: var(--color-text-2);
        background-color: var(--color-text-n8);
        .ms-pagination-jumper-input {
          width: 48px;
          background-color: var(--color-text-fff);
          input {
            height: 18px;
          }
        }
        .ms-pagination-jumper-total-page {
          @apply hidden;
        }
      }
    }
  }
</style>
