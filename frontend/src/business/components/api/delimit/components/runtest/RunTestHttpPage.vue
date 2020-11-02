<template>

  <div class="card-container">
    <el-card class="card-content" v-loading="result.loading">

      <el-form :model="httpForm" :rules="rule" ref="httpForm" :inline="true" :label-position="labelPosition">
        <div style="font-size: 16px;color: #333333">{{$t('test_track.plan_view.base_info')}}</div>
        <br/>

        <el-form-item :label="$t('api_report.request')" prop="responsible">

          <el-input :placeholder="$t('api_test.delimit.request.path_info')" v-model="httpForm.request"
                    class="ms-http-input" size="small">
            <el-select v-model="reqValue" slot="prepend" style="width: 100px">
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

        <div style="font-size: 16px;color: #333333;padding-top: 30px">请求参数</div>
        <br/>
        <!-- HTTP 请求参数 -->
        <ms-api-request-form :debug-report-id="debugReportId" :request="selected" :scenario="currentScenario"/>

      </el-form>
      <div style="font-size: 16px;color: #333333 ;padding-top: 30px">响应内容</div>
      <br/>
      <ms-response-text :response="responseData"></ms-response-text>

    </el-card>

    <!-- 加载用例 -->
    <ms-bottom-container v-bind:enableAsideHidden="isHide">
      <ms-api-case-list @apiCaseClose="apiCaseClose" :api="httpForm"></ms-api-case-list>
    </ms-bottom-container>

  </div>
</template>

<script>
  import MsApiRequestForm from "../request/ApiRequestForm";
  import {Request, Scenario} from "../../model/ScenarioModel";
  import MsResponseText from "../../../report/components/ResponseText";
  import MsApiCaseList from "../ApiCaseList";
  import MsContainer from "../../../../common/components/MsContainer";
  import MsBottomContainer from "../BottomContainer";

  export default {
    name: "ApiConfig",
    components: {MsResponseText, MsApiRequestForm, MsApiCaseList, MsContainer, MsBottomContainer},
    data() {
      return {
        result: {},
        rule: {},
        isHide: true,
        labelPosition: 'right',
        httpForm: {},
        options: [],
        reqValue: '',
        debugReportId: '',
        responseData: {},
        currentScenario: Scenario,
        selected: [Scenario, Request],
        reqOptions: [{
          id: 'GET',
          label: 'GET'
        }, {
          id: 'POST',
          label: 'POST'
        }],
        moduleValue: '',
      }
    },
    props: {httpData: {},},
    methods: {
      handleCommand(e) {
        switch (e) {
          case "load_case":
            return this.loadCase();
          case "save_as_case":
            return "";
          case "update_api":
            return "update_api";
          case "save_as_api":
            return "save_as_api";
          default:
            return [];
        }
      },
      saveAs() {
        this.$emit('saveAs', this.httpForm);
      },
      loadCase() {
        console.log(this.httpForm)
        this.isHide = false;
      },
      apiCaseClose() {
        this.isHide = true;
      }
    },
    watch: {
      httpData(v) {
        this.httpForm = v;
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
