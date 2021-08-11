<template>
  <div v-loading="result.loading">
    <div v-for="pe in data" :key="pe.id" style="margin-left: 20px;">
      <el-select v-model="pe['selectEnv']"
                 placeholder="请选择环境"
                 style="margin-top: 8px;width: 200px;"
                 size="small"
                 clearable>
        <el-option v-for="(environment, index) in pe.envs"
                   :key="index"
                   :label="environment.name"
                   :value="environment.id"/>
        <el-button v-if="isShowConfirmButton(pe.id)"
                   @click="openEnvironmentConfig(pe.id, pe['selectEnv'])"
                   class="ms-scenario-button"
                   size="mini"
                   type="primary">
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
        <el-tooltip class="item"
                    effect="light"
                    :content="'存在多个环境' + '(' + pe.conflictEnv + ')'"
                    placement="top-end">
          <i class="el-icon-warning-outline"
             v-show="pe.selectEnv === '' && pe.conflictEnv !== ''"/>
        </el-tooltip>
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
    projectIds: Set,
    projectList: Array,
    showConfigButtonWithOutPermission: {
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
    },
    projectEnvMap: {
      type: Object,
      default() {
        return {};
      }
    }
  },
  data() {
    return {
      data: [],
      permissionProjectIds: [],
      dialogVisible: false,
      envMap: new Map()
    }
  },
  methods: {
    isShowConfirmButton(projectId) {
      if (this.showConfigButtonWithOutPermission === true) {
        return true;
      } else {
        if (this.permissionProjectIds) {
          if (this.permissionProjectIds.indexOf(projectId) < 0) {
            return false;
          } else {
            return true;
          }
        } else {
          return false;
        }
      }
    },
    init() {
      //获取当前用户有权限的ID
      if (this.permissionProjectIds.length === 0) {
        this.getUserPermissionProjectIds();
      }
      let arr = [];
      this.projectIds.forEach(projectId => {
        const project = this.projectList.find(p => p.id === projectId);
        if (project) {
          let item = {id: projectId, envs: [], selectEnv: "", conflictEnv: ""};
          this.data.push(item);
          let p = new Promise(resolve => {
            this.$get('/api/environment/list/' + projectId, res => {
              let envs = res.data;
              // 格式化
              envs.forEach(environment => {
                parseEnvironment(environment);
              });
              // 固定环境列表渲染顺序
              let temp = this.data.find(dt => dt.id === projectId);
              temp.envs = envs;
              let envList = [];
              // projectEnvMap {"projectId": {"env1", "env2"}} 获取项目的环境列表
              for (let pid in this.projectEnvMap) {
                if (projectId === pid) {
                  envList = this.projectEnvMap[pid];
                  break;
                }
              }
              // 环境回显
              if (envList.length <= 1) {
                // 选中环境是否存在，无冲突显示环境
                temp.selectEnv = envs.filter(e => e.id === envList[0]).length === 0 ? null : envList[0];
              } else {
                // 环境冲突，记录环境并展示
                envList.forEach(env => {
                  const index = envs.findIndex(e => e.id === env);
                  if (index !== -1) {
                    item.conflictEnv = item.conflictEnv + " " + envs[index].name;
                  }
                });
              }
              resolve();
            })
          });
          arr.push(p);
        }
      })
      return arr;
    },
    getUserPermissionProjectIds() {
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
      this.data.forEach(dt => {
        map.set(dt.id, dt.selectEnv);
      })
      this.$emit('setProjectEnvMap', map);
      this.$emit('close');
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
