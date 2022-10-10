<template>
  <el-dialog :close-on-click-modal="false" :title="'测试结果'" width="60%"
             :visible.sync="visible" class="api-import" @close="close">
    <micro-app route-name="ApiReportView"
               service="api"
               :route-params="{response}"/>
  </el-dialog>
</template>

<script>
import MicroApp from "metersphere-frontend/src/components/MicroApp";

export default {
  name: "TestPlanApiCaseResult",
  components: {MicroApp},
  props: ['response'],
  data() {
    return {
      visible: false
    }
  },
  methods: {
    close() {
      this.visible = false;
    },
    open() {
      this.visible = true;
    }
  }
}
</script>

<style scoped>

</style>
