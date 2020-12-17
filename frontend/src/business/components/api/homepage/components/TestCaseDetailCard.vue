<template>
  <el-card class="table-card" v-loading="result.loading">
    <template v-slot:header>
      <span class="title">
        {{$t('api_test.home_page.test_case_details_card.title')}}
      </span>
    </template>
    <el-container>
      <el-main>
        <div class="text item">
          {{$t('api_test.home_page.test_case_details_card.this_week_add',[testCaseCountData.thisWeekAddedCount])}}
        </div>
        <div class="text item">
          {{$t('api_test.home_page.test_case_details_card.this_week_execute',[testCaseCountData.thisWeekExecutedCount])}}
        </div>
        <div class="text item">
          {{$t('api_test.home_page.test_case_details_card.executed',[testCaseCountData.executedCount])}}
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
    testCaseCountData:{},
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

</style>
