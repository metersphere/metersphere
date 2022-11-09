<template>
  <div class="main-info-card">
    <div style="margin:16px">
      <span class="main-info-card-title">
        {{ title }}
      </span>
      <div style="margin-top: 4px;height: 40px">
        <span v-if="isExecuteInfo" class="addition-num">{{ countData.executedTimesInWeek }}</span>
        <span v-else class="main-num">{{ countData.total }}</span>
      </div>
      <div style="margin-top: 32px">
        <div v-if="isExecuteInfo">
          <span class="main-info-card-title">{{ $t("home.dashboard.public.executed_times") }}</span>
          <div class="common-amount">
            <span class="addition-num">
              {{ countData.executedCount }}
            </span>
          </div>
        </div>
        <div v-else>
          <el-row>
            <el-col :span="12">
              <span class="main-info-card-title">{{ $t("home.dashboard.public.this_week") }}</span>
              <div class="common-amount">
                <el-button class="common-amount-button" @click="redirect('createdInWeek')">
                  <span type="num" class="addition-num">
                    +{{ countData.createdInWeek }}
                  </span>
                  <img class="main-info-card-right" src="/assets/figma/icon_right_outlined.svg" alt="">
                </el-button>
              </div>
            </el-col>
            <el-col :span="12">
              <span class="main-info-card-title">{{ $t("home.dashboard.public.fake_error") }}</span>
              <div class="common-amount">
                <el-button class="common-amount-button" @click="redirect('fakeError')">
                <span class="addition-num">
                  {{ countData.fakeErrorCount }}
                </span>
                  <img class="main-info-card-right" src="/assets/figma/icon_right_outlined.svg" alt="">
                </el-button>
              </div>
            </el-col>
          </el-row>
        </div>
      </div>

    </div>
  </div>
</template>

<script>
export default {
  name: "MainInfoCard",
  props: {
    title: String,
    isExecuteInfo: Boolean,
    countData: Object,
    redirectPageName: String,
    redirectDataType: String,
  },
  methods: {
    redirect(seletDataType) {
      this.$emit('redirectPage', this.redirectPageName, this.redirectDataType, seletDataType, null);
    }
  }
}
</script>

<style scoped>
.main-info-card {
  font-size: 14px;
  font-weight: 400;
  line-height: 22px;
}

.addition-num {
  font-size: 20px;
  font-weight: 500;
  line-height: 28px;
}

.main-num {
  font-size: 32px;
  font-weight: 500;
  line-height: 40px;
}

.common-amount-button {
  padding: 0 4px 0 4px;
  border: 0;
  margin: 0 -4px 0 -8px;
}

.common-amount-button :deep(.addition-num) {
  margin: 0 -4px 0 4px;
}

.main-info-card-right {
  height: 12px;
  width: 12px;
}

.common-amount-button:focus img {
  transform: translateY(-999999px);
  filter: drop-shadow(#783887 0px 999999px);
}

.common-amount-button:hover img {
  transform: translateY(-999999px);
  filter: drop-shadow(#783887 0px 999999px);
}

</style>
