<template>
  <ms-edit-dialog
    width="600px"
    :visible.sync="visible"
    @confirm="confirm"
    :title="$t('test_track.report.template_configuration')"
    append-to-body
    ref="msEditDialog">
    <el-scrollbar>
      <div class="config">
        <test-plan-report-config :config="editConfig"/>
      </div>
    </el-scrollbar>
  </ms-edit-dialog>
</template>
<script>


import MsEditDialog from "@/business/components/common/components/MsEditDialog";
import TestPlanReportConfig
  from "@/business/components/track/plan/view/comonents/report/detail/component/TestPlanReportConfig";
import {editPlanReportConfig} from "@/network/test-plan";
export default {
  name: "TestPlanReportEdit",
  components: {TestPlanReportConfig, MsEditDialog},
  data() {
    return {
      visible: false,
      editConfig: {}
    }
  },
  props: {
    planId: String,
    config: {
      type: [Object, String],
    }
  },
  methods: {
    open() {
      this.visible = true;
      this.editConfig = JSON.parse(JSON.stringify(this.config));
    },
    handleClose() {
      this.visible = false;
    },
    confirm() {
      let param = {
        id: this.planId,
        reportConfig: JSON.stringify(this.editConfig)
      };
      editPlanReportConfig(param, () => {
        this.$emit('update:config', JSON.parse(JSON.stringify(this.editConfig)));
      });
      this.$emit('refresh');
      this.visible = false;
    }
  }
};
</script>

<style scoped>
.config {
  margin-left: 200px;
}
</style>
