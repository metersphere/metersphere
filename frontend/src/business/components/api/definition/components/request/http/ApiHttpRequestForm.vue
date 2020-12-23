<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
  <div>
    <el-row>
      <el-col :span="21">
        <!-- HTTP 请求参数 -->
        <div style="border:1px #DCDFE6 solid; height: 100%;border-radius: 4px ;width: 100%" v-loading="isReloadData">
          <el-tabs v-model="activeName" class="request-tabs">
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
                <div class="el-step__icon is-text ms-api-col ms-header" v-if="request.arguments.length>1">
                  <div class="el-step__icon-inner">{{request.arguments.length-1}}</div>
                </div></span>
              </el-tooltip>
              <el-row>
                <el-link class="ms-el-link" @click="batchAdd"> {{$t("commons.batch_add")}}</el-link>
              </el-row>
              <ms-api-variable :is-read-only="isReadOnly" :isShowEnable="isShowEnable" :parameters="request.arguments"/>
            </el-tab-pane>

            <!--REST 参数-->
            <el-tab-pane :label="$t('api_test.definition.request.rest_param')" name="rest">
              <el-tooltip class="item-tabs" effect="dark" :content="$t('api_test.definition.request.rest_info')" placement="top-start" slot="label">
              <span>
                {{$t('api_test.definition.request.rest_param')}}
                <div class="el-step__icon is-text ms-api-col ms-header" v-if="request.rest.length>1">
                  <div class="el-step__icon-inner">{{request.rest.length-1}}</div>
                </div>
              </span>
              </el-tooltip>
              <el-row>
                <el-link class="ms-el-link" @click="batchAdd"> {{$t("commons.batch_add")}}</el-link>
              </el-row>
              <ms-api-variable :is-read-only="isReadOnly" :isShowEnable="isShowEnable" :parameters="request.rest"/>
            </el-tab-pane>

            <!--请求体-->
            <el-tab-pane v-if="isBodyShow" :label="$t('api_test.request.body')" name="body" style="overflow: auto">
              <ms-api-body @headersChange="reloadBody" :is-read-only="isReadOnly" :isShowEnable="isShowEnable" :headers="headers" :body="request.body"/>
            </el-tab-pane>

            <!-- 认证配置 -->
            <el-tab-pane :label="$t('api_test.definition.request.auth_config')" name="authConfig">
              <el-tooltip class="item-tabs" effect="dark" :content="$t('api_test.definition.request.auth_config_info')" placement="top-start" slot="label">
                <span>{{$t('api_test.definition.request.auth_config')}}</span>
              </el-tooltip>

              <ms-api-auth-config :is-read-only="isReadOnly" :request="request"/>
            </el-tab-pane>

            <el-tab-pane label="其他设置" name="advancedConfig">
              <ms-api-advanced-config :is-read-only="isReadOnly" :request="request"/>
            </el-tab-pane>

          </el-tabs>
        </div>
        <div v-if="!referenced">
          <div v-for="row in request.hashTree" :key="row.id">
            <!-- 前置脚本 -->
            <ms-jsr233-processor v-if="row.label ==='JSR223 PreProcessor'" @copyRow="copyRow" @remove="remove" :is-read-only="false" :title="$t('api_test.definition.request.pre_script')" style-type="color: #B8741A;background-color: #F9F1EA"
                                 :jsr223-processor="row"/>
            <!--后置脚本-->
            <ms-jsr233-processor v-if="row.label ==='JSR223 PostProcessor'" @copyRow="copyRow" @remove="remove" :is-read-only="false" :title="$t('api_test.definition.request.post_script')" style-type="color: #783887;background-color: #F2ECF3"
                                 :jsr223-processor="row"/>
            <!--断言规则-->
            <ms-api-assertions v-if="row.type==='Assertions'" @copyRow="copyRow" @remove="remove" :is-read-only="isReadOnly" :assertions="row"/>
            <!--提取规则-->
            <ms-api-extract :is-read-only="isReadOnly" @copyRow="copyRow" @remove="remove" v-if="row.type==='Extract'" :extract="row"/>
          </div>


        </div>
      </el-col>
      <!--操作按钮-->
      <el-col :span="3" class="ms-left-cell" v-if="!referenced && showScript">
        <el-button class="ms-left-buttion" size="small" @click="addPre">+{{$t('api_test.definition.request.pre_script')}}</el-button>
        <br/>
        <el-button class="ms-left-buttion" size="small" @click="addPost">+{{$t('api_test.definition.request.post_script')}}</el-button>
        <br/>
        <el-button class="ms-left-buttion" size="small" @click="addAssertions">+{{$t('api_test.definition.request.assertions_rule')}}</el-button>
        <br/>
        <el-button class="ms-left-buttion" size="small" @click="addExtract">+{{$t('api_test.definition.request.extract_param')}}</el-button>
      </el-col>
    </el-row>
    <batch-add-parameter @batchSave="batchSave" ref="batchAddParameter"/>
  </div>
