<template>
  <span>
    <span v-if="triggerMode == 'MANUAL'">手动</span>
    <span v-if="triggerMode == 'SCHEDULE'">定时任务</span>
    <span v-if="triggerMode == 'API'">API</span>
  </span>
</template>

<script>
    export default {
      name: "ReportTriggerModeItem",
      props: ['triggerMode']
    }
</script>

<style scoped>

</style>
