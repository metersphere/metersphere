<template>
  <div>
    <span class="user-select-left">{{ user.name }} ({{ user.id }})</span>
    <span class="user-select-right">{{ user.email }}</span>
  </div>
</template>

<script>
export default {
  name: "UserOptionItem",
  props: {
    user: {
      type: Object,
      default() {
        return {
          id: "",
          name: "",
          email: ""
        };
      }
    }
  }
}
</script>

<style scoped>
.user-select-left {
  float: left;
  display: inline-block;
  max-width: 180px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.user-select-left:hover {
  overflow: visible;
}

.user-select-right {
  float: right;
  color: #8492a6;
  font-size: 13px;
}
</style>
