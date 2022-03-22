<template>

  <div class="card-container">
    <el-card class="card-content" v-loading="httpForm.loading">
      <el-form :model="httpForm" :rules="rule" ref="httpForm" label-width="80px" label-position="right">
        <!-- 操作按钮 -->
        <div style="float: right;margin-right: 20px" class="ms-opt-btn">
          <el-tooltip :content="$t('commons.follow')" placement="bottom" effect="dark" v-if="!showFollow">
            <i class="el-icon-star-off"
               style="color: #783987; font-size: 25px; margin-right: 5px; position: relative; top: 5px; cursor: pointer "
               @click="saveFollow"/>
          </el-tooltip>
          <el-tooltip :content="$t('commons.cancel')" placement="bottom" effect="dark" v-if="showFollow">
            <i class="el-icon-star-on"
               style="color: #783987; font-size: 28px; margin-right: 5px; position: relative; top: 5px; cursor: pointer "
               @click="saveFollow"/>
          </el-tooltip>
          <el-link type="primary" style="margin-right: 5px" @click="openHis" v-if="httpForm.id">
            {{ $t('operating_log.change_history') }}
          </el-link>
          <!--  版本历史 -->
          <ms-version-history v-xpack
                              ref="versionHistory"
                              :version-data="versionData"
                              :current-id="httpForm.id"
                              @compare="compare" @checkout="checkout" @create="create" @del="del"/>
          <el-button type="primary" size="small" @click="saveApi" title="ctrl + s"
                     v-permission="['PROJECT_API_DEFINITION:READ+EDIT_API']">{{ $t('commons.save') }}
          </el-button>
        </div>
        <br/>

        <ms-form-divider :title="$t('test_track.plan_view.base_info')"/>

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
                    :label="item.name + ' (' + item.id + ')'"
                    :value="item.id">
                  </el-option>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item :label="$t('test_track.module.module')" prop="moduleId">
                <ms-select-tree size="small" :data="moduleOptions" :defaultKey="httpForm.moduleId" @getValue="setModule"
                                :obj="moduleObj" clearable checkStrictly/>
              </el-form-item>
            </el-col>

            <el-col :span="8">
              <el-form-item :label="$t('commons.status')" prop="status">
                <el-select class="ms-http-select" size="small" v-model="httpForm.status">
                  <el-option v-for="item in options" :key="item.id" :label="$t(item.label)" :value="item.id"/>
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>
          <el-row>
            <el-col :span="8">
              <el-form-item :label="$t('commons.tag')" prop="tag">
                <ms-input-tag :currentScenario="httpForm" ref="tag" v-model="httpForm.tags"/>
              </el-form-item>
            </el-col>
            <el-col :span="8">
              <el-form-item :label="$t('commons.description')" prop="description">
                <el-input class="ms-http-textarea"
                          v-model="httpForm.description"
                          type="textarea"
                          :autosize="{ minRows: 1, maxRows: 10}"
                          :rows="1" size="small"/>
              </el-form-item>
            </el-col>
          </el-row>
        </div>

        <!-- MOCK信息 -->
        <ms-form-divider :title="$t('test_track.plan_view.mock_info')"/>
        <div class="base-info mock-info">
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
          <ms-form-divider :title="$t('api_test.definition.request.req_param')"/>
          <ms-api-request-form :showScript="false" :request="request" :headers="request.headers"
                               :isShowEnable="isShowEnable"/>
        </div>

      </el-form>

      <!-- 响应内容-->
      <ms-form-divider :title="$t('api_test.definition.request.res_param')"/>
      <ms-response-text :response="response"/>

      <api-other-info :api="httpForm" ref="apiOtherInfo"/>

      <ms-change-history ref="changeHistory"/>

      <el-dialog
        :fullscreen="true"
        :visible.sync="dialogVisible"
        :destroy-on-close="true"
        width="100%"
      >
        <http-api-version-diff
          v-if="dialogVisible"
          :old-data="httpForm"
          :show-follow="showFollow"
          :new-data="newData"
          :new-show-follow="newShowFollow"
          :old-mock-url="getUrlPrefix"
          :new-mock-url="newMockUrl"
          :rule="rule"
          :maintainer-options="maintainerOptions"
          :module-options="moduleOptions"
          :request="request"
          :old-request="oldRequest"
          :response="response"
          :old-response="oldResponse"
        ></http-api-version-diff>
      </el-dialog>

    </el-card>

    <el-dialog
      :title="$t('commons.sync_other_info')"
      :visible.sync="createNewVersionVisible"
      :show-close="false"
      width="30%"
    >
      <div>
        <el-checkbox v-model="httpForm.newVersionRemark">{{ $t('commons.remark') }}</el-checkbox>
        <el-checkbox v-model="httpForm.newVersionDeps">{{ $t('commons.relationship.name') }}</el-checkbox>
      </div>

      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="cancelCreateNewVersion"
          :title="$t('commons.edit_info')"
          @confirm="saveApi">
        </ms-dialog-footer>
      </template>
    </el-dialog>
  </div>
