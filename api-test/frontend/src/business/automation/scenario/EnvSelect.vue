<template>
  <div v-loading="result.loading">
    <div v-for="pe in currentProjectEnv" :key="pe.id" style="margin-left: 20px; margin-bottom: 20px">
      <span :v-show="pe.id === currentProjectID">
        <span :title="getProjectName(pe.id)" style="margin-left: 30px; margin-right: 10px">
          {{ getProjectName(pe.id) }}
        </span>
        <el-select
          v-model="pe['selectEnv']"
          filterable
          :placeholder="$t('workspace.env_group.please_select_env')"
          style="margin-top: 8px; width: 250px"
          size="small">
          <el-option
            v-for="(environment, index) in pe.envs"
            :key="index"
            :label="environment.name"
            :value="environment.id" />
          <el-button
            class="ms-scenario-button"
            v-if="isShowConfirmButton(pe.id)"
            size="mini"
            type="primary"
            @click="openEnvironmentConfig(pe.id, pe['selectEnv'])">
            {{ $t('api_test.environment.environment_config') }}
          </el-button>
          <template v-slot:empty>
            <div v-if="isShowConfirmButton(pe.id) && pe.envs.length === 0" class="empty-environment">
              <el-button
                class="ms-scenario-button"
                size="mini"
                type="primary"
                @click="openEnvironmentConfig(pe.id, pe['selectEnv'])">
                {{ $t('api_test.environment.environment_config') }}
              </el-button>
            </div>
          </template>
        </el-select>
      </span>
    </div>
    <div v-show="data.length > 0">
      <span @click="active" @click.stop>
        <el-tooltip>
          <i class="header-icon el-icon-info" />
          <div slot="content">
            <div style="width: 560px">{{ $t('automation.project_env_info_tips') }}</div>
          </div>
        </el-tooltip>
        {{ $t('automation.project_env_info') }}
        <i class="icon el-icon-arrow-right" :class="{ 'is-active': isActive }" @click="active" @click.stop />
      </span>

      <el-collapse-transition>
        <div v-if="isActive">
          <div v-for="pe in data" :key="pe.id" style="margin-left: 20px">
            <span :v-if="pe.id !== currentProjectID">
              <span class="project-name" :title="getProjectName(pe.id)">
                {{ getProjectName(pe.id) }}
              </span>
              <el-select
                v-model="pe['selectEnv']"
                filterable
                :placeholder="$t('workspace.env_group.please_select_env')"
                style="margin-top: 8px; width: 250px"
                clearable
                size="small">
                <el-option
                  v-for="(environment, index) in pe.envs"
                  :key="index"
                  :label="environment.name"
                  :value="environment.id" />
                <el-button
                  class="ms-scenario-button"
                  v-if="isShowConfirmButton(pe.id)"
                  size="mini"
                  type="primary"
                  @click="openEnvironmentConfig(pe.id, pe['selectEnv'])">
                  {{ $t('api_test.environment.environment_config') }}
                </el-button>
                <template v-slot:empty>
                  <!--这里只做没有可搜索内容时使用，否则如果没有符合搜索条件的，也会显示该项，与上面的btn重复显示 -->
                  <div v-if="isShowConfirmButton(pe.id) && pe.envs.length === 0" class="empty-environment">
                    <el-button
                      class="ms-scenario-button"
                      size="mini"
                      type="primary"
                      @click="openEnvironmentConfig(pe.id, pe['selectEnv'])">
                      {{ $t('api_test.environment.environment_config') }}
                    </el-button>
                  </div>
                </template>
              </el-select>
            </span>
          </div>
        </div>
      </el-collapse-transition>
    </div>

    <el-button type="primary" @click="handleConfirm" size="small" :style="btnStyle" class="env-confirm">
      {{ $t('workspace.env_group.confirm') }}
    </el-button>

    <!-- 环境配置 -->
    <api-environment-config ref="environmentConfig" @saveRefresh="environmentConfigClose" />
  </div>
</template>

