<template>
  <common-monitor-chart>
    <template>
      <div id="response-time-chart" :style="{ width:'100%',height:'100%' }">
        <chart :options="getOptions()" :style="{ width:'100%' }"></chart>
      </div>
    </template>
  </common-monitor-chart>
</template>

<script>
import CommonMonitorChart from '@/business/components/api/monitor/components/CommonMonitorChart';

export default {

  name: 'ApiErrorMonitorChart',
  components: {CommonMonitorChart},
  props: [
    'data',
    'xAxis',
    'apiUrl',
  ],
  data() {
    return {
      reportId: '',
    };
  },
  methods: {
    click(params) {
      //如果状态不是以2开头
      if (params.value.substr(0, 1) !== '2') {
        let startTime = params.name;
        this.result = this.$$get('/api/monitor/getReportId', {'startTime': startTime}, {
          'apiUrl': this.apiUrl
        }, response => {
          this.reportId = response.data;
          let reportId = this.reportId
          let url = '#/api/report/view/' + reportId;
          let target = '_blank';
          window.open(url, target);
        });
      }

    },
    getOptions() {
      return {
        title: {text: 'HTTP状态码趋势'},
        tooltip: {},
        toolbox: {
          show: true,
          feature: {
            dataZoom: {
              yAxisIndex: 'none'
            },
            dataView: {
              readOnly: false
            },
            restore: {},
            saveAsImage: {}
          }
        },
        dataZoom: [{
          start: 0
        }],
        xAxis: {
          type: 'category',
          data: this.xAxis
        },
        yAxis: {
          type: 'value',
          min: 100,
          max: 500,
          splitNumber: 5
        },
        series: [{
          type: 'line',
          smooth: true,
          data: this.data,

          lineStyle: {
            color: '#32CD32'
          },
          itemStyle: {},
        }],
      };
    },
  },
}
</script>

<style scoped>

</style>
