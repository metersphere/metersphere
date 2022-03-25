<template>
  <el-main v-loading="result.loading" class="environment-edit" style="margin-left: 0px">
    <el-form :model="environment" :rules="rules" ref="environment" label-width="80px">
      <el-form-item prop="name" :label="$t('api_test.environment.name')">
        <el-input v-model="environment.name" :disabled="isReadOnly" :placeholder="this.$t('commons.input_name')"
                  clearable/>
      </el-form-item>


      <el-tabs v-model="activeName">

        <el-tab-pane :label="$t('api_test.environment.common_config')" name="common">
          <ms-environment-common-config :common-config="environment.config.commonConfig" ref="commonConfig"
                                        :is-read-only="isReadOnly"/>
        </el-tab-pane>

        <el-tab-pane :label="$t('api_test.environment.http_config')" name="http">
          <ms-environment-http-config :project-id="projectId" :http-config="environment.config.httpConfig"
                                      ref="httpConfig" :is-read-only="isReadOnly"/>
        </el-tab-pane>
        <el-tab-pane :label="$t('api_test.environment.database_config')" name="sql">
          <ms-database-config :configs="environment.config.databaseConfigs" :is-read-only="isReadOnly"/>
        </el-tab-pane>
        <el-tab-pane :label="$t('api_test.environment.tcp_config')" name="tcp">
          <ms-tcp-config :config="environment.config.tcpConfig" :is-read-only="isReadOnly"/>
        </el-tab-pane>
        <el-tab-pane :label="$t('commons.ssl.config')" name="ssl">
          <ms-environment-s-s-l-config :project-id="projectId" :ssl-config="environment.config.sslConfig"
                                       :is-read-only="isReadOnly"/>
        </el-tab-pane>
        <el-tab-pane :label="$t('api_test.definition.request.all_pre_script')" name="prescript">
          <div style="padding-bottom: 20px;" v-if="!ifCreate">
            <el-link style="float: right;" type="primary" @click="openHis">
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
            <el-link style="float: right;" type="primary" @click="openHis">
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
        <!--        全局断言-->
        <el-tab-pane :label="$t('env_options.all_assertions')" name="assertions">
          <el-tooltip class="item-tabs" effect="dark" :content="$t('env_options.all_assertions')"
                      placement="top-start" slot="label">
            <span>{{ $t('env_options.all_assertions') }}</span>
          </el-tooltip>
          <el-row type="flex" :gutter="20" v-if="hasLicense">
            <el-col :span="12">
              <el-form-item
                :label="$t('error_report_library.use_error_report')"
                prop="status">
                <el-switch v-model="environment.config.useErrorCode" style="margin-right: 10px" :disabled="isReadOnly"/>
                {{$t('error_report_library.use_desc')}}
              </el-form-item>
            </el-col>
          </el-row>
          <global-assertions :is-read-only="isReadOnly" :assertions="environment.config.assertions" :is-show-json-path-suggest="false"/>
        </el-tab-pane>
      </el-tabs>
<!--      <div class="environment-footer">-->
<!--        <ms-dialog-footer-->
<!--          @cancel="cancel"-->
<!--          @confirm="save()"/>-->
<!--      </div>-->
    </el-form>
    <ms-change-history ref="changeHistory"/>
  </el-main>
</template>

<script>
import MsApiScenarioVariables from "../ApiScenarioVariables";
import MsApiKeyValue from "../ApiKeyValue";
import MsDialogFooter from "../../../../common/components/MsDialogFooter";
import {REQUEST_HEADERS} from "@/common/js/constants";
import {Environment} from "../../model/EnvironmentModel";
import MsApiHostTable from "../ApiHostTable";
import MsDatabaseConfig from "../request/database/DatabaseConfig";
import MsEnvironmentHttpConfig from "./EnvironmentHttpConfig";
import MsEnvironmentCommonConfig from "./EnvironmentCommonConfig";
import MsEnvironmentSSLConfig from "./EnvironmentSSLConfig";
import MsApiAuthConfig from "@/business/components/api/definition/components/auth/ApiAuthConfig";
import MsTcpConfig from "@/business/components/api/test/components/request/tcp/TcpConfig";
import {getUUID,hasLicense} from "@/common/js/utils";
import Jsr233ProcessorContent from "@/business/components/api/automation/scenario/common/Jsr233ProcessorContent";
import {createComponent} from "@/business/components/api/definition/components/jmeter/components";
import EnvironmentGlobalScript from "@/business/components/api/test/components/environment/EnvironmentGlobalScript";
import GlobalAssertions from "@/business/components/api/definition/components/assertion/GlobalAssertions";
import MsChangeHistory from "../../../../history/ChangeHistory";
import MsDialogHeader from "../../../../common/components/MsDialogHeader";

