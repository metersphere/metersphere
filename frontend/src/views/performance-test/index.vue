<template>
  <div> <MsFormCreate :rule="rule" :option="options" /> </div
></template>

<script setup>
  import MsFormCreate from '@/components/pure/ms-form-create/formCreate.vue';

  const rule = ref([
    {
      type: 'input',
      field: 'name',
      title: '名称',
      info: '',
      _fc_drag_tag: 'input',
      hidden: false,
      display: true,
    },
    {
      type: 'input',
      field: 'comments',
      title: '描述',
      info: '',
      _fc_drag_tag: 'input',
      hidden: false,
      display: true,
    },
    {
      type: 'el-divider',
      props: {
        contentPosition: 'left',
      },
      wrap: {
        show: false,
      },
      native: false,
      children: ['目标Server配置'],
      _fc_drag_tag: 'el-divider',
      hidden: false,
      display: true,
    },
    {
      type: 'FcRow',
      children: [
        {
          type: 'col',
          props: {
            span: 12,
          },
          children: [
            {
              type: 'input',
              field: 'serverIp',
              title: 'IP',
              info: '',
              _fc_drag_tag: 'input',
              hidden: false,
              display: true,
            },
            {
              type: 'input',
              field: 'connTimeout',
              title: '连接超时时间',
              info: '',
              props: {
                type: 'number',
              },
              _fc_drag_tag: 'input',
              hidden: false,
              display: true,
            },
          ],
          _fc_drag_tag: 'col',
          hidden: false,
          display: true,
        },
        {
          type: 'col',
          props: {
            span: 12,
          },
          children: [
            {
              type: 'input',
              field: 'port',
              title: 'Port',
              info: '',
              _fc_drag_tag: 'input',
              hidden: false,
              display: true,
            },
            {
              type: 'input',
              field: 'resTimeout',
              title: '响应超时时间',
              info: '',
              props: {
                type: 'number',
              },
              _fc_drag_tag: 'input',
              hidden: false,
              display: true,
            },
          ],
          _fc_drag_tag: 'col',
          hidden: false,
          display: true,
        },
      ],
      _fc_drag_tag: 'row',
      hidden: false,
      display: true,
    },
    {
      type: 'FcRow',
      children: [
        {
          type: 'col',
          props: {
            span: 5,
          },
          children: [
            {
              type: 'input',
              field: 'soLinger',
              title: 'SO LINGER',
              info: '',
              _fc_drag_tag: 'input',
              hidden: false,
              display: true,
            },
          ],
          _fc_drag_tag: 'col',
          hidden: false,
          display: true,
        },
        {
          type: 'col',
          props: {
            span: 5,
          },
          children: [
            {
              type: 'input',
              field: 'eolByte',
              title: 'EOL',
              info: '',
              _fc_drag_tag: 'input',
              hidden: false,
              display: true,
            },
          ],
          _fc_drag_tag: 'col',
          hidden: false,
          display: true,
        },
        {
          type: 'col',
          props: {
            span: 5,
          },
          children: [
            {
              type: 'checkbox',
              field: 'closeConnection',
              title: '关闭连接',
              info: '',
              effect: {
                fetch: '',
              },
              options: [
                {
                  label: '',
                  value: '',
                },
              ],
              _fc_drag_tag: 'checkbox',
              hidden: false,
              display: true,
            },
          ],
          _fc_drag_tag: 'col',
          hidden: false,
          display: true,
        },
        {
          type: 'col',
          props: {
            span: 5,
          },
          children: [
            {
              type: 'checkbox',
              field: 'reUseConnection',
              title: 'Re-use',
              info: '',
              effect: {
                fetch: '',
              },
              options: [
                {
                  label: '',
                  value: '',
                },
              ],
              _fc_drag_tag: 'checkbox',
              hidden: false,
              display: true,
            },
          ],
          _fc_drag_tag: 'col',
          hidden: false,
          display: true,
        },
        {
          type: 'col',
          props: {
            span: 4,
          },
          children: [
            {
              type: 'checkbox',
              field: 'noDelay',
              title: '无延迟',
              info: '',
              effect: {
                fetch: '',
              },
              options: [
                {
                  label: '',
                  value: '',
                },
              ],
              _fc_drag_tag: 'checkbox',
              hidden: false,
              display: true,
            },
          ],
          _fc_drag_tag: 'col',
          hidden: false,
          display: true,
        },
      ],
      _fc_drag_tag: 'row',
      hidden: false,
      display: true,
    },
    {
      type: 'input',
      field: 'request',
      title: '发送内容',
      info: '',
      props: {
        type: 'textarea',
      },
      _fc_drag_tag: 'input',
      hidden: false,
      display: true,
    },
    {
      type: 'el-divider',
      props: {
        contentPosition: 'left',
      },
      wrap: {
        show: false,
      },
      native: false,
      children: ['客户端或登录配置'],
      _fc_drag_tag: 'el-divider',
      hidden: false,
      display: true,
    },
    {
      type: 'FcRow',
      children: [
        {
          type: 'col',
          props: {
            span: 8,
          },
          children: [
            {
              type: 'select',
              field: 'classname',
              title: '客户端类名',
              info: '',
              effect: {
                fetch: '',
              },
              options: [
                {
                  value: 'TCPClientImpl',
                  label: 'TCPClientImpl',
                },
                {
                  value: 'BinaryTCPClientImpl',
                  label: 'BinaryTCPClientImpl',
                },
                {
                  value: 'LengthPrefixedBinaryTCPClientImpl',
                  label: 'LengthPrefixedBinaryTCPClientImpl',
                },
              ],
              _fc_drag_tag: 'select',
              hidden: false,
              display: true,
              $required: '',
            },
          ],
          _fc_drag_tag: 'col',
          hidden: false,
          display: true,
        },
        {
          type: 'col',
          props: {
            span: 8,
          },
          children: [
            {
              type: 'input',
              field: 'username',
              title: '用户名',
              info: '',
              _fc_drag_tag: 'input',
              hidden: false,
              display: true,
            },
          ],
          _fc_drag_tag: 'col',
          hidden: false,
          display: true,
        },
        {
          type: 'col',
          props: {
            span: 8,
          },
          children: [
            {
              type: 'input',
              field: 'password',
              title: '密码',
              info: '',
              props: {
                type: 'password',
              },
              _fc_drag_tag: 'input',
              hidden: false,
              display: true,
            },
          ],
          _fc_drag_tag: 'col',
          hidden: false,
          display: true,
        },
      ],
      _fc_drag_tag: 'row',
      hidden: false,
      display: true,
    },
  ]);

  const options = {
    form: {
      labelPosition: 'right',
      size: 'small',
      labelWidth: '00px',
      hideRequiredAsterisk: false,
      showMessage: true,
      inlineMessage: false,
    },
    submitBtn: true,
    resetBtn: false,
  };
</script>
