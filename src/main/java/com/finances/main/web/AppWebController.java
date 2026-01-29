package com.finances.main.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controlador web para servir la interfaz básica de la aplicación.
 */
@Controller
@RequestMapping("/app")
public class AppWebController {

    /**
     * Redirige la ruta /app hacia el archivo estático principal.
     */
    @GetMapping({"", "/"})
    public String index() {
        return "forward:/app/index.html";
    }
}
