<template>
  <div>
    <div style="border:1px #DCDFE6 solid; height: 100%;border-radius: 4px ;width: 98% ;">
      <el-form class="tcp" :model="request" ref="request" :disabled="isReadOnly" style="margin: 20px">
        <el-tabs v-model="activeName" class="request-tabs">

          <el-tab-pane name="parameters" :label="$t('api_test.definition.request.req_param')" v-if="request.parameters">
            <ms-api-key-value-detail :items="request.parameters" :show-required="true" :showDesc="true" :format="request.headerId"/>
          </el-tab-pane>
          <el-tab-pane :label="$t('api_test.definition.document.request_body')" name="request" v-if="request.body && (request.body.jsonSchema ||request.body.xml || request.body.raw_1 || request.body.raw_2)">
            <el-radio-group v-model="reportType" size="mini" style="margin: 10px 0px;">
              <el-radio :disabled="isReadOnly" label="json">
                json
              </el-radio>
              <el-radio :disabled="isReadOnly" label="xml">
                xml
              </el-radio>
              <el-radio :disabled="isReadOnly" label="raw">
                raw
              </el-radio>
            </el-radio-group>
            <div v-if="reportType === 'xml'">
              <pre v-html="getDiff(request.body.xml_2,request.body.xml_1)"></pre>
            </div>
            <div v-if="reportType === 'json'">
              <div class="send-request">
                <ms-json-code-edit :body="request.body" ref="jsonCodeEdit"/>
              </div>
            </div>
            <div v-if="reportType === 'raw'">
              <div class="send-request">
                <pre v-html="getDiff(request.body.raw_2,request.body.raw_1)"></pre>
              </div>
            </div>
          </el-tab-pane>

          <el-tab-pane :label="$t('api_test.definition.request.pre_script')" name="script" v-if="request.script_1 || request.script_2">
            <pre v-html="getDiff(request.script_2,request.script_1)"></pre>
          </el-tab-pane>

          <!--          <el-tab-pane :label="$t('api_test.definition.request.other_config')" name="other" class="other-config">-->

          <!--          </el-tab-pane>-->
        </el-tabs>
      </el-form>
    </div>
  </div>
</template>

<script>
import MsJsonCodeEdit from "./json-view/ComparedEditor";
import MsApiKeyValueDetail from "./common/ApiKeyValueDetail";

const jsondiffpatch = require('jsondiffpatch');
const formattersHtml = jsondiffpatch.formatters.html;

export default {
  name: "MsApiTcpParameters",
  components: {MsJsonCodeEdit, MsApiKeyValueDetail},
  props: {
    request: {},
    basisData: {},
    moduleOptions: Array,
    isReadOnly: {
      type: Boolean,
      default: false
    },
    showScript: {
      type: Boolean,
      default: true,
    },
    referenced: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      spanNum: 21,
      activeName: "request",
      reportType: "xml",
      isReloadData: false,
      refreshedXmlTable: true,
      currentProjectId: "",
    }
  },
  created() {
    if (this.request.body && (this.request.body.jsonSchema || this.request.body.xml || this.request.body.raw_1 || this.request.body.raw_2)) {
      this.activeName = "request";
      if (this.request.body.jsonSchema) {
        this.reportType = "json";
      }
      if (this.request.body.xml) {
        this.reportType = "xml";
      }
      if (this.request.body.raw_1 || this.request.body.raw_2) {
        this.reportType = "raw";
      }
    } else if (this.request.parameters) {
      this.activeName = "parameters";
    } else if (this.request.script_1 || this.request.script_2) {
      this.activeName = "script";
    }
  },
  watch: {
    'request.headerId'() {
      if (this.request.body) {
        this.activeName = "request";
        if (this.request.body.jsonSchema) {
          this.reportType = "json";
        }
        if (this.request.body.xml) {
          this.reportType = "xml";
        }
        if (this.request.body.raw_1 || this.request.body.raw_2) {
          this.reportType = "raw";
        }
      } else if (this.request.parameters) {
        this.activeName = "parameters";
      } else if (this.request.script_1 || this.request.script_2) {
        this.activeName = "script";
      }
    }
  },
  methods: {
    getDiff(v1, v2) {
      let delta = jsondiffpatch.diff(v1, v2);
      return formattersHtml.format(delta, v1);
    },
  }
}
</script>

<style scoped>
.tcp >>> .el-input-number {
  width: 100%;
}

.send-request {
  padding: 0px 0;
  height: 300px;
  border: 1px #DCDFE6 solid;
  border-radius: 4px;
  width: 100%;
}

.ms-left-cell {
  margin-top: 40px;
}

.ms-left-buttion {
  margin: 6px 0px 8px 30px;
}

/deep/ .el-form-item {
  margin-bottom: 15px;
}

.ms-left-cell {
  margin-top: 40px;
}

.ms-left-buttion {
  margin: 6px 0px 8px 30px;
}

/deep/ .el-form-item {
  margin-bottom: 15px;
}

/deep/ .instructions-icon {
  font-size: 14px !important;
}

.request-tabs {
  margin: 20px;
  min-height: 200px;
}

.other-config {
  padding: 15px;
}

@import "~jsondiffpatch/dist/formatters-styles/html.css";
@import "~jsondiffpatch/dist/formatters-styles/annotated.css";

</style>
