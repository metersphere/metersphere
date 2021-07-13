<template>
  <div v-loading="result.loading">
    <el-card class="table-card">
      <template v-slot:header>
        <ms-table-header :create-permission="['WORKSPACE_USER:READ+CREATE']" :condition.sync="condition" @search="initTableData" @create="create"
                         :create-tip="$t('member.create')" :title="$t('commons.member')"/>
      </template>
      <el-table border class="adjust-table" :data="tableData" style="width: 100%"
                :height="screenHeight"
                ref="userTable">
        <el-table-column prop="id" label="ID"/>
        <el-table-column prop="name" :label="$t('commons.username')"/>
        <el-table-column prop="email" :label="$t('commons.email')"/>
        <el-table-column prop="phone" :label="$t('commons.phone')"/>
        <el-table-column prop="groups" :label="$t('commons.group')" width="160">
          <template v-slot:default="scope">
            <ms-roles-tag :roles="scope.row.groups" type="success"/>
          </template>
        </el-table-column>
        <el-table-column :label="$t('commons.operating')">
          <template v-slot:default="scope">
            <div>
              <ms-table-operator :edit-permission="['WORKSPACE_USER:READ+EDIT']"
                                 :delete-permission="['WORKSPACE_USER:READ+DELETE']"
                                 :tip2="$t('commons.remove')" @editClick="edit(scope.row)"
                                 @deleteClick="del(scope.row)"/>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <ms-table-pagination :change="initTableData" :current-page.sync="currentPage" :page-size.sync="pageSize"
                           :total="total"/>
    </el-card>

    <add-member :group-type="'WORKSPACE'" :group-scope-id="orgId" ref="addMember" @submit="submitForm"/>

    <el-dialog :close-on-click-modal="false" :title="$t('member.modify')" :visible.sync="updateVisible" width="30%" :destroy-on-close="true"
               @close="handleClose">
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
          @confirm="updateWorkspaceMember('updateUserForm')"/>
      </template>
    </el-dialog>
    <user-cascader :lable="batchAddLable" :title="batchAddTitle" @confirm="cascaderConfirm" ref="cascaderDialog"></user-cascader>
  </div>
</template>

