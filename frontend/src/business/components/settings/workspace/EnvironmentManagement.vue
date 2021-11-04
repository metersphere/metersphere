<template>
  <el-card>
    <el-tabs class="system-setting" v-model="activeName">

      <el-tab-pane :label="$t('api_test.environment.environment_list')" name="list">
        <ws-environment-list/>
      </el-tab-pane>
      <el-tab-pane :label="$t('api_test.environment.environment_group')" name="group">
        <ws-environment-group/>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script>
import WsEnvironmentList from "@/business/components/settings/workspace/WsEnvironmentList";
import WsEnvironmentGroup from "@/business/components/settings/workspace/WsEnvironmentGroup";

export default {
  name: "EnvironmentManagement",
  components: {
    WsEnvironmentList , WsEnvironmentGroup
  },
  data() {
    return {
      activeName: 'list',
    };
  },
};
</script>

<style scoped>

</style>
