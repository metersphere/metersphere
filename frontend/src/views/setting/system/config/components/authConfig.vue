<template>
  <div>
    <MsCard :loading="loading" auto-height simple>
      <div class="mb-4 flex items-center justify-between">
        <a-button v-permission="['SYSTEM_PARAMETER_SETTING_AUTH:READ+ADD']" type="primary" @click="createAuth">
          {{ t('system.config.auth.add') }}
        </a-button>
        <a-input-search
          v-model:model-value="keyword"
          :placeholder="t('system.config.auth.searchTip')"
          class="w-[230px]"
          allow-clear
          @search="searchAuth"
          @press-enter="searchAuth"
          @clear="searchAuth"
        />
      </div>
      <ms-base-table v-bind="propsRes" no-disable v-on="propsEvent">
        <template #name="{ record }">
          <a-button
            type="text"
            class="max-w-full justify-start overflow-hidden px-0"
            @click="openAuthDetail(record.id)"
          >
            <div class="one-line-text">
              {{ record.name }}
            </div>
          </a-button>
        </template>
        <template #type="{ record }">
          <div>{{ record.type === 'OAUTH2' ? 'OAuth 2.0' : record.type }}</div>
        </template>
        <template #action="{ record }">
          <MsButton v-permission="['SYSTEM_PARAMETER_SETTING_AUTH:READ+UPDATE']" @click="editAuth(record)">
            {{ t('system.config.auth.edit') }}
          </MsButton>
          <MsButton
            v-show="record.enable"
            v-permission="['SYSTEM_PARAMETER_SETTING_AUTH:READ+UPDATE']"
            @click="disabledAuth(record)"
          >
            {{ t('system.config.auth.disable') }}
          </MsButton>
          <MsButton
            v-show="!record.enable"
            v-permission="['SYSTEM_PARAMETER_SETTING_AUTH:READ+UPDATE']"
            @click="enableAuth(record)"
          >
            {{ t('system.config.auth.enable') }}
          </MsButton>
          <MsTableMoreAction
            v-permission="['SYSTEM_PARAMETER_SETTING_AUTH:READ+DELETE']"
            :list="tableActions"
            @select="handleSelect($event, record)"
          ></MsTableMoreAction>
        </template>
      </ms-base-table>
    </MsCard>
    <MsDrawer
      v-model:visible="showDetailDrawer"
      :width="680"
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
        <a-button
          v-permission="['SYSTEM_PARAMETER_SETTING_AUTH:READ+UPDATE']"
          type="outline"
          size="mini"
          :disabled="detailDrawerLoading"
          @click="editAuth(activeAuthDetail, true)"
        >
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
      :show-continue="!isEdit"
      @confirm="handleDrawerConfirm"
      @continue="handleDrawerConfirm(true)"
      @cancel="handleDrawerCancel"
    >
      <a-form ref="authFormRef" :model="activeAuthForm" layout="vertical">
        <a-form-item
          :label="t('system.config.auth.name')"
          field="name"
          asterisk-position="end"
          :rules="[{ required: true, message: t('system.config.auth.nameRequired') }]"
          required
        >
          <a-input
            v-model:model-value="activeAuthForm.name"
            :max-length="255"
            :placeholder="t('system.config.auth.namePlaceholder')"
            allow-clear
          ></a-input>
        </a-form-item>
        <a-form-item :label="t('common.desc')" field="description" asterisk-position="end">
          <a-textarea
            v-model:model-value="activeAuthForm.description"
            :max-length="1000"
            :placeholder="t('system.config.auth.descPlaceholder')"
            allow-clear
          ></a-textarea>
        </a-form-item>
        <a-form-item :label="t('system.config.auth.addResource')" field="type" asterisk-position="end">
          <a-radio-group v-model:model-value="activeAuthForm.type" type="button" :disabled="!!activeAuthForm.id">
            <a-radio v-for="item of authTypeList" :key="item" :value="item">
              {{ item === 'OAUTH2' ? 'OAuth 2.0' : item }}
            </a-radio>
          </a-radio-group>
        </a-form-item>
        <template v-if="activeAuthForm.type === 'CAS'">
          <a-form-item
            :label="t('system.config.auth.serviceUrl')"
            field="configuration.casUrl"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.serviceUrlRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.casUrl"
              :max-length="255"
              :placeholder="t('system.config.auth.commonUrlPlaceholder', { url: 'http://<casurl>' })"
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.loginUrl')"
            field="configuration.loginUrl"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.loginUrlRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.loginUrl"
              :max-length="255"
              :placeholder="t('system.config.auth.commonUrlPlaceholder', { url: 'http://<casurl>/login' })"
              allow-clear
            ></a-input>
            <MsFormItemSub :text="t('system.config.auth.loginUrlTip')" :show-fill-icon="false" />
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.callbackUrl')"
            field="configuration.redirectUrl"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.callbackUrlRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.redirectUrl"
              :max-length="255"
              :placeholder="
                t('system.config.auth.commonUrlPlaceholder', {
                  url: 'http://<meteresphere-endpoint>/sso/callback/cas/{authId}',
                })
              "
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.verifyUrl')"
            field="configuration.validateUrl"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.verifyUrlRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.validateUrl"
              :max-length="255"
              :placeholder="t('system.config.auth.commonUrlPlaceholder', { url: 'http://<casurl>/serviceValidate' })"
              allow-clear
            ></a-input>
            <MsFormItemSub :text="t('system.config.auth.verifyUrlTip')" :show-fill-icon="false" />
          </a-form-item>
        </template>
        <template v-else-if="activeAuthForm.type === 'OIDC'">
          <a-form-item
            :label="t('system.config.auth.authUrl')"
            field="configuration.authUrl"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.authUrlRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.authUrl"
              :max-length="255"
              :placeholder="
                t('system.config.auth.commonUrlPlaceholder', {
                  url: 'http://<keyclock>auth/realms/<metersphere>/protocol/openid-connect/auth',
                })
              "
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.tokenUrl')"
            field="configuration.tokenUrl"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.tokenUrlRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.tokenUrl"
              :max-length="255"
              :placeholder="
                t('system.config.auth.commonUrlPlaceholder', {
                  url: 'http://<keyclock>auth/realms/<metersphere>/protocol/openid-connect/token',
                })
              "
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.userInfoUrl')"
            field="configuration.userInfoUrl"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.userInfoUrlRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.userInfoUrl"
              :max-length="255"
              :placeholder="
                t('system.config.auth.commonUrlPlaceholder', {
                  url: 'http://<keyclock>auth/realms/<metersphere>/protocol/openid-connect/userinfo',
                })
              "
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.callbackUrl')"
            field="configuration.redirectUrl"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.callbackUrlRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.redirectUrl"
              :max-length="255"
              :placeholder="
                t('system.config.auth.commonUrlPlaceholder', {
                  url: 'http://<metersphere-endpoint>/sso/callback or http://<metersphere-endpoint>/sso/callback/authld',
                })
              "
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.clientId')"
            field="configuration.clientId"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.clientIdRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.clientId"
              :max-length="255"
              :placeholder="t('system.config.auth.clientIdPlaceholder')"
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.clientSecret')"
            field="configuration.secret"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.clientSecretRequired') }]"
            required
          >
            <a-input-password
              v-model:model-value="activeAuthForm.configuration.secret"
              :max-length="255"
              :placeholder="t('system.config.auth.clientSecretPlaceholder')"
              allow-clear
            ></a-input-password>
          </a-form-item>
          <!--          <a-form-item
            :label="t('system.config.auth.logoutSessionUrl')"
            field="configuration.logoutUrl"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.logoutSessionUrlRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.logoutUrl"
              :max-length="255"
              :placeholder="
                t('system.config.auth.commonUrlPlaceholder', {
                  url: 'http://<keyclock>/auth/realms/<metersphere>/protocol/openid-connect/logout',
                })
              "
              allow-clear
            ></a-input>
          </a-form-item>-->
          <!--          <a-form-item :label="t('system.config.auth.loginUrl')" field="configuration.loginUrl" asterisk-position="end">
            <a-input
              v-model:model-value="activeAuthForm.configuration.loginUrl"
              :max-length="255"
              :placeholder="t('system.config.auth.commonUrlPlaceholder', { url: 'http://<casurl>/login' })"
              allow-clear
            ></a-input>
            <MsFormItemSub :text="t('system.config.auth.loginUrlTip')" :show-fill-icon="false" />
          </a-form-item>-->
        </template>
        <template v-else-if="activeAuthForm.type === 'OAUTH2'">
          <a-form-item
            :label="t('system.config.auth.authUrl')"
            field="configuration.authUrl"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.authUrlRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.authUrl"
              :max-length="255"
              :placeholder="
                t('system.config.auth.commonUrlPlaceholder', {
                  url: 'http://<meteresphere-endpoint>/login/oauth/authorize',
                })
              "
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.tokenUrl')"
            field="configuration.tokenUrl"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.tokenUrlRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.tokenUrl"
              :max-length="255"
              :placeholder="
                t('system.config.auth.commonUrlPlaceholder', {
                  url: 'https://<meteresphere-endpoint>/login/oauth/access_token',
                })
              "
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.userInfoUrl')"
            field="configuration.userInfoUrl"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.userInfoUrlRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.userInfoUrl"
              :max-length="255"
              :placeholder="
                t('system.config.auth.commonUrlPlaceholder', {
                  url: 'https://<meteresphere-endpoint>/user',
                })
              "
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.callbackUrl')"
            field="configuration.redirectUrl"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.callbackUrlRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.redirectUrl"
              :max-length="255"
              :placeholder="
                t('system.config.auth.commonUrlPlaceholder', {
                  url: 'http://<meteresphere-endpoint>/sso/callback/oauth2',
                })
              "
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.clientId')"
            field="configuration.clientId"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.clientIdRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.clientId"
              :max-length="255"
              :placeholder="t('system.config.auth.clientIdPlaceholder')"
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.clientSecret')"
            field="configuration.secret"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.clientSecretRequired') }]"
            required
          >
            <a-input-password
              v-model:model-value="activeAuthForm.configuration.secret"
              :max-length="255"
              :placeholder="t('system.config.auth.oauth.clientSecretPlaceholder')"
              allow-clear
            ></a-input-password>
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.propertyMap')"
            field="configuration.mapping"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.propertyMapRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.mapping"
              :max-length="255"
              placeholder="{'userid':'login','username':'name','email':'email'}"
              allow-clear
            ></a-input>
          </a-form-item>
          <!--          <a-form-item
            :label="t('system.config.auth.logoutSessionUrl')"
            field="configuration.logoutUrl"
            asterisk-position="end"
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.logoutUrl"
              :max-length="255"
              :placeholder="
                t('system.config.auth.commonUrlPlaceholder', {
                  url: 'http://<keyclock>/auth/realms/<metersphere>/protocol/openid-connect/logout',
                })
              "
              allow-clear
            ></a-input>
          </a-form-item>-->
          <a-form-item :label="t('system.config.auth.linkRange')" field="configuration.scope" asterisk-position="end">
            <a-input
              v-model:model-value="activeAuthForm.configuration.scope"
              :max-length="255"
              :placeholder="t('system.config.auth.linkRangePlaceholder')"
              allow-clear
            ></a-input>
          </a-form-item>
          <!--          <a-form-item :label="t('system.config.auth.loginUrl')" field="configuration.loginUrl" asterisk-position="end">
            <a-input
              v-model:model-value="activeAuthForm.configuration.loginUrl"
              :max-length="255"
              :placeholder="t('system.config.auth.commonUrlPlaceholder')"
              allow-clear
            ></a-input>
            <MsFormItemSub :text="t('system.config.auth.loginUrlTip')" :show-fill-icon="false" />
          </a-form-item>-->
        </template>
        <template v-else-if="activeAuthForm.type === 'LDAP'">
          <a-form-item
            :label="t('system.config.auth.LDAPUrl')"
            field="configuration.url"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.LDAPUrlRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.url"
              :max-length="255"
              :placeholder="t('system.config.auth.LDAPUrlPlaceholder')"
              allow-clear
            ></a-input>
            <MsFormItemSub :text="t('system.config.auth.LDAPUrlTip')" :show-fill-icon="false" />
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.DN')"
            field="configuration.dn"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.DNRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.dn"
              :max-length="255"
              :placeholder="t('system.config.auth.DNPlaceholder')"
              allow-clear
            ></a-input>
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.password')"
            field="configuration.password"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.passwordRequired') }]"
            required
          >
            <a-input-password
              v-model:model-value="activeAuthForm.configuration.password"
              :max-length="64"
              :placeholder="t('system.config.auth.LDAPPasswordPlaceholder')"
              allow-clear
              autocomplete="new-password"
            />
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.OU')"
            field="configuration.ou"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.OURequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.ou"
              :max-length="255"
              :placeholder="t('system.config.auth.OUPlaceholder')"
              allow-clear
            ></a-input>
            <MsFormItemSub :text="t('system.config.auth.OUTip')" :show-fill-icon="false" />
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.userFilter')"
            field="configuration.filter"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.userFilterRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.filter"
              :max-length="255"
              :placeholder="t('system.config.auth.userFilterPlaceholder')"
              allow-clear
            ></a-input>
            <MsFormItemSub :text="t('system.config.auth.userFilterTip')" :show-fill-icon="false" />
          </a-form-item>
          <a-form-item
            :label="t('system.config.auth.LDAPPropertyMap')"
            field="configuration.mapping"
            asterisk-position="end"
            :rules="[{ required: true, message: t('system.config.auth.LDAPPropertyMapRequired') }]"
            required
          >
            <a-input
              v-model:model-value="activeAuthForm.configuration.mapping"
              :max-length="255"
              :placeholder="t('system.config.auth.LDAPPropertyMapPlaceholder', { map: '{key:value}' })"
              allow-clear
            ></a-input>
            <MsFormItemSub :text="t('system.config.auth.LDAPPropertyMapTip')" :show-fill-icon="false" />
          </a-form-item>
          <div>
            <a-button type="outline" class="mr-[16px]" :loading="LDAPTestLoading" @click="testLink">
              {{ t('system.config.auth.testLink') }}
            </a-button>
            <a-button type="outline" :loading="LDAPTestLoading" @click="beforeTestLogin">{{
              t('system.config.auth.testLogin')
            }}</a-button>
          </div>
        </template>
      </a-form>
    </MsDrawer>
    <a-modal
      v-model:visible="testLoginModalVisible"
      :title="t('system.config.auth.testLogin')"
      title-align="start"
      class="ms-modal-form ms-modal-medium"
      :mask-closable="false"
      @close="handleTestLoginModalClose"
    >
      <a-form ref="LDAPFormRef" class="rounded-[4px]" :model="LDAPForm" layout="vertical">
        <a-form-item
          field="username"
          :label="t('system.config.auth.testLoginName')"
          :rules="[{ required: true, message: t('system.config.auth.testLoginNameNotNull') }]"
          asterisk-position="end"
        >
          <a-input
            v-model:model-value="LDAPForm.username"
            :placeholder="t('system.config.auth.testLoginNamePlaceholder')"
            :max-length="255"
          ></a-input>
        </a-form-item>
        <a-form-item
          field="password"
          :label="t('system.config.auth.testLoginPassword')"
          :rules="[{ required: true, message: t('system.config.auth.testLoginPasswordNotNull') }]"
          asterisk-position="end"
        >
          <a-input-password
            v-model:model-value="LDAPForm.password"
            :placeholder="t('system.config.auth.testLoginPasswordPlaceholder')"
            autocomplete="new-password"
            :max-length="64"
          />
        </a-form-item>
      </a-form>
      <template #footer>
        <a-button type="secondary" :disabled="LDAPTestLoading" @click="handleTestLoginModalClose">
          {{ t('system.config.auth.testLoginCancel') }}
        </a-button>
        <a-button type="primary" :loading="LDAPTestLoading" @click="testLogin">
          {{ t('system.config.auth.testLogin') }}
        </a-button>
      </template>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
  import { computed, onBeforeMount, ref } from 'vue';
  import { useRoute } from 'vue-router';
  import { Message } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';
  import MsCard from '@/components/pure/ms-card/index.vue';
  import type { Description } from '@/components/pure/ms-description/index.vue';
  import MsDrawer from '@/components/pure/ms-drawer/index.vue';
  import MsBaseTable from '@/components/pure/ms-table/base-table.vue';
  import type { MsTableColumn } from '@/components/pure/ms-table/type';
  import useTable from '@/components/pure/ms-table/useTable';
  import MsTableMoreAction from '@/components/pure/ms-table-more-action/index.vue';
  import type { ActionsItem } from '@/components/pure/ms-table-more-action/types';
  import MsFormItemSub from '@/components/business/ms-form-item-sub/index.vue';

  import {
    addAuth,
    deleteAuth,
    getAuthDetail,
    getAuthList,
    testLdapConnect,
    testLdapLogin,
    updateAuth,
    updateAuthStatus,
  } from '@/api/modules/setting/config';
  import { useI18n } from '@/hooks/useI18n';
  import useModal from '@/hooks/useModal';
  import { useTableStore } from '@/store';
  import { characterLimit } from '@/utils';
  import { scrollIntoView } from '@/utils/dom';
  import { hasAnyPermission } from '@/utils/permission';

  import type { AuthDetail, AuthForm, AuthItem, AuthType } from '@/models/setting/config';
  import { TableKeyEnum } from '@/enums/tableEnum';

  import type { FormInstance, ValidatedError } from '@arco-design/web-vue';

  const route = useRoute();
  const { t } = useI18n();
  const { openModal } = useModal();
  const loading = ref(false);

  const hasOperationPermission = computed(() =>
    hasAnyPermission(['SYSTEM_PARAMETER_SETTING_AUTH:READ+UPDATE', 'SYSTEM_PARAMETER_SETTING_AUTH:READ+DELETE'])
  );

  const tableActions: ActionsItem[] = [
    {
      label: 'system.config.auth.delete',
      eventTag: 'delete',
      danger: true,
      permission: ['SYSTEM_PARAMETER_SETTING_AUTH:READ+DELETE'],
    },
  ];
  const columns: MsTableColumn = [
    {
      title: 'system.config.auth.name',
      slotName: 'name',
      dataIndex: 'name',
      width: 200,
      showTooltip: true,
    },
    {
      title: 'system.config.auth.status',
      slotName: 'enable',
      dataIndex: 'enable',
    },
    {
      title: 'system.config.auth.type',
      slotName: 'type',
      dataIndex: 'type',
    },
    {
      title: 'common.desc',
      dataIndex: 'description',
      showTooltip: true,
    },
    {
      title: 'system.config.auth.createTime',
      dataIndex: 'createTime',
      width: 180,
    },
    {
      title: 'system.config.auth.updateTime',
      dataIndex: 'updateTime',
      width: 180,
    },
    {
      title: hasOperationPermission.value ? 'system.config.auth.action' : '',
      slotName: 'action',
      fixed: 'right',
      width: hasOperationPermission.value ? 140 : 50,
      dataIndex: 'operation',
      showInTable: true,
    },
  ];
  const tableStore = useTableStore();
  const { propsRes, propsEvent, loadList, setLoadListParams } = useTable(getAuthList, {
    tableKey: TableKeyEnum.SYSTEM_AUTH,
    columns,
    scroll: { y: 'auto', x: '100%' },
    selectable: false,
    showSelectAll: false,
  });

  onBeforeMount(() => {
    loadList();
  });

  const keyword = ref('');

  function searchAuth() {
    setLoadListParams({
      keyword: keyword.value,
    });
    loadList();
  }

  /**
   * 启用认证源
   */
  async function enableAuth(record: AuthItem) {
    openModal({
      type: 'info',
      title: t('system.config.auth.enableTipTitle', { name: characterLimit(record.name) }),
      content: t('system.config.auth.enableTipContent'),
      okText: t('system.config.auth.enableConfirm'),
      cancelText: t('system.config.auth.cancel'),
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          await updateAuthStatus({
            id: record.id,
            enable: true,
          });
          Message.success(t('system.config.auth.enableSuccess'));
          loadList();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  /**
   * 禁用认证源
   */
  function disabledAuth(record: AuthItem) {
    openModal({
      type: 'info',
      title: t('system.config.auth.disableTipTitle', { name: characterLimit(record.name) }),
      content: t('system.config.auth.disableTipContent'),
      okText: t('system.config.auth.disableConfirm'),
      cancelText: t('system.config.auth.cancel'),
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          await updateAuthStatus({
            id: record.id,
            enable: false,
          });
          Message.success(t('system.config.auth.disableSuccess'));
          loadList();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  /**
   * 删除认证源
   */
  function delAuth(record: AuthItem) {
    openModal({
      type: 'error',
      title: t('system.config.auth.deleteTipTitle', { name: characterLimit(record.name) }),
      content: t('system.config.auth.deleteTipContent'),
      okText: t('system.config.auth.deleteConfirm'),
      cancelText: t('system.config.auth.cancel'),
      okButtonProps: {
        status: 'danger',
      },
      maskClosable: false,
      onBeforeOk: async () => {
        try {
          await deleteAuth(record.id);
          Message.success(t('system.config.auth.deleteSuccess'));
          loadList();
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        }
      },
      hideCancel: false,
    });
  }

  /**
   * 处理表格更多按钮事件
   * @param item
   */
  function handleSelect(item: ActionsItem, record: AuthItem) {
    switch (item.eventTag) {
      case 'delete':
        delAuth(record);
        break;
      default:
        break;
    }
  }

  const showDetailDrawer = ref(false);
  const detailDrawerLoading = ref(false);
  const activeAuthDesc = ref<Description[]>([]);
  const activeAuthDetail = ref<AuthDetail>({
    id: '',
    enable: true,
    description: '',
    name: '',
    type: 'LDAP',
    updateTime: 0,
    createTime: 0,
    configuration: {},
  });

  /**
   * 查看认证源
   * @param id 表格项 id
   */
  async function openAuthDetail(id: string) {
    try {
      showDetailDrawer.value = true;
      detailDrawerLoading.value = true;
      const res = await getAuthDetail(id);
      activeAuthDetail.value = { ...res, configuration: JSON.parse(res.configuration || '{}') };
      const { configuration } = activeAuthDetail.value;
      let description: Description[] = [
        {
          label: t('common.desc'),
          value: activeAuthDetail.value.description,
        },
      ];
      switch (res.type) {
        case 'CAS':
          description = description.concat([
            {
              label: t('system.config.auth.serviceUrl'),
              value: configuration.casUrl,
            },
            {
              label: t('system.config.auth.loginUrl'),
              value: configuration.loginUrl,
            },
            {
              label: t('system.config.auth.callbackUrl'),
              value: configuration.redirectUrl,
            },
            {
              label: t('system.config.auth.verifyUrl'),
              value: configuration.validateUrl,
            },
          ]);
          break;
        case 'OIDC':
          description = description.concat([
            {
              label: t('system.config.auth.authUrl'),
              value: configuration.authUrl,
            },
            {
              label: t('system.config.auth.tokenUrl'),
              value: configuration.tokenUrl,
            },
            {
              label: t('system.config.auth.userInfoUrl'),
              value: configuration.userInfoUrl,
            },
            {
              label: t('system.config.auth.callbackUrl'),
              value: configuration.redirectUrl,
            },
            {
              label: t('system.config.auth.clientId'),
              value: configuration.clientId,
            },
            {
              label: t('system.config.auth.clientSecret'),
              value: configuration.secret,
            },
            {
              label: t('system.config.auth.logoutSessionUrl'),
              value: configuration.logoutUrl,
            },
            {
              label: t('system.config.auth.loginUrl'),
              value: configuration.loginUrl,
            },
          ]);
          break;
        case 'OAUTH2':
          description = description.concat([
            {
              label: t('system.config.auth.authUrl'),
              value: configuration.authUrl,
            },
            {
              label: t('system.config.auth.tokenUrl'),
              value: configuration.tokenUrl,
            },
            {
              label: t('system.config.auth.userInfoUrl'),
              value: configuration.userInfoUrl,
            },
            {
              label: t('system.config.auth.callbackUrl'),
              value: configuration.redirectUrl,
            },
            {
              label: t('system.config.auth.clientId'),
              value: configuration.clientId,
            },
            {
              label: t('system.config.auth.clientSecret'),
              value: configuration.secret,
            },
            {
              label: t('system.config.auth.logoutSessionUrl'),
              value: configuration.logoutUrl,
            },
            {
              label: t('system.config.auth.linkRange'),
              value: configuration.scope,
            },
            {
              label: t('system.config.auth.loginUrl'),
              value: configuration.loginUrl,
            },
          ]);
          break;
        case 'LDAP':
          description = description.concat([
            {
              label: t('system.config.auth.LDAPUrl'),
              value: configuration.url,
            },
            {
              label: t('system.config.auth.DN'),
              value: configuration.dn,
            },
            {
              label: t('system.config.auth.password'),
              value: configuration.password,
            },
            {
              label: t('system.config.auth.OU'),
              value: configuration.ou,
            },
            {
              label: t('system.config.auth.userFilter'),
              value: configuration.filter,
            },
            {
              label: t('system.config.auth.LDAPPropertyMap'),
              value: configuration.mapping,
            },
          ]);
          break;
        default:
          break;
      }
      activeAuthDesc.value = description;
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
      showDetailDrawer.value = false;
    } finally {
      detailDrawerLoading.value = false;
    }
  }

  const drawerTitle = ref('');
  const showDrawer = ref(false);
  const drawerLoading = ref(false);
  const authFormRef = ref<FormInstance>();
  const authTypeList = ['CAS', 'OIDC', 'OAUTH2', 'LDAP'];
  const defaultAuth = {
    id: '',
    enable: true,
    description: '',
    name: '',
    type: 'LDAP' as AuthType,
    configuration: {},
  };
  const activeAuthForm = ref<AuthForm>({
    ...defaultAuth,
  });
  const isEdit = computed(() => !!activeAuthForm.value.id);

  /**
   * 编辑认证源
   * @param record 表格项
   * @param isFromDetail 是否从详情抽屉打开编辑
   */
  async function editAuth(record: AuthItem | AuthDetail, isFromDetail = false) {
    if (isFromDetail) {
      drawerTitle.value = t('system.config.auth.update');
      showDrawer.value = true;
      activeAuthForm.value = { ...record } as AuthDetail;
      showDetailDrawer.value = false;
      return;
    }
    try {
      drawerTitle.value = t('system.config.auth.update');
      showDrawer.value = true;
      drawerLoading.value = true;
      const res = await getAuthDetail(record.id);
      activeAuthForm.value = {
        ...res,
        configuration:
          typeof res.configuration === 'string' ? JSON.parse(res.configuration || '{}') : res.configuration,
      };
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      drawerLoading.value = false;
    }
  }

  /**
   * 添加认证源
   */
  function createAuth() {
    drawerTitle.value = t('system.config.auth.add');
    showDrawer.value = true;
    activeAuthForm.value = { ...defaultAuth };
  }

  const LDAPTestLoading = ref(false);
  const testLoginModalVisible = ref(false);
  const LDAPForm = ref({
    username: '',
    password: '',
  });
  const LDAPFormRef = ref<FormInstance>();

  function handleTestLoginModalClose() {
    LDAPFormRef.value?.resetFields();
    testLoginModalVisible.value = false;
  }

  function testLink() {
    authFormRef.value?.validateField(
      ['configuration.url', 'configuration.dn', 'configuration.password'],
      async (res) => {
        if (!res) {
          try {
            LDAPTestLoading.value = true;
            await testLdapConnect({
              ldapUrl: activeAuthForm.value.configuration.url,
              ldapDn: activeAuthForm.value.configuration.dn,
              ldapPassword: activeAuthForm.value.configuration.password,
            });
            Message.success(t('system.config.auth.testLinkSuccess'));
          } catch (error) {
            // eslint-disable-next-line no-console
            console.log(error);
          } finally {
            LDAPTestLoading.value = false;
          }
        }
      }
    );
  }

  function testLogin() {
    LDAPFormRef.value?.validate(async (res) => {
      if (!res) {
        try {
          LDAPTestLoading.value = true;
          await testLdapLogin({
            ldapUrl: activeAuthForm.value.configuration.url,
            ldapDn: activeAuthForm.value.configuration.dn,
            ldapPassword: activeAuthForm.value.configuration.password,
            username: LDAPForm.value.username,
            password: LDAPForm.value.password,
            ldapUserFilter: activeAuthForm.value.configuration.filter,
            ldapUserOu: activeAuthForm.value.configuration.ou,
            ldapUserMapping: activeAuthForm.value.configuration.mapping,
          });
          Message.success(t('system.config.auth.testLinkSuccess'));
        } catch (error) {
          // eslint-disable-next-line no-console
          console.log(error);
        } finally {
          LDAPTestLoading.value = false;
        }
      }
    });
  }

  function beforeTestLogin() {
    authFormRef.value?.validateField(
      [
        'configuration.url',
        'configuration.dn',
        'configuration.password',
        'configuration.filter',
        'configuration.ou',
        'configuration.mapping',
      ],
      async (errors: Record<string, ValidatedError> | undefined) => {
        if (!errors) {
          testLoginModalVisible.value = true;
        } else {
          scrollIntoView(document.querySelector('.arco-form-item-message'), { block: 'center' });
        }
      }
    );
  }

  /**
   * 保存认证信息
   * @param isContinue 是否继续添加
   */
  async function saveAuth(isContinue: boolean) {
    try {
      drawerLoading.value = true;
      const { configuration } = activeAuthForm.value;
      let _configuration = {};
      switch (activeAuthForm.value.type) {
        case 'CAS':
          _configuration = {
            casUrl: configuration.casUrl,
            loginUrl: configuration.loginUrl,
            redirectUrl: configuration.redirectUrl,
            validateUrl: configuration.validateUrl,
          };
          break;
        case 'OIDC':
          _configuration = {
            authUrl: configuration.authUrl,
            tokenUrl: configuration.tokenUrl,
            userInfoUrl: configuration.userInfoUrl,
            redirectUrl: configuration.redirectUrl,
            clientId: configuration.clientId,
            secret: configuration.secret,
            logoutUrl: configuration.logoutUrl,
            loginUrl: configuration.loginUrl,
          };
          break;
        case 'OAUTH2':
          _configuration = {
            authUrl: configuration.authUrl,
            tokenUrl: configuration.tokenUrl,
            userInfoUrl: configuration.userInfoUrl,
            redirectUrl: configuration.redirectUrl,
            clientId: configuration.clientId,
            secret: configuration.secret,
            mapping: configuration.mapping,
            logoutUrl: configuration.logoutUrl,
            scope: configuration.scope,
            loginUrl: configuration.loginUrl,
          };
          break;
        case 'LDAP':
          _configuration = {
            url: configuration.url,
            dn: configuration.dn,
            password: configuration.password,
            ou: configuration.ou,
            filter: configuration.filter,
            mapping: configuration.mapping,
          };
          break;
        default:
          break;
      }
      const params = {
        ...activeAuthForm.value,
        configuration: JSON.stringify(_configuration),
      };
      if (isEdit.value) {
        await updateAuth(params);
        Message.success(t('system.config.auth.updateSuccess'));
        showDrawer.value = false;
      } else {
        await addAuth(params);
        Message.success(t('system.config.auth.addSuccess'));
        if (isContinue) {
          authFormRef.value?.resetFields();
        } else {
          showDrawer.value = false;
        }
      }
      authFormRef.value?.resetFields();
      loadList();
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    } finally {
      drawerLoading.value = false;
    }
  }

  /**
   * 处理抽屉确认
   * @param isContinue 是否继续添加
   */
  function handleDrawerConfirm(isContinue: boolean) {
    authFormRef.value?.validate(async (errors: Record<string, ValidatedError> | undefined) => {
      if (!errors) {
        saveAuth(isContinue);
      } else {
        scrollIntoView(authFormRef.value?.$el.querySelector('.arco-form-item-message'), { block: 'center' });
      }
    });
  }

  function handleDrawerCancel() {
    showDrawer.value = false;
    authFormRef.value?.resetFields();
  }

  onBeforeMount(() => {
    if (route.query.id) {
      openAuthDetail(route.query.id as string);
    }
  });

  await tableStore.initColumn(TableKeyEnum.SYSTEM_AUTH, columns, 'drawer');
</script>

<style lang="less" scoped></style>
