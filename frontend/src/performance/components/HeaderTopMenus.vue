<template>
  <el-menu mode="horizontal" menu-trigger="click"
           background-color="#2c2a48"
           class="header-top-menus"
           text-color="#F2F2F2"
           active-text-color="#fff"
           :default-active="activeIndex"
           @select="handleSelect"
           router>
    <el-menu-item index="1" v-permission="['test_manager','test_user','test_viewer']">
      {{$t('commons.functional')}}
    </el-menu-item>
    <el-menu-item index="/loadtest" onselectstart="return false"
                  v-permission="['test_manager','test_user','test_viewer']">
      {{$t('commons.performance')}}
    </el-menu-item>
    <el-menu-item index="/setting/personsetting" onselectstart="return false">
      {{$t('commons.system_setting')}}
    </el-menu-item>
  </el-menu>
</template>

<script>
  export default {
    name: "MsTopMenus",
    data() {
      return {
        activeIndex: '/'
      }
    },
    watch: {
      '$route'() {
        this.handleSelect(this.activeIndex);
      }
    },
    mounted() {
      if (this.$route.matched.length > 0) {
        this.activeIndex = this.$route.matched[0].path;
      }
    },
    methods: {
      handleSelect(index) {
        this.activeIndex = index
      }
    }
  }
</script>
<style>
  .header-top-menus.el-menu--horizontal > li {
    height: 40px;
    line-height: 40px;
    color: inherit;
  }

  .header-top-menus.el-menu--horizontal > li.el-submenu > * {
    height: 39px;
    line-height: 40px;
    color: inherit;
  }

  .header-top-menus.el-menu--horizontal > li.is-active {
    background: #595591 !important;
  }
</style>
<style scoped>

</style>
