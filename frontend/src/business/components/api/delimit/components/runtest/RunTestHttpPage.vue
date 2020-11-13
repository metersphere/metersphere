<template>

  <div class="card-container">
    <el-card class="card-content" v-loading="loading">

      <el-form :model="api" :rules="rules" ref="apiData" :inline="true" label-position="right">

        <p class="tip">{{$t('test_track.plan_view.base_info')}} </p>
        <!-- 请求方法 -->
        <el-form-item :label="$t('api_report.request')" prop="path">
          <el-select v-model="api.path" style="width: 100px" size="small">
            <el-option v-for="item in reqOptions" :key="item.id" :label="item.label" :value="item.id"/>
          </el-select>
        </el-form-item>

        <!-- 执行环境 -->
        <el-form-item prop="environmentId">
          <el-select v-model="api.environmentId" size="small" class="ms-htt-width"
                     :placeholder="$t('api_test.delimit.request.run_env')"
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
        <el-form-item prop="url">
          <el-input :placeholder="$t('api_test.delimit.request.path_info')" v-model="api.url" class="ms-htt-width"
                    size="small" :disabled="false"/>
        </el-form-item>

        <!-- 操作按钮 -->
        <el-form-item>
          <el-dropdown split-button type="primary" class="ms-api-buttion" @click="handleCommand('add')"
                       @command="handleCommand" size="small">
            {{$t('commons.test')}}
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="load_case">{{$t('api_test.delimit.request.load_case')}}
              </el-dropdown-item>
              <el-dropdown-item command="save_as_case">{{$t('api_test.delimit.request.save_as_case')}}
              </el-dropdown-item>
              <el-dropdown-item command="update_api">{{$t('api_test.delimit.request.update_api')}}</el-dropdown-item>
              <el-dropdown-item command="save_as_api">{{$t('api_test.delimit.request.save_as')}}</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>

        </el-form-item>

        <p class="tip">{{$t('api_test.delimit.request.req_param')}} </p>
        <!-- HTTP 请求参数 -->
        <ms-api-request-form :request="api.request"/>

      </el-form>
      <!--返回结果-->
      <!-- HTTP 请求返回数据 -->
      <p class="tip">{{$t('api_test.delimit.request.res_param')}} </p>
      <ms-request-result-tail :response="responseData" ref="runResult"/>

    </el-card>

    <!-- 加载用例 -->
    <ms-bottom-container v-bind:enableAsideHidden="isHide">
      <ms-api-case-list @apiCaseClose="apiCaseClose" @selectTestCase="selectTestCase" :api="api"
                        :currentProject="currentProject" :loaded="loaded"
                        ref="caseList"/>
    </ms-bottom-container>

    <!-- 环境 -->
    <api-environment-config ref="environmentConfig" @close="environmentConfigClose"/>

  </div>
</template>

