package com.morad.studdybuddy;

public class Module {

    private String moduleCode;
    private String moduleName;
    private String moduleDescription;
    private String subjectArea;

    public Module() {
        // Required empty constructor for Firestore
    }

    public Module(String moduleCode, String moduleName, String moduleDescription, String subjectArea) {
        this.moduleCode = moduleCode;
        this.moduleName = moduleName;
        this.moduleDescription = moduleDescription;
        this.subjectArea = subjectArea;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public String getModuleName() {
        return moduleName;
    }

    public String getModuleDescription() {
        return moduleDescription;
    }

    public String getSubjectArea() {
        return subjectArea;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public void setModuleDescription(String moduleDescription) {
        this.moduleDescription = moduleDescription;
    }

    public void setSubjectArea(String subjectArea) {
        this.subjectArea = subjectArea;
    }
}