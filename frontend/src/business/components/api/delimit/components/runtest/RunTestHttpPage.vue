<template>

  <div class="card-container">
    <el-card class="card-content">

      <el-form :model="apiData" ref="apiData" :inline="true" label-position="right">
        <div style="font-size: 16px;color: #333333">{{$t('test_track.plan_view.base_info')}}</div>
        <br/>

        <el-form-item :label="$t('api_report.request')" prop="responsible">

          <el-input :placeholder="$t('api_test.delimit.request.path_info')" v-model="url"
                    class="ms-http-input" size="small" :disabled="false">
            <el-select v-model="path" slot="prepend" style="width: 100px">
              <el-option v-for="item in reqOptions" :key="item.id" :label="item.label" :value="item.id"/>
            </el-select>
          </el-input>

        </el-form-item>
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

        <div style="font-size: 16px;color: #333333;padding-top: 30px">{{$t('api_test.delimit.request.req_param')}}</div>
        <br/>
        <!-- HTTP 请求参数 -->
        <ms-api-request-form :request="apiData.request"/>

      </el-form>
      <div style="font-size: 16px;color: #333333 ;padding-top: 30px">{{$t('api_test.delimit.request.res_param')}}</div>
      <br/>
      <ms-response-text :response="responseData"></ms-response-text>

    </el-card>

    <!-- 加载用例 -->
    <ms-bottom-container v-bind:enableAsideHidden="isHide">
      <ms-api-case-list @apiCaseClose="apiCaseClose" @selectTestCase="selectTestCase" :api="apiData" ref="caseList"/>
    </ms-bottom-container>

  </div>
</template>

<script>
  import MsApiRequestForm from "../request/ApiRequestForm";
  import {downloadFile, getUUID} from "@/common/js/utils";
  import MsResponseText from "../../../report/components/ResponseText";
  import MsApiCaseList from "../ApiCaseList";
  import MsContainer from "../../../../common/components/MsContainer";
  import MsBottomContainer from "../BottomContainer";
  import {RequestFactory, Test} from "../../model/ScenarioModel";
  import {REQ_METHOD} from "../../model/JsonData";

  export default {
    name: "ApiConfig",
    components: {MsResponseText, MsApiRequestForm, MsApiCaseList, MsContainer, MsBottomContainer},
    data() {
      return {
        isHide: true,
        url: '',
        path: '',
        currentRequest: {},
        responseData: {},
        reqOptions: REQ_METHOD,
      }
    },
    props: {apiData: {}},
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
            return [];
        }
      },
      saveAs() {
        this.$emit('saveAs', this.apiData);
      },
      loadCase() {
        this.isHide = false;
      },
      apiCaseClose() {
        this.isHide = true;
      },
      getBodyUploadFiles() {
        let bodyUploadFiles = [];
        this.apiData.bodyUploadIds = [];
        let request = this.apiData.request;
        if (request.body) {
          request.body.kvs.forEach(param => {
            if (param.files) {
              param.files.forEach(item => {
                if (item.file) {
                  let fileId = getUUID().substring(0, 8);
                  item.name = item.file.name;
                  item.id = fileId;
                  this.apiData.bodyUploadIds.push(fileId);
                  bodyUploadFiles.push(item.file);
                }
              });
            }
          });
        }
        return bodyUploadFiles;
      },
      saveAsCase() {
        let testCase = {};
        let test = new Test();
        test.request = this.apiData.request;
        testCase.test = test;
        testCase.request = this.apiData.request;
        testCase.name = this.apiData.name;
        testCase.priority = "P0";
        this.$refs.caseList.saveTestCase(testCase);
      },
      saveAsApi() {
        let data = {};
        data.request = JSON.stringify(this.apiData.request);
        data.path = this.apiData.path;
        data.url = this.apiData.url;
        data.status = this.apiData.status;
        data.userId = this.apiData.userId;
        data.description = this.apiData.description;
        this.$emit('saveAsApi', data);
      },
      editApi(url) {
        this.apiData.url = this.url;
        this.apiData.path = this.path;
        let bodyFiles = this.getBodyUploadFiles();
        let jmx = this.apiData.test.toJMX();
        let blob = new Blob([jmx.xml], {type: "application/octet-stream"});
        let file = new File([blob], jmx.name);
        this.$fileUpload(url, file, bodyFiles, this.apiData, () => {
          this.$success(this.$t('commons.save_success'));
          this.$emit('saveApi', this.apiData);
        });
      },
      updateApi() {
        let url = "/api/delimit/update";
        this.editApi(url);
      },
      selectTestCase(item) {
        if (item != null) {
          this.apiData.request = new RequestFactory(JSON.parse(item.request));
        } else {
          this.apiData.request = this.currentRequest;
        }
      }
    },
    created() {
      this.currentRequest = this.apiData.request;
      this.url = this.apiData.url;
      this.path = this.apiData.path;
    }
  }
</script>

<style scoped>
  .ms-http-input {
    width: 500px;
    margin-top: 5px;
  }
</style>
