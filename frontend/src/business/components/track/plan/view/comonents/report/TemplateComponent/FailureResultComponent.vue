<template>

  <common-component :title="'失败用例'">
    <template>
      <el-table
        row-key="id"
        :data="failureTestCases">
        <el-table-column
          prop="name"
          :label="$t('commons.name')"
          show-overflow-tooltip>
        </el-table-column>
        <el-table-column
          prop="priority"
          column-key="priority"
          :label="$t('test_track.case.priority')">
          <template v-slot:default="scope">
            <priority-table-item :value="scope.row.priority" ref="priority"/>
          </template>
        </el-table-column>

        <el-table-column
          prop="type"
          column-key="type"
          :label="$t('test_track.case.type')"
          show-overflow-tooltip>
          <template v-slot:default="scope">
            <type-table-item :value="scope.row.type"/>
          </template>
        </el-table-column>

        <el-table-column
          prop="method"
          column-key="method"
          :label="$t('test_track.case.method')"
          show-overflow-tooltip>
          <template v-slot:default="scope">
            <method-table-item :value="scope.row.method"/>
          </template>
        </el-table-column>

        <el-table-column
          prop="nodePath"
          :label="$t('test_track.case.module')"
          show-overflow-tooltip>
        </el-table-column>

        <el-table-column
          prop="executorName"
          :label="$t('test_track.plan_view.executor')">
        </el-table-column>

        <el-table-column
          prop="status"
          column-key="status"
          :label="$t('test_track.plan_view.execute_result')">
          <template v-slot:default="scope">
            <status-table-item :value="scope.row.status"/>
          </template>
        </el-table-column>

        <el-table-column
          sortable
          prop="updateTime"
          :label="$t('commons.update_time')"
          show-overflow-tooltip>
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
      </el-table>
    </template>

  </common-component>

</template>

<script>
    import CommonComponent from "./CommonComponent";
    import PriorityTableItem from "../../../../../common/tableItems/planview/PriorityTableItem";
    import TypeTableItem from "../../../../../common/tableItems/planview/TypeTableItem";
    import MethodTableItem from "../../../../../common/tableItems/planview/MethodTableItem";
    import StatusTableItem from "../../../../../common/tableItems/planview/StatusTableItem";
    export default {
      name: "FailureResultComponent",
      components: {StatusTableItem, MethodTableItem, TypeTableItem, PriorityTableItem, CommonComponent},
      props: {
        failureTestCases: {
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
