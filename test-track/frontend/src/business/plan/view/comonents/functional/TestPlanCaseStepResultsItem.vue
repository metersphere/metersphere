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
      <el-table-column :label="$t('test_track.case.number')" prop="num" min-width="5%"/>

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
            v-model="scope.row.result"/>
        </template>
      </el-table-column>f
      <el-table-column :label="$t('test_track.plan_view.actual_result')" min-width="21%">
        <template v-slot:default="scope">
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
            @change="stepResultChange(scope)"
            filterable
            size="mini"
            :ref="'stepResultSelect' + scope.$index">
            <el-option v-for="item in executeResultOption"
                       :key="item.value"
                       :style="{color: item.color}"
                       :value="item.value"
                       :label="item.label"/>
          </el-select>
        </template>
      </el-table-column>
    </el-table>
  </el-form-item>
</template>

<script>
import {resizeTextarea} from "metersphere-frontend/src/utils";

export default {
  name: "TestPlanCaseStepResultsItem",
  props: ['testCase', 'isReadOnly', 'labelWidth'],
  data() {
    return {
      visible: true,
      executeResultOption: [
        {
          value: 'Pass',
          label: this.$t('test_track.plan_view.pass'),
          color: '#7ebf50'
        },        {
          value: 'Failure',
          label: this.$t('test_track.plan_view.failure'),
          color: '#e57471'
        },        {
          value: 'Blocking',
          label: this.$t('test_track.plan_view.blocking'),
          color: '#dda451'
        },        {
          value: 'Skip',
          label: this.$t('test_track.plan_view.skip'),
          color: '#919399'
        },
      ]
    }
  },
  watch: {
    'testCase.steptResults'() {
      this.$nextTick(() => {
        this.resizeTextarea();
        for (let i = 0; i < this.testCase.steptResults.length; i++) {
          let item = this.testCase.steptResults[i];
          this.changeTextColor(item.executeResult, i);
        }
      });
    }
  },
  methods: {
    stepResultChange(scope) {
      if (this.testCase.method === 'manual' || !this.testCase.method) {
        this.isFailure = this.testCase.steptResults.filter(s => {
          return s.executeResult === 'Failure' || s.executeResult === 'Blocking';
        }).length > 0;
      } else {
        this.isFailure = false;
      }

      this.changeTextColor(scope.row.executeResult, scope.$index);
    },
    // 同一行文本框高度保持一致
    resizeTextarea(scope) {
      resizeTextarea(3, scope ? scope.$index : null);
    },
    changeTextColor(val, index) {
      this.executeResultOption.forEach(item => {
        if (item.value === val) {
          let name = 'stepResultSelect' + index;
          // 改变下拉框颜色值
          this.$refs[name].$el.children[0].children[0].style.color = item.color;
          return;
        }
      });
    }
  }
}
</script>

<style scoped>
:deep(.table-edit-input .el-textarea__inner, .table-edit-input .el-input__inner) {
  border-style: solid;
}


.el-table :deep(td:nth-child(2) .cell,.el-table :deep(td:nth-child(2))),
.el-table :deep(td:nth-child(3) .cell,.el-table :deep(td:nth-child(3))),
.el-table :deep(td:nth-child(4) .cell,.el-table :deep(td:nth-child(4))) {
  padding: 0;
}

.el-table :deep(td:nth-child(1) .cell) {
  text-align: center;
}
</style>
