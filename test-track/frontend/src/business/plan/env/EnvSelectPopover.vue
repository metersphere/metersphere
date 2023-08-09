<template>
  <div>
    <!--    {{ JSON.stringify(eventData) }}-->
    <el-radio-group
      v-model="radio"
      style="width: 100%"
      @change="radioChange"
      class="radio-change"
    >
      <el-radio :label="ENV_TYPE.JSON">{{
          $t("workspace.env_group.env_list")
        }}
      </el-radio>
      <el-radio :label="ENV_TYPE.GROUP" v-if="showEnvGroup"
      >{{
          $t("workspace.env_group.name")
        }}<i class="el-icon-tickets mode-span" @click="viewGroup"></i
        ></el-radio>
    </el-radio-group>
    <div
      v-for="(pe, pIndex) in eventData"
      :key="pe.id"
      v-show="radio === ENV_TYPE.JSON"
    >
      <el-card
        shadow="never"
        style="margin-top: 8px; background: #f5f6f7; border-radius: 4px"
      >
        <i
          @click="expandCard(pIndex)"
          v-if="pe.expendStatus === 'close'"
          class="el-icon-caret-right"
          style="color: var(--primary_color)"
        />
        <i
          @click="expandCard(pIndex)"
          v-else
          class="el-icon-caret-bottom"
          style="color: var(--primary_color)"
        />
        <span class="project-name" :title="getProjectName(pe.id)">
          {{ getProjectName(pe.id) }} </span
        ><br/>
        <div v-if="pe.expendStatus === 'open'">
          <el-radio-group
            v-model="pe.envRadio"
            style="width: 100%"
            @change="envRadioChange(pe.envRadio, pIndex)"
            class="radio-change"
          >
            <el-radio label="DEFAULT_ENV" style="margin-top: 7px">{{
                $t("api_test.environment.default_environment")
              }}
            </el-radio>
            <el-radio label="CUSTOMIZE_ENV" style="margin-top: 7px">{{
                $t("api_test.environment.choose_new_environment")
              }}
            </el-radio>
          </el-radio-group>
          <div v-if="isEnvSaved">
            <el-tag
              v-show="!pe.showEnvSelect"
              v-for="(itemName, index) in selectedEnvName.get(pe.id)"
              :key="index"
              size="mini"
              style="margin-left: 0; margin-right: 2px; margin-top: 8px"
            >
              {{ itemName }}
            </el-tag>
          </div>
          <el-select
            v-show="pe.showEnvSelect"
            v-model="pe['selectEnv']"
            filterable
            :placeholder="$t('api_test.environment.select_environment')"
            style="margin-top: 8px; width: 100%"
            size="small"
            @change="chooseEnv"
          >
            <el-option
              v-for="(environment, index) in pe.envs"
              :key="index"
              :label="environment.name"
              :value="environment.id"
            />
          </el-select>
        </div>
      </el-card>
    </div>
    <div v-show="radio === ENV_TYPE.GROUP">
      <div>
        <el-select
          v-show="!hasOptionGroup"
          v-model="envGroupId"
          :placeholder="$t('workspace.env_group.select')"
          @change="chooseEnvGroup"
          style="margin-top: 8px; width: 100%"
          size="small"
        >
          <el-option
            v-for="(group, index) in groups"
            :key="index"
            :label="group.name"
            :value="group.id"
          />
        </el-select>
        <el-select
          v-show="hasOptionGroup"
          v-model="envGroupId"
          :placeholder="$t('workspace.env_group.select')"
          style="margin-top: 8px; width: 100%"
          @change="chooseEnvGroup"
          size="small"
          clearable
        >
          <el-option-group
            v-for="group in groups"
            :key="group.label"
            :label="group.label"
          >
            <el-option
              v-for="item in group.options"
              :key="item.name"
              :label="item.name"
              :disabled="item.disabled"
              :value="item.id"
            >
            </el-option>
          </el-option-group>
        </el-select>
      </div>
      <el-dialog
        :visible="visible"
        append-to-body
        :title="$t('workspace.env_group.name')"
        @close="visible = false"
        style="height: 800px"
      >
        <template>
          <environment-group
            style="overflow-y: auto"
            :screen-height="'350px'"
            :read-only="true"
          ></environment-group>
        </template>
      </el-dialog>
    </div>
  </div>
</template>

<script>
import {ENV_TYPE} from "metersphere-frontend/src/utils/constants";
import {environmentGetALL, getEnvironmentOptions,} from "metersphere-frontend/src/api/environment";
import MsTag from "metersphere-frontend/src/components/MsTag";
import EnvironmentGroup from "@/business/plan/env/EnvironmentGroupList";
import {getEnvironmentByProjectId} from "@/api/remote/api/api-environment";
import {parseEnvironment} from "metersphere-frontend/src/model/EnvironmentModel";

