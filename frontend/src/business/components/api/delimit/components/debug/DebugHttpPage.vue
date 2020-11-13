<template>

  <div class="card-container">
    <el-card class="card-content">
      <el-form :model="debugForm" :rules="rules" ref="debugForm" :inline="true" label-position="right">
        <p class="tip">{{$t('test_track.plan_view.base_info')}} </p>

        <el-form-item :label="$t('api_report.request')" prop="url">
          <el-input :placeholder="$t('api_test.delimit.request.path_all_info')" v-model="debugForm.url"
                    class="ms-http-input" size="small">
            <el-select v-model="debugForm.path" slot="prepend" style="width: 100px" size="small">
              <el-option v-for="item in reqOptions" :key="item.id" :label="item.label" :value="item.id"/>
            </el-select>
          </el-input>
        </el-form-item>

        <el-form-item>
          <el-dropdown split-button type="primary" class="ms-api-buttion" @click="handleCommand"
                       @command="handleCommand" size="small">
            {{$t('commons.test')}}
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item command="save_as">{{$t('api_test.delimit.request.save_as')}}</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
        </el-form-item>

        <p class="tip">{{$t('api_test.delimit.request.req_param')}} </p>
        <!-- HTTP 请求参数 -->
        <ms-api-request-form :request="test.request"/>

      </el-form>
      <!-- HTTP 请求返回数据 -->
      <p class="tip">{{$t('api_test.delimit.request.res_param')}} </p>
      <ms-request-result-tail v-loading="loading" :response="responseData" ref="debugResult"/>
    </el-card>
  </div>
</template>

<script>
  import MsApiRequestForm from "../request/ApiRequestForm";
  import {Test} from "../../model/ApiTestModel";
  import MsResponseResult from "../response/ResponseResult";
  import MsRequestMetric from "../response/RequestMetric";
  import {getUUID, getCurrentUser} from "@/common/js/utils";
  import MsResponseText from "../response/ResponseText";


  import {REQ_METHOD} from "../../model/JsonData";
  import MsRequestResultTail from "../response/RequestResultTail";

  export default {
    name: "ApiConfig",
    components: {MsRequestResultTail, MsResponseResult, MsApiRequestForm, MsRequestMetric, MsResponseText},
    data() {
      return {
        rules: {
          path: [{required: true, message: this.$t('test_track.case.input_maintainer'), trigger: 'change'}],
          url: [{required: true, message: this.$t('api_test.delimit.request.path_all_info'), trigger: 'blur'}],
        },
        debugForm: {path: REQ_METHOD[0].id},
        options: [],
        responseData: {type: 'HTTP', responseResult: {}, subRequestResults: []},
        loading: false,
        debugResultId: "",
        test: new Test(),
        reqOptions: REQ_METHOD,
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
      getBodyUploadFiles(data) {
        let bodyUploadFiles = [];
        data.bodyUploadIds = [];
        let request = data.request;
        if (request.body) {
          request.body.kvs.forEach(param => {
            if (param.files) {
              param.files.forEach(item => {
                if (item.file) {
                  let fileId = getUUID().substring(0, 8);
                  item.name = item.file.name;
                  item.id = fileId;
                  data.bodyUploadIds.push(fileId);
                  bodyUploadFiles.push(item.file);
                }
              });
            }
          });
        }
        return bodyUploadFiles;
      },
      runDebug() {
        this.$refs['debugForm'].validate((valid) => {
          if (valid) {
            this.loading = true;
            let url = "/api/delimit/run/debug";
            let bodyFiles = this.getBodyUploadFiles(this.test);
            this.test.request.url = this.debugForm.url;
            this.test.request.path = this.debugForm.path;
            this.test.id = getUUID().substring(0, 8);
            let jmx = this.test.toJMX();
            let blob = new Blob([jmx.xml], {type: "application/octet-stream"});
            let file = new File([blob], jmx.name);
            this.$fileUpload(url, file, bodyFiles, this.test, response => {
              this.debugResultId = response.data;
            });
          }
        })
      },
      getResult() {
        if (this.debugResultId) {
          let url = "/api/delimit/report/get/" + this.debugResultId + "/" + "debug";
          this.$get(url, response => {
            if (response.data) {
              let testResult = JSON.parse(response.data.content);
              this.responseData = testResult.requestResults[0];
              this.loading = false;
              this.$refs.debugResult.reload();
            } else {
              setTimeout(this.getResult, 2000)
            }
          });
        }
      },
      saveAs() {
        this.$refs['debugForm'].validate((valid) => {
          if (valid) {
            this.debugForm.request = JSON.stringify(this.test.request);
            this.debugForm.userId = getCurrentUser().id;
            this.debugForm.status = "Underway";
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
