<template>
  <MsDrawer
    v-model:visible="innerVisible"
    :width="props.width"
    :footer="false"
    class="ms-drawer"
    no-content-padding
    unmount-on-close
  >
    <template #title>
      <div class="flex w-full items-center">
        <a-tooltip :content="props.title" position="bottom">
          <div class="one-line-text max-w-[300px]">
            {{ props.title }}
          </div>
        </a-tooltip>

        <MsPrevNextButton
          ref="prevNextButtonRef"
          v-model:loading="loading"
          class="ml-[16px]"
          :page-change="props.pageChange"
          :pagination="props.pagination"
          :get-detail-func="props.getDetailFunc"
          :detail-id="props.detailId"
          :detail-index="props.detailIndex"
          :table-data="props.tableData"
          @loaded="(e) => emit('loaded', e)"
        />
        <div class="ml-auto flex items-center">
          <slot name="titleRight" :loading="loading" :detail="detail"></slot>
        </div>
      </div>
    </template>
    <slot :loading="loading" :detail="detail"></slot>
  </MsDrawer>
</template>

<script setup lang="ts">
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import type { MsPaginationI } from '@/components/pure/ms-table/type';
  import MsPrevNextButton from '@/components/business/ms-prev-next-button/index.vue';

  const props = defineProps<{
    visible: boolean;
    title: string;
    width: number;
    detailId: string; // 详情 id
    detailIndex: number; // 详情 下标
    tableData: any[]; // 表格数据
    pagination: MsPaginationI; // 分页器对象
    pageChange: (page: number) => Promise<void>; // 分页变更函数
    getDetailFunc: (id: string) => Promise<any>; // 获取详情的请求函数
  }>();

  const emit = defineEmits(['update:visible', 'loaded']);

  const prevNextButtonRef = ref<InstanceType<typeof MsPrevNextButton>>();

  const innerVisible = ref(false);

  watch(
    () => props.visible,
    (val) => {
      innerVisible.value = val;
    }
  );

  watch(
    () => innerVisible.value,
    (val) => {
      emit('update:visible', val);
    }
  );

  const loading = ref(false);
  const detail = ref<any>({});

  function initDetail() {
    prevNextButtonRef.value?.initDetail();
  }

  function openPrevDetail() {
    prevNextButtonRef.value?.openPrevDetail();
  }

  function openNextDetail() {
    prevNextButtonRef.value?.openNextDetail();
  }

  watch(
    () => innerVisible.value,
    (val) => {
      if (val) {
        nextTick(() => {
          // 为了确保 prevNextButtonRef 已渲染
          initDetail();
        });
      }
    }
  );

  defineExpose({
    initDetail,
    openPrevDetail,
    openNextDetail,
  });
</script>

<style lang="less" scoped></style>
