<template>
  <div class="project-container">
    <div class="main-content">
      <el-card v-loading="result.loading">
        <div slot="header">
          <el-row type="flex" justify="space-between" align="middle">
            <span class="title">
              {{$t('commons.project')}}
              <ms-create-box :tips="btnTips" :exec="create"/>
            </span>
            <span class="search">
                    <el-input type="text" size="small" :placeholder="$t('project.search_by_name')"
                              prefix-icon="el-icon-search"
                              maxlength="60" v-model="condition" clearable/>
                </span>
          </el-row>
        </div>
        <el-table :data="items" style="width: 100%">
          <el-table-column prop="name" :label="$t('commons.name')"/>
          <el-table-column prop="description" :label="$t('commons.description')"/>
          <el-table-column prop="workspaceName" label="所属工作空间"/>
          <el-table-column>
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

      <el-dialog :title="title" :visible.sync="createVisible">
        <el-form :model="form" :rules="rules" ref="form" label-position="left" label-width="100px" size="small">
          <el-form-item :label="$t('commons.name')">
            <el-input v-model="form.name" autocomplete="off"></el-input>
          </el-form-item>
          <el-form-item :label="$t('commons.description')">
            <el-input type="textarea" v-model="form.description"></el-input>
          </el-form-item>
          <el-form-item :label="$t('project.owning_workspace')" v-permission="['org_admin']">
            <el-select v-model="form.workspaceId" :placeholder="$t('project.please_choose_workspace')" class="select-width">
              <el-option
                v-for="item in form.allWorkspace"
                :key="item.id"
                :label="item.name"
                :value="item.id">
              </el-option>
            </el-select>
          </el-form-item>
        </el-form>
        <div slot="footer" class="dialog-footer">
          <el-button type="primary" @click="submit('form')" size="medium">{{$t('commons.save')}}</el-button>
        </div>
      </el-dialog>
    </div>
  </div>
</template>

<script>
  import MsCreateBox from "../settings/CreateBox";
  import {Message} from "element-ui";

  export default {
    name: "MsProject",
    components: {MsCreateBox},
    data() {
      return {
        createVisible: false,
        result: {},
        btnTips: this.$t('project.create'),
        title: this.$t('project.create'),
        condition: "",
        items: [],
        form: {},
        currentPage: 1,
        pageSize: 5,
        total: 0,
        rules: {
          name: [
            {required: true, message: this.$t('project.input_name'), trigger: 'blur'},
            {min: 2, max: 50, message: this.$t('commons.input_limit', [2, 50]), trigger: 'blur'}
          ]
        },
      }
    },
    mounted() {
      if (this.$route.path.split('/')[2] === 'create') {
        this.create();
        this.$router.push('/project/all');
      }
      this.list();
    },
    watch: {
      '$route'(to) {
        if (to.path.split('/')[2] === 'create') {
          this.create();
          this.$router.push('/project/all');
        }
      }
    },
    destroyed() {
      this.createVisible = false;
    },
    methods: {
      create() {
        this.title = this.$t('project.create');
        this.createVisible = true;
        this.form = {};
      },
      edit(row) {
        this.title = this.$t('project.edit');
        this.createVisible = true;
        this.form = Object.assign({}, row);

        let workspaceId = this.form.workspaceId;
        this.result = this.$get('workspace/list/orgworkspace/', response => {
          this.$set(this.form, "allWorkspace", response.data);
        })
        // 编辑使填充角色信息
        this.$set(this.form, 'workspaceId', workspaceId);
      },
      submit(formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            let saveType = "add";
            if (this.form.id) {
              saveType = "update"
            }
            this.result = this.$post("/project/" + saveType, this.form, () => {
              this.createVisible = false;
              this.list();
              Message.success(this.$t('commons.save_success'));
            });
          } else {
            return false;
          }
        });
      },
      del(row) {
        this.$confirm(this.$t('project.delete_confirm'), this.$t('commons.prompt'), {
          confirmButtonText: this.$t('commons.confirm'),
          cancelButtonText: this.$t('commons.cancel'),
          type: 'warning'
        }).then(() => {
          this.$get('/project/delete/' + row.id, () => {
            Message.success(this.$t('commons.delete_success'));
            this.list();
          });
        }).catch(() => {
        });
      },
      list() {
        let url = "/project/list/" + this.currentPage + '/' + this.pageSize;
        this.result = this.$post(url, {}, (response) => {
          let data = response.data;
          this.items = data.listObject;
          this.total = data.itemCount;
        })
      },
      handleSizeChange(size) {
        this.pageSize = size;
        this.list();
      },
      handleCurrentChange(current) {
        this.currentPage = current;
        this.list();
      },
    }
  }
</script>

<style scoped>
  .project-container {
    padding: 15px;
    width: 100%;
    height: 100%;
    box-sizing: border-box;
  }

  .main-content {
    margin: 0 auto;
    width: 100%;
    max-width: 1200px;
  }

  .table-page {
    padding-top: 20px;
    margin-right: -9px;
    float: right;
  }

  .select-width {
    width: 100%;
  }
</style>
