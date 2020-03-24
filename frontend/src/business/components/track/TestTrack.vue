<template>
  <el-col>
    <header-menus :beaseUrl="beaseUrl"/>
    <div>
      <transition>
        <keep-alive>
          <router-view :beaseUrl="beaseUrl"/>
        </keep-alive>
      </transition>
    </div>
  </el-col>
</template>

<script>

  import HeaderMenus from "../common/head/HeaderMenus";

    export default {
      name: "TrackHome",
      components: {HeaderMenus},
      data() {
        return {
          beaseUrl: "track"
        }
      }
    }
</script>

<style scoped>

</style>
