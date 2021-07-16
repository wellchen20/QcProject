package com.mtkj.utils.entity;

import com.google.gson.JsonElement;

import java.io.Serializable;
import java.util.List;

public class FilesEntity implements Serializable {

    private List<JsonElement> fileId;

    public List<JsonElement> getFileId() {
        return fileId;
    }

    public void setFileId(List<JsonElement> fileId) {
        this.fileId = fileId;
    }

}
