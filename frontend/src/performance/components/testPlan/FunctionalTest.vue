<template>
  <el-col>
    <header-menus :beaseUrl="beaseUrl"/>
    <div>
      <transition>
        <keep-alive>
          <router-view/>
        </keep-alive>
      </transition>
    </div>
  </el-col>
</template>
<script>

  import HeaderMenus from "../HeaderMenus";

  export default {
    name: "FunctionalTest",
    components: {HeaderMenus},
    data() {
      return {
        beaseUrl: "functional"
      }
    }
  }

</script>

<style scoped>

</style>
