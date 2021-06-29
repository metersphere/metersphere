<template>
  <span>
     <el-select v-if="data.type === 'select' || data.type === 'multipleSelect'"
                :disabled="disabled"
                :multiple="data.type === 'multipleSelect'"
                @change="handleChange"
                filterable v-model="data[prop]" :placeholder="$t('commons.default')">
      <el-option
        v-for="(item,index) in data.options ? data.options : []"
        :key="index"
        @change="handleChange"
        :label="getTranslateOption(item)"
        :value="item.value">
      </el-option>
    </el-select>

    <el-input
      v-else-if="data.type === 'textarea'"
      type="textarea"
      @change="handleChange"
      :rows="2"
      :disabled="disabled"
      :placeholder="$t('commons.input_content')"
      class="custom-with"
      v-model="data[prop]">
    </el-input>

    <el-checkbox-group
      v-else-if="data.type === 'checkbox'"
      :disabled="disabled"
      v-model="data[prop]">
      <el-checkbox v-for="(item, index) in data.options ? data.options : []"
                   :key="index"
                   @change="handleChange"
                   :label="getTranslateOption(item)"></el-checkbox>
    </el-checkbox-group>

    <el-radio
      v-else-if="data.type === 'radio'"
      v-model="data[prop]"
      :disabled="disabled"
      v-for="(item,index) in data.options ? data.options : []"
      :key="index"
      @change="handleChange"
      :label="getTranslateOption(item)"></el-radio>

    <el-input-number
      v-else-if="data.type === 'int'"
      v-model="data[prop]"
      :disabled="disabled"
      @change="handleChange"></el-input-number>

    <el-input-number
      v-else-if="data.type === 'float'"
      :disabled="disabled"
      @change="handleChange"
      v-model="data[prop]" :precision="2" :step="0.1"></el-input-number>

     <el-date-picker
       class="custom-with"
       @change="handleChange"
       v-else-if="data.type === 'data'"
       :disabled="disabled"
       v-model="data[prop]"
       type="date"
       :placeholder="$t('commons.select_date')">
    </el-date-picker>

    <el-select v-else-if="data.type === 'member' || data.type === 'multipleMember'"
               :multiple="data.type === 'multipleMember'"
               @change="handleChange"
               :disabled="disabled"
               filterable v-model="data[prop]" :placeholder="$t('commons.default')">
       <el-option
         v-for="(item) in memberOptions"
         :key="item.id"
         :label="item.name + ' (' + item.id + ')'"
         :value="item.id">
       </el-option>
    </el-select>

    <el-input class="custom-with"
              @change="handleChange"
              :disabled="disabled"
              v-else v-model="data[prop]"/>

  </span>

</template>

<script>
import MsTableColumn from "@/business/components/common/components/table/MsTableColumn";
import {getCurrentProjectID, getCurrentWorkspaceId} from "@/common/js/utils";
export default {
  name: "CustomFiledComponent",
  components: {MsTableColumn},
  props: [
    'data',
    'prop',
    'form',
    'disabled'
  ],
  data() {
    return {
      memberOptions: [],
    };
  },
  mounted() {
    if (this.data.type === 'member' || this.data.type === 'multipleMember') {
      this.$post('/user/project/member/tester/list', {projectId: getCurrentProjectID()}, response => {
        this.memberOptions = response.data;
      });
    }
  },
  methods: {
    getTranslateOption(item) {
      return item.system ? this.$t(item.text) : item.text;
    },
    handleChange() {
      if (this.form) {
        this.$set(this.form, this.data.name, this.data[this.prop]);
        this.$emit('reload');
      }
    }
  }
};
</script>

<style scoped>
</style>
