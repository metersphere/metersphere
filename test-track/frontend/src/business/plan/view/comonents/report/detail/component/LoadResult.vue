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

import MsPieChart from "metersphere-frontend/src/components/MsPieChart";
import MsDoughnutPieChart from "metersphere-frontend/src/components/MsDoughnutPieChart";
import {REPORT_STATUS_MAP} from "@/business/utils/constants";
export default {
  name: "loadResult",
  components: {MsDoughnutPieChart, MsPieChart},
  data() {
    return {
      caseDataMap: REPORT_STATUS_MAP,
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
