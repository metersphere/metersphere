<template>
  <span>
      <el-tag v-if="value === 'Prepare'" class="status-label prepare">
        {{ $t('test_track.plan.plan_status_prepare') }}
      </el-tag>
      <el-tag v-if="value === 'Underway'" class="status-label underway">
        {{ $t('test_track.plan.plan_status_running') }}
      </el-tag>
      <el-tag v-if="value === 'Finished'" class="status-label finished">
        {{ $t('test_track.plan.plan_status_finished') }}
      </el-tag>
      <el-tag v-if="value === 'Completed'" class="status-label completed">
        {{ $t('test_track.plan.plan_status_completed') }}
      </el-tag>
      <el-tag v-if="value === 'Trash'" class="status-label trash">
        {{ $t('test_track.plan.plan_status_trash') }}
      </el-tag>
      <el-tag v-if="value === 'Archived'" class="status-label archived">
        {{ $t('test_track.plan.plan_status_archived') }}
      </el-tag>

    <!--Api首页中的两种定时任务-->
    <el-tag v-if="value === 'API_SCENARIO_TEST'" class="status-label schedule-task-scenario">
       {{ $t('commons.scenario_case') }}
    </el-tag>
    <el-tag v-if="value === 'SWAGGER_IMPORT'" class="status-label schedule-task-swagger">
      {{ $t('api_test.home_page.running_task_list.swagger_schedule') }}
    </el-tag>
  </span>

</template>

<script>
export default {
  name: "BasicStatus",
  components: {},
  props: {
    value: {
      type: String
    }
  }
}
</script>

<style scoped>
.status-label {
  height: 24px;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  padding: 0 6px;
  border-radius: 2px;
  border-color: transparent;
}

.completed {
  background-color: rgba(52, 199, 36, 0.2);
  color: #34C724;
}

.underway {
  background-color: rgba(120, 56, 135, 0.2);
  color: #783887;
}

.prepare {
  background-color: rgba(31, 35, 41, 0.1);
  color: #646A73;
}

.finished {
  background-color: rgba(0, 214, 185, 0.2);
  color: #078372;
}

.trash {
  background-color: rgba(245, 74, 69, 0.2);
  color: #D83931;
}

.archived {
  background-color: rgba(255, 136, 0, 0.2);
  color: #DE7802;
}

.schedule-task-scenario {
  background: rgba(52, 199, 36, 0.2);
  color: #2EA121;
}

.schedule-task-swagger {
  background: rgba(0, 214, 185, 0.2);
  color: #078372;
}
</style>
