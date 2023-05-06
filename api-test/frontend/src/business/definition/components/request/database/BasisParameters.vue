<template>
  <div v-loading="isReloadData || result">
    <el-row>
      <el-col :span="spanNum" style="padding-bottom: 20px">
        <div style="border: 1px #dcdfe6 solid; height: 100%; border-radius: 4px; width: 100%">
          <el-form
            :model="request"
            :rules="rules"
            ref="request"
            label-width="100px"
            :disabled="isReadOnly"
            style="margin: 10px">
            <el-row>
              <el-col :span="7">
                <el-form-item prop="environmentId" :label="$t('api_test.definition.request.run_env')">
                  <el-select
                    v-model="request.environmentId"
                    size="small"
                    class="ms-htt-width"
                    :placeholder="$t('api_test.definition.request.run_env')"
                    @change="environmentChange"
                    clearable>
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
                        <el-button
                          class="environment-button"
                          size="small"
                          type="primary"
                          @click="openEnvironmentConfig">
                          {{ $t('api_test.environment.environment_config') }}
                        </el-button>
                      </div>
                    </template>
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item
                  :label="$t('api_test.request.sql.dataSource')"
                  prop="dataSourceId"
                  style="margin-left: 10px">
                  <el-select v-model="request.dataSourceId" size="small" @change="reload">
                    <el-option
                      v-for="(item, index) in databaseConfigsOptions"
                      :key="index"
                      :value="item.id"
                      :label="item.name" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="7">
                <el-form-item :label="$t('api_test.request.sql.timeout')" prop="queryTimeout" style="margin-left: 10px">
                  <el-input-number
                    :disabled="isReadOnly"
                    size="small"
                    v-model="request.queryTimeout"
                    :placeholder="$t('commons.millisecond')"
                    :max="1000 * 10000000"
                    :min="0" />
                </el-form-item>
              </el-col>
              <el-col :span="4">
                <el-checkbox
                  v-if="request.referenced === 'Created' && scenarioId !== ''"
                  v-model="request.isRefEnvironment"
                  :disabled="request.disabled"
                  class="ref_environment"
                  @change="getEnvironments">
                  {{ $t('api_test.request.refer_to_environment') }}
                </el-checkbox>
              </el-col>
            </el-row>

            <el-form-item :label="$t('api_test.request.sql.result_variable')" prop="resultVariable">
              <el-input v-model="request.resultVariable" maxlength="500" show-word-limit size="small" />
            </el-form-item>

            <el-form-item :label="$t('api_test.request.sql.variable_names')" prop="variableNames">
              <el-input v-model="request.variableNames" maxlength="500" show-word-limit size="small" />
            </el-form-item>

            <el-tabs v-model="activeName" @tab-click="tabClick" class="ms-tab-content ms-tabs__nav-scroll">
              <el-tab-pane :label="$t('api_test.scenario.variables')" name="variables" v-if="isBodyShow">
                <ms-api-scenario-variables
                  :is-read-only="isReadOnly"
                  :items="request.variables"
                  :description="$t('api_test.scenario.kv_description')" />
              </el-tab-pane>
              <el-tab-pane :label="$t('api_test.request.sql.sql_script')" name="sql">
                <ms-code-edit
                  mode="sql"
                  :read-only="isReadOnly"
                  :modes="['sql']"
                  :data.sync="request.query"
                  :height="200"
                  theme="eclipse"
                  ref="codeEdit"
                  :key="request.id" />
              </el-tab-pane>
              <!-- 脚本步骤/断言步骤 -->
              <el-tab-pane :label="$t('api_test.definition.request.pre_operation')" name="preOperate" v-if="showScript">
                <span class="item-tabs" effect="dark" placement="top-start" slot="label">
                  {{ $t('api_test.definition.request.pre_operation') }}
                  <div class="el-step__icon is-text ms-api-col ms-header" v-if="request.preSize > 0">
                    <div class="el-step__icon-inner">{{ request.preSize }}</div>
                  </div>
                </span>
                <ms-jmx-step
                  :request="request"
                  :scenarioId="scenarioId"
                  :apiId="request.id"
                  :response="response"
                  :tab-type="'pre'"
                  ref="preStep" />
              </el-tab-pane>
              <el-tab-pane
                :label="$t('api_test.definition.request.post_operation')"
                name="postOperate"
                v-if="showScript">
                <span class="item-tabs" effect="dark" placement="top-start" slot="label">
                  {{ $t('api_test.definition.request.post_operation') }}
                  <div class="el-step__icon is-text ms-api-col ms-header" v-if="request.postSize > 0">
                    <div class="el-step__icon-inner">
                      {{ request.postSize }}
                    </div>
                  </div>
                </span>
                <ms-jmx-step
                  :request="request"
                  :scenarioId="scenarioId"
                  :apiId="request.id"
                  :response="response"
                  :tab-type="'post'"
                  ref="postStep" />
              </el-tab-pane>
              <el-tab-pane
                :label="$t('api_test.definition.request.assertions_rule')"
                name="assertionsRule"
                v-if="showScript">
                <span class="item-tabs" effect="dark" placement="top-start" slot="label">
                  {{ $t('api_test.definition.request.assertions_rule') }}
                  <div class="el-step__icon is-text ms-api-col ms-header" v-if="request.ruleSize > 0">
                    <div class="el-step__icon-inner">
                      {{ request.ruleSize }}
                    </div>
                  </div>
                </span>
                <ms-jmx-step
                  :request="request"
                  :apiId="request.id"
                  :response="response"
                  :scenarioId="scenarioId"
                  @reload="reloadBody"
                  :tab-type="'assertionsRule'"
                  ref="assertionsRule" />
              </el-tab-pane>
            </el-tabs>
          </el-form>
        </div>
      </el-col>
    </el-row>

    <!-- 环境 -->
    <api-environment-config ref="environmentConfig" @close="environmentConfigClose" />
  </div>
