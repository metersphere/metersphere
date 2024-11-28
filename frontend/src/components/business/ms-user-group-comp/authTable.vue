<template>
  <div class="flex h-full flex-col gap-[16px] overflow-hidden">
    <div class="group-auth-table">
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
          <a-table-column
            cell-class="auth-type-class"
            :width="100"
            :title="t('system.userGroup.function')"
            data-index="ability"
          />
          <a-table-column :width="150" :title="t('system.userGroup.operationObject')" data-index="operationObject" />
          <a-table-column>
            <template #title>
              <div class="flex w-full flex-row justify-between">
                <div>{{ t('system.userGroup.auth') }}</div>
                <a-checkbox
                  v-if="tableData && tableData?.length > 0"
                  :model-value="allChecked"
                  :indeterminate="allIndeterminate"
                  :disabled="systemAdminDisabled || disabled"
                  class="mr-[7px]"
                  @change="handleAllAuthChangeByCheckbox"
                ></a-checkbox>
              </div>
            </template>
            <template #cell="{ record, rowIndex }">
              <div class="flex flex-row items-center justify-between">
                <a-checkbox-group
                  :model-value="record.perChecked"
                  @change="(v, e) => handleCellAuthChange(v, rowIndex, record, e)"
                >
                  <template v-for="item in record.permissions" :key="item.id">
                    <a-checkbox
                      v-if="(item.license && licenseStore.hasLicense()) || !item.license"
                      :disabled="systemAdminDisabled || disabled"
                      :value="item.id"
                    >
                      {{ t(item.name) }}
                    </a-checkbox>
                  </template>
                </a-checkbox-group>
                <a-checkbox
                  class="mr-[7px]"
                  :model-value="record.enable"
                  :indeterminate="record.indeterminate"
                  :disabled="systemAdminDisabled || disabled"
                  @change="(value) => handleRowAuthChange(value, rowIndex)"
                />
              </div>
            </template>
          </a-table-column>
        </template>
      </a-table>
    </div>
    <div
      v-if="props.showBottom && props.current.id !== 'admin' && !systemAdminDisabled"
      v-permission="props.savePermission || []"
      class="footer"
    >
      <ms-button :disabled="!canSave" @click="handleReset">{{ t('system.userGroup.reset') }}</ms-button>
      <a-button v-permission="props.savePermission || []" :disabled="!canSave" type="primary" @click="handleSave">
        {{ t('system.userGroup.save') }}
      </a-button>
    </div>
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
  import useLicenseStore from '@/store/modules/setting/license';

  import {
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
    fixed?: boolean;
    render?: (record: TableData) => VNodeChild;
    isLastLeftFixed?: boolean;
  }

  const props = withDefaults(
    defineProps<{
      current: CurrentUserGroupItem;
      savePermission?: string[];
      showBottom?: boolean;
      disabled?: boolean;
      scroll?: {
        x?: number | string;
        y?: number | string;
        minWidth?: number | string;
        maxHeight?: number | string;
      };
    }>(),
    {
      showBottom: true,
      disabled: false,
      scroll() {
        return {
          x: '800px',
          y: '100%',
        };
      },
    }
  );

  const licenseStore = useLicenseStore();

  const systemType = inject<AuthScopeEnum>('systemType');

  const loading = ref(false);

  // 表格的总全选
  const allChecked = ref(false);
  const allIndeterminate = ref(false);

  const tableData = ref<AuthTableItem[]>();
  // 是否可以保存
  const canSave = ref(false);

  // 不可编辑的权限
  const systemAdminDisabled = computed(() => {
    const adminArr = ['admin', 'org_admin', 'project_admin'];
    const { id } = props.current;
    if (adminArr.includes(id)) {
      // 系统管理员,组织管理员，项目管理员都不可编辑
      return true;
    }

    return props.disabled;
  });

  const dataSpanMethod = (data: {
    record: TableData;
    column: TableColumnData | TableOperationColumn;
    rowIndex: number;
    columnIndex: number;
  }) => {
    const { record, column } = data;
    if ((column as TableColumnData).dataIndex === 'ability') {
      return {
        rowspan: record.rowSpan,
      };
    }
  };

  const { t } = useI18n();

  /**
   * 生成数据
   * @param item
   * @param type
   */
  const makeData = (item: UserGroupAuthSetting) => {
    const result: AuthTableItem[] = [];
    item.children?.forEach((child, index) => {
      if (!child.license || (child.license && licenseStore.hasLicense())) {
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
          rowSpan: index === 0 ? item.children?.length || 1 : undefined,
        });
      }
    });
    return result;
  };
  // 转换数据 计算系统，组织，项目的合并行数
  const transformData = (data: UserGroupAuthSetting[]) => {
    const result: AuthTableItem[] = [];
    data.forEach((item) => {
      result.push(...makeData(item));
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
  // 当选中某个权限值时判断当前选中的列中有没有read权限
  const setAutoRead = (record: TableData, currentValue: string) => {
    if (!record.perChecked.includes(currentValue)) {
      // 如果当前没有选中则执行自动添加查询权限逻辑
      // 添加权限值
      record.perChecked.push(currentValue);
      const preStr = currentValue.split(':')[0];
      const postStr = currentValue.split(':')[1];
      const lastEditStr = currentValue.split('+')[1]; // 编辑类权限通过+号拼接
      const existRead = record.perChecked.some(
        (item: string) => item.split(':')[0] === preStr && item.split(':')[1] === 'READ'
      );
      const existCreate = record.perChecked.some(
        (item: string) => item.split(':')[0] === preStr && item.split(':')[1] === 'ADD'
      );
      if (!existRead && postStr !== 'READ') {
        record.perChecked.push(`${preStr}:READ`);
      }
      if (!existCreate && lastEditStr === 'IMPORT') {
        // 勾选导入时自动勾选新增和查询
        record.perChecked.push(`${preStr}:ADD`);
        record.perChecked.push(`${preStr}:READ+UPDATE`);
      }
    } else {
      // 删除权限值
      const preStr = currentValue.split(':')[0];
      const postStr = currentValue.split(':')[1];
      if (postStr === 'READ') {
        // 当前是查询 那 移除所有相关的
        record.perChecked = record.perChecked.filter((item: string) => !item.includes(preStr));
      } else {
        record.perChecked.splice(record.perChecked.indexOf(currentValue), 1);
      }
    }
  };

  // 表格第三列的复选框change事件
  const handleCellAuthChange = (
    values: (string | number | boolean)[],
    rowIndex: number,
    record: TableData,
    e: Event
  ) => {
    setAutoRead(record, (e.target as HTMLInputElement).value);
    if (!tableData.value) return;
    const tmpArr = tableData.value;
    const length = tmpArr[rowIndex].permissions?.length || 0;
    if (record.perChecked.length === length) {
      tmpArr[rowIndex].enable = true;
      tmpArr[rowIndex].indeterminate = false;
    } else if (record.perChecked.length === 0) {
      tmpArr[rowIndex].enable = false;
      tmpArr[rowIndex].indeterminate = false;
    } else {
      tmpArr[rowIndex].enable = false;
      tmpArr[rowIndex].indeterminate = true;
    }
    handleAllChange();
  };

  // 初始化数据
  const initData = async (id: string) => {
    try {
      loading.value = true;
      tableData.value = []; // 重置数据，可以使表格滚动条重新计算
      let res: UserGroupAuthSetting[] = [];
      if (systemType === AuthScopeEnum.SYSTEM) {
        res = await getGlobalUSetting(id);
      } else if (systemType === AuthScopeEnum.ORGANIZATION) {
        res = await getOrgUSetting(id);
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
      canSave.value = false;
      Message.success(t('common.saveSuccess'));
      initData(props.current.id);
    } catch (error) {
      // eslint-disable-next-line no-console
      console.log(error);
    }
  };

  // 恢复默认值
  const handleReset = () => {
    if (props.current.id) {
      initData(props.current.id);
    }
  };

  watchEffect(() => {
    if (props.current.id) {
      initData(props.current.id);
    }
  });
  defineExpose({
    canSave,
    handleSave,
    handleReset,
  });
</script>

<style scoped lang="less">
  .group-auth-table {
    @apply flex-1 overflow-hidden;

    padding: 0 16px 16px;
    div,
    span {
      color: var(--color-text-1);
    }
    :deep(.arco-table-container) {
      border-top: 1px solid var(--color-text-n8) !important;
      border-left: 1px solid var(--color-text-n8) !important;
    }
    :deep(.arco-table-th-title) {
      width: 100%;
    }
    :deep(.arco-table-th) {
      background-color: var(--color-text-n9);
      line-height: normal;
    }
  }
  :deep(.auth-type-class) {
    background-color: var(--color-text-n9);
  }
  .footer {
    display: flex;
    justify-content: flex-end;
    padding: 24px;
    background-color: var(--color-text-fff);
    box-shadow: 0 -1px 4px rgb(2 2 2 / 10%);
    gap: 16px;
  }
</style>
