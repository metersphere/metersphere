<template>
  <div
    class="el-input-tag input-tag-wrapper"
    :class="[size ? 'el-input-tag--' + size : '']"
    style="height: auto"
    @click="focusTagInput">
    <el-tag
      v-for="(tag, idx) in tagList"
      v-bind="$attrs"
      type="info"
      :key="tag"
      :size="size"
      :closable="!readOnly"
      :disable-transitions="false"
      @close="remove(idx)">
      <span v-if="tag && tag.length > 10">
        <el-tooltip class="item" effect="light" :content="tag" placement="top" :enterable="false">
          <span>{{ tag && tag.length > 10 ? tag.substring(0, 10) + '...' : tag }}</span>
        </el-tooltip>
      </span>
      <span v-else>
        {{ tag }}
      </span>
    </el-tag>
    <el-tooltip :content="tooltipContent" placement="top">
      <el-tag v-show="showEllipsisTag" type="info" :size="size" :closable="false" :disable-transitions="false">
        {{ `+${ellipsisCount}` }}
      </el-tag>
    </el-tooltip>
    <input
      :disabled="readOnly"
      class="tag-input el-input"
      v-model="newTag"
      :placeholder="defaultPlaceHolder"
      @keydown.delete.stop="removeLastTag"
      @keydown="addNew"
      @blur="addNew" />
  </div>
</template>

<script>
export default {
  name: 'MsInputTag',
  props: {
    currentScenario: {},
    placeholder: {
      type: String,
    },
    errorInfo: String,
    addTagOnKeys: {
      type: Array,
      default: () => [13, 188, 9],
    },
    readOnly: {
      type: Boolean,
      default: false,
    },
    size: { type: String, default: 'small' },
    prop: {
      type: String,
      default: 'tags',
    },
    maxShowTagLength: {
      type: Number,
      default: 6,
    },
  },
  created() {
    if (!this.currentScenario[this.prop]) {
      this.currentScenario[this.prop] = [];
    }
    if (this.placeholder) {
      this.defaultPlaceHolder = this.placeholder;
    }
  },
  data() {
    return {
      defaultPlaceHolder: this.$t('commons.tag_tip'),
      newTag: '',
      innerTags: this.currentScenario[this.prop] ? [...this.currentScenario[this.prop]] : [],
    };
  },
  watch: {
    innerTags() {
      this.currentScenario[this.prop] = this.innerTags;
      this.tagChange();
    },
    'currentScenario.tags'() {
      if (this.prop === 'tags') {
        if (
          !this.currentScenario[this.prop] ||
          this.currentScenario[this.prop] === '' ||
          this.currentScenario[this.prop].length === 0
        ) {
          if (this.innerTags.length !== 0) {
            this.innerTags = [];
          }
        }
      }
    },
  },
  computed: {
    tooltipContent() {
      return this.innerTags.map((item) => item).join(',');
    },
    tagList() {
      return this.innerTags
        .map((item) => item)
        .splice(0, this.maxShowTagLength > this.innerTags.length ? this.innerTags.length : this.maxShowTagLength);
    },
    showEllipsisTag() {
      return this.innerTags.length > this.maxShowTagLength;
    },
    ellipsisCount() {
      return this.innerTags.length - this.maxShowTagLength;
    },
  },
  methods: {
    focusTagInput() {
      if (!this.readOnly && this.$el.querySelector('.tag-input')) {
        this.$el.querySelector('.tag-input').focus();
      }
    },
    addNew(e) {
      if (e && !this.addTagOnKeys.includes(e.keyCode) && e.type !== 'blur') {
        return;
      }
      if (e) {
        e.stopPropagation();
        e.preventDefault();
      }
      let addSuccess = false;
      if (this.newTag.includes(',')) {
        this.newTag.split(',').forEach((item) => {
          if (this.addTag(item.trim())) {
            addSuccess = true;
          }
        });
      } else {
        if (this.addTag(this.newTag.trim())) {
          addSuccess = true;
        }
      }
      if (addSuccess) {
        this.tagChange();
        this.newTag = '';
      }
      this.$emit('onblur');
    },
    addTag(tag) {
      tag = tag.trim();
      if (tag && !this.innerTags.includes(tag)) {
        if (tag.length > 15) {
          this.$error(this.$t('commons.tag_length_tip'));
          return false;
        }
        this.innerTags.push(tag);
        return true;
      } else {
        if (tag !== '' && this.errorInfo) {
          this.$error(this.errorInfo);
        }
      }
      return false;
    },
    remove(index) {
      this.innerTags.splice(index, 1);
      this.tagChange();
      this.$nextTick(() => {
        //删除tag元素操作是在输入框中去掉元素，也应当触发onblur操作
        this.$emit('onblur');
      });
    },
    removeLastTag() {
      if (this.newTag) {
        return;
      }
      this.innerTags.pop();
      this.tagChange();
    },
    tagChange() {
      this.$emit('input', this.innerTags);
    },
  },
};
</script>

<style scoped>
.input-tag-wrapper {
  position: relative;
  font-size: 14px;
  background-color: #fff;
  background-image: none;
  border-radius: 4px;
  border: 1px solid #dcdfe6;
  box-sizing: border-box;
  color: #606266;
  display: inline-block;
  outline: none;
  padding: 0 10px 0 5px;
  transition: border-color 0.2s cubic-bezier(0.645, 0.045, 0.355, 1);
  width: 100%;
}

.el-tag {
  margin-right: 4px;
}

.tag-input {
  background: transparent;
  border: 0;
  color: #303133;
  font-size: 12px;
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', Arial, sans-serif;
  outline: none;
  padding-left: 0;
  width: 100px;
}

.el-input-tag {
  height: 40px;
  line-height: 40px;
}

.el-input-tag--mini {
  height: 28px;
  line-height: 28px;
  font-size: 12px;
}

.el-input-tag--small {
  line-height: 30px;
  width: 800px;
}

.el-input-tag--medium {
  height: 36px;
  line-height: 36px;
}
</style>
