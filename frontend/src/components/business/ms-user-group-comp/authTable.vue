<template>
  <div class="usergroup-auth-table">
    <a-table
      :span-method="dataSpanMethod"
      :scroll="props.scroll"
      :data="tableData"
      :loading="loading"
      :bordered="{ wrapper: true, cell: true }"
      size="small"
      :pagination="false"
    >
      <template #columns>
        <a-table-column :width="100" :title="t('system.userGroup.function')" data-index="ability" />
        <a-table-column :width="150" :title="t('system.userGroup.operationObject')" data-index="operationObject" />
        <a-table-column>
          <template #title>
            <div class="flex w-full flex-row justify-between">
              <div>{{ t('system.userGroup.auth') }}</div>
              <a-checkbox
                v-if="tableData && tableData?.length > 0"
                :model-value="allChecked"
                :indeterminate="allIndeterminate"
                :disabled="currentInternal"
                class="mr-[7px]"
                @change="handleAllAuthChangeByCheckbox"
              ></a-checkbox>
            </div>
          </template>
          <template #cell="{ record, rowIndex }">
            <div class="flex flex-row items-center justify-between">
              <a-checkbox-group v-model="record.perChecked" @change="(v) => handleCellAuthChange(v, rowIndex)">
                <a-checkbox
                  v-for="item in record.permissions"
                  :key="item.id"
                  :disabled="item.license || currentInternal"
                  :value="item.id"
                  >{{ t(item.name) }}</a-checkbox
                >
              </a-checkbox-group>
              <a-checkbox
                class="mr-[7px]"
                :model-value="record.enable"
                :indeterminate="record.indeterminate"
                :disabled="currentInternal"
                @change="(value) => handleRowAuthChange(value, rowIndex)"
              />
            </div>
          </template>
        </a-table-column>
      </template>
    </a-table>
  </div>
  <div
    v-if="props.showBottom"
    class="fixed bottom-[16px] right-[16px] z-[999] flex justify-between bg-white p-[24px] shadow-[0_-1px_4px_rgba(2,2,2,0.1)]"
    :style="{ width: props.width }"
  >
    <ms-button :disabled="!canSave" @click="handleReset">{{ t('system.userGroup.reset') }}</ms-button>
    <a-button :disabled="!canSave" type="primary" @click="handleSave">{{ t('system.userGroup.save') }}</a-button>
  </div>
</template>

