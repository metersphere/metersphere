<template>
  <div v-loading="result.loading">
    <el-card class="setting-card">
      <div class="setting-div">
        <el-card style="height: 100%;width: 100%">
          <div slot="header" style="padding: 5px 0">
            <span style="font-size: 18px;font-weight: bold;">{{ $t('commons.system_data') }}</span>
          </div>
          <div style="position: absolute; top: 50%; left: 50%;transform: translate(-50%, -50%);">
            <div class="div-item">
              <i class="el-icon-user-solid icon-color"></i>
              {{ $t('commons.system_user') }} <span class="number"> {{ statistic.userSize }} </span>
              {{ $t('commons.user_unit') }}
            </div>
            <div class="div-item">
              <i class="el-icon-s-platform icon-color"></i>
              {{ $t('commons.system_workspace') }} <span class="number"> {{ statistic.workspaceSize }} </span>
              {{ $t('commons.workspace_unit') }}
            </div>
            <div class="div-item">
              <i class="el-icon-s-cooperation icon-color"></i>
              {{ $t('commons.system_project') }} <span class="number"> {{ statistic.projectSize }} </span>
              {{ $t('commons.workspace_unit') }}
            </div>
          </div>
        </el-card>
      </div>
    </el-card>
  </div>

</template>

<script>
import {hasPermissions} from "@/common/js/utils";

export default {
  name: "SettingHome",
  computed: {
    color: function () {
      return `var(--primary_color)`;
    }
  },
  data() {
    return {
      statistic: {
        userSize: 0,
        workspaceSize: 0,
        projectSize: 0
      },
      result: {}
    };
  },
  activated() {
    let projectPermissions = hasPermissions('PROJECT_USER:READ', 'PROJECT_ENVIRONMENT:READ', 'PROJECT_OPERATING_LOG:READ',
      'PROJECT_FILE:READ+JAR', 'PROJECT_FILE:READ+FILE', 'PROJECT_CUSTOM_CODE:READ');

    let wsPermissions = hasPermissions('WORKSPACE_SERVICE:READ', 'PROJECT_MESSAGE:READ', 'WORKSPACE_USER:READ',
      'WORKSPACE_PROJECT_MANAGER:READ', 'WORKSPACE_PROJECT_ENVIRONMENT:READ', 'WORKSPACE_OPERATING_LOG:READ',
      'PROJECT_TEMPLATE:READ');

    let sysPermissions = hasPermissions('SYSTEM_USER:READ', 'SYSTEM_WORKSPACE:READ', 'SYSTEM_GROUP:READ',
      'SYSTEM_TEST_POOL:READ', 'SYSTEM_SETTING:READ', 'SYSTEM_AUTH:READ',
      'SYSTEM_QUOTA:READ', 'SYSTEM_OPERATING_LOG:READ');

    // 只有项目权限时跳转到项目列表
    if (projectPermissions && !wsPermissions && !sysPermissions) {
      this.$router.push('/project/home');
      return;
    }
    // 只有工作空间权限时跳转到项目列表
    if (wsPermissions && !sysPermissions) {
      this.$router.push('/setting/project/:type');
    }
  },
  mounted() {
    this.result = this.$get("/system/statistics/data", res => {
      this.statistic = res.data;
    });
  }
};
</script>

<style scoped>
.setting-div {
  width: 60vh;
  height: 60vh;
  position: absolute;
  top: 50%;
  left: 50%;
  min-width: 400px;
  min-height: 400px;
  transform: translate(-50%, -50%);
}

.setting-card {
  height: calc(100vh - 55px);
  position: relative;
}

.icon-color {
  font-size: 30px;
  color: var(--primary_color);
  margin-right: 6px;
}

.div-item {
  width: 100%;
  padding: 20px;
}

.number {
  font-size: 20px;
  color: var(--primary_color);
}
</style>
