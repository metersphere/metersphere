<template>

  <div class="card-container">
    <el-card class="card-content" v-loading="loading">

      <el-form :model="api" :rules="rules" ref="apiData" :inline="true" label-position="right">

        <p class="tip">{{$t('test_track.plan_view.base_info')}} </p>
        <!-- 请求方法 -->
        <el-form-item :label="$t('api_report.request')" prop="method">
          <el-select v-model="api.request.method" style="width: 100px" size="small">
            <el-option v-for="item in reqOptions" :key="item.id" :label="item.label" :value="item.id"/>
          </el-select>
        </el-form-item>

        <!-- 执行环境 -->
        <el-form-item prop="environmentId">
          <el-select v-model="api.environmentId" size="small" class="ms-htt-width"
                     :placeholder="$t('api_test.definition.request.run_env')"
                     @change="environmentChange" clearable>
            <el-option v-for="(environment, index) in environments" :key="index"
                       :label="environment.name + (environment.config.httpConfig.socket ? (': ' + environment.config.httpConfig.protocol + '://' + environment.config.httpConfig.socket) : '')"
                       :value="environment.id"/>
            <el-button class="environment-button" size="mini" type="primary" @click="openEnvironmentConfig">
              {{ $t('api_test.environment.environment_config') }}
            </el-button>
            <template v-slot:empty>
              <div class="empty-environment">
                <el-button class="environment-button" size="mini" type="primary" @click="openEnvironmentConfig">
                  {{ $t('api_test.environment.environment_config') }}
                </el-button>
              </div>
            </template>
          </el-select>
        </el-form-item>

        <!-- 请求地址 -->
        <el-form-item prop="path">
          <el-input :placeholder="$t('api_test.definition.request.path_info')" v-model="api.request.path" class="ms-htt-width"
                    size="small" :disabled="false"/>
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item>
          <el-dropdown split-button type="primary" class="ms-api-buttion" @click="handleCommand('add')"
                       @command="handleCommand" size="small">
            {{$t('commons.test')}}
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="load_case">{{$t('api_test.definition.request.load_case')}}
              </el-dropdown-item>
              <el-dropdown-item command="save_as_case">{{$t('api_test.definition.request.save_as_case')}}
              </el-dropdown-item>
              <el-dropdown-item command="update_api">{{$t('api_test.definition.request.update_api')}}</el-dropdown-item>
              <el-dropdown-item command="save_as_api">{{$t('api_test.definition.request.save_as')}}</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>

        </el-form-item>

        <p class="tip">{{$t('api_test.definition.request.req_param')}} </p>
        <!-- HTTP 请求参数 -->
        <ms-api-request-form :headers="api.request.headers" :request="api.request"/>

      </el-form>
      <!--返回结果-->
      <!-- HTTP 请求返回数据 -->
      <p class="tip">{{$t('api_test.definition.request.res_param')}} </p>
      <ms-request-result-tail :response="responseData" ref="runResult"/>

    </el-card>

    <!-- 加载用例 -->
    <ms-api-case-list @selectTestCase="selectTestCase" @refresh="refresh"
                      :loaded="loaded"
                      :refreshSign="refreshSign"
                      :createCase="createCase"
                      :currentApi="api"
                      ref="caseList"/>

    <!-- 环境 -->
    <api-environment-config ref="environmentConfig" @close="environmentConfigClose"/>
    <!-- 执行组件 -->
    <ms-run :debug="false" :environment="api.environment" :reportId="reportId" :run-data="runData"
            @runRefresh="runRefresh" ref="runTest"/>

  </div>
</template>

