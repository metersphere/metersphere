<template>
    <span>
      <ms-tag v-if="value == 'Prepare'" :content="$t('api_test.home_page.detail_card.unexecute')"
              style="background-color: #D7D7D7; border-color: #D7D7D7"
              type="info"/>
      <ms-tag v-else-if="value == 'Underway'" type="primary" :content="$t('test_track.plan.plan_status_running')"/>
      <ms-tag v-else-if="value == 'Pass'" type="success" :content="$t('test_track.plan_view.pass')"/>
      <ms-tag v-else-if="value == 'UnPass'" type="danger" :content="$t('test_track.plan_view.not_pass')"/>
      <ms-tag v-else-if="value == 'STOP'" type="info" :content="$t('test_track.plan_view.stop')"/>
      <ms-tag v-else-if="value == 'stop'" type="info" :content="$t('test_track.plan_view.stop')"/>
      <ms-tag v-else-if="value == 'Failure'" type="danger" :content="$t('test_track.plan_view.failure')"/>
      <ms-tag v-else-if="value == 'Blocking'" type="warning" :content="$t('test_track.plan_view.blocking')"/>
      <ms-tag v-else-if="value == 'Skip'" type="info" :content="$t('test_track.plan_view.skip')"/>
      <ms-tag v-else-if="value == 'ErrorReportResult'" :content="$t('error_report_library.option.name')"
              style="background-color: #F6972A; border-color: #F6972A"
              type="warning"/>
      <ms-tag v-else-if="value == 'ApiCaseStop'" type="info" :content="$t('api_test.home_page.detail_card.unexecute')"/>
      <ms-tag v-else-if="value == 'ApiCasePrepare'" :content="$t('api_test.home_page.detail_card.unexecute')"
              type="info"/>
      <ms-tag v-else type="info" :content="value"/>
    </span>
</template>

<script>
import MsTag from "../../../../common/components/MsTag";

export default {
  name: "StatusTableItem",
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
