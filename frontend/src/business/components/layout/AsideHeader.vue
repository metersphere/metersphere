<template>
  <el-menu
    class="ms-menu-vertical ms-menu horizontal-collapse-transition"
    text-color="#F2F2F2"
    active-text-color="#fff"
    :key="menuKey"
    :collapse="isCollapse"
    :background-color="color"
    router>
    <el-menu-item>
      <div>
        <img :src="'/display/file/logo'" class="logo" alt="">
        <span slot="title" class="ms-title-width">{{ title }}</span>
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
    color: String,
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

.ms-title-width {
  font-size: 15px;
  font-weight: bold;
  margin-left: 4px;
  color: #fff;
  display: inline-block;
  overflow-x: hidden;
  text-overflow: ellipsis;
  vertical-align: middle;
  white-space: nowrap;
  width: 105px;
}

/deep/ .el-menu-item {
  padding-left: 5px !important;
  height: 41px;
  line-height: 41px;
}

.el-menu-item:hover {
  background-color: var(--color) !important;
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
}

.horizontal-collapse-transition {
  transition: 0s width ease-in-out, 0s padding-left ease-in-out, 0s padding-right ease-in-out;
}
</style>
