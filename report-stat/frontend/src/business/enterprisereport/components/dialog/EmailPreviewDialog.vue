<template>
  <div>
    <el-dialog v-loading="loading"
               :close-on-click-modal="false"
               :destroy-on-close="true"
               :visible.sync="dialogFormVisible"
               top="8vh"
               width="80%">

      <email-preview @refresh="refresh" @closeDialog="close" :show-button="showButton" v-if="dialogFormVisible"
                     :data.sync="this.reportData"/>
    </el-dialog>
  </div>

</template>

<script>

import EmailPreview from "@/business/enterprisereport/preview/EmailPreview";

export default {
  name: "SelectReportStatistics",
  components: {EmailPreview},
  data() {
    return {
      loading: false,
      reportData: {},
      dialogFormVisible: false,
    };
  },
  props: {
    showButton: {
      type: Boolean,
      default: false
    }
  },
  computed: {},
  watch: {
    recordName() {
      this.searchReportRecord();
    }
  },
  methods: {
    open(param) {
      this.reportData = {};
      if (param) {
        this.reportData = param;
      }
      this.dialogFormVisible = true;
    },
    close() {
      this.reportData = {};
      this.dialogFormVisible = false;
    },
    refresh() {
      this.$emit("refresh");
    }
  }
}
</script>

<style scoped>
</style>
