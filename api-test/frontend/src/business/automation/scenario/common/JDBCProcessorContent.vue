<template>
  <div v-loading="isReloadData || result">
    <div class="jdbc-class">
      <el-form
        :model="request"
        :rules="rules"
        ref="request"
        label-width="100px"
        :disabled="request.disabled"
        style="margin: 10px">
        <el-row>
          <el-col :span="8">
            <el-form-item prop="environmentId" :label="$t('api_test.definition.request.run_env')">
              <el-select
                v-model="request.environmentId"
                size="small"
                class="ms-htt-width"
                :placeholder="$t('api_test.definition.request.run_env')"
                @change="environmentChange"
                clearable
                :disabled="isReadOnly">
                <el-option
                  v-for="(environment, index) in environments"
                  :key="index"
                  :label="environment.name"
                  :value="environment.id" />
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
              <el-select v-model="request.dataSourceId" size="small" @change="reload" :disabled="request.disabled">
                <el-option
                  v-for="(item, index) in databaseConfigsOptions"
                  :key="index"
                  :value="item.id"
                  :label="item.name" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item :label="$t('api_test.request.sql.timeout')" prop="queryTimeout" style="margin-left: 10px">
              <el-input-number
                :disabled="request.disabled"
                size="small"
                v-model="request.queryTimeout"
                :placeholder="$t('commons.millisecond')"
                :max="1000 * 10000000"
                :min="0" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item :label="$t('api_test.request.sql.result_variable')" prop="resultVariable">
          <el-input v-model="request.resultVariable" maxlength="500" show-word-limit size="small" />
        </el-form-item>

        <el-form-item :label="$t('api_test.request.sql.variable_names')" prop="variableNames">
          <el-input v-model="request.variableNames" maxlength="500" show-word-limit size="small" />
        </el-form-item>

        <el-tabs v-model="activeName" class="ms-sql-tabs">
          <el-tab-pane :label="$t('api_test.scenario.variables')" name="variables">
            <ms-api-scenario-variables
              :is-read-only="isReadOnly"
              :items="request.variables"
              :description="$t('api_test.scenario.kv_description')" />
          </el-tab-pane>
          <el-tab-pane :label="$t('api_test.request.sql.sql_script')" name="sql">
            <ms-code-edit
              :height="120"
              :read-only="isReadOnly"
              :modes="['sql']"
              :data.sync="request.query"
              theme="eclipse"
              mode="sql"
              ref="codeEdit" />
          </el-tab-pane>
        </el-tabs>
      </el-form>
    </div>

    <!-- 环境 -->
    <api-environment-config ref="environmentConfig" @close="environmentConfigClose" />
  </div>
</template>

<script>
import MsApiKeyValue from '@/business/definition/components/ApiKeyValue';
import MsApiExtract from '@/business/definition/components/extract/ApiExtract';
import ApiRequestMethodSelect from '@/business/definition/components/collapse/ApiRequestMethodSelect';
import MsCodeEdit from 'metersphere-frontend/src/components/MsCodeEdit';
import MsApiScenarioVariables from '@/business/definition/components/ApiScenarioVariables';
import { parseEnvironment } from '@/business/environment/model/EnvironmentModel';
import ApiEnvironmentConfig from 'metersphere-frontend/src/components/environment/ApiEnvironmentConfig';
import { getCurrentProjectID } from 'metersphere-frontend/src/utils/token';
import { getUUID, objToStrMap } from 'metersphere-frontend/src/utils';
import MsJsr233Processor from '@/business/automation/scenario/component/Jsr233Processor';
import { getEnvironmentByProjectId } from 'metersphere-frontend/src/api/environment';
import { useApiStore } from '@/store';

