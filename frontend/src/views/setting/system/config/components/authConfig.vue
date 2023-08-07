<template>
  <div>
    <MsCard :loading="loading" simple>
      <div class="mb-4 flex items-center justify-between">
        <a-button type="primary" @click="createAuth">
          {{ t('system.config.auth.add') }}
        </a-button>
      </div>
      <ms-base-table v-bind="propsRes" no-disable v-on="propsEvent">
        <template #name="{ record }">
          <a-button type="text" @click="openAuthDetail(record)">{{ record.name }}</a-button>
        </template>
        <template #enable="{ record }">
          <div v-if="record.enable" class="flex items-center">
            <icon-check-circle-fill class="mr-[2px] text-[rgb(var(--success-6))]" />
            {{ t('system.config.auth.enable') }}
          </div>
          <div v-else class="flex items-center text-[var(--color-text-4)]">
            <icon-stop class="mr-[2px]" />
            {{ t('system.config.auth.disable') }}
          </div>
        </template>
        <template #action="{ record }">
          <MsButton @click="editAuth(record)">{{ t('system.config.auth.edit') }}</MsButton>
          <MsButton v-if="record.enable" @click="disabledAuth(record)">
            {{ t('system.config.auth.disable') }}
          </MsButton>
          <MsButton v-else @click="enableAuth(record)">{{ t('system.config.auth.enable') }}</MsButton>
          <MsTableMoreAction :list="tableActions" @select="handleSelect($event, record)"></MsTableMoreAction>
        </template>
      </ms-base-table>
    </MsCard>
    <MsDrawer
      v-model:visible="showDetailDrawer"
      :width="480"
      :title="activeAuthDetail.name"
      :title-tag="activeAuthDetail.enable ? t('system.config.auth.enable') : t('system.config.auth.disable')"
      :title-tag-color="activeAuthDetail.enable ? 'green' : 'gray'"
      :descriptions="activeAuthDesc"
      :footer="false"
      :mask="false"
      :show-skeleton="detailDrawerLoading"
      show-description
    >
      <template #tbutton>
        <a-button type="outline" size="mini" :disabled="detailDrawerLoading" @click="editAuth(activeAuthDetail)">
          {{ t('system.config.auth.edit') }}
        </a-button>
      </template>
    </MsDrawer>
    <MsDrawer
      v-model:visible="showDrawer"
      :title="t(isEdit ? 'system.config.auth.updateTitle' : 'system.config.auth.add')"
      :ok-text="t(isEdit ? 'system.config.auth.update' : 'system.config.auth.drawerAdd')"
      :ok-loading="drawerLoading"
      :width="680"
      show-continue
      @confirm="handleDrawerConfirm"
      @continue="handleDrawerConfirm(true)"
      @cancel="handleDrawerCancel"
    >
      <a-form ref="authFormRef" :model="activeAuthForm" layout="vertical">
        <a-form-item
          :label="t('system.config.auth.name')"
          field="url"
          asterisk-position="end"
          :rules="[{ required: true, message: t('system.config.auth.nameRequired') }]"
          required
        >
          <a-input
            v-model:model-value="activeAuthForm.name"
            :max-length="250"
            :placeholder="t('system.config.auth.namePlaceholder')"
            allow-clear
          ></a-input>
        </a-form-item>
        <a-form-item :label="t('system.config.auth.desc')" field="url" asterisk-position="end">
          <a-textarea
            v-model:model-value="activeAuthForm.description"
            :max-length="250"
            :placeholder="t('system.config.auth.descPlaceholder')"
            allow-clear
          ></a-textarea>
        </a-form-item>
        <a-form-item :label="t('system.config.auth.addResource')" field="url" asterisk-position="end">
          <a-radio-group v-model:model-value="activeAuthForm.type" type="button">
            <a-radio v-for="item of authTypeList" :key="item" :value="item">{{ item }}</a-radio>
          </a-radio-group>
        </a-form-item>
        <template v-if="activeAuthForm.type === 'CAS'">
          <a-form-item
            :label="t('system.config.auth.serviceUrl')"
            field="url"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.serviceUrlRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.serviceUrl"
              :max-length="250"
              :placeholder="t('system.config.auth.serviceUrlPlaceholder')"
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.loginUrl')"
            field="url"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.loginUrlRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.loginUrl"
              :max-length="250"
              :placeholder="t('system.config.auth.loginUrlPlaceholder')"
              allow-clear
            ></a-input>
            <MsFormItemSub :text="t('system.config.auth.loginUrlTip')" :show-fill-icon="false" />
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.callbackUrl')"
            field="url"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.callbackUrlRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.callbackUrl"
              :max-length="250"
              :placeholder="t('system.config.auth.callbackUrlPlaceholder')"
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.verifyUrl')"
            field="url"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.verifyUrlRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.verifyUrl"
              :max-length="250"
              :placeholder="t('system.config.auth.verifyUrlPlaceholder')"
              allow-clear
            ></a-input>
            <MsFormItemSub :text="t('system.config.auth.verifyUrlTip')" :show-fill-icon="false" />
          </a-form-item>
        </template>
        <template v-else-if="activeAuthForm.type === 'OIDC'">
          <a-form-item
            :label="t('system.config.auth.authUrl')"
            field="url"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.authUrlRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.authUrl"
              :max-length="250"
              :placeholder="t('system.config.auth.authUrlPlaceholder')"
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.tokenUrl')"
            field="url"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.tokenUrlRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.tokenUrl"
              :max-length="250"
              :placeholder="t('system.config.auth.tokenUrlPlaceholder')"
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.userInfoUrl')"
            field="url"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.userInfoUrlRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.userInfoUrl"
              :max-length="250"
              :placeholder="t('system.config.auth.userInfoUrlPlaceholder')"
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.callbackUrl')"
            field="url"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.callbackUrlRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.callbackUrl"
              :max-length="250"
              :placeholder="t('system.config.auth.OIDCCallbackUrlPlaceholder')"
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.clientId')"
            field="url"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.clientIdRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.clientId"
              :max-length="250"
              :placeholder="t('system.config.auth.clientIdPlaceholder')"
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.clientSecret')"
            field="url"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.clientSecretRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.clientSecret"
              :max-length="250"
              :placeholder="t('system.config.auth.clientSecretPlaceholder')"
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.logoutSessionUrl')"
            field="url"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.logoutSessionUrlRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.logoutSessionUrl"
              :max-length="250"
              :placeholder="t('system.config.auth.logoutSessionUrlPlaceholder')"
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item :label="t('system.config.auth.loginUrl')" field="url" asterisk-position="end">
            <a-input
              v-model:model-value="activeAuthForm.configuration.loginUrl"
              :max-length="250"
              :placeholder="t('system.config.auth.loginUrlPlaceholder')"
              allow-clear
            ></a-input>
            <MsFormItemSub :text="t('system.config.auth.loginUrlTip')" :show-fill-icon="false" />
          </a-form-item>
        </template>
        <template v-else-if="activeAuthForm.type === 'OAuth2'">
          <a-form-item
            :label="t('system.config.auth.authUrl')"
            field="url"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.authUrlRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.authUrl"
              :max-length="250"
              :placeholder="t('system.config.auth.authUrlPlaceholder')"
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.tokenUrl')"
            field="url"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.tokenUrlRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.tokenUrl"
              :max-length="250"
              :placeholder="t('system.config.auth.tokenUrlPlaceholder')"
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.userInfoUrl')"
            field="url"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.userInfoUrlRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.userInfoUrl"
              :max-length="250"
              :placeholder="t('system.config.auth.userInfoUrlPlaceholder')"
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.callbackUrl')"
            field="url"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.callbackUrlRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.callbackUrl"
              :max-length="250"
              :placeholder="t('system.config.auth.OIDCCallbackUrlPlaceholder')"
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.clientId')"
            field="url"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.clientIdRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.clientId"
              :max-length="250"
              :placeholder="t('system.config.auth.clientIdPlaceholder')"
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.clientSecret')"
            field="url"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.clientSecretRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.clientSecret"
              :max-length="250"
              :placeholder="t('system.config.auth.clientSecretPlaceholder')"
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.propertyMap')"
            field="url"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.propertyMapRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.propertyMap"
              :max-length="250"
              :placeholder="t('system.config.auth.propertyMapPlaceholder')"
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item :label="t('system.config.auth.logoutSessionUrl')" field="url" asterisk-position="end">
            <a-input
              v-model:model-value="activeAuthForm.configuration.logoutSessionUrl"
              :max-length="250"
              :placeholder="t('system.config.auth.logoutSessionUrlPlaceholder')"
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item :label="t('system.config.auth.linkRange')" field="url" asterisk-position="end">
            <a-input
              v-model:model-value="activeAuthForm.configuration.linkRange"
              :max-length="250"
              :placeholder="t('system.config.auth.linkRangePlaceholder')"
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item :label="t('system.config.auth.loginUrl')" field="url" asterisk-position="end">
            <a-input
              v-model:model-value="activeAuthForm.configuration.loginUrl"
              :max-length="250"
              :placeholder="t('system.config.auth.loginUrlPlaceholder')"
              allow-clear
            ></a-input>
            <MsFormItemSub :text="t('system.config.auth.loginUrlTip')" :show-fill-icon="false" />
          </a-form-item>
        </template>
        <template v-else-if="activeAuthForm.type === 'LDAP'">
          <a-form-item
            :label="t('system.config.auth.LDAPUrl')"
            field="url"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.LDAPUrlRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.LDAPUrl"
              :max-length="250"
              :placeholder="t('system.config.auth.LDAPUrlPlaceholder')"
              allow-clear
            ></a-input>
            <MsFormItemSub :text="t('system.config.auth.LDAPUrlTip')" :show-fill-icon="false" />
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.DN')"
            field="url"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.DNRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.DN"
              :max-length="250"
              :placeholder="t('system.config.auth.DNPlaceholder')"
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.password')"
            field="url"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.passwordRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.password"
              :max-length="250"
              :placeholder="t('system.config.auth.passwordPlaceholder')"
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.OU')"
            field="url"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.OURequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.OU"
              :max-length="250"
              :placeholder="t('system.config.auth.OUPlaceholder')"
              allow-clear
            ></a-input>
            <MsFormItemSub :text="t('system.config.auth.OUTip')" :show-fill-icon="false" />
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.userFilter')"
            field="url"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.userFilterRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.userFilter"
              :max-length="250"
              :placeholder="t('system.config.auth.userFilterPlaceholder')"
              allow-clear
            ></a-input>
            <MsFormItemSub :text="t('system.config.auth.userFilterTip')" :show-fill-icon="false" />
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.LDAPPropertyMap')"
            field="url"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.LDAPPropertyMapRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.LDAPPropertyMap"
              :max-length="250"
              :placeholder="t('system.config.auth.LDAPPropertyMapPlaceholder')"
              allow-clear
            ></a-input>
            <MsFormItemSub :text="t('system.config.auth.LDAPPropertyMapTip')" :show-fill-icon="false" />
          </a-form-item>
          <div>
            <a-button type="outline" class="mr-[16px]" @click="testLink">
              {{ t('system.config.auth.testLink') }}
            </a-button>
            <a-button type="outline" @click="testLogin">{{ t('system.config.auth.testLogin') }}</a-button>
          </div>
        </template>
      </a-form>
    </MsDrawer>
  </div>
