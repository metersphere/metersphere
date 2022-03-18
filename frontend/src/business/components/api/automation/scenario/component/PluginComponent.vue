<template>
  <div>
    <api-base-component
      @copy="copyRow"
      @remove="remove"
      @active="active"
      :data="request"
      :draggable="draggable"
      :color="defColor"
      :is-max="isMax"
      :show-btn="showBtn"
      :show-version="showVersion"
      :background-color="defBackgroundColor"
      :title="pluginName">

      <template v-slot:request>
        <legend style="width: 100%">
          <p class="tip">{{ $t('api_test.definition.request.req_param') }} </p>
          <el-tabs v-model="activeName" class="request-tabs" @tab-click="tabClick">
            <!-- 请求头-->
            <el-tab-pane label="插件数据" name="base">
              <div class="ms-form">
                <div class="ms-form-create" v-loading="loading">
                  <formCreate
                    v-model="pluginForm"
                    :rule="rules"
                    :option="option"
                    :value.sync="data"
                    @prefix-change="change"
                    @prefix-click="change"
                    @display-change="changeDisplay"
                    @prefix-visible-change="visibleChange"
                  />
                </div>
              </div>
            </el-tab-pane>

            <!-- 脚本步骤/断言步骤 -->
            <el-tab-pane :label="$t('api_test.definition.request.pre_operation')" name="preOperate">
              <span class="item-tabs" effect="dark" placement="top-start" slot="label">
                {{ $t('api_test.definition.request.pre_operation') }}
                <div class="el-step__icon is-text ms-api-col ms-header" v-if="request.preSize > 0">
                  <div class="el-step__icon-inner">{{ request.preSize }}</div>
                </div>
              </span>
              <ms-jmx-step :request="request" :apiId="request.id" :response="response" :tab-type="'pre'" ref="preStep"/>
            </el-tab-pane>
            <el-tab-pane :label="$t('api_test.definition.request.post_operation')" name="postOperate">
                <span class="item-tabs" effect="dark" placement="top-start" slot="label">
                {{ $t('api_test.definition.request.post_operation') }}
                <div class="el-step__icon is-text ms-api-col ms-header" v-if="request.postSize > 0">
                  <div class="el-step__icon-inner">{{ request.postSize }}</div>
                </div>
              </span>
              <ms-jmx-step :request="request" :apiId="request.id" :response="response" :tab-type="'post'" ref="postStep"/>
            </el-tab-pane>
            <el-tab-pane :label="$t('api_test.definition.request.assertions_rule')" name="assertionsRule">
                <span class="item-tabs" effect="dark" placement="top-start" slot="label">
                {{ $t('api_test.definition.request.assertions_rule') }}
                <div class="el-step__icon is-text ms-api-col ms-header" v-if="request.ruleSize > 0">
                  <div class="el-step__icon-inner">{{ request.ruleSize }}</div>
                </div>
              </span>
              <div style="margin-right: 20px">
                <ms-jmx-step :request="request" :apiId="request.id" :response="response" @reload="reload" :tab-type="'assertionsRule'" ref="assertionsRule"/>
              </div>
            </el-tab-pane>

          </el-tabs>
        </legend>
      </template>

      <template v-slot:debugStepCode>
       <span v-if="request.testing" class="ms-test-running">
         <i class="el-icon-loading" style="font-size: 16px"/>
         {{ $t('commons.testing') }}
       </span>
        <span class="ms-step-debug-code" :class="request.requestResult[0].success?'ms-req-success':'ms-req-error'" v-if="!loading && request.debug && request.requestResult[0] && request.requestResult[0].responseResult">
          {{ request.requestResult[0].success ? 'success' : 'error' }}
        </span>
      </template>

      <template v-slot:button v-if="allSampler.indexOf(request.type) !==-1">
        <el-tooltip :content="$t('api_test.run')" placement="top" v-if="!loading">
          <el-button :disabled="!request.enable" @click="run" icon="el-icon-video-play" style="padding: 5px" class="ms-btn" size="mini" circle/>
        </el-tooltip>
        <el-tooltip :content="$t('report.stop_btn')" placement="top" :enterable="false" v-else>
          <el-button :disabled="!request.enable" @click.once="stop" size="mini" style="color:white;padding: 0 0.1px;width: 24px;height: 24px;" class="stop-btn" circle>
            <div style="transform: scale(0.66)">
              <span style="margin-left: -4.5px;font-weight: bold;">STOP</span>
            </div>
          </el-button>
        </el-tooltip>
      </template>

      <template v-slot:result>
        <div v-if="allSampler.indexOf(request.type) !==-1">
          <p class="tip">{{ $t('api_test.definition.request.res_param') }} </p>
          <div v-if="request.result">
            <div v-for="(scenario,h) in request.result.scenarios" :key="h">
              <el-tabs v-model="request.activeName" closable class="ms-tabs">
                <el-tab-pane v-for="(item,i) in scenario.requestResults" :label="'循环'+(i+1)" :key="i" style="margin-bottom: 5px">
                  <api-response-component :currentProtocol="request.protocol" :apiActive="true" :result="item"/>
                </el-tab-pane>
              </el-tabs>
            </div>
          </div>
          <div v-else>
            <el-tabs v-model="request.activeName" closable class="ms-tabs" v-if="request.requestResult && request.requestResult.length > 1">
              <el-tab-pane v-for="(item,i) in request.requestResult" :label="'循环'+(i+1)" :key="i" style="margin-bottom: 5px">
                <api-response-component
                  :currentProtocol="request.protocol"
                  :apiActive="true"
                  :result="item"
                />
              </el-tab-pane>
            </el-tabs>
            <api-response-component
              :currentProtocol="request.protocol"
              :apiActive="true"
              :result="request.requestResult[0]"
              v-else/>
          </div>
        </div>
      </template>
    </api-base-component>
    <ms-run :debug="true" :reportId="reportId" :run-data="runData" :env-map="envMap"
            @runRefresh="runRefresh" @errorRefresh="errorRefresh" ref="runTest"/>
  </div>
