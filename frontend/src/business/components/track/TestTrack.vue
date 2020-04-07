<template>
  <el-col>
    <header-menus/>
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

  import HeaderMenus from "./head/HeaderMenus";

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
