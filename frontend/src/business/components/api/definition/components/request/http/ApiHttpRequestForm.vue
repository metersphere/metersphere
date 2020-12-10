<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
  <el-row>
    <el-col :span="21">
      <!-- HTTP 请求参数 -->
      <div style="border:1px #DCDFE6 solid; height: 100%;border-radius: 4px ;width: 100%">
        <el-tabs v-model="activeName" style="margin: 20px;min-height: 200px">
          <!-- 请求头-->
          <el-tab-pane :label="$t('api_test.request.headers')" name="headers">
            <el-tooltip class="item-tabs" effect="dark" :content="$t('api_test.request.headers')" placement="top-start" slot="label">
              <span>{{$t('api_test.request.headers')}}
                <div class="el-step__icon is-text ms-api-col ms-header" v-if="headers.length>1">
                  <div class="el-step__icon-inner">{{headers.length-1}}</div>
                </div>
              </span>
            </el-tooltip>

            <ms-api-key-value :is-read-only="isReadOnly" :isShowEnable="isShowEnable" :suggestions="headerSuggestions" :items="headers"/>
          </el-tab-pane>

          <!--query 参数-->
          <el-tab-pane :label="$t('api_test.definition.request.query_param')" name="parameters">
            <el-tooltip class="item-tabs" effect="dark" :content="$t('api_test.definition.request.query_info')" placement="top-start" slot="label">
              <span>{{$t('api_test.definition.request.query_param')}}
                <div class="el-step__icon is-text ms-api-col ms-query" v-if="request.arguments.length>1">
                  <div class="el-step__icon-inner">{{request.arguments.length-1}}</div>
                </div></span>
            </el-tooltip>

            <ms-api-variable :is-read-only="isReadOnly" :isShowEnable="isShowEnable" :parameters="request.arguments"/>
          </el-tab-pane>

          <!--REST 参数-->
          <el-tab-pane :label="$t('api_test.definition.request.rest_param')" name="rest" >
            <el-tooltip class="item-tabs" effect="dark" :content="$t('api_test.definition.request.rest_info')" placement="top-start" slot="label">
              <span>
                {{$t('api_test.definition.request.rest_param')}}
                <div class="el-step__icon is-text ms-api-col ms-query" v-if="request.rest.length>1">
                  <div class="el-step__icon-inner">{{request.rest.length-1}}</div>
                </div>
              </span>
            </el-tooltip>
            <ms-api-variable :is-read-only="isReadOnly" :isShowEnable="isShowEnable" :parameters="request.rest"/>
          </el-tab-pane>

          <!--请求体-->
          <el-tab-pane :label="$t('api_test.request.body')" name="body">
            <ms-api-body :is-read-only="isReadOnly" :isShowEnable="isShowEnable" :headers="headers" :body="request.body"/>
          </el-tab-pane>

          <!-- 认证配置 -->
          <el-tab-pane :label="$t('api_test.definition.request.auth_config')" name="authConfig">
            <el-tooltip class="item-tabs" effect="dark" :content="$t('api_test.definition.request.auth_config_info')" placement="top-start" slot="label">
              <span>{{$t('api_test.definition.request.auth_config')}}</span>
            </el-tooltip>

            <ms-api-auth-config :is-read-only="isReadOnly" :request="request"/>
          </el-tab-pane>

        </el-tabs>
      </div>

      <div v-for="row in request.hashTree" :key="row.id" v-loading="isReloadData">
        <!-- 前置脚本 -->
        <ms-jsr233-processor v-if="row.label ==='JSR223 PreProcessor'" @remove="remove" :is-read-only="false" :title="$t('api_test.definition.request.pre_script')" style-type="color: #B8741A;background-color: #F9F1EA"
                             :jsr223-processor="row"/>
        <!--后置脚本-->
        <ms-jsr233-processor v-if="row.label ==='JSR223 PostProcessor'" @remove="remove" :is-read-only="false" :title="$t('api_test.definition.request.post_script')" style-type="color: #783887;background-color: #F2ECF3"
                             :jsr223-processor="row"/>
        <!--断言规则-->
        <ms-api-assertions v-if="row.type==='Assertions'" @remove="remove" :is-read-only="isReadOnly" :assertions="row"/>
        <!--提取规则-->
        <ms-api-extract :is-read-only="isReadOnly" @remove="remove" v-if="row.type==='Extract'" :extract="row"/>

      </div>
    </el-col>
    <!--操作按钮-->
    <el-col :span="3" class="ms-left-cell">
      <el-button class="ms-left-buttion" size="small" style="color: #B8741A;background-color: #F9F1EA" @click="addPre">+{{$t('api_test.definition.request.pre_script')}}</el-button>
      <br/>
      <el-button class="ms-left-buttion" size="small" style="color: #783887;background-color: #F2ECF3" @click="addPost">+{{$t('api_test.definition.request.post_script')}}</el-button>
      <br/>
      <el-button class="ms-left-buttion" size="small" style="color: #A30014;background-color: #F7E6E9" @click="addAssertions">+{{$t('api_test.definition.request.assertions_rule')}}</el-button>
      <br/>
      <el-button class="ms-left-buttion" size="small" style="color: #015478;background-color: #E6EEF2" @click="addExtract">+{{$t('api_test.definition.request.extract_param')}}</el-button>
    </el-col>
  </el-row>
