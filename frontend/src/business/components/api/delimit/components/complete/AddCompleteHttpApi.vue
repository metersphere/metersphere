<template>

  <div class="card-container">
    <el-card class="card-content" v-loading="result.loading">

      <el-form :model="httpForm" :rules="rule" ref="httpForm" :inline="true" :label-position="labelPosition">
        <div style="float: right;margin-right: 20px">
          <el-button type="primary" size="small">{{$t('commons.save')}}</el-button>
          <el-button type="primary" size="small" @click="runTest(httpForm)">{{$t('commons.test')}}</el-button>
        </div>
        <br/>
        <div style="font-size: 16px;color: #333333">{{$t('test_track.plan_view.base_info')}}</div>
        <br/>
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input class="ms-http-input" size="small"/>
        </el-form-item>
        <el-form-item :label="$t('test_track.module.module')" prop="module">
          <el-select class="ms-http-input" size="small" v-model="moduleValue">
            <el-option v-for="item in options" :key="item.id" :label="item.label" :value="item.id"/>
          </el-select>
        </el-form-item>

        <el-form-item :label="$t('api_test.delimit.request.responsible')" prop="responsible">
          <el-input class="ms-http-input" size="small"/>
        </el-form-item>


        <el-form-item :label="$t('commons.status')" prop="status">
          <el-select class="ms-http-input" size="small" v-model="moduleValue">
            <el-option v-for="item in options" :key="item.id" :label="item.label" :value="item.id"/>
          </el-select>
        </el-form-item>

        <el-form-item :label="$t('api_report.request')" prop="responsible">
          <el-input :placeholder="$t('api_test.delimit.request.path_info')" v-model="httpForm.request"
                    class="ms-http-input" size="small" style="margin-top: 5px">
            <el-select v-model="reqValue" slot="prepend" style="width: 100px" size="small">
              <el-option v-for="item in reqOptions" :key="item.id" :label="item.label" :value="item.id"/>
            </el-select>
          </el-input>

        </el-form-item>

        <el-form-item :label="$t('commons.description')" prop="description">
          <el-input class="ms-http-textarea"
                    type="textarea"
                    :autosize="{ minRows: 2, maxRows: 10}"
                    :rows="2" size="small"/>
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
  </div>
</template>

<script>
  import MsApiRequestForm from "../request/ApiRequestForm";
  import {Request, Scenario} from "../../model/ScenarioModel";
  import MsResponseText from "../../../report/components/ResponseText";


  export default {
    name: "MsAddCompleteHttpApi",
    components: {MsResponseText, MsApiRequestForm},
    data() {
      return {
        result: {},
        rule: {},
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
      runTest(data) {
        this.$emit('runTest', data);
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
  }

  .ms-http-textarea {
    width: 500px;
  }
</style>
