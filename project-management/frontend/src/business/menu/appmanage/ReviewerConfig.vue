<template>
  <app-manage-item
    :title="name"
    :description="popTitle"
    :append-span="3"
    :middle-span="12"
    :prepend-span="9"
  >
    <template #prepend>
      <span style="margin-left: 10px; line-height: 35px">{{ name }}</span>
      <el-tooltip
        v-show="popTitle"
        class="item"
        effect="dark"
        :content="popTitle"
        placement="right-start"
      >
        <i class="el-icon-info" />
      </el-tooltip>
    </template>
    <template #middle>
      <span>{{ $t("pj.reviewers") }}</span>
      <el-select
        v-model="config.performanceScriptReviewer"
        @change="reviewerChange"
        size="mini"
        style="margin-left: 5px"
        filterable
        :placeholder="$t('api_test.definition.api_principal')"
      >
        <el-option
          v-for="item in reviewers"
          :key="item.id"
          :value="item.id"
          :label="item.name"
        >
        </el-option>
      </el-select>
    </template>
    <template #append>
      <el-switch
        v-model="config.performanceReviewLoadTestScript"
        @change="switchChange"
      ></el-switch>
    </template>
  </app-manage-item>
</template>

<script>
import AppManageItem from "@/business/menu/appmanage/AppManageItem.vue";

export default {
  name: "ReviewerConfig",
  components: { AppManageItem },
  props: {
    name: String,
    popTitle: String,
    reviewers: Array,
    config: Object,
  },
  setup() {
    return {};
  },
  data() {
    return {};
  },
  computed: {},
  watch: {},
  created() {},
  mounted() {},
  methods: {
    switchChange() {
      this.$emit("chooseChange");
    },
    reviewerChange() {
      this.$emit("reviewerChange");
    },
  },
};
</script>

<style scoped></style>
