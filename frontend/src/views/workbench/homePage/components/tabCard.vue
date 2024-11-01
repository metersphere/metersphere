<template>
  <div>
    <a-tabs default-active-key="1" class="ms-tab-card">
      <a-tab-pane v-for="item of props.contentTabList" :key="item.value" :title="`${item.label}`">
        <template #title>
          <div>
            <div class="card-title flex items-center gap-[8px]">
              <MsIcon :type="item.icon" class="text-[rgb(var(--primary-5))]" size="16" />
              <div class="text-[var(--color-text-1)]"> {{ item.label }}</div>
            </div>
            <div class="card-number !text-[20px] !font-medium"> {{ addCommasToNumber(item.count) }} </div>
          </div>
        </template>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import { addCommasToNumber } from '@/utils';

  const props = defineProps<{
    contentTabList: { label: string | number; value: string | number; count: number; icon: string }[];
  }>();
</script>

<style scoped lang="less">
  :deep(.ms-tab-card) {
    .arco-tabs-nav-tab {
      .arco-tabs-nav-ink {
        display: none;
      }
      .arco-tabs-tab {
        margin: 0 8px;
        padding: 16px !important;
        border: 1px solid var(--color-text-n8);
        border-radius: 4px;
        &.arco-tabs-tab-active {
          color: var(--color-text-1);
        }
        &:hover {
          color: var(--color-text-1);
        }
      }
      .arco-tabs-tab-title {
        .card-number {
          margin-left: 24px;
          font-size: 20px !important;
        }
      }
    }
  }
  :deep(.arco-tabs-nav-button) {
    width: 36px;
    height: 36px;
    border: 1px solid var(--color-text-n8);
    border-radius: 2px;
  }
</style>
