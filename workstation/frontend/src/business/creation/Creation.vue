<template>
  <workstation-detail
    :is-creation="true"
    v-if="projectId !== '' && projectId !== 'no_such_project'"
    :current-todo-name="currentTodo"
  ></workstation-detail>
</template>
<script>
import WorkstationDetail from "@/business/detail/WorkstationDetail";
import { getCurrentProjectID } from "metersphere-frontend/src/utils/token";

export default {
  name: "Creation",
  components: {
    WorkstationDetail,
  },
  watch: {},
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
.workstation-card {
  height: 100%;
}
</style>
