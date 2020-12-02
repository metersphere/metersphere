<template>
  <el-dialog :close-on-click-modal="false" :title="$t('api_test.definition.request.title')" :visible.sync="visible"
             width="45%"
             :destroy-on-close="true">
    <el-form :model="tagForm" label-position="right" label-width="80px" size="small" :rules="rule"
             ref="tagForm">
      <el-form-item :label="$t('commons.name')" prop="name">
        <el-input v-model="tagForm.name" autocomplete="off" :placeholder="$t('commons.name')"/>
      </el-form-item>
    </el-form>

    <template v-slot:footer>
      <ms-dialog-footer
        @cancel="visible = false"
        @confirm="saveTag">
      </ms-dialog-footer>

    </template>

  </el-dialog>
</template>

<script>
  import {WORKSPACE_ID} from '@/common/js/constants';
  import {getCurrentUser, getUUID} from "@/common/js/utils";
  import MsDialogFooter from "@/business/components/common/components/MsDialogFooter";

  export default {
    name: "MsAddTag",
    components: {MsDialogFooter},
    props: {},
    data() {
      return {
        tagForm: {},
        visible: false,
        currentModule: {},
        projectId: "",
        userOptions: [],
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
    }
    ,
    methods: {
      saveTag() {
        this.$refs['tagForm'].validate((valid) => {
          if (valid) {
            let path = "/api/tag/create";
            this.setParameter();
            this.result = this.$post(path, this.tagForm, () => {
              this.visible = false;
              this.$emit('refreshTags');
            });
          } else {
            return false;
          }
        })
      },
      setParameter() {
        this.tagForm.id = getUUID();
        this.tagForm.projectId = this.projectId;
      },
      open(projectId) {
        this.projectId = projectId;
        this.visible = true;
      }
    }
  }
</script>
