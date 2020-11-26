<template>
  <div>
    <el-row>
      <el-col :span="21" style="padding-bottom: 20px">
        <div style="border:1px #DCDFE6 solid; height: 100%;border-radius: 4px ;width: 100% ;margin: 20px">
          <el-form class="tcp" :model="request" :rules="rules" ref="request" label-width="auto" :disabled="isReadOnly" style="margin: 20px">

            <el-row :gutter="10">
              <el-col :span="9">
                <el-form-item label="TCPClient" prop="classname">
                  <el-select v-model="request.classname" style="width: 100%" size="small">
                    <el-option v-for="c in classes" :key="c" :label="c" :value="c"/>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="9">
                <el-form-item :label="$t('api_test.request.tcp.server')" prop="server">
                  <el-input v-model="request.server" maxlength="300" show-word-limit size="small"/>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item :label="$t('api_test.request.tcp.port')" prop="port" label-width="60px">
                  <el-input-number v-model="request.port" controls-position="right" :min="0" :max="65535" size="small"/>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="10">
              <el-col :span="9">
                <el-form-item :label="$t('api_test.request.tcp.connect')" prop="ctimeout">
                  <el-input-number v-model="request.ctimeout" controls-position="right" :min="0" size="small"/>
                </el-form-item>
              </el-col>
              <el-col :span="9">
                <el-form-item :label="$t('api_test.request.tcp.response')" prop="timeout">
                  <el-input-number v-model="request.timeout" controls-position="right" :min="0" size="small"/>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item :label="$t('api_test.request.tcp.so_linger')" prop="soLinger">
                  <el-input v-model="request.soLinger" size="small"/>
                </el-form-item>
              </el-col>
            </el-row>

            <el-row :gutter="10">
              <el-col :span="9">
                <el-form-item :label="$t('api_test.request.tcp.username')" prop="username">
                  <el-input v-model="request.username" maxlength="100" show-word-limit size="small"/>
                </el-form-item>
              </el-col>
              <el-col :span="9">
                <el-form-item :label="$t('api_test.request.tcp.password')" prop="password">
                  <el-input v-model="request.password" maxlength="30" show-word-limit show-password
                            autocomplete="new-password" size="small"/>
                </el-form-item>
              </el-col>

              <el-col :span="6">
                <el-form-item :label="$t('api_test.request.tcp.eol_byte')" prop="eolByte">
                  <el-input v-model="request.eolByte" size="small"/>
                </el-form-item>
              </el-col>

            </el-row>

            <el-row :gutter="10" style="margin-left: 30px">
              <el-col :span="9">
                <el-form-item :label="$t('api_test.request.tcp.re_use_connection')">
                  <el-checkbox v-model="request.reUseConnection"/>
                </el-form-item>
              </el-col>
              <el-col :span="9">
                <el-form-item :label="$t('api_test.request.tcp.close_connection')">
                  <el-checkbox v-model="request.closeConnection"/>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item :label="$t('api_test.request.tcp.no_delay')">
                  <el-checkbox v-model="request.nodelay"/>
                </el-form-item>
              </el-col>
            </el-row>

            <el-form-item :label="$t('api_test.request.tcp.request')" prop="request">
              <div class="send-request">
                <ms-code-edit mode="text" :read-only="isReadOnly" :data.sync="request.request"
                              :modes="['text', 'json', 'xml', 'html']" theme="eclipse"/>
              </div>
            </el-form-item>

          </el-form>
        </div>
        <div v-for="row in request.hashTree" :key="row.id" v-loading="isReloadData" style="margin-left: 20px;width: 100%">
          <!-- 前置脚本 -->
          <ms-jsr233-processor v-if="row.label ==='JSR223 PreProcessor'" @remove="remove" :is-read-only="false" :title="$t('api_test.definition.request.pre_script')" style-type="warning"
                               :jsr223-processor="row"/>
          <!--后置脚本-->
          <ms-jsr233-processor v-if="row.label ==='JSR223 PostProcessor'" @remove="remove" :is-read-only="false" :title="$t('api_test.definition.request.post_script')" style-type="success"
                               :jsr223-processor="row"/>
          <!--断言规则-->
          <ms-api-assertions v-if="row.type==='Assertions'" @remove="remove" :is-read-only="isReadOnly" :assertions="row"/>
          <!--提取规则-->
          <ms-api-extract :is-read-only="isReadOnly" @remove="remove" v-if="row.type==='Extract'" :extract="row"/>

        </div>
      </el-col>

      <el-col :span="3" class="ms-left-cell">

        <el-button class="ms-left-buttion" size="small" type="warning" @click="addPre" plain>+{{$t('api_test.definition.request.pre_script')}}</el-button>
        <br/>
        <el-button class="ms-left-buttion" size="small" type="success" @click="addPost" plain>+{{$t('api_test.definition.request.post_script')}}</el-button>
        <br/>
        <el-button class="ms-left-buttion" size="small" type="danger" @click="addAssertions" plain>+{{$t('api_test.definition.request.assertions_rule')}}</el-button>
        <br/>
        <el-button class="ms-left-buttion" size="small" type="info" @click="addExtract" plain>+{{$t('api_test.definition.request.extract_param')}}</el-button>
      </el-col>
    </el-row>
  </div>
</template>

