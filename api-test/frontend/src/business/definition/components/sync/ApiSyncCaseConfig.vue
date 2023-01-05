<template>
  <el-dialog
    width="600px"
    :visible.sync="batchSyncApiVisible" :close-on-click-modal="false"
    :title="$t('commons.save') + '&' + $t('workstation.sync') + $t('commons.setting')"
    v-if="isXpack">
    <el-row class="box-class" style="margin-bottom: 16px;">
      <div class="time-class">
        <span>{{ $t('api_test.definition.one_click_sync') + 'case' }}</span>
        <el-switch v-model="apiSyncRuleRelation.syncCase" style="float:right;"></el-switch>
      </div>
      <span class="tip-class">{{ $t('workstation.batch_sync_api_tips') }}</span>

      <div v-if="apiSyncRuleRelation.syncCase">
        <div style="border-top: solid rgba(31, 35, 41, 0.15) 1px; margin: 16px 0;"></div>
        <span class="time-class">
          {{ $t('workstation.sync') + $t('commons.setting') }}
        </span>
        <sync-setting
          style="margin-top: 16px"
          v-bind:sync-data="apiSyncRuleRelation.apiSyncConfig"
          ref="synSetting"
          @updateSyncData="updateSyncData"></sync-setting>
      </div>
    </el-row>
    <el-row class="box-class" style="margin-bottom: 10px;">
      <div class="time-class">
        <div style="display: flex">
          <span>
            {{ $t('api_test.definition.change_notification') }}
          </span>
          <el-tooltip
            class="ms-num"
            effect="dark"
            :content="$t('project_application.workstation.api_receiver_tip')"
            placement="top">
            <svg-icon iconClass="question" class-name="ms-menu-img"
                      :style="{color:'#8F959E',margin: '4px 0 0 0'}"></svg-icon>
          </el-tooltip>
        </div>
        <el-switch v-model="apiSyncRuleRelation.sendNotice" style="float:right;"></el-switch>
      </div>
      <span class="tip-class"> {{ $t('api_test.definition.recipient_tips') }} </span>
      <span
        class="tip-class" style="color: var(--primary_color);cursor: pointer;" @click="gotoApiMessage">
        {{ $t('project_application.workstation.go_to_api_message') }}
      </span>
      <el-row v-if="apiSyncRuleRelation.sendNotice">
        <div style="border-top: solid rgba(31, 35, 41, 0.15) 1px; margin: 16px 0;"></div>
        <span class="text-class">{{ $t('api_test.definition.recipient') }}</span>
        <div style="color: var(--primary_color);margin-top: 8px;">
          <el-checkbox v-model="apiSyncRuleRelation.caseCreator"><span
            class="text-class">{{ 'CASE' + $t('api_test.creator') }}</span></el-checkbox>
          <el-checkbox v-model="apiSyncRuleRelation.scenarioCreator">
            <span class="text-class">{{ $t('commons.scenario') + $t('api_test.creator') }}</span>
          </el-checkbox>
        </div>
      </el-row>
    </el-row>
    <span slot="footer" class="dialog-footer">
      <div class="bottom-class">
        <div class="close-class">
          <el-checkbox v-model="apiSyncRuleRelation.showUpdateRule" style="color: #8F959E;width: 16px;height: 16px">
          </el-checkbox>
          <span class="text-class" style="margin-left: 8px;margin-right: 5px;">
              {{ $t('project_application.workstation.no_show_setting') }}
            </span>
            <el-tooltip
              class="ms-num"
              effect="dark"
              :content="$t('project_application.workstation.no_show_setting_tip')"
              placement="top">
              <svg-icon iconClass="question" class-name="ms-menu-img"
                        :style="{color:'#8F959E',margin: '2px 0 0 0'}"></svg-icon>
            </el-tooltip>
        </div>
        <div>
          <el-button @click="batchSyncApiVisible = false">{{ $t('commons.cancel') }}</el-button>
          <el-button type="primary" @click="batchSync()">{{ $t('commons.confirm') }}</el-button>
        </div>
      </div>
    </span>
  </el-dialog>
</template>

<script>
import SyncSetting from "@/business/definition/util/SyncSetting";

export default {
  name: "ApiSyncCaseConfig",
  components: {
    SyncSetting,
  },
  props: {
    apiSyncRuleRelation: {},
    showApiSyncConfig: {
      type: Boolean,
      default: false,
    },
    isXpack: {
      type: Boolean,
      default: false,
    }
  },
  data() {
    return {
      batchSyncApiVisible: false,
    }
  },
  methods: {
    updateSyncData(value) {
      this.apiSyncRuleRelation.apiSyncConfig = value;
    },
    gotoApiMessage() {
      let apiResolve = this.$router.resolve({
        path: '/project/messagesettings',
      });
      window.open(apiResolve.href, '_blank');
    },
    batchSync() {
      let fromData;
      if (this.$refs.synSetting && this.$refs.synSetting.fromData) {
        fromData = this.$refs.synSetting.fromData;
        fromData.method = true;
        fromData.path = true;
        fromData.protocol = true;
      }
      this.$emit('batchSync', fromData)
    },
    show() {
      this.batchSyncApiVisible = true
    },
    close() {
      this.batchSyncApiVisible = false
    }
  },
}
</script>

<style scoped>
.box-class {
  background: #FFFFFF;
  border: 1px solid #DEE0E3;
  border-radius: 4px;
  width: 552px;
  left: 4px;
  top: -16px;
  padding: 16px;
}

:deep(.el-dialog__title) {
  font-size: 16px;
  font-weight: 500;
  /* left: 4px; */
  margin-left: 4px;
  color: #1F2329;
  font-style: normal;
  line-height: 24px;
}

.time-class {
  font-style: normal;
  font-weight: 500;
  font-size: 14px;
  line-height: 22px;
  color: #1F2329;
  margin-bottom: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tip-class {
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #646A73;
}

.bottom-class {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: -40px 4px 4px;
}

.text-class {
  font-style: normal;
  font-weight: 400;
  font-size: 14px;
  line-height: 22px;
  color: #1F2329;
}

.close-class {
  display: flex;
  align-items: center;
}

</style>
<style lang="scss" scoped>
.ms-menu-img {
  width: 15px;
  height: 15px;
  border: 0;
  display: inline-block;
  box-sizing: border-box;
  background-repeat: no-repeat;
  background-position: 50% center;
}
</style>
