<template>
  <el-col>
    <w-s-header/>
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
import WSHeader from "./head/WSHeader";

export default {
  name: "Workstation",
  components: {WSHeader},
  data() {
    return {
      baseUrl: "workstation"
    }
  },
  activated() {
    this.$refs.table.doLayout()
  },
}
</script>
<style scoped>

</style>
