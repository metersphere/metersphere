<template>
  <common-component :title="$t('test_track.plan_view.defect_list')">
    <template>
      <el-table
        row-key="id"
        >
        <el-table-column
          prop="id"
          :label="$t('commons.id')"
          show-overflow-tooltip>
        </el-table-column>
        <el-table-column
          prop="module"
          :label="$t('test_track.module.module')"
          show-overflow-tooltip>
        </el-table-column>
        <el-table-column
          prop="title"
          :label="$t('test_track.module.title')"
          show-overflow-tooltip>
        </el-table-column>


        <el-table-column
          prop="title"
          :label="$t('描述')"
          show-overflow-tooltip>
        </el-table-column>

        <el-table-column
          prop="title"
          :label="$t('状态')"
          show-overflow-tooltip>
        </el-table-column>
        <el-table-column
          prop="title"
          :label="$t('处理人')"
          show-overflow-tooltip>
        </el-table-column>

        <el-table-column
          prop="title"
          :label="$t('创建时间')">
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
      name: "DefectListComponent",
      components: {StatusTableItem, MethodTableItem, TypeTableItem, PriorityTableItem, CommonComponent},
      props: {
        failureTestCases: {
          type: Array,
          default() {
            return [
              {
                title: 'testCase1',
              },
              {
                title: 'testCase1',
              }
            ]
          }
        }
      }
    }
</script>

<style scoped>

</style>
