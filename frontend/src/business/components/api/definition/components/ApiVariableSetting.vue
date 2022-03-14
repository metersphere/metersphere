<template>
  <ms-edit-dialog
      :visible.sync="visible"
      width="700px"
      :title="$t('run_mode.other_config')"
      :with-footer="false"
      :append-to-body="appendToBody"
      :close-on-click-modal="true">
    <el-form>
      <el-row>
        <el-col :span="8">
          <el-form-item :label="$t('schema.minLength')">
            <el-input-number :min="0" v-model="data.min" :placeholder="$t('schema.minLength')" size="small"/>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item :label="$t('schema.maxLength')">
            <el-input-number :min="0" v-model="data.max" :placeholder="$t('schema.maxLength')" size="small"/>
          </el-form-item>
        </el-col>
        <el-col :span="8">
          <el-form-item :label="$t('commons.encode')">
            <el-select v-model="data.urlEncode" size="small" clearable style="width: 50%;">
              <el-option
                  v-for="item in options"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value">
              </el-option>
            </el-select>
          </el-form-item>
        </el-col>
      </el-row>
      <el-row>
        <el-col>
          <el-form-item :label="$t('commons.description')">
            <el-input v-model="data.description" size="small" maxlength="200"
                      :placeholder="$t('commons.description')" show-word-limit style="width: 80%;margin-left: 25px">
            </el-input>
          </el-form-item>
        </el-col>
      </el-row>
    </el-form>
  </ms-edit-dialog>
</template>

<script>

import MsEditDialog from "@/business/components/common/components/MsEditDialog";

export default {
  name: "ApiVariableSetting",
  components: {MsEditDialog},
  data() {
    return {
      visible: false,
      data: {},
      options: [{
        value: true,
        label: this.$t('commons.yes')
      }, {
        value: false,
        label: this.$t('commons.no')
      }],
    }
  },
  props: {
    appendToBody: {
      type: Boolean,
      default() {
        return false;
      }
    },
  },
  methods: {
    open(item) {
      this.visible = true;
      this.data = item;
    }
  }

}
</script>

<style scoped>

</style>
