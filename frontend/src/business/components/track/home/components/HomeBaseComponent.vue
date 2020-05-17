<template>
  <div class="track-home-component">
    <el-card>
      <template v-slot:header>
        <span class="title">{{title}}</span>
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

  .el-card {
    padding: 15px;
  }

  .track-home-component {
    padding: 0 15px 15px;
  }

</style>
