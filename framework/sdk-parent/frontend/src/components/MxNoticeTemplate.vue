<template>
  <div>
    <el-dialog @close="cancel"
               :destroy-on-close="true"
               :close-on-click-modal="false"
               :visible.sync="dialogFormVisible" :append-to-body=true>
      <template v-slot:title>
        <span>
        {{ $t('organization.message.template') }}
        </span>
        <el-popover
          placement="bottom"
          width="450"
          trigger="hover">
          <el-table :data="variables" height="500">
            <el-table-column prop="label" :label="$t('schema.description')"/>
            <el-table-column prop="value" :label="$t('load_test.param_name')">
              <template v-slot:default="scope">
                ${<span>{{ scope.row.value }}</span>}
              </template>
            </el-table-column>
          </el-table>
          <i class="el-icon-info" slot="reference" style="margin-right: 10px;"></i>
        </el-popover>
      </template>
      <el-input
        type="textarea"
        :rows="5"
        v-model="currentRow.template"
        :placeholder="content"
        @focus="focusContent"
      >
      </el-input>
      <div slot="footer" class="dialog-footer">
        <el-button @click="cancel" size="mini">{{ $t('commons.cancel') }}</el-button>
        <el-button type="primary" @click="saveTemplate()" size="mini">{{ $t('commons.confirm') }}</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>

import {saveNoticeTemplate} from "../api/notice";

export default {
  name: "MxNoticeTemplate",
  props: {
    variables: {
      type: Array,
      default() {
        return [
          {
            label: this.$t('group.operator'),
            value: 'operator',
          },
          {
            label: this.$t('commons.name'),
            value: 'name',
          }];
      }
    }
  },
  data() {
    return {
      dialogFormVisible: false,
      currentRow: {},
      content: ""
    };
  },
  methods: {
    saveTemplate() {
      if (this.currentRow.template == "undefined" || this.currentRow.template == "" || this.currentRow.template == null) {
        this.currentRow.template = "";
        saveNoticeTemplate(this.currentRow).then(response => {
          this.$success(this.$t('commons.save_success'));
          this.dialogFormVisible = false;
        });
      } else {
        saveNoticeTemplate(this.currentRow).then(response => {
          this.$success(this.$t('commons.save_success'));
          this.dialogFormVisible = false;
        });
      }

    },
    open(row, robotTemplate) {
      this.dialogFormVisible = true;
      this.currentRow = row;
      if (("template" in row) && row.template !== null && row.template !== "") {
        if (row.template.length > 0) {
          this.currentRow = row;
        }
      } else {
        this.currentRow = row;
        this.content = robotTemplate;
      }
    },
    focusContent() {
      if (!this.currentRow.template) {
        this.$set(this.currentRow, 'template', this.content);
      }
    },
    cancel() {
      this.dialogFormVisible = false;
    }
  },
};
</script>

<style scoped>

</style>
