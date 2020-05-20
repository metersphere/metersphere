<template>

  <common-component :title="$t('test_track.plan_view.test_result')">
    <template>
      <el-table
        :data="testResults"
        stripe
        style="width: 100%">
        <el-table-column
          prop="moduleName"
          :label="$t('test_track.module.module')"
          width="180">
        </el-table-column>
        <el-table-column
          prop="caseCount"
          :label="$t('test_track.plan_view.case_count')"
          width="180">
        </el-table-column>
        <el-table-column
          prop="passRate"
          :label="$t('test_track.pass_rate')">
          <template v-slot:default="scope">
            {{scope.row.passRate}}%
          </template>
        </el-table-column>
        <el-table-column
          prop="issuesCount"
          :label="$t('test_track.plan_view.issues_count')">
        </el-table-column>
      </el-table>
    </template>

  </common-component>

</template>

<script>
    import CommonComponent from "./CommonComponent";
    export default {
      name: "TestResultComponent",
      components: {CommonComponent},
      props: {
        testResults: {
          type: Array,
          default() {
            return [
              {
                moduleName: this.$t('test_track.module.module') + '1',
                caseCount: '14',
                passRate: 10.8,
                issuesCount: 3
              },
              {
                moduleName: this.$t('test_track.module.module') + '2',
                caseCount: '24',
                passRate: 40,
                issuesCount: 6
              },
              {
                moduleName: this.$t('test_track.module.module') + '3',
                caseCount: '50',
                passRate: 76.9,
                issuesCount: 8
              }
            ]
          }
        }
      }
    }
</script>

<style scoped>

</style>