</template>

<script>

import MsApiRequestForm from "../request/http/ApiHttpRequestForm";
import MsResponseText from "../response/ResponseText";
import {API_STATUS, REQ_METHOD} from "../../model/JsonData";
import {KeyValue} from "../../model/ApiTestModel";
import MsInputTag from "@/business/components/api/automation/scenario/MsInputTag";
import MsJsr233Processor from "../../../automation/scenario/component/Jsr233Processor";
import MsSelectTree from "../../../../common/select-tree/SelectTree";
import MsChangeHistory from "../../../../history/ChangeHistory";
import {getCurrentProjectID, getCurrentUser, getUUID, hasLicense} from "@/common/js/utils";
import MsFormDivider from "@/business/components/common/components/MsFormDivider";
import ApiOtherInfo from "@/business/components/api/definition/components/complete/ApiOtherInfo";
import HttpApiVersionDiff from "./version/HttpApiVersionDiff";
import {createComponent} from ".././jmeter/components";
import {TYPE_TO_C} from "@/business/components/api/automation/scenario/Setting";
import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";

const {Body} = require("@/business/components/api/definition/model/ApiTestModel");
const Sampler = require("@/business/components/api/definition/components/jmeter/components/sampler/sampler");

const requireComponent = require.context('@/business/components/xpack/', true, /\.vue$/);
const versionHistory = requireComponent.keys().length > 0 ? requireComponent("./version/VersionHistory.vue") : {};

