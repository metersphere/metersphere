<template>

  <el-form-item :label-width="labelWidth" prop="steps">
    <el-table
      :data="formData.steps"
      class="tb-edit"
      border
      size="mini"
      :default-sort="{prop: 'num', order: 'ascending'}"
      highlight-current-row>
      <el-table-column :label="$t('test_track.case.number')" prop="num" min-width="10%"></el-table-column>
      <el-table-column :label="$t('test_track.case.step_desc')" prop="desc" min-width="35%">
        <template v-slot:default="scope">
          <el-input
            class="table-edit-input"
            size="mini"
            :disabled="readOnly"
            type="textarea"
            :autosize="{ minRows: 1, maxRows: 6}"
            :rows="2"
            v-model="scope.row.desc"
            :placeholder="$t('commons.input_content')"
            clearable/>
        </template>
      </el-table-column>
      <el-table-column :label="$t('test_track.case.expected_results')" prop="result" min-width="35%">
        <template v-slot:default="scope">
          <el-input
            class="table-edit-input"
            size="mini"
            :disabled="readOnly"
            type="textarea"
            :autosize="{ minRows: 1, maxRows: 6}"
            :rows="2"
            v-model="scope.row.result"
            :placeholder="$t('commons.input_content')"
            clearable/>
        </template>
      </el-table-column>
      <el-table-column :label="$t('commons.input_content')" min-width="30%">
        <template v-slot:default="scope">
          <el-button
            type="primary"
            :disabled="readOnly"
            icon="el-icon-plus"
            circle size="mini"
            @click="handleAddStep(scope.$index, scope.row)"></el-button>
          <el-button
            icon="el-icon-document-copy"
            type="success"
            :disabled="readOnly"
            circle size="mini"
            @click="handleCopyStep(scope.$index, scope.row)"></el-button>
          <el-button
            type="danger"
            icon="el-icon-delete"
            circle size="mini"
            @click="handleDeleteStep(scope.$index, scope.row)"
            :disabled="readOnly || (scope.$index === 0 && formData.steps.length <= 1)"></el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-form-item>
</template>

<script>
export default {
  name: "TestCaseStepItem",
  props: {
    labelWidth: String,
    form: Object,
    readOnly: Boolean
  },
  computed:{
   formData() {
     return this.form;
   }
  },
  created() {

    if (!this.formData.steps || this.formData.steps.length < 1) {
      this.formData.steps = [{
        num: 1,
        desc: '',
        result: ''
      }];
    }
  },
  methods: {
    handleAddStep(index, data) {
      let step = {};
      step.num = data.num + 1;
      step.desc = "";
      step.result = "";
      this.formData.steps.forEach(step => {
        if (step.num > data.num) {
          step.num++;
        }
      });
      this.formData.steps.splice(index + 1, 0, step);
    },
    handleCopyStep(index, data) {
      let step = {};
      step.num = data.num + 1;
      step.desc = data.desc;
      step.result = data.result;
      this.formData.steps.forEach(step => {
        if (step.num > data.num) {
          step.num++;
        }
      });
      this.formData.steps.splice(index + 1, 0, step);
    },
    handleDeleteStep(index, data) {
      this.formData.steps.splice(index, 1);
      this.formData.steps.forEach(step => {
        if (step.num > data.num) {
          step.num--;
        }
      });
    },
  }
}
</script>

<style scoped>

</style>
