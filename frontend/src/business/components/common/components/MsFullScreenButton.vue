<template>
  <span class="fulls-screen-btn">
    <font-awesome-icon v-if="!isFullScreen" class="alt-ico" :icon="['fa', 'expand-alt']" size="lg" @click="change(true)"/>
    <font-awesome-icon v-if="isFullScreen" class="alt-ico" :icon="['fa', 'compress-alt']" size="lg" @click="change(false)"/>
  </span>
</template>

<script>
export default {
  name: "MsFullScreenButton",
  props: {
    isFullScreen: {
      type: Boolean,
      default: false
    },
  },
  methods: {
    change(bool) {
      this.$emit("update:isFullScreen", bool);
    }
  }
}
</script>

<style scoped>

.alt-ico:hover {
  color: black;
  font-size: 18px;
}

.alt-ico {
  font-size: 15px;
  color: #8c939d;
}

</style>
