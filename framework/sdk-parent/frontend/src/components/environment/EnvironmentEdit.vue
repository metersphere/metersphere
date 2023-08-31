<template>
  <el-main class="environment-edit" style="margin-left: 0px">
    <el-form :model="environment" :rules="rules" ref="environment" label-width="80px">
      <el-row>
        <el-col :span="10" v-if="!isProject">
          <el-form-item class="project-item" prop="currentProjectId" :label="$t('project.select')">
            <el-select v-model="environment.currentProjectId" filterable clearable
                       size="small" :disabled="!ifCreate">
              <el-option v-for="item in projectList" :key="item.id" :label="item.name" :value="item.id"></el-option>
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="10">
          <el-form-item prop="name" :label="$t('api_test.environment.name')">
            <el-input v-model="environment.name" :disabled="isReadOnly" :placeholder="this.$t('commons.input_name')"
                      clearable size="small"/>
          </el-form-item>
        </el-col>
        <el-col :span="4" v-if="!hideButton" :offset="isProject ? 10 : 0">
          <div style="float: right;width: fit-content;">
            <div style="float: left; margin-right: 8px;">
              <slot name="other"></slot>
            </div>
            <div class="ms_btn">
              <el-button type="primary" @click="confirm" @keydown.enter.native.prevent size="small" :disabled="isReadOnly">
                {{ $t('commons.confirm') }}
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>

      <el-tabs v-model="activeName">

        <el-tab-pane :label="$t('api_test.environment.common_config')" name="common">
          <ms-environment-common-config :common-config="environment.config.commonConfig" ref="commonConfig"
                                        :is-read-only="isReadOnly"/>
        </el-tab-pane>

        <el-tab-pane :label="$t('api_test.environment.http_config')" name="http">
          <ms-environment-http-config :project-id="environment.projectId" :http-config="environment.config.httpConfig"
                                      ref="httpConfig" :is-read-only="isReadOnly"/>
        </el-tab-pane>
        <el-tab-pane :label="$t('api_test.environment.database_config')" name="sql">
          <ms-database-config :configs="environment.config.databaseConfigs" :is-read-only="isReadOnly"/>
        </el-tab-pane>
        <el-tab-pane :label="$t('api_test.environment.tcp_config')" name="tcp">
          <ms-tcp-config :config="environment.config.tcpConfig" :is-read-only="isReadOnly"/>
        </el-tab-pane>
        <el-tab-pane :label="$t('commons.ssl.config')" name="ssl">
          <ms-environment-s-s-l-config :project-id="environment.projectId" :ssl-config="environment.config.sslConfig"
                                       :is-read-only="isReadOnly"/>
        </el-tab-pane>
        <el-tab-pane :label="$t('api_test.definition.request.all_pre_script')" name="prescript">
          <div style="padding-bottom: 20px;" v-if="!ifCreate">
            <el-link style="float: right;" type="primary" @click="openHis('preScript')">
              {{ $t('operating_log.change_history') }}
            </el-link>
          </div>
          <environment-global-script
            v-if="isRefresh && environment.config.globalScriptConfig && environment.config.preProcessor && environment.config.preStepProcessor"
            :filter-request.sync="environment.config.globalScriptConfig.filterRequestPreScript"
            :exec-after-private-script.sync="environment.config.globalScriptConfig.isPreScriptExecAfterPrivateScript"
            :conn-scenario.sync="environment.config.globalScriptConfig.connScenarioPreScript"
            :script-processor="environment.config.preProcessor"
            :scrpit-step-processor="environment.config.preStepProcessor"
            :is-pre-processor="true"
            :is-read-only="isReadOnly"
            @updateGlobalScript="updateGlobalScript"/>

        </el-tab-pane>
        <el-tab-pane :label="$t('api_test.definition.request.all_post_script')" name="postscript">
          <div style="padding-bottom: 20px;" v-if="!ifCreate">
            <el-link style="float: right;" type="primary" @click="openHis('postScript')">
              {{ $t('operating_log.change_history') }}
            </el-link>
          </div>
          <environment-global-script
            v-if="isRefresh && environment.config.globalScriptConfig && environment.config.postProcessor && environment.config.postStepProcessor"
            :filter-request.sync="environment.config.globalScriptConfig.filterRequestPostScript"
            :exec-after-private-script.sync="environment.config.globalScriptConfig.isPostScriptExecAfterPrivateScript"
            :conn-scenario.sync="environment.config.globalScriptConfig.connScenarioPostScript"
            :script-processor="environment.config.postProcessor"
            :scrpit-step-processor="environment.config.postStepProcessor"
            :is-pre-processor="false"
            :is-read-only="isReadOnly"
            @updateGlobalScript="updateGlobalScript"/>
        </el-tab-pane>
        <!-- 认证配置 -->
        <el-tab-pane :label="$t('api_test.definition.request.all_auth_config')" name="authConfig" v-if="isRefresh">
          <el-tooltip class="item-tabs" effect="dark" :content="$t('api_test.definition.request.auth_config_info')"
                      placement="top-start" slot="label">
            <span>{{ $t('api_test.definition.request.all_auth_config') }}</span>
          </el-tooltip>
          <ms-api-auth-config :is-read-only="isReadOnly" :request="environment.config.authManager"/>
        </el-tab-pane>
        <!--全局断言-->
        <el-tab-pane :label="$t('env_options.all_assertions')" name="assertions">
          <el-tooltip class="item-tabs" effect="dark" :content="$t('env_options.all_assertions')"
                      placement="top-start" slot="label">
            <span>{{ $t('env_options.all_assertions') }}</span>
          </el-tooltip>
          <div v-if="hasLicense" style="margin-bottom: 15px">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-form-item
                  :label="$t('error_report_library.use_error_report')"
                  prop="status" style="margin-bottom: 0px;">
                  <el-switch :disabled="isReadOnly"
                             v-model="environment.config.useErrorCode" style="margin-right: 10px"/>
                </el-form-item>
              </el-col>
            </el-row>
            <el-row v-show="environment.config.useErrorCode" :gutter="20">
              <el-col style="margin-left: 30px">
                {{ $t('error_report_library.conflict_with_success') }}
                <el-switch
                  class="errorReportConfigSwitch"
                  v-model="environment.config.higherThanSuccess"
                  :active-text="$t('error_report_library.option.name')"
                  :inactive-text="$t('api_test.automation.request_success')">
                </el-switch>
              </el-col>
            </el-row>
            <el-row v-show="environment.config.useErrorCode" :gutter="20">
              <el-col style="margin-left: 30px">
                {{ $t('error_report_library.conflict_with_error') }}
                <el-switch
                  class="errorReportConfigSwitch"
                  v-model="environment.config.higherThanError"
                  :active-text="$t('error_report_library.option.name')"
                  :inactive-text="$t('api_test.automation.request_error')">
                </el-switch>
              </el-col>
            </el-row>
          </div>

          <global-assertions :is-read-only="isReadOnly" :assertions="environment.config.assertions"
                             :is-show-json-path-suggest="false"/>

        </el-tab-pane>
      </el-tabs>

    </el-form>
    <ms-change-history ref="changeHistory"/>
  </el-main>
