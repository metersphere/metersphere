<template>
  <el-dialog
    :close-on-click-modal="false"
    :title="$t('api_test.definition.request.save_as_case')"
    :visible.sync="httpVisible"
    width="30%"
    :destroy-on-close="true"
    append-to-body
  >
    <el-form
      :model="httpForm"
      label-position="right"
      label-width="80px"
      size="small"
      :rules="rule"
      ref="httpForm"
      v-if="!loading"
    >
      <el-form-item :label="$t('api_definition.case_name')" prop="name">
        <el-input
          v-model="httpForm.name"
          autocomplete="off"
          :placeholder="$t('api_definition.case_name')"
          show-word-limit
          maxlength="100"
        />
      </el-form-item>
    </el-form>
    <template v-slot:footer>
      <ms-dialog-footer
        @cancel="httpVisible = false"
        @confirm="saveApi"
        v-prevent-re-click
      >
      </ms-dialog-footer>
    </template>
  </el-dialog>
</template>

<script>
import { createApiCase } from "@/api/api-test-case";
import MsDialogFooter from "metersphere-frontend/src/components/MsDialogFooter";
import { getUUID } from "metersphere-frontend/src/utils";

export default {
  name: "MsAddApiCase",
  components: { MsDialogFooter },
  data() {
    return {
      httpVisible: false,
      loading: false,
      httpForm: {},
      rule: {
        name: [
          {
            required: true,
            message: this.$t("test_track.case.input_name"),
            trigger: "blur",
          },
          {
            max: 100,
            message: this.$t("test_track.length_less_than") + "100",
            trigger: "blur",
          },
        ],
      },
    };
  },
  methods: {
    saveApi() {
      this.$refs.httpForm.validate(async (valid) => {
        if (valid) {
          this.saveCase(this.httpForm);
        }
      });
    },
    saveCase(api) {
      let obj = {
        apiDefinitionId: api.id,
        name: api.name,
        priority: "P0",
        active: true,
        uuid: getUUID(),
        request: api,
      };
      obj.projectId = api.projectId;
      obj.id = obj.uuid;
      obj.versionId = api.versionId;
      let bodyFiles = this.getBodyUploadFiles(obj);
      createApiCase(null, bodyFiles, obj).then((response) => {
        this.$success(this.$t("commons.save_success"));
        this.httpVisible = false;
      });
    },
    getBodyUploadFiles(data) {
      let bodyUploadFiles = [];
      data.bodyUploadIds = [];
      let request = data.request;
      if (request.body) {
        if (request.body.kvs) {
          request.body.kvs.forEach((param) => {
            if (param.files) {
              param.files.forEach((item) => {
                if (item.file) {
                  let fileId = getUUID().substring(0, 8);
                  item.name = item.file.name;
                  item.id = fileId;
                  data.bodyUploadIds.push(fileId);
                  bodyUploadFiles.push(item.file);
                }
              });
            }
          });
        }
        if (request.body.binary) {
          request.body.binary.forEach((param) => {
            if (param.files) {
              param.files.forEach((item) => {
                if (item.file) {
                  let fileId = getUUID().substring(0, 8);
                  item.name = item.file.name;
                  item.id = fileId;
                  data.bodyUploadIds.push(fileId);
                  bodyUploadFiles.push(item.file);
                }
              });
            }
          });
        }
      }
      return bodyUploadFiles;
    },
    reload() {
      this.loading = true;
      this.$nextTick(() => {
        this.loading = false;
      });
    },
    open(api) {
      if (api) {
        this.httpForm = JSON.parse(JSON.stringify(api));
        this.httpVisible = true;
      }
    },
  },
};
</script>

<style scoped>
.create-tip {
  color: #8c939d;
}
</style>
