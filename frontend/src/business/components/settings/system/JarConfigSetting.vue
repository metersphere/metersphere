<template>
  <div>
    <el-card class="table-card" v-loading="result.loading">
      <template v-slot:header>
        <ms-table-header :condition.sync="condition" @search="list" @create="create"
                         :create-tip="$t('workspace.create')" :title="$t('commons.workspace')"/>
      </template>
      <!-- workspace table -->
      <!--<el-table border class="adjust-table" :data="items" style="width: 100%">-->
        <!--<el-table-column prop="name" :label="$t('commons.name')"/>-->
        <!--<el-table-column prop="description" :label="$t('commons.description')"/>-->
        <!--<el-table-column prop="organizationName" :label="$t('workspace.organization_name')"/>-->
        <!--<el-table-column :label="$t('commons.member')">-->
          <!--<template v-slot:default="scope">-->
            <!--<el-link type="primary" class="member-size" @click="cellClick(scope.row)">-->
              <!--{{scope.row.memberSize}}-->
            <!--</el-link>-->
          <!--</template>-->
        <!--</el-table-column>-->
        <!--<el-table-column :label="$t('commons.operating')">-->
          <!--<template v-slot:default="scope">-->
            <!--<ms-table-operator @editClick="edit(scope.row)" @deleteClick="handleDelete(scope.row)"/>-->
          <!--</template>-->
        <!--</el-table-column>-->
      <!--</el-table>-->

      <el-table border :data="items"
                class="adjust-table table-content"
                highlight-current-row
                @row-click="handleView">

        <el-table-column prop="name" :label="$t('commons.name')" show-overflow-tooltip/>
        <el-table-column prop="fileName" :label="$t('api_test.jar_config.jar_file')"  show-overflow-tooltip/>
        <el-table-column prop="description" :label="$t('commons.description')" show-overflow-tooltip/>
        <el-table-column prop="updateTime" :label="$t('commons.update_time')" show-overflow-tooltip/>
        <el-table-column prop="owner" :label="$t('report.user_name')"  show-overflow-tooltip/>

        <el-table-column :label="$t('commons.operating')" min-width="100">
          <template v-slot:default="scope">
            <ms-table-operator-button v-if="testId && !relevanceConfigSet.has(scope.row.id)" :is-tester-permission="true" :tip="$t('api_test.scenario.reference')" icon="el-icon-connection" type="success" @exec="handleRelevance(scope.row.id, 'relevance')"/>
            <ms-table-operator-button v-if="testId && relevanceConfigSet.has(scope.row.id)" :is-tester-permission="true" :tip="$t('api_test.scenario.reference')" icon="el-icon-unlock" type="warning" @exec="handleRelevance(scope.row.id)"/>
            <ms-table-operator-button :isTesterPermission="true" :tip="$t('commons.delete')" icon="el-icon-delete" type="danger" @exec="handleDelete(scope.row.id)"/>
            <i v-if="testId && relevanceConfigSet.has(scope.row.id)" class="el-icon-check"/>
          </template>
        </el-table-column>

      </el-table>


      <ms-table-pagination :change="list" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </el-card>

    <!-- update workspace member dialog -->
    <el-dialog :close-on-click-modal="false" :title="$t('member.modify')" :visible.sync="dialogWsMemberUpdateVisible" width="30%"
               :destroy-on-close="true"
               @close="handleClose">
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
        <el-form-item :label="$t('commons.role')" prop="roleIds" :rules="{required: true, message: $t('role.please_choose_role'), trigger: 'change'}">
          <el-select filterable v-model="memberForm.roleIds" multiple :placeholder="$t('role.please_choose_role')"
                     class="select-width">
            <el-option
              v-for="item in memberForm.allroles"
              :key="item.id"
              :label="$t('role.' + item.id)"
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <ms-dialog-footer
          @cancel="dialogWsMemberUpdateVisible = false"
          @confirm="updateWorkspaceMember('updateUserForm')"/>
      </template>

    </el-dialog>

    <ms-delete-confirm :title="$t('workspace.delete')" @delete="_handleDelete" ref="deleteConfirm"/>

  </div>
</template>

