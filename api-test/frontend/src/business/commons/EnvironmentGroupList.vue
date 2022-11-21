<template>
  <div>
    <el-card class="table-card" v-loading="result">
      <template v-slot:header>
        <ms-table-header
          :create-tip="btnTips"
          :condition.sync="condition"
          :show-create="!readOnly"
          @search="search"
          @create="createEnvironment"
          :create-permission="['WORKSPACE_PROJECT_ENVIRONMENT:READ+CREATE_GROUP']">
        </ms-table-header>
      </template>
      <el-table
        :data="environmentGroupList"
        style="width: 100%"
        ref="table"
        row-key="id"
        @expand-change="expandChange"
        :height="screenHeight">
        <el-table-column type="expand" prop="id">
          <template v-slot:default="scope">
            <environment-group-row
              :env-group-id="scope.row.id"
              ref="environmentGroupRow"
              :read-only="!scope.row.readOnly"
              style="overflow-x: hidden; overflow-y: auto; height: 180px" />
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.name')" prop="name" show-overflow-tooltip min-width="200">
          <template v-slot:default="scope">
            <span @click.stop v-if="scope.row.showNameInput">
              <el-input
                size="mini"
                v-model="scope.row.name"
                class="name-input"
                @blur="updateGroupName(scope.row)"
                show-word-limit
                maxlength="50"
                :placeholder="$t('commons.input_name')"
                ref="nameEdit" />
            </span>
            <span v-else>
              <span>{{ scope.row.name }}</span>
              <i
                class="el-icon-edit"
                style="cursor: pointer; margin-left: 4px"
                @click="editName(scope.row)"
                v-permission="['WORKSPACE_PROJECT_ENVIRONMENT:READ+EDIT_GROUP']" />
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('commons.create_time')" min-width="200">
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | datetimeFormat }}</span>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="init" :current-page.sync="currentPage" :page-size.sync="pageSize" :total="total" />
    </el-card>

    <edit-environment-group ref="editEnvironmentGroup" @refresh="init" />
    <ms-delete-confirm :title="$t('workspace.env_group.delete')" @delete="_handleDelete" ref="deleteConfirm" />
  </div>
</template>

<script>
import {
  copyEnvironment,
  delEnvironment,
  envGroupList,
  environmentGroupModify,
} from 'metersphere-frontend/src/api/environment';
import MsTableHeader from 'metersphere-frontend/src/components/MsTableHeader';
import MsTableButton from 'metersphere-frontend/src/components/MsTableButton';
import MsTableOperator from 'metersphere-frontend/src/components/MsTableOperator';
import MsTableOperatorButton from 'metersphere-frontend/src/components/MsTableOperatorButton';
import MsTablePagination from 'metersphere-frontend/src/components/pagination/TablePagination';
import ApiEnvironmentConfig from 'metersphere-frontend/src/components/environment/ApiEnvironmentConfig';
import MsAsideItem from 'metersphere-frontend/src/components/MsAsideItem';
import MsAsideContainer from 'metersphere-frontend/src/components/MsAsideContainer';
import ProjectSwitch from 'metersphere-frontend/src/components/head/ProjectSwitch';
import EnvironmentGroupRow from './EnvironmentGroupRow';
import EditEnvironmentGroup from './EditEnvironmentGroup';
import MsDeleteConfirm from 'metersphere-frontend/src/components/MsDeleteConfirm';

export default {
  name: 'EnvironmentGroup',
  components: {
    EditEnvironmentGroup,
    ProjectSwitch,
    MsAsideContainer,
    MsAsideItem,
    ApiEnvironmentConfig,
    MsTablePagination,
    MsTableOperatorButton,
    MsTableOperator,
    MsTableButton,
    MsTableHeader,
    EnvironmentGroupRow,
    MsDeleteConfirm,
  },
  data() {
    return {
      btnTips: this.$t('workspace.env_group.create'),
      envGroupId: '',
      condition: {},
      environmentGroupList: [],
      result: false,
      currentPage: 1,
      pageSize: 10,
      total: 0,
      showNameInput: false,
    };
  },
  props: {
    screenHeight: {
      type: String,
      default() {
        return 'calc(100vh - 170px)';
      },
    },
    readOnly: {
      type: Boolean,
      default() {
        return false;
      },
    },
  },
  created() {
    this.init();
  },
  activated() {
    this.init();
  },
  methods: {
    init() {
      this.result = envGroupList(this.currentPage, this.pageSize, this.condition).then((res) => {
        let data = res.data;
        let { listObject, itemCount } = data;
        this.environmentGroupList = listObject;
        this.total = itemCount;
      });
    },
    createEnvironment() {
      this.$refs.editEnvironmentGroup.open();
    },
    editEnvironment(row) {
      this.$refs.table.toggleRowExpansion(row, true);
      this.$set(row, 'readOnly', true);
    },
    deleteEnvironment(row) {
      if (row.system) {
        this.$warning(this.$t('group.admin_not_allow_delete'));
        return;
      }
      this.$refs.deleteConfirm.open(row);
    },
    copyEnvironment(row) {
      if (row && row.id) {
        copyEnvironment(row.id).then(() => {
          this.$success(this.$t('commons.copy_success'));
          this.init();
        });
      }
    },
    search() {
      this.init();
    },
    _handleDelete(row) {
      delEnvironment(row.id).then(() => {
        this.$success(this.$t('commons.delete_success'));
        this.init();
      });
    },
    expandChange(row, expanded) {
      if (expanded) {
        this.$set(row, 'readOnly', false);
      }
    },
    editName(row) {
      this.$set(row, 'showNameInput', true);
      this.$nextTick(() => {
        this.$refs.nameEdit.focus();
      });
    },
    updateGroupName(row) {
      if (!row || !row.id || !row.name) {
        this.init();
        return false;
      }
      let param = {
        id: row.id,
        name: row.name,
        workspaceId: row.workspaceId,
      };
      environmentGroupModify(param).then(() => {
        this.$success(this.$t('commons.modify_success'));
        this.init();
      });
    },
  },
};
</script>

<style scoped></style>
