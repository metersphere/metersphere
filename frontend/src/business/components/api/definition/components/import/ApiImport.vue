<template>
  <el-dialog :close-on-click-modal="false" :title="$t('api_test.api_import.title')" :width="dialogWidth"
             :visible.sync="visible" class="api-import" v-loading="result.loading" @close="close"
             :destroy-on-close="true">

    <div class="header-bar">
      <div>{{ $t('api_test.api_import.data_format') }}</div>
      <el-radio-group v-model="selectedPlatformValue">
        <span v-for="(item, index) in platforms" :key="index">
          <el-radio v-if="!isScenarioModel || item.name != 'Swagger'" :label="item.value">{{ item.name }}</el-radio>
        </span>
      </el-radio-group>

      <div class="operate-button">
        <el-button class="save-button" type="primary" plain @click="save">
          {{ $t('commons.save') }}
        </el-button>
        <el-button class="cancel-button" type="warning" plain @click="visible = false">
          {{ $t('commons.cancel') }}
        </el-button>
      </div>
    </div>

    <el-form :model="formData" :rules="rules" label-width="100px" v-loading="result.loading" ref="form">
      <el-row>
        <el-col :span="isSwagger2 && authEnable && swaggerUrlEnable ? 8: 11">
          <el-form-item :label="$t('commons.import_module')" prop="moduleId">
            <ms-select-tree size="small" :data="moduleOptions" :defaultKey="formData.moduleId" @getValue="setModule"
                            :obj="moduleObj" clearable checkStrictly/>
          </el-form-item>
          <el-form-item v-if="!isScenarioModel&&showImportModel" :label="$t('commons.import_mode')" prop="modeId">
            <el-select size="small" v-model="formData.modeId" clearable style="width: 100%">
              <el-option v-for="item in modeOptions" :key="item.id" :label="item.name" :value="item.id"/>
            </el-select>
          </el-form-item>
          <el-form-item v-xpack v-if="projectVersionEnable && formData.modeId === 'incrementalMerge'"
                        :label="$t('api_test.api_import.import_version')" prop="versionId">
            <el-select size="small" v-model="formData.versionId" clearable style="width: 100%">
              <el-option v-for="item in versionOptions" :key="item.id" :label="item.name" :value="item.id"/>
            </el-select>
          </el-form-item>
          <el-form-item v-xpack v-if="projectVersionEnable && formData.modeId === 'fullCoverage'"
                        :label="$t('api_test.api_import.data_update_version')" prop="versionId">
            <el-select size="small" v-model="formData.updateVersionId" clearable style="width: 100%">
              <el-option v-for="item in versionOptions" :key="item.id" :label="item.name" :value="item.id"/>
            </el-select>
          </el-form-item>
          <el-form-item v-xpack v-if="projectVersionEnable && formData.modeId === 'fullCoverage'"
                        :label="$t('api_test.api_import.data_new_version')" prop="versionId">
            <el-select size="small" v-model="formData.versionId" clearable style="width: 100%">
              <el-option v-for="item in versionOptions" :key="item.id" :label="item.name" :value="item.id"/>
            </el-select>
          </el-form-item>
          <el-form-item v-if="showTemplate">
            <el-link type="primary" class="download-template"
                     @click="downloadTemplate"
            >{{$t('test_track.case.import.download_template')}}
            </el-link>
          </el-form-item>
          <el-form-item v-if="isSwagger2">
            <el-switch
              v-model="swaggerUrlEnable"
              :active-text="$t('api_test.api_import.swagger_url_import')">
            </el-switch>
          </el-form-item>

        </el-col>
        <el-col :span="1">
          <el-divider direction="vertical"/>
        </el-col>

        <el-col :span="12" v-show="isSwagger2 && swaggerUrlEnable" style="margin-top: 40px">
          <el-form-item :label="'Swagger URL'" prop="swaggerUrl" class="swagger-url">
            <el-input size="small" v-model="formData.swaggerUrl" clearable show-word-limit/>
          </el-form-item>
          <el-switch v-model="authEnable" :active-text="$t('api_test.api_import.add_request_params')"
                     @change="changeAuthEnable"></el-switch>
        </el-col>

        <el-col :span="14" v-show="isSwagger2 && authEnable && swaggerUrlEnable">
            <!--请求头 -->
            <div style="margin-top: 15px;">
              <span>{{$t('api_test.request.headers')}}{{$t('api_test.api_import.optional')}}：</span>
            </div>
            <ms-api-key-value :show-desc="true" :isShowEnable="isShowEnable" :suggestions="headerSuggestions" :items="headers"/>
            <!--query 参数-->
            <div style="margin-top: 10px">
              <span>{{$t('api_test.definition.request.query_param')}}{{$t('api_test.api_import.optional')}}：</span>
            </div>
            <ms-api-variable :with-mor-setting="true" :is-read-only="isReadOnly" :isShowEnable="isShowEnable" :parameters="queryArguments"/>
            <!--认证配置-->
            <div style="margin-top: 10px">
              <span>{{$t('api_test.definition.request.auth_config')}}{{$t('api_test.api_import.optional')}}：</span>
            </div>
            <ms-api-auth-config :is-read-only="isReadOnly" :request="authConfig" :encryptShow="false" ref="importAuth"/>
        </el-col>

        <el-col :span="12"
                v-if="selectedPlatformValue != 'Swagger2' || (selectedPlatformValue == 'Swagger2' && !swaggerUrlEnable)">
          <el-upload
            class="api-upload"
            drag
            action=""
            :http-request="upload"
            :limit="1"
            :beforeUpload="uploadValidate"
            :on-remove="handleRemove"
            :file-list="fileList"
            :on-exceed="handleExceed"
            multiple>
            <i class="el-icon-upload"></i>
            <div class="el-upload__text" v-html="$t('load_test.upload_tips')"></div>
            <div class="el-upload__tip" slot="tip">{{ $t('api_test.api_import.file_size_limit') }}</div>
          </el-upload>
        </el-col>
      </el-row>
    </el-form>

    <div class="format-tip">
      <div>
        <span>{{ $t('api_test.api_import.tip') }}：{{ selectedPlatform.tip }}</span>
      </div>
      <div>
        <span>{{ $t('api_test.api_import.export_tip') }}：{{ selectedPlatform.exportTip }}</span>
      </div>
      <div>
        <span>
          {{ $t('api_test.api_import.import_cover_tip') }}<br/>
          {{ $t('api_test.api_import.cover_tip_1') }}<br/>
          {{ $t('api_test.api_import.cover_tip_2') }}<br/>
          {{ $t('api_test.api_import.cover_tip_3') }}
        </span>
      </div>
    </div>
  </el-dialog>
