<template>
  <el-main v-loading="result.loading" class="environment-edit" style="margin-left: 0px">
    <el-form :model="environment" :rules="rules" ref="environment">

      <span>{{$t('api_test.environment.name')}}</span>
      <el-form-item prop="name">
        <el-input v-model="environment.name" :disabled="isReadOnly" :placeholder="this.$t('commons.input_name')" clearable/>
      </el-form-item>


      <el-tabs v-model="activeName">

        <el-tab-pane :label="$t('api_test.environment.common_config')" name="common">
          <ms-environment-common-config :common-config="environment.config.commonConfig" ref="commonConfig" :is-read-only="isReadOnly"/>
        </el-tab-pane>

        <el-tab-pane :label="$t('api_test.environment.http_config')" name="http">
          <ms-environment-http-config :project-id="projectId" :http-config="environment.config.httpConfig" ref="httpConfig" :is-read-only="isReadOnly"/>
        </el-tab-pane>
        <el-tab-pane :label="$t('api_test.environment.database_config')" name="sql">
          <ms-database-config :configs="environment.config.databaseConfigs" :is-read-only="isReadOnly"/>
        </el-tab-pane>
        <el-tab-pane :label="$t('api_test.environment.tcp_config')" name="tcp">
          <ms-tcp-config :config="environment.config.tcpConfig" :is-read-only="isReadOnly"/>
        </el-tab-pane>
        <el-tab-pane :label="$t('commons.ssl.config')" name="ssl">
          <ms-environment-s-s-l-config :project-id="projectId" :ssl-config="environment.config.sslConfig" :is-read-only="isReadOnly"/>
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
  import MsApiHostTable from "../ApiHostTable";
  import MsDatabaseConfig from "../request/database/DatabaseConfig";
  import MsEnvironmentHttpConfig from "./EnvironmentHttpConfig";
  import MsEnvironmentCommonConfig from "./EnvironmentCommonConfig";
  import MsEnvironmentSSLConfig from "./EnvironmentSSLConfig";

  import MsTcpConfig from "@/business/components/api/test/components/request/tcp/TcpConfig";
  import {getUUID} from "@/common/js/utils";

  export default {
    name: "EnvironmentEdit",
    components: {
      MsTcpConfig,
      MsEnvironmentCommonConfig,
      MsEnvironmentHttpConfig,
      MsEnvironmentSSLConfig,
      MsDatabaseConfig, MsApiHostTable, MsDialogFooter, MsApiKeyValue, MsApiScenarioVariables
    },
    props: {
      environment: new Environment(),
      projectId: String,
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
      _save(environment) {
        if(!this.projectId){
          this.$warning(this.$t('api_test.select_project'));
          return;
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
