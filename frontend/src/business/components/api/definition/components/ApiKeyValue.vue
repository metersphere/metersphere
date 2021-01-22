<template>
  <div>
    <span class="kv-description" v-if="description">
      {{ description }}
    </span>
    <ms-draggable element="ul" @update="endChange"
                  v-model="keyValues" v-bind="{draggable:'.item'}">
      <div class="kv-row item" v-for="(item, index) in keyValues" :key="index">
        <el-row type="flex" :gutter="20" justify="space-between" align="middle">
          <el-button icon="el-icon-sort" circle size="mini"/>

          <el-col class="kv-checkbox" v-if="isShowEnable">
            <input type="checkbox" v-if="!isDisable(index)" v-model="item.enable"
                   :disabled="isReadOnly"/>
          </el-col>

          <el-col class="item">
            <el-input v-if="!suggestions" :disabled="isReadOnly" v-model="item.name" size="small" maxlength="200"
                      @change="change"
                      :placeholder="keyText" show-word-limit/>
            <el-autocomplete :disabled="isReadOnly" :maxlength="200" v-if="suggestions" v-model="item.name" size="small"
                             :fetch-suggestions="querySearch" @change="change" :placeholder="keyText"
                             show-word-limit/>

          </el-col>

          <el-col class="item">
            <el-input :disabled="isReadOnly" v-model="item.value" size="small" @change="change"
                      :placeholder="valueText" show-word-limit/>
          </el-col>
          <el-col class="item kv-delete">
            <el-button size="mini" class="el-icon-delete-solid" circle @click="remove(index)"
                       :disabled="isDisable(index) || isReadOnly"/>
          </el-col>
        </el-row>
      </div>
    </ms-draggable>
  </div>
</template>

<script>
  import {KeyValue} from "../model/ApiTestModel";
  import MsDraggable from 'vuedraggable'

  export default {
    name: "MsApiKeyValue",
    components: {
      MsDraggable
    },

    props: {
      keyPlaceholder: String,
      valuePlaceholder: String,
      isShowEnable: {
        type: Boolean,
        default: false
      },
      description: String,
      items: Array,
      isReadOnly: {
        type: Boolean,
        default: false
      },
      suggestions: Array,
    },
    data() {
      return {
        keyValues: [],
      }
    },
    computed: {
      keyText() {
        return this.keyPlaceholder || this.$t("api_test.key");
      },
      valueText() {
        return this.valuePlaceholder || this.$t("api_test.value");
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
          this.items.push(new KeyValue({enable: true}));
        }
        this.$emit('change', this.items);
        // TODO 检查key重复
      },
      isDisable: function (index) {
        return this.items.length - 1 === index;
      },
      querySearch(queryString, cb) {
        let suggestions = this.suggestions;
        let results = queryString ? suggestions.filter(this.createFilter(queryString)) : suggestions;
        cb(results);
      },
      createFilter(queryString) {
        return (restaurant) => {
          return (restaurant.value.toLowerCase().indexOf(queryString.toLowerCase()) === 0);
        };
      },
      endChange(env) {
        if (env.newIndex == env.oldIndex) {
          return;
        }
        let newItem = this.keyValues[env.newIndex];
        let oldItem = this.keyValues[env.oldIndex];
        this.$set(this.keyValues, env.oldIndex, oldItem);
        this.$set(this.keyValues, env.newIndex, newItem)
        this.items.forEach(item => {
          this.items.splice(0);
        })
        this.keyValues.forEach(item => {
          this.items.push(item);
        })
      }
    },
    created() {
      if (this.items.length === 0 || this.items[this.items.length - 1].name) {
        this.items.push(new KeyValue({enable: true}));
      }
      this.keyValues = this.items;
    }
  }
</script>

<style scoped>
  .kv-description {
    font-size: 13px;
  }

  .kv-row {
    margin-top: 10px;
  }

  .kv-checkbox {
    width: 20px;
    margin-right: 10px;
  }

  .kv-delete {
    width: 60px;
  }

  .el-autocomplete {
    width: 100%;
  }
</style>
