<template>
  <el-form :model="editData" label-position="right" label-width="80px" size="small" ref="form4" :rules="rules">
    <el-form-item :label="$t('api_test.variable_name')" prop="name">
      <el-input v-model="editData.name" :placeholder="$t('api_test.variable_name')"></el-input>
    </el-form-item>

    <el-form-item :label="$t('commons.description')" prop="description">
      <el-input class="ms-http-textarea"
                v-model="editData.description"
                type="textarea"
                :autosize="{ minRows: 2, maxRows: 10}"
                :rows="2" size="small"
                :disabled="disabled"
      />
    </el-form-item>

    <el-form-item :label="$t('api_test.value')" prop="value">
      <el-input v-model="editData.value" :disabled="disabled" placeholder="列表数据用,分隔"/>
    </el-form-item>
  </el-form>
</template>

<script>
export default {
  name: "MsEditListValue",
  components: {},
  props: {
    editData: {},
  },
  computed: {
    disabled() {
      return !(this.editData.name && this.editData.name !== "");
    }
  },
  data() {
    return {
      rules: {
        name: [
          {required: true, message: this.$t('test_track.case.input_name'), trigger: 'blur'},
        ],
      },
    }
  },
}
</script>

<style scoped>

</style>
