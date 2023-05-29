import ECharts from 'vue-echarts'
import * as echarts from 'echarts/core';
import { LineChart, BarChart, PieChart, GraphChart } from 'echarts/charts'
import { TooltipComponent, TitleComponent, ToolboxComponent, DataZoomComponent, LegendComponent, GridComponent } from 'echarts/components'
import {
  CanvasRenderer, SVGRenderer
} from 'echarts/renderers';

echarts.use(
  [
    TitleComponent,
    TooltipComponent,
    GridComponent,
    ToolboxComponent,
    LegendComponent,
    DataZoomComponent,
    BarChart,
    PieChart,
    LineChart,
    GraphChart,
    CanvasRenderer,
    SVGRenderer
  ]
);

export default {
  install(Vue) {
    Vue.component('chart', ECharts);
  }
}
