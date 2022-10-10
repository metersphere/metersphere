<template>
  <ms-edit-dialog
    width="60%"
    :visible.sync="visible"
    @confirm="confirm"
    :title="$t('test_track.issue.create_issue')"
    append-to-body
    ref="msEditDialog">
    <template>
      <issue-edit-detail :plan-case-id="planCaseId" :is-minder="isMinder" :plan-id="planId" :case-id="caseId" :is-case-edit="true" @refresh="refresh" @close="handleClose" ref="issueEditDetail"/>
    </template>
  </ms-edit-dialog>
</template>
<script>

import TemplateComponentEditHeader from "@/business/plan/view/comonents/report/TemplateComponentEditHeader";
import IssueEditDetail from "@/business/issue/IssueEditDetail";
import MsEditDialog from "metersphere-frontend/src/components/MsEditDialog";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
export default {
  name: "TestPlanIssueEdit",
  components: {MsEditDialog, IssueEditDetail, TemplateComponentEditHeader},
  data() {
    return {
      visible: false
    }
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    }
  },
  props: {
    planId: String,
    caseId: String,
    planCaseId: String,
    isMinder: Boolean,
  },
  methods: {
    open(data) {
      this.visible = true;
      this.$nextTick(() => {
        this.$refs.issueEditDetail.open(data);
      })
    },
    handleClose() {
      this.visible = false;
    },
    confirm() {
      this.$refs.issueEditDetail.save();
    },
    refresh(data) {
      this.$emit('refresh', data);
    }
  }
};
</script>

<style scoped>

</style>
