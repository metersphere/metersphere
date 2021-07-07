<template>
  <div>
    <span v-for="(role, index) in roles" :key="index">
      <el-tag :type="type" :effect="effect" color="" size="mini">{{role.name}}</el-tag>
      <br>
    </span>
  </div>
</template>

<script>
  import MsTag from "./MsTag";

  export default {
    name: "MsRolesTag",
    components: {MsTag},
    props: {
      type: {
        type: String,
        default: 'primary',
      },
      roles: {
        type: Array
      },
      effect: {
        type: String,
        default: 'dark',
      }
    }
  }
</script>

<style scoped>

</style>
