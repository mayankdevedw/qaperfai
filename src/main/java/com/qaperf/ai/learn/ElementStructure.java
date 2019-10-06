package com.qaperf.ai.learn;

import org.dom4j.Element;

import java.util.HashMap;
import java.util.List;

public class ElementStructure {

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public HashMap<String, String> getAttributes() {
        return attributes;
    }

    private HashMap<String, String> attributes = new HashMap<>();


    public String getUniquePath() {
        return uniquePath;
    }

    public void setUniquePath(String uniquePath) {
        this.uniquePath = uniquePath;
    }

    private String uniquePath;

    public ElementStructure getParentElementStructure() {
        return parentElementStructure;
    }

    public void setParentElementStructure(ElementStructure parentElementStructure) {
        this.parentElementStructure = parentElementStructure;
    }

    private ElementStructure parentElementStructure;

    public List<ElementStructure> getSiblingStructures() {
        return siblingStructures;
    }

    private List<ElementStructure> siblingStructures;



    public void setSiblingStructures(List<ElementStructure> siblingStructures) {
        this.siblingStructures = siblingStructures;
    }
}
