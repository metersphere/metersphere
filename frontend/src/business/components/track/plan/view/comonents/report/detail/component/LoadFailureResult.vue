<template>
  <el-card>
    <el-scrollbar>
      <el-table
          row-key="id"
          @row-click="rowClick"
          :highlight-current-row="true"
          :data="loadTestCases">
        <el-table-column
            prop="num"
            :label="$t('commons.id')"
            show-overflow-tooltip>
          <template v-slot:default="{row}">
            {{ isCustomNum ? row.customNum : row.num }}
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
          <template v-slot:default="{row}">
            <el-tag size="mini" type="danger" v-if="row.status === 'error'">
              {{ row.status }}
            </el-tag>
            <el-tag size="mini" type="success" v-else-if="row.status === 'success'">
              {{ row.status }}
            </el-tag>
            <el-tag size="mini" v-else-if="row.status === 'run'">
              {{ row.status }}
            </el-tag>
            <el-tag size="mini" v-else-if="row.status === 'Completed'">
              {{ row.status }}
            </el-tag>
            <el-tag size="mini"  v-else-if="!row.status || row.status === '' ">
              Prepare
            </el-tag>
            <el-tag size="mini" v-else>
              {{ row.status }}
            </el-tag>
          </template>
        </el-table-column>

      </el-table>
    </el-scrollbar>
  </el-card>
</template>

<script>
import TypeTableItem from "../../../../../../common/tableItems/planview/TypeTableItem";
import MethodTableItem from "../../../../../../common/tableItems/planview/MethodTableItem";
import StatusTableItem from "../../../../../../common/tableItems/planview/StatusTableItem";
import {
  getPlanLoadAllCase,
  getPlanLoadFailureCase,
  getSharePlanLoadAllCase,
  getSharePlanLoadFailureCase
} from "@/network/test-plan";

export default {
  name: "LoadFailureResult",
  components: {StatusTableItem, MethodTableItem, TypeTableItem},
  props: {
    planId: String,
    report: Object,
    isTemplate: Boolean,
    isShare: Boolean,
    shareId: String,
    isAll: Boolean,
    isDb: Boolean
  },
  data() {
    return {
      loadTestCases: []
    }
  },
  mounted() {
    this.getFailureTestCase();
  },
  watch: {
    loadTestCases() {
      if (this.loadTestCases) {
        this.$emit('setSize', this.loadTestCases.length);
      }
    }
  },
  computed: {
    isCustomNum() {
      return this.$store.state.currentProjectIsCustomNum;
    },
  },
  methods: {
    getFailureTestCase() {
      if (this.isTemplate || this.isDb) {
        if (this.isAll) {
          this.loadTestCases = this.report.loadAllCases ? this.report.loadAllCases : [];
        } else {
          this.loadTestCases = this.report.loadFailureCases ? this.report.loadFailureCases : [];
        }
      } else if (this.isShare) {
        if (this.isAll) {
          getSharePlanLoadAllCase(this.shareId, this.planId, (data) => {
            this.loadTestCases = data;
          });
        } else {
          getSharePlanLoadFailureCase(this.shareId, this.planId, (data) => {
            this.loadTestCases = data;
          });
        }
      } else {
        if (this.isAll) {
          getPlanLoadAllCase(this.planId, (data) => {
            this.loadTestCases = data;
          });
        } else {
          getPlanLoadFailureCase(this.planId, (data) => {
            this.loadTestCases = data;
          });
        }
      }
    },
    handleDefaultClick() {
      let data = this.scenarioCases;
      if (data && data.length > 0) {
        this.rowClick(data[0]);
      }
    },
    rowClick(row) {
      if (this.isAll) {
        this.$emit('rowClick', row);
      }
    }
  }
}
</script>

<style scoped>
.el-card >>> .el-card__body {
  height: 860px;
}
</style>
