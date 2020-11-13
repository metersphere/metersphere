<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
  <el-form :model="request" :rules="rules" ref="request" label-width="100px" :disabled="isReadOnly">
    <el-tabs v-model="activeName">
      <!-- 请求头-->
      <el-tab-pane :label="$t('api_test.request.headers')" name="headers">
        <ms-api-key-value :is-read-only="isReadOnly" :isShowEnable="isShowEnable" :suggestions="headerSuggestions"
                          :items="request.headers"/>
      </el-tab-pane>

      <!--query 参数-->
      <el-tab-pane :label="$t('api_test.delimit.request.query_param')" name="parameters">
        <ms-api-variable :is-read-only="isReadOnly"
                         :parameters="request.parameters"
                         :environment="request.environment"
                         :extract="request.extract"/>
      </el-tab-pane>

      <!--REST 参数-->
      <el-tab-pane :label="$t('api_test.delimit.request.rest_param')" name="rest">
        <ms-api-key-value :is-read-only="isReadOnly" :isShowEnable="isShowEnable" :suggestions="headerSuggestions"
                          :items="request.rest"/>
      </el-tab-pane>

      <!--请求体-->
      <el-tab-pane :label="$t('api_test.request.body')" name="body">
        <ms-api-body :is-read-only="isReadOnly"
                     :body="request.body"
                     :extract="request.extract"
                     :environment="request.environment"/>
      </el-tab-pane>
      <!-- 认证配置 -->
      <el-tab-pane :label="$t('api_test.delimit.request.auth_config')" name="authConfig">
        <ms-api-auth-config :request="request" :is-read-only="isReadOnly" :auth-config="request.authConfig"/>
      </el-tab-pane>

      <el-tab-pane :label="$t('api_test.request.processor.pre_exec_script')" name="jsr223PreProcessor">
        <ms-jsr233-processor :is-read-only="isReadOnly" :jsr223-processor="request.jsr223PreProcessor"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.request.processor.post_exec_script')" name="jsr223PostProcessor">
        <ms-jsr233-processor :is-read-only="isReadOnly" :jsr223-processor="request.jsr223PostProcessor"/>
      </el-tab-pane>

    </el-tabs>
  </el-form>
</template>

<script>
  import MsApiKeyValue from "../ApiKeyValue";
  import MsApiBody from "../body/ApiBody";
  import MsApiAuthConfig from "../auth/ApiAuthConfig";
  import {HttpRequest, KeyValue, Scenario} from "../../model/ApiTestModel";
  import MsApiExtract from "../extract/ApiExtract";
  import ApiRequestMethodSelect from "../collapse/ApiRequestMethodSelect";
  import {REQUEST_HEADERS} from "@/common/js/constants";
  import MsApiVariable from "../ApiVariable";
  import MsJsr233Processor from "../processor/Jsr233Processor";
  import MsApiAdvancedConfig from "../ApiAdvancedConfig";

  export default {
    name: "MsApiHttpRequestForm",
    components: {
      MsJsr233Processor,
      MsApiAdvancedConfig,
      MsApiVariable, ApiRequestMethodSelect, MsApiExtract, MsApiAuthConfig, MsApiBody, MsApiKeyValue
    },
    props: {
      request: HttpRequest,
      isShowEnable:Boolean,
      jsonPathList: Array,
      scenario: Scenario,
      isReadOnly: {
        type: Boolean,
        default: false
      }
    },

    data() {
      let validateURL = (rule, value, callback) => {
        try {
          new URL(this.addProtocol(this.request.url));
        } catch (e) {
          callback(this.$t('api_test.request.url_invalid'));
        }
      };
      return {
        activeName: "headers",
        rules: {
          name: [
            {max: 300, message: this.$t('commons.input_limit', [1, 300]), trigger: 'blur'}
          ],
          url: [
            {max: 500, required: true, message: this.$t('commons.input_limit', [1, 500]), trigger: 'blur'},
            {validator: validateURL, trigger: 'blur'}
          ],
          path: [
            {max: 500, message: this.$t('commons.input_limit', [0, 500]), trigger: 'blur'},
          ]
        },
        headerSuggestions: REQUEST_HEADERS
      }
    },

    methods: {
      urlChange() {
        if (!this.request.url) return;
        let url = this.getURL(this.addProtocol(this.request.url));
        if (url) {
          this.request.url = decodeURIComponent(url.origin + url.pathname);
        }
      },
      pathChange() {
        if (!this.request.path) return;
        let url = this.getURL(this.displayUrl);
        let urlStr = url.origin + url.pathname;
        let envUrl = this.scenario.environment.config.httpConfig.protocol + '://' + this.scenario.environment.config.httpConfig.socket;
        this.request.path = decodeURIComponent(urlStr.substring(envUrl.length, urlStr.length));
      },
      getURL(urlStr) {
        try {
          let url = new URL(urlStr);
          url.searchParams.forEach((value, key) => {
            if (key && value) {
              this.request.parameters.splice(0, 0, new KeyValue({name: key, value: value}));
            }
          });
          return url;
        } catch (e) {
          this.$error(this.$t('api_test.request.url_invalid'), 2000);
        }
      },
      methodChange(value) {
        if (value === 'GET' && this.activeName === 'body') {
          this.activeName = 'parameters';
        }
      },
      useEnvironmentChange(value) {
        if (value && !this.scenario.environment) {
          this.$error(this.$t('api_test.request.please_add_environment_to_scenario'), 2000);
          this.request.useEnvironment = false;
        }
        this.$refs["request"].clearValidate();
      },
      addProtocol(url) {
        if (url) {
          if (!url.toLowerCase().startsWith("https") && !url.toLowerCase().startsWith("http")) {
            return "https://" + url;
          }
        }
        return url;
      },
      runDebug() {
        this.$emit('runDebug');
      }
    },

    computed: {
      displayUrl() {
        return (this.scenario.environment && this.scenario.environment.config.httpConfig.socket) ?
          this.scenario.environment.config.httpConfig.protocol + '://' + this.scenario.environment.config.httpConfig.socket + (this.request.path ? this.request.path : '')
          : '';
      }
    }
  }
</script>

<style scoped>

  .el-tag {
    width: 100%;
    height: 40px;
    line-height: 40px;
  }

  .environment-display {
    font-size: 14px;
  }

  .environment-name {
    font-weight: bold;
    font-style: italic;
  }

  .adjust-margin-bottom {
    margin-bottom: 10px;
  }

  .environment-url-tip {
    color: #F56C6C;
  }

  .follow-redirects-item {
    margin-left: 30px;
  }

  .do-multipart-post {
    margin-left: 10px;
  }

</style>
