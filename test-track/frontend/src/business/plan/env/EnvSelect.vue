<template>
  <div v-loading="result.loading">
    <div v-for="pe in data" :key="pe.id" style="margin-left: 20px">
      <el-select
        v-model="pe['selectEnv']"
        filterable
        :placeholder="$t('api_test.environment.select_environment')"
        style="margin-top: 8px; width: 200px"
        size="small"
      >
        <el-option
          v-for="(environment, index) in pe.envs"
          :key="index"
          :label="environment.name"
          :value="environment.id"
        />
        <el-button
          class="ms-scenario-button"
          v-if="isShowConfirmButton(pe.id)"
          size="mini"
          type="primary"
          @click="openEnvironmentConfig(pe.id)"
        >
          {{ $t("api_test.environment.environment_config") }}
        </el-button>
        <template v-slot:empty>
          <!--这里只做没有可搜索内容时使用，否则如果没有符合搜索条件的，也会显示该项，与上面的btn重复显示 -->
          <div
            v-if="isShowConfirmButton(pe.id) && pe.envs.length === 0"
            class="empty-environment"
          >
            <el-button
              class="ms-scenario-button"
              size="mini"
              type="primary"
              @click="openEnvironmentConfig(pe.id)"
            >
              {{ $t("api_test.environment.environment_config") }}
            </el-button>
          </div>
        </template>
      </el-select>
      <span class="project-name" :title="getProjectName(pe.id)">
        {{ getProjectName(pe.id) }}
      </span>
    </div>

    <el-button
      type="primary"
      @click="handleConfirm"
      size="small"
      class="env-confirm"
      >{{ $t("commons.confirm") }}
    </el-button>

    <!-- 环境配置 -->
    <api-environment-config
      ref="environmentConfig"
      @close="environmentConfigClose"
    />
  </div>
</template>

<script>
import { parseEnvironment } from "metersphere-frontend/src/model/EnvironmentModel";
import ApiEnvironmentConfig from "metersphere-frontend/src/components/environment/ApiEnvironmentConfig";
import { getOwnerProjectIds } from "@/api/project";
import { getEnvironmentByProjectId } from "@/api/remote/api/api-environment";

export default {
  name: "EnvSelect",
  components: {
    ApiEnvironmentConfig,
  },
  props: {
    envMap: Map,
    projectIds: Set,
    showConfigButtonWithOutPermission: {
      type: Boolean,
      default() {
        return true;
      },
    },
    projectList: Array,
  },
  data() {
    return {
      data: [],
      result: { loading: false },
      projects: [],
      environments: [],
      permissionProjectIds: [],
      dialogVisible: false,
      isFullUrl: true,
    };
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

      this.projectIds.forEach((id) => {
        const project = this.projectList.find((p) => p.id === id);
        if (project) {
          let item = { id: id, envs: [], selectEnv: "" };
          this.data.push(item);
          this.result.loading = true;
          getEnvironmentByProjectId(id).then((res) => {
            this.result.loading = false;
            let envs = res.data;
            envs.forEach((environment) => {
              parseEnvironment(environment);
            });
            // 固定环境列表渲染顺序
            let temp = this.data.find((dt) => dt.id === id);
            temp.envs = envs;
            if (this.envMap && this.envMap.size > 0) {
              let envId = this.envMap.get(id);
              // 选中环境是否存在
              temp.selectEnv =
                envs.filter((e) => e.id === envId).length === 0 ? null : envId;
            }
          });
        }
      });
    },
    open() {
      this.data = [];
      if (this.projectIds.size > 0) {
        this.init();
      }
    },
    getProjectName(id) {
      const project = this.projectList.find((p) => p.id === id);
      return project ? project.name : "";
    },
    openEnvironmentConfig(projectId) {
      if (!projectId) {
        this.$error(this.$t("api_test.select_project"));
        return;
      }
      this.$refs.environmentConfig.open(projectId);
    },
    handleConfirm() {
      let map = new Map();
      let projectEnvDesc = {};
      let selectEnv = this.data.filter((dt) => dt.selectEnv);
      if (selectEnv.length === 0) {
        this.$warning("请为当前场景选择一个运行环境！");
        return false;
      }
      this.data.forEach((dt) => {
        map.set(dt.id, dt.selectEnv);
        let filteredEnv = dt.envs.filter((e) => e.id === dt.selectEnv);
        if (filteredEnv.length > 0) {
          projectEnvDesc[this.getProjectName(dt.id)] = filteredEnv[0].name;
        }
      });
      this.$emit("setProjectEnvMap", map, projectEnvDesc);
      this.$emit("close");
    },
    checkEnv() {
      let sign = true;
      this.isFullUrl = true;
      if (this.data.length > 0) {
        this.data.forEach((dt) => {
          if (!dt.selectEnv) {
            sign = false;
            return false;
          }
        });
      } else {
        // 如果有环境，检查环境
        if (this.envMap && this.envMap.size > 0) {
          this.projectIds.forEach((id) => {
            if (!this.envMap.get(id)) {
              sign = false;
              return false;
            }
          });
        }
      }
      return true;
    },
    environmentConfigClose() {
      // todo 关闭处理
    },
    getUserPermissionProjectIds() {
      getOwnerProjectIds().then((res) => {
        this.permissionProjectIds = res.data;
      });
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
  width: 90%;
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
