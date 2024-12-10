export default function bindDataZoomEvent(
  chartRef: any,
  options: Record<string, any>,
  barWidth = 12,
  barWidthMargin = 6,
  barNumber = 7,
  minBarGroupWidth = 24
) {
  const chartDom = chartRef.value?.chartRef;

  const handleDataZoom = (params: any) => {
    const containerWidth = chartDom.getDom()?.offsetWidth;
    // 计算缩放百分比
    const percent = (params.end - params.start) / 100;
    // 计算单组条形图的宽度（包括间隔和最小宽度）
    const singleGroupWidth = barWidth * barNumber + barWidthMargin * (barNumber - 1) + minBarGroupWidth;
    // 计算可视区域内的最大条形图组数
    const maxVisibleGroups = Math.floor(containerWidth / singleGroupWidth);
    // 根据缩放百分比，计算需要显示的分类数量
    const val = options.value.xAxis.data.length * percent;

    const calcCount = Math.ceil(val);
    // 计算每个标签的宽度
    const labelWidth = (containerWidth - calcCount * minBarGroupWidth) / calcCount;
    // 更新图表的配置项，重新设置数据缩放和 x 轴标签的显示
    chartDom.setOption(
      {
        ...options.value,
        dataZoom: [
          {
            ...options.value.dataZoom[0],
            start: params.start,
            end: params.end,
            maxValueSpan: maxVisibleGroups,
          },
        ],
        xAxis: {
          axisLabel: {
            width: labelWidth,
            overflow: 'truncate',
            ellipsis: '...',
            interval: 0,
          },
          ...options.value.xAxis,
        },
      },
      { notMerge: true }
    );
  };

  chartDom.chart.on('dataZoom', handleDataZoom);

  const handleResize = () => {
    if (chartDom) {
      const currentOptions = chartDom.chart?.getOption();

      if (currentOptions && currentOptions?.dataZoom.length) {
        handleDataZoom({ start: currentOptions.dataZoom[0].start, end: currentOptions.dataZoom[0].end });
      }
    }
  };

  window.addEventListener('resize', handleResize);

  return {
    clear: () => {
      chartDom.chart.off('dataZoom', handleDataZoom);
      window.removeEventListener('resize', handleResize);
    },
    handleDataZoom,
  };
}
