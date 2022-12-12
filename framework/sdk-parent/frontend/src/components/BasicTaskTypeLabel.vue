<template>
  <span>
      <el-tag v-if="value === 'API_SCENARIO_TEST'" class="status-label api">
        {{ $t('api_test.home_page.running_task_list.scenario_schedule') }}
      </el-tag>
      <el-tag v-if="value === 'TEST_PLAN_TEST'" class="status-label plan">
        {{ $t('api_test.home_page.running_task_list.test_plan_schedule') }}
      </el-tag>
      <el-tag v-if="value === 'SWAGGER_IMPORT'" class="status-label swagger">
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

.api {
  background-color: rgba(52, 199, 36, 0.2);
  color: #2EA121;
}

.swagger {
  background-color: rgba(255, 136, 0, 0.2);
  color: #DE7802;
}

.plan {
  background-color: rgba(51, 112, 255, 0.2);
  color: #245BDB;
}
</style>
