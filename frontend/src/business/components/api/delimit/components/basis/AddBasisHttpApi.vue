<template>
  <el-dialog :close-on-click-modal="false" :title="$t('api_test.delimit.request.title')" :visible.sync="httpVisible"
             width="35%"
             @closed="closeApi"
             :destroy-on-close="true">
    <el-form :model="httpForm" label-position="right" label-width="120px" size="small" :rules="rule" ref="httpForm">

      <el-form-item :label="$t('commons.name')" prop="name">
        <el-input v-model="httpForm.name" autocomplete="off" :placeholder="$t('commons.name')"/>
      </el-form-item>
      <el-form-item :label="$t('api_report.request')" prop="request">
        <el-select v-model="value" style="width: 20%">
          <el-option v-for="item in options" :key="item.id" :label="item.label" :value="item.id"/>
        </el-select>
        <el-input style="margin-left:10px;width: 76%" v-model="httpForm.request" autocomplete="off"
                  :placeholder="$t('api_test.delimit.request.path_info')"/>
      </el-form-item>
      <el-form-item :label="$t('api_test.delimit.request.responsible')" prop="responsible">
        <el-input v-model="httpForm.responsible" autocomplete="off"
                  :placeholder="$t('api_test.delimit.request.responsible')"/>
      </el-form-item>
      <el-form-item :label="$t('commons.description')" prop="description" style="margin-bottom: 29px">
        <el-input v-model="httpForm.description"
                  :placeholder="$t('commons.description')"/>
      </el-form-item>

    </el-form>

    <template v-slot:footer>
      <ms-dialog-footer
        @cancel="httpVisible = false"
        @confirm="saveApi"/>
    </template>
  </el-dialog>
</template>

<script>
  import MsDialogFooter from "../../../../common/components/MsDialogFooter";

  export default {
    name: "MsAddBasisHttpApi",
    components: {MsDialogFooter},
    props: {},
    data() {
      return {
        httpForm: {},
        httpVisible: false,
        rule: {},
        value: "",
        options: [{
          id: 'GET',
          label: 'GET'
        }, {
          id: 'POST',
          label: 'POST'
        }],
      }
    },
    created() {

    },
    methods: {
      saveApi() {

      },
      closeApi() {

      },
      open() {
        this.httpVisible = true;
      },
    }
  }
</script>

<style scoped>

  .ht-btn-remove {
    color: white;
    background-color: #DCDFE6;
  }

  .ht-btn-confirm {
    color: white;
    background-color: #1483F6;
  }

  .ht-btn-add {
    border: 0px;
    margin-top: 10px;
    color: #1483F6;
    background-color: white;
  }

  .ht-tb {
    background-color: white;
    border: 0px;
  }
</style>
