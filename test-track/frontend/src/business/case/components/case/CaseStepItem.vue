<template>
  <el-form-item prop="steps">
    <el-table
      :data="form.steps"
      class="tb-edit"
      size="mini"
      :default-sort="{ prop: 'num', order: 'ascending' }"
      :header-cell-style="{
        color: '#646A73',
        fontSize: '14px',
        lineHeight: '22px',
        fontWeight: 500,
      }"
    >
      <el-table-column
        label="序号"
        prop="num"
        min-width="10%"
      ></el-table-column>
      <el-table-column
        :label="$t('test_track.case.step_desc')"
        prop="desc"
        min-width="35%"
      >
        <template v-slot:default="scope">
          <el-input
            v-model="scope.row.desc"
            size="mini"
            type="textarea"
            class="table-edit-input sync-textarea"
            :disabled="readOnly"
            :rows="defaultRows"
            :placeholder="$t('commons.input_content')"
            @input="resizeTextarea(scope)"
          />
        </template>
      </el-table-column>
      <el-table-column
        :label="$t('test_track.case.expected_results')"
        prop="result"
        min-width="35%"
      >
        <template v-slot:default="scope">
          <el-input
            v-model="scope.row.result"
            clearable
            size="mini"
            type="textarea"
            class="table-edit-input sync-textarea"
            :rows="defaultRows"
            :disabled="readOnly"
            :placeholder="$t('commons.input_content')"
            @input="resizeTextarea(scope)"
          />
        </template>
      </el-table-column>
      <el-table-column :label="$t('commons.operating')" min-width="8%">
        <template v-slot:default="scope">
          <el-popover
            placement="bottom-start"
            trigger="click"
            popper-class="case-step-item-popover"
            :visible-arrow="false"
          >
            <div class="opt-row">
              <div
                class="copy-row sub-opt-row"
                @click="handleCopyStep(scope.$index, scope.row)"
              >
                <div class="icon">
                  <i class="el-icon-copy-document"></i>
                </div>
                <div class="title">复制该步骤</div>
              </div>
              <div class="split"></div>
              <div
                class="delete-row sub-opt-row"
                @click="handleDeleteStep(scope.$index, scope.row)"
                :class="
                  readOnly || (scope.$index == 0 && form.steps.length <= 1)
                    ? 'opt-readonly'
                    : ''
                "
              >
                <div class="icon">
                  <i class="el-icon-delete"></i>
                </div>
                <div class="title">删除</div>
              </div>
            </div>
            <i slot="reference" class="el-icon-more"></i>
          </el-popover>
          <!-- <el-button
            type="primary"
            :disabled="readOnly"
            icon="el-icon-plus"
            circle
            size="mini"
            @click="handleAddStep(scope.$index, scope.row)"
          ></el-button>
          <el-button
            icon="el-icon-document-copy"
            type="success"
            :disabled="readOnly"
            circle
            size="mini"
            @click="handleCopyStep(scope.$index, scope.row)"
          ></el-button>
          <el-button
            type="danger"
            icon="el-icon-delete"
            circle
            size="mini"
            @click="handleDeleteStep(scope.$index, scope.row)"
            :disabled="
              readOnly || (scope.$index == 0 && form.steps.length <= 1)
            "
          ></el-button> -->
        </template>
      </el-table-column>
    </el-table>
    <div class="add-step-row">
      <div class="add-icon" @click="handleAddStepStandAlone">
        <i class="el-icon-plus"></i>
      </div>
      <div class="add-label" @click="handleAddStepStandAlone">添加步骤</div>
    </div>
  </el-form-item>
</template>

<script>
import { resizeTextarea } from "@/business/utils/sdk-utils";

export default {
  name: "CaseStepItem",
  props: {
    labelWidth: String,
    form: Object,
    readOnly: Boolean,
  },
  data() {
    return {
      defaultRows: 2,
    };
  },
  created() {
    if (!this.form.steps || this.form.steps.length < 1) {
      this.form.steps = [
        {
          num: 1,
          desc: "",
          result: "",
        },
      ];
    }
  },
  watch: {
    "form.steps"() {
      this.$nextTick(() => {
        this.resizeTextarea();
      });
    },
  },
  methods: {
    handleAddStepStandAlone() {
      let step = {};
      let index = this.form.steps.length || 0;
      step.num = index + 1;
      step.desc = "";
      step.result = "";
      this.form.steps.splice(index + 1, 0, step);
    },
    handleAddStep(index, data) {
      let step = {};
      step.num = data.num + 1;
      step.desc = "";
      step.result = "";
      this.form.steps.forEach((step) => {
        if (step.num > data.num) {
          step.num++;
        }
      });
      this.form.steps.splice(index + 1, 0, step);
    },
    handleCopyStep(index, data) {
      let step = {};
      step.num = data.num + 1;
      step.desc = data.desc;
      step.result = data.result;
      this.form.steps.forEach((step) => {
        if (step.num > data.num) {
          step.num++;
        }
      });
      this.form.steps.splice(index + 1, 0, step);
    },
    handleDeleteStep(index, data) {
      if (index == 0 && data.length <= 1) {
        return;
      }
      this.form.steps.splice(index, 1);
      this.form.steps.forEach((step) => {
        if (step.num > data.num) {
          step.num--;
        }
      });
    },
    // 同一行文本框高度保持一致
    resizeTextarea(scope) {
      resizeTextarea(2, scope ? scope.$index : null);
    },
  },
};
</script>

<style scoped>
.el-table :deep(td:nth-child(2) .cell),
.el-table :deep(td:nth-child(2)),
.el-table :deep(td:nth-child(3) .cell),
.el-table :deep(td:nth-child(3)) {
  padding: 0;
}

/* .el-table :deep(td:nth-child(1) .cell) {
  text-align: center;
} */

.add-step-row {
  width: 100%;
  height: 38px;
  line-height: 38px;
  display: flex;
  border-bottom: 1px solid rgba(31, 35, 41, 0.15);
}
.add-icon {
  margin-left: 13px;
  margin-right: 8px;
  /* ms */
  color: #783887;
}
.add-label {
  font-family: "PingFang SC";
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  /* identical to box height, or 157% */
  display: flex;
  align-items: center;
  letter-spacing: -0.1px;
  /* ms */
  color: #783887;
}
</style>
<style>
.case-step-item-popover {
  padding: 0 !important;
  min-width: 118px !important;
}
.case-step-item-popover .opt-row {
  display: flex;
  flex-direction: column;
  align-items: center;
}
.case-step-item-popover .sub-opt-row {
  display: flex;
  width: 118px;
  height: 32px;
  margin-top: 4px;
  margin-bottom: 4px;
  line-height: 32px;
}
.case-step-item-popover .sub-opt-row .icon {
  margin-left: 13px;
  margin-right: 9.33px;
}
.case-step-item-popover .sub-opt-row .title {
  font-family: "PingFang SC";
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  /* identical to box height, or 157% */

  display: flex;
  align-items: center;
  letter-spacing: -0.1px;
}
.case-step-item-popover .split {
  width: 118px;
  height: 1px;
  background-color: rgba(31, 35, 41, 0.15);
}
.case-step-item-popover .copy-row .icon {
  color: #646a73;
}
.case-step-item-popover .copy-row .title {
  color: #1f2329;
}
.case-step-item-popover .delete-row .icon {
  color: #f54a45;
}
.case-step-item-popover .delete-row .title {
  color: #f54a45;
}
.opt-readonly {
  pointer-events: none;
  color: #fab6b6;
  cursor: not-allowed;
}
</style>