<script setup lang="ts">
  import { computed, inject, ref, RenderFunction, VNodeChild, watchEffect } from 'vue';
  import { Message, type TableColumnData, type TableData } from '@arco-design/web-vue';

  import MsButton from '@/components/pure/ms-button/index.vue';

  import { getAuthByUserGroup, saveProjectUGSetting } from '@/api/modules/project-management/usergroup';
  import {
    getGlobalUSetting,
    getOrgUSetting,
    saveGlobalUSetting,
    saveOrgUSetting,
  } from '@/api/modules/setting/usergroup';
  import { useI18n } from '@/hooks/useI18n';

  import {
    type AuthScopeType,
    AuthTableItem,
    CurrentUserGroupItem,
    SavePermissions,
    UserGroupAuthSetting,
  } from '@/models/setting/usergroup';
  import { AuthScopeEnum } from '@/enums/commonEnum';

  export declare type OperationName = 'selection-checkbox' | 'selection-radio' | 'expand' | 'drag-handle';

  export interface TableOperationColumn {
    name: OperationName | string;
    title?: string | RenderFunction;
    width?: number;
    fixed?: boolean;
    render?: (record: TableData) => VNodeChild;
    isLastLeftFixed?: boolean;
  }

  const props = withDefaults(
    defineProps<{
      current: CurrentUserGroupItem;
      width?: string;
      showBottom?: boolean;
      scroll?: {
        x?: number | string;
        y?: number | string;
        minWidth?: number | string;
        maxHeight?: number | string;
      };
    }>(),
    {
      showBottom: true,
      scroll() {
        return {
          x: '800px',
          y: 'calc(100vh - 254px)',
        };
      },
    }
  );

  const systemType = inject<AuthScopeEnum>('systemType');

  const loading = ref(false);

  const systemSpan = ref(1);
  const projectSpan = ref(1);
  const organizationSpan = ref(1);
  const workstationSpan = ref(1);
  const testPlanSpan = ref(1);
  const bugManagementSpan = ref(1);
  const caseManagementSpan = ref(1);
  const uiTestSpan = ref(1);
  const apiTestSpan = ref(1);
  const loadTestSpan = ref(1);
  const personalSpan = ref(1);

  // 表格的总全选
  const allChecked = ref(false);
  const allIndeterminate = ref(false);

  const tableData = ref<AuthTableItem[]>();
  // 是否可以保存
  const canSave = ref(false);
  // 内部用户不可编辑
  const currentInternal = computed(() => {
    return props.current.internal;
  });

  const dataSpanMethod = (data: {
    record: TableData;
    column: TableColumnData | TableOperationColumn;
    rowIndex: number;
    columnIndex: number;
  }) => {
    const { record, column } = data;
    if ((column as TableColumnData).dataIndex === 'ability') {
      if (record.isSystem) {
        return {
          rowspan: systemSpan.value,
        };
      }
      if (record.isOrganization) {
        return {
          rowspan: organizationSpan.value,
        };
      }
      if (record.isProject) {
        return {
          rowspan: projectSpan.value,
        };
      }
      if (record.isWorkstation) {
        return {
          rowspan: workstationSpan.value,
        };
      }
      if (record.isTestPlan) {
        return {
          rowspan: testPlanSpan.value,
        };
      }
      if (record.isBugManagement) {
        return {
          rowspan: bugManagementSpan.value,
        };
      }
      if (record.isCaseManagement) {
        return {
          rowspan: caseManagementSpan.value,
        };
      }
      if (record.isUiTest) {
        return {
          rowspan: uiTestSpan.value,
        };
      }
      if (record.isApiTest) {
        return {
          rowspan: apiTestSpan.value,
        };
      }
      if (record.isLoadTest) {
        return {
          rowspan: loadTestSpan.value,
        };
      }
      if (record.isPersonal) {
        return {
          rowspan: personalSpan.value,
        };
      }
    }
  };

  const { t } = useI18n();

  /**
   * 生成数据
   * @param type
   * @param idx
   */
  const makeData = (item: UserGroupAuthSetting, type: AuthScopeType) => {
    const result: AuthTableItem[] = [];
    item.children?.forEach((child, index) => {
      const perChecked =
        child?.permissions?.reduce((acc: string[], cur) => {
          if (cur.enable) {
            acc.push(cur.id);
          }
          return acc;
        }, []) || [];
      const perCheckedLength = perChecked.length;
      let indeterminate = false;
      if (child?.permissions) {
        indeterminate = perCheckedLength > 0 && perCheckedLength < child?.permissions?.length;
      }
      result.push({
        id: child?.id,
        license: child?.license,
        enable: child?.enable,
        permissions: child?.permissions,
        indeterminate,
        perChecked,
        ability: index === 0 ? item.name : undefined,
        operationObject: t(child.name),
        isSystem: index === 0 && type === 'SYSTEM',
        isOrganization: index === 0 && type === 'ORGANIZATION',
        isProject: index === 0 && type === 'PROJECT',
        isWorkstation: index === 0 && type === 'WORKSTATION',
        isTestPlan: index === 0 && type === 'TEST_PLAN',
        isBugManagement: index === 0 && type === 'BUG_MANAGEMENT',
        isCaseManagement: index === 0 && type === 'CASE_MANAGEMENT',
        isUiTest: index === 0 && type === 'UI_TEST',
        isLoadTest: index === 0 && type === 'LOAD_TEST',
        isApiTest: index === 0 && type === 'API_TEST',
        isPersonal: index === 0 && type === 'PERSONAL',
      });
    });
    return result;
  };
  // 转换数据 计算系统，组织，项目的合并行数
  const transformData = (data: UserGroupAuthSetting[]) => {
    const result: AuthTableItem[] = [];
    data.forEach((item) => {
      if (item.id === 'SYSTEM') {
        systemSpan.value = item.children?.length || 0;
      } else if (item.id === 'PROJECT') {
        projectSpan.value = item.children?.length || 0;
      } else if (item.id === 'ORGANIZATION') {
        organizationSpan.value = item.children?.length || 0;
      } else if (item.id === 'WORKSTATION') {
        workstationSpan.value = item.children?.length || 0;
      } else if (item.id === 'TEST_PLAN') {
        testPlanSpan.value = item.children?.length || 0;
      } else if (item.id === 'BUG_MANAGEMENT') {
        bugManagementSpan.value = item.children?.length || 0;
      } else if (item.id === 'CASE_MANAGEMENT') {
        caseManagementSpan.value = item.children?.length || 0;
      } else if (item.id === 'UI_TEST') {
        uiTestSpan.value = item.children?.length || 0;
      } else if (item.id === 'API_TEST') {
        apiTestSpan.value = item.children?.length || 0;
      } else if (item.id === 'LOAD_TEST') {
        loadTestSpan.value = item.children?.length || 0;
      } else if (item.id === 'PERSONAL') {
        personalSpan.value = item.children?.length || 0;
      }
      result.push(...makeData(item, item.id));
    });
    return result;
  };

  // 表格总全选change事件
  const handleAllAuthChangeByCheckbox = () => {
    if (!tableData.value) return;
    allChecked.value = !allChecked.value;
    allIndeterminate.value = false;
    const tmpArr = tableData.value;
    tmpArr.forEach((item) => {
      item.enable = allChecked.value;
      item.indeterminate = false;
      item.perChecked = allChecked.value ? item.permissions?.map((ele) => ele.id) : [];
    });
    if (!canSave.value) canSave.value = true;
  };

  // 表格总全选联动触发事件
  const handleAllChange = (isInit = false) => {
    if (!tableData.value) return;
    const tmpArr = tableData.value;
    const { length: allLength } = tmpArr;
    const { length } = tmpArr.filter((item) => item.enable);
    if (length === allLength) {
      allChecked.value = true;
      allIndeterminate.value = false;
    } else if (length === 0) {
      allChecked.value = false;
      allIndeterminate.value = false;
    } else {
      allChecked.value = false;
      allIndeterminate.value = true;
    }
    if (!isInit && !canSave.value) canSave.value = true;
  };

  // 表格最后一列的复选框change事件
  const handleRowAuthChange = (value: boolean | (string | number | boolean)[], rowIndex: number) => {
    if (!tableData.value) return;
    const tmpArr = tableData.value;
    tmpArr[rowIndex].indeterminate = false;
    if (value) {
      tmpArr[rowIndex].enable = true;
      tmpArr[rowIndex].perChecked = tmpArr[rowIndex].permissions?.map((item) => item.id);
    } else {
      tmpArr[rowIndex].enable = false;
      tmpArr[rowIndex].perChecked = [];
    }
    tableData.value = [...tmpArr];
    handleAllChange();
    if (!canSave.value) canSave.value = true;
  };

  // 表格第三列的复选框change事件
  const handleCellAuthChange = (values: (string | number | boolean)[], rowIndex: number) => {
    if (!tableData.value) return;
    const tmpArr = tableData.value;
    const length = tmpArr[rowIndex].permissions?.length || 0;
    if (values.length === length) {
      tmpArr[rowIndex].enable = true;
      tmpArr[rowIndex].indeterminate = false;
    } else if (values.length === 0) {
      tmpArr[rowIndex].enable = false;
      tmpArr[rowIndex].indeterminate = false;
    } else {
      tmpArr[rowIndex].enable = false;
      tmpArr[rowIndex].indeterminate = true;
    }
    handleAllChange();
  };

  // 初始化数据
  const initData = async (id: string, internal: boolean) => {
    try {
      let res: UserGroupAuthSetting[] = [];
      loading.value = true;
      if (systemType === AuthScopeEnum.SYSTEM) {
        res = await getGlobalUSetting(id);
      } else if (systemType === AuthScopeEnum.ORGANIZATION) {
        if (internal) {
          res = await getGlobalUSetting(id);
        } else {
          res = await getOrgUSetting(id);
        }
      } else {
        res = await getAuthByUserGroup(id);
      }

      tableData.value = transformData(res);
      handleAllChange(true);
    } catch (error) {
      tableData.value = [];
    } finally {
      loading.value = false;
    }
  };

  // 保存
  const handleSave = async () => {
    if (!tableData.value) return;
    const permissions: SavePermissions[] = [];

    const tmpArr = tableData.value;
    tmpArr.forEach((item) => {
      item.permissions?.forEach((ele) => {
        ele.enable = item.perChecked?.includes(ele.id) || false;
        permissions.push({
          id: ele.id,
          enable: ele.enable,
        });
      });
    });
    try {
      if (systemType === AuthScopeEnum.SYSTEM) {
        await saveGlobalUSetting({
          userRoleId: props.current.id,
          permissions,
        });
      } else if (systemType === AuthScopeEnum.ORGANIZATION) {
        await saveOrgUSetting({
          userRoleId: props.current.id,
          permissions,
        });
      } else {
        // 项目的
        await saveProjectUGSetting({
          userRoleId: props.current.id,
          permissions,
        });
      }
      Message.success(t('common.saveSuccess'));
      initData(props.current.id, props.current.internal);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  };

  // 恢复默认值
  const handleReset = () => {
    if (props.current.id) {
      initData(props.current.id, props.current.internal);
    }
  };

  watchEffect(() => {
    if (props.current.id) {
      initData(props.current.id, props.current.internal);
    }
  });
  defineExpose({
    canSave,
    handleSave,
    handleReset,
  });
</script>

<style scoped lang="less">
  .usergroup-auth-table {
    position: relative;
    min-height: calc(100vh - 230px);
    :deep(.arco-table-container) {
      border-top: 1px solid var(--color-text-n8) !important;
      border-right: 1px solid var(--color-text-n8) !important;
      border-left: 1px solid var(--color-text-n8) !important;
    }
    :deep(.arco-table-th-title) {
      width: 100%;
    }
  }
</style>
