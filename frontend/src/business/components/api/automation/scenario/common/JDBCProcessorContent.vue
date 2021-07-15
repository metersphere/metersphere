<template>
  <div v-loading="isReloadData">
    <el-row>
      <el-col :span="21" style="padding-bottom: 20px">
        <div style="border:1px #DCDFE6 solid; height: 100%;border-radius: 4px ;width: 100% ;margin: 20px">
          <el-form :model="request" :rules="rules" ref="request" label-width="100px" :disabled="isReadOnly" style="margin: 10px">
            <el-row>
              <el-col :span="8">
                <el-form-item prop="environmentId" :label="$t('api_test.definition.request.run_env')">
                  <el-select v-model="request.environmentId" size="small" class="ms-htt-width"
                             :placeholder="$t('api_test.definition.request.run_env')"
                             @change="environmentChange" clearable>
                    <el-option v-for="(environment, index) in environments" :key="index"
                               :label="environment.name"
                               :value="environment.id"/>
                    <el-button class="environment-button" size="small" type="primary" @click="openEnvironmentConfig">
                      {{ $t('api_test.environment.environment_config') }}
                    </el-button>
                    <template v-slot:empty>
                      <div class="empty-environment">
                        <el-button class="environment-button" size="small" type="primary" @click="openEnvironmentConfig">
                          {{ $t('api_test.environment.environment_config') }}
                        </el-button>
                      </div>
                    </template>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="8">
                <el-form-item :label="$t('api_test.request.sql.dataSource')" prop="dataSourceId" style="margin-left: 10px">
                  <el-select v-model="request.dataSourceId" size="small" @change="reload">
                    <el-option v-for="(item, index) in databaseConfigsOptions" :key="index" :value="item.id" :label="item.name"/>
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
              <el-input v-model="request.resultVariable" maxlength="500" show-word-limit size="small"/>
            </el-form-item>

            <el-form-item :label="$t('api_test.request.sql.variable_names')" prop="variableNames">
              <el-input v-model="request.variableNames" maxlength="500" show-word-limit size="small"/>
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
      </el-col>
    </el-row>

    <!-- 环境 -->
    <api-environment-config ref="environmentConfig" @close="environmentConfigClose"/>
  </div>
</template>

<script>
import MsApiKeyValue from "@/business/components/api/definition/components/ApiKeyValue";
import ApiAssertions from "@/business/components/api/definition/components/assertion/ApiAssertions";
import MsApiExtract from "@/business/components/api/definition/components/extract/ApiExtract";
import ApiRequestMethodSelect from "@/business/components/api/definition/components/collapse/ApiRequestMethodSelect";
import MsCodeEdit from "@/business/components/common/components/MsCodeEdit";
import MsApiScenarioVariables from "@/business/components/api/definition/components/ApiScenarioVariables";
import {createComponent} from "@/business/components/api/definition/components/jmeter/components";
import {Assertions, Extract} from "@/business/components/api/definition/model/ApiTestModel";
import {parseEnvironment} from "@/business/components/api/definition/model/EnvironmentModel";
import ApiEnvironmentConfig from "@/business/components/api/test/components/ApiEnvironmentConfig";
import {getCurrentProjectID} from "@/common/js/utils";
import {getUUID} from "@/common/js/utils";
import MsJsr233Processor from "@/business/components/api/automation/scenario/component/Jsr233Processor";

export default {
  name: "JdbcProcessorContent",
  components: {
    MsJsr233Processor,
    MsApiScenarioVariables,
    MsCodeEdit,
    ApiRequestMethodSelect, MsApiExtract, MsApiKeyValue, ApiEnvironmentConfig
  },
  props: {
    request: {},
    basisData: {},
    moduleOptions: Array,
    showScript: {
      type: Boolean,
      default: true,
    },
    isReadOnly: {
      type: Boolean,
      default: false
    },
  },
  data() {
    return {
      environments: [],
      currentEnvironment: {},
      databaseConfigsOptions: [],
      isReloadData: false,
      activeName: "variables",
      rules: {},
    }
  },
  watch: {
    'request.dataSourceId'() {
      this.setDataSource();
    },
  },
  created() {
    this.getEnvironments();
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
  },
  methods: {
    remove(row) {
      let index = this.request.hashTree.indexOf(row);
      this.request.hashTree.splice(index, 1);
      this.reload();
    },
    copyRow(row) {
      let obj = JSON.parse(JSON.stringify(row));
      obj.id = getUUID();
      this.request.hashTree.push(obj);
      this.reload();
    },
    reload() {
      this.isReloadData = true
      this.$nextTick(() => {
        this.isReloadData = false
      })
    },
    validate() {
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
      this.environments = [];
      let id = this.request.projectId ? this.request.projectId : this.projectId;
      this.$get('/api/environment/list/' + id, response => {
        this.environments = response.data;
        this.environments.forEach(environment => {
          parseEnvironment(environment);
        });
        let hasEnvironment = false;
        for (let i in this.environments) {
          if (this.environments[i].id === this.request.environmentId) {
            hasEnvironment = true;
            break;
          }
        }
        if (!hasEnvironment) {
          this.request.environmentId = undefined;
        }
        if (!this.request.environmentId) {
          this.request.dataSourceId = undefined;
        }
        this.initDataSource();
      });
    },
    openEnvironmentConfig() {
      this.$refs.environmentConfig.open(getCurrentProjectID());
    },
    initDataSource() {
      let flag = false;
      let environment = {};
      for (let i in this.environments) {
        if (this.environments[i].id === this.request.environmentId) {
          environment = this.environments[i];
          break;
        }
      }

      this.databaseConfigsOptions = [];
      if(environment.config&&environment.config.databaseConfigs){
        environment.config.databaseConfigs.forEach(item => {
          if (item.id === this.request.dataSourceId) {
            flag = true;
          }
          this.databaseConfigsOptions.push(item);
        });
      }
      if (!flag) {
        this.request.dataSourceId = "";
      }
    },
    setDataSource() {
      this.initDataSource();

      for (let item of this.databaseConfigsOptions) {
        if (this.request.dataSourceId === item.id) {
          this.request.dataSource = item;
          break;
        }
      }
    },
    environmentChange(value) {
      this.request.dataSource = undefined;
      this.request.dataSourceId = "";
      let environment = {};
      for (let i in this.environments) {
        if (this.environments[i].id === value) {
          environment = this.environments[i];
          break;
        }
      }

      this.databaseConfigsOptions = [];
      environment.config.databaseConfigs.forEach(item => {
        this.databaseConfigsOptions.push(item);
      })
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
</style>
