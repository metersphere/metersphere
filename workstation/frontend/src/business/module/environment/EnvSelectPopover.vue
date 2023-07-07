<template>
  <div>
    <el-radio-group v-model="radio" style="width: 100%" @change="radioChange" class="radio-change">
      <el-radio :label="ENV_TYPE.JSON">{{ $t('workspace.env_group.env_list') }}</el-radio>
      <el-radio :label="ENV_TYPE.GROUP"  v-if="isScenario">{{ $t('workspace.env_group.name') }}<i class="el-icon-tickets mode-span" @click="viewGroup"></i></el-radio>
    </el-radio-group>
    <div v-for="(pe, pIndex) in eventData" :key="pe.id" v-show="!radio || radio === ENV_TYPE.JSON">
      <el-card shadow="never" style="margin-top: 8px; background: #f5f6f7; border-radius: 4px">
        <i
          @click="expandCard(pIndex)"
          v-if="pe.expendStatus === 'close'"
          class="el-icon-caret-right"
          style="color: var(--primary_color)" />
        <i @click="expandCard(pIndex)" v-else class="el-icon-caret-bottom" style="color: var(--primary_color)" />
        <span class="project-name" :title="getProjectName(pe.id)"> {{ getProjectName(pe.id) }} </span><br />
        <div v-if="pe.expendStatus === 'open'">
          <el-radio-group
            v-model="pe.envRadio"
            style="width: 100%"
            @change="envRadioChange(pe.envRadio, pIndex)"
            class="radio-change">
            <el-radio label="DEFAULT_ENV" style="margin-top: 7px">{{
              $t('api_test.environment.default_environment')
            }}</el-radio>
            <el-radio label="CUSTOMIZE_ENV" style="margin-top: 7px">{{
              $t('api_test.environment.choose_new_environment')
            }}</el-radio>
          </el-radio-group>
          <el-tag
            v-show="!pe.showEnvSelect"
            v-for="(itemName, index) in selectedEnvName.get(pe.id)"
            :key="index"
            size="mini"
            style="margin-left: 0; margin-right: 2px; margin-top: 8px"
            >{{ itemName }}</el-tag
          >
          <el-select
            v-show="pe.showEnvSelect"
            v-model="pe['selectEnv']"
            :placeholder="$t('api_test.environment.select_environment')"
            style="margin-top: 8px; width: 100%"
            size="small"
            filterable
            @change="chooseEnv">
            <el-option
              v-for="(environment, index) in pe.envs"
              :key="index"
              :label="environment.name"
              :value="environment.id" />
          </el-select>
        </div>
      </el-card>
    </div>
    <div v-show="radio === ENV_TYPE.GROUP ">
      <div >
        <el-select  v-show="!hasOptionGroup" v-model="envGroupId" :placeholder="$t('workspace.env_group.select')" @change="chooseEnvGroup"
                   style="margin-top: 8px;width: 100%;" size="small">
          <el-option v-for="(group, index) in groups" :key="index"
                     :disabled="group.disabled"
                     :label="group.name"
                     :value="group.id"/>
        </el-select>
        <el-select
          v-show="hasOptionGroup"
          v-model="envGroupId"
          :placeholder="$t('workspace.env_group.select')"
          style="margin-top: 8px;width: 100%;"
          size="small"
          @change="chooseEnvGroup"
          clearable>
          <el-option-group v-for="group in groups" :key="group.label" :label="group.label">
            <el-option
              v-for="item in group.options"
              :key="item.name"
              :label="item.name"
              :disabled="item.disabled"
              :value="item.id">
            </el-option>
          </el-option-group>
        </el-select>
      </div>
      <el-dialog :visible="visible" append-to-body :title="$t('workspace.env_group.name')" @close="visible = false"
                 style="height: 800px;">
        <template>
          <environment-group style="overflow-y: auto;"
                             :screen-height="'350px'"
                             :read-only="true"
          ></environment-group>
        </template>
      </el-dialog>
    </div>
    <!-- 对环境组选项进行分类 可用｜不可用 -->

  </div>
</template>

<script>
import { ENV_TYPE } from 'metersphere-frontend/src/utils/constants';
import { environmentGetALL,getEnvironmentOptions } from 'metersphere-frontend/src/api/environment';
import MsTag from 'metersphere-frontend/src/components/MsTag';
import { parseEnvironment } from 'metersphere-frontend/src/model/EnvironmentModel';
import { getEnvironments } from '@/api/environment';
import EnvironmentGroup from './common/EnvironmentGroupList';
import EnvGroupWithOption from "./common/EnvGroupWithOption";

