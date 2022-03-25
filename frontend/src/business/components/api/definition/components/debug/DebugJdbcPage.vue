<template>

  <div class="card-container">
    <el-card class="card-content">
      <el-button size="small" type="primary" class="ms-api-button" style="float: right;margin-right: 20px" @click="stop" v-if="isStop">
        {{ $t('report.stop_btn') }}
      </el-button>
      <div v-else>
        <el-button v-if="scenario" style="float: right;margin-right: 20px" size="small" type="primary"
                   @click="handleCommand"> {{ $t('commons.test') }}
        </el-button>
        <el-dropdown v-else split-button type="primary" class="ms-api-button" @click="handleCommand"
                     @command="handleCommand" size="small" style="float: right;margin-right: 20px">
          {{ $t('commons.test') }}
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="save_as">{{ $t('api_test.definition.request.save_as_case') }}</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>
      <p class="tip">{{ $t('api_test.definition.request.req_param') }} </p>
      <div v-loading="loading">
        <!-- JDBC 请求参数 -->
        <ms-basis-parameters :request="request" @callback="runDebug" ref="requestForm"/>
        <!-- JDBC 请求返回数据 -->
        <p class="tip">{{ $t('api_test.definition.request.res_param') }} </p>
        <ms-request-result-tail v-if="!loading" :response="responseData" :currentProtocol="currentProtocol" ref="debugResult"/>
      </div>

      <!-- 执行组件 -->
      <ms-run :debug="true" :reportId="reportId" :isStop="isStop" :run-data="runData" @runRefresh="runRefresh" ref="runTest"/>
    </el-card>
    <div v-if="scenario">
      <el-button style="float: right;margin: 20px" type="primary" @click="handleCommand('save_as_api')"> {{ $t('commons.save') }}</el-button>
    </div>
    <!-- 加载用例 -->
    <ms-api-case-list @refreshModule="refreshModule" :loaded="false" ref="caseList"/>

  </div>

</template>

<script>
import MsResponseResult from "../response/ResponseResult";
import MsRequestMetric from "../response/RequestMetric";
import {getUUID} from "@/common/js/utils";
import MsResponseText from "../response/ResponseText";
import MsRun from "../Run";
import {createComponent} from "../jmeter/components";
import {REQ_METHOD} from "../../model/JsonData";
import MsRequestResultTail from "../response/RequestResultTail";
import MsBasisParameters from "../request/database/BasisParameters";
import MsApiCaseList from "../case/ApiCaseList";
import {TYPE_TO_C} from "@/business/components/api/automation/scenario/Setting";

export default {
  name: "ApiConfig",
  components: {
    MsRequestResultTail,
    MsResponseResult,
    MsRequestMetric,
    MsResponseText,
    MsRun,
    MsBasisParameters,
    MsApiCaseList
  },
  props: {
    currentProtocol: String,
    scenario: Boolean,
    testCase: {},
  },
  data() {
    return {
      rules: {
        method: [{required: true, message: this.$t('test_track.case.input_maintainer'), trigger: 'change'}],
        url: [{required: true, message: this.$t('api_test.definition.request.path_all_info'), trigger: 'blur'}],
      },
      debugForm: {method: REQ_METHOD[0].id},
      options: [],
      responseData: {type: 'JDBC', responseResult: {}, subRequestResults: []},
      loading: false,
      debugResultId: "",
      runData: [],
      headers: [],
      reportId: "",
      reqOptions: REQ_METHOD,
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
      this.request = createComponent("JDBCSampler");
    }
  },
  watch: {
    debugResultId() {
      this.getResult()
    }
  },
  methods: {
    handleCommand(e) {
      if (e === "save_as") {
        this.saveAs();
      } else if (e === 'save_as_api') {
        this.saveAsApi();
      } else {
        this.$refs['requestForm'].validate();
      }
    },
    refreshModule() {
      this.$emit('refreshModule');
    },
    stop() {
      this.isStop = false;
      let url = "/api/automation/stop/" + this.reportId;
      this.$get(url, () => {
        this.loading = false;
        this.$success(this.$t('report.test_stop_success'));
      });
    },
    runDebug() {
      this.loading = true;
      this.isStop = true;
      this.request.name = getUUID().substring(0, 8);
      this.runData = [];
      this.runData.push(this.request);
      /*触发执行操作*/
      this.reportId = getUUID().substring(0, 8);
    },
    runRefresh(data) {
      this.responseData = data;
      this.loading = false;
      this.isStop = false;
      if (this.$refs.debugResult) {
        this.$refs.debugResult.reload();
      }
    },
    saveAsApi() {
      let obj = {request: this.request};
      obj.request.id = getUUID();
      this.$emit('saveAs', obj);
    },
    compatibleHistory(stepArray) {
      if (stepArray) {
        for (let i in stepArray) {
          if (!stepArray[i].clazzName) {
            stepArray[i].clazzName = TYPE_TO_C.get(stepArray[i].type);
          }
          if (stepArray[i].hashTree && stepArray[i].hashTree.length > 0) {
            this.compatibleHistory(stepArray[i].hashTree);
          }
        }
      }
    },
    saveAs() {
      let obj = {request: this.request};
      obj.request.id = getUUID();
      obj.saved = true;
      obj.protocol = this.currentProtocol;
      obj.status = "Underway";
      obj.method = this.currentProtocol;
      // 历史数据兼容处理
      if (obj.request) {
        obj.request.clazzName = TYPE_TO_C.get(obj.request.type);
        this.compatibleHistory(obj.request.hashTree);
      }
      this.$refs.caseList.saveApiAndCase(obj);
    }
  }
}
</script>

<style scoped>

</style>
