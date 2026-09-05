package br.com.conectasaude.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AcessoController {

    @GetMapping("/api/admin/teste")
    public Map<String, String> admin() {
        return Map.of(
                "acesso", "permitido",
                "perfil", "ADMIN"
        );
    }

    @GetMapping("/api/medico/teste")
    public Map<String, String> medico() {
        return Map.of(
                "acesso", "permitido",
                "perfil", "MEDICO"
        );
    }

    @GetMapping("/api/paciente/teste")
    public Map<String, String> paciente() {
        return Map.of(
                "acesso", "permitido",
                "perfil", "PACIENTE"
        );
    }

    @GetMapping("/api/auditoria/teste")
    public Map<String, String> auditoria() {
        return Map.of(
                "acesso", "permitido",
                "perfil", "AUDITOR ou ADMIN"
        );
    }
}