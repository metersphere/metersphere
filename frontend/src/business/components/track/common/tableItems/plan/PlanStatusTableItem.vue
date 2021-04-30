<template>
    <div>
      <ms-tag v-if="value == 'Prepare'" type="info" :content="$t('test_track.plan.plan_status_prepare')"/>
      <ms-tag v-else-if="value == 'Underway'" type="primary" :content="$t('test_track.plan.plan_status_running')"/>
      <ms-tag v-else-if="value == 'Finished'" type="warning" :content="$t('test_track.plan.plan_status_finished')"/>
      <ms-tag v-else-if="value == 'Completed'" type="success" :content="$t('test_track.plan.plan_status_completed')"/>
      <ms-tag v-else-if="value === 'Trash'" type="danger" effect="plain" :content="$t('test_track.plan.plan_status_trash')"/>
      <ms-tag v-else type="warning" effect="plain" :content="value"/>
    </div>
</template>

<script>
import MsTag from "../../../../common/components/MsTag";

export default {
      name: "PlanStatusTableItem",
      components: {MsTag},
      props: {
        value: {
          type: String
        }
      }
    }
</script>

<style scoped>

</style>