</template>

<script setup lang="ts">
  import { computed, onBeforeMount, ref } from 'vue';
  import { Message } from '@arco-design/web-vue';
  import { useI18n } from '@/hooks/useI18n';
  import { useTableStore } from '@/store';
  import { TableKeyEnum } from '@/enums/tableEnum';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import useModal from '@/hooks/useModal';
  import { getAuthList, getAuthDetail, addAuth, updateAuth } from '@/api/modules/setting/config';
  import MsFormItemSub from '@/components/bussiness/ms-form-item-sub/index.vue';
  import { scrollIntoView } from '@/utils/dom';

  import type { FormInstance, ValidatedError } from '@arco-design/web-vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import type { AuthDetail, AuthForm, AuthItem } from '@/models/setting/config';

  const { t } = useI18n();
  const { openModal } = useModal();
  const loading = ref(false);

  const tableActions: ActionsItem[] = [
    {
      label: 'system.config.auth.delete',
      eventTag: 'delete',
      danger: true,
    },
  ];
  const columns: MsTableColumn = [
    {
      title: 'system.config.auth.name',
      slotName: 'name',
      dataIndex: 'name',
      width: 200,
      showInTable: true,
    },
    {
      title: 'system.config.auth.status',
      slotName: 'enable',
      dataIndex: 'enable',
      showInTable: true,
    },
    {
      title: 'system.config.auth.desc',
      dataIndex: 'description',
      showInTable: true,
    },
    {
      title: 'system.config.auth.createTime',
      dataIndex: 'createTime',
      showInTable: true,
    },
    {
      title: 'system.config.auth.updateTime',
      dataIndex: 'updateTime',
      showInTable: true,
    },
    {
      title: 'system.config.auth.action',
      slotName: 'action',
      fixed: 'right',
      width: 120,
      showInTable: true,
    },
  ];
  const tableStore = useTableStore();
  tableStore.initColumn(TableKeyEnum.SYSTEM_AUTH, columns, 'drawer');
  const { propsRes, propsEvent, loadList } = useTable(getAuthList, {
    tableKey: TableKeyEnum.SYSTEM_AUTH,
    columns,
    scroll: { y: 'auto' },
    selectable: false,
    showSelectAll: false,
  });

  onBeforeMount(() => {
    loadList();
  });

  const enableLoading = ref(false);
  /**
   * 启用认证源
   */
  async function enableAuth(record: any) {
    openModal({
      type: 'warning',
      title: t('system.config.auth.enableTipTitle', { name: record.name }),
      content: t('system.config.auth.enableTipContent'),
      okText: t('system.config.auth.enableConfirm'),
      cancelText: t('system.config.auth.cancel'),
      cancelButtonProps: {
        disabled: enableLoading.value,
      },
      okLoading: enableLoading.value,
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          enableLoading.value = true;
          // await togglePoolStatus(record.id);
          Message.success(t('system.config.auth.enableSuccess'));
          loadList();
          return true;
        } catch (error) {
          console.log(error);
          return false;
        } finally {
          enableLoading.value = false;
        }
      },
      hideCancel: false,
    });
  }

  const disableLoading = ref(false);
  /**
   * 禁用认证源
   */
  function disabledAuth(record: any) {
    openModal({
      type: 'warning',
      title: t('system.config.auth.disableTipTitle', { name: record.name }),
      content: t('system.config.auth.disableTipContent'),
      okText: t('system.config.auth.disableConfirm'),
      cancelText: t('system.config.auth.cancel'),
      cancelButtonProps: {
        disabled: disableLoading.value,
      },
      okLoading: disableLoading.value,
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          disableLoading.value = true;
          // await togglePoolStatus(record.id);
          Message.success(t('system.config.auth.disableSuccess'));
          loadList();
          return true;
        } catch (error) {
          console.log(error);
          return false;
        } finally {
          disableLoading.value = false;
        }
      },
      hideCancel: false,
    });
  }

  const delLoading = ref(false);
  /**
   * 删除认证源
   */
  function deleteAuth(record: any) {
    openModal({
      type: 'warning',
      title: t('system.config.auth.deleteTipTitle', { name: record.name }),
      content: t('system.config.auth.deleteTipContent'),
      okText: t('system.config.auth.deleteConfirm'),
      cancelText: t('system.config.auth.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      cancelButtonProps: {
        disabled: delLoading.value,
      },
      maskClosable: false,
      okLoading: delLoading.value,
      onBeforeOk: async () => {
        try {
          delLoading.value = true;
          // await delPoolInfo(record.id);
          Message.success(t('system.config.auth.deleteSuccess'));
          loadList();
          return true;
        } catch (error) {
          console.log(error);
          return false;
        } finally {
          delLoading.value = false;
        }
      },
      hideCancel: false,
    });
  }

  /**
   * 处理表格更多按钮事件
   * @param item
   */
  function handleSelect(item: ActionsItem, record: any) {
    switch (item.eventTag) {
      case 'delete':
        deleteAuth(record);
        break;
      default:
        break;
    }
  }

  const showDetailDrawer = ref(false);
  const detailDrawerLoading = ref(false);
  const activeAuthDesc = ref([]);
  const activeAuthDetail = ref<AuthDetail>({
    id: '',
    enable: true,
    description: '',
    name: '',
    type: '',
    updateTime: 0,
    createTime: 0,
    configuration: {},
  });

  /**
   * 查看认证源
   * @param record 表格项
   */
  async function openAuthDetail(record: AuthItem) {
    try {
      showDetailDrawer.value = true;
      detailDrawerLoading.value = true;
      const res = await getAuthDetail(record.id);
      activeAuthDetail.value = { ...res, configuration: JSON.parse(res.configuration || '{}') };
    } catch (error) {
      console.log(error);
    } finally {
      detailDrawerLoading.value = false;
    }
  }

  const drawerTitle = ref('');
  const showDrawer = ref(false);
  const drawerLoading = ref(false);
  const authFormRef = ref<FormInstance>();
  const authTypeList = ['CAS', 'OIDC', 'OAuth2', 'LDAP'];
  const defaultAuth = {
    id: '',
    enable: true,
    description: '',
    name: '',
    type: 'CAS',
    configuration: {},
  };
  const activeAuthForm = ref<AuthForm>({
    ...defaultAuth,
  });
  const isEdit = computed(() => !!activeAuthForm.value.id);

  /**
   * 编辑认证源
   * @param record 表格项
   */
  function editAuth(record: AuthItem | AuthDetail) {
    drawerTitle.value = t('system.config.auth.update');
    showDrawer.value = true;
    activeAuthForm.value = {
      ...record,
      configuration:
        typeof record.configuration === 'string' ? JSON.parse(record.configuration || '{}') : record.configuration,
    };
  }

  /**
   * 添加认证源
   */
  function createAuth() {
    drawerTitle.value = t('system.config.auth.add');
    showDrawer.value = true;
    activeAuthForm.value = { ...defaultAuth };
  }

  async function testLink() {}
  async function testLogin() {}

  /**
   * 保存认证信息
   * @param isContinue 是否继续添加
   */
  async function saveAuth(isContinue: boolean) {
    try {
      drawerLoading.value = true;
      const params = {
        ...activeAuthForm.value,
        configuration: JSON.stringify(activeAuthForm.value.configuration),
      };
      if (isEdit.value) {
        await updateAuth(params);
        Message.success(t('system.config.auth.updateSuccess'));
      } else {
        await addAuth(params);
        Message.success(t('system.config.auth.addSuccess'));
        if (isContinue) {
          authFormRef.value?.resetFields();
        } else {
          showDrawer.value = false;
        }
      }
    } catch (error) {
      console.log(error);
    } finally {
      drawerLoading.value = false;
    }
  }

  function handleDrawerConfirm(isContinue: boolean) {
    authFormRef.value?.validate(async (errors: Record<string, ValidatedError> | undefined) => {
      if (!errors) {
        saveAuth(isContinue);
      } else {
        scrollIntoView(document.querySelector('.arco-form-item-message'), { block: 'center' });
      }
    });
  }

  function handleDrawerCancel() {
    showDrawer.value = false;
    authFormRef.value?.resetFields();
  }
</script>

<style lang="less" scoped></style>
