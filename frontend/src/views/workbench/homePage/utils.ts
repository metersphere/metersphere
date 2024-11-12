import { commonConfig, toolTipConfig } from '@/config/testPlan';
import { useI18n } from '@/hooks/useI18n';
import { addCommasToNumber } from '@/utils';

import type { ModuleCardItem } from '@/models/workbench/homePage';
import { WorkCardEnum, WorkOverviewEnum, WorkOverviewIconEnum } from '@/enums/workbenchEnum';

const { t } = useI18n();
// 通用颜色配置
export const commonColorConfig = [
  '#811FA3',
  '#00C261',
  '#3370FF',
  '#AA4FBF',
  '#FFA1FF',
  '#EE50A3',
  '#FF9964',
  '#FFCA59',
  '#F9F871',
  '#C3DD40',
  '#62D256',
  '#14E1C6',
  '#50CEFB',
  '#2B5FD9',
  '#935AF6',
  '#DC9BFF',
  '#D34400',
  '#F4D0BF',
  '#F4D0BF',
  '#0089D1',
  '#0089D1',
  '#62D256',
  '#87F578',
];

// 饼图颜色配置
export const colorMapConfig: Record<string, string[]> = {
  [WorkCardEnum.CASE_COUNT]: ['#ED0303', '#FFA200', '#3370FF', '#D4D4D8'],
  [WorkCardEnum.ASSOCIATE_CASE_COUNT]: ['#00C261', '#3370FF'],
  [WorkCardEnum.REVIEW_CASE_COUNT]: ['#9441B1', '#3370FF', '#00C261', '#D4D4D8'],
  [WorkCardEnum.TEST_PLAN_COUNT]: ['#9441B1', '#3370FF', '#00C261', '#D4D4D8'],
  [WorkCardEnum.PLAN_LEGACY_BUG]: ['#9441B1', '#3370FF', '#00C261', '#D4D4D8'],
  [WorkCardEnum.BUG_COUNT]: ['#FFA200', '#00C261', '#D4D4D8'],
  [WorkCardEnum.HANDLE_BUG_BY_ME]: ['#9441B1', '#3370FF', '#00C261', '#D4D4D8'],
  [WorkCardEnum.CREATE_BY_ME]: ['#9441B1', '#3370FF', '#00C261', '#D4D4D8'],
  [WorkCardEnum.API_COUNT]: ['#811FA3', '#00C261', '#3370FF', '#FFA1FF', '#EE50A3', '#FF9964', '#F9F871', '#C3DD40'],
  [WorkCardEnum.CREATE_BUG_BY_ME]: ['#9441B1', '#3370FF', '#00C261', '#D4D4D8'],
};

