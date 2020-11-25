<template>

  <div class="card-container" v-loading="loading">
    <el-card class="card-content">
      <el-dropdown split-button type="primary" class="ms-api-buttion" @click="handleCommand"
                   @command="handleCommand" size="small" style="float: right;margin-right: 20px">
        {{$t('commons.test')}}
        <el-dropdown-menu slot="dropdown">
          <el-dropdown-item command="save_as">{{$t('api_test.definition.request.save_as')}}</el-dropdown-item>
        </el-dropdown-menu>
      </el-dropdown>

      <p class="tip">{{$t('api_test.definition.request.req_param')}} </p>
      <!-- HTTP 请求参数 -->
      <ms-basis-parameters :request="request" :currentProject="currentProject"/>


      <!-- HTTP 请求返回数据 -->
      <p class="tip">{{$t('api_test.definition.request.res_param')}} </p>
      <ms-request-result-tail  :response="responseData" ref="debugResult"/>

      <!-- 执行组件 -->
      <ms-run :debug="true" :reportId="reportId" :run-data="runData" @runRefresh="runRefresh" ref="runTest"/>
    </el-card>
  </div>
</template>

<script>
  import MsApiRequestForm from "../request/http/ApiRequestForm";
  import MsResponseResult from "../response/ResponseResult";
  import MsRequestMetric from "../response/RequestMetric";
  import {getUUID, getCurrentUser} from "@/common/js/utils";
  import MsResponseText from "../response/ResponseText";
  import MsRun from "../Run";
  import {createComponent, Request} from "../jmeter/components";
  import HeaderManager from "../jmeter/components/configurations/header-manager";
  import {REQ_METHOD} from "../../model/JsonData";
  import MsRequestResultTail from "../response/RequestResultTail";
  import MsBasisParameters from "../request/database/BasisParameters";

  export default {
    name: "ApiConfig",
    components: {MsRequestResultTail, MsResponseResult, MsApiRequestForm, MsRequestMetric, MsResponseText, MsRun, MsBasisParameters},
    props: {
      currentProtocol: String,
      currentProject: {},
    },
    data() {
      return {
        rules: {
          method: [{required: true, message: this.$t('test_track.case.input_maintainer'), trigger: 'change'}],
          url: [{required: true, message: this.$t('api_test.definition.request.path_all_info'), trigger: 'blur'}],
        },
        debugForm: {method: REQ_METHOD[0].id},
        options: [],
        responseData: {type: 'HTTP', responseResult: {}, subRequestResults: []},
        loading: false,
        debugResultId: "",
        runData: [],
        headers: [],
        reportId: "",
        reqOptions: REQ_METHOD,
        request: {},
      }
    },
    created() {
      switch (this.currentProtocol) {
        case Request.TYPES.SQL:
          this.request = createComponent("JDBCSampler");
          break;
        case Request.TYPES.DUBBO:
          this.request = createComponent("JDBCSampler");
          break;
        case Request.TYPES.TCP:
          this.request = createComponent("TCPSampler");
          break;
        default:
          this.createHttp();
          break;
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
        } else {
          this.runDebug();
        }
      },
      createHttp() {
        let header = createComponent("HeaderManager");
        this.request = createComponent("HTTPSamplerProxy");
        this.request.hashTree = [header];
      },
      runDebug() {
        this.loading = true;
        this.request.name = getUUID().substring(0, 8);
        this.runData = [];
        this.runData.push(this.request);
        /*触发执行操作*/
        this.reportId = getUUID().substring(0, 8);
      },
      runRefresh(data) {
        this.responseData = data;
        this.loading = false;
        this.$refs.debugResult.reload();
      },
      saveAs() {
        this.$refs['debugForm'].validate((valid) => {
          if (valid) {
            this.debugForm.request = JSON.stringify(this.request);
            this.debugForm.userId = getCurrentUser().id;
            this.debugForm.status = "Underway";
            this.debugForm.protocol = this.currentProtocol;
            this.$emit('saveAs', this.debugForm);
          }
          else {
            return false;
          }
        })
      }
    }
  }
</script>

<style scoped>
  .ms-http-input {
    width: 500px;
    margin-top: 5px;
  }

  .tip {
    padding: 3px 5px;
    font-size: 16px;
    border-radius: 4px;
    border-left: 4px solid #783887;
    margin: 20px 0;
  }
</style>
