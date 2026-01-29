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

    /**
     * Redirige a la página de movimientos.
     */
    @GetMapping("/movimientos")
    public String movements() {
        return "forward:/app/movimientos.html";
    }

    /**
     * Redirige a la página de objetivos financieros.
     */
    @GetMapping("/objetivos")
    public String goals() {
        return "forward:/app/objetivos.html";
    }

    /**
     * Redirige a la página de gastos e ingresos fijos.
     */
    @GetMapping("/fijos")
    public String fixedMovements() {
        return "forward:/app/fijos.html";
    }
}
