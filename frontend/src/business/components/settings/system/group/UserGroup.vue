<template>
  <div v-loading="result.loading">
    <el-card>
      <template v-slot:header>
        <ms-table-header :create-permission="['SYSTEM_GROUP:READ+CREATE']" :condition.sync="condition" @search="initData" @create="create"
                         create-tip="创建用户组" title="用户组与权限"/>
      </template>

      <el-table :data="groups" border class="adjust-table" style="width: 100%"
                :height="screenHeight">
        <el-table-column prop="name" :label="$t('commons.name')"/>
        <el-table-column prop="type" label="所属类型">
          <template v-slot="scope">
            <span>{{ userGroupType[scope.row.type] ? userGroupType[scope.row.type] : scope.row.type }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="scopeName" label="应用范围"/>
        <el-table-column prop="createTime" :label="$t('commons.create_time')">
          <template v-slot:default="scope">
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" :label="$t('commons.update_time')">
          <template v-slot:default="scope">
            <span>{{ scope.row.updateTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="creator" label="操作人"/>
        <el-table-column prop="description" label="描述"/>
        <el-table-column :label="$t('commons.operating')" min-width="120">
          <template v-slot:default="scope">
            <ms-table-operator :edit-permission="['SYSTEM_GROUP:READ+EDIT']"
                               :delete-permission="['SYSTEM_GROUP:READ+DELETE']"
              @editClick="edit(scope.row)" @deleteClick="del(scope.row)">
              <template v-slot:middle>
                <!--                <ms-table-operator-button tip="复制" icon="el-icon-document-copy" @exec="copy(scope.row)"/>-->
                <ms-table-operator-button v-permission="['SYSTEM_GROUP:READ+SETTING_PERMISSION']" tip="设置权限" icon="el-icon-s-tools" @exec="setPermission(scope.row)"/>
              </template>
            </ms-table-operator>
          </template>
        </el-table-column>
      </el-table>

      <ms-table-pagination :change="initData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </el-card>

    <edit-user-group ref="editUserGroup" @refresh="initData"/>
    <edit-permission ref="editPermission"/>
    <ms-delete-confirm title="删除用户组" @delete="_handleDel" ref="deleteConfirm"/>
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

export default {
  name: "UserGroup",
  components: {
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
      screenHeight: 'calc(100vh - 275px)',
      groups: []
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
          this.total = data.itemCount;
          this.groups = data.listObject;
        }
      });
    },
    create() {
      this.$refs.editUserGroup.open({}, 'create');
    },
    edit(row) {
      this.$refs.editUserGroup.open(row, 'edit');
    },
    _handleDel(row) {
      this.result = this.$get("/user/group/delete/" + row.id, () => {
        this.$success(this.$t('commons.delete_success'));
        this.initData();
      });
    },
    del(row) {
      this.$refs.deleteConfirm.open(row);
    },
    copy(row) {
      // console.log(row);
    },
    setPermission(row) {
      this.$refs.editPermission.open(row);
    },
  }
};
</script>

<style scoped>

</style>
