<template>

  <div class="card-container">
    <el-card class="card-content">
      <el-form :model="debugForm" :rules="rules" ref="debugForm" :inline="true" label-position="right">
        <p class="tip">{{ $t('test_track.plan_view.base_info') }} </p>

        <el-form-item :label="$t('api_report.request')" prop="url">
          <el-input :placeholder="$t('api_test.definition.request.path_all_info')" v-model="debugForm.url"
                    class="ms-http-input" size="small" :disabled="testCase!=undefined" @blur="urlChange">
            <el-select v-model="debugForm.method" slot="prepend" style="width: 100px" size="small">
              <el-option v-for="item in reqOptions" :key="item.id" :label="item.label" :value="item.id"/>
            </el-select>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-button size="small" type="primary" @click="stop" v-if="isStop">
            {{ $t('report.stop_btn') }}
          </el-button>
          <div v-else>
            <el-dropdown split-button type="primary" class="ms-api-button" @click="handleCommand"
                         @command="handleCommand" size="small" v-if="testCase===undefined && !scenario">
              {{ $t('commons.test') }}
              <el-dropdown-menu slot="dropdown">
                <el-dropdown-item command="save_as">{{ $t('api_test.definition.request.save_as_case') }}</el-dropdown-item>
              </el-dropdown-menu>
            </el-dropdown>
            <el-button v-if="scenario" size="small" type="primary" @click="handleCommand">
              {{ $t('commons.test') }}
            </el-button>
            <el-button size="small" type="primary" @click.stop @click="generate"
                       style="margin-left: 10px"
                       v-if="hasPermission('PROJECT_API_DEFINITION:READ+CREATE_API') && hasLicense()">
              {{ $t('commons.generate_test_data') }}
            </el-button>
          </div>

        </el-form-item>
      </el-form>
      <div v-loading="loading">
        <p class="tip">{{ $t('api_test.definition.request.req_param') }} </p>
        <!-- HTTP 请求参数 -->
        <ms-api-request-form :isShowEnable="true" :definition-test="true"  :headers="request.headers" :request="request" :response="responseData" ref="apiRequestForm"/>

        <!-- HTTP 请求返回数据 -->
        <p class="tip">{{ $t('api_test.definition.request.res_param') }} </p>
        <ms-request-result-tail v-if="!loading" :response="responseData" ref="debugResult"/>
        <!-- 执行组件 -->
        <ms-run :debug="true" :reportId="reportId" :isStop="isStop" :run-data="runData" @runRefresh="runRefresh" ref="runTest"/>
      </div>
    </el-card>

    <div v-if="scenario">
      <el-button style="float: right;margin: 20px" type="primary" @click="handleCommand('save_as_api')"> {{ $t('commons.save') }}</el-button>
    </div>
    <!-- 加载用例 -->
    <ms-api-case-list :currentApi="debugForm" @refreshModule="refreshModule" :loaded="false" ref="caseList"/>
  </div>
</template>

<script>
import MsApiRequestForm from "../request/http/ApiHttpRequestForm";
import MsResponseResult from "../response/ResponseResult";
import MsRequestMetric from "../response/RequestMetric";
import {getCurrentUser, getUUID, hasLicense, hasPermission} from "@/common/js/utils";
import MsResponseText from "../response/ResponseText";
import MsRun from "../Run";
import {createComponent} from "../jmeter/components";
import {REQ_METHOD} from "../../model/JsonData";
import MsRequestResultTail from "../response/RequestResultTail";
import {KeyValue} from "../../model/ApiTestModel";
import MsApiCaseList from "../case/ApiCaseList";
import {TYPE_TO_C} from "@/business/components/api/automation/scenario/Setting";

