<template>
  <el-popover
    v-model="visible"
    placement="bottom"
    width="400"
    @show="showPopover"
    trigger="click">
    <env-select :project-ids="projectIds" :env-map="envMap" @close="visible = false"
                ref="envSelect" @setProjectEnvMap="setProjectEnvMap" :project-list="projectList"/>
    <el-button type="primary" slot="reference" size="mini" style="margin-top: 2px;">
      {{ $t('api_test.definition.request.run_env') }}
      <i class="el-icon-caret-bottom el-icon--right"></i>
    </el-button>
  </el-popover>
</template>

<script>
import EnvSelect from "@/business/components/api/automation/scenario/EnvSelect";

export default {
  name: "EnvPopover",
  components: {EnvSelect},
  props: {
    envMap: Map,
    projectIds: Set,
    projectList: Array,
  },
  data() {
    return {
      visible: false
    }
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

}
</script>

<style scoped>

</style>
