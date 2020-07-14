<template>
  <span>
    <span v-if="triggerMode === 'MANUAL'">{{$t('commons.trigger_mode.manual')}}</span>
    <span v-if="triggerMode === 'SCHEDULE'">{{$t('commons.trigger_mode.schedule')}}</span>
    <span v-if="triggerMode === 'API'">{{$t('commons.trigger_mode.api')}}</span>
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
