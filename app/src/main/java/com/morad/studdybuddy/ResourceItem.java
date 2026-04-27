package com.morad.studdybuddy;

public class ResourceItem {

    private String id;
    private String title;
    private String link;
    private String description;
    private String uploadedBy;

    public ResourceItem() {
        // Required empty constructor for Firestore
    }

    public ResourceItem(String title, String link, String description, String uploadedBy) {
        this.title = title;
        this.link = link;
        this.description = description;
        this.uploadedBy = uploadedBy;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public String getDescription() {
        return description;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }
}
