<template>
  <ms-edit-dialog
    :visible.sync="visible"
    width="400px"
    :title="$t('commons.delete_all_version')"
    :with-footer="false"
    :close-on-click-modal="false"
    @close="handleClose">
    <el-row>
      <div v-loading="loading">
        <el-row>
          {{ $t('commons.copy') }}
          <el-select v-model="versionId" size="small" :placeholder="$t('project.version.please_input_version')">
            <el-option v-for="v in versionData" :key="v.id" :label="v.name" :value="v.id" />
          </el-select>
          {{ $t('commons.version_data') }}
        </el-row>
        <el-row style="margin: 0">
          <p style="color: #939496">{{ $t('api_definition.copy_data_from_other_version_tips') }}</p>
        </el-row>
        <el-row style="margin-top: 10px">
          <el-checkbox v-model="selectCase" v-permission="['PROJECT_API_DEFINITION:READ+CREATE_CASE']">{{
            $t('commons.api_case')
          }}</el-checkbox>
          <el-checkbox v-model="selectMock">{{ $t('commons.mock') }}</el-checkbox>
        </el-row>
      </div>
    </el-row>
    <template v-slot:footer>
      <el-button type="primary" :loading="saving" size="small" @click="save" @keydown.enter.native.prevent>{{
        $t('commons.save')
      }}</el-button>
    </template>
  </ms-edit-dialog>
</template>

<script>
import MsEditDialog from '@/business/commons/MsEditDialog';
import { getProjectVersions } from '@/api/xpack';
export default {
  name: 'VersionSelector',
  components: { MsEditDialog },
  data() {
    return {
      loading: false,
      visible: false,
      saving: false,
      versionId: '',
      versionData: [],
      selectCase: true,
      selectMock: true,
    };
  },
  props: {
    tips: String,
    projectId: String,
  },
  methods: {
    open(projectId) {
      this.saving = false;
      this.selectMock = true;
      this.selectCase = true;
      this.versionId = '';
      this.versionData = [];
      this.visible = true;
      this.loading = true;

      getProjectVersions(projectId)
        .then((response) => {
          if (response.data) {
            this.versionData = response.data;
          } else {
            this.versionData = [];
          }

          this.loading = false;
        })
        .catch(() => {
          this.loading = false;
        });
    },
    handleClose() {
      this.saving = false;
      this.$emit('handleClose');
      this.visible = false;
    },
    save() {
      if (!this.versionId || this.versionId === '') {
        this.$error(this.$t('project.version.please_input_version'));
      } else {
        this.saving = true;
        this.$emit('handleSave', this.versionId, this.selectCase, this.selectMock);
      }
    },
  },
};
</script>

<style scoped></style>
