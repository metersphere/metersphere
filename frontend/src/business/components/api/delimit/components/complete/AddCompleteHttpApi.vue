<template>

  <div class="card-container">
    <el-card class="card-content" v-loading="httpForm.loading">

      <el-form :model="httpForm" :rules="rule" ref="httpForm" :inline="true" label-position="right">
        <div style="float: right;margin-right: 20px">
          <el-button type="primary" size="small" @click="saveApi">{{$t('commons.save')}}</el-button>
          <el-button type="primary" size="small" @click="runTest">{{$t('commons.test')}}</el-button>
        </div>
        <br/>
        <div style="font-size: 16px;color: #333333">{{$t('test_track.plan_view.base_info')}}</div>
        <br/>
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input class="ms-http-input" size="small" v-model="httpForm.name"/>
        </el-form-item>
        <el-form-item :label="$t('test_track.module.module')" prop="moduleId">
          <el-select class="ms-http-input" size="small" v-model="httpForm.moduleId">
            <el-option v-for="item in moduleOptions" :key="item.id" :label="item.path" :value="item.id"/>
          </el-select>
        </el-form-item>

        <el-form-item :label="$t('api_report.request')" prop="url">
          <el-input :placeholder="$t('api_test.delimit.request.path_info')" v-model="httpForm.url"
                    class="ms-http-input" size="small" style="margin-top: 5px">
            <el-select v-model="httpForm.path" slot="prepend" style="width: 100px" size="small">
              <el-option v-for="item in reqOptions" :key="item.id" :label="item.label" :value="item.id"/>
            </el-select>
          </el-input>

        </el-form-item>


        <el-form-item :label="$t('commons.status')" prop="status">
          <el-select class="ms-http-input" size="small" v-model="httpForm.status">
            <el-option v-for="item in options" :key="item.id" :label="item.label" :value="item.id"/>
          </el-select>
        </el-form-item>

        <el-form-item :label="$t('api_test.delimit.request.responsible')" prop="userId">
          <el-select v-model="httpForm.userId"
                     :placeholder="$t('api_test.delimit.request.responsible')" filterable size="small"
                     class="ms-http-input">
            <el-option
              v-for="item in maintainerOptions"
              :key="item.id"
              :label="item.id + ' (' + item.name + ')'"
              :value="item.id">
            </el-option>
          </el-select>

        </el-form-item>

        <el-form-item :label="$t('commons.description')" prop="description">
          <el-input class="ms-http-textarea"
                    v-model="httpForm.description"
                    type="textarea"
                    :autosize="{ minRows: 2, maxRows: 10}"
                    :rows="2" size="small"/>
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
  import MsResponseText from "../../../report/components/ResponseText";
  import {WORKSPACE_ID} from '../../../../../../common/js/constants';
  import {REQ_METHOD, API_STATUS} from "../../model/JsonData";

  export default {
    name: "MsAddCompleteHttpApi",
    components: {MsResponseText, MsApiRequestForm},
    data() {
      return {
        rule: {
          name: [
            {required: true, message: this.$t('test_track.case.input_name'), trigger: 'blur'},
            {max: 50, message: this.$t('test_track.length_less_than') + '50', trigger: 'blur'}
          ],
          url: [{required: true, message: this.$t('api_test.delimit.request.path_info'), trigger: 'blur'}],
          userId: [{required: true, message: this.$t('test_track.case.input_maintainer'), trigger: 'change'}],
          moduleId: [{required: true, message: this.$t('test_track.case.input_module'), trigger: 'change'}],
          status: [{required: true, message: this.$t('commons.please_select'), trigger: 'change'}],
        },
        httpForm: {},
        maintainerOptions: [],
        responseData: {},
        currentModule: {},
        reqOptions: REQ_METHOD,
        options: API_STATUS
      }
    },
    props: {httpData: {}, moduleOptions: {}, currentProject: {}, test: {}},
    methods: {
      runTest() {
        if (this.currentProject === null) {
          this.$error(this.$t('api_test.select_project'));
          return;
        }
        this.$refs['httpForm'].validate((valid) => {
          if (valid) {
            this.setParameter();
            this.$emit('runTest', this.httpForm);
          } else {
            return false;
          }
        })
      },
      getMaintainerOptions() {
        let workspaceId = localStorage.getItem(WORKSPACE_ID);
        this.$post('/user/ws/member/tester/list', {workspaceId: workspaceId}, response => {
          this.maintainerOptions = response.data;
        });
      },
      setParameter() {
        this.httpForm.test = this.test;
        this.httpForm.test.request.url = this.httpForm.url;
        this.httpForm.test.request.path = this.httpForm.path;
        this.httpForm.modulePath = this.getPath(this.httpForm.moduleId);
      },
      saveApi() {
        if (this.currentProject === null) {
          this.$error(this.$t('api_test.select_project'), 2000);
          return;
        }
        this.$refs['httpForm'].validate((valid) => {
          if (valid) {
            this.setParameter();
            this.$emit('saveApi', this.httpForm);
          }
          else {
            return false;
          }
        })
      },
      getPath(id) {
        if (id === null) {
          return null;
        }
        let path = this.moduleOptions.filter(function (item) {
          return item.id === id ? item.path : "";
        });
        return path[0].path;
      }
    },

    created() {
      this.getMaintainerOptions();
      this.httpForm = this.httpData;
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
