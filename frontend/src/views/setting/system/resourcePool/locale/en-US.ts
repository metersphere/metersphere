export default {
  'system.resourcePool.createPool': 'Add resource pool',
  'system.resourcePool.searchPool': 'Search by name',
  'system.resourcePool.delete': 'Delete',
  'system.resourcePool.tableEnable': 'Enable',
  'system.resourcePool.tableDisable': 'Disable',
  'system.resourcePool.editPool': 'Edit',
  'system.resourcePool.tableColumnName': 'Name',
  'system.resourcePool.tableColumnStatus': 'Status',
  'system.resourcePool.tableColumnDescription': 'Description',
  'system.resourcePool.tableColumnType': 'Type',
  'system.resourcePool.tableColumnCreateTime': 'CreateTime',
  'system.resourcePool.tableColumnUpdateTime': 'UpdateTime',
  'system.resourcePool.tableColumnActions': 'Actions',
  'system.resourcePool.enablePoolSuccess': 'Enabled successfully',
  'system.resourcePool.disablePoolTip': 'About to disable resource pool `{name}`',
  'system.resourcePool.disablePoolContent': 'When disabled, tests using this resource pool will stop executing',
  'system.resourcePool.disablePoolConfirm': 'Confirm',
  'system.resourcePool.disablePoolCancel': 'Cancel',
  'system.resourcePool.disablePoolSuccess': 'Disabled successfully',
  'system.resourcePool.deletePoolTip': 'Are you sure to delete the `{name}` resource?',
  'system.resourcePool.deletePoolContentUsed':
    'If this resource pool has been used, and related tests will stop immediately after deletion, please operate with caution!',
  'system.resourcePool.deletePoolContentUnuse': 'This resource pool is not in use. Are you sure to delete it?',
  'system.resourcePool.deletePoolConfirm': 'Confirm',
  'system.resourcePool.deletePoolCancel': 'Cancel',
  'system.resourcePool.deletePoolSuccess': 'Deleted successfully',
  'system.resourcePool.detailDesc': 'Description',
  'system.resourcePool.detailUrl': 'Intranet URL',
  'system.resourcePool.detailRange': 'Applied organization',
  'system.resourcePool.detailUse': 'Use',
  'system.resourcePool.detailMirror': 'Mirror',
  'system.resourcePool.detailJMHeap': 'JMeter HEAP',
  'system.resourcePool.detailType': 'Type',
  'system.resourcePool.detailResources': 'Added resource',
  'system.resourcePool.usePerformance': 'Performance test',
  'system.resourcePool.useAPI': 'API test',
  'system.resourcePool.useUI': ' UI test',
  'system.resourcePool.name': 'Resource pool name',
  'system.resourcePool.nameRequired': 'Please enter a resource pool name',
  'system.resourcePool.namePlaceholder': 'Please enter a resource pool name',
  'system.resourcePool.desc': 'Description',
  'system.resourcePool.descPlaceholder': 'Please describe the resource pool',
  'system.resourcePool.serverUrl': 'Intranet URL',
  'system.resourcePool.serverUrlTip':
    'When the resource pool is deployed on the intranet, the intranet address can be used',
  'system.resourcePool.rootUrlPlaceholder': 'MS deployment address',
  'system.resourcePool.orgRange': 'Applied organization',
  'system.resourcePool.orgAll': 'All organization',
  'system.resourcePool.orgSetup': 'Specified organization',
  'system.resourcePool.orgSelect': 'specified organization',
  'system.resourcePool.orgRequired': 'Please select at least one organization',
  'system.resourcePool.orgPlaceholder': 'Please select an organization',
  'system.resourcePool.orgRangeTip': 'This rule is common to new organizations',
  'system.resourcePool.use': 'Use',
  'system.resourcePool.useRequired': 'Please select at least one purpose',
  'system.resourcePool.mirror': 'Mirror',
  'system.resourcePool.mirrorPlaceholder': 'Please enter mirror image',
  'system.resourcePool.testHeap': 'JMeter HEAP',
  'system.resourcePool.testHeapPlaceholder': 'Please enter',
  'system.resourcePool.testHeapExample': 'For example:{heap}',
  'system.resourcePool.uiGrid': 'Selenium-grid',
  'system.resourcePool.uiGridPlaceholder': 'Please enter',
  'system.resourcePool.uiGridExample': 'For example:{grid}',
  'system.resourcePool.uiGridRequired': 'Please enter selenium-grid address',
  'system.resourcePool.girdConcurrentNumber': 'grid maximum number of threads',
  'system.resourcePool.type': 'Type',
  'system.resourcePool.addResource': 'Add resources',
  'system.resourcePool.singleAdd': 'Single add',
  'system.resourcePool.batchAdd': 'Batch add',
  'system.resourcePool.batchAddTipConfirm': 'Got it',
  'system.resourcePool.batchAddResource': 'Batch add resources',
  'system.resourcePool.changeAddTypeTip':
    'After switching, the content of the added resources will continue to appear in csv; the added resources can be modified in batches',
  'system.resourcePool.changeAddTypePopTitle': 'Toggle add resource type?',
  'system.resourcePool.ip': 'IP',
  'system.resourcePool.ipRequired': 'Please enter an IP address',
  'system.resourcePool.ipPlaceholder': 'Please enter an IP address',
  'system.resourcePool.port': 'Port',
  'system.resourcePool.portRequired': 'Please enter Port',
  'system.resourcePool.portPlaceholder': 'Please enter Port',
  'system.resourcePool.monitor': 'Monitor',
  'system.resourcePool.monitorRequired': 'Please enter Monitor',
  'system.resourcePool.monitorPlaceholder': 'Please enter Monitor',
  'system.resourcePool.concurrentNumber': 'Maximum concurrency',
  'system.resourcePool.concurrentNumberRequired': 'Please enter the maximum number of concurrency',
  'system.resourcePool.concurrentNumberMin': 'The maximum concurrent number must be greater than or equal to 1',
  'system.resourcePool.concurrentNumberPlaceholder': 'Please enter the maximum number of concurrency',
  'system.resourcePool.nodeResourceRequired': 'Please fill in the Node resources added in batches correctly',
  'system.resourcePool.nodeConfigEditorTip':
    'Writing format: IP, Port, maximum concurrent number; such as 192.168.1.52,8082,1',
  'system.resourcePool.testResourceDTO.ip': 'IP Address/Domain Name',
  'system.resourcePool.testResourceDTO.ipRequired': 'Please fill in IP address / domain name',
  'system.resourcePool.testResourceDTO.ipPlaceholder': 'example.com',
  'system.resourcePool.testResourceDTO.ipSubTip': 'Example: {ip} or {domain}',
  'system.resourcePool.testResourceDTO.token': 'Token',
  'system.resourcePool.testResourceDTO.tokenRequired': 'Please fill in the Token',
  'system.resourcePool.testResourceDTO.tokenPlaceholder': 'Please fill in the Token',
  'system.resourcePool.testResourceDTO.namespace': 'NameSpaces',
  'system.resourcePool.testResourceDTO.nameSpacesRequired': 'Please fill in the namespace',
  'system.resourcePool.testResourceDTO.nameSpacesPlaceholder':
    'To use the K8S resource pool, you need to deploy the Role.yaml file',
  'system.resourcePool.testResourceDTO.deployName': 'Deploy Name',
  'system.resourcePool.testResourceDTO.deployNameRequired': 'Please fill in Deploy Name',
  'system.resourcePool.testResourceDTO.deployNamePlaceholder':
    'To perform interface testing, a Daemonset.yaml or Deployment .yaml file is required',
  'system.resourcePool.testResourceDTO.apiTestImage': 'API mirroring',
  'system.resourcePool.testResourceDTO.apiTestImageRequired': 'Please fill in the API image',
  'system.resourcePool.testResourceDTO.apiTestImagePlaceholder': 'Please fill in the API image',
  'system.resourcePool.testResourceDTO.concurrentNumber': 'Maximum concurrency',
  'system.resourcePool.testResourceDTO.podThreads': 'Maximum number of threads per Pod',
  'system.resourcePool.testResourceDTO.downloadRoleYaml': 'Download YAML files',
  'system.resourcePool.testResourceDTO.downloadRoleYamlTip':
    'Please fill in the namespace before downloading the YAML file',
  'system.resourcePool.testResourceDTO.downloadDeployYamlTip':
    'Please fill in the namespace and Deploy Name before downloading the YAML file',
  'system.resourcePool.testResourceDTO.downloadDaemonsetYaml': 'Daemonset.yaml',
  'system.resourcePool.testResourceDTO.downloadDeploymentYaml': 'Deployment.yaml',
  'system.resourcePool.customJobTemplate': 'Custom Job Templates',
  'system.resourcePool.jobTemplate': 'Job Templates',
  'system.resourcePool.jobTemplateTip':
    'A Kubernetes job template is a text in YAML format, which is used to define the running parameters of the job. You can edit the job template here.',
  'system.resourcePool.jobTemplateReset': 'Reset Template',
  'system.resourcePool.addSuccess': 'Added resource pool successfully',
  'system.resourcePool.updateSuccess': 'Resource pool updated successfully',
  'system.resourcePool.atLeastOnePool': 'Reserve at least one resource pool',
  'system.resourcePool.add': 'Add',
  'system.resourcePool.addAndContinue': 'Save and continue adding',
};