<template>
  <div class="usergroup-auth-table">
    <a-table
      :span-method="dataSpanMethod"
      :scroll="{ y: '500px', x: '800px' }"
      :data="tableData"
      :loading="loading"
      :bordered="{ wrapper: true, cell: true }"
      size="small"
      :pagination="false"
    >
      <template #columns>
        <a-table-column :width="100" :title="t('system.userGroup.function')" data-index="ability" />
        <a-table-column :width="150" :title="t('system.userGroup.operationObject')" data-index="operationObject" />
        <a-table-column :title="t('system.userGroup.auth')">
          <template #cell="{ record, rowIndex }">
            <a-checkbox-group v-model="record.perChecked" @change="(v) => handleCellAuthChange(v, rowIndex)">
              <a-checkbox
                v-for="item in record.permissions"
                :key="item.id"
                :disabled="item.license || currentInternal"
                :value="item.id"
                >{{ t(item.name) }}</a-checkbox
              >
            </a-checkbox-group>
          </template>
        </a-table-column>
        <a-table-column :width="50" fixed="right" align="center" :bordered="false">
          <template #title>
            <a-checkbox
              v-if="tableData && tableData?.length > 0"
              :model-value="allChecked"
              :indeterminate="allIndeterminate"
              :disabled="currentInternal"
              @change="handleAllAuthChangeByCheckbox"
            ></a-checkbox>
          </template>
          <template #cell="{ record, rowIndex }">
            <a-checkbox
              :model-value="record.enable"
              :indeterminate="record.indeterminate"
              :disabled="currentInternal"
              @change="(value) => handleRowAuthChange(value, rowIndex)"
            />
          </template>
        </a-table-column>
      </template>
    </a-table>
  </div>
</template>

<script setup lang="ts">
  import { useI18n } from '@/hooks/useI18n';
  import { RenderFunction, VNodeChild, ref, watchEffect, computed } from 'vue';
  import { type TableColumnData, type TableData } from '@arco-design/web-vue';
  import useUserGroupStore from '@/store/modules/setting/system/usergroup';
  import { getGlobalUSetting, saveGlobalUSetting } from '@/api/modules/setting/usergroup';
  import { UserGroupAuthSetting, AuthTableItem, type AuthScopeType, SavePermissions } from '@/models/setting/usergroup';

  export declare type OperationName = 'selection-checkbox' | 'selection-radio' | 'expand' | 'drag-handle';

  export interface TableOperationColumn {
    name: OperationName | string;
    title?: string | RenderFunction;
    width?: number;
    fixed?: boolean;
    render?: (record: TableData) => VNodeChild;
    isLastLeftFixed?: boolean;
  }

  const loading = ref(false);
  const store = useUserGroupStore();

  const systemSpan = ref(1);
  const projectSpan = ref(1);
  const organizationSpan = ref(1);
  // 表格的总全选
  const allChecked = ref(false);
  const allIndeterminate = ref(false);

  const tableData = ref<AuthTableItem[]>();
  // 是否可以保存
  const canSave = ref(false);
  // 内部用户不可编辑
  const currentInternal = computed(() => {
    return store.userGroupInfo.currentInternal;
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
    }
  };

  const { t } = useI18n();

  /**
   * 生成数据
   * @param item
   * @param type
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
        ability: index === 0 ? t(`system.userGroup.${type}`) : undefined,
        operationObject: t(child.name),
        isSystem: index === 0 && type === 'SYSTEM',
        isOrganization: index === 0 && type === 'ORGANIZATION',
        isProject: index === 0 && type === 'PROJECT',
      });
    });
    return result;
  };
  // 转换数据 计算系统，组织，项目的合并行数
  const transformData = (data: UserGroupAuthSetting[]) => {
    const result: AuthTableItem[] = [];
    data.forEach((item) => {
      if (item.type === 'SYSTEM') {
        systemSpan.value = item.children?.length || 0;
      }
      if (item.type === 'PROJECT') {
        projectSpan.value = item.children?.length || 0;
      }
      if (item.type === 'ORGANIZATION') {
        organizationSpan.value = item.children?.length || 0;
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

  const initData = async (id: string) => {
    try {
      loading.value = true;
      const res = await getGlobalUSetting(id);
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
      await saveGlobalUSetting({
        userRoleId: store.currentId,
        permissions,
      });
      initData(store.currentId);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log('error', error);
    }
  };

  // 恢复默认值
  const handleReset = () => {
    if (store.currentId) {
      initData(store.currentId);
    }
  };

  watchEffect(() => {
    if (store.currentId) {
      initData(store.currentId);
    }
  });
  defineExpose({
    handleReset,
    handleSave,
    canSave,
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
    .action {
      position: absolute;
      right: 24px;
      bottom: 0;
      left: 24px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      width: calc(100% - 24px);
    }
  }
</style>
