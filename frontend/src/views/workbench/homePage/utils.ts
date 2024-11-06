import { addCommasToNumber } from '@/utils';

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

export const defectStatusColor = ['#00C261', '#FFA200'];

export function getCommonBarOptions(hasRoom: boolean, color: string[]): Record<string, any> {
  return {
    tooltip: {
      trigger: 'item',
      borderWidth: 0,
      formatter(params: any) {
        const html = `
      <div class="w-[186px] h-[50px] p-[16px] flex items-center justify-between">
      <div class=" flex items-center">
      <div class="mb-[2px] mr-[8px] h-[8px] w-[8px] rounded-sm bg-[${params.color}]" style="background:${
          params.color
        }"></div>
      <div style="color:#959598">${params.name}</div>
      </div>
      <div class="text-[#323233] font-medium">${addCommasToNumber(params.value)}</div>
      </div>
      `;
        return html;
      },
    },
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
          padding: [0, 0, 0, -20], // 通过左侧（最后一个值）的负偏移向左移动
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
    colorBy: 'series',
    series: [
      // {
      //   name: '项目A',
      //   type: 'bar',
      //   barWidth: 12,
      //   itemStyle: {
      //     borderRadius: [2, 2, 0, 0], // 上边圆角
      //   },
      //   data: [400, 200, 150, 80, 70, 110, 130],
      // },
      // {
      //   name: '项目B',
      //   type: 'bar',
      //   barWidth: 12,
      //   itemStyle: {
      //     borderRadius: [2, 2, 0, 0], // 上边圆角
      //   },
      //   data: [90, 160, 130, 100, 90, 120, 140],
      // },
      // {
      //   name: '项目C',
      //   type: 'bar',
      //   barWidth: 12,
      //   itemStyle: {
      //     borderRadius: [2, 2, 0, 0], // 上边圆角
      //   },
      //   data: [100, 140, 120, 90, 100, 130, 120],
      // },
      // {
      //   name: '项目D',
      //   type: 'bar',
      //   barWidth: 12,
      //   itemStyle: {
      //     borderRadius: [2, 2, 0, 0], // 上边圆角
      //   },
      //   data: [90, 160, 130, 100, 90, 120, 140],
      // },
      // {
      //   name: '项目E',
      //   type: 'bar',
      //   barWidth: 12,
      //   itemStyle: {
      //     borderRadius: [2, 2, 0, 0], // 上边圆角
      //   },
      //   data: [100, 140, 120, 90, 100, 130, 120],
      // },
      // {
      //   name: '项目F',
      //   type: 'bar',
      //   barWidth: 12,
      //   itemStyle: {
      //     borderRadius: [2, 2, 0, 0], // 上边圆角
      //   },
      //   data: [100, 140, 120, 90, 40, 130, 120],
      // },
      // {
      //   name: '项目G',
      //   type: 'bar',
      //   barWidth: 12,
      //   itemStyle: {
      //     borderRadius: [2, 2, 0, 0], // 上边圆角
      //   },
      //   data: [100, 140, 120, 90, 40, 130, 120],
      // },
    ],
    legend: {
      show: true,
      type: 'scroll',
      itemGap: 20,
      itemWidth: 8,
      itemHeight: 8,
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

export default {};
