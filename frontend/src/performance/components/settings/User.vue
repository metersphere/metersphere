<template>
  <div v-loading="loading">
    <el-card>
      <div slot="header">
        <el-row type="flex" justify="space-between" align="middle">
          <span class="title">用户</span>
          <span class="search">
                    <el-input type="text" size="small" placeholder="根据ID，名称搜索" prefix-icon="el-icon-search"
                              maxlength="60" v-model="condition" clearable/>
          </span>
        </el-row>
      </div>
      <el-table :data="items" style="width: 100%">
        <el-table-column prop="id" label="ID" width="180"/>
        <el-table-column prop="name" label="用户名"/>
        <el-table-column prop="email" label="邮箱"/>
        <el-table-column prop="phone" label="电话"/>
        <el-table-column prop="status" label="状态"/>
        <el-table-column prop="createTime" label="创建时间"/>
        <el-table-column>
          <template slot-scope="scope">
            <el-button @click="edit(scope.row)" type="primary" icon="el-icon-edit" size="mini" circle/>
            <el-button @click="del(scope.row)" type="danger" icon="el-icon-delete" size="mini" circle/>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <ms-create-box :tips="btnTips" :exec="create"/>
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
        <el-form-item label="启用">
          <el-switch v-model="form.enable"/>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
                <el-button type="primary" @click="createUser('createUserForm')" size="medium">创建</el-button>
            </span>
    </el-dialog>

    <el-dialog title="修改用户" :visible.sync="updateVisible" width="30%" :destroy-on-close="true" @close="closeFunc">
      <el-form :model="form" label-position="left" label-width="100px" size="small" :rules="rule" ref="updateUserForm">
        <el-form-item label="ID" prop="id">
          <el-input v-model="form.id" autocomplete="off" disabled="true"/>
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
        <el-form-item label="启用">
          <el-switch v-model="form.enable"/>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
                <el-button type="primary" @click="updateUser('updateUserForm')" size="medium">修改</el-button>
            </span>
    </el-dialog>
  </div>
</template>

<script>
  import MsCreateBox from "./CreateBox";

  export default {
    name: "MsUser",
    components: {MsCreateBox},
    created() {
      this.getUserList();
    },
    methods: {
      create() {
        this.createVisible = true;
      },
      edit(row) {
        window.console.log(row);
        // this.loading = true;
        this.updateVisible = true;
        this.form = row;
        /*let self = this;
        let getUser1 = this.$get("/test/user");
        let getUser2 = this.$get("/test/sleep");
        this.$all([getUser1, getUser2], function (r1, r2) {
          window.console.log(r1.data.data, r2.data.data);
          self.loading = false;
        });*/
        /*this.$post("/update", this.form).then(()=>{
          this.updateVisible = false;
          this.getUserList();
          self.loading = false;
        })*/
      },
      del(row) {
        window.console.log(row);
        this.$confirm('此操作将永久删除该用户, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$get(`/user/delete/${row.id}`).then(() => {
            this.getUserList()
          }),
          this.$message({
            type: 'success',
            message: '删除成功!'
          });
        }).catch(() => {
          this.$message({
            type: 'info',
            message: '已取消删除'
          });
        });
      },
      createUser: function (createUserForm) {
        this.$refs[createUserForm].validate(valide => {
          if (valide) {
            this.$post("/user/add", this.form)
              .then(() => {
                this.$message({
                    type: 'success',
                    message: '添加成功!'
                },
                this.createVisible = false,
                this.getUserList())
              });
          } else {
            return false;
          }
        })
      },
      updateUser(updateUserForm) {
        this.$refs[updateUserForm].validate(valide => {
          if (valide) {
            this.$post("/user/update", this.form)
              .then(() => {
                this.$message({
                    type: 'success',
                    message: '修改成功!'
                  },
                  this.updateVisible = false,
                  this.getUserList(),
                  self.loading = false
                )
              });
          } else {
            return false;
          }
        })
      },
      getUserList() {
        this.$get("/user/list").then(response => {
          this.items = response.data.data;
        })
      },
      closeFunc: function () {
        this.form = {};
      }
    },
    data() {
      return {
        loading: false,
        createVisible: false,
        updateVisible: false,
        btnTips: "添加用户",
        condition: "",
        items: [],
        form: {},
        rule: {
          id: [
            { required: true, message: '请输入ID', trigger: 'blur'},
            { min: 2, max: 10, message: '长度在 2 到 10 个字符', trigger: 'blur' },
            {
              required: true,
              pattern: /^[\u4e00-\u9fa5_a-zA-Z0-9.·-]+$/,
              message: 'ID不支持特殊字符',
              trigger: 'blur'
            }
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
            {required: true, message: '请输入邮箱', trigger: 'blur'},
            {
              required: true,
              pattern: /^([A-Za-z0-9_\-.])+@(163.com|qq.com|gmail.com|126.com)$/,
              message: '邮箱格式不正确！',
              trigger: 'blur'
            }
          ]

        }
      }
    }
  }
</script>

<style scoped>
  .search {
    width: 240px;
  }
</style>
