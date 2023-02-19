export function setIssuePlatformComponent(platformOptions, components) {
  components.forEach(item => {
    if (item.key === 'platform') {
      item.options = [
        {label: "Tapd", value: "Tapd"},
        {label: "Local", value: "Local"},
        {label: "AzureDevops", value: "Azure Devops"}
      ];
      platformOptions.forEach(option => {
        item.options.push({label: option.text, value: option.value});
      });
    }
  });
}
