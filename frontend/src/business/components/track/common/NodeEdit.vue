<template>

  <el-dialog :title="$t('test_track.module.add_module')"
             :visible.sync="dialogFormVisible"
             width="30%">

    <el-row type="flex" justify="center">
      <el-col :span="18">
        <el-form :model="form" :rules="rules">
          <el-form-item
            :label="$t('test_track.module.name')"
            :label-width="formLabelWidth"
            prop="name">
            <el-input v-model="form.name" autocomplete="off"></el-input>
          </el-form-item>
        </el-form>
      </el-col>
    </el-row>

    <template v-slot:footer>
      <ms-dialog-footer
        @cancel="dialogFormVisible = false"
        @confirm="saveNode"/>
    </template>


  </el-dialog>

</template>

<script>
  import {CURRENT_PROJECT} from '../../../../common/js/constants';
  import MsDialogFooter from '../../common/components/MsDialogFooter';

    export default {
      components: {MsDialogFooter},
      data() {
        return {
          name: "NodeEdit",
          form: {
            name: '',
          },
          rules:{
            name :[
              {required: true, message: this.$t('test_track.case.input_name'), trigger: 'blur'},
              { max: 30, message: this.$t('test_track.length_less_than') + '30', trigger: 'blur' }
            ]
          },
          type: '',
          node: {},
          formLabelWidth: '80px',
          dialogFormVisible: false,
        }
      },
      methods: {
        open(type, data) {
          this.type = type;
          this.node = data;
          this.dialogFormVisible = true;
        },
        saveNode() {
          let param = {};
          let url = this.buildParam(param);
          this.$post(url, param, () => {
            this.$message.success(this.$t('commons.save_success'));
            this.$emit('refresh');
            this.close();
          });
        },
        buildParam(param, ) {
          let url = '';
          if (this.type === 'add') {
            url = '/case/node/add';
            param.level = 1;
            if (this.node) {
              //非根节点
              param.pId = this.node.id;
              param.level = this.node.level + 1;
            }
          } else if (this.type === 'edit') {
            url = '/case/node/edit';
            param.id = this.node.id
          }
          param.name = this.form.name;
          param.label = this.form.name;
          if (localStorage.getItem(CURRENT_PROJECT)) {
            param.projectId = JSON.parse(localStorage.getItem(CURRENT_PROJECT)).id;
          }
          return url;
        },
        close() {
          this.form.name = '';
          this.dialogFormVisible = false;
        }
      }
    }
</script>

<style scoped>

</style>
