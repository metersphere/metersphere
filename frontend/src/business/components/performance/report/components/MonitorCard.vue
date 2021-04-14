<template>
  <div v-loading="result.loading">
    <el-tabs type="border-card" :stretch="true">
      <el-tab-pane v-for="item in instances" :key="item" :label="item" class="logging-content">
        <el-row>
          <el-col :span="10" :offset="2">
            <ms-chart ref="chart1" :options="getCpuOption(item)" :autoresize="true"></ms-chart>
          </el-col>
          <el-col :span="10" :offset="2">
            <ms-chart ref="chart2" :options="getMemoryOption(item)" :autoresize="true"></ms-chart>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="10" :offset="2">
            <ms-chart ref="chart3" :options="getDiskOption(item)" :autoresize="true"></ms-chart>
          </el-col>
          <el-col :span="10" :offset="2">
            <ms-chart ref="chart4" :options="getNetInOption(item)" :autoresize="true"></ms-chart>
          </el-col>
        </el-row>
        <el-row>
          <el-col :span="10" :offset="2">
            <ms-chart ref="chart3" :options="getNetOutOption(item)" :autoresize="true"></ms-chart>
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
      resource: [],
      logContent: [],
      result: {},
      id: '',
      loading: false,
      instances: [],
      data: []
    }
  },
  methods: {
    getResource() {
      this.$get("/metric/query/" + this.report.id, result => {
        if (result) {
          let data = result.data;
          this.data = data;
          let set = new Set()
          data.map(d => set.add(d.instance));
          this.instances = Array.from(set);
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
          text: 'CPU',
          textStyle: {
            color: '#99743C'
          },
        },
        xAxis: {
          type: 'category',
          data: xAxis
        },
        yAxis: {
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
          text: 'Disk',
          textStyle: {
            color: '#99743C'
          },
        },
        xAxis: {
          type: 'category',
          data: xAxis
        },
        yAxis: {
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
          text: 'NetIn',
          textStyle: {
            color: '#99743C'
          },
        },
        xAxis: {
          type: 'category',
          data: xAxis
        },
        yAxis: {
          type: 'value'
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
          text: 'NetOut',
          textStyle: {
            color: '#99743C'
          },
        },
        xAxis: {
          type: 'category',
          data: xAxis
        },
        yAxis: {
          type: 'value'
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
          text: 'Memory',
          textStyle: {
            color: '#99743C'
          },
        },
        xAxis: {
          type: 'category',
          data: xAxis
        },
        yAxis: {
          type: 'value'
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
          this.resource = [];
        }
      },
      deep: true
    }
  },
}
</script>

<style scoped>

</style>
