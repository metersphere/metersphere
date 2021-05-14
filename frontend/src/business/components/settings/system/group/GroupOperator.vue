<template>
  <div>
    <div v-for="(operator, index) in operators" :key="index" style="margin-top: 5px;">
      {{ operator.name }}
    </div>
  </div>
</template>

<script>
export default {
  name: "GroupOperator",
  props: {
    operators: {
      type: Array,
      default() {
        return {}
      }
    }
  },
  data() {
    return {
      checked: false
    }
  }
}
</script>

<style scoped>

</style>
