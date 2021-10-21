<template>
  <div v-loading="loading">
    <div class="kv-row item" v-for="(item, index) in data" :key="index">
      <el-row type="flex" :gutter="20" justify="space-between" align="middle" :style="item.style">
        <div class="box" v-if="item.box"/>
        <el-col class="kv-checkbox" v-if="isShowEnable">
          <el-checkbox v-if="!isDisable(index)" v-model="item.enable" :disabled="isReadOnly"/>
        </el-col>
        <span style="margin-left: 10px" v-else/>
        <el-col class="item">
          <input class="el-input el-input__inner" v-if="!suggestions" :disabled="isReadOnly" v-model="item.name" size="small" maxlength="200" show-word-limit :style="item.style"/>
          <el-autocomplete :disabled="isReadOnly" :maxlength="400" v-if="suggestions" v-model="item.name" size="small" show-word-limit :style="item.style"/>
        </el-col>
        <el-col v-if="showRequired">
          <input class="el-input el-input__inner" :disabled="isReadOnly" v-model="item.required" size="small" :style="item.style"/>
        </el-col>
        <el-col class="item">
          <input class="el-input el-input__inner" :disabled="isReadOnly" v-model="item.value" size="small" show-word-limit :style="item.style"/>
        </el-col>
        <el-col class="item" v-if="showDesc">
          <input class="el-input el-input__inner" v-model="item.description" size="small" maxlength="200"
                 :style="item.style"
                 :placeholder="$t('commons.description')" show-word-limit/>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script>
const background_new = "background:#F3E6E7;";
const background_old = "background:#E2ECDC";
export default {
  name: "MsApiKeyValueDetail",
  components: {},
  props: {
    keyPlaceholder: String,
    valuePlaceholder: String,
    isShowEnable: {
      type: Boolean,
    },
    description: String,
    items: Array,
    isReadOnly: {
      type: Boolean,
      default: false
    },
    suggestions: Array,
    needMock: {
      type: Boolean,
      default: false
    },
    showDesc: Boolean,
    showRequired: {
      type: Boolean,
      default: false,
    },
    format: String,
  },
  data() {
    return {
      keyValues: [],
      loading: false,
      currentItem: {},
      isSelectAll: true,
      data: [],
    }
  },
  watch: {
    isSelectAll: function (to, from) {
      if (from == false && to == true) {
        this.selectAll();
      } else if (from == true && to == false) {
        this.invertSelect();
      }
    },
    format: function (to, from) {
      this.formatItem();
      this.reload();
    }
  },
  methods: {
    reload() {
      this.loading = true
      this.$nextTick(() => {
        this.loading = false
      })
    },
    isDisable: function (index) {
      return this.items.length - 1 === index;
    },

    selectAll() {
      this.items.forEach(item => {
        item.enable = true;
      });
    },
    invertSelect() {
      this.items.forEach(item => {
        item.enable = false;
      });
    },
    formatItem() {
      this.data = [];
      if (this.items && this.items.length > 0) {
        for (let i in this.items) {
          let item = this.items[i];
          item.required = item.required ? this.$t('commons.selector.required') : this.$t('commons.selector.not_required');
          let itemMap = new Map(Object.entries(item));
          let newObj = new Map()
          let itemStr = JSON.stringify(item);
          if (itemStr.indexOf("--name") !== -1) {
            itemStr = itemStr.replaceAll("--", "");
            let obj = JSON.parse(itemStr);
            obj.style = background_new;
            obj.box = true;
            this.data.push(obj);
          } else if (itemStr.indexOf("++name") !== -1) {
            itemStr = itemStr.replaceAll("++", "");
            let obj = JSON.parse(itemStr);
            obj.style = background_old;
            this.data.push(obj);
          } else if (itemStr.indexOf("**") !== -1) {
            itemMap.forEach(function (value, key) {
              if (key && key.indexOf("**") !== -1) {
                item.style = item.style ? item.style : "";
                newObj[key.substr(2)] = value;
              } else {
                item.style = item.style ? item.style : "";
                newObj[key] = value;
              }
            });
            item.style = background_old;
            this.data.push(item);
            newObj["box"] = true;
            newObj["style"] = background_new;
            newObj["required"] = newObj.required ? this.$t('commons.selector.required') : this.$t('commons.selector.not_required');
            this.data.push(newObj);
          } else {
            this.data.push(item);
          }
        }
      }
    }
  },
  created() {
    this.formatItem();
  }
}
</script>

<style scoped>
.el-input {
  margin: 0px;
  height: 32px;
}

.kv-row {
  margin-top: 10px;
}

.kv-checkbox {
  width: 20px;
  margin-right: 10px;
}

.box {
  position: absolute;
  width: 100%;
  height: 1px;
  top: 16px;
  z-index: 999;
  border-color: red;
  background: red;
}

i:hover {
  color: #783887;
}
</style>
