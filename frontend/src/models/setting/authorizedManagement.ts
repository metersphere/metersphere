export interface License {
  corporation: string; // 客户名称
  expired: string; // 授权时间
  product: string; // 产品名称
  edition: string; // 版本
  licenseVersion: string; // 授权版本
  count: number; // 授权数量
}

export interface LicenseInfo {
  status: string | null;
  license: License;
}
