<template>
  <div>
    <el-row>
      <el-col :span="12" v-if="caseCharData && caseCharData.length > 0">
        <ms-doughnut-pie-chart style="margin-right: 200px" :name="$t('api_test.home_page.detail_card.single_case')"
                               :data="caseCharData" ref="functionChar"/>
      </el-col>
      <el-col :span="12" v-if="scenarioCharData && scenarioCharData.length > 0">
        <api-scenario-char-result :name="$t('test_track.plan.test_plan_api_scenario_count')" :data="scenarioCharData"/>
        <api-scenario-char-result style="margin-top: -50px;"
                                  :name="$t('test_track.plan.test_plan_component_case_count')" :data="stepCharData"/>
      </el-col>
    </el-row>
  </div>
</template>

<script>

import MsPieChart from "metersphere-frontend/src/components/MsPieChart";
import MsDoughnutPieChart from "metersphere-frontend/src/components/MsDoughnutPieChart";
import ApiScenarioCharResult
  from "@/business/plan/view/comonents/report/detail/component/ApiScenarioCharResult";
import {REPORT_STATUS_MAP} from "@/business/utils/constants";

export default {
  name: "ApiResult",
  components: {ApiScenarioCharResult, MsDoughnutPieChart, MsPieChart},
  data() {
    return {
      caseDataMap: REPORT_STATUS_MAP,
      caseCharData: [],
      scenarioCharData: [],
      stepCharData: [],
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
      if (this.apiResult.apiCaseData) {
        this.apiResult.apiCaseData.forEach(item => {
          let data = this.getDataByStatus(item.status);
          data.value = item.count;
          caseCharData.push(data);
        });
      }
      this.caseCharData = caseCharData;

      let apiScenarioData = [];
      if (this.apiResult.apiScenarioData) {
        this.apiResult.apiScenarioData.forEach(item => {
          let data = this.getDataByStatus(item.status);
          data.value = item.count;
          apiScenarioData.push(data);
        });
      }

      let stepCharData = [];
      if (this.apiResult.apiScenarioStepData && this.apiResult.apiScenarioStepData.length > 0) {
        for (let i = 0; i < this.apiResult.apiScenarioStepData.length; i++) {
          let stepItem = this.apiResult.apiScenarioStepData[i];
          let data = this.getDataByStatus(stepItem.status);
          data.value = stepItem.count;
          stepCharData.push(data);
        }
      }

      this.scenarioCharData = apiScenarioData;
      this.stepCharData = stepCharData;
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