export default {
  name: "MsAddCompleteHttpApi",
  components: {
    MsDialogFooter,
    'MsVersionHistory': versionHistory.default,
    ApiOtherInfo,
    MsFormDivider,
    MsJsr233Processor, MsResponseText, MsApiRequestForm, MsInputTag, MsSelectTree, MsChangeHistory,
    HttpApiVersionDiff
  },
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
      newData:{environmentId: "", path: "", tags: []},
      dialogVisible:false,
      isShowEnable: true,
      showFollow: false,
      newShowFollow:false,
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
      newMockBaseUrl: "",
      count: 0,
      versionData: [],
      oldRequest:Sampler,
      oldResponse:{},
      createNewVersionVisible: false,
    };
  },
  props: {moduleOptions: {}, request: {}, response: {}, basisData: {}, syncTabs: Array, projectId: String},
  watch: {
    'httpForm.name': {
      handler(v, v1) {
        if (v && v1 && v !== v1) {
          this.apiMapStatus();
        }
      }
    },
    'httpForm.path': {
      handler(v, v1) {
        if (v && v1 && v !== v1) {
          this.apiMapStatus();
        }
      }
    },
    'httpForm.userId': {
      handler(v, v1) {
        if (v && v1 && v !== v1) {
          this.apiMapStatus();
        }
      }
    },
    'httpForm.moduleId': {
      handler(v, v1) {
        if (v && v1 && v !== v1) {
          this.apiMapStatus();
        }
      }
    },
    'httpForm.status': {
      handler(v, v1) {
        if (v && v1 && v !== v1) {
          this.apiMapStatus();
        }
      }
    },
    'httpForm.follows': {
      handler(v, v1) {
        if (v && v1 && JSON.stringify(v) !== JSON.stringify(v1)) {
          this.apiMapStatus();
        }
      }
    },
    'httpForm.description': {
      handler(v, v1) {
        if (v && v1 !== undefined && v !== v1) {
          this.apiMapStatus();
        }
      }
    },
    'httpForm.tags': {
      handler(v, v1) {
        this.count++;
        if (v && v1 && JSON.stringify(v) !== JSON.stringify(v1) && this.count > 1) {
          this.apiMapStatus();
        }
      }
    },
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
            });
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
        return this.mockBaseUrl + path;
      }
    },
    newMockUrl() {
      if (this.newData.path == null) {
        return this.newMockBaseUrl;
      } else {
        let path = this.newData.path;
        let protocol = this.newData.method;
        if (protocol === 'GET' || protocol === 'DELETE') {
          if (this.newData.request != null && this.newData.request.rest != null) {
            let pathUrlArr = path.split("/");
            let newPath = "";
            pathUrlArr.forEach(item => {
              if (item !== "") {
                let pathItem = item;
                if (item.indexOf("{") === 0 && item.indexOf("}") === (item.length - 1)) {
                  let paramItem = item.substr(1, item.length - 2);
                  for (let i = 0; i < this.newData.request.rest.length; i++) {
                    let param = this.newData.request.rest[i];
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
        return this.newMockBaseUrl + path;
      }
    }
  },
  methods: {
    apiMapStatus() {
      this.$store.state.apiStatus.set("fromChange", true);
      if (this.httpForm.id) {
        this.$store.state.apiMap.set(this.httpForm.id, this.$store.state.apiStatus);
      }
    },
    currentUser: () => {
      return getCurrentUser();
    },
    openHis() {
      this.$refs.changeHistory.open(this.httpForm.id, ["接口定义", "接口定義", "Api definition", "API_DEFINITION"]);
    },
    mockSetting() {
      if (this.basisData.id) {
        this.$store.state.currentApiCase = {mock: getUUID()};
        this.$emit('changeTab', 'mock');
      } else {
        this.$alert(this.$t('api_test.mock.create_error'));
      }
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

          if (!this.httpForm.versionId) {
            if (this.$refs.versionHistory && this.$refs.versionHistory.currentVersion) {
              this.httpForm.versionId = this.$refs.versionHistory.currentVersion.id;
            }
          }
          this.$emit('saveApi', this.httpForm);
          this.count = 0;
          this.$store.state.apiMap.delete(this.httpForm.id);
        } else {
          return false;
        }
      });
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
          if (key) {
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
      let protocol = document.location.protocol;
      protocol = protocol.substring(0, protocol.indexOf(":"));
      let url = "/api/definition/getMockEnvironment/";
      this.$get(url + this.projectId, response => {
        this.mockEnvironment = response.data;
        let httpConfig = JSON.parse(this.mockEnvironment.config);
        if (httpConfig != null) {
          httpConfig = httpConfig.httpConfig;
          let httpType = httpConfig.defaultCondition;
          let conditions = httpConfig.conditions;
          conditions.forEach(condition => {
            if (condition.type === httpType) {
              this.mockBaseUrl = condition.protocol + "://" + condition.socket;
              this.newMockBaseUrl = this.mockBaseUrl;
            }
          });
        }
      });
    },
    saveFollow() {
      if (this.showFollow) {
        this.showFollow = false;
        for (let i = 0; i < this.httpForm.follows.length; i++) {
          if (this.httpForm.follows[i] === this.currentUser().id) {
            this.httpForm.follows.splice(i, 1);
            break;
          }
        }
        if (this.basisData.id) {
          this.$post("/api/definition/update/follows/" + this.basisData.id, this.httpForm.follows, () => {
            this.$success(this.$t('commons.cancel_follow_success'));
          });
        }
      } else {
        this.showFollow = true;
        if (!this.httpForm.follows) {
          this.httpForm.follows = [];
        }
        this.httpForm.follows.push(this.currentUser().id);
        if (this.basisData.id) {
          this.$post("/api/definition/update/follows/" + this.basisData.id, this.httpForm.follows, () => {
            this.$success(this.$t('commons.follow_success'));
          });
        }
      }
    },
    getVersionHistory() {
      this.$get('/api/definition/versions/' + this.httpForm.id, response => {
        if (this.httpForm.isCopy) {
          this.versionData = response.data.filter(v => v.versionId === this.httpForm.versionId);
        } else {
          this.versionData = response.data;
        }
      });
    },
    compare(row) {
      this.$get('/api/definition/get/' +  row.id+"/"+this.httpForm.refId, response => {
        this.$get('/api/definition/get/' + response.data.id, res => {
          if (res.data) {
            this.newData = res.data;
            this.dealWithTag(res.data);
            this.setRequest(res.data)
            if (!this.setRequest(res.data)) {
              this.oldRequest = createComponent("HTTPSamplerProxy");
              this.dialogVisible = true;
            }
            this.formatApi(res.data)
          }
        });
      });
    },
    setRequest(api) {
      if (api.request !== undefined) {
        if (Object.prototype.toString.call(api.request).match(/\[object (\w+)\]/)[1].toLowerCase() === 'object') {
          this.oldRequest = api.request;
        } else {
          this.oldRequest = JSON.parse(api.request);
        }
        if (!this.oldRequest.headers) {
          this.oldRequest.headers = [];
        }
        this.dialogVisible = true;
        return true;
      }
      return false;
    },
    dealWithTag(api){
      if(api.tags){
        if(Object.prototype.toString.call(api.tags)==="[object String]"){
          api.tags = JSON.parse(api.tags);
        }
      }
      if(this.httpForm.tags){
        if(Object.prototype.toString.call(this.httpForm.tags)==="[object String]"){
          this.httpForm.tags = JSON.parse(this.httpForm.tags);
        }
      }
    },
    formatApi(api) {
      if (api.response != null && api.response !== 'null' && api.response !== undefined) {
        if (Object.prototype.toString.call(api.response).match(/\[object (\w+)\]/)[1].toLowerCase() === 'object') {
          this.oldResponse = api.response;
        } else {
          this.oldResponse = JSON.parse(api.response);
        }
      } else {
        this.oldResponse = {headers: [], body: new Body(), statusCode: [], type: "HTTP"};
      }
      if (!this.oldRequest.hashTree) {
        this.oldRequest.hashTree = [];
      }
      if (this.oldRequest.body && !this.oldRequest.body.binary) {
        this.oldRequest.body.binary = [];
      }
      // 处理导入数据缺失问题
      if (this.oldResponse.body) {
        let body = new Body();
        Object.assign(body, this.oldResponse.body);
        if (!body.binary) {
          body.binary = [];
        }
        if (!body.kvs) {
          body.kvs = [];
        }
        if (!body.binary) {
          body.binary = [];
        }
        this.oldResponse.body = body;
      }
      this.oldRequest.clazzName = TYPE_TO_C.get(this.oldRequest.type);

      this.sort(this.oldRequest.hashTree);
    },
    sort(stepArray) {
      if (stepArray) {
        for (let i in stepArray) {
          if (!stepArray[i].clazzName) {
            stepArray[i].clazzName = TYPE_TO_C.get(stepArray[i].type);
          }
          if (stepArray[i].type === "Assertions" && !stepArray[i].document) {
            stepArray[i].document = {type: "JSON", data: {xmlFollowAPI: false, jsonFollowAPI: false, json: [], xml: []}};
          }
          if (stepArray[i].hashTree && stepArray[i].hashTree.length > 0) {
            this.sort(stepArray[i].hashTree);
          }
        }
      }
    },
    cancelCreateNewVersion() {
      this.createNewVersionVisible = false;
      this.getVersionHistory();
    },
    checkout(row) {
      let api = this.versionData.filter(v => v.versionId === row.id)[0];
      if (api.tags && api.tags.length > 0) {
        api.tags = JSON.parse(api.tags);
      }
      this.$emit("checkout", api);
    },
    create(row) {
      // 创建新版本
      this.httpForm.versionId = row.id;
      this.httpForm.versionName = row.name;
      this.$set(this.httpForm, 'newVersionRemark', !!this.httpForm.remark);
      this.$set(this.httpForm, 'newVersionDeps', this.$refs.apiOtherInfo.relationshipCount > 0);
      if (this.$refs.apiOtherInfo.relationshipCount > 0 || this.httpForm.remark) {
        this.createNewVersionVisible = true;
      } else {
        this.saveApi();
      }
    },
    del(row) {
      this.$alert(this.$t('api_test.definition.request.delete_confirm') + ' ' + row.name + " ？", '', {
        confirmButtonText: this.$t('commons.confirm'),
        callback: (action) => {
          if (action === 'confirm') {
            this.$get('/api/definition/delete/' + row.id + '/' + this.httpForm.refId, () => {
              this.$success(this.$t('commons.delete_success'));
              this.getVersionHistory();
            });
          }
        }
      });
    }
  },

  created() {
    this.getMaintainerOptions();
    if (!this.basisData.environmentId) {
      this.basisData.environmentId = "";
    }
    if (this.basisData.moduleId && this.basisData.moduleId === 'default-module') {
      this.basisData.moduleId = this.moduleOptions[0].id;
    }
    this.httpForm = JSON.parse(JSON.stringify(this.basisData));
    this.$get('/api/definition/follow/' + this.basisData.id, response => {
      this.httpForm.follows = response.data;
      for (let i = 0; i < response.data.length; i++) {
        if (response.data[i] === this.currentUser().id) {
          this.showFollow = true;
          break;
        }
      }
    });
    this.initMockEnvironment();

    if (hasLicense()) {
      this.getVersionHistory();
    }
  }
};
</script>

<style scoped>

.base-info .el-form-item {
  width: 100%;
}

.mock-info {
  margin: 20px 45px;
}

.ms-opt-btn {
  position: fixed;
  right: 50px;
  z-index: 1;
  top: 128px;
}

/*.base-info .el-form-item >>> .el-form-item__content {*/
/*  width: 80%;*/
/*}*/

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
