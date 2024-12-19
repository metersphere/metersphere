import { cloneDeep } from 'lodash-es';

import getVisualThemeColor from '@/config/chartTheme';
import { toolTipConfig } from '@/config/testPlan';
import { commonRatePieOptions, defaultValueMap } from '@/config/workbench';
import { useI18n } from '@/hooks/useI18n';
import { addCommasToNumber } from '@/utils';

import type { ModuleCardItem, OverViewOfProject } from '@/models/workbench/homePage';
import { RouteEnum } from '@/enums/routeEnum';
import { WorkCardEnum, WorkNavValueEnum } from '@/enums/workbenchEnum';

import VCharts from 'vue-echarts';

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
  [WorkCardEnum.CASE_COUNT]: ['#ED0303', '#FFA200', '#3370FF', 'initItemStyleColor'],
  [WorkCardEnum.ASSOCIATE_CASE_COUNT]: ['#00C261', '#3370FF'],
  [WorkCardEnum.REVIEW_CASE_COUNT]: ['initItemStyleColor', '#3370FF', '#00C261', '#ED0303', '#FFA200'],
  [WorkCardEnum.TEST_PLAN_COUNT]: ['initItemStyleColor', '#3370FF', '#00C261', '#FF9964'],
  [WorkCardEnum.PLAN_LEGACY_BUG]: ['#FFA200', '#3370FF', 'initItemStyleColor', '#00C261', ...getColorScheme(13)],
  [WorkCardEnum.BUG_COUNT]: ['#FFA200', '#3370FF', 'initItemStyleColor', '#00C261', ...getColorScheme(13)],
  [WorkCardEnum.HANDLE_BUG_BY_ME]: ['#FFA200', '#3370FF', 'initItemStyleColor', '#00C261', ...getColorScheme(13)],
  [WorkCardEnum.CREATE_BY_ME]: ['#9441B1', '#3370FF', '#00C261', 'initItemStyleColor'],
  [WorkCardEnum.API_COUNT]: ['#811FA3', '#00C261', '#3370FF', '#FFA1FF', '#EE50A3', '#FF9964', '#F9F871', '#C3DD40'],
  [WorkCardEnum.CREATE_BUG_BY_ME]: ['#FFA200', '#3370FF', 'initItemStyleColor', '#00C261', ...getColorScheme(13)],
};

