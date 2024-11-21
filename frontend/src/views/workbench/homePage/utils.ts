import { cloneDeep } from 'lodash-es';

import { toolTipConfig } from '@/config/testPlan';
import { commonRatePieOptions, defaultValueMap } from '@/config/workbench';
import { useI18n } from '@/hooks/useI18n';
import { addCommasToNumber } from '@/utils';

import { WorkCardEnum } from '@/enums/workbenchEnum';

const { t } = useI18n();
// 通用颜色配置
const commonColorConfig: Record<number, string[]> = {
  2: ['#783887', '#FFC14E'],
  4: ['#783887', '#FFC14E', '#2DFCEF', '#3370FF'],
  8: ['#783887', '#FFC14E', '#2DFCEF', '#3370FF', '#811FA3', '#00D1FF', '#FFA53D', '#00C261'],
  12: [
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
    '#3370FF',
    '#2B5FD9',
  ],
  13: [
    '#AA4FBF',
    '#FFA1FF',
    '#FFCA59',
    '#F9F871',
    '#14E1C6',
    '#2DFCEF',
    '#2B5FD9',
    '#00D1FF',
    '#935AF6',
    '#DC9BFF',
    '#FF9964',
    '#FFC75E',
    '#D34400',
    '#F4D0BF',
    '#EE50A3',
    '#FBD3E8',
    '#C3DD40',
    '#D9F457',
    '#0089D1',
    '#62D256',
    '#87F578',
  ],
};

export function getColorScheme(dataLength: number): string[] {
  if (dataLength <= 2) return commonColorConfig[2];
  if (dataLength <= 4) return commonColorConfig[4];
  if (dataLength <= 8) return commonColorConfig[8];
  if (dataLength <= 12) return commonColorConfig[12];
  return commonColorConfig[13];
}

// 饼图颜色配置
export const colorMapConfig: Record<string, string[]> = {
  [WorkCardEnum.CASE_COUNT]: ['#ED0303', '#FFA200', '#3370FF', '#D4D4D8'],
  [WorkCardEnum.ASSOCIATE_CASE_COUNT]: ['#00C261', '#3370FF'],
  [WorkCardEnum.REVIEW_CASE_COUNT]: ['#D4D4D8', '#3370FF', '#00C261', '#ED0303', '#FFA200'],
  [WorkCardEnum.TEST_PLAN_COUNT]: ['#9441B1', '#3370FF', '#00C261', '#D4D4D8'],
  [WorkCardEnum.PLAN_LEGACY_BUG]: ['#FFA200', '#3370FF', '#D4D4D8', '#00C261', ...getColorScheme(13)],
  [WorkCardEnum.BUG_COUNT]: ['#FFA200', '#3370FF', '#D4D4D8', '#00C261', ...getColorScheme(13)],
  [WorkCardEnum.HANDLE_BUG_BY_ME]: ['#FFA200', '#3370FF', '#D4D4D8', '#00C261', ...getColorScheme(13)],
  [WorkCardEnum.CREATE_BY_ME]: ['#9441B1', '#3370FF', '#00C261', '#D4D4D8'],
  [WorkCardEnum.API_COUNT]: ['#811FA3', '#00C261', '#3370FF', '#FFA1FF', '#EE50A3', '#FF9964', '#F9F871', '#C3DD40'],
  [WorkCardEnum.CREATE_BUG_BY_ME]: ['#FFA200', '#3370FF', '#D4D4D8', '#00C261', ...getColorScheme(13)],
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
        showDelay: 0,
        hideDelay: 100,
        triggerOn: 'mousemove',
        displayMode: 'single',
        enterable: true,
        axisPointer: {
          type: 'shadow',
          axis: 'auto',
        },
        formatter(params: any) {
          const html = `
        <div class="w-[186px] ms-scroll-bar max-h-[206px] overflow-y-auto p-[16px] gap-[8px] flex flex-col">
        ${params
          .map(
            (item: any) => `
          <div class="flex h-[18px] items-center justify-between">
            <div class="flex items-center">
              <div class="mb-[2px] mr-[8px] h-[8px] w-[8px] rounded-sm" style="background:${item.color}"></div>
              <div class="one-line-text max-w-[100px]" style="color:#959598">${item.seriesName}</div>
            </div>
            <div class="text-[#323233] font-medium">${addCommasToNumber(item.data.originValue || 0)}</div>
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
      top: 36,
      left: 0,
      right: 0,
      bottom: hasRoom ? 44 : 5,
      containLabel: true,
    },
    xAxis: {
      show: true,
      splitLine: false,
      boundaryGap: true,
      type: 'category',
      data: [],
      axisLabel: {
        color: '#646466',
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
        alignTicks: true,
        name: t('workbench.homePage.unit'), // 设置单位
        position: 'left',
        axisLine: {
          show: false,
          onZero: false, // 禁止 Y 轴强制显示在 0 位置
        },
        offset: 0, // 第一个 Y 轴默认位置
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
        min: 0,
        max: 100,
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
      textStyle: {
        width: 150,
        overflow: 'truncate',
        ellipsis: '...',
      },
      formatter(name: string) {
        return name;
      },
      tooltip: {
        show: true,
        padding: 16,
        position(point: any) {
          return [point[0], '10%'];
        },
        extraCssText: 'max-width: 300px; white-space: normal; word-wrap: break-word; word-break: break-all;',
        textStyle: {
          width: 300,
          fontSize: 12,
          overflow: 'breakAll',
        },
      },
    },
    dataZoom: hasRoom
      ? [
          {
            type: 'slider',
            height: 24,
            bottom: 10,
            // 按照坐标值显示起始位置
            startValue: 0,
            rangeMode: ['value', 'percent'], // 起点按实际值，终点按百分比动态计算
            showDataShadow: 'auto',
            showDetail: false,
            filterMode: 'filter',
            moveOnMouseMove: true,
            handleSize: 30, // 手柄的大小
            moveHandleSize: 0,
            handleStyle: {
              color: '#fff',
              borderColor: 'rgba(24, 24, 24, 0.15)',
              borderWidth: 1,
            },
            backgroundColor: 'rgba(241, 241, 241, 0.6)',
            borderColor: 'transparent',
            dataBackground: {
              lineStyle: {
                width: 0,
              },
            },
            selectedDataBackground: {
              lineStyle: {
                width: 0,
              },
            },

            moveHandleStyle: {
              color: '#fff',
              opacity: 0,
            },
          },
        ]
      : [],
  };
}

