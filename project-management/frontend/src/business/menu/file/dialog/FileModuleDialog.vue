<template>
  <el-dialog
    :title="dialogTitle"
    :visible.sync="dialogFormVisible"
    :before-close="close"
    width="600px"
  >
    <el-row
      v-show="operationType === 'create'"
      type="flex"
      justify="center"
      style="margin-bottom: 10px"
    >
      <el-radio-group v-model="moduleForm.moduleType">
        <el-radio label="module">{{
          $t("project.project_file.file_module_type.module")
        }}</el-radio>
        <el-radio label="repository">{{
          $t("project.project_file.file_module_type.repository")
        }}</el-radio>
      </el-radio-group>
    </el-row>
    <el-form
      :model="moduleForm"
      :rules="moduleRule"
      ref="form"
      label-width="100px"
      @submit.native.prevent
    >
      <el-form-item
        v-if="moduleForm.moduleType === 'module'"
        :label="$t('test_track.module.name')"
        prop="name"
      >
        <el-input v-model="moduleForm.name"></el-input>
      </el-form-item>
      <div v-if="moduleForm.moduleType === 'repository'">
        <el-form-item
          :label="$t('project.project_file.repository.name')"
          prop="name"
        >
          <el-input v-model="moduleForm.name"></el-input>
        </el-form-item>
        <el-form-item
          :label="$t('project.project_file.repository.path')"
          prop="repositoryPath"
        >
          <el-input v-model="moduleForm.repositoryPath"
                    placeholder="https://gitea.com/xxx/xxx.git"></el-input>
        </el-form-item>

        <el-form-item
          :label="$t('api_test.request.tcp.username')"
          prop="repositoryUserName"
        >
          <el-input v-model="moduleForm.repositoryUserName">
            <template slot="append">
              <el-tooltip
                class="item"
                effect="dark"
                :content="
                  $t('project.project_file.validation.input_gitee_user_please')
                "
                placement="top"
              >
                <i class="el-icon-info" />
              </el-tooltip>
            </template>
          </el-input>
        </el-form-item>

        <el-form-item
          :label="$t('project.project_file.repository.token')"
          prop="repositoryToken"
        >
          <el-input
            show-password
            v-model="moduleForm.repositoryToken"
          ></el-input>
        </el-form-item>
        <el-form-item
          type="textarea"
          :label="$t('project.project_file.repository.desc')"
          prop="repositoryDesc"
        >
          <el-input v-model="moduleForm.repositoryDesc"></el-input>
        </el-form-item>
      </div>
    </el-form>

    <template v-slot:footer>
      <div class="dialog-footer">
        <el-button @click="close">{{ $t("commons.cancel") }}</el-button>
        <el-button
          v-prevent-re-click
          :loading="isConnBtnLoading"
          type="primary"
          @click="testConnect"
          v-show="moduleForm.moduleType === 'repository'"
          @keydown.enter.native.prevent
        >
          {{ $t("system_parameter_setting.test_connection") }}
        </el-button>
        <el-button
          v-prevent-re-click
          :loading="isSaveBtnLoading"
          type="primary"
          @click="saveFileModule"
          @keydown.enter.native.prevent
        >
          {{ $t("commons.confirm") }}
        </el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script>
import {listenGoBack, removeGoBackListener,} from "metersphere-frontend/src/utils";
import {getCurrentProjectID} from "metersphere-frontend/src/utils/token";
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import {createFileModule, modifyFileModule, testConnectRepository,} from "@/api/file";

