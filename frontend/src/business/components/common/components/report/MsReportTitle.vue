<template>
  <div class="clearfix report-title">
    <div class="report-left">
      <div class="title">
        【{{ type }}】- {{ title }}
        <span v-if="report.endTime || report.createTime">
          <span style="margin-left: 10px">{{ $t('report.test_start_time') }}：</span>
          <span class="time"> {{ report.createTime | timestampFormatDate }}</span>
          <span style="margin-left: 10px">{{ $t('report.test_end_time') }}：</span>
          <span class="time"> {{ report.endTime | timestampFormatDate }}</span>
        </span>
      </div>
    </div>
    <div class="report-right">
      <img class="logo" src="@/assets/logo-MeterSphere.png">
    </div>

  </div>
</template>

<script>
    export default {
      name: "MsReportTitle",
      props: {title: String, type: String, report: Object},
      data() {
        return {}
      }
    }
</script>

<style scoped>

  .report-left {
    float: left;
  }

  .report-right {
    float: right;
  }

  .logo {
    height: 30px;
    line-height: 30px;
    vertical-align: middle
  }


  .report-left {
    font-size: 15px;
  }

  .time {
    margin-left: 10px;
  }

  .title {
    margin-bottom: 5px;
  }



</style>