<script>
  import MsApiKeyValue from "../../ApiKeyValue";
  import MsApiAssertions from "../../assertion/ApiAssertions";
  import MsApiExtract from "../../extract/ApiExtract";
  import ApiRequestMethodSelect from "../../collapse/ApiRequestMethodSelect";
  import MsJsr233Processor from "../../processor/Jsr233Processor";
  import MsCodeEdit from "../../../../../common/components/MsCodeEdit";
  import MsApiScenarioVariables from "../../ApiScenarioVariables";
  import {createComponent} from "../../jmeter/components";
  import {Assertions, Extract} from "../../../model/ApiTestModel";
  import {parseEnvironment} from "../../../model/EnvironmentModel";
  import ApiEnvironmentConfig from "../../environment/ApiEnvironmentConfig";
  import {API_STATUS} from "../../../model/JsonData";
  import TCPSampler from "../../jmeter/components/sampler/tcp-sampler";

  export default {
    name: "MsDatabaseConfig",
    components: {
      MsApiScenarioVariables,
      MsCodeEdit,
      MsJsr233Processor, ApiRequestMethodSelect, MsApiExtract, MsApiAssertions, MsApiKeyValue, ApiEnvironmentConfig
    },
    props: {
      request: {},
      basisData: {},
      currentProject: {},
      moduleOptions: Array,
      isReadOnly: {
        type: Boolean,
        default: false
      },
    },
    data() {
      return {
        activeName: "assertions",
        classes: TCPSampler.CLASSES,
        isReloadData: false,
        options: API_STATUS,
        rules: {
          classname: [{required: true, message: "请选择TCPClient", trigger: 'change'}],
          server: [{required: true, message: this.$t('api_test.request.tcp.server_cannot_be_empty'), trigger: 'blur'}],
          port: [{required: true, message: this.$t('commons.port_cannot_be_empty'), trigger: 'change'}],
        },
      }
    },
    created() {
      this.getEnvironments();
    },
    methods: {
      addPre() {
        let jsr223PreProcessor = createComponent("JSR223PreProcessor");
        this.request.hashTree.push(jsr223PreProcessor);
        this.reload();
      },
      addPost() {
        let jsr223PostProcessor = createComponent("JSR223PostProcessor");
        this.request.hashTree.push(jsr223PostProcessor);
        this.reload();
      },
      addAssertions() {
        let assertions = new Assertions();
        this.request.hashTree.push(assertions);
        this.reload();
      },
      addExtract() {
        let jsonPostProcessor = new Extract();
        this.request.hashTree.push(jsonPostProcessor);
        this.reload();
      },
      remove(row) {
        let index = this.request.hashTree.indexOf(row);
        this.request.hashTree.splice(index, 1);
        this.reload();
      },
      reload() {
        this.isReloadData = true
        this.$nextTick(() => {
          this.isReloadData = false
        })
      },
      validateApi() {
        if (this.currentProject === null) {
          this.$error(this.$t('api_test.select_project'), 2000);
          return;
        }
        this.$refs['basicForm'].validate();
      },
      saveApi() {
        this.basisData.method = this.basisData.protocol;
        this.$emit('saveApi', this.basisData);
      },
      runTest() {

      },
      validate() {
        if (this.currentProject === null) {
          this.$error(this.$t('api_test.select_project'), 2000);
          return;
        }
        this.$refs['request'].validate((valid) => {
          if (valid) {
            this.$emit('callback');
          }
        })
      },
      getEnvironments() {
        if (this.currentProject) {
          this.environments = [];
          this.$get('/api/environment/list/' + this.currentProject.id, response => {
            this.environments = response.data;
            this.environments.forEach(environment => {
              parseEnvironment(environment);
            });
            this.initDataSource();
          });
        }
      },
      openEnvironmentConfig() {
        if (!this.currentProject) {
          this.$error(this.$t('api_test.select_project'));
          return;
        }
        this.$refs.environmentConfig.open(this.currentProject.id);
      },
      initDataSource() {
        for (let i in this.environments) {
          if (this.environments[i].id === this.request.environmentId) {
            this.databaseConfigsOptions = [];
            this.environments[i].config.databaseConfigs.forEach(item => {
              this.databaseConfigsOptions.push(item);
            })
            break;
          }
        }
      },
      environmentChange(value) {
        this.request.dataSource = undefined;
        for (let i in this.environments) {
          if (this.environments[i].id === value) {
            this.databaseConfigsOptions = [];
            this.environments[i].config.databaseConfigs.forEach(item => {
              this.databaseConfigsOptions.push(item);
            })
            break;
          }
        }
      },
      environmentConfigClose() {
        this.getEnvironments();
      },
    }

  }
</script>

<style scoped>
  .tcp >>> .el-input-number {
    width: 100%;
  }

  .send-request {
    padding: 0px 0;
    height: 300px;
    border: 1px #DCDFE6 solid;
    border-radius: 4px;
    width: 100%;
  }

  .ms-left-cell {
    margin-top: 40px;
  }

  .ms-left-buttion {
    margin: 6px 0px 8px 30px;
  }

  /deep/ .el-form-item {
    margin-bottom: 15px;
  }

  .ms-left-cell {
    margin-top: 40px;
  }

  .ms-left-buttion {
    margin: 6px 0px 8px 30px;
  }

  /deep/ .el-form-item {
    margin-bottom: 15px;
  }
</style>