<script>
  import MsApiRequestForm from "../request/http/ApiRequestForm";
  import {downloadFile, getUUID, getCurrentProjectID} from "@/common/js/utils";
  import MsApiCaseList from "../case/ApiCaseList";
  import MsContainer from "../../../../common/components/MsContainer";
  import {parseEnvironment} from "../../model/EnvironmentModel";
  import ApiEnvironmentConfig from "../environment/ApiEnvironmentConfig";
  import MsRequestResultTail from "../response/RequestResultTail";
  import MsRun from "../Run";
  import {REQ_METHOD} from "../../model/JsonData";

  export default {
    name: "RunTestHTTPPage",
    components: {
      MsApiRequestForm,
      MsApiCaseList,
      MsContainer,
      MsRequestResultTail,
      ApiEnvironmentConfig,
      MsRun
    },
    data() {
      return {
        visible: false,
        api: {},
        loaded: false,
        loading: false,
        createCase: "",
        currentRequest: {},
        refreshSign: "",
        responseData: {type: 'HTTP', responseResult: {}, subRequestResults: []},
        reqOptions: REQ_METHOD,
        environments: [],
        rules: {
          method: [{required: true, message: this.$t('test_track.case.input_maintainer'), trigger: 'change'}],
          path: [{required: true, message: this.$t('api_test.definition.request.path_info'), trigger: 'blur'}],
          environmentId: [{required: true, message: this.$t('api_test.definition.request.run_env'), trigger: 'change'}],
        },
        runData: [],
        reportId: "",
        projectId: "",
      }
    },
    props: {apiData: {}, currentProtocol: String,},
    methods: {
      handleCommand(e) {
        switch (e) {
          case "load_case":
            return this.loadCase();
          case "save_as_case":
            return this.saveAsCase();
          case "update_api":
            return this.updateApi();
          case "save_as_api":
            return this.saveAsApi();
          default:
            return this.runTest();
        }
      },
      runTest() {
        this.$refs['apiData'].validate((valid) => {
          if (valid) {
            this.loading = true;
            this.api.request.name = this.api.id;
            this.api.request.useEnvironment = this.api.environmentId;
            this.api.protocol = this.currentProtocol;
            this.runData = [];
            this.runData.push(this.api.request);
            /*触发执行操作*/
            this.reportId = getUUID().substring(0, 8);
          }
        })
      },
      runRefresh(data) {
        this.responseData = data;
        this.loading = false;
      },
      saveAs() {
        this.$emit('saveAs', this.api);
      },
      loadCase() {
        this.refreshSign = getUUID();
        this.loaded = true;
        this.$refs.caseList.open();
      },
      apiCaseClose() {
        this.visible = false;
      },
      getBodyUploadFiles() {
        let bodyUploadFiles = [];
        this.api.bodyUploadIds = [];
        let request = this.api.request;
        if (request.body) {
          request.body.kvs.forEach(param => {
            if (param.files) {
              param.files.forEach(item => {
                if (item.file) {
                  let fileId = getUUID().substring(0, 8);
                  item.name = item.file.name;
                  item.id = fileId;
                  this.api.bodyUploadIds.push(fileId);
                  bodyUploadFiles.push(item.file);
                }
              });
            }
          });
        }
        return bodyUploadFiles;
      },
      saveAsCase() {
        //用于触发创建操作
        this.createCase = getUUID();
        this.$refs.caseList.open();
        this.loaded = false;
      },
      saveAsApi() {
        let data = {};
        data.request = JSON.stringify(this.api.request);
        data.method = this.api.method;
        data.url = this.api.url;
        let id = getUUID();
        data.id = id;
        data.status = this.api.status;
        data.userId = this.api.userId;
        data.description = this.api.description;
        this.$emit('saveAsApi', data);
      },
      updateApi() {
        let url = "/api/definition/update";
        let bodyFiles = this.getBodyUploadFiles();
        this.api.method = this.api.request.method;
        this.api.path = this.api.request.path;
        this.$fileUpload(url, null, bodyFiles, this.api, () => {
          this.$success(this.$t('commons.save_success'));
          this.$emit('saveApi', this.api);
        });
      },
      selectTestCase(item) {
        if (item != null) {
          this.api.request = item.request;
        } else {
          this.api.request = this.currentRequest;
        }
      },
      getEnvironments() {
        if (this.projectId) {
          this.$get('/api/environment/list/' + this.projectId, response => {
            this.environments = response.data;
            this.environments.forEach(environment => {
              parseEnvironment(environment);
            });
            let hasEnvironment = false;
            for (let i in this.environments) {
              if (this.environments[i].id === this.api.environmentId) {
                this.api.environmentId = this.environments[i].id;
                hasEnvironment = true;
                break;
              }
            }
            if (!hasEnvironment) {
              this.api.environmentId = '';
              this.api.environment = undefined;
            }
          });
        } else {
          this.api.environmentId = '';
          this.api.environment = undefined;
        }
      },
      openEnvironmentConfig() {
        if (!this.projectId) {
          this.$error(this.$t('api_test.select_project'));
          return;
        }
        this.$refs.environmentConfig.open(this.projectId);
      },
      environmentChange(value) {
        for (let i in this.environments) {
          if (this.environments[i].id === value) {
            this.api.environmentId = value;
            this.api.request.useEnvironment = value;
            break;
          }
        }
      },
      environmentConfigClose() {
        this.getEnvironments();
      },
      refresh() {
        this.$emit('refresh');
      },
      getResult() {
        let url = "/api/definition/report/getReport/" + this.api.id;
        this.$get(url, response => {
          if (response.data) {
            let data = JSON.parse(response.data.content);
            this.responseData = data;
          }
        });
      }
    },
    created() {
      this.projectId = getCurrentProjectID();
      this.api = this.apiData;
      this.api.protocol = this.currentProtocol;
      this.currentRequest = this.api.request;
      this.getEnvironments();
      this.getResult();
    }
  }
</script>

<style scoped>
  .ms-htt-width {
    width: 350px;
  }

  .environment-button {
    margin-left: 20px;
    padding: 7px;
  }

  .tip {
    padding: 3px 5px;
    font-size: 16px;
    border-radius: 4px;
    border-left: 4px solid #783887;
  }

  /deep/ .el-drawer {
    overflow: auto;
  }
</style>
