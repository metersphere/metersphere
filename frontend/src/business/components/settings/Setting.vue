<template>
  <el-row type="flex">
    <div class="menus">
      <ms-current-user/>
      <el-divider/>
      <h1>{{$t('commons.setting')}}</h1>
      <ms-setting-menu/>
    </div>
    <div class="container">
      <keep-alive>
        <router-view class="main-content"/>
      </keep-alive>
    </div>
  </el-row>
</template>

<script>
  import MsCurrentUser from "./CurrentUser";
  import MsSettingMenu from "./SettingMenu";

  export default {
    name: "MsSetting",
    components: {MsSettingMenu, MsCurrentUser},
  }
</script>

<style scoped>
  .menus {
    width: 280px;
    height: 100%;
    border-right: 1px solid #E6E6E6;
    padding: 20px;
    box-sizing: border-box;
    background-color: #FFF;
  }

  h1 {
    font-size: 20px;
    font-weight: 500;
  }

</style>

