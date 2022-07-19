<template>
  <el-menu
    class="ms-menu-vertical ms-menu-title horizontal-collapse-transition"
    :key="menuKey"
    :collapse="isCollapse"
    router>
    <el-menu-item>
      <div>
        <img :src="'/display/file/logo'" class="logo" alt="">
        <span slot="title" class="ms-logo-title">{{ title }}</span>
      </div>
    </el-menu-item>
  </el-menu>
</template>

<script>

export default {
  name: "MsAsideHeader",
  data() {
    return {
      activeIndex: '/',
      isReport: true,
      modules: {},
      menuKey: 0,
    };
  },
  computed: {
    title() {
      return localStorage.getItem("sysTitle") || "MeterSphere";
    }
  },
  props: {
    sideTheme: String,
    isCollapse: {
      type: Boolean,
      default: true,
    }
  },
};
</script>
<style scoped>
.ms-menu-vertical:not(.el-menu--collapse) {
  width: var(--asideOpenWidth);
  padding-bottom: 60px;
  border-right: 0px !important;
}

.el-menu--collapse {
  width: var(--asideWidth);
  padding-bottom: 60px;
  border-right: 0px !important;
}

.ms-menu-vertical > li.is-active {
  background: var(--aside_color) !important;
}

.ms-logo-title {
  font-size: 15px;
  font-weight: bold;
  margin-left: 4px;
  display: inline-block;
  overflow-x: hidden;
  text-overflow: ellipsis;
  vertical-align: middle;
  white-space: nowrap;
  width: 105px;
  color: var(--font_light_color) !important;
}

/deep/ .el-menu-item {
  padding-left: 5px !important;
  height: 41px;
  line-height: 41px;
}

.el-menu-item:hover {
  background-color: var(--aside_color) !important;
}

.ms-menu-title {
  align-items: center;
  background-color: var(--aside_color);
}

.logo {
  margin: 0px;
  width: 34px;
  height: 34px;
  border: 0;
  display: inline-block;
  box-sizing: border-box;
  background-repeat: no-repeat;
  background-position: 50% center;
  color: var(--font_color) !important;
  background-color: var(--aside_color);
}

.horizontal-collapse-transition {
  transition: 0s width ease-in-out, 0s padding-left ease-in-out, 0s padding-right ease-in-out;
}
</style>
