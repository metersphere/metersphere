<template>
  <div v-loading="result.loading">

    <el-card class="table-card">
      <template v-slot:header>
        <ms-table-header :create-permission="['SYSTEM_ORGANIZATION:READ+CREATE']" :condition.sync="condition"
                         @search="initTableData" @create="create"
                         :create-tip="$t('organization.create')" :title="$t('commons.organization')"/>
      </template>
      <!-- system menu organization table-->
      <el-table border class="adjust-table" :data="tableData" style="width: 100%"
                :height="screenHeight"
      >
        <el-table-column prop="name" :label="$t('commons.name')"/>
        <el-table-column prop="description" :label="$t('commons.description')"/>
        <el-table-column :label="$t('commons.member')">
          <template v-slot:default="scope">
            <el-link type="primary" class="member-size" @click="cellClick(scope.row)">
              {{ scope.row.memberSize }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.operating')">
          <template v-slot:default="scope">
            <div>
              <ms-table-operator :edit-permission="['SYSTEM_ORGANIZATION:READ+EDIT']"
                                 :delete-permission="['SYSTEM_ORGANIZATION:READ+DELETE']"
                                 :show-delete="organizationId !== scope.row.id"
                                 @editClick="edit(scope.row)"
                                 @deleteClick="handleDelete(scope.row)"/>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </el-card>

    <!-- dialog of organization member -->
    <el-dialog :close-on-click-modal="false" :visible.sync="dialogOrgMemberVisible" width="70%" :destroy-on-close="true"
               @close="closeFunc"
               class="dialog-css">
      <template v-slot:title>
        <ms-table-header :condition.sync="dialogCondition" @create="addMember" @search="dialogSearch"
                         :create-tip="$t('member.create')" :title="$t('commons.member')"/>
      </template>
      <!-- organization member table -->
      <el-table :border="true" class="adjust-table" :data="memberLineData" style="width: 100%;margin-top:5px;">
        <el-table-column prop="id" label="ID"/>
        <el-table-column prop="name" :label="$t('commons.username')"/>
        <el-table-column prop="email" :label="$t('commons.email')"/>
        <el-table-column prop="phone" :label="$t('commons.phone')"/>
        <el-table-column :label="$t('commons.group')" width="140">
          <template v-slot:default="scope">
            <ms-roles-tag :roles="scope.row.groups"/>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.operating')">
          <template v-slot:default="scope">
            <div>
              <ms-table-operator :tip2="$t('commons.remove')" @editClick="editMember(scope.row)"
                                 @deleteClick="delMember(scope.row)"/>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="dialogSearch" :current-page.sync="dialogCurrentPage"
                           :page-size.sync="dialogPageSize"
                           :total="dialogTotal"/>
    </el-dialog>

    <!-- add organization form -->
    <el-dialog :close-on-click-modal="false" :title="$t('organization.create')" :visible.sync="dialogOrgAddVisible"
               width="30%" @closed="closeFunc"
               :destroy-on-close="true">
      <el-form :model="form" label-position="right" label-width="100px" size="small" :rules="rule"
               ref="createOrganization">
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input v-model="form.name" autocomplete="off"/>
          <!-- 阻止Enter关闭dialog -->
          <el-input v-show="false"/>
        </el-form-item>
        <el-form-item :label="$t('commons.description')" prop="description">
          <el-input v-model="form.description" autocomplete="off" type="textarea"/>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="dialogOrgAddVisible = false"
          @confirm="createOrganization('createOrganization')"/>
      </template>
    </el-dialog>

    <!-- update organization form -->
    <el-dialog :close-on-click-modal="false" :title="$t('organization.modify')" :visible.sync="dialogOrgUpdateVisible"
               width="30%"
               :destroy-on-close="true"
               @close="closeFunc">
      <el-form :model="form" label-position="right" label-width="100px" size="small" :rules="rule"
               ref="updateOrganizationForm">
        <el-form-item :label="$t('commons.name')" prop="name">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item :label="$t('commons.description')" prop="description">
          <el-input v-model="form.description" autocomplete="off"/>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="dialogOrgUpdateVisible = false"
          @confirm="updateOrganization('updateOrganizationForm')"/>
      </template>
    </el-dialog>

    <!-- add organization member form -->
    <add-member :group-type="'ORGANIZATION'" :group-scope-id="orgId" ref="addMember" @submit="submitForm"/>

    <!-- update organization member form -->
    <el-dialog :close-on-click-modal="false" :title="$t('member.modify')" :visible.sync="dialogOrgMemberUpdateVisible"
               width="30%"
               :destroy-on-close="true"
               @close="closeFunc">
      <el-form :model="memberForm" label-position="right" label-width="100px" size="small" ref="updateUserForm">
        <el-form-item label="ID" prop="id">
          <el-input v-model="memberForm.id" autocomplete="off" :disabled="true"/>
        </el-form-item>
        <el-form-item :label="$t('commons.username')" prop="name">
          <el-input v-model="memberForm.name" autocomplete="off" :disabled="true"/>
        </el-form-item>
        <el-form-item :label="$t('commons.email')" prop="email">
          <el-input v-model="memberForm.email" autocomplete="off" :disabled="true"/>
        </el-form-item>
        <el-form-item :label="$t('commons.phone')" prop="phone">
          <el-input v-model="memberForm.phone" autocomplete="off" :disabled="true"/>
        </el-form-item>
        <el-form-item :label="$t('commons.group')" prop="groupIds"
                      :rules="{required: true, message: $t('group.please_select_group'), trigger: 'change'}">
          <el-select filterable v-model="memberForm.groupIds" multiple :placeholder="$t('group.please_select_group')"
                     class="select-width">
            <el-option
              v-for="item in memberForm.allgroups"
              :key="item.id"
              :label="item.name"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="dialogOrgMemberUpdateVisible = false"
          @confirm="updateOrgMember('updateUserForm')"/>
      </template>
    </el-dialog>

    <ms-delete-confirm :title="$t('organization.delete')" @delete="_handleDelete" ref="deleteConfirm"/>

  </div>
</template>

<script>
import MsCreateBox from "../CreateBox";
import MsTablePagination from "../../common/pagination/TablePagination";
import MsTableHeader from "../../common/components/MsTableHeader";
import MsRolesTag from "../../common/components/MsRolesTag";
import MsTableOperator from "../../common/components/MsTableOperator";
import MsTableOperatorButton from "../../common/components/MsTableOperatorButton";
import MsDialogFooter from "../../common/components/MsDialogFooter";
import {
  getCurrentOrganizationId,
  listenGoBack,
  removeGoBackListener
} from "@/common/js/utils";
import {GROUP_ORGANIZATION} from "@/common/js/constants";
import MsDeleteConfirm from "../../common/components/MsDeleteConfirm";
import AddMember from "@/business/components/settings/common/AddMember";

export default {
  name: "MsOrganization",
  components: {
    MsDeleteConfirm,
    MsCreateBox,
    MsTablePagination,
    MsTableHeader,
    MsRolesTag,
    MsTableOperator,
    MsDialogFooter,
    MsTableOperatorButton,
    AddMember
  },
  computed: {
    organizationId() {
      return getCurrentOrganizationId();
    }
  },
  inject: [
    'reloadTopMenus',
  ],
  data() {
    return {
      queryPath: '/organization/list',
      deletePath: '/organization/delete/',
      createPath: '/organization/add',
      updatePath: '/organization/update',
      result: {},
      dialogOrgAddVisible: false,
      dialogOrgUpdateVisible: false,
      dialogOrgMemberVisible: false,
      dialogOrgMemberAddVisible: false,
      dialogOrgMemberUpdateVisible: false,
      multipleSelection: [],
      currentPage: 1,
      pageSize: 10,
      total: 0,
      dialogCurrentPage: 1,
      dialogPageSize: 5,
      dialogTotal: 0,
      currentRow: {},
      screenHeight: 'calc(100vh - 195px)',
      condition: {},
      dialogCondition: {},
      tableData: [],
      memberLineData: [],
      form: {},
      memberForm: {},
      rule: {
        name: [
          {required: true, message: this.$t('organization.input_name'), trigger: 'blur'},
          {min: 2, max: 25, message: this.$t('commons.input_limit', [2, 25]), trigger: 'blur'}
        ],
        description: [
          {max: 50, message: this.$t('commons.input_limit', [0, 50]), trigger: 'blur'}
        ]
      },
      orgMemberRule: {
        userIds: [
          {required: true, message: this.$t('member.please_choose_member'), trigger: ['blur']}
        ],
        groupIds: [
          {required: true, message: this.$t('group.please_select_group'), trigger: ['blur']}
        ]
      },
      orgId: ""
    };
  },
  activated() {
    this.initTableData();
  },
  methods: {
    create() {
      this.dialogOrgAddVisible = true;
      listenGoBack(this.closeFunc);
    },
    addMember() {
      this.$refs.addMember.open();
    },
    dataFilter(val) {
      if (val) {
        this.memberForm.userList = this.memberForm.copyUserList.filter((item) => {
          if (!!~item.id.indexOf(val) || !!~item.id.toUpperCase().indexOf(val.toUpperCase())) {
            return true;
          }
        });
      } else {
        this.memberForm.userList = this.memberForm.copyUserList;
      }
    },
    edit(row) {
      this.dialogOrgUpdateVisible = true;
      this.form = Object.assign({}, row);
      listenGoBack(this.closeFunc);
    },
    editMember(row) {
      this.dialogOrgMemberUpdateVisible = true;
      this.memberForm = Object.assign({}, row);
      let groupIds = this.memberForm.groups.map(r => r.id);
      this.result = this.$post('/user/group/list', {type: GROUP_ORGANIZATION, resourceId: this.orgId}, response => {
        this.$set(this.memberForm, "allgroups", response.data);
      });
      // 编辑时填充角色信息
      this.$set(this.memberForm, 'groupIds', groupIds);
      listenGoBack(this.closeFunc);
    },
    cellClick(row) {
      // 保存当前点击的组织信息到currentRow
      this.currentRow = row;
      this.dialogOrgMemberVisible = true;
      this.orgId = row.id;
      let param = {
        name: '',
        organizationId: row.id
      };
      let path = "/user/special/org/member/list";
      this.result = this.$post(path + "/" + this.dialogCurrentPage + "/" + this.dialogPageSize, param, res => {
        let data = res.data;
        this.memberLineData = data.listObject;
        let url = "/user/group/list/org/" + row.id;
        for (let i = 0; i < this.memberLineData.length; i++) {
          this.$get(url + "/" + encodeURIComponent(this.memberLineData[i].id), response => {
            let groups = response.data;
            this.$set(this.memberLineData[i], "groups", groups);
          });
        }
        this.dialogTotal = data.itemCount;
      });
      listenGoBack(this.closeFunc);
    },
    dialogSearch() {
      let row = this.currentRow;
      this.dialogOrgMemberVisible = true;
      let param = this.dialogCondition;
      this.$set(param, 'organizationId', row.id);
      let path = "/user/special/org/member/list";
      this.result = this.$post(path + "/" + this.dialogCurrentPage + "/" + this.dialogPageSize, param, res => {
        let data = res.data;
        this.memberLineData = data.listObject;
        let url = "/userrole/list/org/" + row.id;
        for (let i = 0; i < this.memberLineData.length; i++) {
          this.result = this.$post('/user/group/list', {
            type: GROUP_ORGANIZATION,
            resourceId: this.memberLineData[i].id
          }, response => {
            this.$set(this.memberLineData[i], "groups", response.data);
          });
        }
        this.dialogTotal = data.itemCount;
      });
    },
    handleDelete(organization) {
      this.$refs.deleteConfirm.open(organization);
    },
    _handleDelete(organization) {
      this.$confirm(this.$t('organization.delete_confirm'), '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        this.result = this.$get(this.deletePath + organization.id, () => {
          this.$success(this.$t('commons.delete_success'));
          this.reloadTopMenus();
        });
      }).catch(() => {
        this.$message({
          type: 'info',
          message: this.$t('commons.delete_cancelled')
        });
      });
    },
    delMember(row) {
      this.$confirm(this.$t('member.remove_member'), '', {
        confirmButtonText: this.$t('commons.confirm'),
        cancelButtonText: this.$t('commons.cancel'),
        type: 'warning'
      }).then(() => {
        this.result = this.$get('/user/special/org/member/delete/' + this.currentRow.id + '/' + encodeURIComponent(row.id), () => {
          this.$success(this.$t('commons.remove_success'));
          this.cellClick(this.currentRow);
        });
      }).catch(() => {
        this.$info(this.$t('commons.remove_cancel'));
      });
    },
    createOrganization(createOrganizationForm) {
      this.$refs[createOrganizationForm].validate(valid => {
        if (valid) {
          this.result = this.$post(this.createPath, this.form, () => {
            this.$success(this.$t('commons.save_success'));
            this.reloadTopMenus();
            this.dialogOrgAddVisible = false;
          });
        } else {
          return false;
        }
      });
    },
    updateOrganization(updateOrganizationForm) {
      this.$refs[updateOrganizationForm].validate(valid => {
        if (valid) {
          this.result = this.$post(this.updatePath, this.form, () => {
            this.$success(this.$t('commons.modify_success'));
            this.dialogOrgUpdateVisible = false;
            this.initTableData();
          });
        } else {
          return false;
        }
      });
    },
    initTableData() {
      this.result = this.$post(this.queryPath + "/" + this.currentPage + "/" + this.pageSize, this.condition, response => {
        let data = response.data;
        this.tableData = data.listObject;
        for (let i = 0; i < this.tableData.length; i++) {
          let param = {
            name: '',
            organizationId: this.tableData[i].id
          };
          let path = "user/special/org/member/list/all";
          this.$post(path, param, res => {
            let member = res.data;
            this.$set(this.tableData[i], "memberSize", member.length);
          });
        }
        this.total = data.itemCount;
      });
    },
    closeFunc() {
      this.memberLineData = [];
      this.initTableData();
      this.form = {};
      removeGoBackListener(this.closeFunc);
      this.dialogOrgAddVisible = false;
      this.dialogOrgUpdateVisible = false;
      this.dialogOrgMemberVisible = false;
      this.dialogOrgMemberAddVisible = false;
      this.dialogOrgMemberUpdateVisible = false;
    },
    handleSelectionChange(val) {
      this.multipleSelection = val;
    },
    submitForm(param) {
      param['organizationId'] = this.currentRow.id;
      this.result = this.$post("user/special/org/member/add", param, () => {
        this.cellClick(this.currentRow);
        this.$refs.addMember.close();
      });
    },
    updateOrgMember(formName) {
      let param = {
        id: this.memberForm.id,
        name: this.memberForm.name,
        email: this.memberForm.email,
        phone: this.memberForm.phone,
        groupIds: this.memberForm.groupIds,
        organizationId: this.currentRow.id
      };
      this.$refs[formName].validate((valid) => {
        if (valid) {
          this.result = this.$post("/organization/member/update", param, () => {
            this.$success(this.$t('commons.modify_success'));
            this.dialogOrgMemberUpdateVisible = false;
            this.cellClick(this.currentRow);
          });
        }
      });
    },
  }

};
</script>

<style scoped>

.member-size {
  text-decoration: underline;
}

.org-member-id {
  float: left;
}

.org-member-email {
  float: right;
  color: #8492a6;
  font-size: 13px;
}

.select-width {
  width: 100%;
}

/*.dialog-css >>> .el-dialog__header {*/
/*  padding: 0;*/
/*}*/

</style>
