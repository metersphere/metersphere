<template>
  <div>
    <el-card class="table-card" v-loading="loading">
      <template v-slot:header>
        <ms-table-header
          :create-permission="['SYSTEM_GROUP:READ+CREATE']"
          :condition.sync="condition" @search="initData" @create="create"
          :create-tip="$t('group.create')" :title="$t('group.group_permission')"/>
      </template>

      <el-table
        :data="groups" border class="adjust-table" :height="screenHeight"
        style="width: 100%" @sort-change="sort">
        <el-table-column prop="name" :label="$t('commons.name')" show-overflow-tooltip/>
        <el-table-column prop="type" :label="$t('group.type')">
          <template v-slot="scope">
            <span>{{ userGroupType[scope.row.type] ? $t(userGroupType[scope.row.type]) : scope.row.type }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.member')" width="100">
          <template v-slot:default="scope">
            <el-link type="primary" class="member-size" @click="memberClick(scope.row)"
                     :disabled="disabledEditGroupMember">
              {{ scope.row.memberSize || 0 }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="scopeName" :label="$t('group.scope')">
          <template v-slot="scope">
            <span v-if="scope.row.scopeId ==='global'">{{ $t('group.global') }}</span>
            <span v-else-if="scope.row.scopeId ==='system'">{{ $t('group.system') }}</span>
            <span v-else>{{ scope.row.scopeName }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" :label="$t('commons.create_time')" sortable show-overflow-tooltip>
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | datetimeFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" :label="$t('commons.update_time')" sortable show-overflow-tooltip>
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | datetimeFormat }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="creator" :label="$t('group.operator')"/>
        <el-table-column prop="description" :label="$t('group.description')" show-overflow-tooltip/>
        <el-table-column :label="$t('commons.operating')" min-width="120">
          <template v-slot="scope">
            <div v-if="scope.row.id === SUPER_GROUP">
              <ms-table-operator
                :is-show="true"
                @editClick="edit(scope.row)" @deleteClick="del(scope.row)">
                <template v-slot:middle>
                  <ms-table-operator-button
                    v-permission="['SYSTEM_GROUP:READ+SETTING_PERMISSION']"
                    :tip="$t('group.set_permission')" icon="el-icon-s-tools"
                    @exec="setPermission(scope.row)"/>
                </template>
              </ms-table-operator>
            </div>
            <div v-else>
              <ms-table-operator
                :edit-permission="['SYSTEM_GROUP:READ+EDIT']"
                :delete-permission="['SYSTEM_GROUP:READ+DELETE']"
                @editClick="edit(scope.row)" @deleteClick="del(scope.row)">
                <template v-slot:middle>
                  <ms-table-operator-button
                    v-permission="['SYSTEM_GROUP:READ+SETTING_PERMISSION']"
                    :tip="$t('group.set_permission')" icon="el-icon-s-tools"
                    @exec="setPermission(scope.row)"/>
                </template>
              </ms-table-operator>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <ms-table-pagination
        :change="initData"
        :current-page.sync="currentPage"
        :page-size.sync="pageSize"
        :total="total"/>
    </el-card>
    <group-member ref="groupMember" @refresh="initData"/>
    <edit-user-group ref="editUserGroup" @refresh="initData"/>
    <edit-permission ref="editPermission"/>
    <ms-delete-confirm :title="$t('group.delete')" @delete="_handleDel" ref="deleteConfirm"/>
  </div>
</template>

<script>
import MsTableHeader from "metersphere-frontend/src/components/MsTableHeader";
import MsTableOperator from "metersphere-frontend/src/components/MsTableOperator";
import MsTablePagination from "metersphere-frontend/src/components/pagination/TablePagination";
import {USER_GROUP_SCOPE} from "metersphere-frontend/src/utils/table-constants";
import EditUserGroup from "./EditUserGroup";
import MsTableOperatorButton from "metersphere-frontend/src/components/MsTableOperatorButton";
import EditPermission from "./EditPermission";
import MsDeleteConfirm from "metersphere-frontend/src/components/MsDeleteConfirm";
import {_sort} from "metersphere-frontend/src/utils/tableUtils";
import GroupMember from "./GroupMember";
import {hasPermission} from "metersphere-frontend/src/utils/permission";
import {delUserGroupById, getUserGroupListByPage} from "../../../api/user-group";
import {SUPER_GROUP} from 'metersphere-frontend/src/utils/constants'


export default {
  name: "UserGroup",
  components: {
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
      loading: false,
      condition: {},
      currentPage: 1,
      pageSize: 10,
      total: 0,
      screenHeight: 'calc(100vh - 160px)',
      groups: [],
      SUPER_GROUP
    };
  },
  activated() {
    this.initData();
  },
  computed: {
    userGroupType() {
      return USER_GROUP_SCOPE;
    },
    disabledEditGroupMember() {
      return !hasPermission('SYSTEM_GROUP:READ');
    }
  },
  methods: {
    initData() {
      this.loading = getUserGroupListByPage(this.currentPage, this.pageSize, this.condition).then(res => {
        let data = res.data;
        if (data) {
          let {itemCount, listObject} = data;
          this.total = itemCount;
          this.groups = listObject;
        }
      });
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
      this.loading = delUserGroupById(row.id).then(() => {
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
