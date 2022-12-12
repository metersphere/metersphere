<template>
  <div class="icon-title" v-if="userName">
    {{ userName.substring(0, 1) }}
  </div>
</template>

<script>
export default {
  name: "MsUserIcon",
  props: {
    userName: String
  }
}
</script>

<style scoped>
.icon-title {
  color: #fff;
  width: 30px;
  background-color: #72dc91;
  height: 30px;
  line-height: 30px;
  text-align: center;
  border-radius: 30px;
  font-size: 14px;
}
</style>
