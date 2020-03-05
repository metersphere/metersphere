import ECharts from 'vue-echarts'
import 'echarts/lib/chart/line'
import 'echarts/lib/chart/bar'
import 'echarts/lib/chart/pie'

export default {
  install(Vue) {
    Vue.component('chart', ECharts);
  }
}
