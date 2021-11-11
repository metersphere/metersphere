<template>
  <el-popover
    v-model="visible"
    :placement="placement"
    width="400"
    :disabled="isReadOnly"
    @show="showPopover"
    trigger="click">
    <el-radio-group v-model="radio" style="margin-left: 20px;" @change="radioChange">
      <el-radio :label="ENV_TYPE.JSON">环境列表</el-radio>
      <el-radio :label="ENV_TYPE.GROUP">环境组</el-radio>
    </el-radio-group>
    <env-select :project-ids="projectIds"
                :result="result"
                :show-config-button-with-out-permission="showConfigButtonWithOutPermission"
                :env-map="envMap"
                :project-list="projectList"
                @close="visible = false"
                @setProjectEnvMap="setProjectEnvMap"
                v-show="!radio || radio === ENV_TYPE.JSON"
                ref="envSelect"/>
    <!-- todo 如果工作空间下的环境组都不包含当前项目则不显示 -->
    <env-group ref="envGroup" v-show="radio === ENV_TYPE.GROUP" @close="visible = false" :project-ids="projectIds"
               @setEnvGroup="setEnvGroup" :group-id="groupId"></env-group>
    <el-button type="primary" slot="reference" size="mini" style="margin-top: 2px;">
      {{ $t('api_test.definition.request.run_env') }}
      <i class="el-icon-caret-bottom el-icon--right"></i>
    </el-button>
  </el-popover>
</template>

<script>
import EnvSelect from "@/business/components/api/automation/scenario/EnvSelect";
import {ENV_TYPE} from "@/common/js/constants";
import EnvGroup from "@/business/components/api/automation/scenario/EnvGroup";

export default {
  name: "EnvPopover",
  components: {EnvGroup, EnvSelect},
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
    isReadOnly: {
      type: Boolean,
      default() {
        return false;
      }
    },
    result: {
      type: Object,
      default() {
        return {loading: false}
      }
    },
    groupId: {
      type: String,
      default() {
        return "";
      }
    },
    environmentType: String,
    isScenario: {
      type: Boolean,
      default() {
        return true;
      }
    },
    placement: {
      type: String,
      default() {
        return "bottom";
      }
    }
  },
  data() {
    return {
      visible: false,
      radio: this.environmentType,
    }
  },
  watch: {
    environmentType(val) {
      this.radio = val;
    }
  },
  computed: {
    ENV_TYPE() {
      return ENV_TYPE;
    }
  },
  methods: {
    showPopover() {
      if (this.isScenario) {
        this.$emit("showPopover");
      } else {
        this.$refs.envSelect.open();
      }
    },
    openEnvSelect() {
      this.$refs.envSelect.open();
      this.$refs.envGroup.init();
      this.$refs.envGroup.open();
    },
    setProjectEnvMap(map) {
      this.$emit("setProjectEnvMap", map);
    },
    setEnvGroup(envGroupId) {
      this.$emit("setEnvGroup", envGroupId);
    },
    initEnv() {
      return this.$refs.envSelect.initEnv();
    },
    checkEnv(data) {
      return new Promise((resolve => {
        if (this.environmentType === ENV_TYPE.JSON) {
          let res = this.$refs.envSelect.checkEnv(data);
          resolve(res);
        } else if (this.environmentType === ENV_TYPE.GROUP) {
          let res = this.$refs.envGroup.checkEnv();
          res.then(r => {
            resolve(r);
          })
        }
      }))
    },
    radioChange(val) {
      this.$emit("update:environmentType", val);
    }
  }

}
</script>

<style scoped>

</style>
