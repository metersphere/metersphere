<template>
  <el-card>
    <el-tabs class="system-setting" v-model="activeName" @tab-click="handleClick">
      <el-tab-pane :label="$t('api_test.environment.environment_list')" name="list">
        <environment-list ref="environmentList"/>
      </el-tab-pane>
      <el-tab-pane :label="$t('workspace.env_group.name')" name="group" lazy>
        <environment-group-list ref="environmentGroupList"/>
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
  methods: {
    handleClick({paneName}) {
      if (paneName === 'list') {
        this.$refs.environmentList.list();
      } else if (paneName === 'group') {
        this.$nextTick(() => {
          this.$refs.environmentGroupList.init();
        })
      }
    }
  }
};
</script>

<style scoped>
</style>
