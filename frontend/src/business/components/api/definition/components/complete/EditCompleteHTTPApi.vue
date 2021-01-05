<template>

  <div class="card-container">
    <el-card class="card-content" v-loading="httpForm.loading">

      <el-form :model="httpForm" :rules="rule" ref="httpForm" :inline="true" label-position="right">
        <!-- 操作按钮 -->
        <div style="float: right;margin-right: 20px">
          <el-button type="primary" size="small" @click="saveApi">{{$t('commons.save')}}</el-button>
          <el-button type="primary" size="small" @click="runTest">{{$t('commons.test')}}</el-button>
        </div>
        <br/>
        <p class="tip">{{$t('test_track.plan_view.base_info')}} </p>

        <!-- 基础信息 -->
        <div class="base-info">
          <el-row>
            <el-col :span="8">
              <el-form-item :label="$t('commons.name')" prop="name">
                <el-input class="ms-http-input" size="small" v-model="httpForm.name"/>
              </el-form-item>
            </el-col>
            <el-col :span="16">
              <el-form-item :label="$t('api_report.request')" prop="path">
                <el-input :placeholder="$t('api_test.definition.request.path_info')" v-model="httpForm.path"
                          class="ms-http-input" size="small" style="margin-top: 5px" @change="urlChange">
                  <el-select v-model="httpForm.method" slot="prepend" style="width: 100px" size="small">
                    <el-option v-for="item in reqOptions" :key="item.id" :label="item.label" :value="item.id"/>
                  </el-select>
                </el-input>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-col :span="8">
              <el-form-item :label="$t('test_track.module.module')" prop="moduleId">
                <el-select class="ms-http-select" size="small" v-model="httpForm.moduleId">
                  <div v-if="moduleOptions.length>0">
                    <el-option v-for="item in moduleOptions" :key="item.id" :label="item.path" :value="item.id"/>
                  </div>
                  <div v-else>
                    <el-option :key="0" :value="''">
                      <div style="margin-left: 40px">
                        <span style="font-size: 14px;color: #606266;font-weight: 48.93">{{$t('api_test.definition.select_comp.no_data')}},
                        </span>
                        <el-link type="primary" @click="createModules">{{$t('api_test.definition.select_comp.add_data')}}</el-link>
                      </div>
                    </el-option>
                  </div>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item :label="$t('api_test.definition.request.responsible')" prop="userId">
                <el-select v-model="httpForm.userId"
                           :placeholder="$t('api_test.definition.request.responsible')" filterable size="small"
                           class="ms-http-select">
                  <el-option
                    v-for="item in maintainerOptions"
                    :key="item.id"
                    :label="item.id + ' (' + item.name + ')'"
                    :value="item.id">
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item :label="$t('commons.status')" prop="status">
                <el-select class="ms-http-select" size="small" v-model="httpForm.status">
                  <el-option v-for="item in options" :key="item.id" :label="item.label" :value="item.id"/>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-row>
            <el-form-item :label="$t('commons.description')" prop="description">
              <el-input class="ms-http-textarea"
                        v-model="httpForm.description"
                        type="textarea"
                        :autosize="{ minRows: 2, maxRows: 10}"
                        :rows="2" size="small"/>
            </el-form-item>
          </el-row>
        </div>

        <!-- 请求参数 -->
        <div>
          <p class="tip">{{$t('api_test.definition.request.req_param')}} </p>
          <ms-api-request-form :showScript="false" :request="request" :headers="request.headers" :isShowEnable="isShowEnable"/>
        </div>

      </el-form>

      <!-- 响应内容-->
      <p class="tip">{{$t('api_test.definition.request.res_param')}} </p>
      <ms-response-text :response="response"></ms-response-text>
    </el-card>
  </div>
</template>

