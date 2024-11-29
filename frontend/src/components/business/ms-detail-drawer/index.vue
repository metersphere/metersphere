<template>
  <MsDrawer
    v-model:visible="innerVisible"
    :width="props.width"
    :footer="false"
    class="ms-drawer"
    :show-full-screen="props.showFullScreen"
    no-content-padding
    unmount-on-close
  >
    <template #title>
      <div class="flex flex-1 items-center overflow-hidden">
        <!-- 如果设置了tooltipText，则优先展示-->
        <div class="flex flex-1 items-center overflow-hidden">
          <a-tooltip :content="props.tooltipText ? props.tooltipText : props.title" position="bottom">
            <slot name="titleName">
              <div class="one-line-text">
                {{ props.title }}
              </div>
            </slot>
          </a-tooltip>
          <div class="mx-4 flex items-center">
            <slot name="titleLeft" :loading="loading" :detail="detail"></slot>
          </div>
        </div>
        <div class="ml-auto flex items-center">
          <MsPrevNextButton
            v-if="props.tableData && props.pagination && props.pageChange"
            ref="prevNextButtonRef"
            v-model:loading="loading"
            class="mr-[16px]"
            :page-change="props.pageChange"
            :pagination="props.pagination"
            :get-detail-func="props.getDetailFunc"
            :detail-id="props.detailId"
            :detail-index="props.detailIndex"
            :table-data="props.tableData"
            @loading-detail="setDetailLoading"
            @loaded="handleDetailLoaded"
          />
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
    tooltipText?: string; // tooltip内容
    detailIndex?: number; // 详情 下标
    tableData?: any[]; // 表格数据
    pagination?: MsPaginationI; // 分页器对象
    showFullScreen?: boolean; // 是否显示全屏按钮
    pageChange?: (page: number) => Promise<void>; // 分页变更函数
    getDetailFunc: (id: string) => Promise<any>; // 获取详情的请求函数
  }>();

  const emit = defineEmits(['update:visible', 'loaded', 'loadingDetail', 'getDetail']);

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

  function initDetail(id?: string) {
    prevNextButtonRef.value?.initDetail(id);
  }

  function openPrevDetail() {
    prevNextButtonRef.value?.openPrevDetail();
  }

  function openNextDetail() {
    prevNextButtonRef.value?.openNextDetail();
  }

  function handleDetailLoaded(val: any) {
    detail.value = val;
    emit('loaded', val);
  }

  function setDetailLoading() {
    emit('loadingDetail');
  }

  watch([() => innerVisible.value, () => props.detailId], () => {
    if (innerVisible.value) {
      nextTick(() => {
        // 为了确保 prevNextButtonRef 已渲染
        if (props.tableData && props.pagination && props.pageChange) {
          initDetail();
        } else {
          emit('getDetail');
        }
      });
    }
  });

  defineExpose({
    initDetail,
    openPrevDetail,
    openNextDetail,
  });
</script>

<style lang="less" scoped></style>
