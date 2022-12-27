<template>
  <workstation-detail :is-creation=true :current-todo-name="currentTodo"></workstation-detail>
</template>
<script>

import WorkstationDetail from "@/business/detail/WorkstationDetail"

export default {
  name: 'Creation',
  components: {
    WorkstationDetail
  },
  watch: {},
  data() {
    return {
      currentTodo:''
    }
  },
  methods: {},
  created() {
    if (this.$route.query.name) {
      this.currentTodo = this.$route.query.name
    } else {
      this.currentTodo = 'track_case'
    }
  }
}
</script>
<style scoped>
.workstation-card {
  height: 100%;
}
</style>
