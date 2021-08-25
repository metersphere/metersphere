<template>
  <el-popover
    v-model="visible"
    placement="bottom-start"
    width="400"
    :disabled="isReadOnly"
    @show="showPopover"
    trigger="click">
    <env-select :project-ids="projectIds"
                :result="result"
                :project-env-map="projectEnvMap"
                :project-list="projectList"
                :show-config-button-with-out-permission="showConfigButtonWithOutPermission"
                @close="visible = false"
                ref="envSelect"
                @setProjectEnvMap="setProjectEnvMap"/>
    <el-button type="primary" slot="reference" size="mini" style="margin-top: 2px;">
      {{ $t('api_test.definition.request.run_env') }}
      <i class="el-icon-caret-bottom el-icon--right"></i>
    </el-button>
  </el-popover>
</template>

<script>
import EnvSelect from "@/business/components/track/plan/common/EnvSelect";

export default {
  name: "EnvPopover",
  components: {EnvSelect},
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
    }
  },
  data() {
    return {
      visible: false
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
    initEnv() {
      return this.$refs.envSelect.initEnv();
    },
    checkEnv(data) {
      return this.$refs.envSelect.checkEnv(data);
    }
  }

}
</script>

<style scoped>

</style>
