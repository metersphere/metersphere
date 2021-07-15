<template>
  <span>
    <span v-if="triggerMode === 'TEST_PLAN_API'">{{ $t('commons.trigger_mode.api') }}</span>
    <span v-if="triggerMode === 'MANUAL'">{{$t('commons.trigger_mode.manual')}}</span>
    <span v-if="triggerMode === 'SCHEDULE'">{{$t('commons.trigger_mode.schedule')}}</span>
    <span v-if="triggerMode === 'TEST_PLAN_SCHEDULE'">{{$t('commons.trigger_mode.schedule')}}</span>
    <span v-if="triggerMode === 'API'">{{$t('commons.trigger_mode.api')}}</span>
    <span v-if="triggerMode === 'BATCH'">{{$t('api_test.automation.batch_execute')}}</span>
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
