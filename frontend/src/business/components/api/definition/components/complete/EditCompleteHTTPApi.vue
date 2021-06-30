<template>

  <div class="card-container">
    <el-card class="card-content" v-loading="httpForm.loading">
      <el-form :model="httpForm" :rules="rule" ref="httpForm" label-width="80px" label-position="right">
        <!-- 操作按钮 -->
        <div style="float: right;margin-right: 20px">
          <el-link type="primary" style="margin-right: 20px" @click="openHis" v-if="httpForm.id">
            {{ $t('operating_log.change_history') }}
          </el-link>
          <el-button type="primary" size="small" @click="saveApi" title="ctrl + s">{{ $t('commons.save') }}</el-button>
          <el-button type="primary" size="small" @click="runTest">{{ $t('commons.test') }}</el-button>
        </div>
        <br/>
        <p class="tip">{{ $t('test_track.plan_view.base_info') }} </p>

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
            <el-col :span="7">
              <el-form-item :label="$t('test_track.module.module')" prop="moduleId">
                <ms-select-tree size="small" :data="moduleOptions" :defaultKey="httpForm.moduleId" @getValue="setModule" :obj="moduleObj" clearable checkStrictly/>
              </el-form-item>
            </el-col>

            <el-col :span="7">
              <el-form-item :label="$t('commons.status')" prop="status">
                <el-select class="ms-http-select" size="small" v-model="httpForm.status">
                  <el-option v-for="item in options" :key="item.id" :label="item.label" :value="item.id"/>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="8">
              <el-form-item :label="$t('commons.tag')" prop="tag">
                <ms-input-tag :currentScenario="httpForm" ref="tag"/>
              </el-form-item>
            </el-col>
            <el-col :span="16">
              <el-form-item :label="$t('commons.description')" prop="description">
                <el-input class="ms-http-textarea"
                          v-model="httpForm.description"
                          type="textarea"
                          :autosize="{ minRows: 2, maxRows: 10}"
                          :rows="2" size="small"/>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- MOCK信息 -->
        <p class="tip">{{ $t('test_track.plan_view.mock_info') }} </p>
        <div class="base-info">
          <el-row>
            <el-col :span="20">
              Mock地址：
              <el-link :href="getUrlPrefix" target="_blank" style="color: black"
                       type="primary">{{ this.getUrlPrefix }}
              </el-link>
            </el-col>
            <el-col :span="4">
              <el-link @click="mockSetting" type="primary">Mock设置</el-link>
            </el-col>
          </el-row>

        </div>

        <!-- 请求参数 -->
        <div>
          <p class="tip">{{ $t('api_test.definition.request.req_param') }} </p>
          <ms-api-request-form :showScript="false" :request="request" :headers="request.headers"
                               :isShowEnable="isShowEnable"/>
        </div>

      </el-form>

      <!-- 响应内容-->
      <p class="tip">{{ $t('api_test.definition.request.res_param') }} </p>
      <ms-response-text :response="response"></ms-response-text>

      <ms-change-history ref="changeHistory"/>

    </el-card>
  </div>
</template>

