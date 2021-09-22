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
            class="border-hidden"
            type="textarea"
            :autosize="{ minRows: scope.row.minRows, maxRows: 4}"
            :disabled="true"
            v-model="scope.row.desc"/>
        </template>
      </el-table-column>
      <el-table-column :label="$t('test_track.case.expected_results')" prop="result" min-width="21%">
        <template v-slot:default="scope">
          <el-input
            size="mini"
            class="border-hidden"
            type="textarea"
            :autosize="{ minRows: scope.row.minRows, maxRows: 4}"
            :disabled="true"
            v-model="scope.row.result"/>
        </template>
      </el-table-column>
      <el-table-column :label="$t('test_track.plan_view.actual_result')" min-width="21%">
        <template v-slot:default="scope">
          <el-input
            class="table-edit-input"
            size="mini"
            type="textarea"
            :autosize="{ minRows: scope.row.minRows, maxRows: 4}"
            :disabled="isReadOnly"
            @blur="actualResultChange(scope.row)"
            v-model="scope.row.actualResult"
            :placeholder="$t('commons.input_content')"
            clearable/>
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
import {getCharCountInStr} from "@/common/js/utils";

export default {
  name: "TestPlanCaseStepResultsItem",
  props: ['testCase', 'isReadOnly', 'labelWidth'],
  data() {
    return {
      visible: true
    }
  },
  mounted() {
    let step = this.testCase.steptResults;
    if (step) {
      step.forEach(item => {
        let maxCount = Math.max(
          getCharCountInStr(item.desc, '\n'),
          getCharCountInStr(item.result, '\n'),
          getCharCountInStr(item.actualResult, '\n')
        );
        let minRows = maxCount + 1;
        minRows = minRows > 4 ? 4 : minRows;
        this.$set(item, 'minRows', minRows);
      });
    }
  },
  methods: {
    actualResultChange(item) {
      let minRows = getCharCountInStr(item.actualResult, '\n') + 1;
      if (minRows > item.minRows) {
        this.$set(item, 'minRows', Math.min(minRows, 4));
        this.visible = false;
        this.$nextTick(() => {
          this.visible = true;
        });
      }
    },
    stepResultChange() {
      if (this.testCase.method === 'manual' || !this.testCase.method) {
        this.isFailure = this.testCase.steptResults.filter(s => {
          return s.executeResult === 'Failure' || s.executeResult === 'Blocking';
        }).length > 0;
      } else {
        this.isFailure = false;
      }
    },
  }
}
</script>

<style scoped>
/deep/ .table-edit-input .el-textarea__inner, .table-edit-input .el-input__inner {
  border-style: solid;
}
</style>
