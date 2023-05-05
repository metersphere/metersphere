<template>
  <div class="container">
    <el-table row-key="id" :data="testCases">
      <el-table-column
        prop="num"
        :label="$t('commons.id')"
        show-overflow-tooltip
      >
        <template v-slot:default="{ row }">
          <span v-if="isTemplate || isShare">
            {{ row.isCustomNum ? row.customNum : row.num }}
          </span>
          <el-link
            v-else
            type="primary"
            @click="redirectFunctionCaseEditPage(row.caseId)"
          >
            {{ row.isCustomNum ? row.customNum : row.num }}
          </el-link>
        </template>
      </el-table-column>
      <el-table-column
        prop="name"
        :label="$t('commons.name')"
        show-overflow-tooltip
      >
      </el-table-column>
      <el-table-column
        prop="priority"
        column-key="priority"
        :label="$t('test_track.case.priority')"
      >
        <template v-slot:default="scope">
          <priority-table-item :value="scope.row.priority" ref="priority" />
        </template>
      </el-table-column>

      <el-table-column
        prop="projectName"
        :label="$t('test_track.case.project_name')"
        show-overflow-tooltip
      >
      </el-table-column>

      <el-table-column
        prop="executorName"
        :label="$t('test_track.plan_view.executor')"
      >
      </el-table-column>

      <el-table-column
        prop="maintainerName"
        :label="$t('test_track.plan.plan_principal')"
      >
      </el-table-column>

      <el-table-column
        prop="status"
        column-key="status"
        :label="$t('test_track.plan_view.execute_result')"
      >
        <template v-slot:default="scope">
          <status-table-item :value="scope.row.status" />
        </template>
      </el-table-column>

      <el-table-column
        prop="updateTime"
        :label="$t('commons.update_time')"
        show-overflow-tooltip
      >
        <template v-slot:default="scope">
          <span>{{ scope.row.updateTime | datetimeFormat }}</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import PriorityTableItem from "../../../../../../common/tableItems/planview/PriorityTableItem";
import TypeTableItem from "../../../../../../common/tableItems/planview/TypeTableItem";
import MethodTableItem from "../../../../../../common/tableItems/planview/MethodTableItem";
import StatusTableItem from "../../../../../../common/tableItems/planview/StatusTableItem";

export default {
  name: "FunctionalCases",
  components: {
    StatusTableItem,
    MethodTableItem,
    TypeTableItem,
    PriorityTableItem,
  },
  props: {
    planId: String,
    isTemplate: Boolean,
    isShare: Boolean,
    report: {},
    shareId: String,
    isAll: Boolean,
    isDb: Boolean,
    filterStatus: String,
    allTestCase: {
      type: Array,
      default() {
        return [];
      },
    },
  },
  data() {
    return {
      testCases: [],
    };
  },
  mounted() {
    this.getFunctionalTestCase();
  },
  watch: {
    testCases() {
      if (this.testCases) {
        this.$emit("setSize", this.testCases.length);
      }
    },
    allTestCase() {
      this.getFunctionalTestCase();
    },
  },
  methods: {
    getFunctionalTestCase() {
      this.testCases = [];
      if (this.filterStatus) {
        this.allTestCase.forEach((item) => {
          if (item.status === this.filterStatus) {
            this.testCases.push(item);
          }
        });
      } else {
        this.testCases = this.allTestCase;
      }
    },
    redirectFunctionCaseEditPage(caseId) {
      let home = this.$router.resolve({
        name: "testCaseEdit",
        params: {
          caseId: caseId,
        },
      });
      window.open(home.href, "_blank");
    },
  },
};
</script>

<style scoped></style>
