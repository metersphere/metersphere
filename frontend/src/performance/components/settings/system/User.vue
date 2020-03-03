<template>
  <div v-loading="result.loading">

    <el-card>
      <div slot="header">
        <el-row type="flex" justify="space-between" align="middle">
          <span class="title">用户
            <ms-create-box :tips="btnTips" :exec="create"/>
          </span>
          <span class="search">
            <el-input type="text" size="small" placeholder="根据ID，名称搜索" prefix-icon="el-icon-search" maxlength="60" v-model="condition" clearable/>
          </span>
        </el-row>
      </div>
      <el-table :data="tableData" style="width: 100%">
        <el-table-column type="selection" width="55"/>
        <el-table-column prop="id" label="ID"/>
        <el-table-column prop="name" label="用户名" width="120"/>
        <el-table-column prop="email" label="邮箱"/>
        <el-table-column prop="phone" label="电话"/>
        <el-table-column prop="status" label="启用/禁用" width="100">
          <template slot-scope="scope">
            <el-switch v-model="scope.row.status"
                       active-color="#13ce66"
                       inactive-color="#ff4949"
                       active-value="1"
                       inactive-value="0"
                       @change="changeSwitch(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template slot-scope="scope">
            <span>{{ scope.row.createTime | timestampFormatDate }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作">
          <template slot-scope="scope">
            <el-button @click="edit(scope.row)" type="primary" icon="el-icon-edit" size="mini" circle/>
            <el-button @click="del(scope.row)" type="danger" icon="el-icon-delete" size="mini" circle/>
          </template>
        </el-table-column>
      </el-table>

      <div>
        <el-row>
          <el-col :span="22" :offset="1">
            <div class="table-page">
              <el-pagination
                @size-change="handleSizeChange"
                @current-change="handleCurrentChange"
                :current-page.sync="currentPage"
                :page-sizes="[5, 10, 20, 50, 100]"
                :page-size="pageSize"
                layout="total, sizes, prev, pager, next, jumper"
                :total="total">
              </el-pagination>
            </div>
          </el-col>
        </el-row>
      </div>

    </el-card>

    <el-dialog title="创建用户" :visible.sync="createVisible" width="30%" @closed="closeFunc" :destroy-on-close="true">
      <el-form :model="form" label-position="left" label-width="100px" size="small" :rules="rule" ref="createUserForm">
        <el-form-item label="ID" prop="id">
          <el-input v-model="form.id" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="用户名" prop="name">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" autocomplete="off"/>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="createUser('createUserForm')" size="medium">创建</el-button>
      </span>
    </el-dialog>

    <el-dialog title="修改用户" :visible.sync="updateVisible" width="30%" :destroy-on-close="true" @close="closeFunc">
      <el-form :model="form" label-position="left" label-width="100px" size="small" :rules="rule" ref="updateUserForm">
        <el-form-item label="ID" prop="id">
          <el-input v-model="form.id" autocomplete="off" :disabled="true"/>
        </el-form-item>
        <el-form-item label="用户名" prop="name">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" autocomplete="off"/>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="updateUser('updateUserForm')" size="medium">修改</el-button>
      </span>
    </el-dialog>

  </div>
</template>

<script>
  import MsCreateBox from "../CreateBox";

  export default {
    data() {
      return {
        queryPath: '/user/list',
        deletePath: '/user/delete/',
        createPath: '/user/add',
        updatePath: '/user/update',
        result: {},
        createVisible: false,
        updateVisible: false,
        multipleSelection: [],
        currentPage: 1,
        pageSize: 5,
        total: 0,
        btnTips: "添加用户",
        condition: "",
        tableData: [],
        form: {},
        rule: {
          id: [
            { required: true, message: '请输入ID', trigger: 'blur'},
            { min: 2, max: 10, message: '长度在 2 到 10 个字符', trigger: 'blur' }
          ],
          name: [
            {required: true, message: '请输入姓名', trigger: 'blur'},
            { min: 2, max: 10, message: '长度在 2 到 10 个字符', trigger: 'blur' },
            {
              required: true,
              pattern: /^[\u4e00-\u9fa5_a-zA-Z0-9.·-]+$/,
              message: '姓名不支持特殊字符',
              trigger: 'blur'
            }
          ],
          phone: [
            {
              required: false,
              pattern: '^1(3|4|5|7|8)\\d{9}$',
              message: '手机号码格式不正确！',
              trigger: 'blur'
            }
          ],
          email: [
            { required: true, message: '请输入邮箱', trigger: 'blur' },
            {
              required: true,
              pattern: /^([A-Za-z0-9_\-.])+@([A-Za-z0-9]+\.)+[A-Za-z]{2,6}$/,
              message: '邮箱格式不正确！',
              trigger: 'blur'
            }
          ]
        }
      }
    },
    name: "MsUser",
    components: {MsCreateBox},
    created() {
      this.initTableData();
    },
    methods: {
      create() {
        this.createVisible = true;
      },
      edit(row) {
        this.updateVisible = true;
        this.form = row;
      },
      del(row) {
        this.$confirm('是否删除用户 ' + row.name + ' ?', '', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.result = this.$get(this.deletePath + row.id, () => {
            this.$message({
              type: 'success',
              message: '删除成功!'
            });
            this.initTableData();
          });
        }).catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          });
        });
      },
      createUser(createUserForm) {
        this.$refs[createUserForm].validate(valide => {
          if (valide) {
            this.result = this.$post(this.createPath, this.form, () => {
              this.$message({
                type: 'success',
                message: '添加成功!'
              });
              this.initTableData();
              this.createVisible = false;
              });
          } else {
            return false;
          }
        })
      },
      updateUser(updateUserForm) {
        this.$refs[updateUserForm].validate(valide => {
          if (valide) {
            this.result = this.$post(this.updatePath, this.form,() => {
              this.$message({
                  type: 'success',
                  message: '修改成功!'
              });
              this.updateVisible = false;
              this.initTableData();
              });
          } else {
            return false;
          }
        })
      },
      initTableData() {
        this.result = this.$post(this.buildPagePath(this.queryPath),{},response => {
            let data = response.data;
            this.total = data.itemCount;
            this.tableData = data.listObject;
        })
      },
      closeFunc() {
        this.form = {};
      },
      changeSwitch(row) {
        this.$post(this.updatePath, row,() =>{
          this.$message({
            type: 'success',
            message: '状态修改成功!'
          });
        })
      },
      buildPagePath(path) {
        return path + "/" + this.currentPage + "/" + this.pageSize;
      },
      handleSizeChange(size) {
        this.pageSize = size;
        this.initTableData();
      },
      handleCurrentChange(current) {
        this.currentPage = current;
        this.initTableData();
      },
      handleSelectionChange(val) {
        this.multipleSelection = val;
      }
    }
  }
</script>

<style scoped>
  .search {
    width: 240px;
  }

  .table-page {
    padding-top: 20px;
    margin-right: -9px;
    float: right;
  }
</style>
