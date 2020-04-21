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
          <el-button size="mini" class="el-icon-delete-solid" circle @click="remove(index)"/>
        </el-col>
      </el-row>
    </div>

    <div class="kv-row">
      <el-row type="flex" :gutter="20" justify="space-between" align="middle">
        <el-col>
          <el-input v-model="kv.key" placeholder="Key" size="small" maxlength="100" @change="add"/>
        </el-col>
        <el-col>
          <el-input v-model="kv.value" placeholder="Value" size="small" maxlength="100" @change="add"/>
        </el-col>
        <el-col class="kv-delete">
          <el-button size="mini" class="el-icon-delete-solid" circle :disabled="true"/>
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

    data() {
      return {
        kv: new KeyValue()
      }
    },

    methods: {
      add: function () {
        if (this.kv.key || this.kv.value) {
          this.items.push(this.kv);
          this.kv = new KeyValue();
        }
      },
      remove: function (index) {
        this.items.splice(index, 1);
      },
      check: function () {
        let removeIndex = -1;
        this.items.forEach((item, index) => {
          if (!item.key && !item.value) {
            removeIndex = index;
          }
        });
        if (removeIndex !== -1) {
          this.remove(removeIndex);
        }
        // TODO 检查key重复
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
