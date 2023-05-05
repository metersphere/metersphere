<template>
  <workstation-detail
    :is-focus="true"
    v-if="projectId !== '' && projectId !== 'no_such_project' && currentTodo !== 'empty'"
    :current-todo-name="currentTodo"
  />
</template>
<script>
import WorkstationDetail from "@/business/detail/WorkstationDetail";
import { getCurrentProjectID } from "metersphere-frontend/src/utils/token";
import { hasPermissions } from "metersphere-frontend/src/utils/permission"

export default {
  name: "Focus",
  components: {
    WorkstationDetail,
  },
  data() {
    return {
      currentTodo: "",
      projectId: getCurrentProjectID(),
    };
  },
  methods: {
    setDefaultCurrentTodo() {
      // 设置当前默认TAB页为下一个有权限的菜单TAB
      // 如果该用户无权限存在, 则空白
      if (hasPermissions('PROJECT_TRACK_CASE:READ')) {
        this.currentTodo = 'track_case';
      } else if (hasPermissions('PROJECT_TRACK_PLAN:READ')) {
        this.currentTodo = 'track_plan';
      } else if (hasPermissions('PROJECT_TRACK_REVIEW:READ')) {
        this.currentTodo = 'track_review';
      } else if (hasPermissions('PROJECT_TRACK_ISSUE:READ')) {
        this.currentTodo = 'track_issue';
      } else if (hasPermissions('PROJECT_API_DEFINITION:READ')) {
        this.currentTodo = 'api_definition';
      } else if (hasPermissions('PROJECT_API_SCENARIO:READ')) {
        this.currentTodo = 'api_automation';
      } else if (hasPermissions('PROJECT_PERFORMANCE_TEST:READ')) {
        this.currentTodo = 'performance';
      } else {
        this.currentTodo = 'empty'
      }
    }
  },
  created() {
    if (this.$route.query.name) {
      this.currentTodo = this.$route.query.name;
    } else {
      this.setDefaultCurrentTodo();
    }
  },
};
</script>
<style scoped>
.workstation-card {
  height: 100%;
}
</style>