export default {
  name: "EnvSelectPopover",
  components: {MsTag, EnvironmentGroup},
  data() {
    return {
      radio: this.environmentType,
      visible: false,
      groups: [],
      disabledGroups: [],
      notDisabledGroups: [],
      selectedEnvName: new Map(),
      showEnvName: false,
      eventData: [],
      evnList: [],
      selectEnvMap: new Map(),
      envGroupId: this.groupId,
    };
  },
  watch: {
    groupId(val) {
      this.envGroupId = val;
    },
    environmentType(val) {
      this.radio = val;
    },
  },
  computed: {
    ENV_TYPE() {
      return ENV_TYPE;
    },
  },
  props: {
    showEnvGroup: {
      type: Boolean,
      default: true,
    },
    projectIds: Set,
    projectList: Array,
    //环境是否保存过
    isEnvSaved: {
      type: Boolean,
      default: true,
    },
    projectEnvMap: Object,
    envMap: Map,
    environmentType: String,
    groupId: {
      type: String,
      default() {
        return "";
      },
    },
    hasOptionGroup: {
      type: Boolean,
      default() {
        return false;
      },
    },
  },
  methods: {
    open() {
      this.envGroupId = this.groupId;
      this.initDefaultEnv();
      this.getgroups();
    },
    radioChange(val) {
      this.$emit("update:environmentType", val);
    },
    getProjectName(id) {
      const project = this.projectList.find((p) => p.id === id);
      return project ? project.name : "";
    },
    envRadioChange(val, index) {
      this.eventData[index].envRadio = val;
      this.eventData[index].showEnvSelect =
        this.eventData[index].envRadio === "CUSTOMIZE_ENV";
    },
    viewGroup() {
      this.visible = true;
    },
    getgroups() {
      if (!this.hasOptionGroup) {
        environmentGetALL().then((res) => {
          let data = res.data;
          this.groups = data ? data : [];
        });
      } else {
        getEnvironmentOptions({projectIds: [...this.projectIds]}).then(
          (res) => {
            let groups = res.data;
            this.disabledGroups = groups.filter(
              (group) => group.disabled === true
            );
            this.notDisabledGroups = groups.filter(
              (group) => group.disabled === false
            );
            this.$set(this.groups, 0, {
              label: this.$t("workspace.env_group.available_group"),
              options: this.notDisabledGroups,
            });
            this.$set(this.groups, 1, {
              label: this.$t("workspace.env_group.not_available_group"),
              options: this.disabledGroups,
            });
          }
        );
      }
    },
    chooseEnv(val) {
      let filter = this.evnList.filter((e) => e.id === val);
      this.selectEnvMap.set(filter[0].projectId, val);
      this.$emit("setProjectEnvMap", this.selectEnvMap);
    },
    chooseEnvGroup(envGroupId) {
      this.$emit("setEnvGroup", envGroupId);
    },
    initDefaultEnv() {
      this.selectedEnvName = new Map();
      let defaultEnv = new Map();
      this.evnList = [];
      this.projectIds.forEach((d) => {
        let item = {
          id: d,
          envs: [],
          selectEnv: "",
          envRadio: "DEFAULT_ENV",
          showEnvSelect: false,
          expendStatus: "open",
        };
        this.eventData.push(item);
        getEnvironmentByProjectId(d).then((res) => {
          let envs = res.data;
          envs.forEach((environment) => {
            parseEnvironment(environment);
          });
          // 固定环境列表渲染顺序
          let temp = this.eventData.find((dt) => dt.id === d);
          temp.envs = envs;
          envs.forEach((t) => {
            this.evnList.push(t);
          });
          if (this.envMap && this.envMap.size > 0) {
            let envId = this.envMap.get(id);
            // 选中环境是否存在
            temp.selectEnv =
              envs.filter((e) => e.id === envId).length === 0 ? null : envId;
          }
          if (
            this.projectEnvMap &&
            Object.keys(this.projectEnvMap).length > 0
          ) {
            let projectEnvMapElement = this.projectEnvMap[d];
            if (projectEnvMapElement.length > 0) {
              projectEnvMapElement.forEach((envId) => {
                let filteredEnv = envs.filter((e) => e.id === envId);
                if (!this.selectedEnvName.has(d)) {
                  let name = [];
                  if (filteredEnv.length > 0) {
                    name.push(filteredEnv[0].name);
                    this.selectedEnvName.set(d, name);
                    this.$emit("setDefaultEnv", d, filteredEnv[0].id);
                  }
                } else {
                  if (filteredEnv.length > 0) {
                    this.selectedEnvName.get(d).push(filteredEnv[0].name);
                    this.$emit("setDefaultEnv", d, filteredEnv[0].id);
                  }
                }
              });
            }
          }
        });
      });
    },
    expandCard(index) {
      if (this.eventData[index].expendStatus === "open") {
        this.eventData[index].expendStatus = "close";
      } else {
        this.eventData[index].expendStatus = "open";
      }
    },
  },
};
</script>

<style scoped>
.mode-span {
  margin-left: 6px;
}
</style>
<style lang="scss" scoped>
.radio-change:deep(.el-radio__input.is-checked + .el-radio__label) {
  color: #606266 !important;
}
</style>
