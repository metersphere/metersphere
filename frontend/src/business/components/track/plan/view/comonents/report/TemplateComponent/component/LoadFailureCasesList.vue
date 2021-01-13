<template>
  <div class="failure-cases-list">
    <div class="failure-cases-list-header">
      性能测试用例
    </div>

    <el-table
      row-key="id"
      @row-click="goFailureTestCase"
      :data="loadTestCases">

      <el-table-column
        prop="caseName"
        :label="$t('commons.name')"
        show-overflow-tooltip>
      </el-table-column>
      <el-table-column
        prop="projectName"
        :label="$t('load_test.project_name')"
        show-overflow-tooltip>
      </el-table-column>
      <el-table-column
        prop="userName"
        :label="$t('load_test.user_name')"
        show-overflow-tooltip>
      </el-table-column>
      <el-table-column
        sortable
        prop="createTime"
        :label="$t('commons.create_time')">
        <template v-slot:default="scope">
          <span>{{ scope.row.createTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>
      <el-table-column
        prop="status"
        column-key="status"
        :filters="statusFilters"
        :label="$t('commons.status')">
        <template v-slot:default="{row}">
          <ms-performance-test-status :row="row"/>
        </template>
      </el-table-column>
<!--      <el-table-column-->
<!--        prop="caseStatus"-->
<!--        :label="$t('test_track.plan.load_case.execution_status')">-->
<!--        <template v-slot:default="{row}">-->
<!--          <el-tag size="mini" type="danger" v-if="row.caseStatus === 'error'">-->
<!--            {{ row.caseStatus }}-->
<!--          </el-tag>-->
<!--          <el-tag size="mini" type="success" v-else-if="row.caseStatus === 'success'">-->
<!--            {{ row.caseStatus }}-->
<!--          </el-tag>-->
<!--          <span v-else>-</span>-->
<!--        </template>-->
<!--      </el-table-column>-->

    </el-table>
  </div>
</template>

<script>
import MsTag from "@/business/components/common/components/MsTag";
import PriorityTableItem from "@/business/components/track/common/tableItems/planview/PriorityTableItem";
import TypeTableItem from "@/business/components/track/common/tableItems/planview/TypeTableItem";
import MethodTableItem from "@/business/components/track/common/tableItems/planview/MethodTableItem";
import StatusTableItem from "@/business/components/track/common/tableItems/planview/StatusTableItem";
import MsPerformanceTestStatus from "@/business/components/performance/test/PerformanceTestStatus";

export default {
  name: "LoadFailureCasesList",
  components: {MsTag, PriorityTableItem, TypeTableItem, MethodTableItem, StatusTableItem, MsPerformanceTestStatus},
  props: ['loadTestCases'],
  data() {
    return {
      statusFilters: [
        {text: 'Saved', value: 'Saved'},
        {text: 'Starting', value: 'Starting'},
        {text: 'Running', value: 'Running'},
        {text: 'Reporting', value: 'Reporting'},
        {text: 'Completed', value: 'Completed'},
        {text: 'Error', value: 'Error'}
      ],
    }
  },
  methods: {
    goFailureTestCase(row) {
      this.$emit("openFailureTestCase", row);
    }
  }
}
</script>

<style scoped>

</style>