<script>
  import MsCreateBox from "../CreateBox";
  import {Message} from "element-ui";
  import MsTablePagination from "../../common/pagination/TablePagination";
  import MsTableHeader from "../../common/components/MsTableHeader";
  import MsRolesTag from "../../common/components/MsRolesTag";
  import MsTableOperator from "../../common/components/MsTableOperator";
  import MsTableOperatorButton from "../../common/components/MsTableOperatorButton";
  import MsDialogFooter from "../../common/components/MsDialogFooter";
  import {
    getCurrentUser,
    getCurrentWorkspaceId, listenGoBack,
    refreshSessionAndCookies, removeGoBackListener
  } from "@/common/js/utils";
  import {DEFAULT, WORKSPACE} from "@/common/js/constants";
  import MsDeleteConfirm from "../../common/components/MsDeleteConfirm";

  export default {
    name: "MsJarConfigList",
    components: {
      MsDeleteConfirm,
      MsCreateBox,
      MsTablePagination,
      MsTableHeader,
      MsRolesTag,
      MsTableOperator,
      MsDialogFooter,
      MsTableOperatorButton
    },
    data() {
      return {
        result: {},
        condition: {},
        dialogCondition: {},
        items: [],
        currentPage: 1,
        pageSize: 5,
        total: 0,
        dialogCurrentPage: 1,
        dialogPageSize: 5,
        dialogTotal: 0,
        form: {
          // name: "",
          // description: ""
        },
        rules: {
          name: [
            {required: true, message: this.$t('workspace.input_name'), trigger: 'blur'},
            {min: 2, max: 25, message: this.$t('commons.input_limit', [2, 25]), trigger: 'blur'},
            {
              required: true,
              pattern: /^(?!-)(?!.*?-$)[a-zA-Z0-9\u4e00-\u9fa5-]+$/,
              message: this.$t('workspace.special_characters_are_not_supported'),
              trigger: 'blur'
            }
          ],
          description: [
            {max: 50, message: this.$t('commons.input_limit', [0, 50]), trigger: 'blur'}
          ],
          organizationId: [
            {required: true, message: this.$t('organization.select_organization'), trigger: ['blur']}
          ]
        },
      }
    },
    activated() {
      this.list();
    },
    methods: {
      create() {
        this.form = {};
        this.$get("/organization/list", response => {
          this.$set(this.form, "orgList", response.data);
        })
        listenGoBack(this.close);
      },
      getJarConfigs() {
        this.result = this.$get("/jar/list/" + this.projectId, response => {
          this.configs = response.data;
          this.currentConfig = {};
        })
      },
      submit(formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            let saveType = 'special/add';
            if (this.form.id) {
              saveType = 'update'
            }
            this.result = this.$post("/workspace/" + saveType, this.form, () => {
              this.dialogWsAddVisible = false;
              this.list();
              Message.success(this.$t('commons.save_success'));
            });
          } else {
            return false;
          }
        });
      },

      edit(row) {
        // copy user
        this.form = Object.assign({}, row);
        this.$get("/organization/list", response => {
          this.$set(this.form, "orgList1", response.data);
        })
        listenGoBack(this.close);
      },
      close() {
        removeGoBackListener(this.close);
      },
      updateWorkspace(updateForm) {
        this.$refs[updateForm].validate(valid => {
          if (valid) {
            this.result = this.$post("/workspace/special/update", this.form, () => {
              this.$success(this.$t('commons.modify_success'));
              this.list();
            });
          } else {
            return false;
          }
        })
      },

      list() {
        let url = '/jar/list/' + this.currentPage + '/' + this.pageSize;
        this.result = this.$post(url, this.condition, response => {
          let data = response.data;
          this.items = data.listObject;
          this.total = data.itemCount;
        });
      },
      submitForm(formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            let param = {
              userIds: this.memberForm.userIds,
              roleIds: this.memberForm.roleIds,
              workspaceId: this.currentWorkspaceRow.id
            };
            this.result = this.$post("user/special/ws/member/add", param, () => {
              this.cellClick(this.currentWorkspaceRow);
              this.dialogWsMemberAddVisible = false;
            })
          } else {
            return false;
          }
        });
      },

      handleDelete(workspace) {
        this.$refs.deleteConfirm.open(workspace);
      },
      _handleDelete(workspace) {
        this.$confirm(this.$t('workspace.delete_confirm'), '', {
          confirmButtonText: this.$t('commons.confirm'),
          cancelButtonText: this.$t('commons.cancel'),
          type: 'warning'
        }).then(() => {
          this.$get('/workspace/special/delete/' + workspace.id, () => {
            let lastWorkspaceId = getCurrentWorkspaceId();
            let sourceId = workspace.id;
            if (lastWorkspaceId === sourceId) {
              let sign = DEFAULT;
              refreshSessionAndCookies(sign, sourceId);
            }
            Message.success(this.$t('commons.delete_success'));
            this.list();
          });
        }).catch(() => {
          this.$message({
            type: 'info',
            message: this.$t('commons.delete_cancelled')
          });
        });
      }
    }
  }
</script>

<style scoped>

  .el-table__row:hover .edit {
    opacity: 1;
  }

  .member-size {
    text-decoration: underline;
    cursor: pointer;
  }

  .ws-member-id {
    float: left;
  }

  .ws-member-email {
    float: right;
    color: #8492a6;
    font-size: 13px;
  }

  .select-width {
    width: 100%;
  }

  .dialog-css >>> .el-dialog__header {
    padding: 0;
  }

</style>

