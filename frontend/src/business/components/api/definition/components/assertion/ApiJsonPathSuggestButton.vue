<template>
  <el-row :gutter="10" class="json-path-suggest-button">
    <el-button size="small" type="primary" @click="$emit('open')">
      {{ $t('api_test.request.assertions.json_path_suggest') }}
    </el-button>
    <el-button size="small" type="danger" @click="$emit('clear')">
      {{ $t('api_test.request.assertions.json_path_clear') }}
    </el-button>
  </el-row>
</template>

<script>
    export default {
        name: "ApiJsonPathSuggestButton"
    }
</script>

<style scoped>

  .json-path-suggest-button {
    text-align: right;
  }

</style>
