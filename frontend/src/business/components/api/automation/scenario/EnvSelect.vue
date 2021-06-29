<template>
  <div v-loading="result.loading">
    <div v-for="pe in data" :key="pe.id" style="margin-left: 20px;">
      <el-select v-model="pe['selectEnv']" placeholder="请选择环境" style="margin-top: 8px;width: 200px;" size="small">
        <el-option v-for="(environment, index) in pe.envs" :key="index"
                   :label="environment.name"
                   :value="environment.id"/>
        <el-button class="ms-scenario-button" v-if="isShowConfirmButton(pe.id)" size="mini" type="primary"
                   @click="openEnvironmentConfig(pe.id, pe['selectEnv'])">
          {{ $t('api_test.environment.environment_config') }}
        </el-button>
        <template v-slot:empty>
          <div v-if="isShowConfirmButton(pe.id)" class="empty-environment">
            <el-button class="ms-scenario-button" size="mini" type="primary"
                       @click="openEnvironmentConfig(pe.id, pe['selectEnv'])">
              {{ $t('api_test.environment.environment_config') }}
            </el-button>
          </div>
        </template>
      </el-select>
      <span class="project-name" :title="getProjectName(pe.id)">
        {{ getProjectName(pe.id) }}
      </span>
    </div>

    <el-button type="primary" @click="handleConfirm" size="small" class="env-confirm">确 定</el-button>

    <!-- 环境配置 -->
    <api-environment-config ref="environmentConfig" @close="environmentConfigClose"/>
  </div>
</template>

<script>
import {parseEnvironment} from "@/business/components/api/test/model/EnvironmentModel";
import ApiEnvironmentConfig from "@/business/components/api/test/components/ApiEnvironmentConfig";

