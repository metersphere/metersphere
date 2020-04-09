<template>
  <el-col>
    <track-header-menus/>
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

  import TrackHeaderMenus from "./head/TrackHeaderMenus";

    export default {
      name: "TrackHome",
      components: {TrackHeaderMenus},
      data() {
        return {
          beaseUrl: "track"
        }
      }
    }
</script>

<style scoped>

</style>
