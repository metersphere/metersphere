<template>
  <el-card>

    <template v-slot:header>
      <ms-table-header :is-tester-permission="true" :condition.sync="condition" @search="getFiles" @create="handleCreate"
                       icon="el-icon-upload2" :create-tip="$t('load_test.upload_file')" :title="$t('project.file_manager')"/>
    </template>

    <ms-table
      v-loading="result.loading"
      :data="tableData"
      :condition="condition"
      :total="total"
      :page-size.sync="pageSize"
      :operators="operators"
      @handlePageChange="getFiles"
      @refresh="getFiles">

      <ms-table-column
        :label="$t('project.custom_name')"
        :fields="fields"
        prop="name">
        <template v-slot="scope">
          <span> {{scope.row.name}} </span>
        </template>
      </ms-table-column>

      <ms-table-column
        :label="$t('load_test.file_type')"
        :fields="fields"
        prop="type">
        <template v-slot="scope">
          <span>{{ scope.row.type }}</span>
        </template>
      </ms-table-column>

      <ms-table-column
        :label="$t('load_test.file_name')"
        :fields="fields"
        prop="fileName">
        <template v-slot="scope">
          <span>{{ scope.row.fileName }}</span>
        </template>
      </ms-table-column>

      <ms-table-column
        :label="$t('custom_field.issue_creator')"
        :fields="fields"
        prop="creator">
        <template v-slot="scope">
          {{scope.row.creator == null? '——' : scope.row.creator}}
        </template>
      </ms-table-column>

      <ms-table-column
        :label="$t('commons.modifier')"
        :fields="fields"
        prop="modifier">
        <template v-slot="scope">
          {{scope.row.modifier == null? '——' : scope.row.modifier}}
        </template>
      </ms-table-column>

      <ms-table-column
        sortable
        :label="$t('commons.create_time')"
        :fields="fields"
        prop="createTime">
        <template v-slot="scope">
          <span>{{ scope.row.createTime | timestampFormatDate }}</span>
        </template>
      </ms-table-column>

      <ms-table-column
        sortable
        :label="$t('commons.update_time')"
        :fields="fields"
        prop="updateTime">
        <template v-slot="scope">
          <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
        </template>
      </ms-table-column>

    </ms-table>

    <ms-table-pagination :change="getFiles" :current-page.sync="currentPage" :page-size.sync="pageSize" :total="total"/>

    <files-config-form
      @refresh="getFiles"
      :callback="saveFile"
      :config="currentConfig"
      :accept-file-string="'.jar,.csv,.json,.pdf,.jpg,.png,.jpeg,.doc,.docx,.xlsx,.txt,.jmx'"
      :accept-file-type="['jar', 'csv', 'json', 'pdf', 'jpg', 'png', 'jpeg', 'doc', 'docx', 'xlsx', 'txt', 'jmx']"
      ref="fileConfigForm">

    </files-config-form>
  </el-card>
</template>

<script>
  import MsTable from "@/business/components/common/components/table/MsTable";
  import {getCurrentWorkspaceId} from "@/common/js/utils";
  import MsTableColumn from "@/business/components/common/components/table/Ms-table-column";
  import MsTableOperators from "@/business/components/common/components/MsTableOperators";
  import MsTableButton from "@/business/components/common/components/MsTableButton";
  import CustomFieldEdit from "@/business/components/settings/workspace/template/CustomFieldEdit";
  import MsTablePagination from "@/business/components/common/pagination/TablePagination";
  import MsTableHeader from "@/business/components/common/components/MsTableHeader";
  import FilesConfigForm from "./FilesConfigForm";
  export default {
    name: "FileList",
    components: {
      FilesConfigForm,
      MsTableHeader,
      MsTablePagination, CustomFieldEdit, MsTableButton, MsTableOperators, MsTableColumn, MsTable},
    data() {
      return {
        tableData: [],
        condition: {},
        total: 0,
        pageSize: 10,
        currentPage: 1,
        currentConfig: {},
        result: {},
        operators: [
          {
            tip: this.$t('commons.edit'), icon: "el-icon-edit",
            exec: this.handleEdit
          },
          {
            tip: this.$t('commons.delete'), icon: "el-icon-delete", type: "danger",
            exec: this.handleDelete
          }
        ],
      };
    },
    activated() {
      this.getFiles();
    },
    computed: {
      fields() {
        return new Set([
          'name',
          'type',
          'fileName',
          'creator',
          'modifier',
          'createTime',
          'updateTime'
        ]);
      },
    },
    methods: {
      saveFile(type, url, projectId, fileConfig, file) {
        if(type === 'update') {
          this.result = this.$fileUpload(url, file, null, fileConfig, () => {
            this.$success(this.$t('commons.save_success'));
            this.getFiles();
          });
        } else if(type === 'add') {
          this.$fileUpload(url + '/' + projectId, file, null, null, () => {
            this.$success(this.$t('commons.save_success'));
            this.getFiles();
          });
        }
      },
      getFiles() {
        if(this.condition.name === null || this.condition.name === '') {
          this.condition.workspaceId = getCurrentWorkspaceId();
          this.result = this.$post('file/listAll/' + this.currentPage + '/' + this.pageSize,
            this.condition, (response) => {
              let data = response.data;
              this.total = data.itemCount;
              this.tableData = data.listObject;
            });
        } else {  //  关键字搜索，根据文件名和名称
          let file = {name: this.condition.name};
          this.currentPage = 1;
          this.result = this.$post('file/search/' + this.currentPage + '/' + this.pageSize, file, (response) => {
            let data = response.data;
            this.total = data.itemCount;
            this.tableData = data.listObject;
          });
        }
      },
      handleEdit(data) {
        this.currentConfig = data;
        this.$refs.fileConfigForm.open(data);
      },
      handleCreate(data) {
        this.$refs.fileConfigForm.open(data);
      },
      handleDelete(data) {
        this.result = this.$get('/file/delete/' + data.id,  () => {
          this.$success(this.$t('commons.delete_success'));
          this.getFiles();
        });
      }
    }
  };
</script>

<style scoped>
  /deep/ .el-table__fixed-body-wrapper {
    top: 58PX !IMPORTANT;
  }
</style>
