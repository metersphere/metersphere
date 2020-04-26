<template>
  <el-col>
    <track-header-menus/>
    <div>
      <transition>
        <keep-alive>
          <router-view :baseUrl="baseUrl"/>
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
          baseUrl: "track"
        }
      }
    }
</script>


<style>

  .tree-aside {
    position: relative;
    border: 1px solid #EBEEF5;
    box-sizing: border-box;
    background: white;
  }
</style>

<style scoped>


</style>
