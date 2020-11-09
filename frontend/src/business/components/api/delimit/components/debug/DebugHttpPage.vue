<template>

  <div class="card-container">
    <el-card class="card-content">
      <el-form :model="httpForm" :rules="rules" ref="httpForm" :inline="true" label-position="right">
        <div style="font-size: 16px;color: #333333">{{$t('test_track.plan_view.base_info')}}</div>
        <br/>

        <el-form-item :label="$t('api_report.request')" prop="responsible">
          <el-input :placeholder="$t('api_test.delimit.request.path_info')" v-model="httpForm.url"
                    class="ms-http-input" size="small">
            <el-select v-model="httpForm.path" slot="prepend" style="width: 100px" size="small">
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

        <div style="font-size: 16px;color: #333333;padding-top: 30px">{{$t('api_test.delimit.request.req_param')}}</div>
        <br/>
        <!-- HTTP 请求参数 -->
        <ms-api-request-form :request="test.request"/>

      </el-form>
      <div style="font-size: 16px;color: #333333 ;padding-top: 30px">{{$t('api_test.delimit.request.res_param')}}</div>
      <br/>
      <ms-response-text :response="responseData"></ms-response-text>
    </el-card>
  </div>
</template>

<script>
  import MsApiRequestForm from "../request/ApiRequestForm";
  import {Test} from "../../model/ScenarioModel";
  import MsResponseText from "../../../report/components/ResponseText";
  import {REQ_METHOD} from "../../model/JsonData";

  export default {
    name: "ApiConfig",
    components: {MsResponseText, MsApiRequestForm},
    data() {
      return {
        rules: {
          path: [{required: true, message: this.$t('test_track.case.input_maintainer'), trigger: 'change'}],
          url: [{required: true, message: this.$t('api_test.delimit.request.path_info'), trigger: 'blur'}],
        },
        httpForm: {path: REQ_METHOD[0].id},
        options: [],
        responseData: {},
        test: new Test(),
        reqOptions: REQ_METHOD,
      }
    },
    methods: {
      handleCommand(e) {
        if (e === "save_as") {
          this.saveAs();
        }
      },
      saveAs() {
        this.$refs['httpForm'].validate((valid) => {
          if (valid) {
            this.httpForm.request = JSON.stringify(this.test.request);
            this.$emit('saveAs', this.httpForm);
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
</style>