export default {
  name: "ApiConfig",
  components: {
    MsRequestResultTail,
    MsResponseResult,
    MsApiRequestForm,
    MsRequestMetric,
    MsResponseText,
    MsRun,
    MsApiCaseList
  },
  props: {
    currentProtocol: String,
    testCase: {},
    scenario: Boolean,
  },
  data() {
    let validateURL = (rule, value, callback) => {
      try {
        new URL(this.debugForm.url);
        callback();
      } catch (e) {
        callback(this.$t('api_test.request.url_invalid'));
      }
    };
    return {
      rules: {
        method: [{required: true, message: this.$t('test_track.case.input_maintainer'), trigger: 'change'}],
        url: [
          {max: 500, required: true, message: this.$t('commons.input_limit', [1, 500]), trigger: 'blur'},
          /*
                      {validator: validateURL, trigger: 'blur'}
          */
        ],
      },
      debugForm: {method: REQ_METHOD[0].id, environmentId: ""},
      options: [],
      responseData: {type: 'HTTP', responseResult: {}, subRequestResults: []},
      loading: false,
      debugResultId: "",
      runData: [],
      reportId: "",
      reqOptions: REQ_METHOD,
      createCase: "",
      request: {},
      isStop: false,
    }
  },
  created() {
    if (this.testCase) {
      if (this.testCase.id) {
        // 执行结果信息
        let url = "/api/definition/report/getReport/" + this.testCase.id;
        this.$get(url, response => {
          if (response.data) {
            let data = JSON.parse(response.data.content);
            this.responseData = data;
          }
        });
      }
      this.request = this.testCase.request;
      if (this.request) {
        this.debugForm.method = this.request.method;
        if (this.request.url) {
          this.debugForm.url = this.request.url;
        } else {
          this.debugForm.url = this.request.path;
        }
      }
    } else {
      this.createHttp();
    }
  },
  watch: {
    debugResultId() {
      this.getResult()
    },
  },
  methods: {
    hasPermission, hasLicense,
    generate() {
      this.$refs.apiRequestForm.generate();
    },
    handleCommand(e) {
      if (e === "save_as") {
        this.saveAs();
      } else if (e === 'save_as_api') {
        this.saveAsApi();
      } else {
        this.runDebug();
      }
    },
    stop() {
      this.isStop = false;
      let url = "/api/automation/stop/" + this.reportId;
      this.$get(url, () => {
        this.loading = false;
        this.$success(this.$t('report.test_stop_success'));
      });
    },
    createHttp() {
      this.request = createComponent("HTTPSamplerProxy");
    },
    runDebug() {
      this.$refs['debugForm'].validate((valid) => {
        if (valid) {
          this.loading = true;
          this.isStop = true;
          this.request.url = this.debugForm.url;
          this.request.method = this.debugForm.method;
          this.request.name = getUUID().substring(0, 8);
          this.runData = [];
          this.runData.push(this.request);
          /*触发执行操作*/
          this.reportId = getUUID().substring(0, 8);
        }
      })
    },
    refreshModule() {
      this.$emit('refreshModule');
    },
    runRefresh(data) {
      this.responseData = data;
      this.loading = false;
      this.isStop = false;
      if(this.$refs.debugResult) {
        this.$refs.debugResult.reload();
      }
    },
    saveAsApi() {
      this.$refs['debugForm'].validate((valid) => {
        if (valid) {
          this.debugForm.id = null;
          this.request.id = getUUID();
          this.debugForm.request = this.request;
          this.debugForm.userId = getCurrentUser().id;
          this.debugForm.status = "Underway";
          this.debugForm.protocol = this.currentProtocol;
          this.$emit('saveAs', this.debugForm);
        } else {
          return false;
        }
      })
    },
    compatibleHistory(stepArray) {
      if (stepArray) {
        for (let i in stepArray) {
          if (!stepArray[i].clazzName) {
            stepArray[i].clazzName = TYPE_TO_C.get(stepArray[i].type);
          }
          if (stepArray[i] && stepArray[i].authManager && !stepArray[i].authManager.clazzName) {
            stepArray[i].authManager.clazzName = TYPE_TO_C.get(stepArray[i].authManager.type);
          }
          if (stepArray[i].hashTree && stepArray[i].hashTree.length > 0) {
            this.compatibleHistory(stepArray[i].hashTree);
          }
        }
      }
    },
    saveAs() {
      this.$refs['debugForm'].validate((valid) => {
        if (valid) {
          this.request.id = getUUID();
          this.request.method = this.debugForm.method;
          this.request.path = this.debugForm.path;
          this.protocol = this.currentProtocol;
          this.debugForm.id = this.request.id;
          this.debugForm.request = this.request;
          this.debugForm.userId = getCurrentUser().id;
          this.debugForm.status = "Underway";
          this.debugForm.protocol = this.currentProtocol;
          this.debugForm.saved = true;
          // 历史数据兼容处理
          if (this.debugForm.request) {
            this.debugForm.request.clazzName = TYPE_TO_C.get(this.debugForm.request.type);
            this.compatibleHistory(this.debugForm.request.hashTree);
          }
          this.$refs.caseList.saveApiAndCase(this.debugForm);
        } else {
          return false;
        }
      })
    },
    urlChange() {
      if (!this.debugForm.url) return;
      let url = this.getURL(this.debugForm.url);
      if (url && url.pathname) {
        if (this.debugForm.url.indexOf('?') != -1) {
          this.debugForm.url = decodeURIComponent(this.debugForm.url.substr(0, this.debugForm.url.indexOf("?")));
        }
        this.debugForm.path = url.pathname;
      } else {
        this.debugForm.path = url;
      }

    },
    getURL(urlStr) {
      try {
        let url = new URL(urlStr);
        url.searchParams.forEach((value, key) => {
          if (key && value) {
            this.request.arguments.splice(0, 0, new KeyValue({name: key, required: false, value: value}));
          }
        });
        return url;
      } catch (e) {
        return urlStr;
      }
    },
  }
}
</script>

<style scoped>
.ms-http-input {
  width: 500px;
  margin-top: 5px;
}
</style>
