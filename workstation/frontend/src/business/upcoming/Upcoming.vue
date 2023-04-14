<template>
  <workstation-detail
    :is-upcoming="true"
    :current-todo-name="currentTodo"
    v-if="projectId !== '' && projectId !== 'no_such_project'"
  ></workstation-detail>
</template>
<script>
import WorkstationDetail from "@/business/detail/WorkstationDetail";
import { getCurrentProjectID } from "metersphere-frontend/src/utils/token";

export default {
  name: "Upcoming",
  components: {
    WorkstationDetail,
  },
  data() {
    return {
      currentTodo: "",
      projectId: getCurrentProjectID(),
    };
  },
  methods: {},
  created() {
    if (this.$route.query.name) {
      this.currentTodo = this.$route.query.name;
    } else {
      this.currentTodo = "track_case";
    }
  },
};
</script>
<style scoped>
:deep(.el-main) {
  overflow: hidden;
}

.workstation-card {
  height: 100%;
}
</style>
