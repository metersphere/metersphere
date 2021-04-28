<template>
  <div v-loading="result.loading">
    <el-tabs type="border-card" :stretch="true">
      <el-tab-pane v-for="(item,index) in instances" :key="index" :label="item" class="logging-content">
        <el-row>
          <el-col :span="12">
            <ms-chart ref="chart1" class="chart-config" :options="getCpuOption(item)" :autoresize="true"></ms-chart>
          </el-col>
          <el-col :span="12">
            <ms-chart ref="chart2" class="chart-config" :options="getMemoryOption(item)" :autoresize="true"></ms-chart>
          </el-col>
          <el-col :span="12">
            <ms-chart ref="chart3" class="chart-config" :options="getDiskOption(item)" :autoresize="true"></ms-chart>
          </el-col>
          <el-col :span="12">
            <ms-chart ref="chart3" class="chart-config" :options="getNetOutOption(item)" :autoresize="true"></ms-chart>
          </el-col>
          <el-col :span="12">
            <ms-chart ref="chart4" class="chart-config" :options="getNetInOption(item)" :autoresize="true"></ms-chart>
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>

import MsChart from "@/business/components/common/chart/MsChart";

export default {
  name: "MonitorCard",
  props: ['report'],
  components: {MsChart},
  data() {
    return {
      result: {},
      id: '',
      loading: false,
      instances: [],
      data: []
    };
  },
  created() {
    this.data = [];
    this.instances = [];
  },
  methods: {
    getResource() {
      this.result = this.$get("/metric/query/resource/" + this.report.id, data => {
        this.instances = data.data;
      });

      this.$get("/metric/query/" + this.report.id, result => {
        if (result) {
          this.data = result.data;
        }
      });
    },
    getCpuOption(id) {
      let xAxis = [];
      let yAxis = [];
      this.data.map(d => {
        if (d.instance === id && d.seriesName === 'cpu') {
          xAxis = d.timestamps;
          yAxis = d.values;
        }
      });
      let option = {
        title: {
          left: 'center',
          text: 'CPU使用率',
          textStyle: {
            color: '#8492a6'
          },
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: xAxis
        },
        yAxis: {
          name: '使用率(%)',
          type: 'value'
        },
        series: [{
          data: yAxis,
          type: 'line'
        }]
      };
      return option;
    },
    getDiskOption(id) {
      let xAxis = [];
      let yAxis = [];
      this.data.map(d => {
        if (d.instance === id && d.seriesName === 'disk') {
          xAxis = d.timestamps;
          yAxis = d.values;
        }
      });
      let option = {
        title: {
          left: 'center',
          text: '磁盘使用率',
          textStyle: {
            color: '#8492a6'
          },
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: xAxis
        },
        yAxis: {
          name: '使用率(%)',
          type: 'value'
        },
        series: [{
          data: yAxis,
          type: 'line'
        }]
      };
      return option;
    },
    getNetInOption(id) {
      let xAxis = [];
      let yAxis = [];
      this.data.map(d => {
        if (d.instance === id && d.seriesName === 'netIn') {
          xAxis = d.timestamps;
          yAxis = d.values;
        }
      });
      let option = {
        title: {
          left: 'center',
          text: '入口流量',
          textStyle: {
            color: '#8492a6'
          },
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: xAxis
        },
        yAxis: {
          type: 'value',
          name: '流量(MB/s)'
        },
        series: [{
          data: yAxis,
          type: 'line'
        }]
      };
      return option;
    },
    getNetOutOption(id) {
      let xAxis = [];
      let yAxis = [];
      this.data.map(d => {
        if (d.instance === id && d.seriesName === 'netOut') {
          xAxis = d.timestamps;
          yAxis = d.values;
        }
      });
      let option = {
        title: {
          left: 'center',
          text: '出口流量',
          textStyle: {
            color: '#8492a6'
          },
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: xAxis
        },
        yAxis: {
          type: 'value',
          name: '流量(MB/s)'
        },
        series: [{
          data: yAxis,
          type: 'line'
        }]
      };
      return option;
    },
    getMemoryOption(id) {
      let xAxis = [];
      let yAxis = [];
      this.data.map(d => {
        if (d.instance === id && d.seriesName === 'memory') {
          xAxis = d.timestamps;
          yAxis = d.values;
        }
      });
      let option = {
        title: {
          left: 'center',
          text: '内存使用率',
          textStyle: {
            color: '#8492a6'
          },
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: xAxis
        },
        yAxis: {
          type: 'value',
          name: '使用率(%)',
        },
        series: [{
          data: yAxis,
          type: 'line'
        }]
      };
      return option;
    }
  },
  watch: {
    report: {
      handler(val) {
        if (!val.status || !val.id) {
          return;
        }
        let status = val.status;
        this.id = val.id;
        if (status === "Completed" || status === "Running") {
          this.getResource();
        } else {
          this.instances = [];
        }
      },
      deep: true
    }
  },
};
</script>

<style scoped>
.chart-config {
  width: 100%;
}
</style>