// 柱状图
export function getCommonBarOptions(hasRoom: boolean, color: string[]): Record<string, any> {
  return {
    tooltip: [
      {
        trigger: 'axis',
        borderWidth: 0,
        padding: 0,
        label: {
          width: 50,
          overflow: 'truncate',
        },
        displayMode: 'single',
        enterable: true,
        // TODO 单例模式
        // formatter(params: any) {
        //   const html = `
        // <div class="w-[186px] h-[50px] p-[16px] flex items-center justify-between">
        // <div class=" flex items-center">
        // <div class="mb-[2px] mr-[8px] h-[8px] w-[8px] rounded-sm bg-[${params.color}]" style="background:${
        //     params.color
        //   }"></div>
        // <div style="color:#959598">${params.name}</div>
        // </div>
        // <div class="text-[#323233] font-medium">${addCommasToNumber(params.value)}</div>
        // </div>
        // `;
        //   return html;
        // },
        formatter(params: any) {
          const html = `
        <div class="w-[186px] ms-scroll-bar max-h-[206px] overflow-y-auto p-[16px] gap-[8px] flex flex-col">
        ${params
          .map(
            (item: any) => `
          <div class="flex h-[18px] items-center justify-between">
            <div class="flex items-center">
              <div class="mb-[2px] mr-[8px] h-[8px] w-[8px] rounded-sm" style="background:${item.color}"></div>
              <div class="one-line-text max-w-[120px]" style="color:#959598">${item.seriesName}</div>
            </div>
            <div class="text-[#323233] font-medium">${addCommasToNumber(item.value)}</div>
          </div>
        `
          )
          .join('')}
        </div>
      `;
          return html;
        },
      },
    ],
    color,
    grid: {
      top: '36px',
      left: '10px',
      right: '10px',
      bottom: hasRoom ? '54px' : '5px',
      containLabel: true,
    },
    xAxis: {
      splitLine: false,
      boundaryGap: true,
      type: 'category',
      data: [],
      axisLabel: {
        color: '#646466',
        interval: 0,
      },
      axisTick: {
        show: false, // 隐藏刻度线
      },
      axisLine: {
        lineStyle: {
          color: '#EDEDF1',
        },
      },
    },
    yAxis: [
      {
        type: 'value',
        name: '单位：个', // 设置单位
        nameLocation: 'end',
        nameTextStyle: {
          fontSize: 12,
          color: '#AEAEB2', // 自定义字体大小和颜色
          padding: [0, 0, 0, 10], // 通过padding控制Y轴单位距离左侧的距离
        },
        nameGap: 20,
        splitLine: {
          show: true, // 控制是否显示水平线
          lineStyle: {
            color: '#EDEDF1', // 水平线颜色
            width: 1, // 水平线宽度
            type: 'dashed', // 水平线线型，可选 'solid'、'dashed'、'dotted'
          },
        },
      },
    ],
    graphic: {
      type: 'text',
      left: 'center',
      top: 'middle',
      style: {
        text: '',
        fontSize: 14,
        fill: '#959598',
        backgroundColor: '#F9F9FE',
        padding: [6, 16, 6, 16],
        borderRadius: 4,
      },
      invisible: true,
    },

    colorBy: 'series',
    series: [],
    barCategoryGap: '50%', // 控制 X 轴分布居中效果
    legend: {
      width: '60%',
      show: true,
      type: 'scroll',
      itemGap: 20,
      itemWidth: 8,
      itemHeight: 8,
      left: 'center',
      pageButtonItemGap: 5,
      pageButtonGap: 5,
      pageIconColor: '#00000099',
      pageIconInactiveColor: '#00000042',
      pageIconSize: [10, 8],
      pageTextStyle: {
        color: '#00000099',
        fontSize: 12,
      },
    },
    dataZoom: hasRoom
      ? [
          {
            type: 'inside',
          },
          {
            type: 'slider',
          },
        ]
      : [],
  };
}

export const contentTabList = ref<ModuleCardItem[]>([
  {
    label: t('workbench.homePage.functionalUseCase'),
    value: WorkOverviewEnum.FUNCTIONAL,
    icon: WorkOverviewIconEnum.FUNCTIONAL,
    color: 'rgb(var(--primary-5))',
    count: 0,
  },
  {
    label: t('workbench.homePage.useCaseReview'),
    value: WorkOverviewEnum.CASE_REVIEW,
    icon: WorkOverviewIconEnum.CASE_REVIEW,
    color: 'rgb(var(--success-6))',
    count: 0,
  },
  {
    label: t('workbench.homePage.interfaceAPI'),
    value: WorkOverviewEnum.API,
    icon: WorkOverviewIconEnum.API,
    color: 'rgb(var(--link-6))',
    count: 0,
  },
  {
    label: t('workbench.homePage.interfaceCASE'),
    value: WorkOverviewEnum.API_CASE,
    icon: WorkOverviewIconEnum.API_CASE,
    color: 'rgb(var(--link-6))',
    count: 0,
  },
  {
    label: t('workbench.homePage.interfaceScenario'),
    value: WorkOverviewEnum.API_SCENARIO,
    icon: WorkOverviewIconEnum.API_SCENARIO,
    color: 'rgb(var(--link-6))',
    count: 0,
  },
  {
    label: t('workbench.homePage.apiPlan'),
    value: WorkOverviewEnum.TEST_PLAN,
    icon: WorkOverviewIconEnum.TEST_PLAN,
    color: 'rgb(var(--link-6))',
    count: 0,
  },
  {
    label: t('workbench.homePage.bugCount'),
    value: WorkOverviewEnum.BUG_COUNT,
    icon: WorkOverviewIconEnum.BUG_COUNT,
    color: 'rgb(var(--danger-6))',
    count: 0,
  },
]);

