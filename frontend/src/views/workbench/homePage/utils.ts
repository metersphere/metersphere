import { cloneDeep } from 'lodash-es';

import { toolTipConfig } from '@/config/testPlan';
import { commonRatePieOptions, defaultValueMap } from '@/config/workbench';
import { useI18n } from '@/hooks/useI18n';
import { addCommasToNumber } from '@/utils';

import type { ModuleCardItem, OverViewOfProject } from '@/models/workbench/homePage';
import { RouteEnum } from '@/enums/routeEnum';
import { WorkCardEnum, WorkNavValueEnum } from '@/enums/workbenchEnum';

const { t } = useI18n();
// TODO 通用颜色配置注: 目前柱状图只用到了7种色阶，其他色阶暂时保留
const commonColorConfig: Record<number, string[]> = {
  2: ['#783887', '#FFC14E'],
  4: ['#783887', '#FFC14E', '#2DFCEF', '#3370FF'],
  7: ['#811FA3', '#00C261', '#FF9964', '#50CEFB', '#EE50A3', '#3370FF', '#D34400'],
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
  if (dataLength <= 7) return commonColorConfig[7];
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
export function getCommonBarOptions(hasRoom: boolean, color: string[], isTestPlan = false): Record<string, any> {
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
          let testPlanHtml = '';
          if (isTestPlan) {
            const [assigning, complete] = params;
            const passRate = assigning.value > 0 ? `${((complete.value / assigning.value) * 100).toFixed(2)}%` : '0%';
            testPlanHtml = `<div class="flex items-center justify-between">
                              <div class="flex items-center gap-[8px] text-[var(--color-text-2)]">
                                <div style="background:#00C261;" class="flex items-center justify-center w-[11px] h-[11px] rounded-full text-[10px]">
                                  <span class="text-[var(--color-text-fff)] text-center">✓</span>
                                </div>
                                   ${t('workbench.homePage.completeRate')}
                              </div>
                             
                              <div class="text-[rgb(var(--success-6))] font-semibold">${passRate}</div>
                            </div>`;
          }
          const html = `
        <div class="w-[186px] ms-scroll-bar max-h-[236px] overflow-y-auto p-[16px] gap-[8px] flex flex-col">
        <div class="font-medium max-w-[150px] one-line-text" style="color:#323233">${params[0].axisValueLabel}</div>
        ${testPlanHtml}
        ${params
          .map(
            (item: any) => `
          <div class="flex h-[18px] items-center justify-between">
            <div class="flex items-center">
              <div class="mb-[2px] mr-[8px] h-[8px] w-[8px] rounded-sm" style="background:${item.color}"></div>
              <div class="one-line-text max-w-[100px] text-[var(--color-text-2)]">${item.seriesName}</div>
            </div>
            <div class="text-[var(--color-text-1)] font-semibold">${addCommasToNumber(item.data.originValue || 0)}</div>
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
        show: true,
        color: '#646466',
        width: 120,
        overflow: 'truncate',
        ellipsis: '...',
        showMinLabel: true,
        showMaxLabel: true,
        // TOTO 等待优化
        interval: 0,
        triggerEvent: true,
      },
      axisPointer: {
        type: 'shadow',
      },
      axisTick: {
        alignWithLabel: true,
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
            // TODO 待优化
            minSpan: 1,
            maxSpan: 26,
            startValue: 0,
            end: 30,
            rangeMode: ['value', 'percent'], // 起点按实际值，终点按百分比动态计算
            showDataShadow: 'auto',
            showDetail: false,
            filterMode: 'none',
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
    tooltip: { show: true },
    legend: {
      show: false,
    },
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
  const options: Record<string, any> = getPieCharOptions(key);
  const lastStatusPercentList = statusPercentList ?? [];
  const hasDataLength = lastStatusPercentList.filter((e) => Number(e.count) > 0).length;
  const pieBorderWidth = hasDataLength === 1 ? 0 : 2;

  options.series.data =
    hasDataLength > 0
      ? lastStatusPercentList.map((item) => ({
          name: item.status,
          value: item.count,
          tooltip: {
            ...toolTipConfig,
            position: 'right',
            show: !!hasPermission,
          },
          itemStyle: {
            borderWidth: pieBorderWidth,
            borderColor: '#ffffff',
          },
        }))
      : [];

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

  return options;
}

export const routeNavigationMap: Record<string, any> = {
  // 功能用例数
  [WorkCardEnum.CASE_COUNT]: {
    review: {
      status: [WorkNavValueEnum.CASE_REVIEWED, WorkNavValueEnum.CASE_UN_REVIEWED],
      route: RouteEnum.CASE_MANAGEMENT,
    },
    pass: {
      status: [WorkNavValueEnum.CASE_REVIEWED_PASS, WorkNavValueEnum.CASE_REVIEWED_UN_PASS],
      route: RouteEnum.CASE_MANAGEMENT,
    },
  },
  // 用例评审数
  [WorkCardEnum.REVIEW_CASE_COUNT]: {
    cover: {
      status: [WorkNavValueEnum.CASE_REVIEWED, WorkNavValueEnum.CASE_UN_REVIEWED],
      route: RouteEnum.CASE_MANAGEMENT,
    },
  },
  // 关联用例数
  [WorkCardEnum.ASSOCIATE_CASE_COUNT]: {
    cover: {
      status: [WorkNavValueEnum.CASE_ASSOCIATED, WorkNavValueEnum.CASE_NOT_ASSOCIATED],
      route: RouteEnum.CASE_MANAGEMENT,
    },
  },
  // 接口数量
  [WorkCardEnum.API_COUNT]: {
    cover: {
      status: [
        WorkNavValueEnum.API_COUNT_COVER, // 已覆盖
        WorkNavValueEnum.API_COUNT_UN_COVER, // 未覆盖
      ],
      route: RouteEnum.API_TEST_MANAGEMENT,
    },
    complete: {
      status: [
        WorkNavValueEnum.API_COUNT_DONE, // 已完成
        WorkNavValueEnum.API_COUNT_PROCESSING, // 进行中
        WorkNavValueEnum.API_COUNT_DEBUGGING, // 联调中
        WorkNavValueEnum.API_COUNT_DEPRECATED, // 已废弃
      ],
      route: RouteEnum.API_TEST_MANAGEMENT,
    },
  },
  // 接口用例
  [WorkCardEnum.API_CASE_COUNT]: {
    cover: {
      status: [
        WorkNavValueEnum.API_CASE_COUNT_UN_COVER, // 未覆盖
        WorkNavValueEnum.API_CASE_COUNT_COVER, // 已覆盖
      ],
      route: RouteEnum.API_TEST_MANAGEMENT,
    },
    passRate: {
      status: [
        WorkNavValueEnum.API_COUNT_EXECUTE_ERROR, // 执行结果-未通过
        WorkNavValueEnum.API_COUNT_EXECUTE_SUCCESS, // 执行结果-已通过
      ],
      route: RouteEnum.API_TEST_MANAGEMENT,
    },
    executeRate: {
      status: [
        WorkNavValueEnum.API_COUNT_EXECUTED_NOT_RESULT, //  接口用例-无执行结果
        WorkNavValueEnum.API_COUNT_EXECUTED_RESULT, // 接口用例-有执行结果
      ],
      route: RouteEnum.API_TEST_MANAGEMENT,
    },
  },
  // 场景用例
  [WorkCardEnum.SCENARIO_COUNT]: {
    cover: {
      status: [
        WorkNavValueEnum.SCENARIO_UN_COVER, // 未覆盖
        WorkNavValueEnum.SCENARIO_COVER, // 已覆盖
      ],
      route: RouteEnum.API_TEST_MANAGEMENT,
    },
    passRate: {
      status: [
        WorkNavValueEnum.SCENARIO_COUNT_EXECUTE_ERROR, // 场景用例-执行结果-未通过
        WorkNavValueEnum.SCENARIO_COUNT_EXECUTE_SUCCESS, // 场景用例-执行结果-已通过
      ],
      route: RouteEnum.API_TEST_SCENARIO,
    },
    executeRate: {
      status: [
        WorkNavValueEnum.SCENARIO_COUNT_EXECUTED_NOT_RESULT, //  场景用例-无执行结果
        WorkNavValueEnum.SCENARIO_COUNT_EXECUTED_RESULT, // 场景用例-有执行结果
      ],
      route: RouteEnum.API_TEST_SCENARIO,
    },
  },
  // 测试计划数量
  [WorkCardEnum.TEST_PLAN_COUNT]: {
    pass: {
      status: [WorkNavValueEnum.TEST_PLAN_PASSED, WorkNavValueEnum.TEST_PLAN_NOT_PASS],
      route: RouteEnum.TEST_PLAN_INDEX,
    },
    complete: {
      status: [
        WorkNavValueEnum.TEST_PLAN_COMPLETED, //   测试计划-已完成
        WorkNavValueEnum.TEST_PLAN_UNDERWAY, // 测试计划-进行中
        WorkNavValueEnum.TEST_PLAN_PREPARED, // 测试计划-未开始
        WorkNavValueEnum.TEST_PLAN_ARCHIVED, // 测试计划-已归档
      ],
      route: RouteEnum.TEST_PLAN_INDEX,
    },
  },
  [WorkCardEnum.PLAN_LEGACY_BUG]: {
    legacy: {
      status: [WorkNavValueEnum.TEST_PLAN_LEGACY, WorkNavValueEnum.TEST_PLAN_BUG],
      route: RouteEnum.BUG_MANAGEMENT_INDEX,
    },
  },
  [WorkCardEnum.BUG_COUNT]: {
    legacy: {
      status: [WorkNavValueEnum.BUG_COUNT, WorkNavValueEnum.BUG_COUNT_LEGACY],
      route: RouteEnum.BUG_MANAGEMENT_INDEX,
    },
  },
  [WorkCardEnum.CREATE_BUG_BY_ME]: {
    legacy: {
      status: [WorkNavValueEnum.BUG_COUNT_BY_ME, WorkNavValueEnum.BUG_COUNT_BY_ME_LEGACY],
      route: RouteEnum.BUG_MANAGEMENT_INDEX,
    },
  },
  [WorkCardEnum.HANDLE_BUG_BY_ME]: {
    legacy: {
      status: [WorkNavValueEnum.BUG_HANDLE_BY_ME, WorkNavValueEnum.BUG_HANDLE_BY_ME_LEGACY],
      route: RouteEnum.BUG_MANAGEMENT_INDEX,
    },
  },
};

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
    const pieBorderWidth = countList.slice(1).filter((e) => Number(e.count) > 0).length === 1 ? 0 : 1;

    lastCountList = countList.slice(1).map((item) => {
      return {
        value: item.count,
        label: item.name,
        name: item.name,
        itemStyle: {
          borderWidth: pieBorderWidth,
          borderColor: '#ffffff',
        },
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

  const lastValueList = lastCountList.map((item, index) => {
    return {
      ...item,
      route: routeNavigationMap[typeKey][valueKey].route,
      status: routeNavigationMap[typeKey][valueKey].status[index],
    };
  });

  return {
    valueList: lastValueList,
    options,
  };
}

export function getSeriesData(
  contentTabList: ModuleCardItem[],
  detail: OverViewOfProject,
  colorConfig: string[],
  isTestPlan = false
) {
  let options: Record<string, any> = {};

  const { projectCountList, xaxis, errorCode } = detail;
  const hasPermission = errorCode !== 109001;

  options = getCommonBarOptions(xaxis.length >= 7, colorConfig, isTestPlan);
  options.xAxis.data = xaxis;
  const { invisible, text } = handleNoDataDisplay(xaxis, hasPermission);
  options.graphic.invisible = invisible;
  options.graphic.style.text = text;

  let maxAxis = 5;
  const seriesData = projectCountList.map((item, sid) => {
    const countData: Record<string, any>[] = item.count.map((e) => {
      return {
        name: t(contentTabList[sid].label),
        value: e,
        originValue: e,
        tooltip: {
          show: true,
          trigger: 'item',
          enterable: true,
          formatter(params: any) {
            const html = `
                <div class="w-[186px] h-[50px] p-[16px] flex items-center justify-between">
                <div class=" flex items-center">
                <div class="mb-[2px] mr-[8px] h-[8px] w-[8px] rounded-sm bg-[${params.color}]" style="background:${
              params.color
            }"></div>
                <div class="one-line-text max-w-[100px]"" style="color:#959598">${params.name}</div>
                </div>
                <div class="text-[var(--color-text-1)] font-semibold">${addCommasToNumber(params.value)}</div>
                </div>
                `;
            return html;
          },
        },
      };
    });

    const itemMax = Math.max(...item.count);

    maxAxis = Math.max(itemMax, maxAxis);

    return {
      name: t(contentTabList[sid].label),
      type: 'bar',
      barWidth: 12,
      legendHoverLink: true,
      large: true,
      itemStyle: {
        borderRadius: [2, 2, 0, 0],
      },
      barCategoryGap: 24,
      data: countData,
      barMinHeight: ((optionData: Record<string, any>[]) => {
        optionData.forEach((itemValue: any, index: number) => {
          if (itemValue.value === 0) optionData[index].value = null;
        });
        let hasZero = false;
        for (let i = 0; i < optionData.length; i++) {
          if (optionData[i].value === 0) {
            hasZero = true;
            break;
          }
        }
        return hasZero ? 0 : 5;
      })(countData),
    };
  });

  // 动态步长调整函数
  const calculateStep = (num: number) => {
    const magnitude = 10 ** Math.floor(Math.log10(num));
    const step = num > 2 * magnitude ? magnitude * 2 : magnitude;
    return step;
  };

  const roundUpToNearest = (num: number, step: number) => Math.ceil(num / step) * step;

  maxAxis = roundUpToNearest(maxAxis, calculateStep(maxAxis));

  options.series = hasPermission ? seriesData : [];
  options.yAxis[0].max = maxAxis;

  options.yAxis[0].nameTextStyle.padding = maxAxis < 10 ? [0, 0, 0, 20] : [0, 0, 0, 0];

  return options;
}
