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
    height: calc(100vh - 80px);
    background: white;
  }

  .node-tree {
    margin: 3%;
  }

  .case-container {
    min-height: 600px;
    margin-top: 0;
    margin-left: 0;
  }

</style>

<style scoped>


</style>
