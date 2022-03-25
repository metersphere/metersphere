<template>
  <el-card>
    <el-tabs class="system-setting" v-model="activeName">

      <el-tab-pane :label="$t('api_test.environment.environment_list')" name="list">
        <environment-list v-if="activeName === 'list'"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('workspace.env_group.name')" name="group">
        <environment-group-list v-if="activeName === 'group'"/>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script>
import EnvironmentList from "@/business/components/settings/workspace/environment/EnvironmentList";
import EnvironmentGroupList from "@/business/components/settings/workspace/environment/EnvironmentGroupList";
export default {
  name: "EnvironmentManage",
  components: {
    EnvironmentList,
    EnvironmentGroupList
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
