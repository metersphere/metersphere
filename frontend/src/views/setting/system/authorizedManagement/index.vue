<template>
  <MsCard :loading="loading" simple>
    <div class="wrapper">
      <div class="content-wrapper">
        <div class="authorized_logo">
          <div>
            <svg-icon width="380px" height="78px" name="authorized_logo" />
          </div>
        </div>
        <div class="ms-info-wrapper">
          <ul class="info">
            <li>
              <span>{{ t('system.authorized.customerName') }}</span>
              <div>
                <span>{{ licenseInfo?.license?.corporation }}</span>
              </div>
            </li>
            <li>
              <span>{{ t('system.authorized.authorizationTime') }}</span>
              <div>
                <span>{{ licenseInfo?.license?.expired }}</span>
              </div>
            </li>
            <li>
              <span>{{ t('system.authorized.productName') }}</span>
              <div>
                <span>{{ licenseInfo?.license?.product }}</span>
              </div>
            </li>
            <li>
              <span>{{ t('system.authorized.productionVersion') }}</span>
              <div>
                <span>{{ licenseInfo?.license?.edition }}</span>
              </div>
            </li>
            <li>
              <span>{{ t('system.authorized.authorizedVersion') }}</span>
              <div>
                <span> v3 </span>
              </div>
            </li>
            <li>
              <span>{{ t('system.authorized.authorizationsCount') }}</span>
              <div>
                <span>{{ addCommasToNumber(licenseInfo?.license?.count || 0) }}</span>
              </div>
            </li>
            <li>
              <span>{{ t('system.authorized.authorizationStatus') }}</span>
              <div>
                <span
                  >{{
                    licenseInfo?.status === 'valid'
                      ? t('system.authorized.valid')
                      : licenseInfo?.status === 'expired'
                      ? t('system.authorized.invalid')
                      : t('system.authorized.failure')
                  }}
                </span>
              </div>
            </li>
            <li>
              <MsButton v-permission="['SYSTEM_AUTH:READ+UPDATE']" class="font-medium" @click="authChecking">
                {{ t('system.authorized.authorityUpdate') }}
              </MsButton>
            </li>
          </ul>
        </div>
      </div>
    </div>
    <MsDrawer
      v-model:visible="authDrawer"
      :title="t('system.authorized.authorityChecking')"
      :ok-text="t('common.update')"
      :ok-loading="drawerLoading"
      :width="680"
      @confirm="confirmHandler"
      @cancel="cancelHandler"
    >
      <a-form ref="authFormRef" :model="authorizedForm" layout="vertical">
        <a-form-item
          :label="t('system.authorized.license')"
          field="licenseCode"
          asterisk-position="end"
          required
          :validate-trigger="['input']"
          :rules="[{ required: true, message: t('system.authorized.LicenseIsRequired') }]"
        >
          <MsUpload
            v-model:file-list="fileList"
            accept="none"
            :is-limit="false"
            :show-sub-text="false"
            :show-file-list="false"
            :auto-upload="false"
            class="w-full"
          />
          <a-textarea
            v-model="authorizedForm.licenseCode"
            class="mt-4"
            :placeholder="t('system.authorized.licenseCode')"
            :auto-size="{
              minRows: 3,
            }"
            :rules="[{ required: true, message: t('system.authorized.LicenseIsRequired') }]"
            :max-length="1000"
          ></a-textarea>
        </a-form-item>
      </a-form>
    </MsDrawer>
  </MsCard>
</template>

<script setup lang="ts">
  /**
   * @description 系统管理-系统-授权管理
   */
  import { onBeforeMount, reactive, ref, watch } from 'vue';
  import { FormInstance, Message, ValidatedError } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsUpload from '@/components/pure/ms-upload/index.vue';

  import { addLicense, getLicenseInfo } from '@/api/modules/setting/authorizedManagement';
  import { useI18n } from '@/hooks/useI18n';
  import useLicenseStore from '@/store/modules/setting/license';
  import { addCommasToNumber } from '@/utils';

  import type { LicenseInfo } from '@/models/setting/authorizedManagement';

  const { t } = useI18n();
  const licenseStore = useLicenseStore();

  const loading = ref<boolean>(false);
  const licenseInfo = ref<LicenseInfo>();
  const authorizedForm = reactive<any>({
    licenseCode: '',
  });

  // 获取License信息
  const getLicenseDetail = async () => {
    loading.value = true;
    try {
      const result = await getLicenseInfo();
      licenseInfo.value = result;
      licenseStore.setLicenseInfo(licenseInfo.value);
      licenseStore.getExpirationTime(licenseInfo.value.license.expired);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      loading.value = false;
    }
  };

  const authDrawer = ref<boolean>(false);
  const drawerLoading = ref<boolean>(false);
  const authFormRef = ref<FormInstance | null>(null);
  const fileList = ref([]);

  const cancelHandler = () => {
    authDrawer.value = false;
    fileList.value = [];
    authFormRef.value?.resetFields();
  };

  const confirmHandler = () => {
    authFormRef.value?.validate(async (errors: undefined | Record<string, ValidatedError>) => {
      if (!errors) {
        drawerLoading.value = true;
        try {
          await addLicense(authorizedForm.licenseCode);
          authDrawer.value = false;
          Message.success(t('system.authorized.licenseSuccessTip'));
          getLicenseDetail();
          cancelHandler();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          drawerLoading.value = false;
        }
      } else {
        return false;
      }
    });
  };

  const authChecking = () => {
    authDrawer.value = true;
  };

  const handlePreview = () => {
    const reader = new FileReader();
    if (typeof FileReader === 'undefined') {
      Message.warning(t('system.plugin.uploadFileTip'));
    }
    reader.readAsText((fileList.value[0] as any)?.file, 'UTF-8');
    reader.onload = (e) => {
      authorizedForm.licenseCode = e.target?.result;
      authFormRef.value?.validate();
    };
  };

  watch(
    () => fileList.value,
    (val) => {
      if (val.length) {
        handlePreview();
      }
    }
  );

  // watch(
  //   () => authorizedForm.value.licenseCode,
  //   (val) => {
  //     if (val.trim().length) {
  //       authFormRef.value?.validate();
  //     }
  //   }
  // );

  onBeforeMount(() => {
    getLicenseDetail();
  });
</script>

<style scoped lang="less">
  .wrapper {
    width: 100%;
    height: calc(100vh - 138px);
    @apply flex items-center justify-center;
  }
  .content-wrapper {
    width: 888px;
    min-height: 400px;
    box-shadow: 0 0 10px rgb(120 56 135 / 10%);
    .authorized_logo {
      width: 888px;
      height: 181px;
      background: url('@/assets/images/authorized_bg.png');
      background-size: cover;
      @apply flex items-center justify-center;
    }
    .ms-info-wrapper {
      width: 100%;
      .info {
        margin: auto;
        padding: 42px;
        width: 532px;
        height: 360px;
        @apply flex flex-col justify-between;
        li {
          @apply flex justify-between;
          div {
            width: 100px;
            span {
              font-weight: 500;
            }
          }
        }
      }
      @apply flex flex-col justify-between;
    }
  }
</style>
