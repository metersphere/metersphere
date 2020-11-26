<template>
  <div>
    <el-row>
      <el-col :span="21" style="padding-bottom: 20px">
        <div style="border:1px #DCDFE6 solid; height: 100%;border-radius: 4px ;width: 100% ;margin: 20px">
          <el-form :model="request" :rules="rules" ref="request" label-width="100px" :disabled="isReadOnly" style="margin: 20px">

            <el-row>
              <el-col :span="8">
                <el-form-item prop="environmentId" :label="$t('api_test.definition.request.run_env')">
                  <el-select v-model="request.environmentId" size="small" class="ms-htt-width"
                             :placeholder="$t('api_test.definition.request.run_env')"
                             @change="environmentChange" clearable>
                    <el-option v-for="(environment, index) in environments" :key="index"
                               :label="environment.name + (environment.config.httpConfig.socket ? (': ' + environment.config.httpConfig.protocol + '://' + environment.config.httpConfig.socket) : '')"
                               :value="environment.id"/>
                    <el-button class="environment-button" size="mini" type="primary" @click="openEnvironmentConfig">
                      {{ $t('api_test.environment.environment_config') }}
                    </el-button>
                    <template v-slot:empty>
                      <div class="empty-environment">
                        <el-button class="environment-button" size="mini" type="primary" @click="openEnvironmentConfig">
                          {{ $t('api_test.environment.environment_config') }}
                        </el-button>
                      </div>
                    </template>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item :label="$t('api_test.request.sql.dataSource')" prop="dataSource" style="margin-left: 10px">
                  <el-select v-model="request.dataSource" size="small">
                    <el-option v-for="(item, index) in databaseConfigsOptions" :key="index" :value="item" :label="item.name"/>
                  </el-select>
                </el-form-item>

              </el-col>
              <el-col :span="8">
                <el-form-item :label="$t('api_test.request.sql.timeout')" prop="queryTimeout" style="margin-left: 10px">
                  <el-input-number :disabled="isReadOnly" size="small" v-model="request.queryTimeout" :placeholder="$t('commons.millisecond')" :max="1000*10000000" :min="0"/>
                </el-form-item>
              </el-col>
            </el-row>


            <el-form-item :label="$t('api_test.request.sql.result_variable')" prop="resultVariable">
              <el-input v-model="request.resultVariable" maxlength="300" show-word-limit/>
            </el-form-item>

            <el-form-item :label="$t('api_test.request.sql.variable_names')" prop="variableNames">
              <el-input v-model="request.variableNames" maxlength="300" show-word-limit/>
            </el-form-item>

            <el-tabs v-model="activeName">
              <el-tab-pane :label="$t('api_test.scenario.variables')" name="variables">
                <ms-api-scenario-variables :is-read-only="isReadOnly" :items="request.variables"
                                           :description="$t('api_test.scenario.kv_description')"/>
              </el-tab-pane>
              <el-tab-pane :label="$t('api_test.request.sql.sql_script')" name="sql">
                <div class="sql-content">
                  <ms-code-edit mode="sql" :read-only="isReadOnly" :modes="['sql']" :data.sync="request.query" theme="eclipse" ref="codeEdit"/>
                </div>
              </el-tab-pane>
            </el-tabs>
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

    <!-- 环境 -->
    <api-environment-config ref="environmentConfig" @close="environmentConfigClose"/>
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
        environments: [],
        databaseConfigsOptions: [],
        isReloadData: false,
        activeName: "variables",
        rules: {
          environmentId: [{required: true, message: this.$t('test_track.case.input_maintainer'), trigger: 'change'}],
          dataSource: [{required: true, message: this.$t('api_test.request.sql.dataSource'), trigger: 'change'}],
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
      saveApi() {
        this.basisData.method = this.basisData.protocol;
        this.$emit('saveApi', this.basisData);
      },
      runTest() {

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
  .sql-content {
    height: calc(100vh - 570px);
  }

  .one-row .el-form-item {
    display: inline-block;
  }

  .one-row .el-form-item:nth-child(2) {
    margin-left: 60px;
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

  .tip {
    padding: 3px 5px;
    font-size: 16px;
    border-radius: 4px;
    border-left: 4px solid #783887;
    margin: 0px 20px 0px;
  }
</style>