// 下方饼图配置
export function getPieCharOptions(key: WorkCardEnum) {
  return {
    title: {
      show: true,
      text: '总数(个)',
      left: 85,
      top: '30%',
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
      textAlign: 'center', // 确保副标题居中
    },
    color: colorMapConfig[key],
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
      padAngle: 2,
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
  };
}

// 空数据和无权限处理
export function handleNoDataDisplay(
  xAxis: string[],
  projectCountList: { id: string; name: string; count: number[] }[]
) {
  if (!xAxis.length) {
    return {
      invisible: false,
      text: t('workbench.homePage.notHasResPermission'),
    };
  }

  const isEmptyData = projectCountList.every((item) =>
    item.count.every((e) => e === 0 || e === null || e === undefined)
  );

  if (isEmptyData) {
    return {
      invisible: false,
      text: t('workbench.homePage.notHasData'),
    };
  }
  return {
    invisible: true,
    text: '',
  };
}

// XX率饼图配置
export const commonRatePieOptions = {
  ...commonConfig,
  title: {
    show: true,
    text: '',
    left: 26,
    top: '20%',
    textStyle: {
      fontSize: 12,
      fontWeight: 'normal',
      color: '#959598',
    },
    triggerEvent: true, // 开启鼠标事件
    subtext: '0',
    subtextStyle: {
      fontSize: 12,
      color: '#323233',
      fontWeight: 'bold',
      align: 'center',
      lineHeight: 3,
    },
    textAlign: 'center',
    tooltip: {
      ...toolTipConfig,
      position: 'right',
    },
  },
  tooltip: {
    ...toolTipConfig,
    position: 'right',
  },
  legend: {
    show: false,
  },
  series: {
    name: '',
    type: 'pie',
    color: [],
    padAngle: 2,
    radius: ['85%', '100%'],
    center: [30, '50%'],
    avoidLabelOverlap: false,
    label: {
      show: false,
      position: 'center',
    },
    emphasis: {
      scale: false, // 禁用放大效果
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
  // graphic: [
  //   {
  //     type: 'text',
  //     left: 'center',
  //     top: '5%',
  //     style: {
  //       text: '饼图标题',
  //       fontSize: 18,
  //       fontWeight: 'bold',
  //       fill: '#333',
  //       cursor: 'pointer'
  //     },
  //     onmouseover (params) {
  //       // 悬浮到标题上时显示提示信息
  //       // chart.dispatchAction({
  //       //   type: 'showTip',
  //       //   position: [params.event.offsetX, params.event.offsetY],
  //       //   // 配置提示内容
  //       //   formatter: '这是饼图标题的提示内容'
  //       // });
  //     },
  //     onmouseout () {
  //       // 离开标题时隐藏提示信息
  //       // chart.dispatchAction({
  //       //   type: 'hideTip'
  //       // });
  //     }
  //   }
  // ]
};

// 统一处理下方饼图数据结构
export function handlePieData(
  key: WorkCardEnum,
  statusPercentList: {
    status: string; // 状态
    count: number;
    percentValue: string; // 百分比
  }[]
) {
  const options: Record<string, any> = getPieCharOptions(key);
  options.series.data = statusPercentList.map((item) => ({
    name: item.status,
    value: item.count,
  }));

  // 计算总数和图例格式
  const tempObject: Record<string, any> = {};
  let totalCount = 0;
  statusPercentList.forEach((item) => {
    tempObject[item.status] = item;
    totalCount += item.count;
  });

  // 设置图例的格式化函数，显示百分比
  options.legend.formatter = (name: string) => {
    return `{a|${tempObject[name].status}}  {b|${addCommasToNumber(tempObject[name].count)}} {c|${
      tempObject[name].percentValue
    }}`;
  };

  // 设置副标题为总数
  options.title.subtext = addCommasToNumber(totalCount);

  return options;
}
