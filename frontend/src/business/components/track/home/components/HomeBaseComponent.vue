<template>
  <div>
    <el-card>
      <template v-slot:header>
        <span class="title">{{ title }}</span>
      </template>
      <slot></slot>
    </el-card>
  </div>
</template>

<script>
export default {
  name: "HomeBaseComponent",
  props: {
    title: {
      type: String,
      default() {
        this.$t('commons.title')
      }
    }
  }
}
</script>

<style scoped>

</style>
