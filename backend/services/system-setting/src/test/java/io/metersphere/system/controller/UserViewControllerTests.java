package io.metersphere.system.controller;

import io.metersphere.sdk.constants.InternalUser;
import io.metersphere.sdk.util.BeanUtils;
import io.metersphere.system.base.BaseTest;
import io.metersphere.system.constants.InternalUserView;
import io.metersphere.system.constants.UserViewType;
import io.metersphere.system.domain.UserView;
import io.metersphere.system.dto.UserViewDTO;
import io.metersphere.system.dto.UserViewListGroupedDTO;
import io.metersphere.system.dto.request.UserViewAddRequest;
import io.metersphere.system.dto.request.UserViewUpdateRequest;
import io.metersphere.sdk.dto.CombineCondition;
import io.metersphere.sdk.dto.CombineSearch;
import io.metersphere.system.mapper.UserViewMapper;
import io.metersphere.system.service.UserViewService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;

import static io.metersphere.system.controller.result.SystemResultCode.USER_VIEW_EXIST;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserViewControllerTests extends BaseTest {

    private final String BASE_URL = "/user-view/functional-case/";
    private final String LIST = "list?scopeId={0}";
    private final String GROUPED_LIST = "grouped/list?scopeId={0}";

    private static UserViewDTO addUserViewDTO;
    private static UserViewDTO anotherAddUserViewDTO;

    @Resource
    private UserViewService userViewService;
    @Resource
    private UserViewMapper userViewMapper;

    @Override
    public String getBasePath() {
        return BASE_URL;
    }

    @Test
    @Order(1)
    public void emptyList() throws Exception {
        // @@请求成功
        MvcResult mvcResult = this.requestGetWithOkAndReturn(LIST, DEFAULT_PROJECT_ID);
        List<UserView> result = getResultDataArray(mvcResult, UserView.class);
        Assertions.assertEquals(result.size(), 3);
        UserView allData = result.get(0);
        Assertions.assertEquals(allData.getName(), "全部数据");
        Assertions.assertEquals(allData.getId(), InternalUserView.ALL_DATA.name().toLowerCase());

        UserView myFollow = result.get(1);
        Assertions.assertEquals(myFollow.getName(), "我关注的");
        Assertions.assertEquals(myFollow.getId(), InternalUserView.MY_FOLLOW.name().toLowerCase());

        UserView myCreate = result.get(2);
        Assertions.assertEquals(myCreate.getName(), "我创建的");
        Assertions.assertEquals(myCreate.getId(), InternalUserView.MY_CREATE.name().toLowerCase());

        for (UserView item : result) {
            Assertions.assertEquals(item.getScopeId(), DEFAULT_PROJECT_ID);
            Assertions.assertEquals(item.getUserId(), InternalUser.ADMIN.getValue());
            Assertions.assertEquals(item.getSearchMode(), CombineSearch.SearchMode.AND.name());
        }
    }

    @Test
    @Order(1)
    public void emptyGroupedList() throws Exception {
        // @@请求成功
        MvcResult mvcResult = this.requestGetWithOkAndReturn(GROUPED_LIST, DEFAULT_PROJECT_ID);
        UserViewListGroupedDTO result = getResultData(mvcResult, UserViewListGroupedDTO.class);
        Assertions.assertEquals(result.getInternalViews().size(), 3);
    }

    @Test
    @Order(2)
    public void add() throws Exception {
        UserViewAddRequest request = new UserViewAddRequest();
        request.setName(UUID.randomUUID().toString());
        request.setScopeId(DEFAULT_PROJECT_ID);
        request.setSearchMode(CombineSearch.SearchMode.AND.name());
        CombineCondition condition = new CombineCondition();
        condition.setCustomField(true);
        condition.setName(UUID.randomUUID().toString());
        condition.setOperator(CombineCondition.CombineConditionOperator.EQUALS.name());
        condition.setValue(UUID.randomUUID().toString());
        CombineCondition arrayCondition = new CombineCondition();
        arrayCondition.setName(UUID.randomUUID().toString());
        arrayCondition.setOperator(CombineCondition.CombineConditionOperator.IN.name());
        arrayCondition.setValue(List.of(UUID.randomUUID().toString()));
        CombineCondition intCondition = new CombineCondition();
        intCondition.setName(UUID.randomUUID().toString());
        intCondition.setOperator(CombineCondition.CombineConditionOperator.GT.name());
        intCondition.setValue(1130L);
        CombineCondition flotCondition = new CombineCondition();
        flotCondition.setName(UUID.randomUUID().toString());
        flotCondition.setOperator(CombineCondition.CombineConditionOperator.GT.name());
        flotCondition.setValue(Double.valueOf(1130.1));
        CombineCondition blankCondition = new CombineCondition();
        blankCondition.setName(UUID.randomUUID().toString());
        request.setConditions(List.of(condition, arrayCondition, intCondition, flotCondition, blankCondition));

        // @@请求成功
        MvcResult mvcResult = this.requestPostWithOkAndReturn(DEFAULT_ADD, request);
        addUserViewDTO = getResultData(mvcResult, UserViewDTO.class);
        addUserViewDTO.setConditions(request.getConditions());
        UserViewDTO userViewDTO = userViewService.get(addUserViewDTO.getId(), UserViewType.FUNCTIONAL_CASE, InternalUser.ADMIN.getValue());
        Assertions.assertEquals(request, BeanUtils.copyBean(new UserViewAddRequest(), userViewDTO));
        Assertions.assertEquals(request.getConditions(), userViewDTO.getConditions());

        // @@重名校验异常
        assertErrorCode(this.requestPost(DEFAULT_ADD, request), USER_VIEW_EXIST);

        request.setName(UUID.randomUUID().toString());
        request.setConditions(null);
        mvcResult = this.requestPostWithOkAndReturn(DEFAULT_ADD, request);
        anotherAddUserViewDTO = getResultData(mvcResult, UserViewDTO.class);
    }

    @Test
    @Order(3)
    public void update() throws Exception {
        UserViewUpdateRequest request = BeanUtils.copyBean(new UserViewUpdateRequest(), addUserViewDTO);
        request.setSearchMode(CombineSearch.SearchMode.OR.name());
        request.getConditions().get(0).setName(UUID.randomUUID().toString());

        // @@请求成功
        MvcResult mvcResult = this.requestPostWithOkAndReturn(DEFAULT_UPDATE, request);
        request.setName(UUID.randomUUID().toString());
        mvcResult = this.requestPostWithOkAndReturn(DEFAULT_UPDATE, request);
        addUserViewDTO = getResultData(mvcResult, UserViewDTO.class);
        addUserViewDTO.setConditions(request.getConditions());
        UserViewDTO userViewDTO = userViewService.get(addUserViewDTO.getId(), UserViewType.FUNCTIONAL_CASE, InternalUser.ADMIN.getValue());
        Assertions.assertEquals(request, BeanUtils.copyBean(new UserViewUpdateRequest(), userViewDTO));
        Assertions.assertEquals(request.getConditions(), userViewDTO.getConditions());

        // @@ Conditions 为 null
        request.setId(anotherAddUserViewDTO.getId());
        assertErrorCode(this.requestPost(DEFAULT_UPDATE, request), USER_VIEW_EXIST);
        request.setName(UUID.randomUUID().toString());
        request.setConditions(null);
        this.requestPostWithOkAndReturn(DEFAULT_UPDATE, request);
    }

    @Test
    @Order(4)
    public void get() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(DEFAULT_GET, addUserViewDTO.getId());
        UserViewDTO resultData = getResultData(mvcResult, UserViewDTO.class);
        resultData.getConditions().get(2).setValue(Long.valueOf(resultData.getConditions().get(2).getValue().toString()));
        resultData.getConditions().get(3).setValue(Double.valueOf(resultData.getConditions().get(3).getValue().toString()));
        Assertions.assertEquals(resultData, addUserViewDTO);
        Assertions.assertEquals(resultData.getConditions(), addUserViewDTO.getConditions());

        mvcResult = this.requestGetWithOkAndReturn(DEFAULT_GET, InternalUserView.ALL_DATA.name());
        resultData = getResultData(mvcResult, UserViewDTO.class);
        Assertions.assertEquals(resultData.getName(), "全部数据");
        Assertions.assertEquals(resultData.getId(), InternalUserView.ALL_DATA.name().toLowerCase());
        Assertions.assertEquals(resultData.getConditions().size(), 0);

        mvcResult = this.requestGetWithOkAndReturn(DEFAULT_GET, InternalUserView.MY_CREATE.name());
        resultData = getResultData(mvcResult, UserViewDTO.class);
        Assertions.assertEquals(resultData.getId(), InternalUserView.MY_CREATE.name().toLowerCase());
        Assertions.assertEquals(resultData.getConditions().size(), 0);
    }

    @Test
    @Order(4)
    public void groupedList() throws Exception {
        MvcResult mvcResult = this.requestGetWithOkAndReturn(GROUPED_LIST, DEFAULT_PROJECT_ID);
        UserViewListGroupedDTO result = getResultData(mvcResult, UserViewListGroupedDTO.class);
        Assertions.assertEquals(result.getInternalViews().size(), 3);
        Assertions.assertEquals(result.getCustomViews().size(), 2);
        Assertions.assertEquals(result.getCustomViews().getLast(), BeanUtils.copyBean(new UserView(), addUserViewDTO));
    }

    @Test
    @Order(5)
    public void delete() throws Exception {
        this.requestGetWithOkAndReturn(DEFAULT_DELETE, addUserViewDTO.getId());
        Assertions.assertNull(userViewMapper.selectByPrimaryKey(addUserViewDTO.getId()));
    }
}
