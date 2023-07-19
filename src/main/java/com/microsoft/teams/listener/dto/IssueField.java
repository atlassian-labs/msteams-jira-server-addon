package com.microsoft.teams.listener.dto;

public class IssueField {

    private String fieldName;

    public IssueField(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public String toString() {
        return "IssueField{" +
                "fieldName='" + fieldName + '\'' +
                '}';
    }
}
