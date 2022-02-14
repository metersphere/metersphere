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
          if (value !== null && value.diffValue && value.diffValue !== 'null') {
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
        if (diffValue.globalScriptConfigRaw1 || diffValue.globalScriptConfigRaw2) {
          this.detail.script.globalScriptConfigRaw1 = diffValue.globalScriptConfigRaw1;
          this.detail.script.globalScriptConfigRaw2 = diffValue.globalScriptConfigRaw2;
        }
        if (diffValue.preProcessorRaw1 || diffValue.preProcessorRaw2) {
          this.detail.script.preProcessorRaw1 = diffValue.preProcessorRaw1;
          this.detail.script.preProcessorRaw2 = diffValue.preProcessorRaw2;
        }
        if (diffValue.preStepProcessorRaw1 || diffValue.preStepProcessorRaw2) {
          this.detail.script.preStepProcessorRaw1 = diffValue.preStepProcessorRaw1;
          this.detail.script.preStepProcessorRaw2 = diffValue.preStepProcessorRaw2;
        }
        if (diffValue.postProcessorRaw1 || diffValue.postProcessorRaw2) {
          this.detail.script.postProcessorRaw1 = diffValue.postProcessorRaw1;
          this.detail.script.postProcessorRaw2 = diffValue.postProcessorRaw2;
        }
        if (diffValue.postStepProcessorRaw1 || diffValue.postStepProcessorRaw2) {
          this.detail.script.postStepProcessorRaw1 = diffValue.postStepProcessorRaw1;
          this.detail.script.postStepProcessorRaw2 = diffValue.postStepProcessorRaw2;
        }
      },
    }
  }
</script>

<style scoped>
</style>
