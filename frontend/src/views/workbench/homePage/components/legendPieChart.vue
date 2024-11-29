<template>
  <div class="flex h-full w-full">
    <div class="w-[180px]">
      <MsChart ref="chartRef" :options="props.options" />
    </div>
    <div v-if="props.hasPermission" class="relative mt-[8px] h-full flex-1">
      <!-- 图例部分 -->
      <div class="flex w-full flex-col gap-4">
        <div
          v-for="(ele, i) of currentData"
          :key="`${ele.status}-${i}`"
          class="grid flex-1 grid-cols-3 gap-4"
          @mouseover="handleMouseOver(ele.status)"
          @mouseout="handleMouseOut(ele.status)"
        >
          <div class="flex items-center text-left text-[var(--color-text-3)]">
            <div
              :style="{
                background: ele.selected ? `${ele.color}` : '#D4D4D8',
              }"
              class="mr-[8px] h-[8px] w-[8px] cursor-pointer rounded-lg"
              @click="toggleLegend(ele.status, i)"
            >
            </div>
            {{ ele.status }}
          </div>
          <div class="text-center">{{ ele.count }}</div>
          <div class="text-right">{{ ele.percentValue }}</div>
        </div>
      </div>

      <div v-if="totalPages > 1" class="legend-pagination">
        <span :class="`toggle-button ${currentPage === 1 ? 'disabled' : ''}`" @click="prevPage">
          <icon-caret-up
            :class="`text-[14px] ${
              currentPage === 1 ? 'text-[var(--color-text-brand)]' : 'text-[var(--color-text-4)]'
            }`"
          />
        </span>
        <span class="mx-[4px] text-[var(--color-text-4)]">{{ currentPage }} / {{ totalPages }}</span>
        <span :class="`toggle-button ${currentPage === totalPages ? 'disabled' : ''}`" @click="nextPage">
          <icon-caret-down
            :class="`text-[14px] ${
              currentPage === totalPages ? 'text-[var(--color-text-brand)]' : 'text-[var(--color-text-4)]'
            }`"
          />
        </span>
      </div>
    </div>
    <div v-else class="flex h-full flex-1 items-center justify-center">
      <div class="rounded bg-[var(--color-text-n9)] px-[16px] py-[4px] text-[var(--color-text-4)]"
        >{{ t('workbench.homePage.notHasResPermission') }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { computed } from 'vue';

  import MsChart from '@/components/pure/chart/index.vue';

  import { useI18n } from '@/hooks/useI18n';

  const { t } = useI18n();

  export interface legendDataType {
    status: string;
    count: number;
    percentValue: string;
    selected: boolean;
    color: string;
  }

  const props = withDefaults(
    defineProps<{
      data: legendDataType[] | null;
      itemsPerPage?: number; // 每页数量
      options: Record<string, any>;
      hasPermission?: boolean;
    }>(),
    {
      itemsPerPage: 4,
    }
  );

  const currentPage = defineModel<number>('currentPage', { required: true });

  const list = ref<legendDataType[]>([...(props.data || [])]);

  // 总页数
  const totalPages = computed(() => Math.ceil(list.value.length / props.itemsPerPage));

  // 当前页显示的数据
  const currentData = computed(() => {
    const startIndex = (currentPage.value - 1) * props.itemsPerPage;
    const endIndex = startIndex + props.itemsPerPage;
    return list.value.slice(startIndex, endIndex);
  });

  // 分页方法
  const prevPage = () => {
    if (currentPage.value > 1) currentPage.value -= 1;
  };

  const nextPage = () => {
    if (currentPage.value < totalPages.value) currentPage.value += 1;
  };

  // 切换图例
  const chartRef = ref<InstanceType<typeof MsChart>>();
  function toggleLegend(name: string, index: number) {
    const chart = chartRef.value?.chartRef;
    if (chart) {
      chart.dispatchAction({
        type: 'legendToggleSelect',
        name,
      });

      if (list.value) {
        list.value[index].selected = !list.value[index].selected;
      }
    }
  }

  // 悬浮放大交互效果
  function handleMouseOver(name: string) {
    const chart = chartRef.value?.chartRef;
    if (chart) {
      chart.dispatchAction({
        type: 'highlight',
        name,
      });
    }
  }
  // 移除放大交互效果
  function handleMouseOut(name: string) {
    const chart = chartRef.value?.chartRef;
    if (chart) {
      chart.dispatchAction({
        type: 'downplay',
        name,
      });
    }
  }

  watch(
    () => props.data,
    (val) => {
      list.value = [...(val || [])];
    }
  );
</script>

<style scoped>
  .legend-pagination {
    position: absolute;
    right: 0;
    bottom: -10px;
    left: 0;
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 4px;
    .toggle-button {
      @apply cursor-pointer;
      &.disabled {
        cursor: not-allowed;
      }
    }
  }
</style>
