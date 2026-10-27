package io.metersphere.system.service;

import io.metersphere.sdk.constants.TemplateScene;
import io.metersphere.system.domain.Template;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

class CreateTemplateResourceServiceTests {

    @Test
    void clearsValidationStateWhenTemplateCopyFails() {
        Template template = new Template();
        template.setId("org-template");
        BaseTemplateService baseTemplateService = mock(BaseTemplateService.class);
        BaseTemplateCustomFieldService baseTemplateCustomFieldService = mock(BaseTemplateCustomFieldService.class);
        BaseCustomFieldService baseCustomFieldService = mock(BaseCustomFieldService.class);
        CreateTemplateResourceService service = spy(new CreateTemplateResourceService());
        service.baseTemplateService = baseTemplateService;
        service.baseTemplateCustomFieldService = baseTemplateCustomFieldService;
        service.baseCustomFieldService = baseCustomFieldService;

        when(baseTemplateService.getTemplates("org", TemplateScene.FUNCTIONAL.name())).thenReturn(List.of(template));
        when(baseTemplateCustomFieldService.getByTemplateIds(List.of("org-template"))).thenReturn(List.of());
        when(baseCustomFieldService.getByScopeIdAndScene("org", TemplateScene.FUNCTIONAL.name())).thenReturn(List.of());
        doThrow(new IllegalStateException("template copy failed")).when(service)
                .addRefProjectTemplate(anyString(), any(Template.class), anyList(), isNull());

        assertThrows(IllegalStateException.class, () -> ReflectionTestUtils.invokeMethod(
                service, "createProjectTemplate", "project", "org", TemplateScene.FUNCTIONAL));
        assertNull(BaseTemplateCustomFieldService.validateDefaultValue.get());
    }
}
