<template>
  <div v-loading="loading">

    <el-card>
      <div slot="header">
        <el-row type="flex" justify="space-between" align="middle">
          <span class="title">组织
            <ms-create-box :tips="btnTips" :exec="create"/>
          </span>
          <span class="search">
            <el-input type="text" size="small" placeholder="根据名称搜索" prefix-icon="el-icon-search"
                              maxlength="60" v-model="condition" clearable/>
          </span>
        </el-row>
      </div>
      <el-table :data="items" style="width: 100%">
        <el-table-column prop="name" label="名称"/>
        <el-table-column prop="description" label="描述"/>
        <el-table-column>
          <template slot-scope="scope">
            <el-button @click="edit(scope.row)" type="primary" icon="el-icon-edit" size="mini" circle/>
            <el-button @click="del(scope.row)" type="danger" icon="el-icon-delete" size="mini" circle/>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog title="创建组织" :visible.sync="createVisible" width="30%" @closed="closeFunc" :destroy-on-close="true">
      <el-form :model="form" label-position="left" label-width="100px" size="small" :rules="rule" ref="createOrganization">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" autocomplete="off"/>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="createOrganization('createOrganization')" size="medium">创建</el-button>
      </span>
    </el-dialog>

    <el-dialog title="修改组织" :visible.sync="updateVisible" width="30%" :destroy-on-close="true" @close="closeFunc">
      <el-form :model="form" label-position="left" label-width="100px" size="small" :rules="rule" ref="updateOrganizationForm">
        <el-form-item label="用户名" prop="name">
          <el-input v-model="form.name" autocomplete="off"/>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="form.description" autocomplete="off"/>
        </el-form-item>
      </el-form>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="updateOrganization('updateOrganizationForm')" size="medium">修改</el-button>
      </span>
    </el-dialog>

  </div>
</template>

<script>
  import MsCreateBox from "./CreateBox";

  export default {
    name: "MsOrganization",
    components: {MsCreateBox},
    created() {
      this.getOrganizationList();
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
      },
      del(row) {
        window.console.log(row);
        this.$confirm('此操作将永久删除该组织, 是否继续?', '提示', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$get(`/organization/delete/${row.id}`).then(() => {
            this.getOrganizationList()
          });
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
      createOrganization(createOrganizationForm) {
        this.$refs[createOrganizationForm].validate( valide => {
          if (valide) {
            this.$post("/organization/add", this.form)
              .then(() => {
                this.$message({
                    type: 'success',
                    message: '添加成功!'
                  },
                this.createVisible = false,
                this.getOrganizationList())
              });
          } else {
            return false;
          }
        })
      },
      updateOrganization(udpateOrganizationForm) {
        this.$refs[udpateOrganizationForm].validate(valide => {
          if (valide) {
            this.$post("/organization/update", this.form)
              .then(() => {
                this.$message({
                    type: 'success',
                    message: '修改成功!'
                  },
                this.updateVisible = false,
                this.getOrganizationList(),
                self.loading = false)
              });
          } else {
            return false;
          }
        })
      },
      getOrganizationList() {
        this.$get("/organization/list").then(response => {
          this.items = response.data.data;
        })
      },
      closeFunc() {
        this.form = {};
      }
    },
    data() {
      return {
        loading: false,
        createVisible: false,
        updateVisible: false,
        btnTips: "添加组织",
        condition: "",
        items: [],
        form: {},
        rule: {
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
          description: [
            { max: 60, message: '最大长度 60 个字符', trigger: 'blur'}
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
