export function getPageDate(response, page) {
  let data = response.data;
  page.total = data.itemCount;
  return data.listObject;
}
