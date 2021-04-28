<template>
  <el-form-item class="result-item" :label-width="labelWidth">
    <el-table
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
            :autosize="{ minRows: 1, maxRows: 4}"
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
            :autosize="{ minRows: 1, maxRows: 4}"
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
            :autosize="{ minRows: 2, maxRows: 4}"
            :rows="2"
            :disabled="isReadOnly"
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
export default {
  name: "TestPlanCaseStepResultsItem",
  props: ['testCase', 'isReadOnly', 'labelWidth'],
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
  }
}
</script>

<style scoped>
</style>
