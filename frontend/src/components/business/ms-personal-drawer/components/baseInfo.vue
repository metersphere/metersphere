<template>
  <div class="flex h-full flex-col overflow-hidden">
    <div class="mb-[16px] flex items-center justify-between">
      <div class="font-medium text-[var(--color-text-1)]">{{ t('ms.personal.baseInfo') }}</div>
      <a-button v-if="!isEdit" type="outline" size="mini" class="p-[2px_8px]" @click="isEdit = true">
        {{ t('common.update') }}
      </a-button>
    </div>
    <div class="mb-[16px] flex items-center">
      <MsAvatar :avatar="userStore.avatar || 'default'" :size="58" class="mb-[4px]" />
      <a-button
        type="outline"
        class="arco-btn-outline--secondary ml-[8px] p-[2px_8px]"
        size="mini"
        @click="avatarModalVisible = true"
      >
        {{ t('ms.personal.changeAvatar') }}
      </a-button>
    </div>
    <a-form v-if="isEdit" ref="baseInfoFormRef" :model="baseInfoForm" layout="vertical">
      <a-form-item
        field="name"
        :label="t('ms.personal.name')"
        :rules="[{ required: true, message: t('ms.personal.nameRequired') }]"
        asterisk-position="end"
      >
        <a-input
          v-model:modelValue="baseInfoForm.name"
          :placeholder="t('ms.personal.namePlaceholder')"
          :max-length="255"
        />
      </a-form-item>
      <a-form-item
        field="email"
        :label="t('ms.personal.email')"
        :rules="[{ required: true, message: t('ms.personal.emailRequired') }, { validator: checkUerEmail }]"
        asterisk-position="end"
      >
        <a-input v-model:modelValue="baseInfoForm.email" :placeholder="t('ms.personal.emailPlaceholder')" />
        <MsFormItemSub :text="t('ms.personal.emailTip')" :show-fill-icon="false" />
      </a-form-item>
      <a-form-item
        field="phone"
        :label="t('ms.personal.phone')"
        :rules="[{ required: true, message: t('ms.personal.phoneRequired') }, { validator: checkUerPhone }]"
        asterisk-position="end"
      >
        <a-input
          v-model:modelValue="baseInfoForm.phone"
          :placeholder="t('ms.personal.phonePlaceholder')"
          :max-length="11"
        />
      </a-form-item>
      <a-form-item>
        <a-button type="primary" class="mr-[14px]" :loading="updateLoading" @click="editBaseInfo">
          {{ t('common.update') }}
        </a-button>
        <a-button type="secondary" :disabled="updateLoading" @click="cancelEdit">{{ t('common.cancel') }}</a-button>
      </a-form-item>
    </a-form>
    <div v-else class="h-[calc(100%-118px)]">
      <MsDescription :descriptions="descriptions">
        <template #tag>
          <div v-for="org of orgList" :key="org.orgId" class="mb-[16px]">
            <MsTag class="h-[26px]" max-width="100%">
              <span>{{ org.orgName }}</span>
            </MsTag>
            <br />
            <MsTag
              v-for="project of org.projectList"
              :key="project.projectId"
              class="!mr-[8px] mt-[8px] !bg-[rgb(var(--primary-1))] !text-[rgb(var(--primary-5))]"
              max-width="100%"
            >
              <span>{{ project.projectName }}</span>
            </MsTag>
          </div>
        </template>
      </MsDescription>
    </div>
  </div>
  <a-modal
    v-model:visible="avatarModalVisible"
    :title="t('ms.personal.changeAvatar')"
    title-align="start"
    :ok-text="t('common.save')"
    class="ms-usemodal"
    :width="680"
    @before-ok="handleChangeAvatarConfirm"
  >
    <a-radio-group v-model:model-value="activeAvatarType" type="button">
      <a-radio value="builtIn" class="show-type-icon">{{ t('ms.personal.builtIn') }}</a-radio>
      <a-radio value="word" class="show-type-icon">{{ t('ms.personal.wordAvatar') }}</a-radio>
    </a-radio-group>
    <div v-show="activeAvatarType === 'builtIn'" class="avatar-content">
      <div class="avatar" @click="changeAvatar('default')">
        <MsAvatar avatar="default" class="mb-[4px]" />
        <div class="text-[12px] text-[var(--color-text-1)]">{{ t('ms.personal.default') }}</div>
        <MsIcon
          v-if="activeAvatar === 'default'"
          type="icon-icon_succeed_filled"
          :style="{ color: 'rgb(var(--success-6))' }"
          class="check-icon"
        />
      </div>
      <div v-for="(avatar, index) of avatarList" :key="avatar" class="avatar" @click="changeAvatar(avatar)">
        <MsAvatar :avatar="avatar" class="mb-[4px]" />
        <div class="text-[12px] text-[var(--color-text-1)]">{{ t('ms.personal.avatar', { index: index }) }}</div>
        <MsIcon
          v-if="activeAvatar === avatar"
          type="icon-icon_succeed_filled"
          :style="{ color: 'rgb(var(--success-6))' }"
          class="check-icon"
        />
      </div>
    </div>
    <div v-show="activeAvatarType === 'word'" class="mb-[8px] flex flex-wrap gap-[24px] pt-[14px]">
      <div class="avatar" @click="changeAvatar('word')">
        <MsAvatar avatar="word" class="mb-[4px]" />
        <div class="text-[12px] text-[var(--color-text-1)]">{{ t('ms.personal.wordAvatar') }}</div>
        <MsIcon
          v-if="activeAvatar === 'word'"
          type="icon-icon_succeed_filled"
          :style="{ color: 'rgb(var(--success-6))' }"
          class="check-icon"
        />
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
  import { Message } from '@arco-design/web-vue';

  import MsAvatar from '@/components/pure/ms-avatar/index.vue';
  import MsDescription, { Description } from '@/components/pure/ms-description/index.vue';
  import MsIcon from '@/components/pure/ms-icon-font/index.vue';
  import MsTag from '@/components/pure/ms-tag/ms-tag.vue';
  import MsFormItemSub from '@/components/business/ms-form-item-sub/index.vue';

  import { getBaseInfo, updateBaseInfo } from '@/api/modules/user';
  import { useI18n } from '@/hooks/useI18n';
  import useUserStore from '@/store/modules/user/index';
  import { formatPhoneNumber } from '@/utils';
  import { validateEmail, validatePhone } from '@/utils/validate';

  import { OrganizationProjectListItem } from '@/models/user';

  import type { FormInstance } from '@arco-design/web-vue';

  const userStore = useUserStore();

  const { t } = useI18n();

  const loading = ref(false);
  const isEdit = ref(false);
  const descriptions = ref<Description[]>([]);
  const baseInfoForm = ref({
    name: userStore.name,
    email: userStore.email,
    phone: userStore.phone,
  });
  const baseInfoFormRef = ref<FormInstance>();
  const orgList = ref<OrganizationProjectListItem[]>([]);
  const activeAvatarType = ref<'builtIn' | 'word'>('builtIn');
  const activeAvatar = ref('default');

  function initBaseInfo() {
    descriptions.value = [
      {
        label: t('ms.personal.name'),
        value: userStore.name || '',
      },
      {
        label: t('ms.personal.email'),
        value: userStore.email || '',
      },
      {
        label: t('ms.personal.phone'),
        value: formatPhoneNumber(userStore.phone || ''),
      },
      {
        label: t('ms.personal.org'),
        value: [],
        isTag: true,
      },
    ];
  }

  onBeforeMount(async () => {
    initBaseInfo();
    try {
      loading.value = true;
      const res = await getBaseInfo(userStore.id || '');
      orgList.value = res.orgProjectList;
      activeAvatar.value = res.avatar;
      activeAvatarType.value = res.avatar === 'word' ? 'word' : 'builtIn';
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  });

  const updateLoading = ref(false);

  function cancelEdit() {
    isEdit.value = false;
    baseInfoFormRef.value?.resetFields();
  }

  /**
   * 校验用户邮箱
   * @param value 输入的值
   * @param callback 失败回调，入参是提示信息
   * @param index 当前输入的表单项对应 list 的下标，用于校验重复输入的时候排除自身
   */
  function checkUerEmail(value: string | undefined, callback: (error?: string) => void) {
    if (value === '' || value === undefined) {
      callback(t('system.user.createUserEmailNotNull'));
    } else if (!validateEmail(value)) {
      callback(t('system.user.createUserEmailErr'));
    }
  }

  /**
   * 校验用户手机号
   * @param value 输入的值
   * @param callback 失败回调，入参是提示信息
   */
  function checkUerPhone(value: string | undefined, callback: (error?: string) => void) {
    if (value !== '' && value !== undefined && !validatePhone(value)) {
      callback(t('system.user.createUserPhoneErr'));
    }
  }

  function editBaseInfo() {
    baseInfoFormRef.value?.validate(async (errors) => {
      if (!errors) {
        try {
          updateLoading.value = true;
          await updateBaseInfo({
            id: userStore.id || '',
            username: baseInfoForm.value.name || '',
            email: baseInfoForm.value.email || '',
            phone: baseInfoForm.value.phone || '',
            avatar: userStore.avatar || '',
          });
          Message.success(t('common.updateSuccess'));
          await userStore.isLogin();
          initBaseInfo();
          isEdit.value = false;
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          updateLoading.value = false;
        }
      }
    });
  }

  const avatarModalVisible = ref(false);
  const avatarList = ref<string[]>([]);

  watch(
    () => avatarModalVisible.value,
    (val) => {
      if (val && avatarList.value.length === 0) {
        // 初始化头像列表，避免一开始就加载所有头像
        let i = 1;
        while (i <= 46) {
          avatarList.value.push(`/images/avatar/avatar-${i}.jpg`);
          i++;
        }
      }
    }
  );

  function changeAvatar(avatar: string) {
    activeAvatar.value = avatar;
  }

  async function handleChangeAvatarConfirm(done: (closed: boolean) => void) {
    try {
      await updateBaseInfo({
        id: userStore.id || '',
        username: baseInfoForm.value.name || '',
        email: baseInfoForm.value.email || '',
        phone: baseInfoForm.value.phone || '',
        avatar: activeAvatar.value,
      });
      Message.success(t('common.updateSuccess'));
      await userStore.isLogin();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      done(false);
    } finally {
      done(true);
    }
  }
</script>

<style lang="less" scoped>
  .avatar-content {
    @apply grid items-center justify-center;

    grid-template-columns: repeat(auto-fill, minmax(42px, 1fr));
    margin-bottom: 8px;
    gap: 24px;
    padding-top: 14px;
  }
  .avatar {
    @apply relative  flex cursor-pointer flex-col items-center justify-center;

    margin-bottom: 16px;
  }
  .check-icon {
    @apply absolute right-0 rounded-full;

    bottom: 22px;
    background-color: var(--color-text-fff);
  }
  :deep(.ms-description-item-value) {
    -webkit-line-clamp: unset !important;
  }
</style>
