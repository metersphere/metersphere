<template>
  <div>
    <el-table
      :data="data"
      class="test-content document-table"
      style="width: 100%"
      ref="table"
    >
      <el-table-column
        prop="commitId"
        :label="$t('project.project_file.repository.file_version')"
        min-width="120px"
        show-overflow-tooltip
      />
      <el-table-column
        prop="commitMessage"
        :label="$t('project.project_file.repository.update_log')"
        min-width="200"
        show-overflow-tooltip
      />
      <el-table-column
        prop="operator"
        :label="$t('operating_log.user')"
        min-width="120"
        show-overflow-tooltip
      />
      <el-table-column
        prop="operatorTime"
        min-width="160"
        :label="$t('operating_log.time')"
        sortable
      >
        <template v-slot:default="scope">
          <span>{{ scope.row.operatorTime | datetimeFormat }}</span>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<script>
import { getFileVersion } from "@/api/file";

export default {
  name: "FileVersionList",
  data() {
    return {
      data: [],
    };
  },
  props: {
    fileMetadataRefId: String,
  },
  watch: {
    fileMetadataRefId() {
      this.selectData();
    },
  },
  created() {
    this.selectData();
  },
  methods: {
    selectData() {
      if (this.fileMetadataRefId) {
        getFileVersion(this.fileMetadataRefId).then((response) => {
          if (response.data) {
            this.data = response.data;
          }
          this.$nextTick(() => {
            if (this.$refs.table) {
              //加这一层判断时是因为有可能出现还未等到接口出现返回就关闭了窗口，导致前台报错
              this.$refs.table.doLayout();
            }
          });
        });
      }
    },
  },
};
</script>

<style scoped></style>
