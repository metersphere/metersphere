<template>
  <div class="dashboard-card">
    <el-card shadow="never" class="box-card" style="height: 100%">
      <div slot="header" class="clearfix">
        <span class="dashboard-title">{{ $t('test_track.home.case_maintenance') }}</span>
      </div>

      <div v-loading="loading" element-loading-background="#FFFFFF">
        <div v-if="loadError"
             style="width: 100%; height: 300px; display: flex; flex-direction: column; justify-content: center;align-items: center">
          <img style="height: 100px;width: 100px;"
               src="/assets/module/figma/icon_load_error.svg"/>
          <span class="addition-info-title" style="color: #646A73">{{ $t("home.dashboard.public.load_error") }}</span>
        </div>
        <div v-if="!loadError">
          <el-container>
            <ms-chart ref="chart1" :options="caseOption" :autoresize="true" style="width: 100%;height: 340px;"></ms-chart>
          </el-container>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import MsChart from "metersphere-frontend/src/components/chart/MsChart";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import {getTrackCaseBar} from "@/api/track";

export default {
  name: "CaseMaintenance",
  components: {MsChart},
  data() {
    return {
      loading: false,
      loadError: false,
      caseOption: {}
    }
  },
  activated() {
    this.search();
  },
  methods: {
    search() {
      this.loading = true;
      this.loadError = false;
      let selectProjectId = getCurrentProjectID();
      getTrackCaseBar(selectProjectId)
        .then(r => {
          this.loading = false;
          this.loadError = false;
          let data = r.data;
          this.setBarOption(data);
        }).catch(() => {
          this.loading = false;
          this.loadError = true;
        });
    },
    setBarOption(data) {
      let xAxis = [];
      data.map(d => {
        if (!xAxis.includes(d.xAxis)) {
          xAxis.push(d.xAxis);
        }
      });
      let yAxis1 = data.filter(d => d.groupName === 'FUNCTIONCASE').map(d => [d.xAxis, d.yAxis]);
      let yAxis2 = data.filter(d => d.groupName === 'RELEVANCECASE').map(d => [d.xAxis, d.yAxis]);
      let option = {
        tooltip: {
          trigger: 'axis',
          axisPointer: {
            type: 'shadow'
          }
        },
        xAxis: {
          type: 'category',
          data: xAxis,
          axisTick:{
            show:false // 不显示坐标轴刻度线
          },
        },
        yAxis: {
          type: 'value',
          axisLine: {
            show: false
          },
          axisTick: {
            show: false
          },
          min: 0,
          max: data.length === 0 ? 40 : null
        },
        grid: {
          left: 0,
          containLabel: true,
          top: 80,
          right: 24,
          width: 600,
          height: 255
        },
        legend: {
          itemWidth: 8,
          itemHeight: 8,
          data: [{icon: 'rect', name: this.$t('test_track.home.function_case_count')}, {icon: 'rect', name: this.$t('test_track.home.relevance_case_count')}],
          orient: 'horizontal',
          left: '0',
          top: '24'
        },
        series: [
          {
            name: this.$t('test_track.home.function_case_count'),
            data: yAxis1,
            type: 'bar',
            barWidth: 16,
            barMinHeight: 5,
            itemStyle: {
              color: '#AA4FBF',
              barBorderRadius: [2, 2, 0, 0]
            }
          },
          {
            name: this.$t('test_track.home.relevance_case_count'),
            data: yAxis2,
            type: 'bar',
            barWidth: 16,
            barMinHeight: 5,
            itemStyle: {
              color: '#F9CB2E',
              barBorderRadius: [2, 2, 0, 0]
            }
          }]
      };
      this.caseOption = option;
    },
  }
}
</script>

<style scoped>
.no-shadow-card{
  -webkit-box-shadow: 0 0px 0px 0 rgba(0,0,0,.1);
  box-shadow: 0 0px 0px 0 rgba(0,0,0,.1);
}

.el-card :deep( .el-card__header ) {
  border-bottom: 0px solid #EBEEF5;
  padding-bottom: 0px;
}
</style>
