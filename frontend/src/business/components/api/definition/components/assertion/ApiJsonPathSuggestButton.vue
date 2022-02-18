<template>
  <span class="json-path-suggest-button">
    <el-button size="mini" type="primary" @click="$emit('open')" @click.stop>
      {{ openTip }}
    </el-button>
    <el-button size="mini" type="danger" @click="$emit('clear')">
      {{ clearTip }}
    </el-button>
    </span>
</template>

<script>
export default {
  name: "ApiJsonPathSuggestButton",
  props: ['openTip', 'clearTip']
}
</script>

<style scoped>

.json-path-suggest-button {
}

</style>
