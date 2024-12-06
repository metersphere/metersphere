<template>
  <VCharts v-if="chartId" ref="chartRef" :option="options" :autoresize="autoResize" :style="{ width, height }" />
</template>

<script lang="ts" setup>
  import { ref } from 'vue';

  import { getGenerateId } from '@/utils';

  import { BarChart, CustomChart, LineChart, PieChart, RadarChart } from 'echarts/charts';
  import {
    DataZoomComponent,
    GraphicComponent,
    GridComponent,
    LegendComponent,
    TitleComponent,
    TooltipComponent,
  } from 'echarts/components';
  import { use } from 'echarts/core';
  import { CanvasRenderer } from 'echarts/renderers';
  import VCharts from 'vue-echarts';

  use([
    CanvasRenderer,
    BarChart,
    CustomChart,
    LineChart,
    PieChart,
    RadarChart,
    GridComponent,
    TooltipComponent,
    LegendComponent,
    DataZoomComponent,
    GraphicComponent,
    TitleComponent,
  ]);

  defineProps({
    options: {
      type: Object,
      default() {
        return {};
      },
    },
    autoResize: {
      type: Boolean,
      default: true,
    },
    width: {
      type: String,
      default: '100%',
    },
    height: {
      type: String,
      default: '100%',
    },
  });

  const chartRef = ref<InstanceType<typeof VCharts>>();

  const chartId = ref('');
  onMounted(() => {
    chartId.value = getGenerateId();
  });

  onUnmounted(() => {
    chartId.value = '';
  });

  defineExpose({
    chartRef,
  });
</script>
