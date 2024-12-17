<template>
  <a-spin class="w-full" :loading="props.loading">
    <div class="rate-content relative flex flex-col items-center">
      <div class="relative flex h-full w-full items-center justify-center">
        <a-tooltip
          v-if="props.rateConfig.tooltipText"
          :mouse-enter-delay="500"
          :content="t(props.rateConfig.tooltipText || '')"
          position="bottom"
        >
          <div class="tooltip-rate-tooltip"></div>
        </a-tooltip>
        <MsChart ref="chartRef" :options="options" width="100px" height="100px" />
      </div>
      <div class="flex w-full items-center justify-center">
        <div class="flex w-[80%] flex-col gap-[8px]">
          <template v-if="props.hasPermission">
            <div
              v-for="(ele, i) of legend"
              :key="`${ele.name}-${i}`"
              class="flex justify-between"
              @mouseover="handleMouseOver(ele.name)"
              @mouseout="handleMouseOut(ele.name)"
            >
              <div class="flex items-center text-left text-[var(--color-text-3)]">
                <div
                  :style="{
                    background: ele.selected ? `${ele.color}` : '#D4D4D8',
                  }"
                  class="mr-[8px] h-[8px] w-[8px] cursor-pointer rounded-lg"
                  @click="toggleLegend(ele.name, i)"
                >
                </div>
                {{ ele.name }}
              </div>
              <div class="cursor-pointer text-[16px] text-[rgb(var(--primary-5))]" @click="goNavigation(ele)">
                {{ addCommasToNumber(ele.value) }}
              </div>
            </div>
          </template>
          <div v-else class="mt-[16px] flex h-full flex-1 items-center justify-center">
            <div class="rounded bg-[var(--color-text-n9)] px-[16px] py-[4px] text-[var(--color-text-4)]">
              {{ t('workbench.homePage.notHasResPermission') }}
            </div>
          </div>
        </div>
      </div>
    </div>
  </a-spin>
</template>

<script setup lang="ts">
  import { ref } from 'vue';

  import MsChart from '@/components/pure/chart/index.vue';

  import getVisualThemeColor from '@/config/chartTheme';
  import { toolTipConfig } from '@/config/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useOpenNewPage from '@/hooks/useOpenNewPage';
  import { addCommasToNumber } from '@/utils';

  import type { StatusValueItem } from '@/models/workbench/homePage';
  import { WorkCardEnum } from '@/enums/workbenchEnum';

  const { t } = useI18n();
  const { openNewPage } = useOpenNewPage();

  const props = defineProps<{
    data: StatusValueItem[];
    hasPermission: boolean;
    valueKey: WorkCardEnum;
    loading?: boolean;
    projectId: string;
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
      top: 30,
      textStyle: {
        fontSize: 12,
        fontWeight: 'normal',
        color: '#959598',
      },
      subtext: '',
      subtextStyle: {
        fontSize: 14,
        color: getVisualThemeColor('subtextStyleColor'),
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
      show: true,
    },
    color: [],
    legend: {
      show: false,
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

      tooltip: {
        show: false,
      },
    },
    series: {
      name: '',
      type: 'pie',
      radius: ['75%', '90%'],
      center: ['50%', '50%'],
      color: [],
      minAngle: 5,
      minShowLabelAngle: 10,
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

  const legend = ref<{ name: string; value: number; color: string; selected: boolean }[]>([]);

  function initOptions() {
    const { name, color } = props.rateConfig;
    const temData = [...props.data.slice(1)];
    const hasDataLength = temData.filter((e) => Number(e.value) > 0).length;

    const pieBorderWidth = hasDataLength === 1 ? 0 : 1;

    const seriesData = temData
      .map((e, i) => {
        return {
          ...e,
          tooltip: {
            ...toolTipConfig,
            show: !!props.hasPermission,
          },
          itemStyle: {
            color: color[i],
            borderWidth: pieBorderWidth,
            borderColor: getVisualThemeColor('itemStyleBorderColor'),
          },
        };
      })
      .filter((e) => e.value !== 0);

    options.value.series.data =
      hasDataLength > 0
        ? seriesData
        : [
            {
              name: '',
              value: 1,
              tooltip: {
                show: false,
              },
              itemStyle: {
                color: getVisualThemeColor('initItemStyleColor'),
              },
            },
          ];
    legend.value = [...props.data.slice(1)].map((e, i) => {
      return {
        ...e,
        selected: true,
        color: color[i],
      };
    });

    options.value.legend.formatter = (seriousName: string) => {
      const item = props.data.find((e) => e.name === seriousName);
      return `{a|${seriousName}}  {b|${addCommasToNumber(item?.value || 0)}}`;
    };

    if (props.hasPermission) {
      options.value.title.subtext = `${props.data[0].value ?? 0}%`;
    } else {
      options.value.series.data = [
        {
          name: '',
          value: 1,
          tooltip: {
            show: false,
          },
          itemStyle: {
            color: getVisualThemeColor('initItemStyleColor'),
          },
        },
      ];
      options.value.title.subtext = `-%`;
    }

    options.value.tooltip.show = !!props.hasPermission;
    options.value.title.text = name;
  }

  const chartRef = ref<InstanceType<typeof MsChart>>();
  function toggleLegend(name: string, index: number) {
    const chart = chartRef.value?.chartRef;
    if (chart) {
      chart.dispatchAction({
        type: 'legendToggleSelect',
        name,
      });

      if (legend.value) {
        legend.value[index].selected = !legend.value[index].selected;
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

  function goNavigation(item: StatusValueItem) {
    const params: Record<string, any> = {
      pId: props.projectId,
      home: item.status,
    };
    if (item.tab) {
      params.tab = item.tab;
    }
    openNewPage(item.route, params);
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
  .tooltip-rate-tooltip {
    position: absolute;
    top: 20px;
    z-index: 9;
    width: 60px;
    height: 60px;
    border-radius: 50%;
  }
</style>
