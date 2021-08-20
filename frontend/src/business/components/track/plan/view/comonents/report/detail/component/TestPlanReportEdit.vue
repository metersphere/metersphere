<template>
  <ms-edit-dialog
    width="60%"
    :visible.sync="visible"
    @confirm="confirm"
    :title="'模板配置'"
    append-to-body
    ref="msEditDialog">

  </ms-edit-dialog>
</template>
<script>


import MsEditDialog from "@/business/components/common/components/MsEditDialog";
import {getCurrentProjectID} from "@/common/js/utils";
export default {
  name: "TestPlanReportEdit",
  components: {MsEditDialog},
  data() {
    return {
      visible: false,
      config: {
        api: {

        }
      }
    }
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    }
  },
  props: ['caseId', 'planId'],
  methods: {
    open(data) {
      this.visible = true;
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
