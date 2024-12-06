<template>
  <div v-if="props.contentTabList.length" class="card-list">
    <div v-for="ele of contentTabList" :key="ele.icon" class="card-list-item">
      <div class="w-full">
        <div class="card-title flex items-center gap-[8px]">
          <div :class="`card-title-icon bg-[${ele?.color}]`">
            <MsIcon :type="ele.icon" class="text-[var(--color-text-fff)]" size="12" />
          </div>
          <div class="text-[var(--color-text-1)]"> {{ ele.label }}</div>
        </div>
        <div class="card-number !text-[20px] !font-medium"> {{ addCommasToNumber(ele.count || 0) }} </div>
      </div>
    </div>
  </div>
  <NoData v-else :no-permission-text="props.noPermissionText" />
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import NoData from '../../components/notData.vue';

  import { addCommasToNumber } from '@/utils';

  const props = defineProps<{
    contentTabList: {
      label: string;
      value: string | number;
      count?: number;
      icon?: string;
      color?: string;
      [key: string]: any;
    }[];
    noPermissionText?: string;
  }>();
</script>

<style scoped lang="less">
  .card-list {
    gap: 16px;
    @apply flex  flex-1;
    .card-list-item {
      padding: 16px;
      border: 1px solid var(--color-text-n8);
      border-radius: 4px;
      @apply flex-1;
      .card-title-icon {
        width: 20px;
        height: 20px;
        border-radius: 50%;
        @apply flex items-center justify-center;
      }
      .card-number {
        margin-left: 28px;
        font-size: 20px;
      }
    }
  }
</style>
