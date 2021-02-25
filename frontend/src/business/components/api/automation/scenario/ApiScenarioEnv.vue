<template>
  <el-dialog
    title="环境选择"
    :visible.sync="dialogVisible"
    width="30%"
    :destroy-on-close="true"
    :before-close="handleClose">

    <div v-for="pe in data" :key="pe.id">
      <div>
        {{ getProjectName(pe.id) }}
        <el-select v-model="pe['selectEnv']" placeholder="请选择环境" style="margin-left:10px; margin-top: 10px;"
                   size="small">
          <el-option v-for="(environment, index) in pe.envs" :key="index"
                     :label="environment.name + (environment.config.httpConfig.socket ? (': ' + environment.config.httpConfig.protocol + '://' + environment.config.httpConfig.socket) : '')"
                     :value="environment.id"/>
          <el-button class="ms-scenario-button" size="mini" type="primary" @click="openEnvironmentConfig(pe.id)">
            {{ $t('api_test.environment.environment_config') }}
          </el-button>
          <template v-slot:empty>
            <div class="empty-environment">
              <el-button class="ms-scenario-button" size="mini" type="primary" @click="openEnvironmentConfig(pe.id)">
                {{ $t('api_test.environment.environment_config') }}
              </el-button>
            </div>
          </template>
        </el-select>
      </div>
    </div>

    <span slot="footer" class="dialog-footer">
    <el-button @click="dialogVisible = false" size="small">取 消</el-button>
    <el-button type="primary" @click="handleConfirm" size="small">确 定</el-button>
  </span>
    <api-environment-config ref="environmentConfig" @close="environmentConfigClose"/>
  </el-dialog>
</template>

<script>
import {parseEnvironment} from "@/business/components/api/test/model/EnvironmentModel";
import ApiEnvironmentConfig from "@/business/components/api/definition/components/environment/ApiEnvironmentConfig";
export default {
  name: "ApiScenarioEnv",
  components: {ApiEnvironmentConfig},
  props: {
    projectIds: Set,
    envMap: Map
  },
  data() {
    return {
      dialogVisible: false,
      projects: [],
      data: [],
      environmentId: '',
      environments: []
    }
  },
  created() {
    this.getWsProjects();
  },
  methods: {
    handleClose() {
      this.dialogVisible = false;
    },
    init() {
      this.projectIds.forEach(id => {
        this.result = this.$get('/api/environment/list/' + id, res => {
          let envs = res.data;
          envs.forEach(environment => {
            parseEnvironment(environment);
          });
          let item = {};
          item.id = id;
          item.envs = envs;
          item.selectEnv = this.envMap.get(id);
          this.data.push(item)
        })
      })
    },
    open() {
      this.data = [];
      this.dialogVisible = true;
      if (this.projectIds.size > 0) {
        this.init();
      }
    },
    getWsProjects() {
      this.$get("/project/listAll", res => {
        this.projects = res.data;
      })
    },
    getProjectName(id) {
      const project = this.projects.find(p => p.id === id);
      return project ? project.name : "";
    },
    openEnvironmentConfig(projectId) {
      if (!projectId) {
        this.$error(this.$t('api_test.select_project'));
        return;
      }
      this.$refs.environmentConfig.open(projectId);
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
      this.dialogVisible = false;
    },
    checkEnv() {
      let sign = true;
      if (this.data.length > 0) {
        this.data.forEach(dt => {
          if (!dt.selectEnv) {
            sign = false;
            return;
          }
        })
      } else {
        sign = false;
      }

      if (!sign) {
        this.$warning("请为每个项目选择一个运行环境！");
        return false;
      }
      return true;
    },
    environmentConfigClose(id) {
      // todo
    }
  }
}
</script>

<style scoped>

</style>
