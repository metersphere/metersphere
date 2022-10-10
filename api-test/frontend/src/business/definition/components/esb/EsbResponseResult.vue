<template>
  <div class="text-container">

    <el-tabs v-model="activeName" v-show="isActive">
      <el-tab-pane :label="$t('api_test.definition.request.response_template')" name="esbData" class="pane">
        <esb-table :table-data="request.backEsbDataStruct" :show-options-button="showOptionsButton" @saveTableData="validateEsbDataStruct" ref="esbTable"></esb-table>
      </el-tab-pane>
      <el-tab-pane v-if="isApiComponent === false" :label="$t('api_test.definition.request.response_body')" name="body"
                   class="pane">
        <ms-code-edit v-if="isMsCodeEditShow" :mode="mode" :read-only="true" :modes="modes"  height="200px"
                      :data.sync="response.responseResult.body" ref="codeEdit"/>
      </el-tab-pane>

      <el-tab-pane v-if="isApiComponent === false" :label="$t('api_test.definition.request.console')" name="console"
                   class="pane">
        <ms-code-edit :mode="'text'" :read-only="true" :data.sync="response.responseResult.console" height="200px"/>
      </el-tab-pane>

      <el-tab-pane v-if="isApiComponent === false" :label="$t('api_report.assertions')" name="assertions"
                   class="pane assertions">
        <ms-assertion-results :assertions="response.responseResult.assertions"/>
      </el-tab-pane>

      <el-tab-pane v-if="isApiComponent === false" :label="$t('api_test.request.extract.label')" name="label"
                   class="pane">
        <ms-code-edit :mode="'text'" :read-only="true" :data.sync="response.responseResult.vars" height="200px"/>
      </el-tab-pane>

      <el-tab-pane v-if="isApiComponent === false" :label="$t('api_report.request_body')" name="request_body"
                   class="pane">
        <ms-code-edit :mode="'text'" :read-only="true" :data.sync="reqMessages" height="200px"/>
      </el-tab-pane>
      <el-tab-pane v-if="isApiComponent === true" :label="$t('api_test.definition.request.post_script')" name="script">
        <jsr233-processor-content
          :jsr223-processor="request.backScript"
          :is-pre-processor="false"
          :is-read-only="false"/>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import MsAssertionResults from "@/business/definition/components/response/AssertionResults";
import MsCodeEdit from "@/business/definition/components/MsCodeEdit";
import MsDropdown from "@/business/commons/MsDropdown";
import {BODY_FORMAT} from "@/business/definition/model/ApiTestModel";
import MsSqlResultTable from "@/business/definition/components/response/SqlResultTable";
import EsbTable from "./EsbTable";
import Jsr233ProcessorContent from "@/business/automation/scenario/common/Jsr233ProcessorContent";
import {createComponent} from "@/business/definition/components/jmeter/components";

export default {
  name: "EsbResponseResult",

  components: {
    EsbTable,
    MsDropdown,
    MsCodeEdit,
    MsAssertionResults,
    MsSqlResultTable,
    Jsr233ProcessorContent
  },

  props: {
    response: Object,
    isApiComponent: Boolean,
    showOptionsButton: Boolean,
    request:{}
  },

  data() {
    return {
      isActive: true,
      activeName: "body",
      modes: ['text', 'json', 'xml', 'html'],
      sqlModes: ['text', 'table'],
      mode: BODY_FORMAT.TEXT,
      isMsCodeEditShow: true,
      reqMessages: "",
    }
  },
  watch: {
    response() {
      this.setBodyType();
      this.setReqMessage();
    }
  },
  created() {
    if(this.isApiComponent){
      this.activeName = "esbData";
    }
    if(this.response == null ){
      this.response = {};
    }
    if(this.request.backScript == null){
      this.request.backScript = createComponent("JSR223PostProcessor");
    }
  },
  methods: {
    validateEsbDataStruct(esbDataStruct){
      this.refreshEsbDataStruct(esbDataStruct);
      let result = this.checkEsbDataStructData(esbDataStruct);
      return result;
    },
    refreshEsbDataStruct(esbDataStruct){
      if(esbDataStruct && esbDataStruct.length > 0){
        esbDataStruct.forEach( row => {
          row.status = "";
          if(row.children == null || row.children.length === 0){
            row.children = [];
          }else if(row.children.length>0){
            this.refreshEsbDataStruct(row.children);
          }
        });
      }
    },
    checkEsbDataStructData(esbDataStruct){
      let allCheckResult = true;
      if(esbDataStruct && esbDataStruct.length > 0){
        for(let i = 0;i<esbDataStruct.length;i++){
          let row = esbDataStruct[i];
          allCheckResult = this.$refs.esbTable.validateRowData(row);
          if(allCheckResult){
            if(row.children != null && row.children.length > 0){
              allCheckResult = this.checkEsbDataStructData(row.children);
              if(!allCheckResult){
                return false;
              }
            }
          }else{
            return false;
          }
        }
      }
      return allCheckResult;
    },
    modeChange(mode) {
      this.mode = mode;
    },
    sqlModeChange(mode) {
      this.mode = mode;
    },
    setBodyType() {
      if (!this.response.responseResult || !this.response.responseResult.headers) {
        return;
      }
      if (this.response.responseResult.headers.indexOf("Content-Type: application/json") > 0) {
        this.mode = BODY_FORMAT.JSON;
        this.$nextTick(() => {
          if (this.$refs.modeDropdown) {
            this.$refs.modeDropdown.handleCommand(BODY_FORMAT.JSON);
            this.msCodeReload();
          }
        })
      }
    },
    msCodeReload() {
      this.isMsCodeEditShow = false;
      this.$nextTick(() => {
        this.isMsCodeEditShow = true;
      });
    },
    setReqMessage() {
      if (this.response) {
        if (!this.response.url) {
          this.response.url = "";
        }
        if (!this.response.headers) {
          this.response.headers = "";
        }
        if (!this.response.cookies) {
          this.response.cookies = "";
        }
        if (!this.response.body) {
          this.response.body = "";
        }
        if (!this.response.responseResult) {
          this.response.responseResult= {};
        }
        if (!this.response.responseResult.vars) {
          this.response.responseResult.vars = "";
        }
        this.reqMessages = this.$t('api_test.request.address') + ":\n" + this.response.url + "\n" +
          this.$t('api_test.scenario.headers') + ":\n" + this.response.headers + "\n" + "Cookies :\n" +
          this.response.cookies + "\n" + "Body:" + "\n" + this.response.body;
      }
    },
  },
  mounted() {
    this.setBodyType();
    this.setReqMessage();
  },
  computed: {}
}
</script>

<style scoped>
.text-container .icon {
  padding: 5px;
}

.text-container .collapse {
  cursor: pointer;
}

.text-container .collapse:hover {
  opacity: 0.8;
}

.text-container .icon.is-active {
  transform: rotate(90deg);
}

.text-container .pane {
  /*background-color: #F5F5F5;*/
  padding: 0 10px;
  /*height: 250px;*/
  overflow-y: auto;
}

.text-container .pane.cookie {
  padding: 0;
}

:deep(.el-tabs__nav-wrap::after) {
  height: 0px;
}

.ms-div {
  margin-top: 20px;
}

pre {
  margin: 0;
}
</style>
