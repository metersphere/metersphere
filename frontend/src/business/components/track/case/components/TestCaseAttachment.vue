<template>
  <div>
    <el-row type="flex" justify="center">
      <el-col>
        <el-table class="basic-config" :data="tableData">
          <el-table-column
            prop="name"
            :label="$t('load_test.file_name')">
            <template v-slot:default="scope">
              <el-tooltip class="item" effect="dark" :content="scope.row.name" placement="top">
                <el-progress
                  :color="scope.row.progress >= 100 ? '' : uploadProgressColor"
                  type="line"
                  :format="clearPercentage(scope.row)"
                  :stroke-width="40"
                  :text-inside="true"
                  :percentage="scope.row.progress >= 100 ? 100 : scope.row.progress"
                ></el-progress>
              </el-tooltip>
            </template>
          </el-table-column>
          <el-table-column
            prop="size"
            :width="100"
            :label="$t('load_test.file_size')">
          </el-table-column>
          <el-table-column
            :width="200"
            :label="$t('test_track.case.upload_time')">
            <template v-slot:default="scope">
              <i class="el-icon-time"/>
              <span class="last-modified">{{ scope.row.updateTime | timestampFormatDate }}</span>
            </template>
          </el-table-column>
          <el-table-column
            prop="status"
            :width="70"
            :label="$t('commons.status')">
            <template v-slot:default="scope">
              <span :class="scope.row.status === 'success' ? 'green' : scope.row.status === 'error' ? 'red' : ''">{{ scope.row.status | formatStatus}}</span>
            </template>
          </el-table-column>
          <el-table-column
            prop="creator"
            :width="120"
            :label="$t('group.operator')">
          </el-table-column>
          <el-table-column
            :width="140"
            :label="$t('commons.operating')">
            <template v-slot:default="scope">
              <el-button @click="preview(scope.row)" :disabled="!scope.row.id" type="primary"
                         v-if="scope.row.progress === 100 && isPreview(scope.row)"
                         icon="el-icon-view" size="mini" circle/>
              <el-button @click="handleDownload(scope.row)"  type="primary" :disabled="!scope.row.id"
                         v-if="scope.row.progress === 100"
                         icon="el-icon-download" size="mini" circle/>
              <el-button :disabled="readOnly || !isDelete || isCopy || !scope.row.id"
                         @click="handleDelete(scope.row, scope.$index)" type="danger"
                         v-if="scope.row.progress === 100"
                         icon="el-icon-delete" size="mini"
                         circle/>
              <el-button :disabled="readOnly || !isDelete" @click="handleCancel(scope.row, scope.$index)" type="danger"
                         v-if="scope.row.progress < 100"
                         icon="el-icon-close" size="mini"
                         circle/>
            </template>
          </el-table-column>
        </el-table>
      </el-col>
    </el-row>

    <test-case-file ref="testCaseFile"/>
  </div>
</template>

<script>
import TestCaseFile from "@/business/components/track/case/components/TestCaseFile";
import DownloadNotice from "@/business/components/notice/DownloadNotice";

export default {
  name: "TestCaseAttachment",
  components: {TestCaseFile},
  props: {
    tableData: Array,
    readOnly: {
      type: Boolean,
      default: false
    },
    isDelete: {
      type: Boolean,
      default: false
    },
    isCopy: {
      type: Boolean,
      default: false
    }
  },
  mixins: [DownloadNotice],
  data() {
    return {
      uploadProgressColor: '#d4f6d4',
      uploadSuccessColor: '#FFFFFF'
    }
  },
  methods: {
    clearPercentage(row) {
      return () => {
        return row.name;
      }
    },
    preview(row) {
      this.$refs.testCaseFile.open(row);
    },
    isPreview(row) {
      const fileType = row.type;
      return fileType === 'JPG' || fileType === 'JPEG' || fileType === 'PDF' || fileType === 'PNG';
    },
    handleDownload(file) {
      this.$fileDownloadPost('/attachment/download', {
        name: file.name,
        id: file.id,
      });
    },
    handleDelete(file, index) {
      this.$emit("handleDelete", file, index);
    },
    handleCancel(file, index) {
      this.$emit("handleCancel", file, index);
    },
  },
  filters: {
    formatStatus(status) {
      if (isNaN(status)) {
        return status === 'success' ? '完成' : '失败'
      }
      return Math.floor(status * 100 / 100) + "%";
    }
  }
}
</script>

<style scoped>
.el-progress-bar__innerText {
  color: black;
}

::v-deep .el-progress-bar__outer,
::v-deep .el-progress-bar__inner {
  border-radius: inherit ;
  background-color: transparent;
}

::v-deep .el-progress-bar__innerText {
  float: left;
  color: #606266;
  margin-top: 15px;
}

.green {
  color: green;
}

.red {
  color: red;
}
</style>
