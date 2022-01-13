<template>
  <div>
    <el-row >
      <el-col :span="12">
        <ms-doughnut-pie-chart :name="$t('test_track.plan_view.performance_case_count')" :data="caseCharData" ref="functionChar"/>
      </el-col>
      <el-col :span="12">
      </el-col>
    </el-row>
  </div>
</template>

<script>

import MsPieChart from "@/business/components/common/components/MsPieChart";
import MsDoughnutPieChart from "@/business/components/common/components/MsDoughnutPieChart";
export default {
  name: "loadResult",
  components: {MsDoughnutPieChart, MsPieChart},
  data() {
    return {
      caseDataMap: new Map([
        ["success", {name: this.$t('test_track.plan_view.pass'), itemStyle: {color: '#67C23A'}}],
        ["Success", {name: this.$t('test_track.plan_view.pass'), itemStyle: {color: '#67C23A'}}],
        ["SUCCESS", {name: this.$t('test_track.plan_view.pass'), itemStyle: {color: '#67C23A'}}],
        ["error", {name: this.$t('test_track.plan_view.failure'), itemStyle: {color: '#F56C6C'}}],
        ["Error", {name: this.$t('test_track.plan_view.failure'), itemStyle: {color: '#F56C6C'}}],
        ["run", {name: this.$t('test_track.plan_view.running'), itemStyle: {color: '#DEDE10'}}],
        ["Prepare", {name: this.$t('api_test.home_page.detail_card.unexecute'), itemStyle: {color: '#909399'}}],
      ]),
      caseCharData: [],
      isShow: true
    }
  },
  props: {
    loadResult: {
      type: Object,
      default() {
        return {
          caseData: [],
        }
      }
    }
  },
  watch: {
    loadResult() {
      this.getCaseCharData();
    }
  },
  created() {
    this.getCaseCharData();
  },
  methods: {
    getCaseCharData() {
      let caseCharData = [];
      this.loadResult.caseData.forEach(item => {
        let data = this.caseDataMap.get(item.status);
        if (!data) {
          data = this.caseDataMap.get('Prepare');
        }
        data.value = item.count;
        caseCharData.push(data);
      });
      this.caseCharData = caseCharData;
    },
  }
}
</script>

<style scoped>
</style>
