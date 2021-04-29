<template>
  <ms-edit-dialog
    width="60%"
    :visible.sync="visible"
    @confirm="confirm"
    :title="$t('test_track.issue.create_issue')"
    append-to-body
    ref="msEditDialog">
    <template v-slot:default="scope">
      <issue-edit-detail :case-id="caseId" :is-plan="true" @refresh="$emit('refresh')" @close="handleClose" ref="issueEditDetail"/>
    </template>
  </ms-edit-dialog>
</template>
<script>

import TemplateComponentEditHeader
  from "@/business/components/track/plan/view/comonents/report/TemplateComponentEditHeader";
import IssueEditDetail from "@/business/components/track/issue/IssueEditDetail";
import MsEditDialog from "@/business/components/common/components/MsEditDialog";
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
      return this.$store.state.projectId;
    }
  },
  props: ['caseId'],
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
    }
  }
};
</script>

<style scoped>

</style>