const store = useApiStore();
export default {
  name: 'JdbcProcessorContent',
  components: {
    MsJsr233Processor,
    MsApiScenarioVariables,
    MsCodeEdit,
    ApiRequestMethodSelect,
    MsApiExtract,
    MsApiKeyValue,
    ApiEnvironmentConfig,
  },
  props: {
    request: {},
    basisData: {},
    moduleOptions: Array,
    showScript: {
      type: Boolean,
      default: true,
    },
    scenarioId: String,
    isReadOnly: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      result: false,
      environments: [],
      currentEnvironment: {},
      databaseConfigsOptions: [],
      isReloadData: false,
      activeName: 'variables',
      rules: {},
    };
  },
  created() {
    this.getEnvironments(null, 'created');
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    },
    storeScenarioEnvMap() {
      return store.scenarioEnvMap;
    },
    storeUseEnvironment() {
      return store.useEnvironment;
    },
  },
  watch: {
    // 场景环境监听
    storeScenarioEnvMap: {
      handler(v) {
        this.getEnvironments();
      },
      deep: true,
    },
    // 接口/用例 右上角公共环境监听
    storeUseEnvironment: function () {
      if (!this.scenarioId) {
        this.getEnvironments(store.useEnvironment);
      }
    },
    // 接口/用例 自身环境监听
    'request.environmentId': function () {
      if (!this.scenarioId) {
        this.setStep(undefined, undefined, this.request.targetDataSourceName);
      }
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
      this.isReloadData = true;
      this.$nextTick(() => {
        this.isReloadData = false;
      });
    },
    validate() {
      this.$refs['request'].validate((valid) => {
        if (valid) {
          this.$emit('callback');
        }
      });
    },
    saveApi() {
      this.basisData.method = this.basisData.protocol;
      this.$emit('saveApi', this.basisData);
    },
    itselfEnvironment(environmentId) {
      let id = this.request.projectId ? this.request.projectId : this.projectId;
      this.result = getEnvironmentByProjectId(id).then((response) => {
        this.environments = response.data;
        let targetDataSourceName = this.request.targetDataSourceName;
        this.environments.forEach((environment) => {
          parseEnvironment(environment);
          // 找到原始环境和数据源名称
          if (environment.id === this.request.environmentId) {
            if (!environmentId) {
              environmentId = environment.id;
            }
            if (environment.config && environment.config.databaseConfigs) {
              environment.config.databaseConfigs.forEach((item) => {
                if (item.id === this.request.dataSourceId) {
                  targetDataSourceName = item.name;
                }
              });
            }
          }
        });
        if (environmentId) {
          this.request.environmentId = environmentId;
        } else {
          this.request.environmentId = null;
        }
        this.initDataSource(undefined, undefined, targetDataSourceName);
      });
    },
    // 跨项目步骤如果没有环境则走当前场景环境
    async selectProjectId(environmentId) {
      let id = this.request.projectId ? this.request.projectId : this.projectId;
      // 来自单接口请求
      if (environmentId) {
        return id;
      }
      let scenarioEnvId = this.scenarioId !== '' ? this.scenarioId + '_' + id : id;
      if (store.scenarioEnvMap && store.scenarioEnvMap instanceof Map && store.scenarioEnvMap.has(scenarioEnvId)) {
        return id;
      }
      return this.projectId;
    },
    async getEnvironments(environmentId, isCreated) {
      let envId = '';
      let id = await this.selectProjectId(environmentId);
      let scenarioEnvId = this.scenarioId !== '' ? this.scenarioId + '_' + id : id;
      if (store.scenarioEnvMap && store.scenarioEnvMap instanceof Map && store.scenarioEnvMap.has(scenarioEnvId)) {
        envId = store.scenarioEnvMap.get(scenarioEnvId);
      }
      if (!this.scenarioId && !this.request.customizeReq) {
        this.itselfEnvironment(environmentId);
        return;
      }
      this.environments = [];
      // 场景开启自身环境
      if (this.request.environmentEnable && this.request.refEevMap) {
        let obj = Object.prototype.toString
          .call(this.request.refEevMap)
          .match(/\[object (\w+)\]/)[1]
          .toLowerCase();
        if (obj !== 'object' && obj !== 'map') {
          this.request.refEevMap = objToStrMap(JSON.parse(this.request.refEevMap));
        } else if (obj === 'object' && obj !== 'map') {
          this.request.refEevMap = objToStrMap(this.request.refEevMap);
        }
        if (this.request.refEevMap instanceof Map && this.request.refEevMap.has(id)) {
          envId = this.request.refEevMap.get(id);
        }
      }
      if (
        envId === this.request.originalEnvironmentId &&
        this.request.originalDataSourceId &&
        isCreated !== 'created'
      ) {
        this.request.dataSourceId = this.request.originalDataSourceId;
      }
      let targetDataSourceName = '';
      let currentEnvironment = {};
      this.result = getEnvironmentByProjectId(id).then((response) => {
        this.environments = response.data;
        let hasEnvironment = false;
        this.environments.forEach((environment) => {
          parseEnvironment(environment);
          // 找到原始环境和数据源名称
          if (environment.id === this.request.environmentId && environment.id !== envId) {
            hasEnvironment = true;
            if (environment.config && environment.config.databaseConfigs) {
              environment.config.databaseConfigs.forEach((item) => {
                if (item.id === this.request.dataSourceId) {
                  targetDataSourceName = item.name;
                }
              });
            }
          }
          if (envId && environment.id === envId) {
            currentEnvironment = environment;
            this.environments = [currentEnvironment];
          }
        });
        if (!hasEnvironment) {
          this.request.environmentId = '';
        }
        this.initDataSource(envId, currentEnvironment, targetDataSourceName);
      });
    },
    openEnvironmentConfig() {
      this.$refs.environmentConfig.open(getCurrentProjectID());
    },
    setStep(envId, currentEnvironment, targetDataSourceName) {
      let envs = this.environments.filter((item) => this.request && item.id === this.request.environmentId);
      if (envs && envs.length === 0) {
        let id = this.request.projectId ? this.request.projectId : this.projectId;
        this.result = getEnvironmentByProjectId(id).then((response) => {
          this.environments = response.data;
          this.environments.forEach((environment) => {
            parseEnvironment(environment);
          });
          this.initDataSource(envId, currentEnvironment, targetDataSourceName);
        });
      } else {
        this.initDataSource(envId, currentEnvironment, targetDataSourceName);
      }
    },
    initDataSource(envId, currentEnvironment, targetDataSourceName) {
      this.databaseConfigsOptions = [];
      if (envId) {
        this.request.environmentId = envId;
      } else {
        for (let i in this.environments) {
          if (this.environments[i].id === this.request.environmentId) {
            currentEnvironment = this.environments[i];
            break;
          }
        }
      }
      let flag = false;
      if (currentEnvironment && currentEnvironment.config && currentEnvironment.config.databaseConfigs) {
        currentEnvironment.config.databaseConfigs.forEach((item) => {
          if (item.id === this.request.dataSourceId) {
            flag = true;
          }
          // 按照名称匹配
          else if (targetDataSourceName && item.name === targetDataSourceName) {
            this.request.dataSourceId = item.id;
            flag = true;
          }
          this.databaseConfigsOptions.push(item);
        });
        if (!flag && currentEnvironment.config.databaseConfigs.length > 0) {
          this.request.dataSourceId = currentEnvironment.config.databaseConfigs[0].id;
          flag = true;
        }
      }
      if (!flag) {
        this.request.dataSourceId = '';
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
      this.request.dataSourceId = '';
      let environment = {};
      for (let i in this.environments) {
        if (this.environments[i].id === value) {
          environment = this.environments[i];
          break;
        }
      }
      this.databaseConfigsOptions = [];
      if (environment.config && environment.config.databaseConfigs) {
        environment.config.databaseConfigs.forEach((item) => {
          this.databaseConfigsOptions.push(item);
        });
      }
    },
    environmentConfigClose() {
      this.getEnvironments();
    },
  },
};
</script>

<style scoped>
.ms-sql-tabs {
  min-height: 160px;
}

.one-row .el-form-item {
  display: inline-block;
}

.one-row .el-form-item:nth-child(2) {
  margin-left: 60px;
}

.environment-button {
  margin-left: 20px;
  padding: 7px;
}

:deep(.el-form-item) {
  margin-bottom: 15px;
}

.jdbc-class {
  border: 1px #dcdfe6 solid;
  height: 100%;
  border-radius: 4px;
  width: 100%;
}
</style>
