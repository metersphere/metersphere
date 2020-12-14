<template>
  <el-dialog :close-on-click-modal="false" :title="$t('api_test.definition.request.title')" :visible.sync="visible"
             width="45%"
             :destroy-on-close="true">
    <el-form :model="tagForm" label-position="right" label-width="80px" size="small" :rules="rule" ref="tagForm">
      <el-form-item :label="$t('commons.name')" prop="name">
        <el-row>
          <el-col :span="20">
            <el-input v-model="tagForm.name" autocomplete="off" :placeholder="$t('commons.name')"/>
          </el-col>
          <el-col :span="4">
            <el-button style="margin-left: 20px" @click="saveTag">{{$t('commons.save')}}</el-button>
          </el-col>
        </el-row>
      </el-form-item>
    </el-form>
    <el-table :data="tagData" row-key="id">

      <el-table-column prop="name" :label="$t('api_test.definition.api_name')" show-overflow-tooltip/>
      <el-table-column :label="$t('commons.operating')" min-width="130" align="center">
        <template v-slot:default="scope">
          <el-button type="text" @click="editApi(scope.row)">编辑</el-button>
          <el-button type="text" @click="handleDelete(scope.row)" style="color: #F56C6C">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <ms-table-pagination :change="initTable" :current-page.sync="currentPage" :page-size.sync="pageSize"
                         :total="total"/>
    <template v-slot:footer>
      <ms-dialog-footer
        @cancel="confirm"
        @confirm="confirm">
      </ms-dialog-footer>
    </template>

  </el-dialog>
</template>

<script>
  import {WORKSPACE_ID} from '@/common/js/constants';
  import {getCurrentUser, getUUID,getCurrentProjectID} from "@/common/js/utils";
  import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";
  import MsTablePagination from "../../../common/pagination/TablePagination";
  export default {
    name: "MsAddTag",
    components: {MsDialogFooter,MsTablePagination},
    props: {},
    data() {
      return {
        tagForm: {},
        visible: false,
        currentModule: {},
        projectId: "",
        userOptions: [],
        currentPage: 1,
        pageSize: 10,
        total: 0,
        tagData: [],
        path: "/api/tag/create",
        rule: {
          name: [
            {required: true, message: this.$t('test_track.case.input_name'), trigger: 'blur'},
            {max: 50, message: this.$t('test_track.length_less_than') + '50', trigger: 'blur'}
          ],
          principal: [{
            required: true,
            message: this.$t('api_test.automation.scenario.select_principal'),
            trigger: 'change'
          }],
        },
      }
    },
    methods: {
      saveTag() {
        if (this.tagForm.id != undefined && this.tagForm.id != null) {
          this.path = "/api/tag/update";
        } else {
          this.path = "/api/tag/create";
          this.tagForm.id = getUUID();
        }
        this.$refs['tagForm'].validate((valid) => {
          if (valid) {
            this.setParameter();
            this.result = this.$post(this.path, this.tagForm, () => {
              this.$success(this.$t('commons.save_success'));
              this.initTable();
            });
          } else {
            return false;
          }
        })
      },
      setParameter() {
        this.tagForm.projectId = this.projectId;
      },
      open() {
        this.projectId = getCurrentProjectID();
        this.visible = true;
        this.initTable();
      },
      initTable() {
        let condition = {};
        condition.projectId = this.projectId;
        this.result = this.$post("/api/tag/getTgas/" + this.currentPage + "/" + this.pageSize, condition, response => {
          this.total = response.data.itemCount;
          this.tagData = response.data.listObject;
        });
      },
      editApi(row) {
        this.tagForm = row;
      },
      handleDelete(row) {
        this.result = this.$get("/api/tag/delete/" + row.id, response => {
          this.$success(this.$t('commons.delete_success'));
          this.initTable();
        });
      },
      confirm() {
        this.visible = false
        this.$emit('refreshTags');
      }
    }
  }
</script>
