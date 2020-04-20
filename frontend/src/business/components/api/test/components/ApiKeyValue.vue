<template>
  <div>
    <span class="kv-description" v-if="description">
      {{description}}
    </span>
    <div class="kv-row" v-for="(item, index) in items" :key="index">
      <el-row type="flex" :gutter="20" justify="space-between" align="middle">
        <el-col>
          <el-input v-model="item.key" placeholder="Key" size="small" maxlength="100" @change="check"/>
        </el-col>
        <el-col>
          <el-input v-model="item.value" placeholder="Value" size="small" maxlength="100" @change="check"/>
        </el-col>
        <el-col class="kv-delete">
          <el-button size="mini" class="el-icon-delete-solid" circle @click="remove(index)"
                     :disabled="isDisable(index)"/>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
  import {KeyValue} from "../model/ScenarioModel";

  export default {
    name: "MsApiKeyValue",

    props: {
      description: String,
      items: Array
    },

    methods: {
      add: function () {
        this.items.push(new KeyValue());
      },
      remove: function (index) {
        this.items.splice(index, 1);
        if (this.items.length === 0) {
          this.add();
        }
      },
      check: function () {
        let isNeedCreate = true;
        let removeIndex = -1;
        this.items.forEach((item, index) => {
          if (!item.key && !item.value) {
            // 多余的空行
            if (index !== this.items.length - 1) {
              removeIndex = index;
            }
            // 没有空行，需要创建空行
            isNeedCreate = false;
          }
        });
        if (isNeedCreate) {
          this.add();
        }
        if (removeIndex !== -1) {
          this.remove(removeIndex);
        }
        // TODO 检查key重复
      },
      isDisable: function (index) {
        return this.items.length - 1 === index;
      }
    },

    created() {
      if (this.items.length === 0) {
        this.add();
      }
    }
  }
</script>

<style scoped>
  .kv-description {
    font-size: 14px;
  }

  .kv-row {
    margin-top: 10px;
  }

  .kv-delete {
    width: 60px;
  }
</style>
