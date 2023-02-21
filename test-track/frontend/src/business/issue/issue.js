export function setIssuePlatformComponent(platformOptions, components) {
  components.forEach(item => {
    if (item.key === 'platform') {
      item.options = [
        {label: "Tapd", value: "Tapd"},
        {label: "Local", value: "Local"},
        {label: "Azure Devops", value: "AzureDevops"}
      ];
      platformOptions.forEach(option => {
        item.options.push({label: option.text, value: option.value});
      });
    }
  });
}
