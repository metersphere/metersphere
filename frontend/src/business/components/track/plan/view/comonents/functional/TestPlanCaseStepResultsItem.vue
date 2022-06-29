<template>
  <el-form-item class="result-item" :label-width="labelWidth">
    <el-table
      v-if="visible"
      :data="testCase.steptResults"
      class="tb-edit"
      size="mini"
      :border="true"
      :default-sort="{prop: 'num', order: 'ascending'}"
      highlight-current-row>
      <el-table-column :label="$t('test_track.case.number')" prop="num" min-width="5%"></el-table-column>

      <el-table-column :label="$t('test_track.case.step_desc')" prop="desc" min-width="21%">
        <template v-slot:default="scope">
          <el-input
            size="mini"
            class="border-hidden sync-textarea"
            type="textarea"
            :disabled="true"
            v-model="scope.row.desc"/>
        </template>
      </el-table-column>
      <el-table-column :label="$t('test_track.case.expected_results')" prop="result" min-width="21%">
        <template v-slot:default="scope">
          <el-input
            size="mini"
            class="border-hidden sync-textarea"
            type="textarea"
            :disabled="true"
            v-model="scope.row.result"/>eee
        </template>
      </el-table-column>f
      <el-table-column :label="$t('test_track.plan_view.actual_result')" min-width="21%">
        G<template v-slot:default="scope">DD
          <el-input
            v-model="scope.row.actualResult"
            clearable
            size="mini"
            type="textarea"
            class="table-edit-input sync-textarea"
            :rows="2"
            :disabled="isReadOnly"
            :placeholder="$t('commons.input_content')"
            @input="resizeTextarea(scope)"/>
        </template>
      </el-table-column>
      <el-table-column :label="$t('test_track.plan_view.step_result')" min-width="12%">
        <template v-slot:default="scope">
          <el-select
            :disabled="isReadOnly"
            v-model="scope.row.executeResult"
            @change="stepResultChange()"
            filterable
            size="mini">
            <el-option :label="$t('test_track.plan_view.pass')" value="Pass"
                       style="color: #7ebf50;"></el-option>
            <el-option :label="$t('test_track.plan_view.failure')" value="Failure"
                       style="color: #e57471;"></el-option>
            <el-option :label="$t('test_track.plan_view.blocking')" value="Blocking"
                       style="color: #dda451;"></el-option>
            <el-option :label="$t('test_track.plan_view.skip')" value="Skip"
                       style="color: #919399;"></el-option>
          </el-select>
        </template>
      </el-table-column>
    </el-table>
  </el-form-item>
</template>

<script>
import {resizeTextarea} from "@/common/js/utils";

export default {
  name: "TestPlanCaseStepResultsItem",
  props: ['testCase', 'isReadOnly', 'labelWidth'],
  data() {
    return {
      visible: true
    }
  },
  watch: {
    'testCase.steptResults.length'() {
      this.$nextTick(() => {
        this.resizeTextarea();
      });
    }
  },
  methods: {
    stepResultChange() {
      if (this.testCase.method === 'manual' || !this.testCase.method) {
        this.isFailure = this.testCase.steptResults.filter(s => {
          return s.executeResult === 'Failure' || s.executeResult === 'Blocking';
        }).length > 0;
      } else {
        this.isFailure = false;
      }
    },
    // 同一行文本框高度保持一致
    resizeTextarea(scope) {
      resizeTextarea(3, scope ? scope.$index : null);
    }
  }
}
</script>

<style scoped>
/deep/ .table-edit-input .el-textarea__inner, .table-edit-input .el-input__inner {
  border-style: solid;
}

.el-table >>> td:nth-child(2) .cell,.el-table >>> td:nth-child(2),
.el-table >>> td:nth-child(3) .cell,.el-table >>> td:nth-child(3),
.el-table >>> td:nth-child(4) .cell,.el-table >>> td:nth-child(4) {
  padding: 0;
}

.el-table >>> td:nth-child(1) .cell {
  text-align: center;
}
</style>