export default {
  name: "EnvironmentSelect",
  components: {ApiEnvironmentConfig},
  props: {
    envMap: Map,
    projectIds: Set,
    projectList: Array,
    showConfigButtonWithOutPermission:{
      type: Boolean,
      default() {
        return true;
      }
    },
    result: {
      type: Object,
      default() {
        return {loading: false}
      }
    }
  },
  data() {
    return {
      data: [],
      // result: {},
      projects: [],
      environments: [],
      permissionProjectIds:[],
      dialogVisible: false,
      isFullUrl: true,
    }
  },
  methods: {
    isShowConfirmButton(projectId){
      if(this.showConfigButtonWithOutPermission === true){
        return true;
      }else{
        if(this.permissionProjectIds){
          if(this.permissionProjectIds.indexOf(projectId)<0){
            return false;
          }else {
            return true;
          }
        }else{
          return false;
        }
      }
    },
    init() {
      //获取当前用户有权限的ID
      if(this.permissionProjectIds.length === 0){
        this.getUserPermissionProjectIds();
      }

      let arr = [];
      this.projectIds.forEach(id => {
        const project = this.projectList.find(p => p.id === id);
        if (project) {
          let item = {id: id, envs: [], selectEnv: ""};
          this.data.push(item);
          let p = new Promise(resolve => {
            this.$get('/api/environment/list/' + id, res => {
              let envs = res.data;
              envs.forEach(environment => {
                parseEnvironment(environment);
              });
              // 固定环境列表渲染顺序
              let temp = this.data.find(dt => dt.id === id);
              temp.envs = envs;
              let envId = this.envMap.get(id);
              // 选中环境是否存在
              temp.selectEnv = envs.filter(e => e.id === envId).length === 0 ? null : envId;
              resolve();
            })
          })
          arr.push(p);
        }
      })
      return arr;
    },
    getUserPermissionProjectIds(){
      this.$get('/project/getOwnerProjectIds/', res => {
        this.permissionProjectIds = res.data;
      })
    },
    open() {
      this.data = [];
      if (this.projectIds.size > 0) {
        this.init();
      }
    },
    initEnv() {
      this.data = [];
      return Promise.all(this.init());
    },
    getProjectName(id) {
      const project = this.projectList.find(p => p.id === id);
      return project ? project.name : "";
    },
    openEnvironmentConfig(projectId, envId) {
      if (!projectId) {
        this.$error(this.$t('api_test.select_project'));
        return;
      }
      this.$refs.environmentConfig.open(projectId, envId);
    },
    handleConfirm() {
      let map = new Map();
      let sign = true;
      this.data.forEach(dt => {
        if (!dt.selectEnv) {
          sign = false;
          return;
        }
        map.set(dt.id, dt.selectEnv);
      })
      if (!sign) {
        this.$warning("请为每个项目选择一个运行环境！");
        return;
      }
      this.$emit('setProjectEnvMap', map);
      this.$emit('close');
    },
    getApiInfo(request) {
      if (request.id && request.referenced === 'REF') {
        let requestResult = request.requestResult;
        let url = request.refType && request.refType === 'CASE' ? "/api/testcase/get/" : "/api/definition/get/";
        let enable = request.enable;
        this.$get(url + request.id, response => {
          if (response.data) {
            Object.assign(request, JSON.parse(response.data.request));
            request.name = response.data.name;
            request.enable = enable;
            if (response.data.path && response.data.path != null) {
              request.path = response.data.path;
              request.url = response.data.url;
              this.setUrl(request.path);
            }
            if (response.data.method && response.data.method != null) {
              request.method = response.data.method;
            }
            request.requestResult = requestResult;
            request.id = response.data.id;
            request.disabled = true;
            request.root = true;
            if (!request.projectId) {
              request.projectId = response.data.projectId;
            }
            this.reload();
            this.sort();
          } else {
            request.referenced = "Deleted";
          }
        })
      }
    },
    getScenario(scenario) {
      this.result = this.$get("/api/automation/getApiScenario/" + scenario.id, response => {
        if (response.data) {
          scenario.loaded = true;
          let obj = {};
          if (response.data.scenarioDefinition) {
            obj = JSON.parse(response.data.scenarioDefinition);
            scenario.hashTree = obj.hashTree;
          }
          //scenario.disabled = true;
          scenario.name = response.data.name;
          if (!scenario.projectId) {
            scenario.projectId = response.data.projectId;
          }
          scenario.headers = obj.headers;
          scenario.variables = obj.variables;
          scenario.environmentMap = obj.environmentMap;
          this.$emit('refReload');
        } else {
          scenario.referenced = "Deleted";
        }
      })
    },
    recursiveSorting(arr) {
      for (let i in arr) {
        if (arr[i].referenced === 'REF') {
          // 分场景和接口
          if (arr[i].type === "HTTPSamplerProxy") {
            // 获取源头内容
            this.getApiInfo(arr[i]);
            // 校验是否是全路径
            if (!arr[i].url || (!arr[i].url.startsWith("http://") && !arr[i].url.startsWith("https://"))) {
              this.isFullUrl = false;
            }
          } else if (arr[i].type === "scenario") {
            this.getScenario(arr[i]);
          }

        } else {
          if (arr[i].type === "HTTPSamplerProxy") {
            // 校验是否是全路径
            if (arr[i].enable) {
              if (!arr[i].url || (!arr[i].url.startsWith("http://") && !arr[i].url.startsWith("https://"))) {
                this.isFullUrl = false;
              }
            }
          }
        }
        if (arr[i].hashTree != undefined && arr[i].hashTree.length > 0) {
          this.recursiveSorting(arr[i].hashTree);
        }
      }
    },
    checkFullUrl(scenarioDefinition) {
      for (let i in scenarioDefinition) {
        // 设置项目ID
        let request = scenarioDefinition[i];
        if (request.referenced === 'REF') {
          if (request.type === "HTTPSamplerProxy") {
            this.getApiInfo(request);
            // 校验是否是全路径
            if (!request.url || (!request.url.startsWith("http://") && !request.url.startsWith("https://"))) {
              this.isFullUrl = false;
            }
          } else if (request.type === "scenario") {
            this.getScenario(request);
          }
        } else {
          if (request.type === "HTTPSamplerProxy") {
            // 校验是否是全路径

            if (request.enable) {
              if (!request.url || (!request.url.startsWith("http://") && !request.url.startsWith("https://"))) {
                this.isFullUrl = false;
              }
            }
          }
        }
        if (scenarioDefinition[i].hashTree != undefined && scenarioDefinition[i].hashTree.length > 0) {
          this.recursiveSorting(scenarioDefinition[i].hashTree);
        }
      }
    },
    checkEnv(data) {
      let sign = true;
      this.isFullUrl = true;
      if(data){
        return true;
      }
      if (this.data.length > 0) {
        this.data.forEach(dt => {
          if (!dt.selectEnv) {
            sign = false;
            return false;
          }
        })
      } else {
        // 如果有环境，检查环境
        if (this.envMap && this.envMap.size > 0) {
          this.projectIds.forEach(id => {
            if (!this.envMap.get(id)) {
              sign = false;
              return false;
            }
          })
        }
      }

      if (!sign) {
        this.$warning("请为每个项目选择一个运行环境！");
        return false;
      }
      return true;
    },
    environmentConfigClose() {
      // todo 关闭处理
    }
  }
}
</script>

<style scoped>
.ms-scenario-button {
  margin-left: 20px;
}

.env-confirm {
  margin-left: 20px;
  width: 360px;
  margin-top: 10px;
}

.project-name {
  display: inline-block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 150px;
  margin-left: 8px;
  vertical-align: middle;
}
</style>
