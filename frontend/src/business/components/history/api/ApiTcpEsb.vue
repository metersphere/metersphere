<template>
  <div>
    <div style="border:1px #DCDFE6 solid; height: 100%;border-radius: 4px ;width: 98% ;">
      <el-form class="tcp" :model="request" ref="request" :disabled="isReadOnly" style="margin: 20px">
        <el-tabs v-model="activeName" class="request-tabs">

          <el-tab-pane name="parameters" :label="$t('api_test.definition.request.req_param')"
                       v-if="request.query1 || request.query2">
            <pre v-html="getDiff(request.query2,request.query1)"></pre>
          </el-tab-pane>

          <!--报文模版-->
          <el-tab-pane :label="$t('api_test.definition.request.message_template')" name="request"
                       v-if="request.request1 || request.request2">
            <pre v-html="getDiff(request.request2,request.request1)"></pre>
          </el-tab-pane>

          <!--其他设置-->
          <el-tab-pane :label="$t('api_test.definition.request.other_config')" name="other" class="other-config"
                       v-if="request.otherConfig">
            <el-table :data="request.otherConfig">
              <el-table-column prop="columnTitle" :label="$t('operating_log.change_field')"/>
              <el-table-column prop="originalValue" :label="$t('operating_log.before_change')">
                <template v-slot:default="scope">
                  <el-tooltip :content="scope.row.originalValue">
                    <div class="current-value ms-tag-del">{{ scope.row.originalValue }}</div>
                  </el-tooltip>
                </template>
              </el-table-column>
              <el-table-column prop="newValue" :label="$t('operating_log.after_change')">
                <template v-slot:default="scope">
                  <el-tooltip :content="scope.row.newValue">
                    <div class="current-value ms-tag-add">{{ scope.row.newValue }}</div>
                  </el-tooltip>
                </template>
              </el-table-column>
            </el-table>
          </el-tab-pane>

          <el-tab-pane :label="$t('api_test.definition.request.response_template')" name="esbData" class="pane"
                       v-if="request.backEsbDataStruct1 || request.backEsbDataStruct2">
            <pre v-html="getDiff(request.backEsbDataStruct2,request.backEsbDataStruct1)"></pre>
          </el-tab-pane>

          <el-tab-pane :label="$t('api_test.definition.request.post_script')" name="backScript" class="pane"
                       v-if="request.backScript1 || request.backScript2">
            <pre v-html="getDiff(request.backScript2 ,request.backScript1)"></pre>
          </el-tab-pane>

        </el-tabs>
      </el-form>
    </div>
  </div>
</template>

<script>
import MsJsonCodeEdit from "./json-view/ComparedEditor";
import MsApiKeyValueDetail from "./common/ApiKeyValueDetail";
import MsCodeEdit from "@/business/components/common/components/MsCodeEdit";
const jsondiffpatch = require('jsondiffpatch');
const formattersHtml = jsondiffpatch.formatters.html;

export default {
  name: "MsApiTcpParameters",
  components: {MsJsonCodeEdit, MsApiKeyValueDetail, MsCodeEdit},
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
    } else if (this.request.query1 || this.request.query2) {
      this.activeName = "parameters";
    } else if (this.request.request1 || this.request.request2) {
      this.activeName = "request";
    } else if (this.request.script1 || this.request.script2) {
      this.activeName = "script";
    } else if (this.request.otherConfig) {
      this.activeName = "other";
    } else if (this.request.backEsbDataStruct1 || this.request.backEsbDataStruct2) {
      this.activeName = "esbData";
    } else if (this.request.backScript1 || this.request.backScript2) {
      this.activeName = "backScript";
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
      } else if (this.request.query_1 || this.request.query_2) {
        this.activeName = "parameters";
      } else if (this.request.request_1 || this.request.request_2) {
        this.activeName = "request";
      } else if (this.request.script_1 || this.request.script_2) {
        this.activeName = "script";
      } else if (this.request.other_config) {
        this.activeName = "other";
      } else if (this.request.backEsbDataStruct_1 || this.request.backEsbDataStruct_2) {
        this.activeName = "esbData";
      } else if (this.request.backScript_1 || this.request.backScript_2) {
        this.activeName = "backScript";
      }
    }
  },
  methods: {
    getDiff(v1, v2) {
      let delta = jsondiffpatch.diff(JSON.parse(v1), JSON.parse(v2));
      return formattersHtml.format(delta, JSON.parse(v1));
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

.current-value {
  display: inline-block;
  overflow-x: hidden;
  padding-bottom: 0;
  text-overflow: ellipsis;
  vertical-align: middle;
  white-space: nowrap;
  width: 120px;
}

.ms-tag-del {
  text-decoration: line-through;
  text-decoration-color: red;
  -moz-text-decoration-line: line-through;
  background: #F3E6E7;
}

.ms-tag-add {
  background: #E2ECDC;
}

@import "~jsondiffpatch/dist/formatters-styles/html.css";
@import "~jsondiffpatch/dist/formatters-styles/annotated.css";

</style>