</template>

<script>
import MsApiKeyValue from '../../ApiKeyValue';
import MsApiAssertions from '../../assertion/ApiAssertions';
import MsApiExtract from '../../extract/ApiExtract';
import ApiRequestMethodSelect from '../../collapse/ApiRequestMethodSelect';
import MsCodeEdit from 'metersphere-frontend/src/components/MsCodeEdit';
import MsApiScenarioVariables from '../../ApiScenarioVariables';
import { parseEnvironment } from '@/business/environment/model/EnvironmentModel';
import ApiEnvironmentConfig from 'metersphere-frontend/src/components/environment/ApiEnvironmentConfig';
import { getUUID, objToStrMap } from 'metersphere-frontend/src/utils';
import { getCurrentProjectID } from 'metersphere-frontend/src/utils/token';
import MsJsr233Processor from '@/business/automation/scenario/component/Jsr233Processor';
import MsJmxStep from '../../step/JmxStep';
import { hisDataProcessing, stepCompute } from '@/business/definition/api-definition';
import { getEnvironmentByProjectId } from 'metersphere-frontend/src/api/environment';
import { useApiStore } from '@/store';

const store = useApiStore();

export default {
  name: 'MsDatabaseConfig',
  components: {
    MsJsr233Processor,
    MsApiScenarioVariables,
    MsCodeEdit,
    ApiRequestMethodSelect,
    MsApiExtract,
    MsApiAssertions,
    MsApiKeyValue,
    ApiEnvironmentConfig,
    MsJmxStep,
  },
  props: {
    request: {},
    basisData: {},
    response: {},
    moduleOptions: Array,
    showScript: {
      type: Boolean,
      default: true,
    },
    isCase: {
      type: Boolean,
      default: false,
    },
    scenarioId: String,
    isReadOnly: {
      type: Boolean,
      default: false,
    },
  },
  data() {
    return {
      spanNum: 24,
      environments: [],
      isBodyShow: true,
      currentEnvironment: {},
      databaseConfigsOptions: [],
      isReloadData: false,
      activeName: 'variables',
      rules: {},
      result: false,
    };
  },
  watch: {
    'request.hashTree': {
      handler(v) {
        this.initStepSize(this.request.hashTree);
      },
      deep: true,
    },
    storeUseEnvironment: function () {
      if (this.scenarioId !== '') {
        this.getEnvironments(store.useEnvironment);
      }
    },
    storeScenarioEnvMap: {
      handler(v) {
        this.getEnvironments();
      },
      deep: true,
    },
    'request.refEevMap': {
      handler(v) {
        this.getEnvironments();
      },
      deep: true,
    },
  },
  created() {
    this.getEnvironments();
    if (this.request.hashTree) {
      this.initStepSize(this.request.hashTree);
      this.historicalDataProcessing(this.request.hashTree);
    }
  },
  computed: {
    storeUseEnvironment() {
      return store.useEnvironment;
    },
    storeScenarioEnvMap() {
      return store.scenarioEnvMap;
    },
    projectId() {
      return getCurrentProjectID();
    },
  },
  methods: {
    tabClick() {
      if (this.activeName === 'preOperate') {
        this.$refs.preStep.filter();
      }
      if (this.activeName === 'postOperate') {
        this.$refs.postStep.filter();
      }
      if (this.activeName === 'assertionsRule') {
        this.$refs.assertionsRule.filter();
      }
    },
    historicalDataProcessing(array) {
      hisDataProcessing(array, this.request);
    },
    initStepSize(array) {
      stepCompute(array, this.request);
      this.reloadBody();
    },
    reloadBody() {
      // 解决修改请求头后 body 显示错位
      this.isBodyShow = false;
      this.$nextTick(() => {
        this.isBodyShow = true;
      });
    },
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
    runTest() {},
    itselfEnvironment(environmentId) {
      let id = this.request.projectId ? this.request.projectId : this.projectId;
      getEnvironmentByProjectId(id).then((response) => {
        this.environments = response.data;
        let targetDataSourceName = undefined;
        let currentEnvironment = undefined;
        let hasEnvironment = false;
        this.environments.forEach((environment) => {
          parseEnvironment(environment);
          // 找到原始环境和数据源名称
          if (environment.id === environmentId) {
            currentEnvironment = environment;
          }
          if (environment.id === this.request.environmentId) {
            hasEnvironment = true;
            if (environment.config && environment.config.databaseConfigs) {
              environment.config.databaseConfigs.forEach((item) => {
                if (item.id === this.request.dataSourceId) {
                  targetDataSourceName = item.name;
                }
              });
            }
          }
        });
        if (!hasEnvironment) {
         this.request.environmentId = store.useEnvironment;
         this.environments.forEach((environment) => {
           if (environment.id === store.useEnvironment) {
             currentEnvironment = environment;
           }
           if (environment.config && environment.config.databaseConfigs) {
             environment.config.databaseConfigs.forEach((item) => {
               if (item.id === this.request.dataSourceId) {
                 targetDataSourceName = item.name;
               }
             });
           }
         });
        }
        this.initDataSource(environmentId, currentEnvironment, targetDataSourceName);
      });
    },
    getEnvironments(environmentId) {
      let envId = '';
      let id = this.request.projectId ? this.request.projectId : this.projectId;
      let scenarioEnvId = this.request.currentScenarioId ? this.request.currentScenarioId + '_' + id : id;
      if (store.scenarioEnvMap && store.scenarioEnvMap instanceof Map && store.scenarioEnvMap.has(scenarioEnvId)) {
        envId = store.scenarioEnvMap.get(scenarioEnvId);
      }
      if (this.request.referenced === 'Created' && this.scenarioId !== '' && !this.request.isRefEnvironment) {
        this.itselfEnvironment(environmentId);
        return;
      } else if (!this.scenarioId && !this.request.customizeReq) {
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
      let targetDataSourceName = '';
      let currentEnvironment = {};

      if (envId === this.request.originalEnvironmentId && this.request.originalDataSourceId) {
        this.request.dataSourceId = this.request.originalDataSourceId;
      }
      getEnvironmentByProjectId(id).then((response) => {
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
            if (!this.isCase) {
              this.environments = [currentEnvironment];
            }
          }
        });
        if (!hasEnvironment) {
          this.request.environmentId = "";
        }
        this.initDataSource(envId, currentEnvironment, targetDataSourceName);
      });
    },
    openEnvironmentConfig() {
      this.$refs.environmentConfig.open(this.request.projectId ? this.request.projectId : getCurrentProjectID());
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
      for (let i in this.environments) {
        if (this.environments[i].id === value) {
          this.databaseConfigsOptions = [];
          this.environments[i].config.databaseConfigs.forEach((item) => {
            this.databaseConfigsOptions.push(item);
          });
          if (this.request.hashTree && !this.scenarioId) {
            this.setOwnEnvironment(this.request.hashTree, value);
          }
          break;
        }
      }
    },
    setOwnEnvironment(scenarioDefinition, env) {
      for (let i in scenarioDefinition) {
        let typeArray = ['JDBCPostProcessor', 'JDBCSampler', 'JDBCPreProcessor'];
        if (typeArray.indexOf(scenarioDefinition[i].type) !== -1) {
          // 找到原始数据源名称
          this.getTargetSource(scenarioDefinition[i]);
          scenarioDefinition[i].environmentId = env;
        }
        if (scenarioDefinition[i].hashTree && scenarioDefinition[i].hashTree.length > 0) {
          this.setOwnEnvironment(scenarioDefinition[i].hashTree, env);
        }
      }
    },
    getTargetSource(obj) {
      this.environments.forEach((environment) => {
        parseEnvironment(environment);
        // 找到原始环境和数据源名称
        if (environment.id === obj.environmentId) {
          if (environment.config && environment.config.databaseConfigs) {
            environment.config.databaseConfigs.forEach((item) => {
              if (item.id === obj.dataSourceId) {
                obj.targetDataSourceName = item.name;
              }
            });
          }
        }
      });
    },
    environmentConfigClose() {
      this.getEnvironments();
    },
  },
};
</script>

<style scoped>
.ms-tab-content {
  min-height: 200px;
}

.one-row .el-form-item {
  display: inline-block;
}

.one-row .el-form-item:nth-child(2) {
  margin-left: 60px;
}

.ms-header {
  background: #783887;
  color: white;
  height: 18px;
  font-size: xx-small;
  border-radius: 50%;
}

.environment-button {
  margin-left: 20px;
  padding: 7px;
}

:deep(.el-form-item) {
  margin-bottom: 15px;
}

.ref_environment {
  margin-top: 13px;
  float: right;
}

.ms-tabs__nav-scroll :deep(.el-tabs__nav-wrap.is-top) {
  width: 100%;
}
</style>
