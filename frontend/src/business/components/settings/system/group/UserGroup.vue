<template>
  <div v-loading="result.loading">
    <el-card>
      <template v-slot:header>
        <ms-table-header :create-permission="['SYSTEM_GROUP:READ+CREATE','ORGANIZATION_GROUP:READ+CREATE']"
                         :condition.sync="condition" @search="initData" @create="create"
                         :create-tip="$t('group.create')" :title="$t('group.group_permission')"/>
      </template>

      <el-table :data="groups" border class="adjust-table" style="width: 100%"
                :height="screenHeight" @sort-change="sort">
        <el-table-column prop="name" :label="$t('commons.name')" show-overflow-tooltip/>
        <el-table-column prop="type" :label="$t('group.type')">
          <template v-slot="scope">
            <span>{{ userGroupType[scope.row.type] ? userGroupType[scope.row.type] : scope.row.type }}</span>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.member')" width="100">
          <template v-slot:default="scope">
            <el-link type="primary" class="member-size" @click="memberClick(scope.row)">
              {{ scope.row.memberSize || 0 }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="scopeName" :label="$t('group.scope')"/>
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
              <ms-table-operator :edit-permission="['SYSTEM_GROUP:READ+EDIT', 'ORGANIZATION_GROUP:READ+EDIT']"
                                 :delete-permission="['SYSTEM_GROUP:READ+DELETE', 'ORGANIZATION_GROUP:READ+DELETE']"
                                 @editClick="edit(scope.row)" @deleteClick="del(scope.row)">
                <template v-slot:middle>
                  <!--                <ms-table-operator-button tip="复制" icon="el-icon-document-copy" @exec="copy(scope.row)"/>-->
                  <ms-table-operator-button
                    v-permission="['SYSTEM_GROUP:READ+SETTING_PERMISSION', 'ORGANIZATION_GROUP:READ+SETTING_PERMISSION']"
                    :tip="$t('group.set_permission')" icon="el-icon-s-tools" @exec="setPermission(scope.row)"/>
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
</template>

<script>
import MsTableHeader from "@/business/components/common/components/MsTableHeader";
import MsTableOperator from "@/business/components/common/components/MsTableOperator";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import {USER_GROUP_SCOPE} from "@/common/js/table-constants";
import EditUserGroup from "@/business/components/settings/system/group/EditUserGroup";
import MsTableOperatorButton from "@/business/components/common/components/MsTableOperatorButton";
import EditPermission from "@/business/components/settings/system/group/EditPermission";
import MsDeleteConfirm from "@/business/components/common/components/MsDeleteConfirm";
import {_sort} from "@/common/js/tableUtils";
import GroupMember from "@/business/components/settings/system/group/GroupMember";

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
      result: {},
      condition: {},
      currentPage: 1,
      pageSize: 10,
      total: 0,
      screenHeight: 'calc(100vh - 200px)',
      groups: [],
      currentGroup: {}
    };
  },
  activated() {
    this.initData();
  },
  computed: {
    userGroupType() {
      return USER_GROUP_SCOPE;
    }
  },
  methods: {
    initData() {
      this.result = this.$post("/user/group/get/" + this.currentPage + "/" + this.pageSize, this.condition, res => {
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
      // console.log(row);
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
