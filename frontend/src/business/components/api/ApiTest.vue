<template>
  <el-col>
    <api-header-menus/>
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

  import ApiHeaderMenus from "./head/ApiHeaderMenus";

  export default {
    name: "ApiTest",
    components: {ApiHeaderMenus},
    data() {
      return {
        beaseUrl: "api"
      }
    }
  }

</script>

<style scoped>

</style>
