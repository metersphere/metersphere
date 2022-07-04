<template>
  <el-menu menu-trigger="click"
           class="ms-menu-vertical ms-menu-title ms-footer" :collapse="isCollapse"
           text-color="#F2F2F2"
           active-text-color="#fff"
           :default-active="activeIndex"
           :key="menuKey"
           router>
    <el-menu-item index="/setting" onselectstart="return false"
                  v-permission="['SYSTEM_USER:READ', 'SYSTEM_WORKSPACE:READ', 'SYSTEM_GROUP:READ', 'SYSTEM_TEST_POOL:READ', 'SYSTEM_SETTING:READ', 'SYSTEM_AUTH:READ', 'SYSTEM_QUOTA:READ','SYSTEM_OPERATING_LOG:READ',
                  'WORKSPACE_SERVICE:READ', 'WORKSPACE_USER:READ', 'WORKSPACE_PROJECT_MANAGER:READ', 'WORKSPACE_PROJECT_ENVIRONMENT:READ', 'WORKSPACE_OPERATING_LOG:READ']">
      <div>
        <img src="../../../assets/module/system.svg" alt="" class="ms-menu-img">
        <span slot="title" class="ms-menu-item-title">{{ $t('commons.system_setting') }}</span>
      </div>
    </el-menu-item>
  </el-menu>
</template>

<script>

export default {
  name: "MsAsideFooter",
  data() {
    return {
      activeIndex: '/',
      isReport: true,
      modules: {},
      menuKey: 0,
    };
  },
  props: {
    color: String,
    isCollapse: {
      type: Boolean,
      default: true,
    }
  },
};
</script>
<style scoped>
.ms-footer {
  position: absolute;
  bottom: 0px;
}

.ms-menu-vertical:not(.el-menu--collapse) {
  width: var(--asideOpenWidth);
  padding-bottom: 20px;
  border-right: 0px !important;
}

.ms-menu-item-title {
  margin-left: 10px;
  font-size: 13px;
  color: #fff;
}

.el-menu--collapse {
  width: var(--asideWidth);
  padding-bottom: 20px;
  border-right: 0px !important;
}

/deep/ .el-menu-item {
  padding-left: 11px !important;
}

.ms-menu-title {
  align-items: center;
  background-color: var(--aside_color);
}

.ms-menu-img {
  margin: 0px;
  width: 25px;
  border: 0;
  display: inline-block;
  box-sizing: border-box;
  background-repeat: no-repeat;
  background-position: 50% center;
}

.ms-menu-vertical > li.is-active {
  background: var(--color) !important;
  mix-blend-mode: hard-light;
  color: #fff !important;
}
</style>
