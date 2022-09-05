<template>
  <el-dialog :title="dialogTitle"
             :visible.sync="dialogFormVisible"
             :before-close="close"
             width="600px">
    <el-form :model="metadataForm" :rules="metadataRule" ref="form" label-width="80px" @submit.native.prevent>
      <el-form-item
        :label="$t('project.project_file.file.branch')"
        prop="repositoryBranch">
        <el-input v-model="metadataForm.repositoryBranch"></el-input>
      </el-form-item>
      <el-form-item
        :label="$t('project.project_file.file.path')"
        prop="repositoryPath">
        <el-input v-model="metadataForm.repositoryPath"></el-input>
      </el-form-item>
    </el-form>

    <template v-slot:footer>
      <el-button @click="close">{{ $t('commons.cancel') }}</el-button>
      <el-button v-xpack v-prevent-re-click type="primary" :loading="isSaveBtnLoading" @click="saveFileMetadata"
                 @keydown.enter.native.prevent>{{ $t('commons.confirm') }}
      </el-button>
    </template>
  </el-dialog>
</template>

<script>
import {getCurrentProjectID, getCurrentUserId, getUUID, listenGoBack, removeGoBackListener} from "@/common/js/utils";
import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";

export default {
  name: "FileMetadataDialog",
  components: {
    MsDialogFooter,
  },
  data() {
    return {
      dialogFormVisible: false,
      dialogTitle: '',
      operationType: '',
      isSaveBtnLoading: false,
      projectId: getCurrentProjectID(),
      metadataForm: {
        repositoryBranch: '',
        repositoryPath: '',
      },
      metadataRule: {
        repositoryBranch: [
          {required: true, message: this.$t('project.project_file.validation.input_file_branch'), trigger: 'blur'},
        ],
        repositoryPath: [
          {required: true, message: this.$t('project.project_file.validation.input_file_path'), trigger: 'blur'},
        ],
      },
    }
  },
  watch: {},
  props: {
    moduleId: String,
  },
  computed: {},
  methods: {
    saveFileMetadata() {
      this.$refs.form.validate(valid => {
        if (valid) {
          let url = '';
          let param = JSON.parse(JSON.stringify(this.metadataForm));
          param.storage = 'GIT';
          if (param.id) {
            url = "/file/metadata/edit";
          } else {
            url = "/file/metadata/create";
            param.id = getUUID();
            param.createUser = getCurrentUserId();
            param.updateUser = getCurrentUserId();
            param.projectId = this.projectId;
            param.moduleId = this.moduleId;
          }
          let formData = new FormData();
          formData.append("request", new Blob([JSON.stringify(param)], {type: "application/json"}));
          let options = {
            method: 'POST',
            url: url,
            data: formData,
            headers: {
              'Content-Type': undefined
            }
          };
          this.isSaveBtnLoading = true;
          this.result = this.$request(options, () => {
            this.$success(this.$t('commons.save_success'));
            this.$emit('refresh');
            this.isSaveBtnLoading = false;
            this.close();
          }, (error) => {
            this.$emit('refresh');
            this.isSaveBtnLoading = false;
          });

        } else {
          return false;
        }
      });
    },
    open(operationType, param) {
      this.isSaveBtnLoading = false;
      listenGoBack(this.close);
      this.initDialog(operationType);
      if (param) {
        this.metadataForm = JSON.parse(JSON.stringify(param));
      } else {
        this.metadataForm = {
          repositoryBranch: '',
          repositoryPath: '',
        };
      }
      this.dialogFormVisible = true;
    },
    close() {
      this.isSaveBtnLoading = false;
      removeGoBackListener(this.close);
      this.$refs.form.resetFields();
      this.dialogFormVisible = false;
    },
    initDialog(operationType) {
      this.operationType = operationType;
      //初始化弹窗数据-title
      if (operationType === 'create') {
        this.dialogTitle = this.$t('commons.create');
      } else if (operationType === 'edit') {
        this.dialogTitle = this.$t('commons.edit');
      } else {
        this.dialogTitle = '';
      }
    },
    clearForm() {
      this.metadataForm = {
        repositoryBranch: '',
        repositoryPath: '',
      };
    }
  }
}
</script>

<style scoped>

</style>
