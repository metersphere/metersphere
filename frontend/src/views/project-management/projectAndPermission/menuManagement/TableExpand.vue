<template>
  <div class="flex flex-col">
    <div class="flex h-[54px] flex-row items-center pl-[61px]">
      <div class="title w-[251px] py-[16px] pl-[36px] pr-[8px]"> 报告保留时间范围 </div>
      <div class="w-[547px] px-[16px]">
        <a-input :style="{ width: '320px' }" placeholder="Please enter something" allow-clear>
          <template #append> RMB </template>
        </a-input>
      </div>
      <div class="w-[90px] p-[16px]">
        <a-switch />
      </div>
    </div>
  </div>
</template>

<script lang="ts" setup>
  import { MenuTableListItem } from '@/models/projectManagement/menuManagement';

  const props = defineProps<{
    record: MenuTableListItem;
  }>();
</script>
