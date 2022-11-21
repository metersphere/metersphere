<template>
  <div v-if="isShow">
    <el-button-group v-if="isShowChangeButton">
      <el-button plain class="ms-relevance-api-btn" :class="{ active: isApiListEnable }" @click="apiChange('api')">
        API
      </el-button>
      <el-button plain class="ms-relevance-case-btn" :class="{ active: !isApiListEnable }" @click="caseChange('case')">
        CASE
      </el-button>
    </el-button-group>
    <slot name="version"></slot>
    <slot></slot>
  </div>
</template>

<script>
export default {
  name: 'ApiListContainer',
  data() {
    return {
      isShow: true,
    };
  },
  props: {
    isApiListEnable: Boolean,
    isShowChangeButton: {
      type: Boolean,
      default: true,
    },
  },
  methods: {
    apiChange() {
      this.$emit('isApiListEnableChange', true);
    },
    caseChange() {
      this.$emit('isApiListEnableChange', false);
    },
  },
};
</script>

<style scoped>
.active {
  border: solid 1px #6d317c;
  background-color: #7c3985;
  color: #ffffff;
}

.ms-relevance-api-btn {
  width: 44px;
  height: 32px;
  padding: 5px 8px;
}

.ms-relevance-case-btn {
  width: 44px;
  height: 32px;
  padding: 1px;
}
</style>
