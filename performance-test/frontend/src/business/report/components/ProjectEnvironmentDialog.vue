<template>
  <el-dialog
      :title="$t('commons.environment')"
      :visible.sync="dialogVisible"
      width="30%"
      :append-to-body="true"
      :destroy-on-close="true"
      z-index="99998"
      :before-close="handleClose">
    <div>
      <div v-if="projectEnvMap" type="flex">
        <div v-for="(values,key) in projectEnvMap" :key="key" style="margin-right: 10px;margin-bottom: 10px">
          {{ key + ":" }}
          <ms-tag v-for="(item,index) in values" :key="index" type="success" :content="item"
                  style="margin-left: 2px"/>
        </div>
      </div>
    </div>
  </el-dialog>
</template>

<script>

import MsTag from "metersphere-frontend/src/components/MsTag";

export default {
  name: "ProjectEnvironmentDialog",
  components: {MsTag},
  data() {
    return {
      projectEnvMap: {},
      dialogVisible: false
    }
  },
  methods: {
    handleClose() {
      this.dialogVisible = false;
      this.projectEnvMap = {};
    },
    open(projectEnvMap) {
      this.dialogVisible = true;
      this.projectEnvMap = projectEnvMap;
    }

  }
}
</script>

<style scoped>

</style>