// 下方饼图配置
export function getPieCharOptions(key: WorkCardEnum, hasPermission: boolean) {
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
    tooltip: { show: true },
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
      tooltip: {
        show: false, // 禁用图例的 tooltip
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
    graphic: {
      type: 'text',
      left: 'center',
      top: 'middle',
      style: {
        text: t('workbench.homePage.notHasResPermission'),
        fontSize: 14,
        fill: '#959598',
        backgroundColor: '#F9F9FE',
        padding: [6, 16, 6, 16],
        borderRadius: 4,
      },
      invisible: !!hasPermission,
    },
  };
}

// 空数据和无权限处理
export function handleNoDataDisplay(xAxis: string[], hasPermission: boolean) {
  if (!hasPermission) {
    return {
      invisible: false,
      text: t('workbench.homePage.notHasResPermission'),
    };
  }

  if (!xAxis.length) {
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

// 统一处理下方饼图数据结构
export function handlePieData(
  key: WorkCardEnum,
  hasPermission: boolean,
  statusPercentList:
    | {
        status: string; // 状态
        count: number;
        percentValue: string; // 百分比
      }[]
    | null = []
) {
  const options: Record<string, any> = getPieCharOptions(key, hasPermission);
  const lastStatusPercentList = statusPercentList ?? [];
  options.series.data = lastStatusPercentList.map((item) => ({
    name: item.status,
    value: item.count,
    tooltip: {
      ...toolTipConfig,
      position: 'right',
      show: !!hasPermission,
    },
  }));

  // 计算总数和图例格式
  const tempObject: Record<string, any> = {};
  let totalCount = 0;
  lastStatusPercentList.forEach((item) => {
    tempObject[item.status] = item;
    totalCount += item.count;
  });

  // 设置副标题为总数
  options.title.subtext = addCommasToNumber(totalCount);
  if (!hasPermission) {
    options.title.subtext = '-';
    options.series.data = [];
  }

  // 设置图例的格式化函数，显示百分比
  options.legend.formatter = (name: string) => {
    return `{a|${tempObject[name].status}}  {b|${addCommasToNumber(tempObject[name].count)}} {c|${
      tempObject[name].percentValue
    }}`;
  };

  return options;
}

// 更新options
export function handleUpdateTabPie(
  list: {
    name: string;
    count: number;
  }[],
  hasPermission: boolean, // 是否有权限
  key: string
) {
  const options: Record<string, any> = cloneDeep(commonRatePieOptions);
  const typeKey = key.split('-')[0];
  const valueKey = key.split('-')[1];
  const countList = list || [];
  let lastCountList: { value: number | string; label: string; name: string }[] = [];
  if (hasPermission) {
    lastCountList = countList.slice(1).map((item) => {
      return {
        value: item.count,
        label: item.name,
        name: item.name,
      };
    });
    options.series.data = lastCountList.every((e) => e.value === 0) ? [] : lastCountList;

    options.title.text = countList[0].name ?? '';
    options.title.subtext = `${countList[0].count ?? 0}%`;
  } else {
    options.series.data = [];
    lastCountList = defaultValueMap[typeKey][valueKey].defaultList.map((e: any) => {
      return {
        ...e,
        label: t(e.label),
      };
    });
    options.title.text = t(defaultValueMap[typeKey][valueKey].defaultName);
    options.title.subtext = '-%';
  }

  options.series.color = defaultValueMap[typeKey][valueKey].color;

  return {
    valueList: lastCountList,
    options,
  };
}
