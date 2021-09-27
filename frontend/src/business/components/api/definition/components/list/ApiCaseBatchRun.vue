<template>
  <ms-edit-dialog
    width="20%"
    :visible.sync="visible"
    :title="$t('test_track.plan.load_case.batch_exec_cases')"
    @confirm="save">
    <ms-environment-select
      :project-id="projectId"
      @setEnvironment="setEnvironment" ref="environmentSelect"/>
  </ms-edit-dialog>

</template>

<script>
import MsEditDialog from "@/business/components/common/components/MsEditDialog";
import MsEnvironmentSelect from "@/business/components/api/definition/components/case/MsEnvironmentSelect";
export default {
  name: "ApiCaseBatchRun",
  components: {MsEnvironmentSelect, MsEditDialog},
  props: ['projectId'],
  data() {
    return {
      visible: false,
      environment: {}
    }
  },
  methods: {
    setEnvironment(environment) {
      this.environment = environment;
    },
    open() {
      this.visible = true;
    },
    close() {
      this.visible = false;
    },
    save() {
      this.$emit('batchRun', this.environment);
      this.close();
    }
  }
}
</script>

<style scoped>

</style>
