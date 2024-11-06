<template>
  <div class="card-wrapper">
    <div class="flex items-center justify-between">
      <div class="title"> {{ t('workbench.homePage.defectProcessingNumber') }} </div>
      <div class="flex items-center gap-[8px]">
        <MsSelect
          v-model:model-value="projectIds"
          :options="projectOptions"
          :allow-search="false"
          allow-clear
          class="!w-[240px]"
          :prefix="t('workbench.homePage.project')"
          :has-all-select="true"
          :default-all-select="true"
        >
        </MsSelect>
        <MsSelect
          v-model:model-value="memberIds"
          :options="memberOptions"
          :allow-search="false"
          allow-clear
          class="!w-[240px]"
          :prefix="t('workbench.homePage.staff')"
          :multiple="true"
          :has-all-select="true"
          :default-all-select="true"
        >
        </MsSelect>
      </div>
    </div>
    <div class="mt-[16px]">
      <MsChart height="300px" :options="options" />
    </div>
  </div>
</template>

<script setup lang="ts">
  /**
   * @desc 缺陷处理人数量
   */
  import { ref } from 'vue';

  import MsChart from '@/components/pure/chart/index.vue';
  import MsSelect from '@/components/business/ms-select';

  import { useI18n } from '@/hooks/useI18n';

  import { defectStatusColor, getCommonBarOptions } from '../utils';
  import type { SelectOptionData } from '@arco-design/web-vue';

  const { t } = useI18n();

  const memberIds = ref('');
  const projectIds = ref('');

  const memberOptions = ref<SelectOptionData[]>([]);
  const projectOptions = ref<SelectOptionData[]>([]);

  const options = ref<Record<string, any>>({});
  const members = computed(() => ['张三', '李四', '王五', '小王']);
  const hasRoom = computed(() => members.value.length >= 7);
  const seriesData = ref<Record<string, any>[]>([
    {
      name: '已结束',
      type: 'bar',
      barWidth: 12,
      itemStyle: {
        borderRadius: [2, 2, 0, 0],
      },
      data: [400, 200, 400, 200, 400, 200],
    },
    {
      name: '未结束',
      type: 'bar',
      barWidth: 12,
      itemStyle: {
        borderRadius: [2, 2, 0, 0],
      },
      data: [90, 160, 90, 160, 90, 160],
    },
  ]);

  onMounted(() => {
    options.value = getCommonBarOptions(hasRoom.value, defectStatusColor);
    options.value.xAxis.data = members.value;
    options.value.series = seriesData.value;
  });
</script>

<style scoped></style>
