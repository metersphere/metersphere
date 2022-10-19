<template>
  <div>
    <el-row >
      <el-col :span="12" v-if="caseCharData && caseCharData.length > 0">
        <ms-doughnut-pie-chart style="margin-right: 200px" :name="$t('api_test.home_page.ui_details_card.scenario_cases')" :data="caseCharData" ref="functionChar"/>
      </el-col>
      <el-col :span="12" v-if="scenarioCharData && scenarioCharData.length > 0">
        <api-scenario-char-result style="margin-top: -50px;" :name="$t('api_test.home_page.ui_details_card.step_count')" :data="stepCharData"/>
      </el-col>
    </el-row>
  </div>
</template>

<script>

import MsPieChart from "metersphere-frontend/src/components/MsPieChart";
import MsDoughnutPieChart from "metersphere-frontend/src/components/MsDoughnutPieChart";
import ApiScenarioCharResult from "@/business/plan/view/comonents/report/detail/component/ApiScenarioCharResult";
import {REPORT_STATUS_MAP} from "@/business/utils/constants";
export default {
  name: "UiResult",
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
    uiResult: {
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
    uiResult() {
      this.getCaseCharData();
    }
  },
  created() {
    this.getCaseCharData();
  },
  methods: {
    getCaseCharData() {
      let caseCharData = [];
      if (this.uiResult.uiScenarioData) {
        this.uiResult.uiScenarioData.forEach(item => {
          let data = this.getDataByStatus(item.status);
          data.value = item.count;
          caseCharData.push(data);
        });
      }
      this.caseCharData = caseCharData;

      let uiScenarioData = [];
      if (this.uiResult.uiScenarioData) {
        this.uiResult.uiScenarioData.forEach(item => {
          let data = this.getDataByStatus(item.status);
          data.value = item.count;
          uiScenarioData.push(data);
        });
      }

      let stepCharData = [];
      for (let i = 0; i < this.uiResult.uiScenarioStepData.length; i++) {
        let stepItem = this.uiResult.uiScenarioStepData[i];
        let data = this.getDataByStatus(stepItem.status);
        data.value = stepItem.count;
        stepCharData.push(data);
      }

      this.scenarioCharData = uiScenarioData;
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
