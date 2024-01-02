package io.metersphere.system.controller;

import io.metersphere.system.base.BaseTest;
import io.metersphere.system.dto.RelationshipEdgeDTO;
import io.metersphere.system.utils.RelationshipEdgeUtils;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;

import java.util.ArrayList;
import java.util.List;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class RelationshipEdgeTests extends BaseTest {

    @Test
    @Order(1)
    @Sql(scripts = {"/dml/init_relationship_edge_test.sql"}, config = @SqlConfig(encoding = "utf-8", transactionMode = SqlConfig.TransactionMode.ISOLATED))
    public void testRelatePage() throws Exception {
        RelationshipEdgeDTO dto = new RelationshipEdgeDTO("1", "2");
        List<RelationshipEdgeDTO> list = new ArrayList<>();
        list.add(dto);
        RelationshipEdgeUtils.checkEdge(list);

        RelationshipEdgeUtils.updateGraphId("1", this::getGraphId, this::getEdgeByGraphId, this::update);
    }

    private void update(List list, String s) {
    }

    private List<RelationshipEdgeDTO> getEdgeByGraphId(String s) {
        RelationshipEdgeDTO dto = new RelationshipEdgeDTO("1", "2");
        List<RelationshipEdgeDTO> list = new ArrayList<>();
        list.add(dto);
        return list;
    }

    private RelationshipEdgeDTO getGraphId(String s) {
        return new RelationshipEdgeDTO("1", "2");
    }
}
