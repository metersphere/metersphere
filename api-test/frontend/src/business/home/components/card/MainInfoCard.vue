<template>
  <div class="main-info-card">
    <div style="margin: 16px">
      <span class="main-info-card-title">
        {{ title }}
      </span>
      <div style="margin-top: 4px; height: 40px">
        <span v-if="isExecuteInfo" class="addition-num">{{ formatAmount(countData.executedTimesInWeek) }}</span>
        <span v-else class="main-num">{{ formatAmount(countData.total) }}</span>
      </div>
      <div style="margin-top: 32px">
        <div v-if="isExecuteInfo">
          <span class="main-info-card-title">{{ $t('home.dashboard.public.executed_times') }}</span>
          <div class="common-amount">
            <span class="addition-num">
              {{ formatAmount(countData.executedTimes) }}
            </span>
          </div>
        </div>
        <div v-else>
          <el-row>
            <el-col :span="12">
              <span class="main-info-card-title">{{ $t('home.dashboard.public.this_week') }}</span>
              <div class="common-amount">
                <el-button
                  class="common-amount-button"
                  v-permission-disable="linkPermission"
                  @click="redirect('createdInWeek')">
                  +{{ formatAmount(countData.createdInWeek) }}
                  <img class="main-info-card-right" src="/assets/figma/icon_right_outlined.svg" alt="" />
                </el-button>
              </div>
            </el-col>
            <el-col :span="12">
              <span class="main-info-card-title">{{ $t('home.dashboard.public.fake_error') }}</span>
              <div class="common-amount">
                <el-button
                  class="common-amount-button"
                  v-permission-disable="linkPermission"
                  @click="redirect('fakeError')">
                  {{ formatAmount(countData.fakeErrorCount) }}
                  <img class="main-info-card-right" src="/assets/figma/icon_right_outlined.svg" alt="" />
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
import { formatNumber } from '@/api/home';

export default {
  name: 'MainInfoCard',
  props: {
    title: String,
    isExecuteInfo: Boolean,
    countData: Object,
    redirectPageName: String,
    redirectDataType: String,
    linkPermission: [],
  },
  methods: {
    redirect(seletDataType) {
      this.$emit('redirectPage', this.redirectPageName, this.redirectDataType, seletDataType, null);
    },
    formatAmount(param) {
      return formatNumber(param);
    },
  },
};
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
  color: #1f2329;
}

.main-num {
  font-size: 32px;
  font-weight: 500;
  line-height: 40px;
  color: #1f2329;
}

.common-amount-button {
  padding: 0 4px 0 4px;
  border: 0;
  margin: 0 -4px 0 -4px;
  font-size: 20px;
  font-weight: 500;
  line-height: 28px;
  color: #1f2329;
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

.common-amount-button:focus {
  color: #783887;
}

.common-amount-button:hover {
  color: #783887;
}
</style>
