<template>
  <el-card class="table-card" v-loading="result.loading">
    <template v-slot:header>
      <span class="title">
        {{$t('api_test.home_page.schedule_task_details_card.title')}}
      </span>
    </template>
    <el-container>
      <el-main>
        <div class="text item">
          {{$t('api_test.home_page.schedule_task_details_card.this_week_add',[scheduleTaskCountData.thisWeekAddedCount])}}
        </div>
        <div class="text item">
          {{$t('api_test.home_page.schedule_task_details_card.this_week_execute',[scheduleTaskCountData.thisWeekExecutedCount])}}
        </div>
        <div class="text item">
          {{$t('api_test.home_page.schedule_task_details_card.executed',[scheduleTaskCountData.executedCount])}}
        </div>
      </el-main>
    </el-container>
  </el-card>
</template>

<script>

export default {
  name: "MsTestCaseDetailCard",

  components: {},

  data() {
    return {
      result: {},
      loading: false
    }
  },

  props:{
    scheduleTaskCountData:{},
  },

  methods: {
  },

  created() {
  },
  activated() {
  }
}
</script>
<style scoped>
.el-aside {
  /*background-color: #D3DCE6;*/
  /*color: #333;*/
  text-align: center;
  /*line-height: 200px;*/
}
</style>
