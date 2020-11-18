<template>
  <div class="text-container" style="border:1px #DCDFE6 solid; height: 100%;border-radius: 4px ;width: 88%">
    <el-form :model="response" ref="response" label-width="100px">

      <el-collapse-transition>
        <el-tabs v-model="activeName" v-show="isActive" style="margin: 20px">
          <el-tab-pane :label="$t('api_test.definition.request.response_header')" name="headers" class="pane">
            <ms-api-key-value :isShowEnable="false" :suggestions="headerSuggestions"
                              :items="response.headers"/>
          </el-tab-pane>
          <el-tab-pane :label="$t('api_test.definition.request.response_body')" name="body" class="pane">
            <ms-api-body
              :body="response.body"
              :extract="response.extract"/>
          </el-tab-pane>

          <el-tab-pane :label="$t('api_test.definition.request.status_code')" name="status_code" class="pane">
            <ms-api-key-value :isShowEnable="false" :suggestions="headerSuggestions"
                              :items="response.statusCode"/>
          </el-tab-pane>

          <el-tab-pane v-if="activeName == 'body'" :disabled="true" name="mode" class="pane cookie">
            <template v-slot:label>
              <ms-dropdown :commands="modes" :default-command="mode" @command="modeChange"/>
            </template>
          </el-tab-pane>

        </el-tabs>
      </el-collapse-transition>
    </el-form>
  </div>
</template>

<script>
  import MsAssertionResults from "./AssertionResults";
  import MsCodeEdit from "../../../../common/components/MsCodeEdit";
  import MsDropdown from "../../../../common/components/MsDropdown";
  import {BODY_FORMAT} from "../../model/ApiTestModel";
  import MsApiKeyValue from "../ApiKeyValue";
  import {REQUEST_HEADERS} from "@/common/js/constants";
  import MsApiBody from "../body/ApiBody";

  export default {
    name: "MsResponseText",
    components: {
      MsDropdown,
      MsCodeEdit,
      MsAssertionResults,
      MsApiKeyValue,
      MsApiBody,
    },

    props: {
      response: Object
    },

    data() {
      return {
        isActive: true,
        activeName: "headers",
        modes: ['text', 'json', 'xml', 'html'],
        mode: BODY_FORMAT.TEXT,
        headerSuggestions: REQUEST_HEADERS

      }
    },

    methods: {
      active() {
        this.isActive = !this.isActive;
      },
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
    background-color: white;
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