export default {
  name: "EnvironmentEdit",
  components: {
    MsTcpConfig,
    GlobalAssertions,
    MsApiAuthConfig,
    Jsr233ProcessorContent,
    MsEnvironmentCommonConfig,
    MsEnvironmentHttpConfig,
    MsEnvironmentSSLConfig,
    EnvironmentGlobalScript,
    MsDatabaseConfig, MsApiHostTable, MsDialogFooter, MsApiKeyValue, MsApiScenarioVariables, MsChangeHistory,
    MsDialogHeader
  },
  props: {
    environment: new Environment(),
    projectId: String,
    isReadOnly: {
      type: Boolean,
      default: false
    },
    ifCreate: {
      type: Boolean,
      default: false
    },
  },
  data() {
    return {
      result: {},
      envEnable: false,
      isRefresh: true,
      rules: {
        name: [
          {required: true, message: this.$t('commons.input_name'), trigger: 'blur'},
          {max: 64, message: this.$t('commons.input_limit', [1, 64]), trigger: 'blur'}
        ],
      },
      headerSuggestions: REQUEST_HEADERS,
      activeName: 'common'
    }
  },
  created() {
    if (!this.environment.config.preProcessor) {
      this.environment.config.preProcessor = createComponent("JSR223PreProcessor");
    }
    if (!this.environment.config.postProcessor) {
      this.environment.config.postProcessor = createComponent("JSR223PostProcessor");
    }
    if (!this.environment.config.preStepProcessor) {
      this.environment.config.preStepProcessor = createComponent("JSR223PreProcessor");
    }
    if (!this.environment.config.postStepProcessor) {
      this.environment.config.postStepProcessor = createComponent("JSR223PostProcessor");
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
      this.environment.config.assertions = {
        duration:{duration:0},
        regex:[],
        jsonPath:[],
        xpath2:[],
        jsr223:[],
        document:{type:"json",data:{json:[],xml:[]}},
      };
    }
  },

  watch: {
    environment: function (o) {
      if (!this.environment.config.preProcessor) {
        this.environment.config.preProcessor = createComponent("JSR223PreProcessor");
        if (!this.environment.config.preProcessor.script) {
          this.environment.config.preProcessor.script = "";
        }
      }
      if (!this.environment.config.postProcessor) {
        this.environment.config.postProcessor = createComponent("JSR223PostProcessor");
        if (!this.environment.config.postProcessor.script) {
          this.environment.config.postProcessor.script = "";
        }
      }
      if (!this.environment.config.preStepProcessor) {
        this.environment.config.preStepProcessor = createComponent("JSR223PreProcessor");
      }
      if (!this.environment.config.postStepProcessor) {
        this.environment.config.postStepProcessor = createComponent("JSR223PostProcessor");
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
        this.environment.config.assertions = {
          duration:{duration:0},
          regex:[],
          jsonPath:[],
          xpath2:[],
          jsr223:[],
          document:{type:"json",data:{json:[],xml:[]}},
        };
      }

      this.isRefresh = false;
      this.$nextTick(() => {
        this.isRefresh = true;
      });
      this.envEnable = o.enable;
    }
  },
  computed: {
    hasLicense(){
      let license= hasLicense();
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
          this._save(this.environment);
        }
      });
    },
    openHis() {
      this.$refs.changeHistory.open(this.environment.id, ["项目-环境设置", "項目-環境設置", "Project environment setting"]);
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
      if (!this.projectId) {
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
      let bodyFiles = this.geFiles(environment);
      let param = this.buildParam(environment);
      let url = '/api/environment/add';
      if (param.id) {
        url = '/api/environment/update';
      }
      this.$fileUpload(url, null, bodyFiles, param, response => {
        //this.result = this.$post(url, param, response => {
        if (!param.id) {
          environment.id = response.data;
        }
        this.$success(this.$t('commons.save_success'));
        this.$emit('refreshAfterSave');   //在EnvironmentList.vue中监听，使在数据修改后进行刷新
        this.cancel();
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
      param.config = JSON.stringify(param.config);
      return param;
    },
    cancel() {
      this.$emit('close');
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

</style>
