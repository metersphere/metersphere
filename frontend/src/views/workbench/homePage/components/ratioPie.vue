<template>
  <div class="rate-content">
    <MsChart :options="options" width="200px" />
  </div>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsChart from '@/components/pure/chart/index.vue';

  import { toolTipConfig } from '@/config/testPlan';
  import { addCommasToNumber } from '@/utils';

  const props = defineProps<{
    data: { name: string; value: number }[];
    rateConfig: {
      name: string;
      count: string;
      color: string[];
    };
  }>();

  const commonOptionConfig = ref({
    title: {
      show: true,
      text: '',
      left: 'center',
      top: 32,
      textStyle: {
        fontSize: 12,
        fontWeight: 'normal',
        color: '#959598',
      },
      subtext: '',
      subtextStyle: {
        fontSize: 14,
        color: '#323233',
        fontWeight: 'bold',
        lineHeight: 3,
      },
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: 40,
    },
    tooltip: {
      ...toolTipConfig,
    },
    legend: {
      orient: 'vertical',
      itemWidth: 8, // 图例标记的宽度
      itemHeight: 8, // 图例标记的高度
      icon: 'circle', // 图例标记的形状
      textStyle: {
        color: '#333', // 图例文本颜色
        fontSize: 14, // 字体大小
      },
      y: 'bottom',
      bottom: 'center',
      formatter: (name: any) => {
        return `{a|${name}}  {b|${addCommasToNumber(1022220)}}`;
      },
    },
    textStyle: {
      rich: {
        a: {
          width: 60,
          color: '#959598', // 状态名称颜色
          fontSize: 12,
        },
        b: {
          width: 60,
          color: '#783887',
          fontSize: 16,
          fontWeight: 'bold',
          padding: [0, 0, 0, 16],
        },
      },
    },
    series: {
      name: '',
      type: 'pie',
      padAngle: 1,
      radius: ['50%', '58%'],
      center: ['50%', '32%'],
      color: [],
      avoidLabelOverlap: false,
      label: {
        show: false,
        position: 'center',
      },
      emphasis: {
        label: {
          show: false,
          fontSize: 40,
          fontWeight: 'bold',
        },
      },
      labelLine: {
        show: false,
      },
      data: [],
    },
  });
  const options = ref({});

  function initOptions() {
    const { name, count, color } = props.rateConfig;
    options.value = {
      ...commonOptionConfig.value,
      title: {
        ...commonOptionConfig.value.title,
        text: name,
        subtext: count,
      },
      series: {
        ...commonOptionConfig.value.series,
        data: [...props.data],
        color,
      },
    };
  }

  onMounted(() => {
    initOptions();
  });
</script>

<style scoped lang="less">
  .rate-content {
    height: 152px;
  }
</style>