</template>

<script>
  import MsApiKeyValue from "../../ApiKeyValue";
  import MsApiBody from "../../body/ApiBody";
  import MsApiAuthConfig from "../../auth/ApiAuthConfig";
  import ApiRequestMethodSelect from "../../collapse/ApiRequestMethodSelect";
  import {REQUEST_HEADERS} from "@/common/js/constants";
  import MsApiVariable from "../../ApiVariable";
  import MsJsr233Processor from "../../processor/Jsr233Processor";
  import {createComponent} from "../../jmeter/components";
  import MsApiAssertions from "../../assertion/ApiAssertions";
  import MsApiExtract from "../../extract/ApiExtract";
  import {Assertions, Body, Extract, KeyValue} from "../../../model/ApiTestModel";
  import {getUUID} from "@/common/js/utils";
  import BatchAddParameter from "../../basis/BatchAddParameter";
  import MsApiAdvancedConfig from "./ApiAdvancedConfig";

  export default {
    name: "MsApiHttpRequestForm",
    components: {
      MsJsr233Processor,
      MsApiAdvancedConfig,
      BatchAddParameter,
      MsApiVariable,
      ApiRequestMethodSelect,
      MsApiExtract,
      MsApiAuthConfig,
      MsApiBody,
      MsApiKeyValue,
      MsApiAssertions
    },
    props: {
      request: {},
      showScript: Boolean,
      headers: {
        type: Array,
        default() {
          return [];
        }
      },
      referenced: Boolean,
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
        isBodyShow: true,
        dialogVisible: false,
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
      copyRow(row) {
        let obj = JSON.parse(JSON.stringify(row));
        obj.id = getUUID();
        this.request.hashTree.push(obj);
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
      },
      // 解决修改请求头后 body 显示错位
      reloadBody() {
        this.isBodyShow = false;
        this.$nextTick(() => {
          this.isBodyShow = true;
        });
      },
      batchAdd() {
        this.$refs.batchAddParameter.open();
      },
      batchSave(data) {
        if (data) {
          let params = data.split("\n");
          let keyValues = [];
          params.forEach(item => {
            let line = item.split(/，|,/);
            let required = false;
            if (line[1] === '必填' || line[1] === 'true') {
              required = true;
            }
            keyValues.push(new KeyValue({name: line[0], required: required, value: line[2], description: line[3], type: "text", valid: false, file: false, encode: true, enable: true, contentType: "text/plain"}));
          })
          keyValues.forEach(item => {
            switch (this.activeName) {
              case "parameters":
                this.request.arguments.unshift(item);
                break;
              case "rest":
                this.request.rest.unshift(item);
                break;
              default:
                break;
            }
          })
        }
      }
    }
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
    background: #783887;
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

  .request-tabs {
    margin: 20px;
    min-height: 200px;
  }

  .ms-left-cell .el-button:nth-of-type(1) {
    color: #B8741A;
    background-color: #F9F1EA;
    border: #F9F1EA;
  }

  .ms-left-cell .el-button:nth-of-type(2) {
    color: #783887;
    background-color: #F2ECF3;
    border: #F2ECF3;
  }

  .ms-left-cell .el-button:nth-of-type(3) {
    color: #A30014;
    background-color: #F7E6E9;
    border: #F7E6E9;
  }

  .ms-left-cell .el-button:nth-of-type(4) {
    color: #015478;
    background-color: #E6EEF2;
    border: #E6EEF2;
  }

  .ms-el-link {
    float: right;
    margin-right: 45px;
  }
</style>
