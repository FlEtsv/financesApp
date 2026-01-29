package com.finances.main.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias del controlador web.
 */
class AppWebControllerTest {

    @Test
    void returnsForwardToStaticIndex() {
        AppWebController controller = new AppWebController();

        String viewName = controller.index();

        assertThat(viewName).isEqualTo("forward:/app/index.html");
    }
}
