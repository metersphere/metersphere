import ECharts from 'vue-echarts'
import 'echarts/lib/chart/line'
import 'echarts/lib/chart/bar'
import 'echarts/lib/chart/pie'
import 'echarts/lib/chart/graph'
import 'echarts/lib/component/tooltip'
import 'echarts/lib/component/title'
import 'echarts/lib/component/toolbox';
import 'echarts/lib/component/dataZoom';
import 'echarts/lib/component/legend';
import 'zrender/lib/svg/svg' // initOption 支持使用svg
import 'zrender/lib/canvas/canvas'  // initOption 支持使用canvas
import 'echarts/lib/component/grid'

export default {
  install(Vue) {
    Vue.component('chart', ECharts);
  }
}