// 柱状图
export function getCommonBarOptions(
  hasRoom: boolean,
  color: string[],
  isTestPlan = false,
  fullScreen = true
): Record<string, any> {
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
        backgroundColor: 'transparent',
        formatter(params: any) {
          let testPlanHtml = '';
          // 如果是测试计划未分配
          let paramsList = [];
          if (isTestPlan) {
            const unAssign = params[0].axisValueLabel === t('workbench.homePage.planUnExecutor');
            paramsList = unAssign ? params.slice(0, 1) : params;
            testPlanHtml = unAssign
              ? ``
              : `<div class="flex items-center justify-between">
                              <div class="flex items-center gap-[8px] text-[var(--color-text-2)]">
                                <div style="background:#00C261;" class="flex items-center justify-center w-[11px] h-[11px] rounded-full text-[10px]">
                                  <span class="text-[var(--color-text-fff)] text-center">✓</span>
                                </div>
                                   ${t('workbench.homePage.completeRate')}
                              </div>
                             
                              <div class="text-[rgb(var(--success-6))] font-semibold">${params[0].data.passRate}</div>
                            </div>`;
          } else {
            paramsList = params;
          }
          const html = `
        <div class="w-[186px] rounded ms-scroll-bar max-h-[236px] overflow-y-auto p-[16px] gap-[8px] flex flex-col bg-[var(--color-bg-5)]">
        <div class="font-medium max-w-[150px] text-[var(--color-text-1)] one-line-text">
        ${params[0].axisValueLabel}
        </div>
        ${testPlanHtml}
        ${paramsList
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
      triggerEvent: true,
      axisLabel: {
        show: true,
        color: getVisualThemeColor('xLabelColor'),
        width: 100,
        overflow: 'truncate',
        ellipsis: '...',
        showMinLabel: true,
        showMaxLabel: true,
        interval: 0,
      },
      axisPointer: {
        type: 'shadow',
      },
      axisTick: {
        show: true,
        alignWithLabel: true,
        lineStyle: {
          color: 'transparent',
        },
      },
      axisLine: {
        lineStyle: {
          color: getVisualThemeColor('splitLineColor'),
        },
      },
    },
    yAxis: [
      {
        type: 'value',
        alignTicks: true,
        position: 'left',
        axisLine: {
          show: false,
          onZero: false, // 禁止 Y 轴强制显示在 0 位置
        },
        axisLabel: {
          color: getVisualThemeColor('yLabelColor'),
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
            color: getVisualThemeColor('splitLineColor'), // 水平线颜色
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
        fill: getVisualThemeColor('graphicFillColor'),
        backgroundColor: getVisualThemeColor('graphicBackgroundColor'),
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
      pageIconColor: getVisualThemeColor('pageIconColor'),
      pageIconInactiveColor: getVisualThemeColor('pageIconInactiveColor'),
      pageIconSize: [10, 8],
      pageTextStyle: {
        color: getVisualThemeColor('pageTextStyleColor'),
        fontSize: 12,
      },
      textStyle: {
        width: 150,
        color: getVisualThemeColor('legendColor'),
        overflow: 'truncate',
        ellipsis: '...',
      },
      formatter(name: string) {
        return name;
      },
      tooltip: {
        show: true,
        padding: 0,
        position(point: any) {
          return [point[0], '10%'];
        },
        backgroundColor: 'transparent',
        formatter(params: any) {
          return `
            <div class="p-[8px] rounded bg-[var(--color-bg-5)] text-[var(--color-text-1)]">${params.name}</div>
          `;
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
            realtime: true,
            minSpan: 1,
            maxValueSpan: fullScreen ? 12 : 6,
            startValue: 0,
            end: 30,
            endValue: fullScreen ? 12 : 6,
            rangeMode: ['percent', 'percent'], // 起点按实际值，终点按百分比动态计算
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
export function getPieCharOptions() {
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
        color: getVisualThemeColor('subtextStyleColor'),
        fontWeight: 'bold',
        align: 'center',
      },
      textAlign: 'center', // 确保副标题居中
    },

    tooltip: { show: true },
    legend: {
      show: false,
    },
    series: {
      name: '',
      type: 'pie',
      radius: ['75%', '90%'],
      center: [90, '48%'],
      minAngle: 5, // 设置扇区的最小角度
      minShowLabelAngle: 10, // 设置标签显示的最小角度
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
  const options: Record<string, any> = getPieCharOptions();
  const lastStatusPercentList = statusPercentList ?? [];
  const hasDataLength = lastStatusPercentList.filter((e) => Number(e.count) > 0).length;
  const pieBorderWidth = hasDataLength === 1 ? 0 : 2;

  const lastData = lastStatusPercentList
    .map((item, color) => {
      return {
        name: item.status,
        value: item.count,
        tooltip: {
          ...toolTipConfig,
          position: 'right',
          show: !!hasPermission,
        },
        itemStyle: {
          color:
            colorMapConfig[key][color] === 'initItemStyleColor'
              ? getVisualThemeColor('initItemStyleColor')
              : colorMapConfig[key][color],
          borderWidth: pieBorderWidth,
          borderColor: getVisualThemeColor('itemStyleBorderColor'),
        },
      };
    })
    .filter((e) => e.value !== 0);

  options.series.data =
    hasDataLength > 0
      ? lastData
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
    options.series.data = [
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
        WorkNavValueEnum.TEST_PLAN_PREPARED, // 测试计划-未开始
        WorkNavValueEnum.TEST_PLAN_UNDERWAY, // 测试计划-进行中
        WorkNavValueEnum.TEST_PLAN_COMPLETED, //   测试计划-已完成
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
  const defaultValueMapConfig = defaultValueMap();

  const emptyData = {
    name: '',
    value: 1,
    tooltip: {
      show: false,
    },
    itemStyle: {
      color: getVisualThemeColor('initItemStyleColor'),
    },
  };
  if (hasPermission) {
    const pieBorderWidth = countList.slice(1).filter((e) => Number(e.count) > 0).length === 1 ? 0 : 1;

    lastCountList = countList.slice(1).map((item, i) => {
      return {
        ...item,
        value: item.count,
        label: item.name,
        name: item.name,
        itemStyle: {
          color: defaultValueMapConfig[typeKey][valueKey].color[i],
          borderWidth: pieBorderWidth,
          borderColor: getVisualThemeColor('itemStyleBorderColor'),
        },
      };
    });

    options.series.data = lastCountList.every((e) => e.value === 0)
      ? [emptyData]
      : lastCountList.filter((e) => e.value !== 0);

    options.title.text = countList[0].name ?? '';
    options.title.subtext = `${countList[0].count ?? 0}%`;
  } else {
    options.series.data = [emptyData];

    lastCountList = defaultValueMapConfig[typeKey][valueKey].defaultList.map((e: any) => {
      return {
        ...e,
        label: t(e.label),
      };
    });
    options.title.text = t(defaultValueMapConfig[typeKey][valueKey].defaultName);
    options.title.subtext = '-%';
  }

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
  isTestPlan = false,
  isStack = false,
  fullScreen = true
) {
  let options: Record<string, any> = {};

  const { projectCountList, xaxis, errorCode } = detail;
  const hasPermission = errorCode !== 109001;

  options = getCommonBarOptions(xaxis.length >= 7, colorConfig, isTestPlan, fullScreen);
  options.xAxis.data = xaxis;
  const { invisible, text } = handleNoDataDisplay(xaxis, hasPermission);
  options.graphic.invisible = invisible;
  options.graphic.style.text = text;

  let maxAxis = 5;

  let result: number[][];

  // 计算通过率
  if (isTestPlan) {
    const columnCount = projectCountList[0]?.count.length || 0;
    result = Array.from({ length: columnCount }, () => []);
    projectCountList.forEach((item) => {
      item.count.forEach((value, index) => {
        result[index].push(value);
      });
    });
  }

  const seriesData = projectCountList.map((item, sid) => {
    const countData: Record<string, any>[] = item.count.map((e, i) => {
      let passRate: string = '0.00%';
      if (isTestPlan) {
        const testPlanPassParams = result[i];
        const [assigning, complete] = testPlanPassParams;
        passRate = assigning > 0 ? `${((complete / assigning) * 100).toFixed(2)}%` : '0.00%';
      }

      return {
        name: t(contentTabList[sid]?.label ?? ''),
        value: e,
        originValue: e,
        passRate,
        tooltip: {
          show: true,
          trigger: 'item',
          enterable: true,
          backgroundColor: 'transparent',
          formatter(params: any) {
            const html = `
                <div class="w-[186px] rounded h-[50px] p-[16px] flex items-center justify-between bg-[var(--color-bg-5)]">
                <div class=" flex items-center">
                <div class="mb-[2px] mr-[8px] h-[8px] w-[8px] rounded-sm bg-[${params.color}]" style="background:${
              params.color
            }"></div>
                <div class="one-line-text max-w-[100px] text-[var(--color-text-2)]">${params.name}</div>
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

    const itemSeries: Record<string, any> = {
      name: t(contentTabList[sid]?.label ?? ''),
      type: 'bar',
      barWidth: 12,
      legendHoverLink: true,
      large: true,
      itemStyle: {
        borderRadius: [2, 2, 0, 0],
      },
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

    if (isStack) {
      itemSeries.stack = 'stack';
    }

    return itemSeries;
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

export function createCustomTooltip(chartDom: InstanceType<typeof VCharts>) {
  if (chartDom && chartDom.chart) {
    const customTooltip = document.createElement('div');
    customTooltip.style.position = 'absolute';

    customTooltip.style.maxWidth = '300px';
    customTooltip.style.padding = '5px';
    customTooltip.style.background = 'var(--color-text-2)';
    customTooltip.style.color = 'var(--color-text-fff)';
    customTooltip.style.borderRadius = '4px';

    customTooltip.style.display = 'none';
    document.body.appendChild(customTooltip);

    // 针对x轴 监听鼠标悬浮事件
    chartDom.chart.on('mouseover', 'xAxis', (params) => {
      const event = params.event?.event as unknown as MouseEvent;
      if (params.componentType === 'xAxis') {
        const { clientX, clientY } = event;

        customTooltip.textContent = `${params.value}`;
        customTooltip.style.display = 'block';

        customTooltip.style.left = `${clientX}px`;
        customTooltip.style.top = `${clientY + 10}px`;
      }
    });

    // 针对x轴 监听鼠标离开事件
    chartDom.chart.on('mouseout', 'xAxis', () => {
      customTooltip.style.display = 'none';
    });
  }
}
