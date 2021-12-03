<template>
  <div>
    <el-card class="table-card" v-loading="result.loading">
      <template v-slot:header>
        <ms-table-header :create-tip="btnTips" :condition.sync="condition" :show-create="!readOnly"
                         @search="search" @create="createEnvironment" :create-permission="['WORKSPACE_PROJECT_ENVIRONMENT:READ+CREATE_GROUP']">
        </ms-table-header>
      </template>
      <el-table :data="environmentGroupList"
                style="width: 100%" ref="table"
                row-key="id"
                @expand-change="expandChange"
                :height="screenHeight">
        <el-table-column type="expand" prop="id">
          <template v-slot:default="scope">
            <environment-group-row :env-group-id="scope.row.id" ref="environmentGroupRow"
                                   :read-only="!scope.row.readOnly"
                                   style="overflow-x:hidden; overflow-y: auto; height: 180px;"/>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.name')" prop="name" show-overflow-tooltip min-width="200">
          <template v-slot:default="scope">
            <span @click.stop v-if="scope.row.showNameInput">
              <el-input size="mini" v-model="scope.row.name" class="name-input" @blur="updateGroupName(scope.row)" show-word-limit
                        maxlength="50" :placeholder="$t('commons.input_name')" ref="nameEdit"/>
            </span>
            <span v-else>
              <span>{{ scope.row.name }}</span>
              <i class="el-icon-edit" style="cursor:pointer;margin-left: 4px;" @click="editName(scope.row)" v-permission="['WORKSPACE_PROJECT_ENVIRONMENT:READ+EDIT_GROUP']"/>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('commons.create_time')" min-width="200">
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.operating')" width="350">
          <template v-slot:default="scope">
            <div v-if="!readOnly">
              <ms-table-operator @editClick="editEnvironment(scope.row)"
                                 :edit-permission="['WORKSPACE_PROJECT_ENVIRONMENT:READ+EDIT_GROUP']"
                                 :delete-permission="['WORKSPACE_PROJECT_ENVIRONMENT:READ+DELETE_GROUP']"
                                 @deleteClick="deleteEnvironment(scope.row)">
                <template v-slot:middle>
                  <ms-table-operator-button :tip="$t('commons.copy')" @exec="copyEnvironment(scope.row)"
                                            v-permission="['WORKSPACE_PROJECT_ENVIRONMENT:READ+COPY_GROUP']"
                                            icon="el-icon-document-copy" type="info"/>
                </template>
              </ms-table-operator>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="init" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </el-card>

    <edit-environment-group ref="editEnvironmentGroup" @refresh="init"/>
    <ms-delete-confirm :title="$t('workspace.env_group.delete')" @delete="_handleDelete" ref="deleteConfirm"/>

  </div>
</template>

<script>
import MsTableHeader from "@/business/components/common/components/MsTableHeader";
import MsTableButton from "@/business/components/common/components/MsTableButton";
import MsTableOperator from "@/business/components/common/components/MsTableOperator";
import MsTableOperatorButton from "@/business/components/common/components/MsTableOperatorButton";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import ApiEnvironmentConfig from "@/business/components/api/test/components/ApiEnvironmentConfig";
import EnvironmentEdit from "@/business/components/api/test/components/environment/EnvironmentEdit";
import MsAsideItem from "@/business/components/common/components/MsAsideItem";
import MsAsideContainer from "@/business/components/common/components/MsAsideContainer";
import ProjectSwitch from "@/business/components/common/head/ProjectSwitch";
import EnvironmentImport from "@/business/components/project/menu/EnvironmentImport";
import EnvironmentGroupRow from "@/business/components/settings/workspace/environment/EnvironmentGroupRow";
import EditEnvironmentGroup from "@/business/components/settings/workspace/environment/EditEnvironmentGroup";
import MsDeleteConfirm from "@/business/components/common/components/MsDeleteConfirm";

export default {
  name: "EnvironmentGroup",
  components: {
    EditEnvironmentGroup,
    EnvironmentImport,
    ProjectSwitch,
    MsAsideContainer,
    MsAsideItem,
    EnvironmentEdit,
    ApiEnvironmentConfig,
    MsTablePagination,
    MsTableOperatorButton,
    MsTableOperator,
    MsTableButton,
    MsTableHeader,
    EnvironmentGroupRow,
    MsDeleteConfirm
  },
  data() {
    return {
      btnTips: this.$t('workspace.env_group.create'),
      envGroupId: '',
      condition: {},
      environmentGroupList: [],
      result: {},
      currentPage: 1,
      pageSize: 10,
      total: 0,
      showNameInput: false
    }
  },
  props: {
    screenHeight: {
      type: String,
      default() {
        return 'calc(100vh - 210px)';
      }
    },
    readOnly: {
      type: Boolean,
      default() {
        return false;
      }
    }
  },
  created() {
    this.init();
  },
  activated() {
    this.init();
  },
  methods: {
    init() {
      this.result = this.$post("/environment/group/list/" + this.currentPage + '/' + this.pageSize, this.condition, res => {
        let data = res.data;
        let {listObject, itemCount} = data;
        this.environmentGroupList = listObject;
        this.total = itemCount;
      })
    },
    createEnvironment() {
      this.$refs.editEnvironmentGroup.open();
    },
    editEnvironment(row) {
      this.$refs.table.toggleRowExpansion(row, true);
      this.$set(row, "readOnly", true);
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
        this.$get("/environment/group/copy/" + row.id, () => {
          this.$success(this.$t("commons.copy_success"));
          this.init();
        });
      }
    },
    search() {
      this.init();
    },
    _handleDelete(row) {
      this.$get('/environment/group/delete/' + row.id, () => {
        this.$success(this.$t('commons.delete_success'));
        this.init();
      })
    },
    expandChange(row, expanded) {
      if (expanded) {
        this.$set(row, "readOnly", false);
      }
    },
    editName(row) {
      this.$set(row, "showNameInput", true);
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
        workspaceId: row.workspaceId
      };
      this.$post("/environment/group/modify", param, () => {
        this.$success(this.$t("commons.modify_success"));
        this.init();
      })
    }
  },
}
</script>

<style scoped>

</style>
