<template>
  <div>
    <div class="kv-row" v-for="(item, index) in items" :key="index">
      <el-row type="flex" :gutter="20" style="margin-top: 7px;">
        <el-col :span="7">
          <!-- todo 过滤中文 -->
          <el-input :disabled="isReadOnly" v-model="item.name" size="small" maxlength="200"
                    @change="change"
                    :placeholder="'参数名称'" show-word-limit/>
        </el-col>

        <el-col :span="7">
          <el-input :disabled="isReadOnly" v-model="item.value" size="small" @change="change"
                    :placeholder="'参数值'" show-word-limit/>
        </el-col>

        <el-col :span="7">
          <el-input :disabled="isReadOnly" v-model="item.remark" size="small" @change="change"
                    :placeholder="'备注'" show-word-limit/>
        </el-col>

        <el-col class="kv-delete" :span="3">
          <el-button size="mini" class="el-icon-delete-solid" circle @click="remove(index)"
                     :disabled="isDisable(index) || isReadOnly"/>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>


export default {
  name: "FunctionParams",
  props: {
    items: {
      type: Array,
      default() {
        return [];
      }
    },
    isReadOnly: {
      type: Boolean,
      default: false
    },
  },
  data() {
    return {

    }
  },
  methods: {
    remove: function (index) {
      // 移除整行输入控件及内容
      this.items.splice(index, 1);
      this.$emit('change', this.items);
    },
    change: function () {
      let isNeedCreate = true;
      let removeIndex = -1;
      this.items.forEach((item, index) => {
        if (!item.name && !item.value) {
          // 多余的空行
          if (index !== this.items.length - 1) {
            removeIndex = index;
          }
          // 没有空行，需要创建空行
          isNeedCreate = false;
        }
      });
      if (isNeedCreate) {
        this.items.push({});
      }
      this.$emit('change', this.items);
      // TODO 检查key重复
    },
    isDisable: function (index) {
      return this.items.length - 1 === index;
    },
  },
  created() {
    if (this.items.length === 0 || this.items[this.items.length - 1].name) {
      this.items.push({});
    }
  }
}
</script>

<style scoped>

</style>
