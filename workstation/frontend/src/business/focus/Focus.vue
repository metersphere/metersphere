<template>
  <workstation-detail :is-focus="true" :current-todo-name="currentTodo"/>
</template>
<script>
import WorkstationDetail from "@/business/detail/WorkstationDetail";

export default {
  name: 'Focus',
  components: {
    WorkstationDetail
  },
  data() {
    return {
      currentTodo:'',
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
