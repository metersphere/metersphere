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
        {{ props.title }}
        <a-tooltip
          :content="activeDetailIsFirst ? t('ms.detail.drawer.noPrev') : t('ms.detail.drawer.prev')"
          :mouse-enter-delay="300"
          mini
        >
          <a-button
            type="outline"
            size="mini"
            class="arco-btn-outline--secondary ml-[16px] mr-[4px]"
            :disabled="activeDetailIsFirst || loading"
            @click="openPrevDetail"
          >
            <template #icon>
              <icon-left />
            </template>
          </a-button>
        </a-tooltip>
        <a-tooltip
          :content="activeDetailIsLast ? t('ms.detail.drawer.noNext') : t('ms.detail.drawer.next')"
          :mouse-enter-delay="300"
          mini
        >
          <a-button
            type="outline"
            size="mini"
            class="arco-btn-outline--secondary"
            :disabled="activeDetailIsLast || loading"
            @click="openNextDetail"
          >
            <template #icon>
              <icon-right />
            </template>
          </a-button>
        </a-tooltip>
        <div class="ml-auto flex items-center">
          <slot name="titleRight" :loading="loading" :detail="detail"></slot>
        </div>
      </div>
    </template>
    <slot :loading="loading" :detail="detail"></slot>
  </MsDrawer>
</template>

<script setup lang="ts">
  import { computed, ref, watch } from 'vue';
  import { useI18n } from '@/hooks/useI18n';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';

  import type { MsPaginationI } from '@/components/pure/ms-table/type';

  const props = defineProps<{
    visible: boolean;
    title: string;
    width: number;
    detailId: string | number; // 详情 id
    detailIndex: number; // 详情 下标
    tableData: any[]; // 表格数据
    pagination?: MsPaginationI; // 分页器对象
    pageChange: (page: number) => Promise<void>; // 分页变更函数
    getDetailFunc: (id: string | number) => Promise<any>; // 获取详情的请求函数
  }>();

  const emit = defineEmits(['update:visible', 'loaded']);
  const { t } = useI18n();

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

  const activeDetailId = ref<string | number>(props.detailId);

  async function initDetail() {
    try {
      loading.value = true;
      await new Promise((resolve) => {
        setTimeout(() => {
          resolve(true);
        }, 3000);
      });
      detail.value = await props.getDetailFunc(activeDetailId.value);
      emit('loaded', detail.value);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  }

  watch(
    () => props.detailId,
    (val) => {
      activeDetailId.value = val;
    }
  );

  const activeDetailIndex = ref(props.detailIndex);

  watch(
    () => props.detailIndex,
    (val) => {
      activeDetailIndex.value = val;
    }
  );

  // 当前查看的是否是总数据的第一条数据，用当前查看数据的下标是否等于0，且当前页码是否等于1
  const activeDetailIsFirst = computed(() => activeDetailIndex.value === 0 && props.pagination?.current === 1);
  const activeDetailIsLast = computed(
    // 当前查看的是否是总数据的最后一条数据，用当前页码*每页条数+当前查看的条数下标，是否等于总条数
    () =>
      activeDetailIndex.value === props.tableData.length - 1 &&
      // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
      props.pagination!.current * props.pagination!.pageSize + activeDetailIndex.value >=
        // eslint-disable-next-line @typescript-eslint/no-non-null-assertion
        props.pagination!.total
  );

  async function openPrevDetail() {
    if (!activeDetailIsFirst.value) {
      // 当前不是第一条，则往前查看
      if (activeDetailIndex.value === 0 && props.pagination) {
        try {
          // 当前查看的是当前页的第一条数据，则需要加载上一页的数据
          loading.value = true;
          await props.pageChange(props.pagination.current - 1);
          activeDetailId.value = props.tableData[props.tableData.length - 1].id;
          activeDetailIndex.value = props.tableData.length - 1;
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
          loading.value = false;
        }
      } else {
        // 当前查看的不是当前页的第一条数据，则直接查看上一条数据
        activeDetailId.value = props.tableData[activeDetailIndex.value - 1].id;
        activeDetailIndex.value -= 1;
      }
    }
  }

  async function openNextDetail() {
    if (!activeDetailIsLast.value) {
      // 当前不是最后一条，则往后查看
      if (activeDetailIndex.value === props.tableData.length - 1 && props.pagination) {
        try {
          // 当前查看的是当前页的最后一条数据，则需要加载下一页的数据
          loading.value = true;
          await props.pageChange(props.pagination.current + 1);
          activeDetailId.value = props.tableData[0].id;
          activeDetailIndex.value = 0;
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
          loading.value = false;
        }
      } else {
        // 当前查看的不是当前页的最后一条数据，则直接查看下一条数据
        activeDetailId.value = props.tableData[activeDetailIndex.value + 1].id;
        activeDetailIndex.value += 1;
      }
    }
  }

  watch(
    () => activeDetailId.value,
    () => {
      initDetail();
    }
  );
</script>

<style lang="less" scoped></style>
