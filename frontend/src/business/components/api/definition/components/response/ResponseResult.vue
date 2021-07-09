<template>
  <div class="text-container">

    <el-tabs v-model="activeName" v-show="isActive">
      <el-tab-pane :label="$t('api_test.definition.request.response_body')" name="body" class="pane">
        <ms-sql-result-table v-if="isSqlType" :body="response.responseResult.body"/>
        <ms-code-edit v-if="!isSqlType && isMsCodeEditShow" :mode="mode" :read-only="true" :modes="modes" :data.sync="response.responseResult.body" ref="codeEdit"/>
      </el-tab-pane>

      <el-tab-pane :label="$t('api_test.definition.request.response_header')" name="headers" class="pane">
        <ms-code-edit :mode="'text'" :read-only="true" :data.sync="response.responseResult.headers"/>
      </el-tab-pane>

      <el-tab-pane :label="$t('api_test.definition.request.console')" name="console" class="pane">
        <ms-code-edit :mode="'text'" :read-only="true" :data.sync="response.responseResult.console"/>
      </el-tab-pane>

      <el-tab-pane :label="$t('api_report.assertions')" name="assertions" class="pane assertions">
        <ms-assertion-results :assertions="response.responseResult.assertions"/>
      </el-tab-pane>

      <el-tab-pane :label="$t('api_test.request.extract.label')" name="label" class="pane">
        <ms-code-edit :mode="'text'" :read-only="true" :data.sync="response.responseResult.vars"/>
      </el-tab-pane>

      <el-tab-pane :label="$t('api_report.request_body')" name="request_body" class="pane">
        <ms-code-edit :mode="'text'" :read-only="true" :data.sync="reqMessages"/>
      </el-tab-pane>

      <el-tab-pane v-if="activeName == 'body'" :disabled="true" name="mode" class="pane cookie">
        <template v-slot:label>
          <ms-dropdown v-if="currentProtocol==='SQL'" :commands="sqlModes" :default-command="mode" @command="sqlModeChange"/>
          <ms-dropdown v-else :commands="modes" :default-command="mode" @command="modeChange" ref="modeDropdown"/>
        </template>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
  import MsAssertionResults from "./AssertionResults";
  import MsCodeEdit from "../MsCodeEdit";
  import MsDropdown from "../../../../common/components/MsDropdown";
  import {BODY_FORMAT} from "../../model/ApiTestModel";
  import MsSqlResultTable from "./SqlResultTable";

  export default {
    name: "MsResponseResult",

    components: {
      MsDropdown,
      MsCodeEdit,
      MsAssertionResults,
      MsSqlResultTable
    },

    props: {
      response: Object,
      currentProtocol: String,
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
    methods: {
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
            this.response.responseResult = {};
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
    computed: {
      isSqlType() {
        return (this.currentProtocol === "SQL" && this.response.responseResult.responseCode === '200' && this.mode === 'table');
      }
    }
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
    background-color: #F5F5F5;
  padding: 1px 0;
    height: 250px;
    overflow-y: auto;
  }

  .text-container .pane.cookie {
    padding: 0;
  }

  /deep/ .el-tabs__nav-wrap::after {
    height: 0px;
  }

  .ms-div {
    margin-top: 20px;
  }

  pre {
    margin: 0;
  }
</style>
