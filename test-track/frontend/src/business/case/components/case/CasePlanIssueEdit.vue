<template>
  <ms-drawer-component
    :enableSelect="false"
    :title="$t('test_track.issue.create_issue')"
    @confirm="confirm"
    @saveAndReset="saveAndReset"
    ref="msEditDialog"
    :enablePagination="false"
    :enableSaveAndReset="true"
  >
    <div slot="content">
      <issue-edit-detail
        :plan-case-id="planCaseId"
        :is-minder="isMinder"
        :plan-id="planId"
        :case-id="caseId"
        :is-case-edit="true"
        @refresh="refresh"
        @close="handleClose"
        ref="issueEditDetail"
      />
    </div>
  </ms-drawer-component>

  <!-- <ms-edit-dialog
    width="60%"
    :visible.sync="visible"
    @confirm="confirm"
    :title="$t('test_track.issue.create_issue')"
    append-to-body
    ref="msEditDialog"
  >
    <template>
      <issue-edit-detail
        :plan-case-id="planCaseId"
        :is-minder="isMinder"
        :plan-id="planId"
        :case-id="caseId"
        :is-case-edit="true"
        @refresh="refresh"
        @close="handleClose"
        ref="issueEditDetail"
      />
    </template>
  </ms-edit-dialog> -->
</template>
<script>
import TemplateComponentEditHeader from "@/business/plan/view/comonents/report/TemplateComponentEditHeader";
import IssueEditDetail from "./CaseIssueEditDetail";
import MsEditDialog from "metersphere-frontend/src/components/MsEditDialog";
import { getCurrentProjectID } from "metersphere-frontend/src/utils/token";
import MsDrawerComponent from "../common/MsDrawerComponent";
export default {
  name: "CasePlanIssueEdit",
  components: {
    MsEditDialog,
    IssueEditDetail,
    TemplateComponentEditHeader,
    MsDrawerComponent,
  },
  data() {
    return {
      visible: false,
    };
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
  },
  props: {
    planId: String,
    caseId: String,
    copyCaseId: String,
    planCaseId: String,
    isMinder: Boolean,
  },
  methods: {
    open(data) {
      this.visible = true;
      this.$refs.msEditDialog.open();
      this.$nextTick(() => {
        this.$refs.issueEditDetail.open(data);
      });
    },
    handleClose() {
      this.visible = false;
      this.$refs.msEditDialog.close();
    },
    confirm() {
      this.$refs.issueEditDetail.save();
    },
    refresh(data) {
      this.$emit("refresh", data);
    },
    saveAndReset() {
      this.$refs.issueEditDetail.save(true);
    },
  },
};
</script>

<style scoped></style>
