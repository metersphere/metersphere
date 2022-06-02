<template>
    <span>
      <el-select v-model="currentData.environmentId" size="small" class="ms-htt-width"
                 :placeholder="$t('api_test.definition.request.run_env')"
                 @change="environmentChange" clearable>
        <el-option v-for="(environment, index) in environments" :key="index"
                   :label="getLabel(environment)"
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

      <!-- 环境 -->
      <api-environment-config ref="environmentConfig" @close="environmentConfigClose"/>
    </span>
</template>

<script>
    import {parseEnvironment} from "../../model/EnvironmentModel";
    import ApiEnvironmentConfig from "@/business/components/api/test/components/ApiEnvironmentConfig";

    export default {
      name: "EnvironmentSelect",
      components: {ApiEnvironmentConfig},
      data() {
        return {
          environments: []
        }
      },
      props: {
        projectId: String,
        currentData: {},
        type: String
      },
      created() {
        this.getEnvironments();
      },
      methods: {
        getEnvironments() {
          if (this.projectId) {
            this.$get('/api/environment/list/' + this.projectId, response => {
              this.environments = response.data;
              this.environments.forEach(environment => {
                parseEnvironment(environment);
              });
              let hasEnvironment = false;
              for (let i in this.environments) {
                if (this.environments[i].id === this.currentData.environmentId) {
                  this.currentData.environmentId = this.environments[i].id;
                  hasEnvironment = true;
                  break;
                }
              }
              if (!hasEnvironment) {
                this.currentData.environmentId =  this.$store.state.useEnvironment;
                this.currentData.environment =  this.$store.state.useEnvironment;
              }
            });
          } else {
            this.currentData.environmentId = '';
            this.currentData.environment = undefined;
          }
        },
        getLabel(environment) {
          if (environment) {
            if (this.type === 'TCP') {
              if (environment.config.tcpConfig && environment.config.tcpConfig.server) {
                return environment.name + ": " + environment.config.tcpConfig.server + ":" +
                  (environment.config.tcpConfig.port ? environment.config.tcpConfig.port : "");
              } else {
                return environment.name;
              }
            }
            return environment.name;
          }
          return "";
        },
        environmentConfigClose() {
          this.getEnvironments();
        },
        environmentChange(value) {
          for (let i in this.environments) {
            if (this.environments[i].id === value) {
              this.currentData.environmentId = value;
              if (this.currentData.request) {
                this.currentData.request.useEnvironment = value;
                // 更改当前步骤中含SQL前后置步骤对应的数据源
                if(this.currentData.request.hashTree) {
                  // 找到原始环境和数据源名称
                  let environment = this.environments[i];
                  this.setOwnEnvironment(this.currentData.request.hashTree, environment);
                }
              }
              break;
            }
          }
        },
        getTargetSource(obj){
          this.environments.forEach(environment => {
            parseEnvironment(environment);
            // 找到原始环境和数据源名称
            if (environment.id === obj.environmentId) {
              if (environment.config && environment.config.databaseConfigs) {
                environment.config.databaseConfigs.forEach(item => {
                  if (item.id === obj.dataSourceId) {
                    obj.targetDataSourceName = item.name;
                  }
                });
              }
            }
          });
        },
        setOwnEnvironment(scenarioDefinition,env) {
          for (let i in scenarioDefinition) {
            let typeArray = ["JDBCPostProcessor", "JDBCSampler", "JDBCPreProcessor"]
            if (typeArray.indexOf(scenarioDefinition[i].type) !== -1) {
              // 找到原始数据源名称
              this.getTargetSource(scenarioDefinition[i])
              scenarioDefinition[i].environmentId = env.id;
            }
            if (scenarioDefinition[i].hashTree !== undefined && scenarioDefinition[i].hashTree.length > 0) {
              this.setOwnEnvironment(scenarioDefinition[i].hashTree,env);
            }
          }
        },
        openEnvironmentConfig() {
          if (!this.projectId) {
            this.$error(this.$t('api_test.select_project'));
            return;
          }
          this.$refs.environmentConfig.open(this.projectId);
        },
        setEnvironment(environmentId){
          this.currentData.environmentId = environmentId;
          if (this.currentData.request) {
            this.currentData.request.useEnvironment = environmentId;
          }
        }
      }
    }
</script>

<style scoped>

  .environment-button {
    margin-left: 20px;
    padding: 7px;
  }

</style>
