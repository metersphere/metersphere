<template>
  <workstation-detail :is-upcoming=true :current-todo-name="currentTodo"></workstation-detail>
</template>
<script>
import WorkstationDetail from "@/business/detail/WorkstationDetail";

export default {
  name: 'Upcoming',
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
:deep(.el-main) {
  overflow: hidden;
}

.workstation-card {
  height: 100%;
}
</style>