</template>

<script>
import {editEnv} from "../../api/environment";
import MsApiScenarioVariables from "./commons/ApiScenarioVariables";
import MsApiKeyValue from "./commons/ApiKeyValue";
import MsDialogFooter from "../MsDialogFooter";
import {REQUEST_HEADERS} from "../../utils/constants";
import {Environment} from "../../model/EnvironmentModel";
import MsApiHostTable from "./commons/ApiHostTable";
import MsDatabaseConfig from "./database/DatabaseConfig";
import MsEnvironmentHttpConfig from "./EnvironmentHttpConfig";
import MsEnvironmentCommonConfig from "./EnvironmentCommonConfig";
import MsEnvironmentSSLConfig from "./EnvironmentSSLConfig";
import MsApiAuthConfig from "./auth/ApiAuthConfig";
import MsTcpConfig from "./tcp/TcpConfig";
import {getUUID} from "../../utils";
import {hasLicense} from "../../utils/permission";
import MsChangeHistory from "./history/EnvHistory";
import MsDialogHeader from "../MsDialogHeader";
import GlobalAssertions from "./assertion/GlobalAssertions";
import EnvironmentGlobalScript from "./EnvironmentGlobalScript";

export default {
  name: "EnvironmentEdit",
  components: {
    MsTcpConfig,
    MsApiAuthConfig,
    MsEnvironmentCommonConfig,
    MsEnvironmentHttpConfig,
    MsEnvironmentSSLConfig,
    MsDatabaseConfig, MsApiHostTable, MsDialogFooter, MsApiKeyValue, MsApiScenarioVariables, MsChangeHistory,
    MsDialogHeader, GlobalAssertions, EnvironmentGlobalScript
  },
  props: {
    environment: new Environment(),
    projectId: String,
    isReadOnly: {
      type: Boolean,
      default: false
    },
    hideButton: Boolean,
    ifCreate: {
      type: Boolean,
      default: false
    },
    projectList: {
      type: Array,
      default() {
        return [];
      }
    },
    isProject: Boolean
  },
  data() {
    return {
      result: false,
      envEnable: false,
      isRefresh: true,
      rules: {
        name: [
          {required: true, message: this.$t('commons.input_name'), trigger: 'blur'},
          {max: 64, message: this.$t('commons.input_limit', [1, 64]), trigger: 'blur'}
        ],
        currentProjectId: [
          {required: true, message: this.$t('workspace.env_group.please_select_project'), trigger: ['change', 'blur']},
        ],
      },
      headerSuggestions: REQUEST_HEADERS,
      activeName: 'common'
    }
  },
  created() {
    if (!this.environment.config.preProcessor) {
      this.environment.config.preProcessor = {};
    }
    if (!this.environment.config.postProcessor) {
      this.environment.config.postProcessor = {};
    }
    if (!this.environment.config.preStepProcessor) {
      this.environment.config.preStepProcessor = {};
    }
    if (!this.environment.config.postStepProcessor) {
      this.environment.config.postStepProcessor = {};
    }
    if (!this.environment.config.globalScriptConfig) {
      this.environment.config.globalScriptConfig = {
        filterRequestPreScript: [],
        filterRequestPostScript: [],
        isPreScriptExecAfterPrivateScript: false,
        isPostScriptExecAfterPrivateScript: false,
        connScenarioPreScript: false,
        connScenarioPostScript: false,
      };
    }
    if (!this.environment.config.authManager) {
      this.environment.config.authManager = {'hashTree': []};
    }
    if (!this.environment.config.authManager.hashTree) {
      this.environment.config.authManager.hashTree = [];
    }
    if (!this.environment.config.assertions) {
      this.$set(this.environment.config, 'assertions', {
        duration: {duration: 0},
        regex: [],
        jsonPath: [],
        xpath2: [],
        jsr223: [],
        document: {type: "JSON", data: {json: [], xml: []}, enable: true},
      });
    }
  },

  watch: {
    environment: function (o) {
      if (!this.environment.config.preProcessor) {
        this.environment.config.preProcessor = {};
        if (!this.environment.config.preProcessor.script) {
          this.environment.config.preProcessor.script = "";
        }
      }
      if (!this.environment.config.postProcessor) {
        this.environment.config.postProcessor = {};
        if (!this.environment.config.postProcessor.script) {
          this.environment.config.postProcessor.script = "";
        }
      }
      if (!this.environment.config.preStepProcessor) {
        this.environment.config.preStepProcessor = {};
      }
      if (!this.environment.config.postStepProcessor) {
        this.environment.config.postStepProcessor = {};
      }
      if (!this.environment.config.globalScriptConfig) {
        this.environment.config.globalScriptConfig = {
          filterRequestPreScript: [],
          filterRequestPostScript: [],
          isPreScriptExecAfterPrivateScript: false,
          isPostScriptExecAfterPrivateScript: false,
          connScenarioPreScript: false,
          connScenarioPostScript: false,
        };
      }

      if (!this.environment.config.authManager) {
        this.environment.config.authManager = {'hashTree': []};
      }
      if (!this.environment.config.authManager.hashTree) {
        this.environment.config.authManager.hashTree = [];
      }
      if (!this.environment.config.assertions) {
        this.$set(this.environment.config, 'assertions', {
          duration: {duration: 0},
          regex: [],
          jsonPath: [],
          xpath2: [],
          jsr223: [],
          document: {type: "JSON", data: {json: [], xml: []}, enable: true},
        });
      }

      this.isRefresh = false;
      this.$nextTick(() => {
        this.isRefresh = true;
      });
      this.envEnable = o.enable;
    },
    //当创建及复制环境所选择的项目变化时，改变当前环境对应的projectId
    'environment.currentProjectId'() {
      // el-select什么都不选时值为''，为''的话也会被当成有效的projectId传给后端，转化使其无效
      if (!this.environment.currentProjectId) {
        this.environment.projectId = null;
      } else {
        this.environment.projectId = this.environment.currentProjectId;
      }
    }
  },
  computed: {
    hasLicense() {
      let license = hasLicense();
      return license;
    },
  },
  methods: {
    updateGlobalScript(isPreScript, filedName, value) {
      if (isPreScript) {
        if (filedName === "connScenario") {
          this.environment.config.globalScriptConfig.connScenarioPreScript = value;
        } else if (filedName === "execAfterPrivateScript") {
          this.environment.config.globalScriptConfig.isPreScriptExecAfterPrivateScript = value;
        } else if (filedName === "filterRequest") {
          this.environment.config.globalScriptConfig.filterRequestPreScript = value;
        }
      } else {
        if (filedName === "connScenario") {
          this.environment.config.globalScriptConfig.connScenarioPostScript = value;
        } else if (filedName === "execAfterPrivateScript") {
          this.environment.config.globalScriptConfig.isPostScriptExecAfterPrivateScript = value;
        } else if (filedName === "filterRequest") {
          this.environment.config.globalScriptConfig.filterRequestPostScript = value;
        }
      }
    },
    save() {
      this.$refs['environment'].validate((valid) => {
        if (valid && this.$refs.commonConfig.validate() && this.$refs.httpConfig.validate()) {
          if (this.$refs.commonConfig) {
            this.$refs.commonConfig.mergeData();
          }
          this._save(this.environment);
        }
      });
    },
    openHis(logType) {
      this.$refs.changeHistory.open(this.environment.id, ["项目-环境设置", "項目-環境設置", "Project environment setting"], logType);
    },
    validate() {
      let isValidate = false;
      this.$refs['environment'].validate((valid) => {
        if (valid && this.$refs.commonConfig.validate() && this.$refs.httpConfig.validate()) {
          isValidate = true;
        } else {
          isValidate = false;
        }
      });
      return isValidate;
    },
    geFiles(obj) {
      let uploadFiles = [];
      obj.uploadIds = [];
      if (obj.config && obj.config.sslConfig && obj.config.sslConfig.files) {
        obj.config.sslConfig.files.forEach(item => {
          if (item.file && item.file.size > 0) {
            if (!item.id) {
              item.name = item.file.name;
              item.id = getUUID();
            }
            obj.uploadIds.push(item.id);
            uploadFiles.push(item.file);
          }
        })
      }
      return uploadFiles;
    },
    getVariablesFiles(obj) {
      let variablesFiles = [];
      obj.variablesFilesIds = [];
      // 场景变量csv 文件
      if (obj.config.commonConfig.variables) {
        obj.config.commonConfig.variables.forEach(param => {
          if (param.type === 'CSV' && param.files) {
            param.files.forEach(item => {
              if (item.file && item.file.name) {
                if (!item.id) {
                  let fileId = getUUID().substring(0, 12);
                  item.name = item.file.name;
                  item.id = fileId;
                }
                obj.variablesFilesIds.push(item.id);
                variablesFiles.push(item.file);
              }
            })
          }
        });
      }
      return variablesFiles;
    },
    check(items) {
      let repeatKey = "";
      items.forEach((item, index) => {
        items.forEach((row, rowIndex) => {
          if (item.name === row.name && index !== rowIndex) {
            repeatKey = item.name;
          }
        });
      });
      return repeatKey;
    },
    _save(environment) {
      if (!environment.projectId) {
        this.$warning(this.$t('api_test.select_project'));
        return;
      }
      if (environment && environment.config && environment.config.commonConfig && environment.config.commonConfig.variables) {
        let repeatKey = this.check(environment.config.commonConfig && environment.config.commonConfig.variables);
        if (repeatKey !== "") {
          this.$warning(this.$t('api_test.environment.common_config') + "【" + repeatKey + "】" + this.$t('load_test.param_is_duplicate'));
          return;
        }
      }
      let message = '';
      if (environment && environment.config && environment.config.httpConfig && environment.config.httpConfig.conditions) {
        environment.config.httpConfig.conditions.forEach(env => {
          if (env.type === "MODULE" && env.details.length === 0) {
            message += this.$t('load_test.domain') + ":" + env.socket + ":" + this.$t('api_test.environment.module_warning');
            return;
          }
          if (env.type === "PATH" && env.details) {
            env.details.forEach(item => {
              if (!item.name) {
                message += this.$t('load_test.domain') + ":" + env.socket + ":" + this.$t('api_test.environment.path_warning');
                return;
              }
            })
          }
        })
      }
      environment.config.commonConfig.variables.forEach(variable => {
        delete variable.showMore;
        if (variable.type === 'CSV' && variable.files.length === 0) {
          message = this.$t('api_test.automation.csv_warning');
          return;
        }
      })
      if (message) {
        this.$warning(message);
        return;
      }

      let bodyFiles = this.geFiles(environment);
      let variablesFiles = this.getVariablesFiles(environment);
      let formData = new FormData();
      if (bodyFiles) {
        bodyFiles.forEach(f => {
          formData.append("files", f);
        })
      }
      if (variablesFiles) {
        variablesFiles.forEach(f => {
          formData.append("variablesFiles", f);
        })
      }
      let param = this.buildParam(environment);
      formData.append('request', new Blob([JSON.stringify(param)], {type: "application/json"}));
      editEnv(formData, param).then((response) => {
        this.$success(this.$t('commons.save_success'));
        this.$emit('refreshAfterSave');   //在EnvironmentList.vue中监听，使在数据修改后进行刷新
        this.cancel()
      }, error => {
        this.$emit('errorRefresh', error);
      });

    },
    buildParam: function (environment) {
      let param = {};
      Object.assign(param, environment);
      let hosts = param.config.commonConfig.hosts;
      if (hosts != undefined) {
        let validHosts = [];
        // 去除掉未确认的host
        hosts.forEach(host => {
          if (host.status === '') {
            validHosts.push(host);
          }
        });
        param.config.commonConfig.hosts = validHosts;
      }
      param.config.commonConfig.variables.forEach(variable => {
        delete variable.hidden;
      })
      param.config = JSON.stringify(param.config);
      return param;
    },
    cancel() {
      this.$emit('close');
    },
    confirm() {
      this.$emit("confirm");
    },
    clearValidate() {
      this.$refs["environment"].clearValidate();
    },
  },
}
</script>

<style scoped>

.ms-opt-btn {
  position: absolute;
  z-index: 10;
  margin-top: 28px;
}

span {
  display: block;
  margin-bottom: 15px;
}

span:not(:first-child) {
  margin-top: 15px;
}

.errorReportConfigSwitch :deep(.el-switch__label) {
  color: #D8DAE2;
}

.errorReportConfigSwitch :deep(.is-active) {
  color: var(--count_number);
}
</style>
