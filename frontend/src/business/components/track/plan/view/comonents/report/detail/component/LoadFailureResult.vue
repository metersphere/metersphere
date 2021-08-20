<template>
    <div class="container">
      <el-table
        row-key="id"
        :data="failureTestCases">
        <el-table-column
          prop="num"
          :label="$t('commons.id')"
          show-overflow-tooltip>
          <template v-slot:default="{row}">
            {{row.isCustomNum ? row.customNum : row.num }}
          </template>
        </el-table-column>
        <el-table-column
          prop="name"
          :label="$t('commons.name')"
          show-overflow-tooltip>
        </el-table-column>

        <el-table-column
          prop="userName"
          :label="$t('commons.create_user')">
        </el-table-column>

        <el-table-column
          prop="status"
          column-key="status"
          :label="$t('test_track.plan_view.execute_result')">
          <template v-slot:default="scope">
            <status-table-item :value="'Failure'"/>
          </template>
        </el-table-column>

      </el-table>
    </div>

</template>

<script>
import TypeTableItem from "../../../../../../common/tableItems/planview/TypeTableItem";
import MethodTableItem from "../../../../../../common/tableItems/planview/MethodTableItem";
import StatusTableItem from "../../../../../../common/tableItems/planview/StatusTableItem";
import {getPlanLoadFailureCase, getSharePlanLoadFailureCase} from "@/network/test-plan";
export default {
  name: "LoadFailureResult",
  components: {StatusTableItem, MethodTableItem, TypeTableItem},
  props: {
    planId: String,
    report: Object,
    isTemplate: Boolean,
    isShare: Boolean
  },
  data() {
    return {
      failureTestCases:  []
    }
  },
  mounted() {
    this.getFailureTestCase();
  },
  methods: {
    getFailureTestCase() {
      if (this.isTemplate) {
        this.failureTestCases = this.report.loadFailureTestCases;
      } else if (this.isShare) {
        getSharePlanLoadFailureCase(this.planId, (data) => {
          this.failureTestCases = data;
        });
      } else {
        getPlanLoadFailureCase(this.planId, (data) => {
          this.failureTestCases = data;
        });
      }
    }
  }
}
</script>

<style scoped>

</style>