</template>

<script>
import ApiBaseComponent from "../common/ApiBaseComponent";
import ApiResponseComponent from "./ApiResponseComponent";
import formCreate from "@form-create/element-ui";
import MsUpload from "../common/MsPluginUpload";
import {PLUGIN_ELEMENTS} from "@/business/components/api/automation/scenario/Setting";
import {getUUID} from "@/common/js/utils";
import MsJmxStep from "../../../definition/components/step/JmxStep";
import {Assertions} from "@/business/components/api/definition/model/ApiTestModel";
import {stepCompute, hisDataProcessing} from "@/business/components/api/definition/api-definition";

formCreate.component("msUpload", MsUpload);

export default {
  name: "PluginComponent",
  components: {
    ApiBaseComponent,
    ApiResponseComponent,
    MsJmxStep,
    MsRun: () => import("../../../definition/components/Run"),
  },
  props: {
    draggable: {
      type: Boolean,
      default: false,
    },
    message: String,
    response: {},
    isReadOnly: {
      type: Boolean,
      default:
        false
    },
    isMax: {
      type: Boolean,
      default: false,
    },
    showBtn: {
      type: Boolean,
      default: true,
    },
    showVersion: {
      type: Boolean,
      default: true,
    },
    request: {
      type: Object,
    },
    currentScenario: {},
    defTitle: {type: String, default: "Plugin"},
    defColor: {type: String, default: "#555855"},
    defBackgroundColor: {type: String, default: "#F4F4FF"},
    node: {},
    envMap: Map,
  },
  data() {
    return {
      activeName: "base",
      loading: false,
      runData: [],
      reportId: "",
      pluginForm: {},
      execEntry: "",
      allSampler: PLUGIN_ELEMENTS.get("menu_generative_controller"),
      data: {},
      rules: [],
      option: formCreate.parseJson(
        '{"form":{"labelPosition":"right","size":"mini","labelWidth":"120px","hideRequiredAsterisk":false,"showMessage":false,"inlineMessage":false}}'
      ),
      pluginName: "",
    }
  },
  computed: {
    isApiImport() {
      if (this.request.referenced != undefined && this.request.referenced === 'Deleted' || this.request.referenced == 'REF' || this.request.referenced === 'Copy') {
        return true
      }
      return false;
    },
    isExternalImport() {
      if (this.request.referenced != undefined && this.request.referenced === 'OT_IMPORT') {
        return true
      }
      return false;
    },
    isCustomizeReq() {
      if (this.request.referenced == undefined || this.request.referenced === 'Created') {
        return true;
      }
      return false;
    },
    isDeletedOrRef() {
      if (this.request.referenced != undefined && this.request.referenced === 'Deleted' || this.request.referenced === 'REF') {
        return true;
      }
      return false;
    },
  },
  created() {
    this.getPlugin();
    if (!this.request.requestResult) {
      this.request.requestResult = [];
    }
    this.data = this.request;
    this.pluginName = this.request.stepName ? this.request.stepName : this.request.type;
    if (this.request.hashTree) {
      this.initStepSize(this.request.hashTree);
      this.historicalDataProcessing(this.request.hashTree);
    }
  },
  watch: {
    message() {
      this.reload();
    },
    'request.hashTree'() {
      this.initStepSize(this.request.hashTree);
    },
    data: {
      handler: function () {
        Object.assign(this.request, this.data);
        if (this.request.condition) {
          this.changeDisplay(this.request.condition);
        }
      },
      deep: true
    }
  },
  methods: {
    tabClick() {
      if (this.activeName === 'preOperate') {
        this.$refs.preStep.filter();
      }
      if (this.activeName === 'postOperate') {
        this.$refs.postStep.filter();
      }
      if (this.activeName === 'assertionsRule') {
        this.$refs.assertionsRule.filter();
      }
    },
    historicalDataProcessing(array) {
      hisDataProcessing(array, this.request);
    },
    initStepSize(array) {
      stepCompute(array, this.request);
      this.reload();
    },
    blur(d) {
    },
    change(fileName) {
    },
    changeDisplay(fileName) {
      if (fileName === 'number of received messages' && this.pluginForm && this.pluginForm.hidden instanceof Function) {
        this.pluginForm.hidden(true, "conditionTime");
      }
      if (fileName === 'specified elapsed time (ms)' && this.pluginForm && this.pluginForm.hidden instanceof Function) {
        this.pluginForm.hidden(false, "conditionTime");
      }
    },
    run() {
      if (this.isApiImport || this.request.isRefEnvironment) {
        if (this.request.type && (this.request.type === "HTTPSamplerProxy" || this.request.type === "JDBCSampler" || this.request.type === "TCPSampler")) {
          if (!this.envMap || this.envMap.size === 0) {
            this.$warning(this.$t('api_test.automation.env_message'));
            return false;
          } else if (this.envMap && this.envMap.size > 0) {
            const env = this.envMap.get(this.request.projectId);
            if (!env) {
              this.$warning(this.$t('api_test.automation.env_message'));
              return false;
            }
          }
        }
      }
      this.request.debug = true;
      this.loading = true;
      this.runData = [];
      this.runData.projectId = this.request.projectId;
      this.request.useEnvironment = this.currentEnvironmentId;
      this.request.customizeReq = this.isCustomizeReq;
      let debugData = {
        id: this.currentScenario.id, name: this.currentScenario.name, type: "scenario",
        variables: this.currentScenario.variables, referenced: 'Created', headers: this.currentScenario.headers,
        enableCookieShare: this.enableCookieShare, environmentId: this.currentEnvironmentId, hashTree: [this.request],
      };
      this.runData.push(debugData);
      this.request.requestResult = [];
      this.request.result = undefined;
      /*触发执行操作*/
      this.reportId = getUUID();
    },
    stop() {
      let url = "/api/automation/stop/" + this.reportId;
      this.$get(url, () => {
        this.runLoading = false;
        this.loading = false;
        this.$success(this.$t('report.test_stop_success'));
      });
    },
    errorRefresh() {
      this.loading = false;
    },
    runRefresh(data) {
      this.request.requestResult = [data];
      this.request.result = undefined;
      this.loading = false;
      this.$emit('refReload', this.request, this.node);
    },
    getValue(val) {
      let reg = /\{(\w+)\}/gi;
      if (val.indexOf("${") !== -1) {
        let result;
        while ((result = reg.exec(val)) !== null) {
          if (this.pluginForm.getRule(result[1])) {
            val = val.replace("$" + result[0], this.pluginForm.getRule(result[1]).value);
          }
        }
        return val
      }
      return val;
    },
    visibleChange(d) {
      if (d && d.inject) {
        if (this.pluginForm.getRule(d.inject[0])) {
          let req = {entry: this.execEntry, request: this.getValue(d.inject[1])};
          this.$post('/plugin/customMethod/', req, response => {
            if (response.data && this.pluginForm.getRule(d.inject[0]).options) {
              this.pluginForm.getRule(d.inject[0]).options = JSON.parse(response.data);
            }
          })
          this.reload();
        }
      }
    },
    getPlugin() {
      let id = this.request.pluginId;
      if (id) {
        this.$get('/plugin/get/' + id, response => {
          let plugin = response.data;
          if (plugin) {
            this.pluginName = plugin.name;
            this.execEntry = plugin.execEntry;
            if (plugin.formScript) {
              this.rules = formCreate.parseJson(plugin.formScript);
            }
            if (plugin.formOption) {
              this.option = formCreate.parseJson(plugin.formOption);
            }
            this.option.submitBtn = {show: false};
            this.request.clazzName = plugin.clazzName;
            if (this.request && this.pluginForm && this.pluginForm.setValue instanceof Function) {
              this.pluginForm.setValue(this.request);
            }
            if (this.request.condition) {
              this.$nextTick(() => {
                this.changeDisplay(this.request.condition);
              });
            }
          } else {
            this.request.enable = false;
            this.request.plugin_del = true;
          }
        });
      }
    },
    reload() {
      this.loading = true
      this.$nextTick(() => {
        this.loading = false
      })
    },
    getCode() {
      if (this.node && this.node.data.code && this.node.data.debug) {
        if (this.node.data.code && this.node.data.code === 'error') {
          return 'error';
        } else {
          return 'success';
        }
      }
      return '';
    },
    remove() {
      this.$emit('remove', this.request, this.node);
    },
    copyRow() {
      this.$emit('copyRow', this.request, this.node);
    },
    active() {
      this.request.active = !this.request.active;
      if (this.request && this.request.active && this.pluginForm && this.pluginForm.setValue instanceof Function) {
        this.pluginForm.setValue(this.request);
      }
    },
  }
}
</script>

