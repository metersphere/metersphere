<template>
  <div class="card-wrapper api-count-wrapper">
    <div class="flex items-center justify-between">
      <div class="title"> {{ t('workbench.homePage.apiCount') }} </div>
      <div>
        <MsSelect
          v-model:model-value="projectIds"
          :options="appStore.projectList"
          allow-clear
          allow-search
          value-key="id"
          label-key="name"
          :search-keys="['name']"
          class="!w-[240px]"
          :prefix="t('workbench.homePage.project')"
        >
        </MsSelect>
      </div>
    </div>
    <div class="my-[16px]">
      <div class="case-count-wrapper">
        <div class="case-count-item">
          <PassRatePie :options="options" :size="60" :value-list="coverValueList" />
        </div>
        <div class="case-count-item">
          <PassRatePie :options="options" :size="60" :value-list="passValueList" />
        </div>
      </div>
      <div class="mt-[16px] h-[148px]">
        <MsChart :options="apiCountOptions" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
  /** *
   * @desc 接口数量
   */
  import { ref } from 'vue';

  import MsChart from '@/components/pure/chart/index.vue';
  import MsSelect from '@/components/business/ms-select';
  import PassRatePie from './passRatePie.vue';

  import { commonConfig, toolTipConfig } from '@/config/testPlan';
  import { useI18n } from '@/hooks/useI18n';
  import useAppStore from '@/store/modules/app';
  import { addCommasToNumber } from '@/utils';

  import type { SelectOptionData } from '@arco-design/web-vue';

  const { t } = useI18n();
  const appStore = useAppStore();

  const projectIds = ref('');
  const projectOptions = ref<SelectOptionData[]>([]);

  const options = ref({
    ...commonConfig,
    tooltip: {
      ...toolTipConfig,
    },
    legend: {
      show: false,
    },
    series: {
      name: '',
      type: 'pie',
      radius: ['80%', '100%'],
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

  const coverValueList = ref([
    {
      label: t('workbench.homePage.covered'),
      value: 10000,
    },
    {
      label: t('workbench.homePage.notCover'),
      value: 2000,
    },
  ]);
  const passValueList = ref([
    {
      label: t('common.completed'),
      value: 10000,
    },
    {
      label: t('common.inProgress'),
      value: 2000,
    },
    {
      label: t('workbench.homePage.unFinish'),
      value: 2000,
    },
  ]);

  const apiCountOptions = ref({
    title: {
      show: true,
      text: '总数(个)',
      left: 60,
      top: '38px',
      textStyle: {
        fontSize: 12,
        fontWeight: 'normal',
        color: '#959598',
      },
      subtext: '100111',
      subtextStyle: {
        fontSize: 20,
        color: '#323233',
        fontWeight: 'bold',
        align: 'center',
      },
    },
    color: ['#811FA3', '#00C261', '#3370FF', '#FFA1FF', '#EE50A3', '#FF9964', '#F9F871', '#C3DD40'],
    tooltip: {
      ...toolTipConfig,
      position: 'right',
    },
    legend: {
      width: '100%',
      height: 128,
      type: 'scroll',
      orient: 'vertical',
      pageButtonItemGap: 5,
      pageButtonGap: 5,
      pageIconColor: '#00000099',
      pageIconInactiveColor: '#00000042',
      pageIconSize: [7, 5],
      pageTextStyle: {
        color: '#00000099',
        fontSize: 12,
      },
      pageButtonPosition: 'end',
      itemGap: 16,
      itemWidth: 8,
      itemHeight: 8,
      icon: 'circle',
      bottom: 'center',
      left: 180,
      formatter: (name: any) => {
        return `{a|${name}}  {b|${addCommasToNumber(1022220)}} {c|${10}}`;
      },
      textStyle: {
        color: '#333',
        fontSize: 14, // 字体大小
        textBorderType: 'solid',
        rich: {
          a: {
            width: 50,
            color: '#959598',
            fontSize: 12,
            align: 'left',
          },
          b: {
            width: 50,
            color: '#323233',
            fontSize: 12,
            fontWeight: 'bold',
            align: 'right',
          },
          c: {
            width: 50,
            color: '#323233',
            fontSize: 12,
            fontWeight: 'bold',
            align: 'right',
          },
        },
      },
    },
    media: [
      {
        query: { maxWidth: 600 },
        option: {
          legend: {
            textStyle: {
              width: 200,
            },
          },
        },
      },
      {
        query: { minWidth: 601, maxWidth: 800 },
        option: {
          legend: {
            textStyle: {
              width: 450,
            },
          },
        },
      },
      {
        query: { minWidth: 801, maxWidth: 1200 },
        option: {
          legend: {
            textStyle: {
              width: 600,
            },
          },
        },
      },
      {
        query: { minWidth: 1201 },
        option: {
          legend: {
            textStyle: {
              width: 1000,
            },
          },
        },
      },
    ],
    series: {
      name: '',
      type: 'pie',
      radius: ['75%', '90%'],
      center: [90, '48%'],
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
</script>

<style scoped lang="less">
  .card-wrapper.api-count-wrapper {
    padding-bottom: 4px;
  }
</style>
