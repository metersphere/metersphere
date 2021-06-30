<template>

  <el-dialog :close-on-click-modal="false" :title="$t('operating_log.info')" :visible.sync="infoVisible" width="900px" :destroy-on-close="true"
             @close="handleClose" append-to-body>
    <div style="height: 700px;overflow: auto">
      <div v-if="detail.createUser">
        <p class="tip">{{ this.$t('report.user_name') }} ：{{detail.createUser}}</p>
      </div>
      <div>
        <p class="tip">{{ this.$t('operating_log.time') }} ：{{ detail.operTime | timestampFormatDate }}</p>
      </div>
      <div style="overflow: auto">
        <p class="tip">{{ this.$t('report.test_log_details') }} </p>
        <el-row>
          <pre v-html="getDiff(detail.originalValue,detail.newValue)"></pre>
          <!--</el-col>-->
        </el-row>
      </div>
    </div>
  </el-dialog>
</template>

<script>
  const jsondiffpatch = require('jsondiffpatch');
  const formattersHtml = jsondiffpatch.formatters.html;
  export default {
    name: "MsHistoryDetail",
    components: {},
    props: {
      title: String,
    },
    data() {
      return {
        infoVisible: false,
        detail: {},
        formatData: ["loadConfiguration", "advancedConfiguration", "config", "variables", "tags", "customFields", "steps", "scenarioDefinition", "request", "response"],
      }
    },
    methods: {
      getDiff(v1, v2) {
        let delta = jsondiffpatch.diff(v1, v2);
        return formattersHtml.format(delta, v1);
      },
      handleClose() {
        this.infoVisible = false;
      },
      open(value) {
        this.infoVisible = true;
        this.detail = value;
      },
      getType(type) {
        return this.LOG_TYPE_MAP.get(type);
      },
    }
  }
</script>

<style scoped>
  @import "~jsondiffpatch/dist/formatters-styles/html.css";
  @import "~jsondiffpatch/dist/formatters-styles/annotated.css";
</style>
