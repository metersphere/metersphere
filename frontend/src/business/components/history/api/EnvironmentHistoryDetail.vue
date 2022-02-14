<template>
  <el-dialog :close-on-click-modal="false" :title="$t('operating_log.info')" :visible.sync="infoVisible" width="900px" :destroy-on-close="true"
             @close="handleClose" append-to-body>
    <div style="height: 700px;overflow: auto">
      <div v-if="detail.createUser">
        <p class="tip">{{ this.$t('report.user_name') }} ：{{ detail.createUser }}</p>
      </div>
      <div>
        <p class="tip">{{ this.$t('operating_log.time') }} ：{{ detail.operTime | timestampFormatDate }}</p>
      </div>
      <div style="overflow: auto">
        <p class="tip">{{ this.$t('report.test_log_details') }} </p>
        <ms-environment-edit-params :request="detail" v-if="detail.columnName === 'config'"/>
      </div>
    </div>
  </el-dialog>
</template>

<script>

import MsEnvironmentEditParams from "./EnvironmentEditParams";

export default {
  name: "EnvironmentHistoryDetail",
  components: {MsEnvironmentEditParams},
  props: {
    title: String,
  },
  data() {
    return {
      infoVisible: false,
      detail: {script: {}, type: ""},
    }
  },
  methods: {
    handleClose() {
      this.infoVisible = false;
    },
    open(value) {
      this.infoVisible = true;
      this.detail = value;
      let diffValue = value.diffValue;
      if (diffValue) {
        if (value != null && value.diffValue != 'null' && value.diffValue != '' && value.diffValue != undefined) {
          if (Object.prototype.toString.call(value.diffValue).match(/\[object (\w+)\]/)[1].toLowerCase() !== 'object'
            && Object.prototype.toString.call(value.diffValue).match(/\[object (\w+)\]/)[1].toLowerCase() !== 'array') {
            diffValue = JSON.parse(value.diffValue);
          }
        }
        if (diffValue.type === 'preAndPostScript') {
          this.formatScript(diffValue);
        }
        this.detail.type = diffValue.type;
      }
    },
    formatScript(diffValue) {
      this.detail.script = {};
      if (diffValue.globalScriptConfig_raw_1 || diffValue.globalScriptConfig_raw_2) {
        this.detail.script.globalScriptConfig_raw_1 = diffValue.globalScriptConfig_raw_1;
        this.detail.script.globalScriptConfig_raw_2 = diffValue.globalScriptConfig_raw_2;
      }
      if (diffValue.preProcessor_raw_1 || diffValue.preProcessor_raw_2) {
        this.detail.script.preProcessor_raw_1 = diffValue.preProcessor_raw_1;
        this.detail.script.preProcessor_raw_2 = diffValue.preProcessor_raw_2;
      }
      if (diffValue.preStepProcessor_raw_1 || diffValue.preStepProcessor_raw_2) {
        this.detail.script.preStepProcessor_raw_1 = diffValue.preStepProcessor_raw_1;
        this.detail.script.preStepProcessor_raw_2 = diffValue.preStepProcessor_raw_2;
      }
      if (diffValue.postProcessor_raw_1 || diffValue.postProcessor_raw_2) {
        this.detail.script.postProcessor_raw_1 = diffValue.postProcessor_raw_1;
        this.detail.script.postProcessor_raw_2 = diffValue.postProcessor_raw_2;
      }
      if (diffValue.postStepProcessor_raw_1 || diffValue.postStepProcessor_raw_2) {
        this.detail.script.postStepProcessor_raw_1 = diffValue.postStepProcessor_raw_1;
        this.detail.script.postStepProcessor_raw_2 = diffValue.postStepProcessor_raw_2;
      }
    },
  }
}
</script>

<style scoped>
</style>