</template>

<script>
  import MsApiKeyValue from "../../ApiKeyValue";
  import MsApiBody from "../../body/ApiBody";
  import MsApiAuthConfig from "../../auth/ApiAuthConfig";
  import ApiRequestMethodSelect from "../../collapse/ApiRequestMethodSelect";
  import {REQUEST_HEADERS} from "@/common/js/constants";
  import MsApiVariable from "../../ApiVariable";
  import MsJsr233Processor from "../../processor/Jsr233Processor";
  import MsApiAdvancedConfig from "../../ApiAdvancedConfig";
  import {createComponent} from "../../jmeter/components";
  import MsApiAssertions from "../../assertion/ApiAssertions";
  import MsApiExtract from "../../extract/ApiExtract";
  import {Assertions, Body, Extract} from "../../../model/ApiTestModel";

  export default {
    name: "MsApiHttpRequestForm",
    components: {
      MsJsr233Processor,
      MsApiAdvancedConfig,
      MsApiVariable, ApiRequestMethodSelect, MsApiExtract, MsApiAuthConfig, MsApiBody, MsApiKeyValue, MsApiAssertions
    },
    props: {
      request: {},
      headers: {
        type: Array,
        default() {
          return [];
        }
      },
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

    created() {
      this.init();
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
        let assertions = new Assertions();
        this.request.hashTree.push(assertions);
        this.reload();
      },
      addExtract() {
        let jsonPostProcessor = new Extract();
        this.request.hashTree.push(jsonPostProcessor);
        this.reload();
      },
      remove(row) {
        let index = this.request.hashTree.indexOf(row);
        this.request.hashTree.splice(index, 1);
        this.reload();
      },
      reload() {
        this.isReloadData = true
        this.$nextTick(() => {
          this.isReloadData = false
        })
      },
      init() {
        if (!this.request.body) {
          this.request.body = new Body();
        }
        if (!this.request.body.kvs) {
          this.request.body.kvs = [];
        }
        if (!this.request.rest) {
          this.request.rest = [];
        }
        if (!this.request.arguments) {
          this.request.arguments = [];
        }
      }
    },
  }
</script>

<style scoped>
  .ms-left-cell {
    margin-top: 30px;
  }

  .ms-left-buttion {
    margin: 6px 0px 8px 30px;
  }

  .ms-query {
    background: #7F7F7F;
    color: white;
    height: 18px;
    border-radius: 42%;
  }

  .ms-header {
    background: #783887;
    color: white;
    height: 18px;
    border-radius: 42%;
  }
</style>
