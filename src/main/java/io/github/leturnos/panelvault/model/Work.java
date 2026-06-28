package io.github.leturnos.panelvault.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.util.Objects;

@Entity
public class Work {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Enumerated(EnumType.STRING)
    private WorkType type;

    private String publisher;
    private String author;
    private Integer totalVolumes;

    @Enumerated(EnumType.STRING)
    private WorkStatus status;

    private String coverUrl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public WorkType getType() {
        return type;
    }

    public void setType(WorkType type) {
        this.type = type;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Integer getTotalVolumes() {
        return totalVolumes;
    }

    public void setTotalVolumes(Integer totalVolumes) {
        this.totalVolumes = totalVolumes;
    }

    public WorkStatus getStatus() {
        return status;
    }

    public void setStatus(WorkStatus status) {
        this.status = status;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Work work)) return false;
        return Objects.equals(id, work.id) && Objects.equals(title, work.title) && Objects.equals(type, work.type) && Objects.equals(publisher, work.publisher) && Objects.equals(author, work.author) && Objects.equals(totalVolumes, work.totalVolumes) && Objects.equals(status, work.status) && Objects.equals(coverUrl, work.coverUrl);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, title, type, publisher, author, totalVolumes, status, coverUrl);
    }
}