export default {
  name: 'EnvSelectPopover',
  components: { EnvironmentGroup,MsTag,EnvGroupWithOption},
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
  computed: {
    ENV_TYPE() {
      return ENV_TYPE;
    },
  },
  props: {
    projectIds: Set,
    projectList: Array,
    projectEnvMap: Object,
    caseIdEnvNameMap: Object,
    envMap: Map,
    environmentType: String,
    groupId: {
      type: String,
      default() {
        return '';
      },
    },
    isScenario: {
      type: Boolean,
      default: true,
    },
    hasOptionGroup: {
      type: Boolean,
      default() {
        return false;
      },
    },
    btnStyle: {
      type: Object,
      default() {
        return { width: '360px' };
      },
    },
  },
  watch: {
    groupId(val) {
      this.envGroupId = val;
    },
    environmentType(val) {
      this.radio = val;
    },
  },
  methods: {
    open() {
      this.envGroupId = this.groupId;
      this.initDefaultEnv();
      this.getgroups();
    },
    chooseEnvGroup(envGroupId){
      this.$emit("setEnvGroup", envGroupId);
    },
    radioChange(val) {
      this.$emit('update:environmentType', val);
    },
    getProjectName(id) {
      const project = this.projectList.find((p) => p.id === id);
      return project ? project.name : '';
    },
    envRadioChange(val, index) {
      this.eventData[index].envRadio = val;
      this.eventData[index].showEnvSelect = this.eventData[index].envRadio === 'CUSTOMIZE_ENV';
    },
    viewGroup() {
      this.visible = true;
    },
    getgroups() {
      if (this.hasOptionGroup) {
        getEnvironmentOptions({
          projectIds: [...this.projectIds],
        }).then((res) => {
          let groups = res.data;
          this.disabledGroups = groups.filter((group) => group.disabled === true);
          this.notDisabledGroups = groups.filter((group) => group.disabled === false);
          this.$set(this.groups, 0, {
            label: this.$t('workspace.env_group.available_group'),
            options: this.notDisabledGroups,
          });
          this.$set(this.groups, 1, {
            label: this.$t('workspace.env_group.not_available_group'),
            options: this.disabledGroups,
          });
        });
      } else {
        environmentGetALL().then((res) => {
          let data = res.data;
          this.groups = data ? data : [];
        });
      }
    },
    chooseEnv(val) {
      let filter = this.evnList.filter((e) => e.id === val);
      this.selectEnvMap.set(filter[0].projectId, val);
      this.$emit('setProjectEnvMap', this.selectEnvMap);
    },
    initDefaultEnv() {
      this.selectedEnvName = new Map();
      this.evnList = [];
      this.projectIds.forEach((d) => {
        let item = {
          id: d,
          envs: [],
          selectEnv: '',
          envRadio: 'DEFAULT_ENV',
          showEnvSelect: false,
          expendStatus: 'open',
        };
        this.eventData.push(item);
        getEnvironments(d).then((res) => {
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
            temp.selectEnv = envs.filter((e) => e.id === envId).length === 0 ? null : envId;
          }
          if (this.isScenario) {
            if (this.projectEnvMap) {
              let projectEnvMapElement = this.projectEnvMap[d];
              if (projectEnvMapElement && projectEnvMapElement.length > 0) {
                projectEnvMapElement.forEach((envId) => {
                  let filter = envs.filter((e) => e.id === envId);
                  if (!this.selectedEnvName.has(d)) {
                    let name = [];
                    name.push(filter[0].name);
                    this.selectedEnvName.set(d, name);
                  } else {
                    this.selectedEnvName.get(d).push(filter[0].name);
                  }
                });
              }
            }
          } else {
            if (this.caseIdEnvNameMap) {
              let envName = new Set();
              for (let key in this.caseIdEnvNameMap) {
                envName.add(this.caseIdEnvNameMap[key]);
              }
              this.selectedEnvName.set(d, envName);
            }
          }
        });
      });
    },
    expandCard(index) {
      if (this.eventData[index].expendStatus === 'open') {
        this.eventData[index].expendStatus = 'close';
      } else {
        this.eventData[index].expendStatus = 'open';
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