<script>
import ApiEnvironmentConfig from 'metersphere-frontend/src/components/environment/ApiEnvironmentConfig';
import { getEnvironmentByProjectIds } from 'metersphere-frontend/src/api/environment';
import { getOwnerProjectIds } from '@/api/project';
import { getCurrentProjectID } from 'metersphere-frontend/src/utils/token';

export default {
  name: 'EnvironmentSelect',
  components: { ApiEnvironmentConfig },
  props: {
    envMap: Map,
    projectIds: Set,
    projectList: Array,
    showConfigButtonWithOutPermission: {
      type: Boolean,
      default() {
        return true;
      },
    },
    result: {
      type: Object,
      default() {
        return { loading: false };
      },
    },
    btnStyle: {
      type: Object,
      default() {
        return { width: '360px' };
      },
    },
  },
  data() {
    return {
      data: [],
      projects: [],
      environments: [],
      permissionProjectIds: [],
      dialogVisible: false,
      isFullUrl: true,
      currentProjectEnv: [],
      isActive: false,
    };
  },
  methods: {
    active() {
      this.isActive = !this.isActive;
    },
    currentProjectID() {
      return getCurrentProjectID();
    },
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
      this.currentProjectEnv = [];
      this.projectIds.forEach((id) => {
        const project = this.projectList.find((p) => p.id === id);
        if (project) {
          let item = { id: id, envs: [], selectEnv: '' };
          if (this.currentProjectID() === project.id) {
            this.currentProjectEnv.push(item);
          } else {
            this.data.push(item);
          }
        }
      });
      let p = new Promise((resolve) => {
        getEnvironmentByProjectIds(Array.from(this.projectIds)).then((res) => {
          let envMap = new Map();
          res.data.forEach((environment) => {
            envMap.has(environment.projectId)
              ? envMap.set(environment.projectId, envMap.get(environment.projectId).concat(environment))
              : envMap.set(environment.projectId, [environment]);
          });
          envMap.forEach((value, key) => {
            // 固定环境列表渲染顺序
            let temp =
              this.currentProjectID() === key
                ? this.currentProjectEnv.find((dt) => dt.id === key)
                : this.data.find((dt) => dt.id === key);
            temp.envs = envMap.get(key);
            let envId = undefined;
            if (this.envMap) {
              envId = this.envMap.get(key);
            }
            // 选中环境是否存在
            temp.selectEnv = envMap.get(key).filter((e) => e.id === envId).length === 0 ? null : envId;
          });
          resolve();
        });
      });
      arr.push(p);
      return arr;
    },
    getUserPermissionProjectIds() {
      getOwnerProjectIds().then((res) => {
        this.permissionProjectIds = res.data;
      });
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
      if (id === getCurrentProjectID()) {
        return this.$t('commons.current_project');
      }
      const project = this.projectList.find((p) => p.id === id);
      return project ? project.name : '';
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
      this.currentProjectEnv.forEach((dt) => {
        if (!dt.selectEnv) {
          return;
        }
        map.set(dt.id, dt.selectEnv);
      });
      // 自定义项目环境
      this.data.forEach((dt) => {
        if (!dt.selectEnv) {
          return;
        }
        map.set(dt.id, dt.selectEnv);
      });
      this.$emit('setProjectEnvMap', map);
      this.$emit('close');
    },
    checkEnv(data) {
      this.isFullUrl = true;
      if (data) {
        return true;
      }
      if (this.currentProjectEnv.length > 0) {
        this.currentProjectEnv.forEach((dt) => {
          if (!dt.selectEnv) {
            return false;
          }
        });
      } else {
        // 如果有环境，检查环境
        if (this.envMap && this.envMap.size > 0) {
          this.projectIds.forEach((id) => {
            if (!this.envMap.get(id)) {
              return false;
            }
          });
        }
      }
      return true;
    },
    environmentConfigClose() {
      this.$emit('saveRefresh');
    },
  },
};
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

:deep(.el-collapse-item__arrow) {
  margin: 0 0px 0px 8px;
}

.project-name {
  display: inline-block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  width: 100px;
  vertical-align: middle;
}

.icon.is-active {
  transform: rotate(90deg);
}

.el-icon-arrow-right {
  margin-right: 3px;
}
</style>
