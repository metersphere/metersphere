import ECharts from 'vue-echarts'
import 'echarts/lib/chart/line'

export default {
  install(Vue) {
    Vue.component('chart', ECharts);
  }
}
