<template>
  <div class="text-container">

    <el-tabs v-model="activeName" v-show="isActive">
      <el-tab-pane :label="$t('api_test.definition.request.response_header')" name="headers" class="pane">
        <pre>{{ response.responseResult.headers }}</pre>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.definition.request.response_body')" name="body" class="pane">
        <ms-code-edit :mode="mode" :read-only="true" :modes="modes" :data.sync="response.responseResult.body" ref="codeEdit"/>
      </el-tab-pane>
      <el-tab-pane label="Cookie" name="cookie" class="pane cookie">
        <pre>{{response.cookies}}</pre>
      </el-tab-pane>

      <el-tab-pane :label="$t('api_test.definition.request.console')" name="console" class="pane">
        <pre>{{response.responseResult.console}}</pre>
      </el-tab-pane>

      <el-tab-pane :label="$t('api_report.assertions')" name="assertions" class="pane assertions">
        <ms-assertion-results :assertions="response.responseResult.assertions"/>
      </el-tab-pane>

      <el-tab-pane :label="$t('api_test.request.extract.label')" name="label" class="pane">
        <pre>{{response.responseResult.vars}}</pre>
      </el-tab-pane>


      <el-tab-pane v-if="activeName == 'body'" :disabled="true" name="mode" class="pane cookie">
        <template v-slot:label>
          <ms-dropdown :commands="modes" :default-command="mode" @command="modeChange"/>
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

  export default {
    name: "MsResponseResult",

    components: {
      MsDropdown,
      MsCodeEdit,
      MsAssertionResults,
    },

    props: {
      response: Object,
    },

    data() {
      return {
        isActive: true,
        activeName: "headers",
        modes: ['text', 'json', 'xml', 'html'],
        mode: BODY_FORMAT.TEXT
      }
    },
    methods: {
      modeChange(mode) {
        this.mode = mode;
      }
    },
    mounted() {
      if (!this.response.headers) {
        return;
      }
      if (this.response.headers.indexOf("Content-Type: application/json") > 0) {
        this.mode = BODY_FORMAT.JSON;
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
    padding: 0 10px;
    height: 250px;
    overflow-y: auto;
  }

  .text-container .pane.cookie {
    padding: 0;
  }

  pre {
    margin: 0;
  }
</style>