export default {
  name: "FileModuleDialog",
  components: {
    MsDialogFooter,
  },
  data() {
    return {
      dialogFormVisible: false,
      dialogTitle: "",
      operationType: "",
      projectId: getCurrentProjectID(),
      isConnBtnLoading: false,
      isSaveBtnLoading: false,
      moduleForm: {
        name: "",
        moduleType: "module",
        repositoryUserName: "",
        repositoryPath: "",
        repositoryToken: "",
        repositoryDesc: "",
      },
      moduleRule: {
        name: [
          {
            required: true,
            message: this.$t("test_track.case.input_name"),
            trigger: "blur",
          },
          {
            max: 60,
            message: this.$t("test_track.length_less_than") + "60",
            trigger: "blur",
          },
        ],
      },
    };
  },
  watch: {
    "moduleForm.moduleType"() {
      if (this.moduleForm.moduleType === "module") {
        this.moduleRule = {
          name: [
            {
              required: true,
              message: this.$t("test_track.case.input_name"),
              trigger: "blur",
            },
            {
              max: 60,
              message: this.$t("test_track.length_less_than") + "60",
              trigger: "blur",
            },
          ],
        };
      } else if (this.moduleForm.moduleType === "repository") {
        this.moduleRule = {
          name: [
            {
              required: true,
              message: this.$t("test_track.case.input_name"),
              trigger: "blur",
            },
            {
              max: 60,
              message: this.$t("test_track.length_less_than") + "60",
              trigger: "blur",
            },
          ],
          repositoryPath: [
            {
              required: true,
              message: this.$t(
                "project.project_file.validation.input_repository_path"
              ),
              trigger: "blur",
            },
            {
              max: 255,
              message: this.$t("test_track.length_less_than") + "255",
              trigger: "blur",
            },
          ],
          repositoryToken: [
            {
              required: true,
              message: this.$t(
                "project.project_file.validation.input_repository_token"
              ),
              trigger: "blur",
            },
            {
              max: 255,
              message: this.$t("test_track.length_less_than") + "255",
              trigger: "blur",
            },
          ],
        };
      }
    },
  },
  props: {},
  computed: {},
  methods: {
    testConnect() {
      let param = this.moduleForm;
      this.isConnBtnLoading = true;
      testConnectRepository(param)
        .then(() => {
          this.$success(this.$t("commons.connection_successful"));
          this.isConnBtnLoading = false;
        })
        .catch(() => {
          this.$emit("refresh");
          this.isConnBtnLoading = false;
        });
    },
    saveFileModule() {
      this.$refs.form.validate((valid) => {
        if (valid) {
          let param = this.moduleForm;
          param.projectId = this.projectId;

          if (this.operationType === "create") {
            this.isSaveBtnLoading = true;
            createFileModule(param)
              .then(() => {
                this.$success(this.$t("commons.save_success"));
                this.$emit("refresh");
                this.isSaveBtnLoading = false;
                this.close();
              })
              .catch(() => {
                this.isSaveBtnLoading = false;
                this.$emit("refresh");
              });
          } else if (this.operationType === "edit") {
            this.isSaveBtnLoading = true;
            modifyFileModule(param)
              .then(() => {
                this.$success(this.$t("commons.save_success"));
                this.$emit("refresh");
                this.isSaveBtnLoading = false;
                this.close();
              })
              .catch(() => {
                this.isSaveBtnLoading = false;
                this.$emit("refresh");
              });
          }
        } else {
          return false;
        }
      });
    },
    open(operationType, param) {
      this.isSaveBtnLoading = false;
      this.isConnBtnLoading = false;
      listenGoBack(this.close);
      this.initDialog(operationType);
      if (operationType === "create") {
        param.moduleType = "module";
      }
      this.moduleForm = JSON.parse(JSON.stringify(param));
      this.dialogFormVisible = true;
    },
    close() {
      this.isSaveBtnLoading = false;
      this.isConnBtnLoading = false;
      removeGoBackListener(this.close);
      this.$refs.form.resetFields();
      this.dialogFormVisible = false;
    },
    initDialog(operationType) {
      this.operationType = operationType;
      //初始化弹窗数据-title
      if (operationType === "create") {
        this.dialogTitle = this.$t("commons.create");
      } else if (operationType === "edit") {
        this.dialogTitle = this.$t("commons.edit");
      } else {
        this.dialogTitle = "";
      }
    },
    clearForm() {
      this.moduleForm = {
        name: "",
        moduleType: "module",
        repositoryPath: "",
        repositoryToken: "",
        repositoryUserName: "",
        repositoryDesc: "",
      };
    },
  },
};
</script>

<style scoped></style>
