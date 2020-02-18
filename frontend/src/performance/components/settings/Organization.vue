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
      <el-table :data="tableData" style="width: 100%">
        <el-table-column type="selection" width="55"/>
        <el-table-column prop="name" label="名称"/>
        <el-table-column prop="description" label="描述"/>
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
    data() {
      return {
        queryPath: '/organization/list',
        deletePath: '/organization/delete/',
        createPath: '/organization/add',
        updatePath: '/organization/update',
        loading: false,
        createVisible: false,
        updateVisible: false,
        multipleSelection: [],
        currentPage: 1,
        pageSize: 5,
        total: 0,
        btnTips: "添加组织",
        condition: "",
        tableData: [],
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
    },
    created() {
      this.initTableData();
    },
    methods: {
      create() {
        this.createVisible = true;
      },
      edit(row) {
        // this.loading = true;
        this.updateVisible = true;
        this.form = row;
      },
      del(row) {
        this.$confirm('是否删除组织' + row.name +' ？', '', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          this.$get(this.deletePath + row.id).then(response => {
            if (response.data.success) {
              this.$message({
                type: 'success',
                message: '删除成功!'
              });
            } else {
              this.$message.error(response.message);
            }
            this.initTableData()
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
            this.$post(this.createPath, this.form)
              .then(response => {
                if (response.data.success) {
                  this.$message({
                    type: 'success',
                    message: '添加成功!'
                  });
                  this.initTableData();
                } else {
                  this.$message.error(response.message);
                }
                this.createVisible = false;
              });
          } else {
            return false;
          }
        })
      },
      updateOrganization(udpateOrganizationForm) {
        this.$refs[udpateOrganizationForm].validate(valide => {
          if (valide) {
            this.$post(this.updatePath, this.form)
              .then(response => {
                if (response.data.success) {
                  this.$message({
                    type: 'success',
                    message: '修改成功!'
                  });
                  this.updateVisible = false;
                } else {
                  this.$message.error(response.message);
                }
                this.initTableData();
                self.loading = false;
              });
          } else {
            return false;
          }
        })
      },
      initTableData() {
        this.$post(this.buildPagePath(this.queryPath)).then(response => {
          if (response.data.success) {
            let data = response.data.data;
            this.total = data.itemCount;
            this.tableData = data.listObject;
          } else {
            this.$message.error(response.message);
          }
        })
      },
      closeFunc() {
        this.form = {};
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
