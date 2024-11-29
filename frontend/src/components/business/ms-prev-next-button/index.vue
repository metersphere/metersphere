<template>
  <div v-if="props.pagination?.total > 1" class="flex items-center">
    <a-tooltip
      :content="activeDetailIsFirst ? t('ms.prevNextButton.noPrev') : t('ms.prevNextButton.prev')"
      :mouse-enter-delay="300"
      mini
    >
      <a-button
        type="outline"
        size="mini"
        class="arco-btn-outline--secondary mr-[4px]"
        :disabled="activeDetailIsFirst || innerLoading"
        @click="openPrevDetail"
      >
        <template #icon>
          <icon-left />
        </template>
      </a-button>
    </a-tooltip>
    <a-tooltip
      :content="activeDetailIsLast ? t('ms.prevNextButton.noNext') : t('ms.prevNextButton.next')"
      :mouse-enter-delay="300"
      mini
    >
      <a-button
        type="outline"
        size="mini"
        class="arco-btn-outline--secondary"
        :disabled="activeDetailIsLast || innerLoading"
        @click="openNextDetail"
      >
        <template #icon>
          <icon-right />
        </template>
      </a-button>
    </a-tooltip>
  </div>
</template>

<script setup lang="ts">
  import { MsPaginationI } from '@/components/pure/ms-table/type';

  import { useI18n } from '@/hooks/useI18n';

  const props = defineProps<{
    loading: boolean;
    detailId: string; // 详情 id
    detailIndex?: number; // 详情 下标
    tableData: any[]; // 表格数据
    pagination: MsPaginationI; // 分页器对象
    pageChange: (page: number) => Promise<void>; // 分页变更函数
    getDetailFunc: (id: string) => Promise<any>; // 获取详情的请求函数
  }>();

  const emit = defineEmits(['update:loading', 'loaded', 'loadingDetail']);

  const { t } = useI18n();

  const innerLoading = ref(false);

  watch(
    () => props.loading,
    (val) => {
      innerLoading.value = val;
    }
  );

  watch(
    () => innerLoading.value,
    (val) => {
      emit('update:loading', val);
    }
  );

  const activeDetailId = ref<string>(props.detailId);

  watch(
    () => props.detailId,
    (val) => {
      activeDetailId.value = val;
    },
    { immediate: true }
  );

  const activeDetailIndex = ref(props.detailIndex || 0);

  watch(
    () => props.detailIndex,
    (val) => {
      activeDetailIndex.value = val as number;
    }
  );

  async function initDetail(id?: string) {
    try {
      innerLoading.value = true;
      emit('loadingDetail');
      const res = await props.getDetailFunc(id || activeDetailId.value);
      emit('loaded', res);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      innerLoading.value = false;
    }
  }

  // 当前查看的是否是总数据的第一条数据，用当前查看数据的下标是否等于0，且当前页码是否等于1
  const activeDetailIsFirst = computed(() => activeDetailIndex.value === 0 && props.pagination.current === 1);
  const activeDetailIsLast = computed(
    // 当前查看的是否是总数据的最后一条数据，用(当前页码-1)*每页条数+当前查看的条数下标，是否等于总条数
    () =>
      activeDetailIndex.value === props.tableData.length - 1 &&
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      (props.pagination.current - 1) * props.pagination.pageSize + (activeDetailIndex.value + 1) >=
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        props.pagination.total
  );

  async function openPrevDetail() {
    if (!activeDetailIsFirst.value) {
      // 当前不是第一条，则往前查看
      if (activeDetailIndex.value === 0 && props.pagination) {
        try {
          // 当前查看的是当前页的第一条数据，则需要加载上一页的数据
          innerLoading.value = true;
          await props.pageChange(props.pagination.current - 1);
          activeDetailId.value = props.tableData[props.tableData.length - 1].id;
          activeDetailIndex.value = props.tableData.length - 1;
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
          innerLoading.value = false;
        }
      } else {
        // 当前查看的不是当前页的第一条数据，则直接查看上一条数据
        activeDetailId.value = props.tableData[activeDetailIndex.value - 1].id;
        activeDetailIndex.value -= 1;
      }
      initDetail();
    }
  }

  async function openNextDetail() {
    if (!activeDetailIsLast.value) {
      // 当前不是最后一条，则往后查看
      if (activeDetailIndex.value === props.tableData.length - 1 && props.pagination) {
        try {
          // 当前查看的是当前页的最后一条数据，则需要加载下一页的数据
          innerLoading.value = true;
          await props.pageChange(props.pagination.current + 1);
          activeDetailId.value = props.tableData[0].id;
          activeDetailIndex.value = 0;
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
          innerLoading.value = false;
        }
      } else {
        // 当前查看的不是当前页的最后一条数据，则直接查看下一条数据
        activeDetailId.value = props.tableData[activeDetailIndex.value + 1].id;
        activeDetailIndex.value += 1;
      }
      initDetail();
    }
  }

  defineExpose({
    initDetail,
    openPrevDetail,
    openNextDetail,
  });
</script>

<style lang="less" scoped></style>
