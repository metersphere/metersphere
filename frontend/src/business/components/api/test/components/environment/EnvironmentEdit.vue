<template>
  <el-main v-loading="result.loading" class="environment-edit" style="margin-left: 0px">
    <el-form :model="environment" :rules="rules" ref="environment">

      <span>{{$t('api_test.environment.name')}}</span>
      <el-form-item prop="name">
        <el-input v-model="environment.name" :disabled="isReadOnly" :placeholder="this.$t('commons.input_name')" clearable/>
      </el-form-item>


      <el-tabs v-model="activeName">

        <el-tab-pane :label="$t('api_test.environment.common_config')"  name="common">
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
        <el-tab-pane :label="$t('api_test.definition.request.all_pre_script')" name="prescript">
          <el-switch v-model="environment.config.preProcessor.connScenario" active-text="关联场景结果"
            style="margin: 5px 5px 5px 5px"/>
          <el-tooltip class="item" effect="dark" content="脚本步骤会统计到场景执行结果中，执行报错时会影响场景的最终执行结果" placement="right">
            <i class="el-icon-info"/>
          </el-tooltip>

          <jsr233-processor-content v-if="isRefresh"
                                    :jsr223-processor="environment.config.preProcessor"
                                    :is-pre-processor="true"
                                    :is-read-only="isReadOnly"/>
        </el-tab-pane>
        <el-tab-pane :label="$t('api_test.definition.request.all_post_script')" name="postscript">
          <el-switch v-model="environment.config.postProcessor.connScenario" active-text="关联场景结果"
                     style="margin: 5px 5px 5px 5px"/>
          <el-tooltip class="item" effect="dark" content="脚本步骤会统计到场景执行结果中，执行报错时会影响场景的最终执行结果" placement="right">
            <i class="el-icon-info"/>
          </el-tooltip>

          <jsr233-processor-content  v-if="isRefresh"
                                     :jsr223-processor="environment.config.postProcessor"
            :is-pre-processor="false"
            :is-read-only="false"/>
        </el-tab-pane>
        <!-- 认证配置 -->
        <el-tab-pane :label="$t('api_test.definition.request.all_auth_config')" name="authConfig" v-if="isRefresh">
          <el-tooltip class="item-tabs" effect="dark" :content="$t('api_test.definition.request.auth_config_info')" placement="top-start" slot="label">
            <span>{{$t('api_test.definition.request.all_auth_config')}}</span>
          </el-tooltip>
          <ms-api-auth-config :is-read-only="isReadOnly" :request="environment.config.authManager"/>
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
  import MsApiAuthConfig from "@/business/components/api/definition/components/auth/ApiAuthConfig";

  import MsTcpConfig from "@/business/components/api/test/components/request/tcp/TcpConfig";
  import {getUUID} from "@/common/js/utils";
  import Jsr233ProcessorContent from "@/business/components/api/automation/scenario/common/Jsr233ProcessorContent";
  import {createComponent} from "@/business/components/api/definition/components/jmeter/components";

  export default {
    name: "EnvironmentEdit",
    components: {
      MsTcpConfig,
      MsApiAuthConfig,
      Jsr233ProcessorContent,
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
      if(!this.environment.config.preProcessor){
        this.environment.config.preProcessor = createComponent("JSR223PreProcessor");
      }
      if(!this.environment.config.preProcessor.connScenario){
        this.environment.config.preProcessor.connScenario = false;
      }
      if(!this.environment.config.postProcessor){
        this.environment.config.postProcessor = createComponent("JSR223PostProcessor");
      }
      if(!this.environment.config.postProcessor.connScenario){
        this.environment.config.postProcessor.connScenario = false;
      }
      if(!this.environment.config.authManager){
        this.environment.config.authManager = {'hashTree':[]};
      }
      if(!this.environment.config.authManager.hashTree){
        this.environment.config.authManager.hashTree = [];
      }

    },

    watch: {
      environment: function (o) {
        if(!this.environment.config.preProcessor){
          this.environment.config.preProcessor = createComponent("JSR223PreProcessor");
          if(!this.environment.config.preProcessor.script){
            this.environment.config.preProcessor.script = "";
          }
        }
        if(!this.environment.config.postProcessor){
          this.environment.config.postProcessor = createComponent("JSR223PostProcessor");
          if(!this.environment.config.postProcessor.script){
            this.environment.config.postProcessor.script = "";
          }
        }
        if(!this.environment.config.preProcessor.connScenario){
          this.environment.config.preProcessor.connScenario = false;
        }
        if(!this.environment.config.postProcessor.connScenario){
          this.environment.config.postProcessor.connScenario = false;
        }

        if(!this.environment.config.authManager){
          this.environment.config.authManager = {'hashTree':[]};
        }
        if(!this.environment.config.authManager.hashTree){
          this.environment.config.authManager.hashTree = [];
        }

        this.isRefresh = false;
        this.$nextTick(() => {
          this.isRefresh = true;
        });
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