<script>
  import MsCreateBox from "../CreateBox";
  import MsTablePagination from "../../common/pagination/TablePagination";
  import MsTableHeader from "../../common/components/MsTableHeader";
  import MsRolesTag from "../../common/components/MsRolesTag";
  import MsTableOperator from "../../common/components/MsTableOperator";
  import MsDialogFooter from "../../common/components/MsDialogFooter";
  import {
    getCurrentOrganizationId, getCurrentProjectID,
    getCurrentUser, getCurrentWorkspaceId,
    listenGoBack,
    removeGoBackListener
  } from "../../../../common/js/utils";
  import MsTableHeaderSelectPopover from "@/business/components/common/components/table/MsTableHeaderSelectPopover";
  import {
    _handleSelect,
    _handleSelectAll,
    getSelectDataCounts,
    setUnSelectIds,
    toggleAllSelection
  } from "@/common/js/tableUtils";
  import UserCascader from "@/business/components/settings/system/components/UserCascader";
  import ShowMoreBtn from "@/business/components/track/case/components/ShowMoreBtn";
  import {GROUP_WORKSPACE} from "@/common/js/constants";
  import AddMember from "@/business/components/settings/common/AddMember";

  export default {
    name: "MsMember",
    components: {
      AddMember, MsCreateBox, MsTablePagination, MsTableHeader, MsRolesTag, MsTableOperator, MsDialogFooter,
      MsTableHeaderSelectPopover,UserCascader,ShowMoreBtn},
    data() {
      return {
        result: {},
        form: {},
        createVisible: false,
        updateVisible: false,
        queryPath: "/user/ws/member/list",
        condition: {},
        tableData: [],
        userList: [],
        rules: {
          userIds: [
            {required: true, message: this.$t('member.please_choose_member'), trigger: ['blur']}
          ],
          groupIds: [
            {required: true, message: this.$t('group.please_select_group'), trigger: ['blur']}
          ]
        },
        screenHeight: 'calc(100vh - 195px)',
        multipleSelection: [],
        currentPage: 1,
        pageSize: 10,
        total: 0,
        selectDataCounts: 0,
        batchAddLable: this.$t('project.please_choose_workspace'),
        batchAddTitle: this.$t('project.batch_choose_workspace'),
        selectRows: new Set(),
        referenced: false,
        batchAddUserRoleOptions: [],
        buttons: [
          // {
          //   name: this.$t('user.button.add_user_role_batch'), handleClick: this.addUserRoleBatch
          // }
        ],
      }
    },
    computed: {
      orgId() {
        return getCurrentOrganizationId();
      }
    },
    activated: function () {
      this.initTableData();
    },
    methods: {
      currentUser: () => {
        return getCurrentUser();
      },
      initTableData() {
        if (getCurrentWorkspaceId() === null) {
          return false;
        }
        this.loading = true;
        let param = {
          name: this.condition.name,
          workspaceId: getCurrentWorkspaceId()
        };

        this.result = this.$post(this.buildPagePath(this.queryPath), param, response => {
          let data = response.data;
          this.tableData = data.listObject;
          let url = "/user/group/list/ws/" + getCurrentWorkspaceId();
          for (let i = 0; i < this.tableData.length; i++) {
            this.$get(url + "/" + encodeURIComponent(this.tableData[i].id), response => {
              let groups = response.data;
              this.$set(this.tableData[i], "groups", groups);
            })
          }
          this.total = data.itemCount;
          this.$nextTick(function(){
            this.checkTableRowIsSelect();
          });
        })
      },
      checkTableRowIsSelect(){
        //如果默认全选的话，则选中应该选中的行
        if(this.condition.selectAll){
          let unSelectIds = this.condition.unSelectIds;
          this.tableData.forEach(row=>{
            if(unSelectIds.indexOf(row.id)<0){
              this.$refs.userTable.toggleRowSelection(row,true);

              //默认全选，需要把选中对行添加到selectRows中。不然会影响到勾选函数统计
              if (!this.selectRows.has(row)) {
                this.$set(row, "showMore", true);
                this.selectRows.add(row);
              }
            }else{
              //不勾选的行，也要判断是否被加入了selectRow中。加入了的话就去除。
              if (this.selectRows.has(row)) {
                this.$set(row, "showMore", false);
                this.selectRows.delete(row);
              }
            }
          })
        }
      },
      buildPagePath(path) {
        return path + "/" + this.currentPage + "/" + this.pageSize;
      },
      handleClose() {
        this.form = {};
        this.createVisible = false;
        this.updateVisible = false;
        removeGoBackListener(this.handleClose);
      },
      del(row) {
        this.$confirm(this.$t('member.remove_member'), '', {
          confirmButtonText: this.$t('commons.confirm'),
          cancelButtonText: this.$t('commons.cancel'),
          type: 'warning'
        }).then(() => {
          this.result = this.$get('/user/ws/member/delete/' + getCurrentWorkspaceId() + '/' + encodeURIComponent(row.id), () => {
            this.$success(this.$t('commons.remove_success'));
            this.initTableData();
          });
        }).catch(() => {
          this.$info(this.$t('commons.remove_cancel'));
        });
      },
      edit(row) {
        this.updateVisible = true;
        this.form = Object.assign({}, row);
        let groupIds = this.form.groups.map(r => r.id);
        this.result = this.$post('/user/group/list', {type: GROUP_WORKSPACE, resourceId: getCurrentOrganizationId()}, response => {
          this.$set(this.form, "allgroups", response.data);
        })
        // 编辑使填充角色信息
        this.$set(this.form, 'groupIds', groupIds);
        listenGoBack(this.handleClose);
      },
      updateWorkspaceMember(formName) {
        let param = {
          id: this.form.id,
          name: this.form.name,
          email: this.form.email,
          phone: this.form.phone,
          groupIds: this.form.groupIds,
          workspaceId: getCurrentWorkspaceId()
        }
        this.$refs[formName].validate((valid) => {
          if (valid) {
            this.result = this.$post("/workspace/member/update", param, () => {
              this.$success(this.$t('commons.modify_success'));
              this.updateVisible = false;
              this.initTableData();
            });
          }
        });
      },
      create() {
        let wsId = getCurrentWorkspaceId();
        if (typeof wsId == "undefined" || wsId == null || wsId == "") {
          this.$warning(this.$t('workspace.please_select_a_workspace_first'));
          return false;
        }
        this.$refs.addMember.open();
        listenGoBack(this.handleClose);
      },
      submitForm(param) {
        param['workspaceId'] = getCurrentWorkspaceId();
        this.result = this.$post("user/ws/member/add", param, () => {
          this.$success(this.$t('commons.save_success'));
          this.initTableData();
          this.selectRows.clear();
          this.$refs.addMember.close();
        })
      },
      querySearch(queryString, cb) {
        var userList = this.userList;
        var results = queryString ? userList.filter(this.createFilter(queryString)) : userList;
        // 调用 callback 返回建议列表的数据
        cb(results);
      },
      createFilter(queryString) {
        return (user) => {
          return (user.email.indexOf(queryString.toLowerCase()) === 0 || user.id.indexOf(queryString.toLowerCase()) === 0);
        };
      },
      initRoleBatchProcessDataStruct(isShow){
        let organizationId = getCurrentOrganizationId();
        this.$get("/user/getWorkspaceUserRoleDataStruct/"+organizationId, response => {
          this.batchAddUserRoleOptions = response.data;
          if(isShow){
            this.$refs.cascaderDialog.open('ADD_USER_ROLE',this.batchAddUserRoleOptions);
          }
        });
      },
      handleSelectAll(selection) {
        _handleSelectAll(this, selection, this.tableData, this.selectRows, this.condition);
        setUnSelectIds(this.tableData, this.condition, this.selectRows);
        this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
        this.$emit('selection', selection);
      },
      handleSelect(selection, row) {
        _handleSelect(this, selection, row, this.selectRows);
        setUnSelectIds(this.tableData, this.condition, this.selectRows);
        this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
        this.$emit('selection', selection);
        this.$set(this.form, "userId", selection.id);
      },
      isSelectDataAll(data) {
        this.condition.selectAll = data;
        setUnSelectIds(this.tableData, this.condition, this.selectRows);
        this.selectDataCounts = getSelectDataCounts(this.condition, this.total, this.selectRows);
        toggleAllSelection(this.$refs.userTable, this.tableData, this.selectRows);
      },
      addUserRoleBatch(){
        if(this.batchAddUserRoleOptions.length == 0){
          this.initRoleBatchProcessDataStruct(true);
        }else{
          this.$refs.cascaderDialog.open('ADD_USER_ROLE',this.batchAddUserRoleOptions);
        }
      },
      cascaderConfirm(batchProcessTypeParam,selectValueArr){
        if(selectValueArr.length == 0){
          this.$success(this.$t('commons.modify_success'));
        }
        let params = {};
        params = this.buildBatchParam(params);
        params.organizationId = getCurrentOrganizationId();
        params.batchType = batchProcessTypeParam;
        params.batchProcessValue = selectValueArr;
        this.$post('/user/special/batchProcessUserInfo', params, () => {
          this.$success(this.$t('commons.modify_success'));
          this.initTableData();
          this.$refs.cascaderDialog.close();
        });
      },
      buildBatchParam(param) {
        param.ids = Array.from(this.selectRows).map(row => row.id);
        param.projectId = getCurrentProjectID();
        param.condition = this.condition;
        return param;
      },
    }
  }
</script>

<style scoped>

  .el-table__row:hover .edit {
    opacity: 1;
  }

  .select-width {
    width: 100%;
  }

  .workspace-member-name {
    float: left;
  }

  .workspace-member-email {
    float: right;
    color: #8492a6;
    font-size: 13px;
  }

  .input-with-autocomplete {
    width: 100%;
  }

  /deep/ .ms-select-all-fixed th:nth-child(2) .el-icon-arrow-down {
    top: -5px;
  }
</style>
