<template>
  <a-spin class="w-full" :loading="props.loading">
    <div class="rate-content relative">
      <div class="relative flex h-full w-full items-center justify-center">
        <a-tooltip
          v-if="props.rateConfig.tooltipText"
          :mouse-enter-delay="500"
          :content="t(props.rateConfig.tooltipText || '')"
          position="bottom"
        >
          <div class="tooltip-rate-tooltip"></div>
        </a-tooltip>
        <MsChart :options="options" width="146px" />
      </div>
    </div>
  </a-spin>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsChart from '@/components/pure/chart/index.vue';

  import { toolTipConfig } from '@/config/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import { addCommasToNumber } from '@/utils';

  const { t } = useI18n();

  const props = defineProps<{
    data: { name: string; value: number }[];
    hasPermission: boolean;
    loading?: boolean;
    rateConfig: {
      name: string;
      color: string[];
      tooltipText: string;
    };
  }>();

  const options = ref<Record<string, any>>({
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
      show: !!props.hasPermission,
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
    graphic: {
      type: 'text',
      left: 'center',
      bottom: 10,
      style: {
        text: t('workbench.homePage.notHasResPermission'),
        fontSize: 14,
        fill: '#959598',
        backgroundColor: '#F9F9FE',
        padding: [6, 16, 6, 16],
        borderRadius: 4,
      },
      invisible: false,
    },
  });

  function initOptions() {
    const { name, color } = props.rateConfig;
    options.value.series.data = [...props.data.slice(1)];

    options.value.legend.formatter = (seriousName: string) => {
      const item = props.data.find((e) => e.name === seriousName);
      return `{a|${seriousName}}  {b|${addCommasToNumber(item?.value || 0)}}`;
    };

    if (props.hasPermission) {
      options.value.title.subtext = `${props.data[0].value ?? 0}%`;
    } else {
      options.value.series.data = [];
      options.value.title.subtext = `-%`;
    }

    options.value.graphic.invisible = !!props.hasPermission;
    options.value.tooltip.show = !!props.hasPermission;
    options.value.title.text = name;

    options.value.series.color = color;
  }

  watch(
    () => props.data,
    (val) => {
      if (val) {
        initOptions();
      }
    }
  );

  watch(
    () => props.hasPermission,
    () => {
      initOptions();
    }
  );

  onMounted(() => {
    initOptions();
  });
</script>

<style scoped lang="less">
  .rate-content {
    height: 158px;
  }
  .tooltip-rate-tooltip {
    position: absolute;
    top: 10px;
    z-index: 9;
    width: 78px;
    height: 78px;
    border-radius: 50%;
  }
</style>
