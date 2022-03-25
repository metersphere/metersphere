<template>
  <span class="json-path-suggest-button">
    <el-button size="mini" type="primary" @click="$emit('open')" @click.stop :disabled="isReadOnly">
      {{ openTip }}
    </el-button>
    <el-button size="mini" type="danger" @click="$emit('clear')" :disabled="isReadOnly">
      {{ clearTip }}
    </el-button>
    </span>
</template>

<script>
export default {
  name: "ApiJsonPathSuggestButton",
  props: ['openTip', 'clearTip','isReadOnly']
}
</script>

<style scoped>

.json-path-suggest-button {
}

</style>
