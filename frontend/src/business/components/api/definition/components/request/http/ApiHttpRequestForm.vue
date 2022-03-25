<template xmlns:v-slot="http://www.w3.org/1999/XSL/Transform">
  <div>
    <!-- HTTP 请求参数 -->
    <div style="border:1px #DCDFE6 solid; height: 100%;border-radius: 4px ;width: 100%" v-loading="isReloadData">
      <el-tabs v-model="activeName" class="request-tabs" @tab-click="tabClick">
        <!-- 请求头-->
        <el-tab-pane :label="$t('api_test.request.headers')" name="headers">
          <el-tooltip class="item-tabs" effect="dark" :content="$t('api_test.request.headers')" placement="top-start" slot="label">
              <span>{{ $t('api_test.request.headers') }}
                <div class="el-step__icon is-text ms-api-col ms-header" v-if="headers.length>1">
                  <div class="el-step__icon-inner">{{ headers.length - 1 }}</div>
                </div>
              </span>
          </el-tooltip>
          <el-row>
            <el-link class="ms-el-link" @click="batchAdd" style="color: #783887"> {{ $t("commons.batch_add") }}</el-link>
          </el-row>
          <ms-api-key-value
            @editScenarioAdvance="editScenarioAdvance"
            :scenario-definition="scenarioDefinition"
            :show-desc="true"
            :is-read-only="isReadOnly"
            :isShowEnable="isShowEnable"
            :suggestions="headerSuggestions"
            :items="headers"
            :need-mock="true" v-if="activeName === 'headers'"/>
        </el-tab-pane>

        <!--query 参数-->
        <el-tab-pane :label="$t('api_test.definition.request.query_param')" name="parameters">
          <el-tooltip class="item-tabs" effect="dark" :content="$t('api_test.definition.request.query_info')" placement="top-start" slot="label">
              <span>{{ $t('api_test.definition.request.query_param') }}
                <div class="el-step__icon is-text ms-api-col ms-header" v-if="request.arguments.length>1">
                  <div class="el-step__icon-inner">{{ request.arguments.length - 1 }}</div>
                </div>
              </span>
          </el-tooltip>
          <el-row>
            <el-link class="ms-el-link" @click="batchAdd" style="color: #783887"> {{ $t("commons.batch_add") }}</el-link>
          </el-row>
          <ms-api-variable
            @editScenarioAdvance="editScenarioAdvance"
            :scenario-definition="scenarioDefinition"
            :with-mor-setting="true"
            :is-read-only="isReadOnly"
            :isShowEnable="isShowEnable"
            :parameters="request.arguments"
            v-if="activeName === 'parameters'"
          />
        </el-tab-pane>

        <!--REST 参数-->
        <el-tab-pane :label="$t('api_test.definition.request.rest_param')" name="rest">
          <el-tooltip class="item-tabs" effect="dark" :content="$t('api_test.definition.request.rest_info')" placement="top-start" slot="label">
              <span>
                {{ $t('api_test.definition.request.rest_param') }}
                <div class="el-step__icon is-text ms-api-col ms-header" v-if="request.rest.length>1">
                  <div class="el-step__icon-inner">{{ request.rest.length - 1 }}</div>
                </div>
              </span>
          </el-tooltip>
          <el-row>
            <el-link class="ms-el-link" @click="batchAdd" style="color: #783887"> {{ $t("commons.batch_add") }}</el-link>
          </el-row>
          <ms-api-variable
            @editScenarioAdvance="editScenarioAdvance"
            :scenario-definition="scenarioDefinition"
            :with-mor-setting="true"
            :is-read-only="isReadOnly"
            :isShowEnable="isShowEnable"
            :parameters="request.rest"
            v-if="activeName === 'rest'"
          />
        </el-tab-pane>

        <!--请求体-->
        <el-tab-pane v-if="isBodyShow" :label="$t('api_test.request.body')" name="body">
          <ms-api-body
            @editScenarioAdvance="editScenarioAdvance"
            :scenario-definition="scenarioDefinition"
            @headersChange="reloadBody"
            :is-read-only="isReadOnly"
            :isShowEnable="isShowEnable"
            :headers="headers"
            :body="request.body"
            v-if="activeName === 'body'"
          />
        </el-tab-pane>

        <!-- 认证配置 -->
        <el-tab-pane :label="$t('api_test.definition.request.auth_config')" name="authConfig">
          <el-tooltip class="item-tabs" effect="dark" :content="$t('api_test.definition.request.auth_config_info')" placement="top-start" slot="label">
            <span>{{ $t('api_test.definition.request.auth_config') }}</span>
          </el-tooltip>

          <ms-api-auth-config
            :is-read-only="isReadOnly"
            :request="request"
            v-if="activeName === 'authConfig'"
          />
        </el-tab-pane>

        <el-tab-pane :label="$t('api_test.definition.request.other_config')" name="advancedConfig">
          <ms-api-advanced-config
            :is-read-only="isReadOnly"
            :request="request"
            v-if="activeName === 'advancedConfig'"
          />
        </el-tab-pane>

        <!-- 脚本步骤/断言步骤 -->
        <el-tab-pane :label="$t('api_test.definition.request.pre_operation')" name="preOperate" v-if="showScript">
          <span class="item-tabs" effect="dark" placement="top-start" slot="label">
            {{ $t('api_test.definition.request.pre_operation') }}
            <div class="el-step__icon is-text ms-api-col ms-header" v-if="request.preSize > 0">
              <div class="el-step__icon-inner">{{ request.preSize }}</div>
            </div>
          </span>
          <ms-jmx-step
            :request="request"
            :apiId="request.id"
            :response="response"
            :tab-type="'pre'"
            ref="preStep"
            v-if="activeName === 'preOperate'"
          />
        </el-tab-pane>
        <el-tab-pane :label="$t('api_test.definition.request.post_operation')" name="postOperate" v-if="showScript">
            <span class="item-tabs" effect="dark" placement="top-start" slot="label">
            {{ $t('api_test.definition.request.post_operation') }}
            <div class="el-step__icon is-text ms-api-col ms-header" v-if="request.postSize > 0">
              <div class="el-step__icon-inner">{{ request.postSize }}</div>
            </div>
          </span>
          <ms-jmx-step
            :request="request"
            :apiId="request.id"
            :response="response"
            :tab-type="'post'"
            ref="postStep"
            v-if="activeName === 'postOperate'"
          />
        </el-tab-pane>
        <el-tab-pane :label="$t('api_test.definition.request.assertions_rule')" name="assertionsRule" v-if="showScript">
            <span class="item-tabs" effect="dark" placement="top-start" slot="label">
            {{ $t('api_test.definition.request.assertions_rule') }}
            <div class="el-step__icon is-text ms-api-col ms-header" v-if="request.ruleSize > 0">
              <div class="el-step__icon-inner">{{ request.ruleSize }}</div>
            </div>
          </span>
          <ms-jmx-step
            :request="request"
            :apiId="request.id"
            :response="response"
            @reload="reloadBody"
            :tab-type="'assertionsRule'"
            ref="assertionsRule"
            v-if="activeName === 'assertionsRule'"/>
        </el-tab-pane>

      </el-tabs>
    </div>
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
import MsApiAssertions from "../../assertion/ApiAssertions";
import MsApiExtract from "../../extract/ApiExtract";
import {Body, KeyValue} from "../../../model/ApiTestModel";
import {hasLicense, getUUID} from "@/common/js/utils";
import BatchAddParameter from "../../basis/BatchAddParameter";
import MsApiAdvancedConfig from "./ApiAdvancedConfig";
import MsJsr233Processor from "../../../../automation/scenario/component/Jsr233Processor";
import {hasPermission} from '@/common/js/utils';
import Convert from "@/business/components/common/json-schema/convert/convert";
import {stepCompute, hisDataProcessing} from "@/business/components/api/definition/api-definition";

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
    MsApiAssertions,
    MsJmxStep: () => import( "@/business/components/api/definition/components/step/JmxStep"),
  },
  props: {
    method: String,
    request: {},
    response: {},
    definitionTest: {
      type: Boolean,
      default() {
        return false;
      }
    },
    showScript: {
      type: Boolean,
      default: true,
    },
    headers: {
      type: Array,
      default() {
        return [];
      }
    },
    referenced: {
      type: Boolean,
      default: false,
    },
    isShowEnable: Boolean,
    jsonPathList: Array,
    isReadOnly: {
      type: Boolean,
      default: false
    },
    type: String,
    scenarioDefinition: Array,
    ifFromVariableAdvance: {
      type: Boolean,
      default: false,
    },
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
      activeName: this.request.method === "POST" ? "body" : "parameters",
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
      spanCount: 21,
      headerSuggestions: REQUEST_HEADERS,
      isReloadData: false,
      isBodyShow: true,
      dialogVisible: false,
      hasOwnProperty: Object.prototype.hasOwnProperty,
      propIsEnumerable: Object.prototype.propertyIsEnumerable

    }
  },
  created() {
    if (!this.referenced && this.showScript) {
      this.spanCount = 21;
    } else {
      this.spanCount = 24;
    }
    this.init();
  },
  watch: {
    'request.changeId'() {
      if (this.request.headers && this.request.headers.length > 1) {
        this.activeName = 'headers';
      }
      if (this.request.rest && this.request.rest.length > 1) {
        this.activeName = 'rest';
      }
      if (this.request.arguments && this.request.arguments.length > 1) {
        this.activeName = 'parameters';
      }
      if (this.request.body) {
        this.request.body.typeChange = this.request.changeId;
      }
      this.reload();
    },
    'request.hashTree': {
      handler(v) {
        this.initStepSize(this.request.hashTree);
      },
      deep: true
    }
  },
  methods: {
    hasPermission,
    hasLicense,
    tabClick() {
      this.$nextTick(() => {
        setTimeout(() => {
          this.filter(this.activeName);
        });
      });
    },
    filter(activeName) {
      if (activeName === 'preOperate' && this.$refs.preStep) {
        this.$refs.preStep.filter();
      }
      if (activeName === 'postOperate' && this.$refs.postStep) {
        this.$refs.postStep.filter();
      }
      if (activeName === 'assertionsRule' && this.$refs.assertionsRule) {
        this.$refs.assertionsRule.filter();
      }
    },
    generate() {
      if (this.request.body && (this.request.body.jsonSchema || this.request.body.raw)) {
        if (!this.request.body.jsonSchema) {
          const MsConvert = new Convert();
          this.request.body.jsonSchema = MsConvert.format(JSON.parse(this.request.body.raw));
        }
        this.$post('/api/test/data/generator', this.request.body.jsonSchema, response => {
          if (response.data) {
            if (this.request.body.format !== 'JSON-SCHEMA') {
              this.request.body.raw = response.data;
            } else {
              const MsConvert = new Convert();
              let data = MsConvert.format(JSON.parse(response.data));
              this.request.body.jsonSchema = this.deepAssign(this.request.body.jsonSchema, data);
            }
            this.reloadBody();
          }
        });
      }
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
      if (Object.prototype.toString.call(this.request).match(/\[object (\w+)\]/)[1].toLowerCase() !== 'object') {
        this.request = JSON.parse(this.request);
      }
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
      if (this.request.hashTree) {
        this.initStepSize(this.request.hashTree);
        this.historicalDataProcessing(this.request.hashTree);
      }
    },
    historicalDataProcessing(array) {
      hisDataProcessing(array, this.request);
    },
    initStepSize(array) {
      stepCompute(array, this.request);
      this.reloadBody();
    },
    reloadBody() {
      // 解决修改请求头后 body 显示错位
      this.isBodyShow = false;
      this.$nextTick(() => {
        this.isBodyShow = true;
      });
    },
    batchAdd() {
      this.$refs.batchAddParameter.open();
    },
    format(array, obj) {
      if (array) {
        let isAdd = true;
        for (let i in array) {
          let item = array[i];
          if (item.name === obj.name) {
            item.value = obj.value;
            isAdd = false;
          }
        }
        if (isAdd) {
          switch (this.activeName) {
            case "parameters":
              this.request.arguments.unshift(obj);
              break;
            case "rest":
              this.request.rest.unshift(obj);
              break;
            case "headers":
              this.request.headers.unshift(obj);
              break;
            default:
              break;
          }
        }
      }
    },
    batchSave(data) {
      if (data) {
        let params = data.split("\n");
        let keyValues = [];
        params.forEach(item => {
          let line = item.split(/：|:/);
          let required = false;
          keyValues.unshift(new KeyValue({
            name: line[0],
            required: required,
            value: line[1],
            description: line[2],
            type: "text",
            valid: false,
            file: false,
            encode: true,
            enable: true,
            contentType: "text/plain"
          }));
        })

        keyValues.forEach(item => {
          switch (this.activeName) {
            case "parameters":
              this.format(this.request.arguments, item);
              break;
            case "rest":
              this.format(this.request.rest, item);
              break;
            case "headers":
              this.format(this.request.headers, item);
              break;
            default:
              break;
          }
        })
      }
    },

    isObj(x) {
      let type = typeof x;
      return x !== null && (type === 'object' || type === 'function');
    },

    toObject(val) {
      if (val === null || val === undefined) {
        return;
      }

      return Object(val);
    },

    assignKey(to, from, key) {
      let val = from[key];

      if (val === undefined || val === null) {
        return;
      }
      if (!this.hasOwnProperty.call(to, key) || !this.isObj(val)) {
        to[key] = val;
      } else {
        to[key] = this.assign(Object(to[key]), from[key]);
      }
    },

    assign(to, from) {
      if (to === from) {
        return to;
      }
      from = Object(from);
      for (let key in from) {
        if (this.hasOwnProperty.call(from, key)) {
          this.assignKey(to, from, key);
        }
      }

      if (Object.getOwnPropertySymbols) {
        let symbols = Object.getOwnPropertySymbols(from);

        for (let i = 0; i < symbols.length; i++) {
          if (this.propIsEnumerable.call(from, symbols[i])) {
            this.assignKey(to, from, symbols[i]);
          }
        }
      }

      return to;
    },

    deepAssign(target) {
      target = this.toObject(target);
      for (let s = 1; s < arguments.length; s++) {
        this.assign(target, arguments[s]);
      }
      return target;
    },
    editScenarioAdvance(data) {
      this.$emit('editScenarioAdvance', data);
    },
  }
}
</script>

<style scoped>
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
  font-size: xx-small;
  border-radius: 50%;
}

.request-tabs {
  margin: 10px;
  min-height: 200px;
}

.ms-el-link {
  float: right;
  margin-right: 45px;
}
</style>
