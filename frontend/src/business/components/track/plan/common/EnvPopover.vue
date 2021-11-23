<template>
  <el-popover
    v-model="visible"
    placement="bottom-start"
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
                :project-env-map="projectEnvMap"
                :project-list="projectList"
                :show-config-button-with-out-permission="showConfigButtonWithOutPermission"
                @close="visible = false"
                ref="envSelect"
                v-show="!radio || radio === ENV_TYPE.JSON"
                @setProjectEnvMap="setProjectEnvMap"/>
    <!-- todo 如果工作空间下的环境组都不包含当前项目则不显示 -->
    <env-group ref="envGroup" v-show="radio === ENV_TYPE.GROUP" @close="visible = false"
               @setEnvGroup="setEnvGroup" :group-id="groupId"></env-group>
    <el-button type="primary" slot="reference" size="mini" style="margin-top: 2px;">
      {{ $t('api_test.definition.request.run_env') }}
      <i class="el-icon-caret-bottom el-icon--right"></i>
    </el-button>
  </el-popover>
</template>

<script>
import EnvSelect from "@/business/components/track/plan/common/EnvSelect";
import {ENV_TYPE} from "@/common/js/constants";
import EnvGroup from "@/business/components/api/automation/scenario/EnvGroup";

export default {
  name: "EnvPopover",
  components: {EnvGroup, EnvSelect},
  props: {
    projectIds: Set,
    projectList: Array,
    showConfigButtonWithOutPermission: {
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
    projectEnvMap: {
      type: Object,
      default() {
        return {};
      }
    },
    groupId: {
      type: String,
      default() {
        return "";
      }
    },
    environmentType: String
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
      this.$emit("showPopover");
    },
    openEnvSelect() {
      return this.$refs.envSelect.open();
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
      return this.$refs.envSelect.checkEnv(data);
    },
    radioChange(val) {
      this.$emit("update:environmentType", val);
    }
  }

}
</script>

<style scoped>

</style>
