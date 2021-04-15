<template>
  <span>
     <el-select v-if="data.type === 'select' || data.type === 'multipleSelect'"
                :multiple="data.type === 'multipleSelect'"
                filterable v-model="data.defaultValue" placeholder="默认值">
      <el-option
        v-for="(item,index) in data.options ? data.options : []"
        :key="index"
        :label="getTranslateOption(item)"
        :value="item.value">
      </el-option>
    </el-select>

    <el-input
      v-else-if="data.type === 'textarea'"
      type="textarea"
      :rows="2"
      placeholder="请输入内容"
      class="custom-with"
      v-model="data.defaultValue">
    </el-input>

    <el-checkbox-group
      v-else-if="data.type === 'checkbox'"
      v-model="data.defaultValue">
      <el-checkbox v-for="(item, index) in data.options ? data.options : []"
                   :key="index"
                   :label="getTranslateOption(item)"></el-checkbox>
    </el-checkbox-group>

    <el-radio
      v-else-if="data.type === 'radio'"
      v-model="data.defaultValue"
      v-for="(item,index) in data.options ? data.options : []"
      :key="index"
      :label="getTranslateOption(item)"></el-radio>

    <el-input-number
      v-else-if="data.type === 'int'"
      v-model="data.defaultValue"
      :min="1" :max="10" label="描述文字"></el-input-number>

    <el-input-number
      v-else-if="data.type === 'float'"
      v-model="data.defaultValue" :precision="2" :step="0.1" :max="10"></el-input-number>

     <el-date-picker
       class="custom-with"
       v-else-if="data.type === 'data'"
       v-model="data.defaultValue"
       type="date"
       placeholder="选择日期">
    </el-date-picker>

    <el-select v-else-if="data.type === 'member' || data.type === 'multipleMember'"
               :multiple="data.type === 'multipleMember'"
               filterable v-model="data.defaultValue" placeholder="默认值">
       <el-option
         v-for="(item) in memberOptions"
         :key="item.id"
         :label="item.name"
         :value="item.id">
       </el-option>
    </el-select>

    <el-input class="custom-with"
              v-else v-model="data.defaultValue"/>

  </span>

</template>

<script>
import MsTableColumn from "@/business/components/common/components/table/Ms-table-column";
import {getCurrentWorkspaceId} from "@/common/js/utils";
export default {
  name: "DefaultValueTableItem",
  components: {MsTableColumn},
  props: [
    'data'
  ],
  data() {
    return {
      memberOptions: []
    };
  },
  mounted() {
    if (this.data.type === 'member' || this.data.type === 'multipleMember') {
      this.$post('/user/ws/member/tester/list', {workspaceId: getCurrentWorkspaceId()}, response => {
        this.memberOptions = response.data;
      });
    }
  },
  methods: {
    getTranslateOption(item) {
      return item.system ? this.$t(item.text) : item.text;
    }
  }
}
</script>

<style scoped>

</style>
