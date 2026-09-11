package br.com.conectasaude.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "notificacoes")
public class Notificacao {

    @Id
    private String id;
    private String title;
    private String message;
    private String time;

    public Notificacao() {}

    public Notificacao(String id, String title, String message, String time) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.time = time;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }
}