<script>
  import MsApiRequestForm from "../request/http/ApiRequestForm";
  import MsResponseText from "../response/ResponseText";
  import {WORKSPACE_ID} from '../../../../../../common/js/constants';
  import {REQ_METHOD, API_STATUS} from "../../model/JsonData";
  import MsJsr233Processor from "../processor/Jsr233Processor";
  import {KeyValue} from "../../model/ApiTestModel";

  export default {
    name: "MsAddCompleteHttpApi",
    components: {MsResponseText, MsApiRequestForm, MsJsr233Processor},
    data() {
      let validateURL = (rule, value, callback) => {
        if (!this.httpForm.path.startsWith("/") || this.httpForm.path.match(/\s/) != null) {
          callback(this.$t('api_test.definition.request.path_valid_info'));
        }
        callback();
      };
      return {
        rule: {
          name: [
            {required: true, message: this.$t('test_track.case.input_name'), trigger: 'blur'},
            {max: 50, message: this.$t('test_track.length_less_than') + '50', trigger: 'blur'}
          ],
          path: [{required: true, message: this.$t('api_test.definition.request.path_info'), trigger: 'blur'}, {validator: validateURL, trigger: 'blur'}],
          userId: [{required: true, message: this.$t('test_track.case.input_maintainer'), trigger: 'change'}],
          moduleId: [{required: true, message: this.$t('test_track.case.input_module'), trigger: 'change'}],
          status: [{required: true, message: this.$t('commons.please_select'), trigger: 'change'}],
        },
        httpForm: {environmentId: ""},
        isShowEnable: false,
        maintainerOptions: [],
        currentModule: {},
        reqOptions: REQ_METHOD,
        options: API_STATUS,
      }
    },
    props: {moduleOptions: {}, request: {}, response: {}, basisData: {}},
    methods: {
      runTest() {
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
        this.httpForm.modulePath = this.getPath(this.httpForm.moduleId);
        this.request.path = this.httpForm.path;
        this.request.method = this.httpForm.method;
        this.httpForm.request.useEnvironment = undefined;
      },
      saveApi() {
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
      createModules() {
        this.$emit("createRootModelInTree");
      },
      getPath(id) {
        if (id === null) {
          return null;
        }
        let path = this.moduleOptions.filter(function (item) {
          return item.id === id ? item.path : "";
        });
        return path[0].path;
      },
      urlChange() {
        if (!this.httpForm.path || this.httpForm.path.indexOf('?') === -1) return;
        let url = this.getURL(this.addProtocol(this.httpForm.path));
        if (url) {
          this.httpForm.path = decodeURIComponent("/" + url.hostname + url.pathname);
        }
      },
      addProtocol(url) {
        if (url) {
          if (!url.toLowerCase().startsWith("https") && !url.toLowerCase().startsWith("http")) {
            return "https://" + url;
          }
        }
        return url;
      },
      getURL(urlStr) {
        try {
          let url = new URL(urlStr);
          url.searchParams.forEach((value, key) => {
            if (key && value) {
              this.request.arguments.splice(0, 0, new KeyValue({name: key, required: false, value: value}));
            }
          });
          return url;
        } catch (e) {
          this.$error(this.$t('api_test.request.url_invalid'), 2000);
        }
      },
    },

    created() {
      this.getMaintainerOptions();
      if (!this.basisData.environmentId) {
        this.basisData.environmentId = "";
      }
      this.httpForm = JSON.parse(JSON.stringify(this.basisData));
    }
  }
</script>

<style scoped>

  .base-info .el-form-item {
    width: 100%;
  }

  .base-info .el-form-item >>> .el-form-item__content {
    width: 80%;
  }

  .base-info .ms-http-select {
    width: 100%;
  }

  .tip {
    padding: 3px 5px;
    font-size: 16px;
    border-radius: 4px;
    border-left: 4px solid #783887;
    margin: 20px 0;
  }

  .ms-http-textarea {
    width: 400px;
  }

  .ms-left-cell {
    margin-top: 100px;
  }

  .ms-left-buttion {
    margin: 6px 0px 8px 30px;
  }
</style>
