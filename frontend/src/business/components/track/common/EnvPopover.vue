<template>
  <el-popover
    v-model="visible"
    placement="bottom"
    width="400"
    :disabled="isReadOnly"
    @show="showPopover"
    trigger="click">
    <env-select :project-ids="projectIds" :env-map="envMap" @close="visible = false"
                :show-config-button-with-out-permission="showConfigButtonWithOutPermission"
                ref="envSelect" @setProjectEnvMap="setProjectEnvMap" :project-list="projectList"/>
    <el-button type="primary" slot="reference" size="mini" style="margin-top: 2px;">
      {{ $t('api_test.definition.request.run_env') }}
      <i class="el-icon-caret-bottom el-icon--right"></i>
    </el-button>
  </el-popover>
</template>

<script>
import EnvSelect from "@/business/components/track/common/EnvSelect";

export default {
  name: "EnvPopover",
  components: {EnvSelect},
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
    }
  },
  data() {
    return {
      visible: false
    };
  },
  methods: {
    showPopover() {
      this.$refs.envSelect.open();
    },
    setProjectEnvMap(map) {
      this.$emit("setProjectEnvMap", map);
    },
    checkEnv() {
      return this.$refs.envSelect.checkEnv();
    }
  }

};
</script>

<style scoped>

</style>