<script>

  import MsApiRequestForm from "../request/http/ApiHttpRequestForm";
  import MsResponseText from "../response/ResponseText";
  import {WORKSPACE_ID} from '../../../../../../common/js/constants';
  import {API_STATUS, REQ_METHOD} from "../../model/JsonData";
  import {KeyValue} from "../../model/ApiTestModel";
  import MsInputTag from "@/business/components/api/automation/scenario/MsInputTag";
  import MsJsr233Processor from "../../../automation/scenario/component/Jsr233Processor";
  import MsSelectTree from "../../../../common/select-tree/SelectTree";
  import MsChangeHistory from "../../../../history/ChangeHistory";
  import {getCurrentProjectID} from "@/common/js/utils";

  export default {
    name: "MsAddCompleteHttpApi",
    components: {MsJsr233Processor, MsResponseText, MsApiRequestForm, MsInputTag, MsSelectTree,MsChangeHistory},
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
            {max: 100, message: this.$t('test_track.length_less_than') + '100', trigger: 'blur'}
          ],
          path: [{required: true, message: this.$t('api_test.definition.request.path_info'), trigger: 'blur'}, {
            validator: validateURL,
            trigger: 'blur'
          }],
          userId: [{required: true, message: this.$t('test_track.case.input_maintainer'), trigger: 'change'}],
          moduleId: [{required: true, message: this.$t('test_track.case.input_module'), trigger: 'change'}],
          status: [{required: true, message: this.$t('commons.please_select'), trigger: 'change'}],
        },
        httpForm: {environmentId: "", path: "", tags: []},
        isShowEnable: true,
        maintainerOptions: [],
        currentModule: {},
        reqOptions: REQ_METHOD,
        options: API_STATUS,
        mockEnvironment: {},
        moduleObj: {
          id: 'id',
          label: 'name',
        },
        mockBaseUrl: "",
      }
    },
    props: {moduleOptions: {}, request: {}, response: {}, basisData: {}, syncTabs: Array, projectId: String},
    watch: {
      syncTabs() {
        if (this.basisData && this.syncTabs && this.syncTabs.includes(this.basisData.id)) {
          // 标示接口在其他地方更新过，当前页面需要同步
          let url = "/api/definition/get/";
          this.$get(url + this.basisData.id, response => {
            if (response.data) {
              let request = JSON.parse(response.data.request);
              let index = this.syncTabs.findIndex(item => {
                if (item === this.basisData.id) {
                  return true;
                }
              })
              this.syncTabs.splice(index, 1);
              this.httpForm.path = response.data.path;
              this.httpForm.method = response.data.method;
              Object.assign(this.request, request);
            }
          });
        }
      }
    },
    computed: {
      getUrlPrefix() {
        if (this.httpForm.path == null) {
          return this.mockBaseUrl;
        } else {
          let path = this.httpForm.path;
          let prefix = "";
          if (path.endsWith("/")) {
            prefix = "/";
          }
          let protocol = this.httpForm.method;
          if (protocol === 'GET' || protocol === 'DELETE') {
            if (this.httpForm.request != null && this.httpForm.request.rest != null) {
              let pathUrlArr = path.split("/");
              let newPath = "";
              pathUrlArr.forEach(item => {
                if (item !== "") {
                  let pathItem = item;
                  if (item.indexOf("{") === 0 && item.indexOf("}") === (item.length - 1)) {
                    let paramItem = item.substr(1, item.length - 2);
                    for (let i = 0; i < this.httpForm.request.rest.length; i++) {
                      let param = this.httpForm.request.rest[i];
                      if (param.name === paramItem) {
                        pathItem = param.value;
                      }
                    }
                  }
                  newPath += "/" + pathItem;
                }
              });
              if (newPath !== "") {
                path = newPath;
              }
            }
          }

          return this.mockBaseUrl + path + prefix;
        }
      }
    },
    methods: {
      openHis(){
        this.$refs.changeHistory.open(this.httpForm.id);
      },
      runTest() {
        this.$refs['httpForm'].validate((valid) => {
          if (valid) {
            this.setParameter();
            this.$emit('runTest', this.httpForm);
          } else {
            return false;
          }
        });
      },
      getMaintainerOptions() {
        this.$post('/user/project/member/tester/list', {projectId: getCurrentProjectID()}, response => {
          this.maintainerOptions = response.data;
        });
      },
      setParameter() {
        this.request.path = this.httpForm.path;
        this.request.method = this.httpForm.method;
        this.httpForm.request.useEnvironment = undefined;
        if (this.httpForm.tags instanceof Array) {
          this.httpForm.tags = JSON.stringify(this.httpForm.tags);
        }
      },
      saveApi() {
        this.$refs['httpForm'].validate((valid) => {
          if (valid) {
            this.setParameter();
            this.$emit('saveApi', this.httpForm);
          } else {
            return false;
          }
        })
      },
      createModules() {
        this.$emit("createRootModelInTree");
      },
      urlChange() {
        if (!this.httpForm.path || this.httpForm.path.indexOf('?') === -1) return;
        let url = this.getURL(this.addProtocol(this.httpForm.path));
        if (url) {
          this.httpForm.path = decodeURIComponent(this.httpForm.path.substr(0, this.httpForm.path.indexOf("?")));
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
      setModule(id, data) {
        this.httpForm.moduleId = id;
        this.httpForm.modulePath = data.path;
      },
      initMockEnvironment() {
        var protocol = document.location.protocol;
        protocol = protocol.substring(0, protocol.indexOf(":"));
        let url = "/api/definition/getMockEnvironment/";
        this.$get(url + this.projectId + "/" + protocol, response => {
          this.mockEnvironment = response.data;
          let httpConfig = JSON.parse(this.mockEnvironment.config);
          if (httpConfig != null) {
            httpConfig = httpConfig.httpConfig;
            let httpType = httpConfig.defaultCondition;
            let conditions = httpConfig.conditions;
            conditions.forEach(condition => {
              if (condition.type === httpType) {
                this.mockBaseUrl = condition.protocol + "://" + condition.socket;
              }
            });
          }
        });
      },
      mockSetting() {
        if (this.httpForm.id == null) {
          this.$alert(this.$t('api_test.mock.create_error'));
        } else {
          let mockParam = {};
          mockParam.projectId = this.projectId;
          mockParam.apiId = this.httpForm.id;

          this.$post('/mockConfig/genMockConfig', mockParam, response => {
            let mockConfig = response.data;
            mockConfig.apiName = this.httpForm.name;
            this.$emit('mockConfig', mockConfig);
          });
        }
      }
    },

    created() {
      this.getMaintainerOptions();
      if (!this.basisData.environmentId) {
        this.basisData.environmentId = "";
      }
      this.httpForm = JSON.parse(JSON.stringify(this.basisData));

      this.initMockEnvironment();
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

  .ms-http-textarea {
    width: 100%;
  }

  .ms-left-cell {
    margin-top: 100px;
  }

  .ms-left-buttion {
    margin: 6px 0px 8px 30px;
  }
</style>
