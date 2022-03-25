<template>
  <el-row :gutter="10" class="json-path-suggest-button">
    <el-button size="mini" type="primary" @click="$emit('open')" @click.stop>
      {{openTip}}
    </el-button>
    <el-button size="mini" type="danger" @click="$emit('clear')">
      {{clearTip}}
    </el-button>
  </el-row>
</template>

<script>
    export default {
      name: "ApiJsonPathSuggestButton",
      props: ['openTip', 'clearTip']
    }
</script>

<style scoped>

  .json-path-suggest-button {
    text-align: right;
    margin-top: 10px;
  }

</style>
