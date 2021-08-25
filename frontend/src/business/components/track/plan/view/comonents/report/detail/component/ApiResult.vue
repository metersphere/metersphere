<template>
  <div>
    <el-row >
      <el-col :span="12">
        <ms-doughnut-pie-chart :name="$t('单接口用例')" :data="caseCharData" ref="functionChar"/>
      </el-col>
      <el-col :span="12">
        <api-scenario-char-result :data="scenarioCharData"/>
      </el-col>
    </el-row>
  </div>
</template>

<script>

import MsPieChart from "@/business/components/common/components/MsPieChart";
import MsDoughnutPieChart from "@/business/components/common/components/MsDoughnutPieChart";
import ApiScenarioCharResult
  from "@/business/components/track/plan/view/comonents/report/detail/component/ApiScenarioCharResult";
export default {
  name: "ApiResult",
  components: {ApiScenarioCharResult, MsDoughnutPieChart, MsPieChart},
  data() {
    return {
      caseDataMap: new Map([
        ["success", {name: this.$t('test_track.plan_view.pass'), itemStyle: {color: '#67C23A'}}],
        ["Success", {name: this.$t('test_track.plan_view.pass'), itemStyle: {color: '#67C23A'}}],
        ["error", {name: this.$t('test_track.plan_view.failure'), itemStyle: {color: '#F56C6C'}}],
        ["Fail", {name: this.$t('test_track.plan_view.failure'), itemStyle: {color: '#F56C6C'}}],
        ["Prepare", {name: this.$t('api_test.home_page.detail_card.unexecute'), itemStyle: {color: '#909399'}}],
      ]),
      caseCharData: [],
      scenarioCharData: [],
      isShow: true
    }
  },
  props: {
    apiResult: {
      type: Object,
      default() {
        return {
          caseData: [],
          issueData: []
        }
      }
    }
  },
  watch: {
    apiResult() {
      this.getCaseCharData();
    }
  },
  created() {
    this.getCaseCharData();
  },
  methods: {
    getCaseCharData() {
      let caseCharData = [];
      this.apiResult.apiCaseData.forEach(item => {
        let data = this.getDataByStatus(item.status);
        data.value = item.count;
        caseCharData.push(data);
      });
      this.caseCharData = caseCharData;

      let apiScenarioData = [];
      this.apiResult.apiScenarioData.forEach(item => {
        let data = this.getDataByStatus(item.status);
        data.data = [item.count];
        for (let i = 0; i < this.apiResult.apiScenarioStepData.length; i++) {
          let stepItem = this.apiResult.apiScenarioStepData[i];
          if (item.status) {
            if (item.status === 'Fail' && stepItem.status === 'Failure') {
              data.data.push(stepItem.count);
              break;
            }
            if (item.status === 'Success' && stepItem.status === 'Pass') {
              data.data.push(stepItem.count);
              break;
            }
            if (item.status === 'Prepare' && stepItem.status === 'Underway') {
              data.data.push(stepItem.count);
              break;
            }
          } else {
            if (stepItem.status === 'Underway') {
              data.data.push(stepItem.count);
              break;
            }
          }
        }
        apiScenarioData.push(data);
      });
      this.scenarioCharData = apiScenarioData;
    },
    getDataByStatus(status) {
      let tmp = this.caseDataMap.get(status);
      if (!tmp) {
        tmp = this.caseDataMap.get('Prepare');
      }
      let data = {};
      Object.assign(data, tmp);
      return data;
    }
  }
}
</script>

<style scoped>
</style>
