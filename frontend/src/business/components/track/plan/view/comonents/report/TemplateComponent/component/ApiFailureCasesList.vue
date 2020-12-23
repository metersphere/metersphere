<template>
  <div class="failure-cases-list">
    <div class="failure-cases-list-header">
      接口测试用例
    </div>

    <el-table
      row-key="id"
      @row-click="goFailureTestCase"
      :data="apiTestCases">

      <el-table-column prop="name" :label="$t('api_test.definition.api_name')" show-overflow-tooltip/>

      <el-table-column
        prop="priority"
        column-key="priority"
        :label="$t('test_track.case.priority')"
        show-overflow-tooltip>
        <template v-slot:default="scope">
          <priority-table-item :value="scope.row.priority"/>
        </template>
      </el-table-column>

      <el-table-column
        prop="path"
        :label="$t('test_track.case.module')"
        show-overflow-tooltip/>

      <el-table-column
        prop="createUser"
        :label="'创建人'"
        show-overflow-tooltip/>

      <el-table-column prop="lastResult" :label="$t('api_test.automation.last_result')">
        <template v-slot:default="{row}">
          <ms-tag type="danger" :content="$t('test_track.plan_view.failure')"/>
        </template>
      </el-table-column>

      <el-table-column
        width="160"
        :label="$t('api_test.definition.api_last_time')"
        prop="updateTime">
        <template v-slot:default="scope">
          <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
        </template>
      </el-table-column>

    </el-table>
  </div>
</template>

<script>
    import StatusTableItem from "../../../../../../common/tableItems/planview/StatusTableItem";
    import MethodTableItem from "../../../../../../common/tableItems/planview/MethodTableItem";
    import TypeTableItem from "../../../../../../common/tableItems/planview/TypeTableItem";
    import PriorityTableItem from "../../../../../../common/tableItems/planview/PriorityTableItem";
    import MsTag from "../../../../../../../common/components/MsTag";
    export default {
      name: "ApiFailureCasesList",
      components: {MsTag, PriorityTableItem, TypeTableItem, MethodTableItem, StatusTableItem},
      props: ['apiTestCases'],
      methods: {
        goFailureTestCase(row) {
          this.$emit("openFailureTestCase", row);
        }
      }
    }
</script>

<style scoped>

</style>
