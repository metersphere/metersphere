<template>
  <span>
      <el-tag v-if="value === 'Prepare'" class="status-label prepare">
        {{ $t('test_track.plan.plan_status_prepare') }}
      </el-tag>
      <el-tag v-if="value === 'Underway'" class="status-label underway">
        {{ $t('test_track.plan.plan_status_running') }}
      </el-tag>
      <el-tag v-if="value === 'Completed'" class="status-label completed">
        {{ $t('test_track.plan.plan_status_completed') }}
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
  padding: 1px 6px;
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
</style>
