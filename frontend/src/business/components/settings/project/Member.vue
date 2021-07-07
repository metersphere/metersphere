<template>
  <div v-loading="result.loading">
    <el-card class="table-card">
      <template v-slot:header>
        <ms-table-header :create-permission="['PROJECT_USER:READ+CREATE']" :condition.sync="condition" @search="initTableData" @create="create"
                         :create-tip="$t('member.create')" :title="$t('commons.member')" :have-search="false"/>
      </template>
      <el-table border class="adjust-table ms-select-all-fixed" :data="tableData" style="width: 100%"
                :height="screenHeight"
                ref="userTable">

        <el-table-column prop="id" label="ID"/>
        <el-table-column prop="name" :label="$t('commons.username')"/>
        <el-table-column prop="email" :label="$t('commons.email')"/>
        <el-table-column prop="phone" :label="$t('commons.phone')"/>
        <el-table-column prop="groups" :label="$t('commons.group')" width="150">
          <template v-slot:default="scope">
            <ms-roles-tag :roles="scope.row.groups" type="success"/>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.operating')">
          <template v-slot:default="scope">
            <div>
              <ms-table-operator :edit-permission="['PROJECT_USER:READ+EDIT']"
                                 :delete-permission="['PROJECT_USER:READ+DELETE']"
                                 :tip2="$t('commons.remove')" @editClick="edit(scope.row)"
                                 @deleteClick="del(scope.row)"/>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </el-card>


    <el-dialog :close-on-click-modal="false" :title="$t('member.modify')" :visible.sync="updateVisible" width="30%" :destroy-on-close="true"
               @close="handleClose" v-loading="dialogResult.loading">
      <el-form :model="form" label-position="right" label-width="100px" size="small" ref="updateUserForm">
        <el-form-item label="ID" prop="id">
          <el-input v-model="form.id" autocomplete="off" :disabled="true"/>
        </el-form-item>
        <el-form-item :label="$t('commons.username')" prop="name">
          <el-input v-model="form.name" autocomplete="off" :disabled="true"/>
        </el-form-item>
        <el-form-item :label="$t('commons.email')" prop="email">
          <el-input v-model="form.email" autocomplete="off" :disabled="true"/>
        </el-form-item>
        <el-form-item :label="$t('commons.phone')" prop="phone">
          <el-input v-model="form.phone" autocomplete="off" :disabled="true"/>
        </el-form-item>
        <el-form-item :label="$t('commons.group')" prop="groupIds" :rules="{required: true, message: $t('group.please_select_group'), trigger: 'change'}">
          <el-select v-model="form.groupIds" multiple :placeholder="$t('group.please_select_group')" class="select-width">
            <el-option
              v-for="item in form.allgroups"
              :key="item.id"
              :label="item.name"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="updateVisible = false"
          @confirm="updateProjectMember('updateUserForm')"/>
      </template>
    </el-dialog>



    <edit-member ref="editMember" @refresh="initTableData"/>
  </div>
</template>

<script>
import MsRolesTag from "@/business/components/common/components/MsRolesTag";
import MsTableOperator from "@/business/components/common/components/MsTableOperator";
import MsTablePagination from "@/business/components/common/pagination/TablePagination";
import MsCreateBox from "@/business/components/settings/CreateBox";
import MsTableHeader from "@/business/components/common/components/MsTableHeader";
import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";
import MsTableHeaderSelectPopover from "@/business/components/common/components/table/MsTableHeaderSelectPopover";
import ShowMoreBtn from "@/business/components/track/case/components/ShowMoreBtn";
import EditMember from "@/business/components/settings/project/EditMember";
import {GROUP_PROJECT, GROUP_WORKSPACE} from "@/common/js/constants";
import {getCurrentOrganizationId, getCurrentProjectID} from "@/common/js/utils";

export default {
  name: "Member",
  components: {
    EditMember,
    MsRolesTag,
    MsTableOperator,
    MsTablePagination,
    MsCreateBox,
    MsTableHeader,
    MsDialogFooter,
    MsTableHeaderSelectPopover,
    ShowMoreBtn,
  },
  data() {
    return {
      condition: {},
      result: {},
      dialogResult: {},
      tableData: [],
      screenHeight: 'calc(100vh - 195px)',
      currentPage: 1,
      pageSize: 10,
      total: 0,
      updateVisible: false,
      form: {}
    }
  },
  computed: {
    projectId() {
      return getCurrentProjectID();
    }
  },
  methods: {
    create() {
      this.$refs.editMember.open();
    },
    initTableData() {
      let param = {};
      param.projectId = this.projectId;
      if (this.projectId) {
        this.result = this.$post('/user/project/member/list/' + this.currentPage + "/" + this.pageSize, param, response => {
          let data = response.data;
          this.tableData = data.listObject;
          let url = "/user/group/list/project/" + this.projectId;
          for (let i = 0; i < this.tableData.length; i++) {
            this.$get(url + "/" + encodeURIComponent(this.tableData[i].id), response => {
              let groups = response.data;
              this.$set(this.tableData[i], "groups", groups);
            })
          }
          this.total = data.itemCount;
        });
      }
    },
    edit(row) {
      this.updateVisible = true;
      this.form = Object.assign({}, row);
      let groupIds = this.form.groups.map(r => r.id);
      this.dialogResult = this.$post('/user/group/list', {type: GROUP_PROJECT, resourceId: getCurrentOrganizationId()}, response => {
        this.$set(this.form, "allgroups", response.data);
      })
      // 编辑使填充角色信息
      this.$set(this.form, 'groupIds', groupIds);
    },
    del(row) {
      this.$confirm(this.$t('member.remove_member'), '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        this.result = this.$get('/user/project/member/delete/' + this.projectId + '/' + encodeURIComponent(row.id),() => {
          this.$success(this.$t('commons.remove_success'));
          this.initTableData();
        });
      }).catch(() => {
        this.$info(this.$t('commons.remove_cancel'));
      });
    },
    handleClose() {
      this.form = {};
      this.updateVisible = false;
    },
    updateProjectMember(formName) {
      let param = {
        id: this.form.id,
        name: this.form.name,
        email: this.form.email,
        phone: this.form.phone,
        groupIds: this.form.groupIds,
        projectId: this.projectId
      }
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.result = this.$post("/project/member/update", param, () => {
            this.$success(this.$t('commons.modify_success'));
            this.updateVisible = false;
            this.initTableData();
          });
        }
      });
    },
  },
  activated() {
    this.initTableData();
  }
}
</script>

<style scoped>
.input-with-autocomplete {
  width: 100%;
}

.select-width {
  width: 100%;
}
</style>