<style scoped>
/deep/ .el-divider {
  margin-bottom: 10px;
}

.ms-req-error {
  color: #F56C6C;
}

.ms-req-success {
  color: #67C23A;
}

.ms-btn {
  background-color: #409EFF;
  color: white;
}

.ms-step-debug-code {
  display: inline-block;
  margin: 0 5px;
  overflow-x: hidden;
  padding-bottom: 0;
  text-overflow: ellipsis;
  vertical-align: middle;
  white-space: nowrap;
  width: 80px;
}

.ms-form-create >>> .el-select {
  width: 100%;
}

/deep/ .fc-upload-btn {
  width: 30px;
  height: 30px;
  line-height: 30px;
  display: inline-block;
  text-align: center;
  border: 1px solid #c0ccda;
  border-radius: 4px;
  overflow: hidden;
  background: #fff;
  position: relative;
  -webkit-box-shadow: 2px 2px 5px rgb(0 0 0 / 10%);
  box-shadow: 2px 2px 5px rgb(0 0 0 / 10%);
  margin-right: 4px;
  -webkit-box-sizing: border-box;
  box-sizing: border-box;
}

.ms-test-running {
  color: #6D317C;
}

.ms-form {
  border: 1px #DCDFE6 solid;
  height: 100%;
  border-radius: 4px;
  width: 100%;
}

.stop-btn {
  background-color: #E62424;
  border-color: #EE6161;
  color: white;
}

.ms-form-create {
  margin: 10px;
}

.ms-header {
  background: #783887;
  color: white;
  height: 18px;
  border-radius: 42%;
}

</style>
