<template>
    <div>
      <el-tag v-if="value == 'Prepare'"
              type="info"
              effect="dark"
              size="mini">{{$t('test_track.plan.plan_status_prepare')}}</el-tag>
      <el-tag v-if="value == 'Pass'"
              type="success"
              effect="dark"
              size="mini">{{$t('test_track.plan_view.pass')}}</el-tag>
      <el-tag v-if="value == 'Failure'"
              type="danger"
              effect="dark"
              size="mini">{{$t('test_track.plan_view.failure')}}</el-tag>
      <el-tag v-if="value == 'Blocking'"
              type="warning"
              effect="dark"
              size="mini">{{$t('test_track.plan_view.blocking')}}</el-tag>
      <el-tag v-if="value == 'Skip'"
              type="info"
              effect="dark"
              size="mini">{{$t('test_track.plan_view.skip')}}</el-tag>
    </div>
</template>

<script>
    export default {
      name: "StatusTableItem",
      props: {
        value: {
          type: String
        }
      }
    }
</script>

<style scoped>

</style>
