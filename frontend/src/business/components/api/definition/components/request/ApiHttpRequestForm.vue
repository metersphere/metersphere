<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
  <el-row>
    <el-col :span="21">
      <!-- HTTP 请求参数 -->
      <div style="border:1px #DCDFE6 solid; height: 100%;border-radius: 4px ;width: 100%">
        <el-tabs v-model="activeName" style="margin: 20px">
          <!-- 请求头-->
          <el-tab-pane :label="$t('api_test.request.headers')" name="headers">
            <ms-api-key-value :is-read-only="isReadOnly" :isShowEnable="isShowEnable" :suggestions="headerSuggestions"
                              :items="headers"/>
          </el-tab-pane>

          <!--query 参数-->
          <el-tab-pane :label="$t('api_test.definition.request.query_param')" name="parameters">
            <ms-api-variable :is-read-only="isReadOnly"  :parameters="request.arguments"/>
          </el-tab-pane>

          <!--REST 参数-->
          <el-tab-pane :label="$t('api_test.definition.request.rest_param')" name="rest">
            <ms-api-variable :is-read-only="isReadOnly"  :parameters="request.rest"/>
          </el-tab-pane>

          <!--请求体-->
          <el-tab-pane :label="$t('api_test.request.body')" name="body">
            <ms-api-body :is-read-only="isReadOnly" :body="request.body"/>
          </el-tab-pane>
          <!-- 认证配置 -->
          <el-tab-pane :label="$t('api_test.definition.request.auth_config')" name="authConfig">
            <ms-api-auth-config :is-read-only="isReadOnly" :auth-config="request.authConfig"/>
          </el-tab-pane>

        </el-tabs>
      </div>
      <div v-for="row in request.hashTree" :key="row.id" v-loading="isReloadData">
        <ms-jsr233-processor v-if="row.label ==='JSR223 PreProcessor'" :is-read-only="false" title="前置脚本" style-type="warning"
                             :jsr223-processor="row"/>

        <ms-jsr233-processor v-if="row.label ==='JSR223 PostProcessor'" :is-read-only="false" title="后置脚本" style-type="success"
                             :jsr223-processor="row"/>

        <!--<ms-api-assertions  v-if="row.label.indexOf('Assertion')>0" :is-read-only="isReadOnly" :request="request"/>-->

        <!--<ms-api-extract :is-read-only="isReadOnly" v-if="row.label==='JSONPostProcessor'" :extract="row"/>-->

      </div>
    </el-col>

    <el-col :span="3" class="ms-left-cell">
      <el-button class="ms-left-buttion" size="small" type="warning" @click="addPre" plain>+前置脚本</el-button>
      <br/>
      <el-button class="ms-left-buttion" size="small" type="success" @click="addPost" plain>+后置脚本</el-button>
      <br/>
      <!--<el-button class="ms-left-buttion" size="small" type="danger" @click="addAssertions" plain>+断言规则</el-button>-->
      <!--<br/>-->
      <!--<el-button class="ms-left-buttion" size="small" type="info" @click="addExtract" plain>+提取参数</el-button>-->
    </el-col>
  </el-row>
</template>

<script>
  import MsApiKeyValue from "../ApiKeyValue";
  import MsApiBody from "../body/ApiBody";
  import MsApiAuthConfig from "../auth/ApiAuthConfig";
  import ApiRequestMethodSelect from "../collapse/ApiRequestMethodSelect";
  import {REQUEST_HEADERS} from "@/common/js/constants";
  import MsApiVariable from "../ApiVariable";
  import MsJsr233Processor from "../processor/Jsr233Processor";
  import MsApiAdvancedConfig from "../ApiAdvancedConfig";
  import {createComponent} from "../jmeter/components";
  import MsApiAssertions from "../assertion/ApiAssertions";
  import MsApiExtract from "../extract/ApiExtract";
  import HTTPSamplerProxy from "../jmeter/components/sampler/http-sampler";

  export default {
    name: "MsApiHttpRequestForm",
    components: {
      MsJsr233Processor,
      MsApiAdvancedConfig,
      MsApiVariable, ApiRequestMethodSelect, MsApiExtract, MsApiAuthConfig, MsApiBody, MsApiKeyValue, MsApiAssertions
    },
    props: {
      request: {},
      headers: Array,
      isShowEnable: Boolean,
      jsonPathList: Array,
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
        headerSuggestions: REQUEST_HEADERS,
        isReloadData: false,
      }
    },

    methods: {
      addPre() {
        let jsr223PreProcessor = createComponent("JSR223PreProcessor");
        this.request.hashTree.push(jsr223PreProcessor);
        this.reload();
      },
      addPost() {
        let jsr223PostProcessor = createComponent("JSR223PostProcessor");
        this.request.hashTree.push(jsr223PostProcessor);
        this.reload();
      },
      addAssertions() {
        let jsonPathAssertion = createComponent("JSONPathAssertion");
        this.request.hashTree.push(jsonPathAssertion);
        this.reload();
      },
      addExtract() {
        let jsonPostProcessor = createComponent("JSONPostProcessor");
        this.request.hashTree.push(jsonPostProcessor);
        this.reload();
      },
      reload() {
        this.isReloadData = true
        this.$nextTick(() => {
          this.isReloadData = false
        })
      },
    },
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

  .ms-left-cell {
    margin-top: 30px;
  }

  .ms-left-buttion {
    margin: 6px 0px 8px 30px;
  }

</style>