</template>

<script>
import MsDialogFooter from "../../../../common/components/MsDialogFooter";
import {getCurrentProjectID, hasLicense, listenGoBack, removeGoBackListener} from "@/common/js/utils";
import MsSelectTree from "../../../../common/select-tree/SelectTree";
import MsApiKeyValue from "../ApiKeyValue";
import MsApiVariable from "../ApiVariable";
import MsApiAuthConfig from "../auth/ApiAuthConfig";
import {REQUEST_HEADERS} from "@/common/js/constants";
import {TYPE_TO_C} from "@/business/components/api/automation/scenario/Setting";
import {KeyValue} from "../../model/ApiTestModel";

export default {
  name: "ApiImport",
  components: {
    MsDialogFooter,
    MsSelectTree,
    MsApiKeyValue,
    MsApiVariable,
    MsApiAuthConfig
  },
  props: {
    saved: {
      type: Boolean,
        default: true,
      },
      moduleOptions: Array,
      propotal: String,
      model: {
        type: String,
        default: 'definition'
      }
    },
    data() {
      return {
        visible: false,
        swaggerUrlEnable: false,
        authEnable: false,
        showEnvironmentSelect: true,
        showXpackCompnent:false,
        headerSuggestions: REQUEST_HEADERS,
        moduleObj: {
          id: 'id',
          label: 'name',
        },
        modeOptions: [
          {
            id: 'fullCoverage',
            name: this.$t('commons.cover')
          },
          {
            id: 'incrementalMerge',
            name: this.$t('commons.not_cover')
          }
        ],
        protocol: "",
        platforms: [
          {
            name: 'MeterSphere',
            value: 'Metersphere',
            tip: this.$t('api_test.api_import.ms_tip'),
            exportTip: this.$t('api_test.api_import.ms_export_tip'),
            suffixes: new Set(['json'])
          },
        ],
        postmanPlanform: {
          name: 'Postman',
          value: 'Postman',
          tip: this.$t('api_test.api_import.postman_tip'),
          exportTip: this.$t('api_test.api_import.post_export_tip'),
          suffixes: new Set(['json'])
        },
        swaggerPlanform: {
          name: 'Swagger',
          value: 'Swagger2',
          tip: this.$t('api_test.api_import.swagger_tip'),
          exportTip: this.$t('api_test.api_import.swagger_export_tip'),
          suffixes: new Set(['json'])
        },
        harPlanform: {
          name: 'HAR',
          value: 'Har',
          tip: this.$t('api_test.api_import.har_tip'),
          exportTip: this.$t('api_test.api_import.har_export_tip'),
          suffixes: new Set(['har'])
        },
        esbPlanform: {
          name: 'ESB',
          value: 'ESB',
          tip: this.$t('api_test.api_import.esb_tip'),
          exportTip: this.$t('api_test.api_import.esb_export_tip'),
          suffixes: new Set(['xlsx', 'xls'])
        },
        jmeterPlatform: {
          name: 'JMeter',
          value: 'Jmeter',
          tip: this.$t('api_test.api_import.jmeter_tip'),
          exportTip: this.$t('api_test.api_import.jmeter_export_tip'),
          suffixes: new Set(['jmx'])
        },
        selectedPlatform: {},
        selectedPlatformValue: 'Metersphere',
        result: {},
        projects: [],
        environments: [],
        useEnvironment: false,
        formData: {
          file: undefined,
          swaggerUrl: '',
          modeId: 'incrementalMerge',
          moduleId: ''
        },
        rules: {
          modeId: [
            {required: true, message: this.$t('commons.please_select_import_mode'), trigger: 'change'},
          ],
        },
        currentModule: {},
        fileList: [],
        isShowEnable: true,
        isReadOnly: false,
        headers: [],
        queryArguments: [],
        authConfig: {
          hashTree: []
        },
        versionOptions: [],
        projectVersionEnable: false,
      }
    },
    created() {
      this.platforms.push(this.postmanPlanform);
      this.platforms.push(this.swaggerPlanform);
      this.platforms.push(this.harPlanform);
      this.platforms.push(this.jmeterPlatform);
      this.selectedPlatform = this.platforms[0];
      //
      this.getVersionOptions();
      this.checkVersionEnable();
    },
    watch: {
      moduleOptions() {
        if (this.moduleOptions.length > 0) {
          this.formData.moduleId = this.moduleOptions[0].id;
        }
      },
      selectedPlatformValue() {
        for (let i in this.platforms) {
          if (this.platforms[i].value === this.selectedPlatformValue) {
            this.selectedPlatform = this.platforms[i];
            break;
          }
        }
        if (this.selectedPlatformValue === 'Har' || this.selectedPlatformValue === 'ESB') {
          this.formData.modeId = 'fullCoverage';
        }
      },
      propotal() {
        let postmanIndex = this.platforms.indexOf(this.postmanPlanform);
        let swaggerPlanformIndex = this.platforms.indexOf(this.swaggerPlanform);
        let harPlanformIndex = this.platforms.indexOf(this.harPlanform);
        let esbPlanformIndex = this.platforms.indexOf(this.esbPlanform);
        if (postmanIndex >= 0) {
          this.platforms.splice(this.platforms.indexOf(this.postmanPlanform), 1);
        }
        if (swaggerPlanformIndex >= 0) {
          this.platforms.splice(this.platforms.indexOf(this.swaggerPlanform), 1);
        }
        if (harPlanformIndex >= 0) {
          this.platforms.splice(this.platforms.indexOf(this.harPlanform), 1);
        }
        if (esbPlanformIndex >= 0) {
          this.platforms.splice(this.platforms.indexOf(this.esbPlanform), 1);
        }
        if (this.propotal === 'TCP') {
          if(hasLicense()){
            this.platforms.push(this.esbPlanform);
          }
          return true;
        } else if (this.propotal === 'HTTP') {
          this.platforms.push(this.postmanPlanform);
          this.platforms.push(this.swaggerPlanform);
          this.platforms.push(this.harPlanform);
          return false;
        }
      }
    },
    computed: {
      isSwagger2() {
        return this.selectedPlatformValue === 'Swagger2';
      },
      showImportModel() {
        return this.selectedPlatformValue != 'Har' && this.selectedPlatformValue != 'ESB';
      },
      showTemplate() {
        return this.selectedPlatformValue === 'ESB';
      },
      isScenarioModel() {
        return this.model === 'scenario';
      },
      projectId() {
        return getCurrentProjectID();
      },
      dialogWidth() {
        if (this.isSwagger2 && this.authEnable && this.swaggerUrlEnable) {
          return '80%';
        }
        return '30%';
      }
    },
    methods: {
      open(module) {
        this.currentModule = module;
        this.visible = true;
        listenGoBack(this.close);

      },
      upload(file) {
        this.formData.file = file.file;
      },
      handleExceed(files, fileList) {
        this.$warning(this.$t('test_track.case.import.upload_limit_count'));
      },
      handleRemove(file, fileList) {
        this.formData.file = undefined;
      },
      downloadTemplate() {
        if (this.selectedPlatformValue == "ESB") {
          this.$fileDownload('/api/definition/export/esbExcelTemplate');
        }
      },
      uploadValidate(file, fileList) {
        let suffix = file.name.substring(file.name.lastIndexOf('.') + 1);
        if (this.selectedPlatform.suffixes && !this.selectedPlatform.suffixes.has(suffix)) {
          this.$warning(this.$t('api_test.api_import.suffixFormatErr'));
          return false;
        }
        if (file.size / 1024 / 1024 > 100) {
          this.$warning(this.$t('test_track.case.import.upload_limit_size'));
          return false;
        }
        return true;
      },
      save() {
        this.$refs.form.validate(valid => {
          if (valid) {
            if ((this.selectedPlatformValue != 'Swagger2' || (this.selectedPlatformValue == 'Swagger2' && !this.swaggerUrlEnable)) && !this.formData.file) {
              this.$warning(this.$t('commons.please_upload'));
              return;
            }
            let url = '/api/definition/import';
            if (this.isScenarioModel) {
              url = '/api/automation/import';
            }
            let param = this.buildParam();
            this.result = this.$fileUpload(url, param.file, null, this.buildParam(), response => {
              let res = response.data;
              this.$success(this.$t('test_track.case.import.success'));
              this.visible = false;
              this.$emit('refresh', res);
            });
          } else {
            return false;
          }
        });
      },
      setModule(id, data) {
        this.formData.moduleId = id;
        this.formData.modulePath = data.path;
      },
      clearAuthInfo(){
        this.headers = [];
        this.queryArguments = [];
        this.headers.push(new KeyValue({enable: true}));
        this.queryArguments.push(new KeyValue({enable: true}));
        this.authConfig = {hashTree: [], authManager: {}};
        this.$refs.importAuth.initData();
      },
      changeAuthEnable() {
        if(!this.authEnable){
          this.clearAuthInfo();
        }
      },
      buildParam() {
        let param = {};
        Object.assign(param, this.formData);
        param.platform = this.selectedPlatformValue;
        param.saved = this.saved;
        param.model = this.model;
        if (this.currentModule) {
          param.moduleId = this.formData.moduleId
          param.modeId = this.formData.modeId
        }
        param.projectId = this.projectId;
        if (!this.swaggerUrlEnable) {
          param.swaggerUrl = undefined;
        }
        if(this.authEnable){
          // 设置请求头
          param.headers = this.headers;
          // 设置 query 参数
          param.arguments = this.queryArguments;
          // 设置 BaseAuth 参数
          if(this.authConfig.authManager != undefined){
            this.authConfig.authManager.clazzName = TYPE_TO_C.get("AuthManager");
            param.authManager = this.authConfig.authManager;
          }
        }
        return param;
      },
      close() {
        this.formData = {
          file: undefined,
          swaggerUrl: '',
          modeId: this.formData.modeId,
        };
        this.fileList = [];
        removeGoBackListener(this.close);
        this.visible = false;
      },
      getVersionOptions() {
        if (hasLicense()) {
          this.$get('/project/version/get-project-versions/' + getCurrentProjectID(), response => {
            this.versionOptions = response.data.filter(v => v.status === 'open');
            this.versionOptions.forEach(v => {
              if (v.latest) {
                v.name = v.name + ' ' + this.$t('api_test.api_import.latest_version');
              }
            });
          });
        }
      },
      checkVersionEnable() {
        if (!this.projectId) {
          return;
        }
        if (hasLicense()) {
          this.$get('/project/version/enable/' + this.projectId, response => {
            this.projectVersionEnable = response.data;
          });
        }
      }
    }
  }
</script>

<style scoped>

  .api-import >>> .el-dialog {
    min-width: 750px;
  }

  .format-tip {
    background: #EDEDED;
  }

  .api-upload {
    text-align: center;
    margin: auto 0;
  }

  .api-upload >>> .el-upload {
    width: 100%;
    max-width: 350px;
  }

  .api-upload >>> .el-upload-dragger {
    width: 100%;
  }

  .el-radio-group {
    margin: 10px 0;
  }

  .el-radio {
    margin-right: 20px;
  }

  .header-bar, .format-tip, .el-form {
    border: solid #E1E1E1 1px;
    margin: 10px 0;
    padding: 10px;
    border-radius: 3px;
  }

  .header-bar {
    padding: 10px 15px;
  }

  .api-import >>> .el-dialog__body {
    padding: 15px 25px;
  }

  .operate-button {
    float: right;
  }

  .save-button {
    margin-left: 10px;
  }

  .el-form {
    padding: 30px 10px;
  }

  .dialog-footer {
    float: right;
  }

  .swagger-url-disable {
    margin-top: 10px;

    margin-left: 80px;
  }

  .el-divider {
    height: 200px;
  }

</style>
