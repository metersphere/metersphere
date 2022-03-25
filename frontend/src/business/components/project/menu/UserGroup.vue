<template>
  <ms-container>
    <ms-main-container>
      <div v-loading="result.loading">
        <el-card class="table-card">
          <template v-slot:header>
            <ms-table-header :create-permission="['PROJECT_GROUP:READ+CREATE']"
                             :condition.sync="condition" @search="initData" @create="create"
                             :create-tip="$t('group.create')"/>
          </template>

          <el-table :data="groups" border class="adjust-table" style="width: 100%"
                    :height="screenHeight" @sort-change="sort">
            <el-table-column prop="name" :label="$t('commons.name')" show-overflow-tooltip/>
            <el-table-column prop="type" :label="$t('group.type')">
              <template v-slot="scope">
                <span>{{ userGroupType[scope.row.type] ? $t(userGroupType[scope.row.type]) : scope.row.type }}</span>
              </template>
            </el-table-column>
            <el-table-column :label="$t('commons.member')" width="100">
              <template v-slot:default="scope">
                <el-link type="primary" class="member-size" @click="memberClick(scope.row)">
                  {{ scope.row.memberSize || 0 }}
                </el-link>
              </template>
            </el-table-column>
            <el-table-column prop="scopeName" :label="$t('group.scope')">
              <template v-slot="scope">
                <span v-if="scope.row.scopeId ==='global'">{{ $t('group.global') }}</span>
                <span v-else>{{ scope.row.scopeName }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" :label="$t('commons.create_time')" sortable show-overflow-tooltip>
              <template v-slot:default="scope">
                <span>{{ scope.row.createTime | timestampFormatDate }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="updateTime" :label="$t('commons.update_time')" sortable show-overflow-tooltip>
              <template v-slot:default="scope">
                <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="creator" :label="$t('group.operator')"/>
            <el-table-column prop="description" :label="$t('group.description')"/>
            <el-table-column :label="$t('commons.operating')" min-width="120">
              <template v-slot:default="scope">
                <div>
                  <ms-table-operator :edit-permission="['PROJECT_GROUP:READ+EDIT']"
                                     :delete-permission="['PROJECT_GROUP:READ+DELETE']"
                                     @editClick="edit(scope.row)" @deleteClick="del(scope.row)"
                                     :isShow="flagChange(scope.row.scopeId ==='global')">
                    <template v-slot:middle>
                      <!--                <ms-table-operator-button tip="复制" icon="el-icon-document-copy" @exec="copy(scope.row)"/>-->
                      <ms-table-operator-button
                        v-permission="['PROJECT_GROUP:READ+SETTING_PERMISSION']"
                        :tip="$t('group.set_permission')"
                        icon="el-icon-s-tools"
                        @exec="setPermission(scope.row)"
                        :disabled="flagChange(scope.row.scopeId ==='global')"/>
                      <ms-table-operator-button :tip="$t('group.view_permission')" icon="el-icon-view" @exec="viewPermission(scope.row)"/>
                    </template>
                  </ms-table-operator>
                </div>
              </template>
            </el-table-column>
          </el-table>

          <ms-table-pagination :change="initData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                               :total="total"/>
        </el-card>
        <group-member ref="groupMember" @refresh="initData"/>
        <edit-user-group ref="editUserGroup" @refresh="initData"/>
        <edit-permission ref="editPermission"/>
        <ms-delete-confirm :title="$t('group.delete')" @delete="_handleDel" ref="deleteConfirm"/>
      </div>
    </ms-main-container>
  </ms-container>
</template>

<script>
import MsTableHeader from "@/business/components/common/components/MsTableHeader";
import MsTableOperator from "@/business/components/common/components/MsTableOperator";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import {USER_GROUP_SCOPE} from "@/common/js/table-constants";
import EditUserGroup from "@/business/components/project/menu/EditUserGroup";
import MsTableOperatorButton from "@/business/components/common/components/MsTableOperatorButton";
import EditPermission from "@/business/components/settings/system/group/EditPermission";
import MsDeleteConfirm from "@/business/components/common/components/MsDeleteConfirm";
import {_sort} from "@/common/js/tableUtils";
import GroupMember from "@/business/components/settings/system/group/GroupMember";
import {getCurrentProjectID, getCurrentUserId, getCurrentWorkspaceId} from "@/common/js/utils";
import MsContainer from "@/business/components/common/components/MsContainer";
import MsMainContainer from "@/business/components/common/components/MsMainContainer";

export default {
  name: "UserGroup",
  components: {
    MsMainContainer,
    MsContainer,
    GroupMember,
    EditUserGroup,
    MsTableHeader,
    MsTableOperator,
    MsTablePagination,
    MsTableOperatorButton,
    EditPermission,
    MsDeleteConfirm
  },
  data() {
    return {
      result: {},
      condition: {},
      currentPage: 1,
      pageSize: 10,
      total: 0,
      screenHeight: 'calc(100vh - 200px)',
      groups: [],
      currentGroup: {},
      flag: false
    };
  },
  created() {
    this.$get("/user/group/list/ws/" + getCurrentWorkspaceId() + "/" + getCurrentUserId(), res => {
      let data = res.data;
      if (data) {
        data.forEach(row => {
          if (row.id === 'ws_admin') {
            this.flag = true;
          }
        })
      }
    })
  },
  activated() {
    this.initData();
  },
  computed: {
    userGroupType() {
      return USER_GROUP_SCOPE;
    },
    projectId() {
      return getCurrentProjectID();
    },
  },
  methods: {
    initData() {
      this.condition.projectId = this.projectId;
      if (this.projectId) {
        this.result = this.$post("/user/group/get/" + this.currentPage + "/" + this.pageSize, this.condition, res => {
          let data = res.data;
          if (data) {
            let {itemCount, listObject} = data;
            this.total = itemCount;
            this.groups = listObject;
          }
        });
      }
    },
    flagChange(data) {
      if (this.flag) {
        return false;
      } else if (data) {
        return true;
      } else {
        return false;
      }
    },
    viewPermission(row) {
      this.$refs.editPermission.open(row, true, this.$t('group.view_permission'));
    },
    create() {
      this.$refs.editUserGroup.open({}, 'create', this.$t('group.create'));
    },
    edit(row) {
      if (row.id === "admin") {
        this.$warning(this.$t('group.admin_not_allow_edit'));
        return;
      }
      this.$refs.editUserGroup.open(row, 'edit', this.$t('group.edit'));
    },
    _handleDel(row) {
      this.result = this.$get("/user/group/delete/" + row.id, () => {
        this.$success(this.$t('commons.delete_success'));
        this.initData();
      });
    },
    del(row) {
      if (row.system) {
        this.$warning(this.$t('group.admin_not_allow_delete'));
        return;
      }
      this.$refs.deleteConfirm.open(row);
    },
    copy(row) {
    },
    setPermission(row) {
      this.$refs.editPermission.open(row);
    },
    sort(column) {
      // 每次只对一个字段排序
      if (this.condition.orders) {
        this.condition.orders = [];
      }
      _sort(column, this.condition);
      this.initData();
    },
    memberClick(row) {
      this.$refs.groupMember.open(row);
    }
  }
};
</script>

<style scoped>

</style>
