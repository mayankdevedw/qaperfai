package com.qaperf.ai.learn;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.qaperf.ai.parser.ThreadMonitor;
import com.qaperf.ai.utils.Config;
import com.qaperf.ai.utils.ConfigLoader;
import com.qaperf.ai.utils.TransactionUtils;
import com.qaperf.ai.xpathparser.DataModel;
import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.Node;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TreePathStructureLearn implements Runnable {
    private Element element;
    // Create ObjectMapper
    ObjectMapper mapper = new ObjectMapper();
    private String fileName;
    Config config = ConfigLoader.loadConfig();
    private DataModel model;
    private String webPage;
    public TreePathStructureLearn(Element element, DataModel model, String fileName, String webPage){
        this.element = element;
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.model = model;
        this.fileName = fileName;
        this.webPage = webPage;

    }


    private void buildStructure(List<ElementStructure> strcutures, int count, ElementStructure baseStructure){
        if(baseStructure == null || count >= strcutures.size())
            return;

        baseStructure.setParentElementStructure(strcutures.get(count));
        buildStructure(strcutures, count+1, baseStructure.getParentElementStructure());

    }

    private List<ElementStructure> getSiblingNodes(Element parentElement){
      List<Node> nodes=  parentElement.selectNodes("*");
      List<ElementStructure> siblingStructures=new ArrayList<>();
      for (int i=0;i< nodes.size();i++){
          if (nodes.get(i) instanceof Element){
              Element element = (Element) nodes.get(i);
              if(element.equals(this.element))
                  continue;

              ElementStructure sibling = new ElementStructure();
              sibling.setName(element.getName());
              Iterator<Attribute> atIr = element.attributeIterator();
              while ((atIr.hasNext())) {
                  Attribute attribute = atIr.next();
                  sibling.getAttributes().put(attribute.getName(), attribute.getValue());

              }
              siblingStructures.add(sibling);
          }
      }
      return siblingStructures;

    }



    public void buildStructure(){
        List<ElementStructure> siblings = getSiblingNodes(element.getParent());
        List<ElementStructure> structures=new ArrayList<>();
        while (!this.element.getName().equals("body")) {
            ElementStructure structure=new ElementStructure();
            structure.setName(element.getName());
            structure.setUniquePath(element.getUniquePath());
            Iterator<Attribute> atIr = element.attributeIterator();
            while ((atIr.hasNext())) {
                Attribute attribute = atIr.next();
                structure.getAttributes().put(attribute.getName(), attribute.getValue());

            }
            structures.add(structure);

            this.element = this.element.getParent();

        }

        ElementStructure bodystructure=new ElementStructure();
        bodystructure.setName(element.getName());
        bodystructure.setUniquePath(element.getUniquePath());
        structures.add(bodystructure);
        structures.get(0).setSiblingStructures(siblings);
        buildStructure(structures,1, structures.get(0));

        try {
            TransactionUtils utils=new TransactionUtils();
            String final_path = config.getBaseDir()+ File.separator +"TestSuites"+File.separator+webPage;
            utils.createDirectory(final_path);
            FileOutputStream fileOutputStream = new FileOutputStream(final_path+File.separator+fileName+".json");
            mapper.writeValue(fileOutputStream, structures.get(0));
            fileOutputStream.close();
        } catch (JsonProcessingException | FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    @Override
    public void run() {
        buildStructure();
        ThreadMonitor.decrement();
    }
}
