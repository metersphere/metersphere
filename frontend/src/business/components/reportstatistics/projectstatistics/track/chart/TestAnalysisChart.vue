<template>
  <div v-loading="loading">
    <el-card class="ms-test-chart" :style="{ width: w+'px', height: h + 'px'}" ref="msDrawer">
      <el-row class="ms-row">
        <p class="tip"><span style="margin-left: 5px"></span> {{$t('commons.report_statistics.chart')}} </p>
        <div class="ms-test-chart-header">
          <el-select v-model="chartType" class="ms-col-type" size="mini" style="width: 100px" @change="generateOption">
            <el-option :key="t.id" :value="t.id" :label="t.name" v-for="t in charts"/>
          </el-select>
          <span style="margin: 0px 10px 10px">|</span>
          <el-select v-model="order" class="ms-col-type" size="mini" style="width: 120px" @change="orderCharts">
            <el-option :key="t.id" :value="t.id" :label="t.name" v-for="t in orders"/>
          </el-select>
          <span style="margin: 0px 10px 10px">|</span>
          <font-awesome-icon v-if="!isFullScreen && showFullScreen" class="report-alt-ico" :icon="['fa', 'expand-alt']" size="lg" @click="fullScreen"/>
          <font-awesome-icon v-if="isFullScreen && showFullScreen" class="report-alt-ico" :icon="['fa', 'compress-alt']" size="lg" @click="unFullScreen"/>
        </div>
      </el-row>
      <el-row>
        <ms-chart ref="chart1" :options="loadOption" class="chart-config" :autoresize="true"/>
      </el-row>
    </el-card>
  </div>
</template>

<script>
  // 这个引用不能删除，删除后图例不显示
  import echarts from "echarts";
  import MsChart from "@/business/components/common/chart/MsChart";

  export default {
    name: "TestAnalysisChart",
    components: {MsChart},
    props: {
      loadOption: {},
    },
    data() {
      return {
        x: 0,
        y: 0,
        w: document.documentElement.clientWidth - 760,
        h: document.documentElement.clientHeight / 1.7,
        isFullScreen: false,
        originalW: 100,
        originalH: 100,
        showFullScreen: {
          type: Boolean,
          default() {
            return true;
          }
        },
        // 头部部分
        chartType: "line",
        charts: [{id: 'line', name: this.$t('commons.report_statistics.line')}, {id: 'bar', name: this.$t('commons.report_statistics.bar')}],
        order: "",
        orders: [{id: '', name: this.$t('commons.sort_default')},{id: 'desc', name: this.$t('commons.report_statistics.desc')}, {id: 'asc', name: this.$t('commons.report_statistics.asc')}],
        loading: false,
        options: {},
      }
    },
    methods: {
      orderCharts() {
        this.$emit('orderCharts', this.order);
      },
      generateOption() {
        this.loadOption.series.forEach(item => {
          item.type = this.chartType;
        })
        this.reload();
      },
      reload() {
        this.loading = true
        this.$nextTick(() => {
          this.loading = false
        })
      },
      fullScreen() {
        this.originalW = this.w;
        this.originalH = this.h;
        this.w = document.body.clientWidth - 50;
        this.h = document.body.clientHeight;
        this.isFullScreen = true;
        this.$emit('hidePage', true);
      },
      unFullScreen() {
        this.w = this.originalW;
        this.h = this.originalH;
        this.isFullScreen = false;
        this.$emit('hidePage', false);
      },
      getOptions(){
        return this.loadOption;
      }
    },
  }
</script>

<style scoped>

  .ms-test-chart-header {
    z-index: 999;
    width: 320px;
    float: right;
    margin-right: 10px;
  }

  .report-alt-ico {
    font-size: 15px;
    margin: 0px 10px 0px;
    color: #8c939d;
  }

  .report-alt-ico:hover {
    color: black;
    cursor: pointer;
    font-size: 18px;
  }

  /deep/ .echarts {
    height: calc(100vh / 1.95);
  }

  .tip {
    float: left;
    font-size: 14px;
    border-radius: 2px;
    border-left: 2px solid #783887;
    margin: 0px 20px 0px;
  }

  .ms-row {
    padding-top: 10px;
  }

  .chart-config {
    width: 100%;
  }

  /deep/ .el-card__body {
    padding: 0px;
  }

</style>