<script>
  import MsApiRequestForm from "../request/ApiRequestForm";
  import {downloadFile, getUUID} from "@/common/js/utils";
  import MsApiCaseList from "../ApiCaseList";
  import MsContainer from "../../../../common/components/MsContainer";
  import MsBottomContainer from "../BottomContainer";
  import {RequestFactory, Test} from "../../model/ApiTestModel";
  import {parseEnvironment} from "../../model/EnvironmentModel";
  import ApiEnvironmentConfig from "../../../test/components/ApiEnvironmentConfig";
  import MsRequestResultTail from "../response/RequestResultTail";

  import {REQ_METHOD} from "../../model/JsonData";

  export default {
    name: "ApiConfig",
    components: {
      MsApiRequestForm,
      MsApiCaseList,
      MsContainer,
      MsBottomContainer,
      MsRequestResultTail,
      ApiEnvironmentConfig
    },
    data() {
      return {
        isHide: true,
        api: {},
        loaded: false,
        loading: false,
        currentRequest: {},
        responseData: {type: 'HTTP', responseResult: {}, subRequestResults: []},
        reqOptions: REQ_METHOD,
        environments: [],
        rules: {
          path: [{required: true, message: this.$t('test_track.case.input_maintainer'), trigger: 'change'}],
          url: [{required: true, message: this.$t('api_test.delimit.request.path_info'), trigger: 'blur'}],
          environmentId: [{required: true, message: this.$t('api_test.delimit.request.run_env'), trigger: 'change'}],
        }
      }
    },
    props: {apiData: {}, currentProject: {}},
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
            let url = "/api/delimit/run";
            let bodyFiles = this.getBodyUploadFiles();
            let env = this.api.environment.config.httpConfig.socket ? (this.api.environment.config.httpConfig.protocol + '://' + this.api.environment.config.httpConfig.socket) : '';
            if (env.endsWith("/")) {
              env = env.substr(0, env.length - 1);
            }
            let sendUrl = this.api.url;
            if (!sendUrl.startsWith("/")) {
              sendUrl = "/" + sendUrl;
            }
            this.api.test.request.url = env + sendUrl;
            this.api.test.request.path = this.api.path;
            this.api.test.request.name = this.api.id;
            this.api.reportId = "run";
            let jmx = this.api.test.toJMX();
            let blob = new Blob([jmx.xml], {type: "application/octet-stream"});
            let file = new File([blob], jmx.name);
            this.$fileUpload(url, file, bodyFiles, this.api, response => {
              this.getResult();
            }, erro => {
              this.loading = false;
            });
          }
        })
      },

      getResult() {
        if (this.api.id) {
          let url = "/api/delimit/report/get/" + this.api.id + "/run";
          this.$get(url, response => {
            if (response.data) {
              let testResult = JSON.parse(response.data.content);
              this.responseData = testResult;
              this.loading = false;
              this.$refs.runResult.reload();
            } else {
              setTimeout(this.getResult, 2000)
            }
          });
        }
      },
      saveAs() {
        this.$emit('saveAs', this.api);
      },
      loadCase() {
        this.loaded = true;
        this.isHide = false;
      },
      apiCaseClose() {
        this.isHide = true;
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
        this.isHide = false;
        this.loaded = false;
        let testCase = {};
        testCase.request = this.api.request;
        testCase.apiDelimitId = this.api.id;
        testCase.priority = "P0";
        this.$refs.caseList.createCase(testCase);
      },
      saveAsApi() {
        let data = {};
        data.request = JSON.stringify(this.api.request);
        data.path = this.api.path;
        data.url = this.api.url;
        data.status = this.api.status;
        data.userId = this.api.userId;
        data.description = this.api.description;
        this.$emit('saveAsApi', data);
      },
      updateApi() {
        let url = "/api/delimit/update";
        let bodyFiles = this.getBodyUploadFiles();
        let jmx = this.api.test.toJMX();
        let blob = new Blob([jmx.xml], {type: "application/octet-stream"});
        let file = new File([blob], jmx.name);
        this.$fileUpload(url, file, bodyFiles, this.api, () => {
          this.$success(this.$t('commons.save_success'));
          this.$emit('saveApi', this.api);
        });
      },
      selectTestCase(item) {
        if (item != null) {
          this.api.request = new RequestFactory(JSON.parse(item.request));
        } else {
          this.api.request = this.currentRequest;
        }
      },
      getEnvironments() {
        if (this.currentProject) {
          this.$get('/api/environment/list/' + this.currentProject.id, response => {
            this.environments = response.data;
            this.environments.forEach(environment => {
              parseEnvironment(environment);
            });
            let hasEnvironment = false;
            for (let i in this.environments) {
              if (this.environments[i].id === this.api.environmentId) {
                this.api.environment = this.environments[i];
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
        if (!this.currentProject) {
          this.$error(this.$t('api_test.select_project'));
          return;
        }
        this.$refs.environmentConfig.open(this.currentProject.id);
      },
      environmentChange(value) {
        for (let i in this.environments) {
          if (this.environments[i].id === value) {
            this.api.environment = this.environments[i];
            break;
          }
        }
      },
      environmentConfigClose() {
        this.getEnvironments();
      },
    },
    created() {
      this.api = this.apiData;
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
    margin: 20px 0;
  }
</style>
