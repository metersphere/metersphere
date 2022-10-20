<template>
  <el-dialog :close-on-click-modal="false" :title="'测试结果'" width="60%"
             :visible.sync="visible" class="api-import" @close="close">
    <div v-loading="loading">
      <micro-app v-if="showReport" route-name="ApiReportView"
                 service="api"
                 :route-params="{response, reportId}"/>
    </div>
  </el-dialog>
</template>

<script>
import MicroApp from "metersphere-frontend/src/components/MicroApp";
import {apiDefinitionPlanReportGetByCaseId} from "@/api/remote/api/api-definition-report";

export default {
  name: "TestPlanApiCaseResult",
  components: {MicroApp},
  data() {
    return {
      visible: false,
      loading: false,
      response: null,
      reportId: null,
      showReport: true // 需要加载后再打开报告
    }
  },
  methods: {
    close() {
      this.visible = false;
      this.reportId = null;
      this.response = null;
    },
    openByCaseId(caseId) {
      this.visible = true;
      this.loading = true;
      this.showReport = false;
      this.$nextTick(() => {
        apiDefinitionPlanReportGetByCaseId(caseId)
            .then(r => {
              this.loading = false;
              this.showReport = true;
              if (r.data) {
                this.response = JSON.parse(r.data.content);
              }
            });
      });
    },
    open(reportId) {
      this.visible = true;
      this.showReport = false;
      this.$nextTick(() => {
        this.showReport = true;
        this.reportId = reportId;
      });
    }
  }
}
</script>

<style scoped>

</style>
