<template>
  <el-main v-loading="result.loading">
    <el-form :model="environment" :rules="rules" ref="environment">

      <span>{{ $t('api_test.environment.name') }}</span>
      <el-form-item prop="name">
        <el-input v-model="environment.name" :disabled="isReadOnly" :placeholder="this.$t('commons.input_name')"
                  clearable/>
      </el-form-item>


      <el-tabs v-model="activeName">
        <el-tab-pane :label="$t('api_test.environment.common_config')" name="common">
          <ms-environment-common-config :common-config="environment.config.commonConfig" ref="commonConfig"
                                        :is-read-only="isReadOnly"/>
        </el-tab-pane>

        <el-tab-pane :label="$t('api_test.environment.http_config')" name="http">
          <ms-environment-http-config :http-config="environment.config.httpConfig" ref="httpConfig"
                                      :is-read-only="isReadOnly"/>
        </el-tab-pane>
        <el-tab-pane :label="$t('api_test.environment.database_config')" name="sql">
          <ms-database-config :configs="environment.config.databaseConfigs" :is-read-only="isReadOnly"/>
        </el-tab-pane>
        <el-tab-pane :label="$t('api_test.environment.tcp_config')" name="tcp">
          <environment-tcp-config :config="environment.config.tcpConfig" :is-read-only="isReadOnly"/>
        </el-tab-pane>
      </el-tabs>

      <div class="environment-footer">
        <ms-dialog-footer
          @cancel="cancel"
          @confirm="save()"/>
      </div>
    </el-form>
  </el-main>
</template>

<script>
import MsApiScenarioVariables from "../ApiScenarioVariables";
import MsApiKeyValue from "../ApiKeyValue";
import MsDialogFooter from "../../../../common/components/MsDialogFooter";
import {REQUEST_HEADERS} from "@/common/js/constants";
import {Environment} from "../../model/EnvironmentModel";
import MsApiHostTable from "./ApiHostTable";
import MsDatabaseConfig from "../request/database/DatabaseConfig";
import MsEnvironmentHttpConfig from "../../../test/components/environment/EnvironmentHttpConfig";
import MsEnvironmentCommonConfig from "./EnvironmentCommonConfig";
import EnvironmentTcpConfig from "./EnvironmentTcpConfig";
import {getUploadConfig, request} from "@/common/js/ajax";
import {getUUID} from "@/common/js/utils";

export default {
  name: "EnvironmentEdit",
  components: {
    EnvironmentTcpConfig,
    MsEnvironmentCommonConfig,
    MsEnvironmentHttpConfig,
    MsDatabaseConfig, MsApiHostTable, MsDialogFooter, MsApiKeyValue, MsApiScenarioVariables
  },
  props: {
    environment: new Environment(),
    isReadOnly: {
      type: Boolean,
      default: false
    },
  },
  data() {

    return {
      result: {},
      envEnable: false,
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
  watch: {
    environment: function (o) {
      this.envEnable = o.enable;
    }
  },
  methods: {
    save() {
      this.$refs['environment'].validate((valid) => {
        if (valid && this.$refs.commonConfig.validate() && this.$refs.httpConfig.validate()) {
          this._save(this.environment);
        }
      });
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
      let url = '/api/environment/add';
      if (param.id) {
        url = '/api/environment/update';
      }
      formData.append('request', new Blob([JSON.stringify(param)], {type: "application/json"}));
      let axiosRequestConfig = getUploadConfig(url, formData);
      request(axiosRequestConfig, (response) => {
        if (response.success) {
          this.$success(this.$t('commons.save_success'));
          this.$emit('refreshAfterSave');   //在EnvironmentList.vue中监听，使在数据修改后进行刷新
          this.cancel()
        }
      }, error => {
        this.$emit('errorRefresh', error);
      });

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

.el-main {
  border: solid 1px #EBEEF5;
  margin-left: 200px;
  min-height: 400px;
  max-height: 700px;

}

.el-row {
  margin-bottom: 15px;
}

.environment-footer {
  margin-top: 15px;
  float: right;
}

span {
  display: block;
  margin-bottom: 15px;
}

span:not(:first-child) {
  margin-top: 15px;
}

</style>
