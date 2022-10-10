<template>
  <el-dialog :close-on-click-modal="false" :visible.sync="showVisible" width="50%" :destroy-on-close="true">
    <el-form :model="licenseForm" label-position="right" label-width="120px" size="small" :rules="rule"
             ref="editLicenseForm">

      <el-form-item label="License" prop="licenseCode" style="margin-bottom: 29px">
        <el-upload
          :element-loading-text="$t('test_track.case.import.importing')"
          element-loading-spinner="el-icon-loading"
          ref="upload"
          :show-file-list="false"
          :on-change="handlePreview"
          :auto-upload="false"
          action="">
          <template v-slot:trigger>
            <el-button size="mini" type="success" plain>{{ $t('test_track.case.import.click_upload') }}</el-button>
          </template>
        </el-upload>
        <el-input v-model="licenseForm.licenseCode" autocomplete="off" type="textarea"
                  :autosize="{ minRows: 10}" placeholder="License Code" @change="handlePreview"/>
      </el-form-item>
    </el-form>

    <div slot="footer">
      <el-button @click="cancel">{{ $t('commons.cancel') }}</el-button>
      <el-button type="primary" @click="confirm">{{ $t('commons.confirm') }}</el-button>
    </div>

  </el-dialog>
</template>

<script>
import {saveLicense} from "@/api/license";

export default {
  name: "ValidLicense",
  props: {
    visible: {
      type: Boolean
    }
  },
  data() {
    return {
      showVisible: false,
      licenseForm: {
        licenseCode: ""
      },
      rule: {
        licenseCode: [
          {
            required: true,
            message: "License" + this.$t('commons.cannot_be_null'),
            trigger: 'blur'
          }
        ],
      }
    }
  },
  watch: {
    visible: function (o) {
      this.showVisible = o;
    }
  },
  computed: {
    isDisabled() {
      return false;
    }
  },
  methods: {
    handlePreview(file) {
      const isLt1M = file.size / 1024 / 1024 < 1;
      if (file.size != undefined && !isLt1M) {
        this.$refs.upload.clearFiles;
        this.$error(this.$t("test_track.case.import.upload_limit_other_size") + " 1 M");
        return;
      }
      let reader = new FileReader();
      if (typeof FileReader === "undefined") {
        this.$message({
          type: "info",
          message: this.$t('license.valid_license_error')
        });
        return;
      }
      reader.readAsText(file.raw, "UTF-8");
      let _this = this;
      reader.onload = function (e) {
        _this.licenseForm.licenseCode = e.target.result;
      };
    },
    confirm() {
      this.$refs['editLicenseForm'].validate(valid => {
        if (valid) {
          saveLicense(this.licenseForm)
            .then(response => {
              this.$success(this.$t("commons.save_success"));
              this.$emit('confirm', response.data);
              this.$emit('update:visible', false);
              this.showVisible = false;
            });
        }
      })
    },
    cancel() {
      this.$emit('update:visible', false);
    },
  }
}
</script>
