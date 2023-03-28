<template>
  <div class="case-name">
    <div v-if="isAdd || editableState">
      {{ editableState ? $t('test_track.case.edit_case') : $t('test_track.case.create_case') }}
    </div>
    <div :class="isNameEdit ? 'name-input' : 'name-text'" v-else>
      <span v-if="isNameEdit">
         <el-input
           :class="{ 'input-error' : showInputError}"
           size="small"
           :placeholder="$t('case.please_enter_the_case_name')"
           v-model="form.name"
           @blur="handleNameEdit"
           :maxlength="255"
           show-word-limit
         />
      </span>
      <span v-else @click="handleNameClick">
        <el-tooltip :content="form.name" effect="dark" placement="bottom-start">
          <span>
            {{ titleNum }}
            {{ form.name }}
          </span>
        </el-tooltip>
      </span>
    </div>
  </div>
</template>

<script>
import {useStore} from "@/store";

export default {
  name: "TestCaseEditNameView",
  props: {
    form: Object,
    isNameEdit: Boolean,
    editableState: Boolean,
    isAdd: Boolean,
    isPublicShow: Boolean
  },
  data() {
    return {
      showInputError: false
    }
  },
  computed: {
    titleNum() {
      let num = this.isCustomNum ? this.form.customNum : this.form.num;
      if (!num) {
        return '';
      }
      return `【${num}】`;
    },
    isCustomNum() {
      return useStore().currentProjectIsCustomNum;
    }
  },
  methods: {
    handleNameEdit() {
      if (!this.form.name) {
        this.$error(this.$t("test_track.case.input_name"));
        this.showInputError = true;
        return;
      }
      this.showInputError = false;
      this.$emit('update:isNameEdit', !this.isNameEdit);
      this.$emit('save');
    },
    handleNameClick() {
      if (this.isPublicShow) {
        return;
      }
      this.$emit('update:isNameEdit', !this.isNameEdit);
    }
  }
}
</script>

<style scoped>
</style>

<style scoped lang="scss">
@import "@/business/style/index.scss";
.name-input {
  min-width: px2rem(900);
}

.name-text:hover {
  cursor: pointer;
  background: rgba(31, 35, 41, 0.1);
  border-radius: 4px;
}

.name-text {
  width: auto;
  padding: 0.1rem 0.4rem;
}

.input-error :deep(.el-input__inner) {
  border-color: #F56C6C;
}

.case-name {
  height: auto;
  font-size: 16px;
  font-family: "PingFang SC";
  font-style: normal;
  font-weight: 500;
  line-height: px2rem(24);
  color: #1f2329;
  margin-left: px2rem(8);
  margin-right: px2rem(8);
  cursor: pointer;
  /* 文本不会换行显示 */
  white-space: nowrap;
  /* 超出盒子部分隐藏 */
  overflow: hidden;
  /* 文本超出的部分打点显示 */
  text-overflow: ellipsis;
  max-width: 85%;
}

</style>
