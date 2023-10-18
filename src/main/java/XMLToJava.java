import javafx.util.Pair;
import org.omg.spec.bpmn._20100524.di.BPMNEdge;
import org.omg.spec.bpmn._20100524.di.BPMNLabel;
import org.omg.spec.bpmn._20100524.di.BPMNPlane;
import org.omg.spec.bpmn._20100524.di.BPMNShape;
import org.omg.spec.bpmn._20100524.model.*;
import org.omg.spec.dd._20100524.dc.Bounds;
import org.omg.spec.dd._20100524.dc.Point;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.omg.spec.dd._20100524.di.DiagramElement;
import org.w3c.dom.*;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.bind.*;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;

public class XMLToJava {

    private final ObjectFactory objectFactory = new ObjectFactory();
    private boolean maxSet = false;
    private int maxY;
    private int minY;
    private List<Integer> movedYUp = new ArrayList<>();
    private List<Integer> movedYDown = new ArrayList<>();
    private List<String> whiteBoxes = new ArrayList<>();
    private List<TTask> toDelete = new ArrayList<>();
    private List<TTask> toRemove = new ArrayList<>();
    private String processID = "";
    private File file;
    private TDefinitions root;
    private Marshaller marshaller;

    public void convertXML(File xmlFile) throws Exception {
        toRemove.clear();
        file = xmlFile;
        try {
            // create a JAXB context instance for the BPMN model package
            JAXBContext jaxbProcessContext = JAXBContext.newInstance("org.omg.spec.bpmn._20100524.model");

            // create an unmarshall to convert XML to Java objects
            Unmarshaller unmarshaller = jaxbProcessContext.createUnmarshaller();
            marshaller = jaxbProcessContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // unmarshal the XML file into a Java object of the Definitions class
            root = (TDefinitions) JAXBIntrospector.getValue(unmarshaller.unmarshal(xmlFile));

            TProcess process = new TProcess();
            TCollaboration collaboration = new TCollaboration();

            if (root.getRootElement().size() == 2) {
                if (root.getRootElement().get(1).getValue() instanceof  TProcess) {
                    process = (TProcess) root.getRootElement().get(1).getValue();
                    collaboration = (TCollaboration) root.getRootElement().get(0).getValue();
                } else {
                    process = (TProcess) root.getRootElement().get(0).getValue();
                    collaboration = (TCollaboration) root.getRootElement().get(1).getValue();
                }
            } else if (root.getRootElement().size() == 1) {
                process = (TProcess) root.getRootElement().get(0).getValue();
                root.getRootElement().add(objectFactory.createCollaboration(collaboration));
            }

            processID = process.getId();


            BPMNPlane plane = root.getBPMNDiagram().get(0).getBPMNPlane();

            // create a DocumentBuilderFactory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

            // create a DocumentBuilder
            DocumentBuilder builder = factory.newDocumentBuilder();

            // parse the XML file and create a Document object
            Document doc = builder.parse(xmlFile);
            doc.getDocumentElement().normalize();

            // Get the process element
            Element rootElement = doc.getDocumentElement();
            NodeList processList = rootElement.getElementsByTagName("bpmndi:BPMNPlane");
            Node parent = processList.item(0);
            NodeList children = parent.getChildNodes();

            //get all diagram elements
            for (int j = 0; j < children.getLength(); j++) {
                if (children.item(j).getNodeType() == Node.ELEMENT_NODE) {
                    if (((Element) children.item(j)).getTagName().contains("Edge")) {
                        int[] x = new int[((Element) children.item(j)).getElementsByTagName("di:waypoint").getLength()];
                        int[] y = new int[((Element) children.item(j)).getElementsByTagName("di:waypoint").getLength()];
                        //get all waypoints
                        for (int k = 0; k < ((Element) children.item(j)).getElementsByTagName("di:waypoint").getLength(); k++) {
                            NamedNodeMap attributes = ((Element) children.item(j)).getElementsByTagName("di:waypoint").item(k).getAttributes();
                            x[k] = Integer.parseInt(attributes.getNamedItem("x").getNodeValue());
                            y[k] = Integer.parseInt(attributes.getNamedItem("y").getNodeValue());
                        }
                        plane.getDiagramElement().add(createEdge(children.item(j).getAttributes().getNamedItem("bpmnElement").getNodeValue(), x, y));
                    } else if (((Element) children.item(j)).getTagName().contains("Shape")) {
                        // Get the NamedNodeMap of attributes for the element
                        Element element = (Element) children.item(j);
                        NamedNodeMap attributes = element.getElementsByTagName("dc:Bounds").item(0).getAttributes();
                        int[] cord = new int[4];
                        cord[0] = (int) Double.parseDouble(attributes.getNamedItem("x").getNodeValue());
                        cord[1] = (int) Double.parseDouble(attributes.getNamedItem("y").getNodeValue());
                        cord[2] = (int) Double.parseDouble(attributes.getNamedItem("width").getNodeValue());
                        cord[3] = (int) Double.parseDouble(attributes.getNamedItem("height").getNodeValue());
                        if (element.getElementsByTagName("bpmndi:BPMNLabel").getLength() > 0) {
                            NamedNodeMap attributes2 = ((Element) element.getElementsByTagName("bpmndi:BPMNLabel").item(0)).getElementsByTagName("dc:Bounds").item(0).getAttributes();
                            int[] cord2 = new int[4];
                            cord2[0] = (int) Double.parseDouble(attributes2.getNamedItem("x").getNodeValue());
                            cord2[1] = (int) Double.parseDouble(attributes2.getNamedItem("y").getNodeValue());
                            cord2[2] = (int) Double.parseDouble(attributes2.getNamedItem("width").getNodeValue());
                            cord2[3] = (int) Double.parseDouble(attributes2.getNamedItem("height").getNodeValue());
                            plane.getDiagramElement().add(createShapeAndLabel(children.item(j).getAttributes().getNamedItem("bpmnElement").getNodeValue(), cord, cord2));
                        } else {
                            plane.getDiagramElement().add(createShape(children.item(j).getAttributes().getNamedItem("bpmnElement").getNodeValue(), cord[0], cord[1], cord[2], cord[3]));
                        }
                    }
                }
            }

            String error = checkIfModelIsValid(process.getFlowElement());

            if (!error.equals("")){
                throw new Exception(error);
            }

            for (int i = 0; i < process.getFlowElement().size(); i++) {
                if (process.getFlowElement().get(i).getValue() instanceof TBoundaryEvent) {
                   // separateBoundary((TBoundaryEvent) process.getFlowElement().get(i).getValue(), process.getFlowElement(), plane);
                }
            }

            conversion(process.getFlowElement(),collaboration,plane);
            //do a second check in case some elements have been skipped in the first iteration
            conversion(process.getFlowElement(),collaboration,plane);


            formatDiagram(process.getFlowElement(),collaboration,plane);

        } catch (JAXBException | SAXException e) {
            e.printStackTrace();
        }
    }

    public void exportXML(File exportFile) throws JAXBException {
        // Marshal the JAXBElement to an XML file
        String fileName = exportFile.getAbsolutePath();
        if (!fileName.endsWith(".bpmn"))
            fileName = fileName+".bpmn";

        marshaller.marshal(root, new File(fileName));
    }

    private String checkIfModelIsValid(List<JAXBElement<? extends TFlowElement>> flowElements) throws Exception {
        String errorMsg = "";
        for (int i = 0; i < flowElements.size(); i++) {
            if (flowElements.get(i).getValue() instanceof TSubProcess) {
                //checkIfModelIsValid(subProcess.getFlowElement());
                throw new Exception("Sub-Processes are currently not supported!");
            }
            if (flowElements.get(i).getValue().getClass() == TStartEvent.class) {
                for (QName key : flowElements.get(i).getValue().getOtherAttributes().keySet()) {
                    TStartEvent startEvent = (TStartEvent) flowElements.get(i).getValue();
                    //checks if it's of iot:type="start"
                    if (key.equals(new QName("http://some-company/schema/bpmn/iot", "type")) && flowElements.get(i).getValue().getOtherAttributes().get(key).equals("start")) {
                       //Do nothing
                    }
                }
            } else if (flowElements.get(i).getValue().getClass() == TActivity.class) {
                //TODO: Do Nothing?
            } else if (flowElements.get(i).getValue().getClass() == TBoundaryEvent.class) {
                //TODO: Do Nothing?
            } else if (flowElements.get(i).getValue().getClass() == TEndEvent.class) {
                for (QName key : flowElements.get(i).getValue().getOtherAttributes().keySet()) {
                    TEndEvent iotEnd = (TEndEvent) flowElements.get(i).getValue();
                    //checks if it's of iot:type="start"
                    if (key.equals(new QName("http://some-company/schema/bpmn/iot", "type")) && flowElements.get(i).getValue().getOtherAttributes().get(key).equals("end")) {
                        //Do nothing
                    }
                }
            } else if (flowElements.get(i).getValue() instanceof TCatchEvent) {
                for (QName key : flowElements.get(i).getValue().getOtherAttributes().keySet()) {
                    //checks if it's of iot:type="catch"
                    if (key.equals(new QName("http://some-company/schema/bpmn/iot", "type")) && flowElements.get(i).getValue().getOtherAttributes().get(key).equals("catch")) {
                        //do nothing
                    } else if (key.equals(new QName("http://some-company/schema/bpmn/iot", "type")) && flowElements.get(i).getValue().getOtherAttributes().get(key).equals("throw")) {
                        //do nothing
                    }
                }
            } else if (flowElements.get(i).getValue().getClass() == TDataObjectReference.class) {
                for (int keyItem = 0; keyItem < flowElements.get(i).getValue().getOtherAttributes().keySet().toArray().length; keyItem++) {
                    QName key = (QName) flowElements.get(i).getValue().getOtherAttributes().keySet().toArray()[keyItem];
                    if (key.equals(new QName("http://some-company/schema/bpmn/iot", "type")) && flowElements.get(i).getValue().getOtherAttributes().get(key).equals("obj")) {
                        flowElements.get(i).getValue().getOtherAttributes().remove(key);
                    } else if (key.equals(new QName("http://some-company/schema/bpmn/iot", "type")) && (flowElements.get(i).getValue().getOtherAttributes().get(key).equals("artefact-catch") || flowElements.get(i).getValue().getOtherAttributes().get(key).equals("artefact-catch-sub"))) {
                        TDataObjectReference reference = (TDataObjectReference) flowElements.get(i).getValue();
                        List<TTask> tasks = getAssociatedTasks(reference.getId(), false, flowElements);
                        if (tasks.size() == 1) {
                            errorMsg = checkValidityOfIoTDependantTask(tasks,reference,true,flowElements);
                        } else {
                            errorMsg = checkValidityOfIoTDependantTask(tasks, reference, true, flowElements);
                        }
                    } else if (key.equals(new QName("http://some-company/schema/bpmn/iot", "type")) && (flowElements.get(i).getValue().getOtherAttributes().get(key).equals("sensor") || flowElements.get(i).getValue().getOtherAttributes().get(key).equals("sensor-sub"))) {
                        TDataObjectReference reference = (TDataObjectReference) flowElements.get(i).getValue();
                        List<TTask> tasks = getAssociatedTasks(reference.getId(), false, flowElements);
                        errorMsg = checkValidityOfIoTDependantTask(tasks, reference, true, flowElements);
                    } else if (key.equals(new QName("http://some-company/schema/bpmn/iot", "type")) && (flowElements.get(i).getValue().getOtherAttributes().get(key).equals("actor") || flowElements.get(i).getValue().getOtherAttributes().get(key).equals("actor-sub"))) {
                        TDataObjectReference reference = (TDataObjectReference) flowElements.get(i).getValue();
                        List<TTask> tasks = getAssociatedTasks(reference.getId(), true, flowElements);
                        errorMsg = checkValidityOfIoTDependantTask(tasks, reference, false, flowElements);
                    }
                }
            }
        }
        return errorMsg;
    }

    private String checkValidityOfIoTDependantTask(List<TTask> tasks, TDataObjectReference reference, boolean isSensor, List<JAXBElement<? extends TFlowElement>> flowElements) throws Exception {
        Iterator<TTask> iterator = tasks.iterator();
        if (isSensor) {
            if (getAssociatedTasks(reference.getId(), false, flowElements).size() > 2){
                throw new Exception("A sensor has more than 2 associations, which is currently not supported by the prototype!");
            }
        } else {
            if (getAssociatedTasks(reference.getId(), true, flowElements).size() > 2){
                throw new Exception("An actuator has more than 2 associations, which is currently not supported by the prototype!");
            }
        }
        while (iterator.hasNext()) {
            //get the current task
            TTask task = iterator.next();
             if (task.getDataInputAssociation().size() > 2) {
                throw new Exception("Some tasks have too many sensor associations and are not supported by this prototype currently!");
            } else if (task.getDataOutputAssociation().size() > 1) {
                throw new Exception("Some tasks have too many actuator associations and are not supported by this prototype currently!");
            } else if (task.getDataInputAssociation().size() == 2){
                boolean sensorPresent = false;
                boolean artefactPresent = false;
                Object keyItem = ((TDataObjectReference)task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()).getOtherAttributes().keySet().toArray()[0];
                if (((TDataObjectReference)task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()).getOtherAttributes().get(keyItem).equals("artefact-catch") || ((TDataObjectReference)task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()).getOtherAttributes().get(keyItem).equals("artefact-catch-sub")) {
                    artefactPresent = true;
                } else if (((TDataObjectReference)task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()).getOtherAttributes().get(keyItem).equals("sensor") || ((TDataObjectReference)task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()).getOtherAttributes().get(keyItem).equals("sensor-sub")) {
                    sensorPresent = true;
                }
                keyItem = ((TDataObjectReference)task.getDataInputAssociation().get(1).getSourceRef().get(0).getValue()).getOtherAttributes().keySet().toArray()[0];
                if (((TDataObjectReference)task.getDataInputAssociation().get(1).getSourceRef().get(0).getValue()).getOtherAttributes().get(keyItem).equals("artefact-catch")  || ((TDataObjectReference)task.getDataInputAssociation().get(1).getSourceRef().get(0).getValue()).getOtherAttributes().get(keyItem).equals("artefact-catch-sub")) {
                    artefactPresent = true;
                } else if (((TDataObjectReference)task.getDataInputAssociation().get(1).getSourceRef().get(0).getValue()).getOtherAttributes().get(keyItem).equals("sensor") || ((TDataObjectReference)task.getDataInputAssociation().get(1).getSourceRef().get(0).getValue()).getOtherAttributes().get(keyItem).equals("sensor-sub")) {
                    sensorPresent = true;
                }
                if (!sensorPresent && !artefactPresent){
                    throw new Exception("Some tasks are associated with more than one sensor or sensor artefact which is not supported by this prototype currently!");
                }
            }
        }
        return "";
    }

    private void conversion(List<JAXBElement<? extends TFlowElement>> flowElements, TCollaboration collaboration, BPMNPlane plane){
        boolean reduceCounter = false;
        for (int i = 0; i < flowElements.size(); i++) {
            if (reduceCounter) {
                i--;
                reduceCounter = false;
            }
            if (flowElements.get(i).getValue().getClass() == TStartEvent.class) {
                for (QName key : flowElements.get(i).getValue().getOtherAttributes().keySet()) {
                    TStartEvent startEvent = (TStartEvent) flowElements.get(i).getValue();
                    //checks if it's of iot:type="start"
                    if (key.equals(new QName("http://some-company/schema/bpmn/iot", "type")) && flowElements.get(i).getValue().getOtherAttributes().get(key).equals("start")) {
                        TStartEvent condEvent = new TStartEvent();
                        condEvent.setId(flowElements.get(i).getValue().getId());
                        condEvent.setName(flowElements.get(i).getValue().getName());
                        for (int j = 0; j < startEvent.getOutgoing().size(); j++) {
                            condEvent.getOutgoing().add(startEvent.getOutgoing().get(j));
                        }
                        TConditionalEventDefinition conditionalEventDefinition = new TConditionalEventDefinition();
                        TExpression expression = new TExpression();
                        expression.setId("ConditionalEvent" + randomNumberSequence());
                        conditionalEventDefinition.setCondition(expression);
                        JAXBElement<TConditionalEventDefinition> eventDefinition = objectFactory.createConditionalEventDefinition(conditionalEventDefinition);
                        condEvent.getEventDefinition().add(eventDefinition);
                        JAXBElement<TStartEvent> startElement = objectFactory.createStartEvent(condEvent);
                        flowElements.remove(i);
                        flowElements.add(startElement);
                        reduceCounter = true;
                    }
                }
            } else if (flowElements.get(i).getValue() instanceof TSubProcess) {
                conversion(((TSubProcess) flowElements.get(i).getValue()).getFlowElement(),collaboration,plane);
            } else if (flowElements.get(i).getValue().getClass() == TEndEvent.class) {
                for (QName key : flowElements.get(i).getValue().getOtherAttributes().keySet()) {
                    TEndEvent iotEnd = (TEndEvent) flowElements.get(i).getValue();
                    //checks if it's of iot:type="start"
                    if (key.equals(new QName("http://some-company/schema/bpmn/iot", "type")) && flowElements.get(i).getValue().getOtherAttributes().get(key).equals("end")) {
                        //create all needed objects
                        TSequenceFlow sequenceFlow = new TSequenceFlow();
                        TServiceTask serviceTask = new TServiceTask();
                        TEndEvent normalEnd = new TEndEvent();

                        //create IDs
                        String taskID = "Activity_" + randomNumberSequence();
                        String flowID = "Flow_" + randomNumberSequence();
                        String endID = "Event_" + randomNumberSequence();

                        //set the attributes for the service task
                        serviceTask.setId(taskID);
                        serviceTask.setName(flowElements.get(i).getValue().getName());
                        serviceTask.getIncoming().add(iotEnd.getIncoming().get(0));
                        serviceTask.getOutgoing().add(QName.valueOf(flowID));
                        if (iotEnd.getProperty().size() > 0) {
                            serviceTask.getProperty().addAll(iotEnd.getProperty());
                        } else {
                            serviceTask.getProperty().clear();
                        }
                        //replace the id of the prior object with the new one
                        replaceAllIDsMentions(flowElements.get(i).getValue().getId(), serviceTask, taskID, flowElements, plane);

                        //set the attributes for the end event
                        normalEnd.setId(endID);
                        normalEnd.getIncoming().add(QName.valueOf(flowID));

                        //set the attributes for the flow event
                        sequenceFlow.setId(flowID);
                        sequenceFlow.setSourceRef(serviceTask);
                        sequenceFlow.setTargetRef(normalEnd);

                        //create JAXBElements out of the objects we have previously created
                        JAXBElement<TServiceTask> taskElement = objectFactory.createServiceTask(serviceTask);
                        JAXBElement<TSequenceFlow> flowElement = objectFactory.createSequenceFlow(sequenceFlow);
                        JAXBElement<TEndEvent> endElement = objectFactory.createEndEvent(normalEnd);

                        //add all elements to the list
                        flowElements.remove(i);
                        flowElements.add(taskElement);
                        flowElements.add(flowElement);
                        flowElements.add(endElement);

                        //reposition and resize the service task
                        double[] serviceCord = getShapeOrEdgeAttributes(taskID, plane);
                        BPMNShape serviceShape = (BPMNShape) getShapeOrEdge(taskID, plane);
                        assert serviceCord != null;
                        serviceCord[1] = serviceCord[1] - serviceCord[3] / 2;
                        assert serviceShape != null;
                        serviceShape.getBounds().setY(serviceCord[1]);
                        serviceShape.getBounds().setWidth(100);
                        serviceShape.getBounds().setHeight(80);

                        //calculate position of the flow nad the end event
                        serviceCord = getShapeOrEdgeAttributes(taskID, plane);
                        int[] edgeX = new int[2];
                        int[] edgeY = new int[2];
                        assert serviceCord != null;
                        edgeX[0] = (int) (serviceCord[0] + serviceCord[2]);
                        edgeY[0] = (int) (serviceCord[1] + serviceCord[3] / 2);
                        edgeX[1] = (int) (serviceCord[0] + serviceCord[2] + 100);
                        edgeY[1] = (int) (serviceCord[1] + serviceCord[3] / 2);
                        //create shape and edge for the newly created objects
                        plane.getDiagramElement().add(createEdge(flowID, edgeX, edgeY));
                        plane.getDiagramElement().add(createShape(endID, edgeX[1], edgeY[1] - 18, 36, 36));
                        //remove the replaced object from the list
                        i--;
                    }
                }
            } else if (flowElements.get(i).getValue() instanceof TCatchEvent) {
                for (QName key : flowElements.get(i).getValue().getOtherAttributes().keySet()) {
                    //checks if it's of iot:type="catch"
                    if (key.equals(new QName("http://some-company/schema/bpmn/iot", "type")) && flowElements.get(i).getValue().getOtherAttributes().get(key).equals("catch")) {
                        TIntermediateCatchEvent iotThrow = (TIntermediateCatchEvent) flowElements.get(i).getValue();
                        //create the service task
                        TBusinessRuleTask businessRuleTask = new TBusinessRuleTask();
                        //generate IDs
                        String taskID = "Activity_"+randomNumberSequence();
                        businessRuleTask.setId(taskID);
                        businessRuleTask.setName(iotThrow.getName());
                        businessRuleTask.getOutgoing().addAll(iotThrow.getOutgoing());
                        businessRuleTask.getIncoming().addAll(iotThrow.getIncoming());
                        JAXBElement<TBusinessRuleTask> taskElement = objectFactory.createBusinessRuleTask(businessRuleTask);
                        flowElements.remove(i);
                        flowElements.add(taskElement);
                        //replace all ids
                        replaceAllIDsMentions(iotThrow.getId(), businessRuleTask, taskID, flowElements, plane);
                        //reposition all items
                        BPMNShape shape = (BPMNShape) getShapeOrEdge(taskID, plane);
                        double y = shape.getBounds().getY() + shape.getBounds().getHeight()/2 - 40;
                        changeShapeAttributes(taskID, 100, 80, (int) shape.getBounds().getX(), y, false, plane);
                        moveEverythingToRight(shape.getBounds().getX(),44,plane);
                        i--;
                    } else if (key.equals(new QName("http://some-company/schema/bpmn/iot", "type")) && flowElements.get(i).getValue().getOtherAttributes().get(key).equals("throw")) {
                        TIntermediateCatchEvent iotThrow = (TIntermediateCatchEvent) flowElements.get(i).getValue();
                        //create the service task
                        String taskID = "Activity_"+randomNumberSequence();
                        TServiceTask serviceTask = new TServiceTask();
                        serviceTask.setId(taskID);
                        serviceTask.setName(iotThrow.getName());
                        serviceTask.getOutgoing().addAll(iotThrow.getOutgoing());
                        serviceTask.getIncoming().addAll(iotThrow.getIncoming());
                        JAXBElement<TServiceTask> taskElement = objectFactory.createServiceTask(serviceTask);
                        flowElements.remove(i);
                        flowElements.add(taskElement);
                        //replace all ids
                        replaceAllIDsMentions(iotThrow.getId(), serviceTask, taskID, flowElements, plane);
                        BPMNShape shape = (BPMNShape) getShapeOrEdge(taskID, plane);
                        double y = shape.getBounds().getY() + shape.getBounds().getHeight()/2 - 40;
                        changeShapeAttributes(taskID, 100, 80, (int) shape.getBounds().getX(), y, false, plane);
                        moveEverythingToRight(shape.getBounds().getX(),44,plane);
                        i--;
                    }
                }
            } else if (flowElements.get(i).getValue().getClass() == TDataObjectReference.class) {
                for (int keyItem = 0; keyItem < flowElements.get(i).getValue().getOtherAttributes().keySet().toArray().length; keyItem++) {
                    QName key = (QName) flowElements.get(i).getValue().getOtherAttributes().keySet().toArray()[keyItem];
                    if (key.equals(new QName("http://some-company/schema/bpmn/iot", "type")) && flowElements.get(i).getValue().getOtherAttributes().get(key).equals("obj")) {
                        flowElements.get(i).getValue().getOtherAttributes().remove(key);
                    } else if (key.equals(new QName("http://some-company/schema/bpmn/iot", "type")) && (flowElements.get(i).getValue().getOtherAttributes().get(key).equals("artefact-catch") || flowElements.get(i).getValue().getOtherAttributes().get(key).equals("artefact-catch-sub"))) {
                        TDataObjectReference reference = (TDataObjectReference)flowElements.get(i).getValue();
                        List<TTask> tasks = getAssociatedTasks(reference.getId(), false, flowElements);
                        if (tasks.size() == 1) {
                            changeIoTDependantTask(tasks,reference,true, false,flowElements,collaboration,plane);
                            //replaceArtefactTask(reference,process,plane);
                            //flowElements.remove(i);
                            i--;
                        } else {
                            keyItem -= changeIoTDependantTask(tasks, reference, true, false, flowElements, collaboration, plane);
                           // replaceSeveralOutputAssociation(tasks.get(0), reference, process, collaboration, plane);
                        }
                    } else if (key.equals(new QName("http://some-company/schema/bpmn/iot", "type")) && (flowElements.get(i).getValue().getOtherAttributes().get(key).equals("sensor") || flowElements.get(i).getValue().getOtherAttributes().get(key).equals("sensor-sub"))) {
                        TDataObjectReference reference = (TDataObjectReference) flowElements.get(i).getValue();
                        List<TTask> tasks = getAssociatedTasks(reference.getId(), false, flowElements);
                        keyItem -= changeIoTDependantTask(tasks, reference, true, false, flowElements, collaboration, plane);
                    } else if (key.equals(new QName("http://some-company/schema/bpmn/iot", "type")) && (flowElements.get(i).getValue().getOtherAttributes().get(key).equals("actor") || flowElements.get(i).getValue().getOtherAttributes().get(key).equals("actor-sub"))) {
                        TDataObjectReference reference = (TDataObjectReference) flowElements.get(i).getValue();
                        List<TTask> tasks = getAssociatedTasks(reference.getId(), true, flowElements);
                        keyItem -= changeIoTDependantTask(tasks, reference, false, false, flowElements, collaboration, plane);
                    }
                }
            }
        }
    }

    private void replaceArtefactTask (TDataObjectReference reference, TTask task, List<JAXBElement<? extends TFlowElement>> flowElements, BPMNPlane plane){
        TBusinessRuleTask businessRuleTask = new TBusinessRuleTask();
        TSequenceFlow taskOut = new TSequenceFlow();
        TSequenceFlow xorOutgoingElse = new TSequenceFlow();
        TSequenceFlow taskIn = new TSequenceFlow();
        TExclusiveGateway exclusiveGatewayLeft = new TExclusiveGateway();
        TExclusiveGateway exclusiveGatewayRight = new TExclusiveGateway();

        //generate IDs
        String gateWay1Id = "Gateway_"+randomNumberSequence();
        String gateWay2Id = "Gateway_"+randomNumberSequence();
        String flow1Id = "Flow_"+randomNumberSequence();
        String flow2Id = "Flow_"+randomNumberSequence();
        String flow3Id = "Flow_"+randomNumberSequence();

        //set attributes
        businessRuleTask.setId(task.getId());
        taskOut.setId(flow2Id);
        taskOut.setName(reference.getName());
        xorOutgoingElse.setId(flow3Id);
        xorOutgoingElse.setName("else");
        taskIn.setId(flow1Id);
        exclusiveGatewayLeft.setId(gateWay1Id);
        exclusiveGatewayRight.setId(gateWay2Id);

        TSequenceFlow originalOutgoingFlow = (TSequenceFlow) getBaseElement(task.getOutgoing().get(0).getLocalPart(),flowElements);
        TSequenceFlow originalIncomingFlow = (TSequenceFlow) getBaseElement(task.getIncoming().get(0).getLocalPart(),flowElements);

        exclusiveGatewayRight.getIncoming().add(QName.valueOf(taskOut.getId()));
        exclusiveGatewayRight.getOutgoing().add(QName.valueOf(originalOutgoingFlow.getId()));
        exclusiveGatewayRight.getOutgoing().add(QName.valueOf(xorOutgoingElse.getId()));

        exclusiveGatewayLeft.getIncoming().add(QName.valueOf(originalIncomingFlow.getId()));
        exclusiveGatewayLeft.getIncoming().add(QName.valueOf(xorOutgoingElse.getId()));
        exclusiveGatewayLeft.getOutgoing().add(QName.valueOf(taskIn.getId()));

        taskOut.setSourceRef(businessRuleTask);
        taskOut.setTargetRef(exclusiveGatewayRight);

        taskIn.setSourceRef(exclusiveGatewayLeft);
        taskIn.setTargetRef(businessRuleTask);

        xorOutgoingElse.setSourceRef(exclusiveGatewayRight);
        xorOutgoingElse.setTargetRef(exclusiveGatewayLeft);

        originalOutgoingFlow.setSourceRef(exclusiveGatewayRight);
        originalOutgoingFlow.setName(reference.getName());

        originalIncomingFlow.setTargetRef(exclusiveGatewayLeft);

        businessRuleTask.setName(task.getName());
        businessRuleTask.getIncoming().add(QName.valueOf(taskIn.getId()));
        businessRuleTask.getOutgoing().add(QName.valueOf(taskOut.getId()));
        businessRuleTask.getProperty().addAll(task.getProperty());

        JAXBElement<TBusinessRuleTask> taskElement = objectFactory.createBusinessRuleTask(businessRuleTask);
        JAXBElement<TExclusiveGateway> gate1Element = objectFactory.createExclusiveGateway(exclusiveGatewayLeft);
        JAXBElement<TExclusiveGateway> gate2Element = objectFactory.createExclusiveGateway(exclusiveGatewayRight);
        JAXBElement<TSequenceFlow> flow1Element = objectFactory.createSequenceFlow(taskOut);
        JAXBElement<TSequenceFlow> flow2Element = objectFactory.createSequenceFlow(xorOutgoingElse);
        JAXBElement<TSequenceFlow> flow3Element = objectFactory.createSequenceFlow(taskIn);


        flowElements.add(gate1Element);
        flowElements.add(gate2Element);
        flowElements.add(flow1Element);
        flowElements.add(flow2Element);
        flowElements.add(flow3Element);


        BPMNShape taskShape = (BPMNShape) getShapeOrEdge(task.getId(),plane);
        moveEverythingToRight(taskShape.getBounds().getX(),200,plane);
        moveEverythingDown(taskShape.getBounds().getY()+taskShape.getBounds().getHeight(),50,plane);
        taskShape.getBounds().setX(taskShape.getBounds().getX()+100);

        org.omg.spec.bpmn._20100524.di.ObjectFactory planeObjectFactory = new org.omg.spec.bpmn._20100524.di.ObjectFactory();

        BPMNShape leftXorGateShape = new BPMNShape();
        Bounds leftXorGateBounds = new Bounds();
        leftXorGateShape.setBounds(leftXorGateBounds);
        leftXorGateShape.setId(exclusiveGatewayLeft.getId() + "_di");
        leftXorGateShape.setBpmnElement(QName.valueOf(exclusiveGatewayLeft.getId()));
        leftXorGateShape.getBounds().setX(taskShape.getBounds().getX() - 50 - 36);
        leftXorGateShape.getBounds().setY(taskShape.getBounds().getY() + taskShape.getBounds().getHeight() / 2 - 18);
        leftXorGateShape.getBounds().setHeight(36);
        leftXorGateShape.getBounds().setWidth(36);
        plane.getDiagramElement().add(planeObjectFactory.createBPMNShape(leftXorGateShape));

        BPMNShape rightXorGateShape = new BPMNShape();
        Bounds rightXorGateBounds = new Bounds();
        rightXorGateShape.setBounds(rightXorGateBounds);
        rightXorGateShape.setId(exclusiveGatewayRight.getId() + "_di");
        rightXorGateShape.setBpmnElement(QName.valueOf(exclusiveGatewayRight.getId()));
        rightXorGateShape.getBounds().setX(taskShape.getBounds().getX() +taskShape.getBounds().getWidth() + 50);
        rightXorGateShape.getBounds().setY(taskShape.getBounds().getY() + taskShape.getBounds().getHeight() / 2 - 18);
        rightXorGateShape.getBounds().setHeight(36);
        rightXorGateShape.getBounds().setWidth(36);
        plane.getDiagramElement().add(planeObjectFactory.createBPMNShape(rightXorGateShape));

        BPMNEdge flowOut = (BPMNEdge) getShapeOrEdge(originalOutgoingFlow.getId(),plane);
        flowOut.getWaypoint().get(0).setX(rightXorGateShape.getBounds().getX()+rightXorGateShape.getBounds().getWidth());

        BPMNEdge flowIn = (BPMNEdge) getShapeOrEdge(originalIncomingFlow.getId(),plane);
        flowIn.getWaypoint().get(flowIn.getWaypoint().size()-1).setX(leftXorGateShape.getBounds().getX());

        BPMNEdge xorOutgoingElseEdge = new BPMNEdge();
        xorOutgoingElseEdge.setId(xorOutgoingElse.getId()+"_di");
        xorOutgoingElseEdge.setBpmnElement(QName.valueOf(xorOutgoingElse.getId()));
        xorOutgoingElseEdge.getWaypoint().add(new Point(rightXorGateBounds.getX()+rightXorGateBounds.getWidth()/2,rightXorGateBounds.getY()+rightXorGateBounds.getHeight()));
        xorOutgoingElseEdge.getWaypoint().add(new Point(rightXorGateBounds.getX()+rightXorGateBounds.getWidth()/2,rightXorGateBounds.getY()+rightXorGateBounds.getHeight()+50));
        xorOutgoingElseEdge.getWaypoint().add(new Point(leftXorGateBounds.getX()+leftXorGateBounds.getWidth()/2,leftXorGateBounds.getY()+leftXorGateBounds.getHeight()+50));
        xorOutgoingElseEdge.getWaypoint().add(new Point(leftXorGateBounds.getX()+leftXorGateBounds.getWidth()/2,leftXorGateBounds.getY()+leftXorGateBounds.getHeight()));


        BPMNEdge taskOutEdge = new BPMNEdge();
        taskOutEdge.setId(taskOut.getId()+"_di");
        taskOutEdge.setBpmnElement(QName.valueOf(taskOut.getId()));
        taskOutEdge.getWaypoint().add(new Point(taskShape.getBounds().getX()+taskShape.getBounds().getWidth(),taskShape.getBounds().getY()+taskShape.getBounds().getHeight()/2));
        taskOutEdge.getWaypoint().add(new Point(rightXorGateBounds.getX(),rightXorGateBounds.getY()+rightXorGateBounds.getHeight()/2));

        BPMNEdge taskInEdge = new BPMNEdge();
        taskInEdge.setId(taskIn.getId()+"_di");
        taskInEdge.setBpmnElement(QName.valueOf(taskIn.getId()));
        taskInEdge.getWaypoint().add(new Point(leftXorGateShape.getBounds().getX()+leftXorGateShape.getBounds().getWidth(),leftXorGateShape.getBounds().getY()+leftXorGateShape.getBounds().getHeight()/2));
        taskInEdge.getWaypoint().add(new Point(taskShape.getBounds().getX(),taskShape.getBounds().getY()+taskShape.getBounds().getHeight()/2));


        plane.getDiagramElement().add(planeObjectFactory.createBPMNEdge(xorOutgoingElseEdge));
        plane.getDiagramElement().add(planeObjectFactory.createBPMNEdge(taskOutEdge));
        plane.getDiagramElement().add(planeObjectFactory.createBPMNEdge(taskInEdge));

        removeFromProcess(task, flowElements);
        flowElements.add(taskElement);
    }

    private void separateBoundary(TBoundaryEvent boundary, List<JAXBElement<? extends TFlowElement>> flowElements, BPMNPlane plane){
        TTask baseTask = (TTask) getBaseElement(boundary.getAttachedToRef().getLocalPart(),flowElements);
        baseTask.getProperty().clear();
        BPMNShape baseShape = (BPMNShape) getShapeOrEdge(baseTask.getId(), plane);

        moveEverythingToRight(baseShape.getBounds().getX(), 200,plane);
        moveEverythingDown(baseShape.getBounds().getY()+baseShape.getBounds().getHeight(),100,plane);
        baseShape.getBounds().setX(baseShape.getBounds().getX()+100);

        //create Objects
        TSequenceFlow leftParallel1Flow = new TSequenceFlow();
        TSequenceFlow leftParallel2Flow = new TSequenceFlow();
        TSequenceFlow xorGateWayOutFlow = new TSequenceFlow();
        TSequenceFlow xorGateWayInFlow = new TSequenceFlow();
        TSequenceFlow rightParallelTaskIncoming = new TSequenceFlow();
        TIntermediateCatchEvent boundaryInter = new TIntermediateCatchEvent();
        TParallelGateway leftGateParallel = new TParallelGateway();
        TParallelGateway rightGateParallel = new TParallelGateway();
        TExclusiveGateway xorGateWay = new TExclusiveGateway(); //TODO: something is wrong with the xor

        //create IDs
        String leftParallelGate = "Gateway_"+randomNumberSequence();
        String rightParallelGate = "Gateway_"+randomNumberSequence();
        String xorGateWayId = "Gateway_"+randomNumberSequence();
        String taskIncomingFlow = "Flow_"+randomNumberSequence();
        String leftParallel2TaskFlow = "Flow_"+randomNumberSequence();
        String xorGateWayLeftFow = "Flow_"+randomNumberSequence();
        String xorGateWayOutFow = "Flow_"+randomNumberSequence();
        String taskOutgoingFlow = "Flow_"+randomNumberSequence();
        String boundaryID = "Flow_"+randomNumberSequence();

        //set attributes
        leftParallel1Flow.setId(taskIncomingFlow);
        leftParallel2Flow.setId(leftParallel2TaskFlow);
        xorGateWayOutFlow.setId(xorGateWayOutFow);
        xorGateWayOutFlow.setName("else");
        xorGateWayInFlow.setId(xorGateWayLeftFow);
        rightParallelTaskIncoming.setId(taskOutgoingFlow);
        leftGateParallel.setId(leftParallelGate);
        rightGateParallel.setId(rightParallelGate);
        xorGateWay.setId(xorGateWayId);
        boundaryInter.setId(boundary.getId());
        boundaryInter.setName("Retrieve data");
        boundaryInter.getEventDefinition().addAll(boundary.getEventDefinition());

        TSequenceFlow originalOutgoingFlow = (TSequenceFlow) getBaseElement(baseTask.getOutgoing().get(0).getLocalPart(),flowElements);
        TSequenceFlow originalIncomingFlow = (TSequenceFlow) getBaseElement(baseTask.getIncoming().get(0).getLocalPart(),flowElements);
        TSequenceFlow xorGateWayRightOut = (TSequenceFlow) getBaseElement(boundary.getOutgoing().get(0).getLocalPart(),flowElements);
        //xorGateWayRightOut.setName(boundary.getName());

        leftGateParallel.getIncoming().add(QName.valueOf(originalIncomingFlow.getId()));
        leftGateParallel.getOutgoing().add(QName.valueOf(leftParallel1Flow.getId()));
        leftGateParallel.getOutgoing().add(QName.valueOf(leftParallel2Flow.getId()));

        rightGateParallel.getIncoming().add(QName.valueOf(rightParallelTaskIncoming.getId()));
        rightGateParallel.getIncoming().add(QName.valueOf(xorGateWayOutFlow.getId()));
        rightGateParallel.getOutgoing().add(QName.valueOf(originalOutgoingFlow.getId()));

        boundaryInter.getIncoming().add(QName.valueOf(leftParallel2Flow.getId()));
        boundaryInter.getOutgoing().add(QName.valueOf(xorGateWayInFlow.getId()));

        baseTask.getOutgoing().remove(originalOutgoingFlow);
        baseTask.getOutgoing().add(QName.valueOf(taskOutgoingFlow));
        baseTask.getIncoming().remove(originalIncomingFlow);
        baseTask.getOutgoing().add(QName.valueOf(taskIncomingFlow));

        xorGateWay.getIncoming().add(QName.valueOf(xorGateWayInFlow.getId()));
        xorGateWay.getOutgoing().add(QName.valueOf(xorGateWayOutFlow.getId()));
        xorGateWay.getOutgoing().add(QName.valueOf(xorGateWayRightOut.getId()));

        leftParallel1Flow.setSourceRef(leftGateParallel.getId());
        leftParallel1Flow.setTargetRef(baseTask.getId());

        leftParallel2Flow.setSourceRef(leftGateParallel.getId());
        leftParallel2Flow.setTargetRef(boundaryInter.getId());

        xorGateWayInFlow.setSourceRef(boundaryInter.getId());
        xorGateWayInFlow.setTargetRef(xorGateWay.getId());

        xorGateWayOutFlow.setSourceRef(xorGateWay.getId());
        xorGateWayOutFlow.setTargetRef(rightGateParallel.getId());

        rightParallelTaskIncoming.setSourceRef(baseTask.getId());
        rightParallelTaskIncoming.setTargetRef(rightGateParallel.getId());

        xorGateWayRightOut.setSourceRef(xorGateWay.getId());

        originalIncomingFlow.setTargetRef(leftGateParallel.getId());
        originalOutgoingFlow.setSourceRef(rightGateParallel.getId());


        JAXBElement<TExclusiveGateway> exclusiveGateway = objectFactory.createExclusiveGateway(xorGateWay);
        JAXBElement<TParallelGateway> parallelGateway1 = objectFactory.createParallelGateway(leftGateParallel);
        JAXBElement<TParallelGateway> parallelGateway2 = objectFactory.createParallelGateway(rightGateParallel);
        JAXBElement<TIntermediateCatchEvent> intermediateCatchEvent = objectFactory.createIntermediateCatchEvent(boundaryInter);
        JAXBElement<TSequenceFlow> flow1Element = objectFactory.createSequenceFlow(leftParallel1Flow);
        JAXBElement<TSequenceFlow> flow2Element = objectFactory.createSequenceFlow(leftParallel2Flow);
        JAXBElement<TSequenceFlow> flow3Element = objectFactory.createSequenceFlow(xorGateWayOutFlow);
        JAXBElement<TSequenceFlow> flow4Element = objectFactory.createSequenceFlow(xorGateWayInFlow);
        JAXBElement<TSequenceFlow> flow5Element = objectFactory.createSequenceFlow(rightParallelTaskIncoming);

        removeFromProcess(boundary,flowElements);
        flowElements.add(exclusiveGateway);
        flowElements.add(parallelGateway1);
        flowElements.add(parallelGateway2);
        flowElements.add(intermediateCatchEvent);
        flowElements.add(flow1Element);
        flowElements.add(flow2Element);
        flowElements.add(flow3Element);
        flowElements.add(flow4Element);
        flowElements.add(flow5Element);


        BPMNEdge flow1 = (BPMNEdge) getShapeOrEdge(originalIncomingFlow.getId(),plane);
        BPMNEdge flow2 = (BPMNEdge) getShapeOrEdge(originalOutgoingFlow.getId(),plane);
        BPMNEdge flow3 = (BPMNEdge) getShapeOrEdge(xorGateWayRightOut.getId(),plane);
        BPMNEdge parallel1 = new BPMNEdge();
        parallel1.setId(leftParallel1Flow.getId()+"_di");
        parallel1.setBpmnElement(new QName(leftParallel1Flow.getId()));
        BPMNEdge parallel2 = new BPMNEdge();
        parallel2.setId(leftParallel2Flow.getId()+"_di");
        parallel2.setBpmnElement(new QName(leftParallel2Flow.getId()));
        BPMNEdge xorIn = new BPMNEdge();
        xorIn.setId(xorGateWayInFlow.getId()+"_di");
        xorIn.setBpmnElement(new QName(xorGateWayInFlow.getId()));
        BPMNEdge xorOutUp = new BPMNEdge();
        xorOutUp.setId(xorGateWayOutFlow.getId()+"_di");
        xorOutUp.setBpmnElement(new QName(xorGateWayOutFlow.getId()));
        BPMNEdge taskOut = new BPMNEdge();
        taskOut.setId(rightParallelTaskIncoming.getId()+"_di");
        taskOut.setBpmnElement(new QName(rightParallelTaskIncoming.getId()));

        double leftParallelX = baseShape.getBounds().getX()-50;
        double rightParallelX = baseShape.getBounds().getX()+baseShape.getBounds().getWidth()+50;
        double boundaryInterX = baseShape.getBounds().getX();
        double xorX = baseShape.getBounds().getX()+baseShape.getBounds().getWidth()+50;
        double leftParallelY = baseShape.getBounds().getY()+18;
        double rightParallelY = baseShape.getBounds().getY()+18;
        double boundaryInterY = flow3.getWaypoint().get(flow3.getWaypoint().size()-1).getY()-18;
        double xorY = baseShape.getBounds().getY()+18;

        plane.getDiagramElement().add(createShape(leftGateParallel.getId(), (int) leftParallelX, (int) leftParallelY,36,36));
        plane.getDiagramElement().add(createShape(rightGateParallel.getId(), (int) rightParallelX, (int) rightParallelY,36,36));
        plane.getDiagramElement().add(createShape(boundaryInter.getId(), (int) boundaryInterX, (int) boundaryInterY ,36,36));
        plane.getDiagramElement().add(createShape(xorGateWay.getId(), (int) xorX, (int) xorY ,36,36));

        flow1.getWaypoint().get(flow1.getWaypoint().size()-1).setX(baseShape.getBounds().getX()-50);

        parallel1.getWaypoint().add(new Point(leftParallelX+36,leftParallelY+18));
        parallel1.getWaypoint().add(new Point(baseShape.getBounds().getX(),leftParallelY+18));

        flow2.getWaypoint().get(0).setX(rightParallelX+36);
        flow2.getWaypoint().get(0).setY(rightParallelY+18);

        parallel2.getWaypoint().add(new Point(leftParallelX+18,leftParallelY+36));
        parallel2.getWaypoint().add(new Point(leftParallelX+18,boundaryInterY+18));
        parallel2.getWaypoint().add(new Point(boundaryInterX,boundaryInterY+18));

        xorIn.getWaypoint().add(new Point(boundaryInterX+36,boundaryInterY+18));
        xorIn.getWaypoint().add(new Point(xorX,xorY+18));

        flow3.getWaypoint().get(0).setX(xorX+36);
        flow3.getWaypoint().get(0).setY(xorY+18);
        if (flow3.getWaypoint().size() == 3)
            flow3.getWaypoint().remove(1);

        xorOutUp.getWaypoint().add(new Point(xorX+18,xorY));
        xorOutUp.getWaypoint().add(new Point(rightParallelX+18,rightParallelY+36));

        taskOut.getWaypoint().add(new Point(baseShape.getBounds().getX()+baseShape.getBounds().getWidth(),baseShape.getBounds().getY()+baseShape.getBounds().getHeight()/2));
        taskOut.getWaypoint().add(new Point(rightParallelX,rightParallelY+18));

        org.omg.spec.bpmn._20100524.di.ObjectFactory objectFactoryEdge = new org.omg.spec.bpmn._20100524.di.ObjectFactory();

        plane.getDiagramElement().add(objectFactoryEdge.createBPMNEdge(parallel1));
        plane.getDiagramElement().add(objectFactoryEdge.createBPMNEdge(parallel2));
        plane.getDiagramElement().add(objectFactoryEdge.createBPMNEdge(xorIn));
        plane.getDiagramElement().add(objectFactoryEdge.createBPMNEdge(xorOutUp));
        plane.getDiagramElement().add(objectFactoryEdge.createBPMNEdge(taskOut));
    }

    private void formatDiagram(List<JAXBElement<? extends TFlowElement>> flowElements, TCollaboration collaboration, BPMNPlane plane){
        for (TTask delete:toDelete) {
            removeFromProcess(delete,flowElements);
        }
        for (int i = 0; i < flowElements.size(); i++) {
            if (flowElements.get(i).getValue() instanceof TIntermediateCatchEvent) {
                TIntermediateCatchEvent intermediateCatchEvent = new TIntermediateCatchEvent();

            }
            else if (flowElements.get(i).getValue() instanceof TIntermediateThrowEvent) {
                TIntermediateThrowEvent intermediateThrowEvent = new TIntermediateThrowEvent();
            }
            else if (flowElements.get(i).getValue() instanceof TGateway) {
                TGateway gateway = (TGateway) flowElements.get(i).getValue();
                if (gateway.getIncoming().size() > 0) {
                    for (int k = 0; k < gateway.getIncoming().size(); k++) {
                        BPMNEdge incoming = (BPMNEdge) getShapeOrEdge(String.valueOf(gateway.getIncoming().get(k)), plane);
                        BPMNShape shape = (BPMNShape) getShapeOrEdge(gateway.getId(), plane);
                        if (incoming != null) {
                            if (shape.getBounds().getY() + shape.getBounds().getHeight() == incoming.getWaypoint().get(incoming.getWaypoint().size() - 1).getY() ||
                                    shape.getBounds().getY() == incoming.getWaypoint().get(incoming.getWaypoint().size() - 1).getY()) {
                                double x = incoming.getWaypoint().get(incoming.getWaypoint().size() - 1).getX();
                                for (int j = 0; j < incoming.getWaypoint().size(); j++) {
                                    if (x == incoming.getWaypoint().get(j).getX())
                                        incoming.getWaypoint().get(j).setX(shape.getBounds().getX() + shape.getBounds().getWidth() / 2);
                                }
                            } else
                                incoming.getWaypoint().get(incoming.getWaypoint().size() - 1).setX(shape.getBounds().getX());
                        }
                    }
                } if (gateway.getOutgoing().size() > 0) {
                    for (int k = 0; k < gateway.getOutgoing().size(); k++) {
                        BPMNEdge outgoing = (BPMNEdge) getShapeOrEdge(String.valueOf(gateway.getOutgoing().get(k)), plane);
                        BPMNShape shape = (BPMNShape) getShapeOrEdge(gateway.getId(), plane);

                        if (outgoing != null) {
                            if (shape.getBounds().getY() + shape.getBounds().getHeight() == outgoing.getWaypoint().get(0).getY() ||
                                    shape.getBounds().getY() == outgoing.getWaypoint().get(0).getY()) {
                                double x = outgoing.getWaypoint().get(0).getX();
                                for (int j = 0; j < outgoing.getWaypoint().size(); j++) {
                                    if (x == outgoing.getWaypoint().get(j).getX())
                                        outgoing.getWaypoint().get(j).setX(shape.getBounds().getX() + shape.getBounds().getWidth() / 2);
                                }

                            } else
                                outgoing.getWaypoint().get(0).setX(shape.getBounds().getX() + shape.getBounds().getWidth());
                        }
                    }
                }
            }

            if (flowElements.get(i).getValue() instanceof TActivity) {
                TActivity task = (TActivity) flowElements.get(i).getValue();
                if (task.getOutgoing().size() > 0) {
                    BPMNEdge outgoing = (BPMNEdge) getShapeOrEdge(String.valueOf(task.getOutgoing().get(0)), plane);
                    BPMNShape shape = (BPMNShape) getShapeOrEdge(task.getId(), plane);
                    if (outgoing != null)
                        outgoing.getWaypoint().get(0).setX(shape.getBounds().getX() + shape.getBounds().getWidth());
                }
                if (task.getIncoming().size() > 0) {
                    BPMNEdge incoming = (BPMNEdge) getShapeOrEdge(String.valueOf(task.getIncoming().get(0)), plane);
                    BPMNShape shape = (BPMNShape) getShapeOrEdge(task.getId(), plane);
                    if (incoming != null)
                        incoming.getWaypoint().get(incoming.getWaypoint().size()-1).setX(shape.getBounds().getX());
                }
            }
        }
        for (int k = 0; k < collaboration.getParticipant().size(); k++) {
            if (collaboration.getParticipant().get(k).getProcessRef() != null){
                BPMNShape processRefShape = (BPMNShape) getShapeOrEdge(collaboration.getParticipant().get(k).getId(),plane);
                processRefShape.getBounds().setWidth(getMaxX(plane));
                break;
            }
        }
        for (int k = 0; k < collaboration.getMessageFlow().size(); k++) {
            BPMNEdge edge = (BPMNEdge) getShapeOrEdge(collaboration.getMessageFlow().get(k).getId(),plane);
            BPMNShape sourceRefShape = (BPMNShape) getShapeOrEdge(collaboration.getMessageFlow().get(k).getSourceRef().getLocalPart(),plane);
            BPMNShape targetRefShape = (BPMNShape) getShapeOrEdge(collaboration.getMessageFlow().get(k).getTargetRef().getLocalPart(),plane);

            for (int i = 0; i < whiteBoxes.size(); i++) {
                if (sourceRefShape.getBpmnElement().getLocalPart().equals(whiteBoxes.get(i))) {
                    if (sourceRefShape.getBounds().getY() < targetRefShape.getBounds().getY()) {
                        edge.getWaypoint().get(0).setX(sourceRefShape.getBounds().getX() + sourceRefShape.getBounds().getWidth() / 2);
                        edge.getWaypoint().get(0).setY(sourceRefShape.getBounds().getY() + sourceRefShape.getBounds().getHeight());

                        edge.getWaypoint().get(edge.getWaypoint().size() - 1).setX(targetRefShape.getBounds().getX() + targetRefShape.getBounds().getWidth() / 2);
                        edge.getWaypoint().get(edge.getWaypoint().size() - 1).setY(targetRefShape.getBounds().getY());
                    } else if (sourceRefShape.getBounds().getY() > targetRefShape.getBounds().getY()) {
                        edge.getWaypoint().get(0).setX(sourceRefShape.getBounds().getX() + sourceRefShape.getBounds().getWidth() / 2);
                        edge.getWaypoint().get(0).setY(sourceRefShape.getBounds().getY());

                        edge.getWaypoint().get(edge.getWaypoint().size() - 1).setX(targetRefShape.getBounds().getX() + targetRefShape.getBounds().getWidth() / 2);
                        edge.getWaypoint().get(edge.getWaypoint().size() - 1).setY(targetRefShape.getBounds().getY() + targetRefShape.getBounds().getHeight());
                    }
                } else if (targetRefShape.getBpmnElement().getLocalPart().equals(whiteBoxes.get(i))) {
                    if (sourceRefShape.getBounds().getY() < targetRefShape.getBounds().getY()) {
                        edge.getWaypoint().get(0).setX(sourceRefShape.getBounds().getX() + sourceRefShape.getBounds().getWidth() / 2);
                        edge.getWaypoint().get(0).setY(sourceRefShape.getBounds().getY() + sourceRefShape.getBounds().getHeight());

                        edge.getWaypoint().get(edge.getWaypoint().size() - 1).setX(targetRefShape.getBounds().getX() + targetRefShape.getBounds().getWidth() / 2);
                        edge.getWaypoint().get(edge.getWaypoint().size() - 1).setY(targetRefShape.getBounds().getY());
                    } else if (sourceRefShape.getBounds().getY() > targetRefShape.getBounds().getY()) {
                        edge.getWaypoint().get(0).setX(sourceRefShape.getBounds().getX() + sourceRefShape.getBounds().getWidth() / 2);
                        edge.getWaypoint().get(0).setY(sourceRefShape.getBounds().getY());

                        edge.getWaypoint().get(edge.getWaypoint().size() - 1).setX(targetRefShape.getBounds().getX() + targetRefShape.getBounds().getWidth() / 2);
                        edge.getWaypoint().get(edge.getWaypoint().size() - 1).setY(targetRefShape.getBounds().getY() + targetRefShape.getBounds().getHeight());
                    }
                }
            }
        }
    }

    private void moveEverythingToRight(double x, int moveAmount, BPMNPlane plane){
        for (int i = 0; i < plane.getDiagramElement().size(); i++) {
            if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNEdge.class) {
                BPMNEdge edge = (BPMNEdge) plane.getDiagramElement().get(i).getValue();
                for (int k = 0; k < edge.getWaypoint().size(); k++) {
                    if (edge.getWaypoint().get(k).getX() > x) {
                        edge.getWaypoint().get(k).setX(edge.getWaypoint().get(k).getX() + moveAmount);
                    }
                }
            } else if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNShape.class) {
                BPMNShape shape = (BPMNShape) plane.getDiagramElement().get(i).getValue();
                if (shape.getBounds().getX() > x) {
                    shape.getBounds().setX(shape.getBounds().getX() + moveAmount);
                }
            }
        }
    }

    private void moveEverythingDown(double y, int moveAmount, BPMNPlane plane){
        boolean alreadyMoved = false;
        for (Integer savedY: movedYDown){
            if (inLine(new Point(0,savedY-5),new Point(0,savedY+5),new Point(0,y))){
                alreadyMoved = true;
            }
        }
        if (!alreadyMoved) {
            movedYDown.add((int) y);
            for (int i = 0; i < plane.getDiagramElement().size(); i++) {
                if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNEdge.class) {
                    BPMNEdge edge = (BPMNEdge) plane.getDiagramElement().get(i).getValue();
                    for (int k = 0; k < edge.getWaypoint().size(); k++) {
                        if (edge.getWaypoint().get(k).getY() > y) {
                            edge.getWaypoint().get(k).setY(edge.getWaypoint().get(k).getY() + moveAmount);
                        }
                    }
                } else if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNShape.class) {
                    BPMNShape shape = (BPMNShape) plane.getDiagramElement().get(i).getValue();
                    if (shape.getBounds().getY() > y) {
                        shape.getBounds().setY(shape.getBounds().getY() + moveAmount);
                    }
                }
            }
        }
    }

    private void moveEverythingUp(double y, int moveAmount, BPMNPlane plane){
        boolean alreadyMoved = false;
        for (Integer savedY: movedYUp){
            if (inLine(new Point(0,savedY-5),new Point(0,savedY+5),new Point(0,y))){
                alreadyMoved = true;
            }
        }
        if (!alreadyMoved) {
            movedYUp.add((int) y);
            for (int i = 0; i < plane.getDiagramElement().size(); i++) {
                if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNEdge.class) {
                    BPMNEdge edge = (BPMNEdge) plane.getDiagramElement().get(i).getValue();
                    for (int k = 0; k < edge.getWaypoint().size(); k++) {
                        if (edge.getWaypoint().get(k).getY() < y) {
                            edge.getWaypoint().get(k).setY(edge.getWaypoint().get(k).getY() - moveAmount);
                        }
                    }
                } else if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNShape.class) {
                    BPMNShape shape = (BPMNShape) plane.getDiagramElement().get(i).getValue();
                    if (shape.getBounds().getY() < y) {
                        shape.getBounds().setY(shape.getBounds().getY() - moveAmount);
                    }
                }
            }
        }
    }


    private int changeIoTDependantTask(List<TTask> tasks, TDataObjectReference reference, boolean isSensor, boolean convertRestToIntermediate, List<JAXBElement<? extends TFlowElement>> flowElements, TCollaboration collaboration, BPMNPlane plane) {
        int counter = 0;
        Iterator<TTask> iterator = tasks.iterator();
        if (isAlreadyAWhiteBox(reference.getId(),collaboration)){
            convertRestToIntermediate = true;
        }
        while (iterator.hasNext()) {
            //get the current task
            TTask task = iterator.next();
            boolean removed = false;
            for (TTask remove : toRemove) {
                if (remove.getId().equals(task.getId())) {
                    removed = true;
                    break;
                }
            }
            if (removed) {
                iterator.remove();
                break;
            }
            if (task.getDataOutputAssociation().size() == 0 && task.getDataInputAssociation().size() == 1) {
                List<TTask> inputTasks = getAssociatedTasks(getIDOfObject(task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()), false, flowElements);
                System.out.println("jippi");
                if (inputTasks.size() == 1 && !convertRestToIntermediate) {
                    boolean artefactCatchPresent = false;
                    Object keyItem = ((TDataObjectReference)task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()).getOtherAttributes().keySet().toArray()[0];
                    if (((TDataObjectReference)task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()).getOtherAttributes().get(keyItem).equals("artefact-catch")) {
                        artefactCatchPresent = true;
                        System.out.println("I'm here");
                    }
                    System.out.println("I'm here2");
                    if (!artefactCatchPresent) {
                        if (!isAlreadyAWhiteBox(reference.getId(),collaboration)) {
                            TBusinessRuleTask businessRuleTask = new TBusinessRuleTask();
                            businessRuleTask.setId(task.getId());
                            businessRuleTask.setName(task.getName());
                            businessRuleTask.getIncoming().addAll(task.getIncoming());
                            businessRuleTask.getOutgoing().addAll(task.getOutgoing());
                            businessRuleTask.getProperty().addAll(task.getProperty());

                            JAXBElement<TBusinessRuleTask> taskElement = objectFactory.createBusinessRuleTask(businessRuleTask);
                            //removeFromProcess(task, process);
                            counter++;
                            removeFromProcess(task,flowElements);
                            flowElements.add(taskElement);

                        } else {
                            changeTaskToIntermediateMessageCatchEvent(task,flowElements,collaboration,plane);
                        }
                    } else {
                        replaceArtefactTask(reference, task,flowElements,plane);
                    }
                }  else if (inputTasks.size() == 1 && convertRestToIntermediate){
                    System.out.println("he");
                    boolean artefactCatchPresent = false;
                    Object keyItem = ((TDataObjectReference)task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()).getOtherAttributes().keySet().toArray()[0];
                    if (((TDataObjectReference)task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()).getOtherAttributes().get(keyItem).equals("artefact-catch")) {
                        artefactCatchPresent = true;
                        System.out.println("I'm here");
                    }

                    if (!artefactCatchPresent) {
                        toRemove.add(task);
                        changeTaskToIntermediateMessageCatchEvent(task,flowElements,collaboration,plane);
                    } else {
                        toRemove.add(inputTasks.get(0));
                        replaceArtefactTask(reference,inputTasks.get(0),flowElements,plane);
                        changeTaskToIntermediateMessageCatchEvent(inputTasks.get(0),flowElements,collaboration,plane);
                    }
                } else if (inputTasks.size() > 1){
                    //create WhiteBox
                    replaceDataAssociationWithWhiteBox(reference.getId(), "Sensor (IoT)" + reference.getName(), flowElements, collaboration, plane);
                     if (inputTasks.get(1).getDataInputAssociation().size() == 1 && inputTasks.get(1).getDataOutputAssociation().size() == 0) {
                        boolean artefactCatchPresent = false;
                        Object keyItem = reference.getOtherAttributes().keySet().toArray()[0];
                        if (((TDataObjectReference)task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()).getOtherAttributes().get(keyItem).equals("artefact-catch")) {
                            artefactCatchPresent = true;
                            System.out.println("I'm here");
                        }
                        System.out.println("I'm here2");
                        if (!artefactCatchPresent) {
                            List<TTask> tempTasks = new ArrayList<>();
                            if (task.getId().equals(inputTasks.get(0))) {
                                tempTasks.add(inputTasks.get(1));
                            } else {
                                tempTasks.add(inputTasks.get(0));
                            }
                            removeFromProcess(inputTasks.get(0), flowElements);
                            //remove original reference
                            changeIoTDependantTask(tempTasks, reference, isSensor, true, flowElements, collaboration, plane);
                            for (TTask exTask : inputTasks) {
                                changeTaskToIntermediateMessageCatchEvent(exTask, flowElements, collaboration, plane);
                            }
                        } else {
                            replaceArtefactTask(reference,inputTasks.get(0),flowElements,plane);
                            changeTaskToIntermediateMessageCatchEvent(inputTasks.get(0), flowElements, collaboration, plane);
                            replaceArtefactTask(reference,inputTasks.get(1),flowElements,plane);
                            changeTaskToIntermediateMessageCatchEvent(inputTasks.get(1), flowElements, collaboration, plane);
                        }
                    } else if (inputTasks.size() < 3) {
                         System.out.println("Am here");
                        replaceDataAssociationWithWhiteBox(reference.getId(), "Sensor (IoT)" + reference.getName(), flowElements, collaboration, plane);
                        ArrayList<TTask> temp = new ArrayList<>();
                        if (task.getId().equals(inputTasks.get(0))) {
                            temp.add(inputTasks.get(1));
                            //toRemove.add(inputTasks.get(0));
                        } else {
                            temp.add(inputTasks.get(0));
                            //toRemove.add(inputTasks.get(1));
                        }
                        changeTaskToIntermediateMessageCatchEvent(task, flowElements, collaboration, plane);
                        changeIoTDependantTask(temp, reference, isSensor, true, flowElements,collaboration,plane);
                        changeTaskToIntermediateMessageCatchEvent(task, flowElements, collaboration, plane);
                    }
                }
                removeFromProcess(reference, flowElements);
            } else if (task.getDataOutputAssociation().size() == 1 && task.getDataInputAssociation().size() == 1) {
                List<TTask> inputTasks = getAssociatedTasks(getIDOfObject(task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()), false, flowElements);
                List<TTask> outputTasks = getAssociatedTasks(getIDOfObject(task.getDataOutputAssociation().get(0).getTargetRef()), true, flowElements);

                boolean artefactCatchPresent = false;
                TServiceTask serviceTask = null;

                Object keyItem = ((TDataObjectReference)task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()).getOtherAttributes().keySet().toArray()[0];
                if (((TDataObjectReference)task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()).getOtherAttributes().get(keyItem).equals("artefact-catch")) {
                    artefactCatchPresent = true;
                }

                if (!artefactCatchPresent && inputTasks.size() > 0) {
                    replaceOneInputAndOneOutputAssociation(task, flowElements, collaboration, plane).getValue();
                } else if (inputTasks.size() > 0){
                    replaceSeveralInputArtefactAndOneActuatorAssociation(task, reference, flowElements, collaboration, plane).getValue();
                }
                if (inputTasks.size() > 1) {
                    ArrayList<TTask> temp = new ArrayList<>();
                    if (inputTasks.get(0).getId().equals(task.getId())){
                        temp.add(inputTasks.get(1));
                    } else {
                        temp.add(inputTasks.get(0));
                    }
                    changeIoTDependantTask(temp,reference,isSensor, true,flowElements,collaboration,plane);
                } if (outputTasks.size() > 1){
                    ArrayList<TTask> temp = new ArrayList<>();
                    if (outputTasks.get(0).getId().equals(task.getId())){
                        temp.add(outputTasks.get(1));
                    } else {
                        temp.add(outputTasks.get(0));
                    }
                    changeIoTDependantTask(temp,reference,isSensor, true,flowElements,collaboration,plane);

                }
                removeFromProcess(reference, flowElements);

            } else if (task.getDataOutputAssociation().size() == 0 && task.getDataInputAssociation().size() > 1) {
                List<TTask> inputTasks = getAssociatedTasks(getIDOfObject(task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()), false, flowElements);
                List<TTask> input2Tasks = getAssociatedTasks(getIDOfObject(task.getDataInputAssociation().get(1).getSourceRef().get(0).getValue()), false, flowElements);
                boolean artefactCatchPresent = false;
                //TODO
                Object keyItem = ((TDataObjectReference)task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()).getOtherAttributes().keySet().toArray()[0];
                if (((TDataObjectReference)task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()).getOtherAttributes().get(keyItem).equals("artefact-catch")) {
                    artefactCatchPresent = true;
                }
                keyItem = ((TDataObjectReference)task.getDataInputAssociation().get(1).getSourceRef().get(0).getValue()).getOtherAttributes().keySet().toArray()[0];
                if (((TDataObjectReference)task.getDataInputAssociation().get(1).getSourceRef().get(0).getValue()).getOtherAttributes().get(keyItem).equals("artefact-catch")) {
                    artefactCatchPresent = true;
                }
                if (!artefactCatchPresent && inputTasks.size() > 0 && input2Tasks.size() > 0) {
                    replaceSeveralInputAssociation(task, reference, flowElements, collaboration, plane);
                } else if (inputTasks.size() > 0 && input2Tasks.size() > 0){
                    replaceSeveralInputArtefactAndSensorAssociation(task,reference, flowElements, collaboration, plane);
                }


                if (inputTasks.size() > 1 && input2Tasks.size() > 1 && !convertRestToIntermediate) {
                    ArrayList<TTask> temp = new ArrayList<>();
                    if (input2Tasks.get(0).getId().equals(task.getId())){
                        temp.add(input2Tasks.get(1));
                        toRemove.add(input2Tasks.get(0));
                    } else {
                        temp.add(input2Tasks.get(0));
                        toRemove.add(input2Tasks.get(1));
                    }
                    if (inputTasks.get(0).getId().equals(task.getId())){
                        temp.add(inputTasks.get(1));
                        toRemove.add(inputTasks.get(0));
                    } else {
                        temp.add(inputTasks.get(0));
                        toRemove.add(inputTasks.get(1));
                    }
                    removeFromProcess(reference,flowElements);
                    //changeIoTDependantTask(temp,reference,isSensor, true,process,collaboration,plane);
                }
                else if (inputTasks.size() > 1  && !convertRestToIntermediate) {
                    ArrayList<TTask> temp = new ArrayList<>();
                    if (inputTasks.get(0).getId().equals(task.getId())){
                        temp.add(inputTasks.get(1));
                        toRemove.add(inputTasks.get(0));
                    } else {
                        temp.add(inputTasks.get(0));
                        toRemove.add(inputTasks.get(1));
                    }
                    removeFromProcess(reference, flowElements);
                    changeIoTDependantTask(temp, (TDataObjectReference) task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue(), isSensor, true, flowElements, collaboration, plane);
                }
                else if (input2Tasks.size() > 1  && !convertRestToIntermediate){
                    ArrayList<TTask> temp = new ArrayList<>();
                    if (input2Tasks.get(0).getId().equals(task.getId())){
                        temp.add(input2Tasks.get(1));
                        toRemove.add(input2Tasks.get(0));
                    } else {
                        temp.add(input2Tasks.get(0));
                        toRemove.add(input2Tasks.get(1));
                    }
                    removeFromProcess(reference,flowElements);
                    changeIoTDependantTask(temp,(TDataObjectReference) task.getDataInputAssociation().get(1).getSourceRef().get(0).getValue(),isSensor, true,flowElements,collaboration,plane);
                }

            } else if (task.getDataOutputAssociation().size() == 1 && task.getDataInputAssociation().size() > 1) {
                List<TTask> inputTasks = getAssociatedTasks(getIDOfObject(task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()), false, flowElements);
                List<TTask> input2Tasks = getAssociatedTasks(getIDOfObject(task.getDataInputAssociation().get(1).getSourceRef().get(0).getValue()), false, flowElements);
                List<TTask> outputTasks = getAssociatedTasks(getIDOfObject(task.getDataOutputAssociation().get(0).getTargetRef()), true, flowElements);
                boolean artefactCatchPresent = false;
                if (inputTasks.size() > 0 && input2Tasks.size() > 0 && outputTasks.size() > 0) {
                    for (int j = 0; j < inputTasks.get(0).getDataInputAssociation().size(); j++) {
                        Object keyItem = ((TDataObjectReference) inputTasks.get(0).getDataInputAssociation().get(j).getSourceRef().get(0).getValue()).getOtherAttributes().keySet().toArray()[0];
                        if (((TDataObjectReference) inputTasks.get(0).getDataInputAssociation().get(j).getSourceRef().get(0).getValue()).getOtherAttributes().get(keyItem).equals("artefact-catch")) {
                            artefactCatchPresent = true;
                        }

                    }
                    if (!artefactCatchPresent) {
                        replaceOneInputAndOneOutputAssociation(task, flowElements, collaboration, plane).getKey();
                    } else {
                        replaceOneSensorOneCatchArtefactOneActuatorAssociation(task, reference, flowElements, collaboration, plane);
                    }
                }

                if (inputTasks.size() > 1 && input2Tasks.size() > 1 && outputTasks.size() > 1) {
                    ArrayList<TTask> temp = new ArrayList<>();
                    if (input2Tasks.get(0).getId().equals(task.getId())){
                        temp.add(input2Tasks.get(1));
                        toRemove.add(input2Tasks.get(0));
                    } else {
                        temp.add(input2Tasks.get(0));
                        toRemove.add(input2Tasks.get(1));
                    }
                    removeFromProcess(reference, flowElements);
                    changeIoTDependantTask(temp, (TDataObjectReference) task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue(), isSensor, true, flowElements, collaboration, plane);

                } else if (inputTasks.size() > 1 && input2Tasks.size() > 1 && outputTasks.size() == 1) {
                    ArrayList<TTask> temp = new ArrayList<>();
                    if (input2Tasks.get(0).getId().equals(task.getId())){
                        temp.add(input2Tasks.get(1));
                        toRemove.add(input2Tasks.get(0));
                    } else {
                        temp.add(input2Tasks.get(0));
                        toRemove.add(input2Tasks.get(1));
                    }
                    removeFromProcess(reference, flowElements);
                    changeIoTDependantTask(temp, (TDataObjectReference) task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue(), isSensor, true, flowElements, collaboration, plane);

                } else if (inputTasks.size() > 1 && input2Tasks.size() == 1 && outputTasks.size() > 1) {
                    ArrayList<TTask> temp = new ArrayList<>();
                    if (inputTasks.get(0).getId().equals(task.getId())){
                        temp.add(inputTasks.get(1));
                        toRemove.add(inputTasks.get(0));
                    } else {
                        temp.add(inputTasks.get(0));
                        toRemove.add(inputTasks.get(1));
                    }
                    removeFromProcess(reference, flowElements);
                    changeIoTDependantTask(temp, (TDataObjectReference) task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue(), isSensor, true, flowElements, collaboration, plane);

                } else if (inputTasks.size() == 1 && input2Tasks.size() > 1 && outputTasks.size() > 1) {
                    ArrayList<TTask> temp = new ArrayList<>();
                    if (input2Tasks.get(0).getId().equals(task.getId())){
                        temp.add(input2Tasks.get(1));
                        toRemove.add(input2Tasks.get(0));
                        toRemove.add(outputTasks.get(0));
                    } else {
                        temp.add(input2Tasks.get(0));
                        toRemove.add(input2Tasks.get(1));
                        toRemove.add(outputTasks.get(1));
                    }
                    removeFromProcess(reference, flowElements);
                    changeIoTDependantTask(temp, (TDataObjectReference) task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue(), isSensor, true, flowElements, collaboration, plane);
                }else if (inputTasks.size() > 1  && !convertRestToIntermediate) {
                    ArrayList<TTask> temp = new ArrayList<>();
                    if (inputTasks.get(0).getId().equals(task.getId())){
                        temp.add(inputTasks.get(1));
                        toRemove.add(inputTasks.get(0));
                    } else {
                        temp.add(inputTasks.get(0));
                        toRemove.add(inputTasks.get(1));
                    }
                    removeFromProcess(reference, flowElements);
                    changeIoTDependantTask(temp, (TDataObjectReference) task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue(), isSensor, true, flowElements, collaboration, plane);
                }
                else if (input2Tasks.size() > 1  && !convertRestToIntermediate){
                    ArrayList<TTask> temp = new ArrayList<>();
                    if (input2Tasks.get(0).getId().equals(task.getId())){
                        temp.add(input2Tasks.get(1));
                        toRemove.add(input2Tasks.get(0));
                    } else {
                        temp.add(input2Tasks.get(0));
                        toRemove.add(input2Tasks.get(1));
                    }
                    removeFromProcess(reference,flowElements);
                    changeIoTDependantTask(temp,(TDataObjectReference) task.getDataInputAssociation().get(1).getSourceRef().get(0).getValue(),isSensor, true,flowElements,collaboration,plane);
                }else if (outputTasks.size() > 1  && !convertRestToIntermediate){
                    ArrayList<TTask> temp = new ArrayList<>();
                    if (outputTasks.get(0).getId().equals(task.getId())){
                        temp.add(outputTasks.get(1));
                        toRemove.add(outputTasks.get(0));
                    } else {
                        temp.add(outputTasks.get(0));
                        toRemove.add(outputTasks.get(1));
                    }
                    removeFromProcess(reference,flowElements);
                    changeIoTDependantTask(temp,(TDataObjectReference) task.getDataOutputAssociation().get(0).getTargetRef(),isSensor, true,flowElements,collaboration,plane);
                }

                removeFromProcess(task, flowElements);

            } else if (task.getDataOutputAssociation().size() == 1 && task.getDataInputAssociation().size() == 0) {
                List<TTask> outputTasks = getAssociatedTasks(getIDOfObject(task.getDataOutputAssociation().get(0).getTargetRef()), true, flowElements);
                if (outputTasks.size() == 1 && !convertRestToIntermediate) {
                    TServiceTask serviceTask = new TServiceTask();
                    serviceTask.setId(task.getId());
                    serviceTask.setName(task.getName());
                    serviceTask.getIncoming().addAll(task.getIncoming());
                    serviceTask.getOutgoing().addAll(task.getOutgoing());
                    serviceTask.getProperty().addAll(task.getProperty());

                    JAXBElement<TServiceTask> taskElement = objectFactory.createServiceTask(serviceTask);
                    removeFromProcess(task, flowElements);
                    counter++;
                    removeFromProcess(reference, flowElements);
                    flowElements.add(taskElement);
                } else if (outputTasks.size() == 1 && convertRestToIntermediate){
                    changeTaskToIntermediateMessageThrowEvent(task,flowElements,collaboration,plane);
                }  else if (outputTasks.size() > 1){
                    //create WhiteBox
                    replaceDataAssociationWithWhiteBox(reference.getId(), "Actuator (IoT)" + reference.getName(), flowElements, collaboration, plane);
                    if (outputTasks.get(1).getDataInputAssociation().size() == 1 && outputTasks.get(1).getDataOutputAssociation().size() == 0) {

                        List<TTask> tempTasks = new ArrayList<>();
                        tempTasks.add(outputTasks.get(1));
                        removeFromProcess(outputTasks.get(0),flowElements);
                        changeIoTDependantTask(tempTasks, reference, isSensor, false, flowElements, collaboration, plane);
                        //remove original reference
                        removeFromProcess(reference, flowElements);
                        for (TTask exTask : outputTasks) {
                            changeTaskToIntermediateMessageThrowEvent(exTask, flowElements, collaboration, plane);
                        }
                    } else {
                        removeFromProcess(reference, flowElements);
                        changeTaskToIntermediateMessageThrowEvent(outputTasks.get(0),flowElements,collaboration,plane);
                        changeTaskToIntermediateMessageThrowEvent(outputTasks.get(1),flowElements,collaboration,plane);
                    }
                }
            }
        }
        return counter;
    }

    /**
     * Removes all messageFlows from the associated Task.
     */
    private void removeTaskFlows(TTask task, TCollaboration collaboration) {
        for (TDataInputAssociation inputAssociation : task.getDataInputAssociation()) {
            removeFromCollaborationMessageFlow(inputAssociation.getId(), task.getId(), collaboration);
        }
        for (TDataOutputAssociation outputAssociation : task.getDataOutputAssociation()) {
            removeFromCollaborationMessageFlow(outputAssociation.getId(), task.getId(), collaboration);
        }
    }

    private Triplet<TBusinessRuleTask, TServiceTask, TBusinessRuleTask> replaceOneSensorOneCatchArtefactOneActuatorAssociation(TTask task, TDataObjectReference reference, List<JAXBElement<? extends TFlowElement>> flowElements, TCollaboration collaboration, BPMNPlane plane) {
        List<TTask> outputTasks = getAssociatedTasks(getIDOfObject(task.getDataOutputAssociation().get(0).getTargetRef()), true, flowElements);
        List<TTask> inputTasksSensor = new ArrayList<>();
        List<TTask> inputTasksCatch = new ArrayList<>();
        List<TDataObjectReference> sensors = new ArrayList<>();
        List<TDataObjectReference> artefact = new ArrayList<>();
        List<TDataInputAssociation> sensorInputs = new ArrayList<>();
        List<TDataInputAssociation> artefactInputs = new ArrayList<>();
        List<TTask> input1Tasks = getAssociatedTasks(getIDOfObject(task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()), false, flowElements);
        for (int i = 0; i < input1Tasks.size(); i++) {
            for (int j = 0; j < input1Tasks.get(i).getDataInputAssociation().size(); j++) {
                Object keyItem = ((TDataObjectReference) input1Tasks.get(i).getDataInputAssociation().get(j).getSourceRef().get(0).getValue()).getOtherAttributes().keySet().toArray()[0];
                if (((TDataObjectReference) input1Tasks.get(i).getDataInputAssociation().get(j).getSourceRef().get(0).getValue()).getOtherAttributes().get(keyItem).equals("artefact-catch")) {
                    inputTasksCatch.add(input1Tasks.get(i));
                    artefact.add((TDataObjectReference)input1Tasks.get(i).getDataInputAssociation().get(j).getSourceRef().get(0).getValue());
                    artefactInputs.add(input1Tasks.get(i).getDataInputAssociation().get(j));
                } else {
                    inputTasksSensor.add(input1Tasks.get(i));
                    sensors.add((TDataObjectReference)input1Tasks.get(i).getDataInputAssociation().get(j).getSourceRef().get(0).getValue());
                    sensorInputs.add(input1Tasks.get(i).getDataInputAssociation().get(j));
                }
            }
        }
        List<TTask> input2Tasks = getAssociatedTasks(getIDOfObject(task.getDataInputAssociation().get(1).getSourceRef().get(0).getValue()), false, flowElements);
        for (int i = 0; i < input2Tasks.size(); i++) {
            for (int j = 0; j < input2Tasks.get(i).getDataInputAssociation().size(); j++) {
                Object keyItem = ((TDataObjectReference) input2Tasks.get(i).getDataInputAssociation().get(j).getSourceRef().get(0).getValue()).getOtherAttributes().keySet().toArray()[0];
                if (((TDataObjectReference) input2Tasks.get(i).getDataInputAssociation().get(j).getSourceRef().get(0).getValue()).getOtherAttributes().get(keyItem).equals("artefact-catch")) {
                    if (!artefactInputs.contains(input2Tasks.get(i).getDataInputAssociation().get(j)))
                        artefactInputs.add(input2Tasks.get(i).getDataInputAssociation().get(j));
                    if (!inputTasksCatch.contains(input2Tasks.get(i)))
                        inputTasksCatch.add(input2Tasks.get(i));
                    if (!artefact.contains((TDataObjectReference)input2Tasks.get(i).getDataInputAssociation().get(j).getSourceRef().get(0).getValue()))
                        artefact.add((TDataObjectReference)input2Tasks.get(i).getDataInputAssociation().get(j).getSourceRef().get(0).getValue());
                } else {
                    if (!sensorInputs.contains(input2Tasks.get(i).getDataInputAssociation().get(j)))
                        sensorInputs.add(input2Tasks.get(i).getDataInputAssociation().get(j));
                    if (!inputTasksSensor.contains(input2Tasks.get(i)))
                        inputTasksSensor.add(input2Tasks.get(i));
                    if (!sensors.contains((TDataObjectReference)input2Tasks.get(i).getDataInputAssociation().get(j).getSourceRef().get(0).getValue()))
                        sensors.add((TDataObjectReference)input2Tasks.get(i).getDataInputAssociation().get(j).getSourceRef().get(0).getValue());
                }
            }
        }

        String leftGW_Id = "Gateway_" + randomNumberSequence();
        String rightGW_Id = "Gateway_" + randomNumberSequence();
        String leftXor_Id = "Gateway_" + randomNumberSequence();
        String rightXor_Id = "Gateway_" + randomNumberSequence();
        String rule_Id = "Activity_" + randomNumberSequence();
        String catch_Id = "Activity_" + randomNumberSequence();
        String service_Id = "Activity_" + randomNumberSequence();
        String leftFlow_1_Id = "Flow_" + randomNumberSequence();
        String leftFlow_2_Id = "Flow_" + randomNumberSequence();
        String leftFlow_3_Id = "Flow_" + randomNumberSequence();
        String rightFlow_1_Id = "Flow_" + randomNumberSequence();
        String rightFlow_2_Id = "Flow_" + randomNumberSequence();
        String rightFlow_3_Id = "Flow_" + randomNumberSequence();

        String xorLeftFlow = "Flow_" + randomNumberSequence();
        String catchRightFlow = "Flow_" + randomNumberSequence();
        String xorElseFlow = "Flow_" + randomNumberSequence();

        //create Object
        TBusinessRuleTask businessRuleTask = new TBusinessRuleTask();
        TBusinessRuleTask businessRuleTaskCatch = new TBusinessRuleTask();
        TServiceTask serviceTask = new TServiceTask();
        TParallelGateway leftGateWay = new TParallelGateway();
        TParallelGateway rightGateWay = new TParallelGateway();
        TExclusiveGateway xorGateLeft = new TExclusiveGateway();
        TExclusiveGateway xorGateRight = new TExclusiveGateway();
        TSequenceFlow leftFlow_1 = new TSequenceFlow();
        TSequenceFlow leftFlow_2 = new TSequenceFlow();
        TSequenceFlow leftFlow_3 = new TSequenceFlow();
        TSequenceFlow rightFlow_1 = new TSequenceFlow();
        TSequenceFlow rightFlow_2 = new TSequenceFlow();
        TSequenceFlow rightFlow_3 = new TSequenceFlow();
        TSequenceFlow xorLeftSequenceFlow = new TSequenceFlow();
        TSequenceFlow catchRightSequenceFlow = new TSequenceFlow();
        TSequenceFlow xorElseSequenceFlow = new TSequenceFlow();

        //change incoming and outgoing flow attributes
        for (int k = 0; k < task.getIncoming().size(); k++) {
            TSequenceFlow incoming = getFlow(task.getIncoming().get(k).getLocalPart(), flowElements);
            assert incoming != null;
            incoming.setTargetRef(leftGateWay);
        }
        for (int k = 0; k < task.getOutgoing().size(); k++) {
            TSequenceFlow outgoing = getFlow(task.getOutgoing().get(k).getLocalPart(), flowElements);
            assert outgoing != null;
            outgoing.setSourceRef(rightGateWay);
        }

        //create business task
        businessRuleTask.setId(rule_Id);
        businessRuleTask.setName("(IoT) " + task.getName());
        businessRuleTask.getIncoming().add(QName.valueOf(leftFlow_2_Id));
        businessRuleTask.getOutgoing().add(QName.valueOf(rightFlow_2_Id));
        businessRuleTask.getDataInputAssociation().addAll(sensorInputs);
        businessRuleTask.getProperty().addAll(task.getProperty());

        //create business task
        businessRuleTaskCatch.setId(catch_Id);
        businessRuleTaskCatch.setName("(IoT) " + task.getName());
        businessRuleTaskCatch.getIncoming().add(QName.valueOf(xorLeftFlow));
        businessRuleTaskCatch.getOutgoing().add(QName.valueOf(catchRightFlow));
        businessRuleTaskCatch.getDataInputAssociation().addAll(artefactInputs);
        businessRuleTaskCatch.getProperty().addAll(task.getProperty());


        //create service task
        serviceTask.setId(service_Id);
        serviceTask.setName("(IoT) " + task.getName());
        serviceTask.getIncoming().add(QName.valueOf(leftFlow_1_Id));
        serviceTask.getOutgoing().add(QName.valueOf(rightFlow_1_Id));
        serviceTask.getDataOutputAssociation().addAll(task.getDataOutputAssociation());
        serviceTask.getProperty().addAll(task.getProperty());

        //create SequenceFlows
        leftFlow_1.setId(leftFlow_1_Id);
        leftFlow_1.setSourceRef(leftGateWay);
        leftFlow_1.setTargetRef(serviceTask);

        leftFlow_2.setId(leftFlow_2_Id);
        leftFlow_2.setSourceRef(leftGateWay);
        leftFlow_2.setTargetRef(businessRuleTask);

        leftFlow_3.setId(leftFlow_3_Id);
        leftFlow_3.setSourceRef(leftGateWay);
        leftFlow_3.setTargetRef(xorGateLeft);

        rightFlow_1.setId(rightFlow_2_Id);
        rightFlow_1.setSourceRef(serviceTask);
        rightFlow_1.setTargetRef(rightGateWay);

        rightFlow_2.setId(rightFlow_1_Id);
        rightFlow_2.setSourceRef(businessRuleTask);
        rightFlow_2.setTargetRef(rightGateWay);

        rightFlow_3.setId(rightFlow_3_Id);
        rightFlow_3.setSourceRef(xorGateRight);
        rightFlow_3.setTargetRef(rightGateWay);
        List<TDataObjectReference> references = getReferences(getIDOfObject(task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()),flowElements);
        String name = "";
        for (TDataObjectReference referenceItem: references){
            if (referenceItem.getName() != null) {
                if (referenceItem.getName().contains("<") || referenceItem.getName().contains(">") || referenceItem.getName().contains("=")) {
                    name = referenceItem.getName();
                    break;
                }
            } else {
                name = "if";
            }
        }
        rightFlow_3.setName(name);

        xorLeftSequenceFlow.setId(xorLeftFlow);
        xorLeftSequenceFlow.setSourceRef(xorGateLeft);
        xorLeftSequenceFlow.setTargetRef(businessRuleTaskCatch);

        catchRightSequenceFlow.setId(catchRightFlow);
        catchRightSequenceFlow.setSourceRef(businessRuleTaskCatch);
        catchRightSequenceFlow.setTargetRef(xorGateRight);

        xorElseSequenceFlow.setId(xorElseFlow);
        xorElseSequenceFlow.setSourceRef(xorGateRight);
        xorElseSequenceFlow.setTargetRef(xorGateLeft);
        xorElseSequenceFlow.setName("else");

        //create parallel Gateways
        leftGateWay.setId(leftGW_Id);
        leftGateWay.getIncoming().addAll(task.getIncoming());
        leftGateWay.getOutgoing().add(QName.valueOf(leftFlow_1.getId()));
        leftGateWay.getOutgoing().add(QName.valueOf(leftFlow_2.getId()));

        rightGateWay.setId(rightGW_Id);
        rightGateWay.getIncoming().add(QName.valueOf(rightFlow_1.getId()));
        rightGateWay.getIncoming().add(QName.valueOf(rightFlow_2.getId()));
        rightGateWay.getOutgoing().addAll(task.getOutgoing());

        xorGateLeft.setId(leftXor_Id);
        xorGateLeft.getIncoming().add(QName.valueOf(leftFlow_3.getId()));
        xorGateLeft.getIncoming().add(QName.valueOf(xorElseSequenceFlow.getId()));
        xorGateLeft.getOutgoing().add(QName.valueOf(xorLeftSequenceFlow.getId()));

        xorGateRight.setId(rightXor_Id);
        xorGateRight.getIncoming().add(QName.valueOf(catchRightSequenceFlow.getId()));
        xorGateRight.getOutgoing().add(QName.valueOf(xorElseSequenceFlow.getId()));
        xorGateRight.getOutgoing().add(QName.valueOf(rightFlow_3.getId()));

        //add elements to the process
        flowElements.add(objectFactory.createBusinessRuleTask(businessRuleTask));
        flowElements.add(objectFactory.createBusinessRuleTask(businessRuleTaskCatch));
        flowElements.add(objectFactory.createServiceTask(serviceTask));
        flowElements.add(objectFactory.createParallelGateway(leftGateWay));
        flowElements.add(objectFactory.createParallelGateway(rightGateWay));
        flowElements.add(objectFactory.createExclusiveGateway(xorGateLeft));
        flowElements.add(objectFactory.createExclusiveGateway(xorGateRight));
        flowElements.add(objectFactory.createSequenceFlow(leftFlow_1));
        flowElements.add(objectFactory.createSequenceFlow(leftFlow_2));
        flowElements.add(objectFactory.createSequenceFlow(leftFlow_3));
        flowElements.add(objectFactory.createSequenceFlow(rightFlow_1));
        flowElements.add(objectFactory.createSequenceFlow(rightFlow_2));
        flowElements.add(objectFactory.createSequenceFlow(rightFlow_3));
        flowElements.add(objectFactory.createSequenceFlow(xorElseSequenceFlow));
        flowElements.add(objectFactory.createSequenceFlow(catchRightSequenceFlow));
        flowElements.add(objectFactory.createSequenceFlow(xorLeftSequenceFlow));

        BPMNShape taskShape = (BPMNShape) getShapeOrEdge(task.getId(), plane);
        removeFromProcess(task,flowElements);

        org.omg.spec.bpmn._20100524.di.ObjectFactory planeObjectFactory = new org.omg.spec.bpmn._20100524.di.ObjectFactory();

        moveEverythingUp(taskShape.getBounds().getY(),100,plane);
        moveEverythingDown(taskShape.getBounds().getY()+taskShape.getBounds().getHeight(),300,plane);
        moveEverythingToRight(taskShape.getBounds().getX(),400,plane);
        taskShape.getBounds().setX(taskShape.getBounds().getX()+150);

        //create the shapes
        BPMNShape ruleShape = new BPMNShape();
        Bounds ruleBounds = new Bounds();
        ruleShape.setBounds(ruleBounds);
        ruleShape.setId(businessRuleTask.getId() + "_di");
        ruleShape.setBpmnElement(QName.valueOf(businessRuleTask.getId()));
        ruleShape.getBounds().setX(taskShape.getBounds().getX());
        ruleShape.getBounds().setY(taskShape.getBounds().getY() - 100);
        ruleShape.getBounds().setHeight(80);
        ruleShape.getBounds().setWidth(100);
        plane.getDiagramElement().add(planeObjectFactory.createBPMNShape(ruleShape));

        BPMNShape catchShape = new BPMNShape();
        Bounds catchBounds = new Bounds();
        catchShape.setBounds(catchBounds);
        catchShape.setId(businessRuleTaskCatch.getId() + "_di");
        catchShape.setBpmnElement(QName.valueOf(businessRuleTaskCatch.getId()));
        catchShape.getBounds().setX(taskShape.getBounds().getX());
        catchShape.getBounds().setY(taskShape.getBounds().getY());
        catchShape.getBounds().setHeight(80);
        catchShape.getBounds().setWidth(100);
        plane.getDiagramElement().add(planeObjectFactory.createBPMNShape(catchShape));

        BPMNShape serviceShape = new BPMNShape();
        Bounds serviceBounds = new Bounds();
        serviceShape.setBounds(serviceBounds);
        serviceShape.setId(serviceTask.getId() + "_di");
        serviceShape.setBpmnElement(QName.valueOf(serviceTask.getId()));
        serviceShape.getBounds().setX(taskShape.getBounds().getX());
        serviceShape.getBounds().setY(taskShape.getBounds().getY() + 150);
        serviceShape.getBounds().setHeight(80);
        serviceShape.getBounds().setWidth(100);
        plane.getDiagramElement().add(planeObjectFactory.createBPMNShape(serviceShape));

        BPMNShape leftGateShape = new BPMNShape();
        Bounds leftGateBounds = new Bounds();
        leftGateShape.setBounds(leftGateBounds);
        leftGateShape.setId(leftGateWay.getId() + "_di");
        leftGateShape.setBpmnElement(QName.valueOf(leftGateWay.getId()));
        leftGateShape.getBounds().setX(taskShape.getBounds().getX() - 150);
        leftGateShape.getBounds().setY(taskShape.getBounds().getY() + taskShape.getBounds().getHeight() / 2 - 18);
        leftGateShape.getBounds().setHeight(36);
        leftGateShape.getBounds().setWidth(36);
        plane.getDiagramElement().add(planeObjectFactory.createBPMNShape(leftGateShape));

        BPMNShape rightGateShape = new BPMNShape();
        Bounds rightGateBounds = new Bounds();
        rightGateShape.setBounds(rightGateBounds);
        rightGateShape.setId(rightGateWay.getId() + "_di");
        rightGateShape.setBpmnElement(QName.valueOf(rightGateWay.getId()));
        rightGateShape.getBounds().setX(taskShape.getBounds().getX() + 150 + taskShape.getBounds().getWidth());
        rightGateShape.getBounds().setY(taskShape.getBounds().getY() + taskShape.getBounds().getHeight() / 2 - 18);
        rightGateShape.getBounds().setHeight(36);
        rightGateShape.getBounds().setWidth(36);
        plane.getDiagramElement().add(planeObjectFactory.createBPMNShape(rightGateShape));

        BPMNShape rightXorGateShape = new BPMNShape();
        Bounds rightXorGateBounds = new Bounds();
        rightXorGateShape.setBounds(rightXorGateBounds);
        rightXorGateShape.setId(xorGateRight.getId() + "_di");
        rightXorGateShape.setBpmnElement(QName.valueOf(xorGateRight.getId()));
        rightXorGateShape.getBounds().setX(catchShape.getBounds().getX() + catchShape.getBounds().getWidth() + 50);
        rightXorGateShape.getBounds().setY(catchShape.getBounds().getY() + taskShape.getBounds().getHeight() / 2 - 18);
        rightXorGateShape.getBounds().setHeight(36);
        rightXorGateShape.getBounds().setWidth(36);
        plane.getDiagramElement().add(planeObjectFactory.createBPMNShape(rightXorGateShape));

        BPMNShape leftXorGateShape = new BPMNShape();
        Bounds leftXorGateBounds = new Bounds();
        leftXorGateShape.setBounds(leftXorGateBounds);
        leftXorGateShape.setId(xorGateLeft.getId() + "_di");
        leftXorGateShape.setBpmnElement(QName.valueOf(xorGateLeft.getId()));
        leftXorGateShape.getBounds().setX(catchShape.getBounds().getX() - 50 - 36);
        leftXorGateShape.getBounds().setY(catchShape.getBounds().getY() + taskShape.getBounds().getHeight() / 2 - 18);
        leftXorGateShape.getBounds().setHeight(36);
        leftXorGateShape.getBounds().setWidth(36);
        plane.getDiagramElement().add(planeObjectFactory.createBPMNShape(leftXorGateShape));


        //adjust the already existing edges
        for (int k = 0; k < task.getIncoming().size(); k++) {
            TSequenceFlow incoming = getFlow(task.getIncoming().get(k).getLocalPart(), flowElements);
            assert incoming != null;
            BPMNEdge incomingEdge = (BPMNEdge) getShapeOrEdge(incoming.getId(), plane);
            assert incomingEdge != null;
            incomingEdge.getWaypoint().get(incomingEdge.getWaypoint().size() - 1).setX(leftGateShape.getBounds().getX());
        }

        for (int k = 0; k < task.getOutgoing().size(); k++) {
            TSequenceFlow outgoing = getFlow(task.getOutgoing().get(k).getLocalPart(), flowElements);
            assert outgoing != null;
            BPMNEdge outgoingEdge = (BPMNEdge) getShapeOrEdge(outgoing.getId(), plane);
            if (outgoingEdge != null) {
                outgoingEdge.getWaypoint().get(0).setX(rightGateShape.getBounds().getX() + rightGateShape.getBounds().getWidth());
            }
        }


        //create missing edges
        int[] leftFlow1X = new int[3];
        int[] leftFlow1Y = new int[3];
        leftFlow1X[0] = (int) (leftGateShape.getBounds().getX() + leftGateShape.getBounds().getWidth() / 2);
        leftFlow1X[1] = (int) (leftGateShape.getBounds().getX() + leftGateShape.getBounds().getWidth() / 2);
        leftFlow1X[2] = (int) (serviceShape.getBounds().getX());

        leftFlow1Y[0] = (int) (leftGateShape.getBounds().getY() + leftGateShape.getBounds().getHeight());
        leftFlow1Y[1] = (int) (serviceShape.getBounds().getY() + serviceShape.getBounds().getHeight() / 2);
        leftFlow1Y[2] = (int) (serviceShape.getBounds().getY() + serviceShape.getBounds().getHeight() / 2);

        plane.getDiagramElement().add(createEdge(leftFlow_1.getId(), leftFlow1X, leftFlow1Y));

        int[] leftFlow2X = new int[3];
        int[] leftFlow2Y = new int[3];
        leftFlow2X[0] = (int) (leftGateShape.getBounds().getX() + leftGateShape.getBounds().getWidth() / 2);
        leftFlow2X[1] = (int) (leftGateShape.getBounds().getX() + leftGateShape.getBounds().getWidth() / 2);
        leftFlow2X[2] = (int) (ruleShape.getBounds().getX());

        leftFlow2Y[0] = (int) (leftGateShape.getBounds().getY() + leftGateShape.getBounds().getHeight());
        leftFlow2Y[1] = (int) (ruleShape.getBounds().getY() + ruleShape.getBounds().getHeight() / 2);
        leftFlow2Y[2] = (int) (ruleShape.getBounds().getY() + ruleShape.getBounds().getHeight() / 2);

        plane.getDiagramElement().add(createEdge(leftFlow_2.getId(), leftFlow2X, leftFlow2Y));

        int[] rightFlow1X = new int[3];
        int[] rightFlow1Y = new int[3];
        rightFlow1X[0] = (int) (ruleShape.getBounds().getX() + ruleShape.getBounds().getWidth());
        rightFlow1X[1] = (int) (rightGateShape.getBounds().getX() + rightGateShape.getBounds().getWidth() / 2);
        rightFlow1X[2] = (int) (rightGateShape.getBounds().getX() + rightGateShape.getBounds().getWidth() / 2);

        rightFlow1Y[0] = (int) (ruleShape.getBounds().getY() + ruleShape.getBounds().getHeight() / 2);
        rightFlow1Y[1] = (int) (ruleShape.getBounds().getY() + ruleShape.getBounds().getHeight() / 2);
        rightFlow1Y[2] = (int) (rightGateShape.getBounds().getY());

        plane.getDiagramElement().add(createEdge(rightFlow_1.getId(), rightFlow1X, rightFlow1Y));

        int[] rightFlow2X = new int[3];
        int[] rightFlow2Y = new int[3];
        rightFlow2X[0] = (int) (serviceShape.getBounds().getX() + serviceShape.getBounds().getWidth());
        rightFlow2X[1] = (int) (rightGateShape.getBounds().getX() + rightGateShape.getBounds().getWidth() / 2);
        rightFlow2X[2] = (int) (rightGateShape.getBounds().getX() + rightGateShape.getBounds().getWidth() / 2);

        rightFlow2Y[0] = (int) (serviceShape.getBounds().getY() + serviceShape.getBounds().getHeight() / 2);
        rightFlow2Y[1] = (int) (serviceShape.getBounds().getY() + serviceShape.getBounds().getHeight() / 2);
        rightFlow2Y[2] = (int) (rightGateShape.getBounds().getY() + rightGateShape.getBounds().getHeight());

        plane.getDiagramElement().add(createEdge(rightFlow_2.getId(), rightFlow2X, rightFlow2Y));

        BPMNEdge andFlow3Left = new BPMNEdge();
        andFlow3Left.setId(leftFlow_3.getId()+"_di");
        andFlow3Left.setBpmnElement(QName.valueOf(leftFlow_3.getId()));
        andFlow3Left.getWaypoint().add(new Point(leftGateBounds.getX()+leftGateBounds.getWidth(),leftGateBounds.getY()+leftGateBounds.getHeight()/2));
        andFlow3Left.getWaypoint().add(new Point(leftXorGateBounds.getX(),leftXorGateBounds.getY()+leftXorGateBounds.getHeight()/2));

        BPMNEdge andFlow3Right = new BPMNEdge();
        andFlow3Right.setId(rightFlow_3.getId()+"_di");
        andFlow3Right.setBpmnElement(QName.valueOf(rightFlow_3.getId()));
        andFlow3Right.getWaypoint().add(new Point(rightXorGateBounds.getX()+rightXorGateBounds.getWidth(),rightXorGateBounds.getY()+rightXorGateBounds.getHeight()/2));
        andFlow3Right.getWaypoint().add(new Point(rightGateBounds.getX(),rightGateBounds.getY()+rightGateBounds.getHeight()/2));

        BPMNEdge xorLeftEdge = new BPMNEdge();
        xorLeftEdge.setId(xorLeftSequenceFlow.getId()+"_di");
        xorLeftEdge.setBpmnElement(QName.valueOf(xorLeftSequenceFlow.getId()));
        xorLeftEdge.getWaypoint().add(new Point(leftXorGateBounds.getX()+leftGateBounds.getWidth(),leftXorGateBounds.getY()+leftXorGateBounds.getHeight()/2));
        xorLeftEdge.getWaypoint().add(new Point(catchBounds.getX(),catchBounds.getY()+catchBounds.getHeight()/2));

        BPMNEdge catchOutEdge = new BPMNEdge();
        catchOutEdge.setId(catchRightSequenceFlow.getId()+"_di");
        catchOutEdge.setBpmnElement(QName.valueOf(catchRightSequenceFlow.getId()));
        catchOutEdge.getWaypoint().add(new Point(catchBounds.getX()+catchBounds.getWidth(),catchBounds.getY()+catchBounds.getHeight()/2));
        catchOutEdge.getWaypoint().add(new Point(rightXorGateBounds.getX(),rightXorGateBounds.getY()+rightXorGateBounds.getHeight()/2));

        BPMNEdge xorElseEdge = new BPMNEdge();
        xorElseEdge.setId(xorElseSequenceFlow.getId()+"_di");
        xorElseEdge.setBpmnElement(QName.valueOf(xorElseSequenceFlow.getId()));
        xorElseEdge.getWaypoint().add(new Point(rightXorGateBounds.getX()+rightXorGateBounds.getWidth()/2,rightXorGateBounds.getY()+rightXorGateBounds.getHeight()));
        xorElseEdge.getWaypoint().add(new Point(rightXorGateBounds.getX()+rightXorGateBounds.getWidth()/2,rightXorGateBounds.getY()+rightXorGateBounds.getHeight()+50));
        xorElseEdge.getWaypoint().add(new Point(leftXorGateBounds.getX()+leftXorGateBounds.getWidth()/2,rightXorGateBounds.getY()+rightXorGateBounds.getHeight()+50));
        xorElseEdge.getWaypoint().add(new Point(leftXorGateBounds.getX()+leftXorGateBounds.getWidth()/2,leftXorGateBounds.getY()+leftXorGateBounds.getHeight()));



        org.omg.spec.bpmn._20100524.di.ObjectFactory objectFactoryEdge = new org.omg.spec.bpmn._20100524.di.ObjectFactory();

        plane.getDiagramElement().add(objectFactoryEdge.createBPMNEdge(andFlow3Left));
        plane.getDiagramElement().add(objectFactoryEdge.createBPMNEdge(andFlow3Right));
        plane.getDiagramElement().add(objectFactoryEdge.createBPMNEdge(xorLeftEdge));
        plane.getDiagramElement().add(objectFactoryEdge.createBPMNEdge(catchOutEdge));
        plane.getDiagramElement().add(objectFactoryEdge.createBPMNEdge(xorElseEdge));

        //remove all unneeded references and objects
        references = getReferences(getIDOfObject(task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()), flowElements);
        for (TDataObjectReference data : references) {
            removeFromProcess(data, flowElements);
        }
        List<TDataObjectReference> references2 = getReferences(getIDOfObject(task.getDataOutputAssociation().get(0).getTargetRef()), flowElements);
        for (TDataObjectReference data : references2) {
            removeFromProcess(data, flowElements);
        }
        if (inputTasksCatch.size() > 1 || isAlreadyAWhiteBox(artefact.get(0).getId(),collaboration)) {
            //create WhiteBox
            replaceDataAssociationWithWhiteBox(artefact.get(0).getId(), "Sensor (IoT)" + artefact.get(0).getName(), flowElements, collaboration, plane);
            //replace businessRuleTask to catch event
            changeTaskToIntermediateMessageCatchEvent(businessRuleTaskCatch, flowElements, collaboration, plane);

            //remove original reference
            removeFromProcess(artefact.get(0), flowElements);

        }
        if (inputTasksSensor.size() > 1 || isAlreadyAWhiteBox(sensors.get(0).getId(),collaboration)){
            //create WhiteBox
            replaceDataAssociationWithWhiteBox(sensors.get(0).getId(), "Sensor (IoT)" + sensors.get(0).getName(), flowElements, collaboration, plane);
            //replace businessRuleTask to catch event
            changeTaskToIntermediateMessageCatchEvent(businessRuleTask, flowElements, collaboration, plane);

            //remove original reference
            removeFromProcess(sensors.get(0), flowElements);
        }
        if (outputTasks.size() > 1 || isAlreadyAWhiteBox(getIDOfObject(task.getDataOutputAssociation().get(0).getTargetRef()),collaboration)) {
            //create WhiteBox
            replaceDataAssociationWithWhiteBox(getIDOfObject(task.getDataOutputAssociation().get(0).getTargetRef()), "Actuator (IoT)" + ((TDataObjectReference)task.getDataOutputAssociation().get(0).getTargetRef()).getName(), flowElements, collaboration, plane);
            //replace serviceTask to throw event
            changeTaskToIntermediateMessageThrowEvent(serviceTask, flowElements, collaboration, plane);

            //remove original reference
            removeFromProcess(task.getDataOutputAssociation().get(0), flowElements);

        }
        return new Triplet<>(businessRuleTask, serviceTask, businessRuleTaskCatch);
    }


    private Pair<TBusinessRuleTask, TServiceTask> replaceOneInputAndOneOutputAssociation(TTask task,  List<JAXBElement<? extends TFlowElement>> flowElements, TCollaboration collaboration, BPMNPlane plane) {
        List<TTask> outputTasks = getAssociatedTasks(getIDOfObject(task.getDataOutputAssociation().get(0).getTargetRef()), true, flowElements);
        List<TTask> inputTasks = getAssociatedTasks(getIDOfObject(task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()), false, flowElements);

        String leftGW_Id = "Gateway_" + randomNumberSequence();
        String rightGW_Id = "Gateway_" + randomNumberSequence();
        String rule_Id = "Activity_" + randomNumberSequence();
        String service_Id = "Activity_" + randomNumberSequence();
        String leftFlow_1_Id = "Flow_" + randomNumberSequence();
        String leftFlow_2_Id = "Flow_" + randomNumberSequence();
        String rightFlow_1_Id = "Flow_" + randomNumberSequence();
        String rightFlow_2_Id = "Flow_" + randomNumberSequence();

        //create Object
        TBusinessRuleTask businessRuleTask = new TBusinessRuleTask();
        TServiceTask serviceTask = new TServiceTask();
        TParallelGateway leftGateWay = new TParallelGateway();
        TParallelGateway rightGateWay = new TParallelGateway();
        TSequenceFlow leftFlow_1 = new TSequenceFlow();
        TSequenceFlow leftFlow_2 = new TSequenceFlow();
        TSequenceFlow rightFlow_1 = new TSequenceFlow();
        TSequenceFlow rightFlow_2 = new TSequenceFlow();

        //change incoming and outgoing flow attributes
        for (int k = 0; k < task.getIncoming().size(); k++) {
            TSequenceFlow incoming = getFlow(task.getIncoming().get(k).getLocalPart(), flowElements);
            assert incoming != null;
            incoming.setTargetRef(leftGateWay);
        }
        for (int k = 0; k < task.getOutgoing().size(); k++) {
            TSequenceFlow outgoing = getFlow(task.getOutgoing().get(k).getLocalPart(), flowElements);
            assert outgoing != null;
            outgoing.setSourceRef(rightGateWay);
        }

        //create business task
        businessRuleTask.setId(rule_Id);
        businessRuleTask.setName("(IoT) " + task.getName());
        businessRuleTask.getIncoming().add(QName.valueOf(leftFlow_2_Id));
        businessRuleTask.getOutgoing().add(QName.valueOf(rightFlow_2_Id));
        businessRuleTask.getDataInputAssociation().addAll(task.getDataInputAssociation());
        businessRuleTask.getProperty().addAll(task.getProperty());


        //create service task
        serviceTask.setId(service_Id);
        serviceTask.setName("(IoT) " + task.getName());
        serviceTask.getIncoming().add(QName.valueOf(leftFlow_1_Id));
        serviceTask.getOutgoing().add(QName.valueOf(rightFlow_1_Id));
        serviceTask.getDataOutputAssociation().addAll(task.getDataOutputAssociation());
        serviceTask.getProperty().addAll(task.getProperty());

        //create SequenceFlows
        leftFlow_1.setId(leftFlow_1_Id);
        leftFlow_1.setSourceRef(leftGateWay);
        leftFlow_1.setTargetRef(serviceTask);

        leftFlow_2.setId(leftFlow_2_Id);
        leftFlow_2.setSourceRef(leftGateWay);
        leftFlow_2.setTargetRef(businessRuleTask);

        rightFlow_1.setId(rightFlow_2_Id);
        rightFlow_1.setSourceRef(serviceTask);
        rightFlow_1.setTargetRef(rightGateWay);

        rightFlow_2.setId(rightFlow_1_Id);
        rightFlow_2.setSourceRef(businessRuleTask);
        rightFlow_2.setTargetRef(rightGateWay);

        //create parallel Gateways
        leftGateWay.setId(leftGW_Id);
        leftGateWay.getIncoming().addAll(task.getIncoming());
        leftGateWay.getOutgoing().add(QName.valueOf(leftFlow_1.getId()));
        leftGateWay.getOutgoing().add(QName.valueOf(leftFlow_2.getId()));

        rightGateWay.setId(rightGW_Id);
        rightGateWay.getIncoming().add(QName.valueOf(rightFlow_1.getId()));
        rightGateWay.getIncoming().add(QName.valueOf(rightFlow_2.getId()));
        rightGateWay.getOutgoing().addAll(task.getOutgoing());

        //add elements to the process

        flowElements.add(objectFactory.createBusinessRuleTask(businessRuleTask));
        flowElements.add(objectFactory.createServiceTask(serviceTask));
        flowElements.add(objectFactory.createParallelGateway(leftGateWay));
        flowElements.add(objectFactory.createParallelGateway(rightGateWay));
        flowElements.add(objectFactory.createSequenceFlow(leftFlow_1));
        flowElements.add(objectFactory.createSequenceFlow(leftFlow_2));
        flowElements.add(objectFactory.createSequenceFlow(rightFlow_1));
        flowElements.add(objectFactory.createSequenceFlow(rightFlow_2));

        BPMNShape taskShape = (BPMNShape) getShapeOrEdge(task.getId(), plane);
        moveEverythingDown(taskShape.getBounds().getY()+taskShape.getBounds().getHeight(),50,plane);
        moveEverythingUp(taskShape.getBounds().getY(),50,plane);
        moveEverythingToRight(taskShape.getBounds().getX()+taskShape.getBounds().getWidth(),100,plane);
        taskShape.getBounds().setX(taskShape.getBounds().getX()+50);

        org.omg.spec.bpmn._20100524.di.ObjectFactory planeObjectFactory = new org.omg.spec.bpmn._20100524.di.ObjectFactory();

        //create the shapes
        BPMNShape ruleShape = new BPMNShape();
        Bounds ruleBounds = new Bounds();
        ruleShape.setBounds(ruleBounds);
        ruleShape.setId(businessRuleTask.getId() + "_di");
        ruleShape.setBpmnElement(QName.valueOf(businessRuleTask.getId()));
        ruleShape.getBounds().setX(taskShape.getBounds().getX());
        ruleShape.getBounds().setY(taskShape.getBounds().getY() - 50);
        ruleShape.getBounds().setHeight(80);
        ruleShape.getBounds().setWidth(100);
        plane.getDiagramElement().add(planeObjectFactory.createBPMNShape(ruleShape));

        BPMNShape serviceShape = new BPMNShape();
        Bounds serviceBounds = new Bounds();
        serviceShape.setBounds(serviceBounds);
        serviceShape.setId(serviceTask.getId() + "_di");
        serviceShape.setBpmnElement(QName.valueOf(serviceTask.getId()));
        serviceShape.getBounds().setX(taskShape.getBounds().getX());
        serviceShape.getBounds().setY(taskShape.getBounds().getY() + 40);
        serviceShape.getBounds().setHeight(80);
        serviceShape.getBounds().setWidth(100);
        plane.getDiagramElement().add(planeObjectFactory.createBPMNShape(serviceShape));

        BPMNShape leftGateShape = new BPMNShape();
        Bounds leftGateBounds = new Bounds();
        leftGateShape.setBounds(leftGateBounds);
        leftGateShape.setId(leftGateWay.getId() + "_di");
        leftGateShape.setBpmnElement(QName.valueOf(leftGateWay.getId()));
        leftGateShape.getBounds().setX(taskShape.getBounds().getX() - 50);
        leftGateShape.getBounds().setY(taskShape.getBounds().getY() + taskShape.getBounds().getHeight() / 2 - 18);
        leftGateShape.getBounds().setHeight(36);
        leftGateShape.getBounds().setWidth(36);
        plane.getDiagramElement().add(planeObjectFactory.createBPMNShape(leftGateShape));

        BPMNShape rightGateShape = new BPMNShape();
        Bounds rightGateBounds = new Bounds();
        rightGateShape.setBounds(rightGateBounds);
        rightGateShape.setId(rightGateWay.getId() + "_di");
        rightGateShape.setBpmnElement(QName.valueOf(rightGateWay.getId()));
        rightGateShape.getBounds().setX(taskShape.getBounds().getX() + 25 + taskShape.getBounds().getWidth());
        rightGateShape.getBounds().setY(taskShape.getBounds().getY() + taskShape.getBounds().getHeight() / 2 - 18);
        rightGateShape.getBounds().setHeight(36);
        rightGateShape.getBounds().setWidth(36);
        plane.getDiagramElement().add(planeObjectFactory.createBPMNShape(rightGateShape));


        //adjust the already existing edges
        for (int k = 0; k < task.getIncoming().size(); k++) {
            TSequenceFlow incoming = getFlow(task.getIncoming().get(k).getLocalPart(), flowElements);
            assert incoming != null;
            BPMNEdge incomingEdge = (BPMNEdge) getShapeOrEdge(incoming.getId(), plane);
            assert incomingEdge != null;
            incomingEdge.getWaypoint().get(incomingEdge.getWaypoint().size() - 1).setX(leftGateShape.getBounds().getX());
        }

        for (int k = 0; k < task.getOutgoing().size(); k++) {
            TSequenceFlow outgoing = getFlow(task.getOutgoing().get(k).getLocalPart(), flowElements);
            assert outgoing != null;
            BPMNEdge outgoingEdge = (BPMNEdge) getShapeOrEdge(outgoing.getId(), plane);
            if (outgoingEdge != null) {
                outgoingEdge.getWaypoint().get(0).setX(rightGateShape.getBounds().getX() + rightGateShape.getBounds().getWidth());
            }
        }


        //create missing edges
        int[] leftFlow1X = new int[3];
        int[] leftFlow1Y = new int[3];
        leftFlow1X[0] = (int) (leftGateShape.getBounds().getX() + leftGateShape.getBounds().getWidth() / 2);
        leftFlow1X[1] = (int) (leftGateShape.getBounds().getX() + leftGateShape.getBounds().getWidth() / 2);
        leftFlow1X[2] = (int) (serviceShape.getBounds().getX());

        leftFlow1Y[0] = (int) (leftGateShape.getBounds().getY() + leftGateShape.getBounds().getHeight());
        leftFlow1Y[1] = (int) (serviceShape.getBounds().getY() + serviceShape.getBounds().getHeight() / 2);
        leftFlow1Y[2] = (int) (serviceShape.getBounds().getY() + serviceShape.getBounds().getHeight() / 2);

        plane.getDiagramElement().add(createEdge(leftFlow_1.getId(), leftFlow1X, leftFlow1Y));

        int[] leftFlow2X = new int[3];
        int[] leftFlow2Y = new int[3];
        leftFlow2X[0] = (int) (leftGateShape.getBounds().getX() + leftGateShape.getBounds().getWidth() / 2);
        leftFlow2X[1] = (int) (leftGateShape.getBounds().getX() + leftGateShape.getBounds().getWidth() / 2);
        leftFlow2X[2] = (int) (ruleShape.getBounds().getX());

        leftFlow2Y[0] = (int) (leftGateShape.getBounds().getY() + leftGateShape.getBounds().getHeight());
        leftFlow2Y[1] = (int) (ruleShape.getBounds().getY() + ruleShape.getBounds().getHeight() / 2);
        leftFlow2Y[2] = (int) (ruleShape.getBounds().getY() + ruleShape.getBounds().getHeight() / 2);

        plane.getDiagramElement().add(createEdge(leftFlow_2.getId(), leftFlow2X, leftFlow2Y));

        int[] rightFlow1X = new int[3];
        int[] rightFlow1Y = new int[3];
        rightFlow1X[0] = (int) (ruleShape.getBounds().getX() + ruleShape.getBounds().getWidth());
        rightFlow1X[1] = (int) (rightGateShape.getBounds().getX() + rightGateShape.getBounds().getWidth() / 2);
        rightFlow1X[2] = (int) (rightGateShape.getBounds().getX() + rightGateShape.getBounds().getWidth() / 2);

        rightFlow1Y[0] = (int) (ruleShape.getBounds().getY() + ruleShape.getBounds().getHeight() / 2);
        rightFlow1Y[1] = (int) (ruleShape.getBounds().getY() + ruleShape.getBounds().getHeight() / 2);
        rightFlow1Y[2] = (int) (rightGateShape.getBounds().getY());

        plane.getDiagramElement().add(createEdge(rightFlow_1.getId(), rightFlow1X, rightFlow1Y));

        int[] rightFlow2X = new int[3];
        int[] rightFlow2Y = new int[3];
        rightFlow2X[0] = (int) (serviceShape.getBounds().getX() + serviceShape.getBounds().getWidth());
        rightFlow2X[1] = (int) (rightGateShape.getBounds().getX() + rightGateShape.getBounds().getWidth() / 2);
        rightFlow2X[2] = (int) (rightGateShape.getBounds().getX() + rightGateShape.getBounds().getWidth() / 2);

        rightFlow2Y[0] = (int) (serviceShape.getBounds().getY() + serviceShape.getBounds().getHeight() / 2);
        rightFlow2Y[1] = (int) (serviceShape.getBounds().getY() + serviceShape.getBounds().getHeight() / 2);
        rightFlow2Y[2] = (int) (rightGateShape.getBounds().getY() + rightGateShape.getBounds().getHeight());

        plane.getDiagramElement().add(createEdge(rightFlow_2.getId(), rightFlow2X, rightFlow2Y));

        //remove all unneeded references and objects
        List<TDataObjectReference> references = getReferences(getIDOfObject(task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()), flowElements);
        for (TDataObjectReference data : references) {
            removeFromProcess(data, flowElements);
        }
        List<TDataObjectReference> references2 = getReferences(getIDOfObject(task.getDataOutputAssociation().get(0).getTargetRef()), flowElements);
        for (TDataObjectReference data : references2) {
            removeFromProcess(data, flowElements);
        }
        if (inputTasks.size() > 1 || isAlreadyAWhiteBox(getIDOfObject(task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()),collaboration)) {
            //create WhiteBox
            replaceDataAssociationWithWhiteBox(getIDOfObject(task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()), "Sensor (IoT)" + task.getDataInputAssociation().get(0).getSourceRef().get(0).getName(), flowElements, collaboration, plane);
            //replace businessRuleTask to catch event
            changeTaskToIntermediateMessageCatchEvent(businessRuleTask, flowElements, collaboration, plane);

            //remove original reference
            removeFromProcess(task.getDataInputAssociation().get(0), flowElements);
        }
        if (outputTasks.size() > 1 || isAlreadyAWhiteBox(getIDOfObject(task.getDataOutputAssociation().get(0).getTargetRef()),collaboration)) {
            //create WhiteBox
            replaceDataAssociationWithWhiteBox(getIDOfObject(task.getDataOutputAssociation().get(0).getTargetRef()), "Actuator (IoT)" + getIDOfObject(task.getDataOutputAssociation().get(0).getTargetRef()), flowElements, collaboration, plane);
            //replace serviceTask to throw event
            changeTaskToIntermediateMessageThrowEvent(serviceTask, flowElements, collaboration, plane);

            //remove original reference
            removeFromProcess(task.getDataOutputAssociation().get(0), flowElements);
        }
        removeFromProcess(task,flowElements);
        return new Pair<>(businessRuleTask, serviceTask);
    }

    private Pair<TBusinessRuleTask, TBusinessRuleTask> replaceSeveralInputArtefactAndSensorAssociation(TTask task, TDataObjectReference reference, List<JAXBElement<? extends TFlowElement>> flowElements, TCollaboration collaboration, BPMNPlane plane) {
        List<TTask> inputTasksSensor = new ArrayList<>();
        List<TTask> inputTasksCatch = new ArrayList<>();
        List<TDataObjectReference> sensors = new ArrayList<>();
        List<TDataObjectReference> artefact = new ArrayList<>();
        List<TDataInputAssociation> sensorInputs = new ArrayList<>();
        List<TDataInputAssociation> artefactInputs = new ArrayList<>();
        List<TTask> input1Tasks = getAssociatedTasks(getIDOfObject(task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()), false, flowElements);
        for (int i = 0; i < input1Tasks.size(); i++) {
            for (int j = 0; j < input1Tasks.get(i).getDataInputAssociation().size(); j++) {
                Object keyItem = ((TDataObjectReference) input1Tasks.get(i).getDataInputAssociation().get(j).getSourceRef().get(0).getValue()).getOtherAttributes().keySet().toArray()[0];
                if (((TDataObjectReference) input1Tasks.get(i).getDataInputAssociation().get(j).getSourceRef().get(0).getValue()).getOtherAttributes().get(keyItem).equals("artefact-catch")) {
                    inputTasksCatch.add(input1Tasks.get(i));
                    artefact.add((TDataObjectReference)input1Tasks.get(i).getDataInputAssociation().get(j).getSourceRef().get(0).getValue());
                    artefactInputs.add(input1Tasks.get(i).getDataInputAssociation().get(j));
                } else {
                    inputTasksSensor.add(input1Tasks.get(i));
                    sensors.add((TDataObjectReference)input1Tasks.get(i).getDataInputAssociation().get(j).getSourceRef().get(0).getValue());
                    sensorInputs.add(input1Tasks.get(i).getDataInputAssociation().get(j));
                }
            }
        }
        List<TTask> input2Tasks = getAssociatedTasks(getIDOfObject(task.getDataInputAssociation().get(1).getSourceRef().get(0).getValue()), false, flowElements);
        for (int i = 0; i < input2Tasks.size(); i++) {
            for (int j = 0; j < input2Tasks.get(i).getDataInputAssociation().size(); j++) {
                Object keyItem = ((TDataObjectReference) input2Tasks.get(i).getDataInputAssociation().get(j).getSourceRef().get(0).getValue()).getOtherAttributes().keySet().toArray()[0];
                if (((TDataObjectReference) input2Tasks.get(i).getDataInputAssociation().get(j).getSourceRef().get(0).getValue()).getOtherAttributes().get(keyItem).equals("artefact-catch")) {
                    if (!artefactInputs.contains(input2Tasks.get(i).getDataInputAssociation().get(j)))
                        artefactInputs.add(input2Tasks.get(i).getDataInputAssociation().get(j));
                    if (!inputTasksCatch.contains(input2Tasks.get(i)))
                        inputTasksCatch.add(input2Tasks.get(i));
                    if (!artefact.contains((TDataObjectReference)input2Tasks.get(i).getDataInputAssociation().get(j).getSourceRef().get(0).getValue()))
                        artefact.add((TDataObjectReference)input2Tasks.get(i).getDataInputAssociation().get(j).getSourceRef().get(0).getValue());
                } else {
                    if (!sensorInputs.contains(input2Tasks.get(i).getDataInputAssociation().get(j)))
                        sensorInputs.add(input2Tasks.get(i).getDataInputAssociation().get(j));
                    if (!inputTasksSensor.contains(input2Tasks.get(i)))
                        inputTasksSensor.add(input2Tasks.get(i));
                    if (!sensors.contains((TDataObjectReference)input2Tasks.get(i).getDataInputAssociation().get(j).getSourceRef().get(0).getValue()))
                        sensors.add((TDataObjectReference)input2Tasks.get(i).getDataInputAssociation().get(j).getSourceRef().get(0).getValue());
                }
            }
        }

        String leftGW_Id = "Gateway_" + randomNumberSequence();
        String rightGW_Id = "Gateway_" + randomNumberSequence();
        String leftXor_Id = "Gateway_" + randomNumberSequence();
        String rightXor_Id = "Gateway_" + randomNumberSequence();
        String rule_Id = "Activity_" + randomNumberSequence();
        String catch_Id = "Activity_" + randomNumberSequence();
        String leftFlow_2_Id = "Flow_" + randomNumberSequence();
        String leftFlow_3_Id = "Flow_" + randomNumberSequence();
        String rightFlow_2_Id = "Flow_" + randomNumberSequence();
        String rightFlow_3_Id = "Flow_" + randomNumberSequence();

        String xorLeftFlow = "Flow_" + randomNumberSequence();
        String catchRightFlow = "Flow_" + randomNumberSequence();
        String xorElseFlow = "Flow_" + randomNumberSequence();

        //create Object
        TBusinessRuleTask businessRuleTask = new TBusinessRuleTask();
        TBusinessRuleTask businessRuleTaskCatch = new TBusinessRuleTask();
        TParallelGateway leftGateWay = new TParallelGateway();
        TParallelGateway rightGateWay = new TParallelGateway();
        TExclusiveGateway xorGateLeft = new TExclusiveGateway();
        TExclusiveGateway xorGateRight = new TExclusiveGateway();
        TSequenceFlow leftFlow_2 = new TSequenceFlow();
        TSequenceFlow leftFlow_3 = new TSequenceFlow();
        TSequenceFlow rightFlow_2 = new TSequenceFlow();
        TSequenceFlow rightFlow_3 = new TSequenceFlow();
        TSequenceFlow xorLeftSequenceFlow = new TSequenceFlow();
        TSequenceFlow catchRightSequenceFlow = new TSequenceFlow();
        TSequenceFlow xorElseSequenceFlow = new TSequenceFlow();

        //change incoming and outgoing flow attributes
        for (int k = 0; k < task.getIncoming().size(); k++) {
            TSequenceFlow incoming = getFlow(task.getIncoming().get(k).getLocalPart(), flowElements);
            assert incoming != null;
            incoming.setTargetRef(leftGateWay);
        }
        for (int k = 0; k < task.getOutgoing().size(); k++) {
            TSequenceFlow outgoing = getFlow(task.getOutgoing().get(k).getLocalPart(), flowElements);
            assert outgoing != null;
            outgoing.setSourceRef(rightGateWay);
        }

        //create business task
        businessRuleTask.setId(rule_Id);
        businessRuleTask.setName("(IoT) " + task.getName());
        businessRuleTask.getIncoming().add(QName.valueOf(leftFlow_2_Id));
        businessRuleTask.getOutgoing().add(QName.valueOf(rightFlow_2_Id));
        businessRuleTask.getDataInputAssociation().addAll(sensorInputs);
        businessRuleTask.getProperty().addAll(task.getProperty());

        //create business task
        businessRuleTaskCatch.setId(catch_Id);
        businessRuleTaskCatch.setName("(IoT) " + task.getName());
        businessRuleTaskCatch.getIncoming().add(QName.valueOf(xorLeftFlow));
        businessRuleTaskCatch.getOutgoing().add(QName.valueOf(catchRightFlow));
        businessRuleTaskCatch.getDataInputAssociation().addAll(artefactInputs);
        businessRuleTaskCatch.getProperty().addAll(task.getProperty());

        leftFlow_2.setId(leftFlow_2_Id);
        leftFlow_2.setSourceRef(leftGateWay);
        leftFlow_2.setTargetRef(businessRuleTask);

        leftFlow_3.setId(leftFlow_3_Id);
        leftFlow_3.setSourceRef(leftGateWay);
        leftFlow_3.setTargetRef(xorGateLeft);


        rightFlow_2.setId(rightFlow_2_Id);
        rightFlow_2.setSourceRef(businessRuleTask);
        rightFlow_2.setTargetRef(rightGateWay);

        rightFlow_3.setId(rightFlow_3_Id);
        rightFlow_3.setSourceRef(xorGateRight);
        rightFlow_3.setTargetRef(rightGateWay);
        List<TDataObjectReference> references = getReferences(getIDOfObject(task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()),flowElements);
        String name = "";
        for (TDataObjectReference referenceItem: references){
            if (referenceItem.getName() != null) {
                if (referenceItem.getName().contains("<") || referenceItem.getName().contains(">") || referenceItem.getName().contains("=")) {
                    name = referenceItem.getName();
                    break;
                }
            } else {
                name = "if";
            }
        }
        rightFlow_3.setName(name);

        xorLeftSequenceFlow.setId(xorLeftFlow);
        xorLeftSequenceFlow.setSourceRef(xorGateLeft);
        xorLeftSequenceFlow.setTargetRef(businessRuleTaskCatch);

        catchRightSequenceFlow.setId(catchRightFlow);
        catchRightSequenceFlow.setSourceRef(businessRuleTaskCatch);
        catchRightSequenceFlow.setTargetRef(xorGateRight);

        xorElseSequenceFlow.setId(xorElseFlow);
        xorElseSequenceFlow.setSourceRef(xorGateRight);
        xorElseSequenceFlow.setTargetRef(xorGateLeft);
        xorElseSequenceFlow.setName("else");

        //create parallel Gateways
        leftGateWay.setId(leftGW_Id);
        leftGateWay.getIncoming().addAll(task.getIncoming());
        leftGateWay.getOutgoing().add(QName.valueOf(leftFlow_2.getId()));
        leftGateWay.getOutgoing().add(QName.valueOf(leftFlow_3.getId()));

        rightGateWay.setId(rightGW_Id);
        rightGateWay.getIncoming().add(QName.valueOf(rightFlow_2.getId()));
        rightGateWay.getIncoming().add(QName.valueOf(rightFlow_3.getId()));
        rightGateWay.getOutgoing().addAll(task.getOutgoing());

        xorGateLeft.setId(leftXor_Id);
        xorGateLeft.getIncoming().add(QName.valueOf(leftFlow_3.getId()));
        xorGateLeft.getIncoming().add(QName.valueOf(xorElseSequenceFlow.getId()));
        xorGateLeft.getOutgoing().add(QName.valueOf(xorLeftSequenceFlow.getId()));

        xorGateRight.setId(rightXor_Id);
        xorGateRight.getIncoming().add(QName.valueOf(catchRightSequenceFlow.getId()));
        xorGateRight.getOutgoing().add(QName.valueOf(xorElseSequenceFlow.getId()));
        xorGateRight.getOutgoing().add(QName.valueOf(rightFlow_3.getId()));

        //add elements to the process
        flowElements.add(objectFactory.createBusinessRuleTask(businessRuleTask));
        flowElements.add(objectFactory.createBusinessRuleTask(businessRuleTaskCatch));
        flowElements.add(objectFactory.createParallelGateway(leftGateWay));
        flowElements.add(objectFactory.createParallelGateway(rightGateWay));
        flowElements.add(objectFactory.createExclusiveGateway(xorGateLeft));
        flowElements.add(objectFactory.createExclusiveGateway(xorGateRight));
        flowElements.add(objectFactory.createSequenceFlow(leftFlow_2));
        flowElements.add(objectFactory.createSequenceFlow(leftFlow_3));
        flowElements.add(objectFactory.createSequenceFlow(rightFlow_2));
        flowElements.add(objectFactory.createSequenceFlow(rightFlow_3));
        flowElements.add(objectFactory.createSequenceFlow(xorElseSequenceFlow));
        flowElements.add(objectFactory.createSequenceFlow(catchRightSequenceFlow));
        flowElements.add(objectFactory.createSequenceFlow(xorLeftSequenceFlow));

        BPMNShape taskShape = (BPMNShape) getShapeOrEdge(task.getId(), plane);
        removeFromProcess(task,flowElements);

        org.omg.spec.bpmn._20100524.di.ObjectFactory planeObjectFactory = new org.omg.spec.bpmn._20100524.di.ObjectFactory();

        moveEverythingUp(taskShape.getBounds().getY(),100,plane);
        moveEverythingDown(taskShape.getBounds().getY()+taskShape.getBounds().getHeight(),300,plane);
        moveEverythingToRight(taskShape.getBounds().getX(),400,plane);
        taskShape.getBounds().setX(taskShape.getBounds().getX()+150);

        //create the shapes
        BPMNShape ruleShape = new BPMNShape();
        Bounds ruleBounds = new Bounds();
        ruleShape.setBounds(ruleBounds);
        ruleShape.setId(businessRuleTask.getId() + "_di");
        ruleShape.setBpmnElement(QName.valueOf(businessRuleTask.getId()));
        ruleShape.getBounds().setX(taskShape.getBounds().getX());
        ruleShape.getBounds().setY(taskShape.getBounds().getY() - 100);
        ruleShape.getBounds().setHeight(80);
        ruleShape.getBounds().setWidth(100);
        plane.getDiagramElement().add(planeObjectFactory.createBPMNShape(ruleShape));

        BPMNShape catchShape = new BPMNShape();
        Bounds catchBounds = new Bounds();
        catchShape.setBounds(catchBounds);
        catchShape.setId(businessRuleTaskCatch.getId() + "_di");
        catchShape.setBpmnElement(QName.valueOf(businessRuleTaskCatch.getId()));
        catchShape.getBounds().setX(taskShape.getBounds().getX());
        catchShape.getBounds().setY(taskShape.getBounds().getY());
        catchShape.getBounds().setHeight(80);
        catchShape.getBounds().setWidth(100);
        plane.getDiagramElement().add(planeObjectFactory.createBPMNShape(catchShape));

        BPMNShape leftGateShape = new BPMNShape();
        Bounds leftGateBounds = new Bounds();
        leftGateShape.setBounds(leftGateBounds);
        leftGateShape.setId(leftGateWay.getId() + "_di");
        leftGateShape.setBpmnElement(QName.valueOf(leftGateWay.getId()));
        leftGateShape.getBounds().setX(taskShape.getBounds().getX() - 150);
        leftGateShape.getBounds().setY(taskShape.getBounds().getY() + taskShape.getBounds().getHeight() / 2 - 18);
        leftGateShape.getBounds().setHeight(36);
        leftGateShape.getBounds().setWidth(36);
        plane.getDiagramElement().add(planeObjectFactory.createBPMNShape(leftGateShape));

        BPMNShape rightGateShape = new BPMNShape();
        Bounds rightGateBounds = new Bounds();
        rightGateShape.setBounds(rightGateBounds);
        rightGateShape.setId(rightGateWay.getId() + "_di");
        rightGateShape.setBpmnElement(QName.valueOf(rightGateWay.getId()));
        rightGateShape.getBounds().setX(taskShape.getBounds().getX() + 150 + taskShape.getBounds().getWidth());
        rightGateShape.getBounds().setY(taskShape.getBounds().getY() + taskShape.getBounds().getHeight() / 2 - 18);
        rightGateShape.getBounds().setHeight(36);
        rightGateShape.getBounds().setWidth(36);
        plane.getDiagramElement().add(planeObjectFactory.createBPMNShape(rightGateShape));

        BPMNShape rightXorGateShape = new BPMNShape();
        Bounds rightXorGateBounds = new Bounds();
        rightXorGateShape.setBounds(rightXorGateBounds);
        rightXorGateShape.setId(xorGateRight.getId() + "_di");
        rightXorGateShape.setBpmnElement(QName.valueOf(xorGateRight.getId()));
        rightXorGateShape.getBounds().setX(catchShape.getBounds().getX() + catchShape.getBounds().getWidth() + 50);
        rightXorGateShape.getBounds().setY(catchShape.getBounds().getY() + taskShape.getBounds().getHeight() / 2 - 18);
        rightXorGateShape.getBounds().setHeight(36);
        rightXorGateShape.getBounds().setWidth(36);
        plane.getDiagramElement().add(planeObjectFactory.createBPMNShape(rightXorGateShape));

        BPMNShape leftXorGateShape = new BPMNShape();
        Bounds leftXorGateBounds = new Bounds();
        leftXorGateShape.setBounds(leftXorGateBounds);
        leftXorGateShape.setId(xorGateLeft.getId() + "_di");
        leftXorGateShape.setBpmnElement(QName.valueOf(xorGateLeft.getId()));
        leftXorGateShape.getBounds().setX(catchShape.getBounds().getX() - 50 - 36);
        leftXorGateShape.getBounds().setY(catchShape.getBounds().getY() + taskShape.getBounds().getHeight() / 2 - 18);
        leftXorGateShape.getBounds().setHeight(36);
        leftXorGateShape.getBounds().setWidth(36);
        plane.getDiagramElement().add(planeObjectFactory.createBPMNShape(leftXorGateShape));


        //adjust the already existing edges
        for (int k = 0; k < task.getIncoming().size(); k++) {
            TSequenceFlow incoming = getFlow(task.getIncoming().get(k).getLocalPart(), flowElements);
            assert incoming != null;
            BPMNEdge incomingEdge = (BPMNEdge) getShapeOrEdge(incoming.getId(), plane);
            assert incomingEdge != null;
            incomingEdge.getWaypoint().get(incomingEdge.getWaypoint().size() - 1).setX(leftGateShape.getBounds().getX());
        }

        for (int k = 0; k < task.getOutgoing().size(); k++) {
            TSequenceFlow outgoing = getFlow(task.getOutgoing().get(k).getLocalPart(), flowElements);
            assert outgoing != null;
            BPMNEdge outgoingEdge = (BPMNEdge) getShapeOrEdge(outgoing.getId(), plane);
            if (outgoingEdge != null) {
                outgoingEdge.getWaypoint().get(0).setX(rightGateShape.getBounds().getX() + rightGateShape.getBounds().getWidth());
            }
        }


        //create missing edges


        int[] rightFlow1X = new int[3];
        int[] rightFlow1Y = new int[3];
        rightFlow1X[0] = (int) (ruleShape.getBounds().getX() + ruleShape.getBounds().getWidth());
        rightFlow1X[1] = (int) (rightGateShape.getBounds().getX() + rightGateShape.getBounds().getWidth() / 2);
        rightFlow1X[2] = (int) (rightGateShape.getBounds().getX() + rightGateShape.getBounds().getWidth() / 2);

        rightFlow1Y[0] = (int) (ruleShape.getBounds().getY() + ruleShape.getBounds().getHeight() / 2);
        rightFlow1Y[1] = (int) (ruleShape.getBounds().getY() + ruleShape.getBounds().getHeight() / 2);
        rightFlow1Y[2] = (int) (rightGateShape.getBounds().getY());

        plane.getDiagramElement().add(createEdge(rightFlow_2.getId(), rightFlow1X, rightFlow1Y));

        BPMNEdge andFlow2Left = new BPMNEdge();
        andFlow2Left.setId(leftFlow_2.getId()+"_di");
        andFlow2Left.setBpmnElement(QName.valueOf(leftFlow_2.getId()));
        andFlow2Left.getWaypoint().add(new Point(leftGateBounds.getX()+leftGateBounds.getWidth()/2,leftGateBounds.getY()+leftGateBounds.getHeight()));
        andFlow2Left.getWaypoint().add(new Point(leftGateBounds.getX()+leftGateBounds.getWidth()/2,ruleBounds.getY()+ruleBounds.getHeight()/2));
        andFlow2Left.getWaypoint().add(new Point(ruleBounds.getX(),ruleBounds.getY()+ruleBounds.getHeight()/2));


        BPMNEdge andFlow3Left = new BPMNEdge();
        andFlow3Left.setId(leftFlow_3.getId()+"_di");
        andFlow3Left.setBpmnElement(QName.valueOf(leftFlow_3.getId()));
        andFlow3Left.getWaypoint().add(new Point(leftGateBounds.getX()+leftGateBounds.getWidth(),leftGateBounds.getY()+leftGateBounds.getHeight()/2));
        andFlow3Left.getWaypoint().add(new Point(leftXorGateBounds.getX(),leftXorGateBounds.getY()+leftXorGateBounds.getHeight()/2));

        BPMNEdge andFlow3Right = new BPMNEdge();
        andFlow3Right.setId(rightFlow_3.getId()+"_di");
        andFlow3Right.setBpmnElement(QName.valueOf(rightFlow_3.getId()));
        andFlow3Right.getWaypoint().add(new Point(rightXorGateBounds.getX()+rightXorGateBounds.getWidth(),rightXorGateBounds.getY()+rightXorGateBounds.getHeight()/2));
        andFlow3Right.getWaypoint().add(new Point(rightGateBounds.getX(),rightGateBounds.getY()+rightGateBounds.getHeight()/2));

        BPMNEdge xorLeftEdge = new BPMNEdge();
        xorLeftEdge.setId(xorLeftSequenceFlow.getId()+"_di");
        xorLeftEdge.setBpmnElement(QName.valueOf(xorLeftSequenceFlow.getId()));
        xorLeftEdge.getWaypoint().add(new Point(leftXorGateBounds.getX()+leftGateBounds.getWidth(),leftXorGateBounds.getY()+leftXorGateBounds.getHeight()/2));
        xorLeftEdge.getWaypoint().add(new Point(catchBounds.getX(),catchBounds.getY()+catchBounds.getHeight()/2));

        BPMNEdge catchOutEdge = new BPMNEdge();
        catchOutEdge.setId(catchRightSequenceFlow.getId()+"_di");
        catchOutEdge.setBpmnElement(QName.valueOf(catchRightSequenceFlow.getId()));
        catchOutEdge.getWaypoint().add(new Point(catchBounds.getX()+catchBounds.getWidth(),catchBounds.getY()+catchBounds.getHeight()/2));
        catchOutEdge.getWaypoint().add(new Point(rightXorGateBounds.getX(),rightXorGateBounds.getY()+rightXorGateBounds.getHeight()/2));

        BPMNEdge xorElseEdge = new BPMNEdge();
        xorElseEdge.setId(xorElseSequenceFlow.getId()+"_di");
        xorElseEdge.setBpmnElement(QName.valueOf(xorElseSequenceFlow.getId()));
        xorElseEdge.getWaypoint().add(new Point(rightXorGateBounds.getX()+rightXorGateBounds.getWidth()/2,rightXorGateBounds.getY()+rightXorGateBounds.getHeight()));
        xorElseEdge.getWaypoint().add(new Point(rightXorGateBounds.getX()+rightXorGateBounds.getWidth()/2,rightXorGateBounds.getY()+rightXorGateBounds.getHeight()+50));
        xorElseEdge.getWaypoint().add(new Point(leftXorGateBounds.getX()+leftXorGateBounds.getWidth()/2,rightXorGateBounds.getY()+rightXorGateBounds.getHeight()+50));
        xorElseEdge.getWaypoint().add(new Point(leftXorGateBounds.getX()+leftXorGateBounds.getWidth()/2,leftXorGateBounds.getY()+leftXorGateBounds.getHeight()));


        org.omg.spec.bpmn._20100524.di.ObjectFactory objectFactoryEdge = new org.omg.spec.bpmn._20100524.di.ObjectFactory();

        plane.getDiagramElement().add(objectFactoryEdge.createBPMNEdge(andFlow3Left));
        plane.getDiagramElement().add(objectFactoryEdge.createBPMNEdge(andFlow3Right));
        plane.getDiagramElement().add(objectFactoryEdge.createBPMNEdge(xorLeftEdge));
        plane.getDiagramElement().add(objectFactoryEdge.createBPMNEdge(catchOutEdge));
        plane.getDiagramElement().add(objectFactoryEdge.createBPMNEdge(xorElseEdge));
        plane.getDiagramElement().add(objectFactoryEdge.createBPMNEdge(andFlow2Left));

        //remove all unneeded references and objects
        if (task.getDataInputAssociation().size() > 0) {
            references = getReferences(getIDOfObject(task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()), flowElements);
            for (TDataObjectReference data : references) {
                removeFromProcess(data, flowElements);
            }
        }
        if (task.getDataOutputAssociation().size() > 0) {
            List<TDataObjectReference> references2 = getReferences(getIDOfObject(task.getDataOutputAssociation().get(0).getTargetRef()), flowElements);
            for (TDataObjectReference data : references2) {
                removeFromProcess(data, flowElements);
            }
        }
        if (inputTasksCatch.size() > 1 || isAlreadyAWhiteBox(artefact.get(0).getId(),collaboration)) {
            //create WhiteBox
            replaceDataAssociationWithWhiteBox(artefact.get(0).getId(), "Sensor (IoT)" + artefact.get(0).getName(), flowElements, collaboration, plane);
            //replace businessRuleTask to catch event
            changeTaskToIntermediateMessageCatchEvent(businessRuleTaskCatch, flowElements, collaboration, plane);

            //remove original reference
            removeFromProcess(artefact.get(0), flowElements);

        }
        if (inputTasksSensor.size() > 1 || isAlreadyAWhiteBox(sensors.get(0).getId(),collaboration)){
            //create WhiteBox
            replaceDataAssociationWithWhiteBox(sensors.get(0).getId(), "Sensor (IoT)" + sensors.get(0).getName(), flowElements, collaboration, plane);
            //replace businessRuleTask to catch event
            changeTaskToIntermediateMessageCatchEvent(businessRuleTask, flowElements, collaboration, plane);

            //remove original reference
            removeFromProcess(sensors.get(0), flowElements);
        }
        return new Pair<>(businessRuleTask,businessRuleTaskCatch);
    }

    private Pair<TBusinessRuleTask, TServiceTask> replaceSeveralInputArtefactAndOneActuatorAssociation(TTask task, TDataObjectReference reference, List<JAXBElement<? extends TFlowElement>> flowElements, TCollaboration collaboration, BPMNPlane plane) {
        List<TTask> outputTasks = getAssociatedTasks(getIDOfObject(task.getDataOutputAssociation().get(0).getTargetRef()), true, flowElements);
        List<TTask> inputTasksCatch = getAssociatedTasks(getIDOfObject(task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()), false, flowElements);

        String leftGW_Id = "Gateway_" + randomNumberSequence();
        String rightGW_Id = "Gateway_" + randomNumberSequence();
        String leftXor_Id = "Gateway_" + randomNumberSequence();
        String rightXor_Id = "Gateway_" + randomNumberSequence();
        String catch_Id = "Activity_" + randomNumberSequence();
        String service_Id = "Activity_" + randomNumberSequence();
        String leftFlow_1_Id = "Flow_" + randomNumberSequence();
        String leftFlow_3_Id = "Flow_" + randomNumberSequence();
        String rightFlow_1_Id = "Flow_" + randomNumberSequence();
        String rightFlow_3_Id = "Flow_" + randomNumberSequence();

        String xorLeftFlow = "Flow_" + randomNumberSequence();
        String catchRightFlow = "Flow_" + randomNumberSequence();
        String xorElseFlow = "Flow_" + randomNumberSequence();

        //create Object
        TBusinessRuleTask businessRuleTaskCatch = new TBusinessRuleTask();
        TServiceTask serviceTask = new TServiceTask();
        TParallelGateway leftGateWay = new TParallelGateway();
        TParallelGateway rightGateWay = new TParallelGateway();
        TExclusiveGateway xorGateLeft = new TExclusiveGateway();
        TExclusiveGateway xorGateRight = new TExclusiveGateway();
        TSequenceFlow leftFlow_1 = new TSequenceFlow();
        TSequenceFlow leftFlow_3 = new TSequenceFlow();
        TSequenceFlow rightFlow_1 = new TSequenceFlow();
        TSequenceFlow rightFlow_3 = new TSequenceFlow();
        TSequenceFlow xorLeftSequenceFlow = new TSequenceFlow();
        TSequenceFlow catchRightSequenceFlow = new TSequenceFlow();
        TSequenceFlow xorElseSequenceFlow = new TSequenceFlow();

        //change incoming and outgoing flow attributes
        for (int k = 0; k < task.getIncoming().size(); k++) {
            TSequenceFlow incoming = getFlow(task.getIncoming().get(k).getLocalPart(), flowElements);
            assert incoming != null;
            incoming.setTargetRef(leftGateWay);
        }
        for (int k = 0; k < task.getOutgoing().size(); k++) {
            TSequenceFlow outgoing = getFlow(task.getOutgoing().get(k).getLocalPart(), flowElements);
            assert outgoing != null;
            outgoing.setSourceRef(rightGateWay);
        }


        //create business task
        businessRuleTaskCatch.setId(catch_Id);
        businessRuleTaskCatch.setName("(IoT) " + task.getName());
        businessRuleTaskCatch.getIncoming().add(QName.valueOf(xorLeftFlow));
        businessRuleTaskCatch.getOutgoing().add(QName.valueOf(catchRightFlow));
        businessRuleTaskCatch.getDataInputAssociation().addAll(task.getDataInputAssociation());
        businessRuleTaskCatch.getProperty().addAll(task.getProperty());


        //create service task
        serviceTask.setId(service_Id);
        serviceTask.setName("(IoT) " + task.getName());
        serviceTask.getIncoming().add(QName.valueOf(leftFlow_1_Id));
        serviceTask.getOutgoing().add(QName.valueOf(rightFlow_1_Id));
        serviceTask.getDataOutputAssociation().addAll(task.getDataOutputAssociation());
        serviceTask.getProperty().addAll(task.getProperty());

        //create SequenceFlows
        leftFlow_1.setId(leftFlow_1_Id);
        leftFlow_1.setSourceRef(leftGateWay);
        leftFlow_1.setTargetRef(serviceTask);


        leftFlow_3.setId(leftFlow_3_Id);
        leftFlow_3.setSourceRef(leftGateWay);
        leftFlow_3.setTargetRef(xorGateLeft);

        rightFlow_1.setId(rightFlow_1_Id);
        rightFlow_1.setSourceRef(serviceTask);
        rightFlow_1.setTargetRef(rightGateWay);


        rightFlow_3.setId(rightFlow_3_Id);
        rightFlow_3.setSourceRef(xorGateRight);
        rightFlow_3.setTargetRef(rightGateWay);
        List<TDataObjectReference> references = getReferences(getIDOfObject(task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()),flowElements);
        String name = "";
        for (TDataObjectReference referenceItem: references){
            if (referenceItem.getName() != null) {
                if (referenceItem.getName().contains("<") || referenceItem.getName().contains(">") || referenceItem.getName().contains("=")) {
                    name = referenceItem.getName();
                    break;
                }
            } else {
                name = "if";
            }
        }
        rightFlow_3.setName(name);

        xorLeftSequenceFlow.setId(xorLeftFlow);
        xorLeftSequenceFlow.setSourceRef(xorGateLeft);
        xorLeftSequenceFlow.setTargetRef(businessRuleTaskCatch);

        catchRightSequenceFlow.setId(catchRightFlow);
        catchRightSequenceFlow.setSourceRef(businessRuleTaskCatch);
        catchRightSequenceFlow.setTargetRef(xorGateRight);

        xorElseSequenceFlow.setId(xorElseFlow);
        xorElseSequenceFlow.setSourceRef(xorGateRight);
        xorElseSequenceFlow.setTargetRef(xorGateLeft);
        xorElseSequenceFlow.setName("else");

        //create parallel Gateways
        leftGateWay.setId(leftGW_Id);
        leftGateWay.getIncoming().addAll(task.getIncoming());
        leftGateWay.getOutgoing().add(QName.valueOf(leftFlow_1.getId()));
        leftGateWay.getOutgoing().add(QName.valueOf(leftFlow_3.getId()));

        rightGateWay.setId(rightGW_Id);
        rightGateWay.getIncoming().add(QName.valueOf(rightFlow_1.getId()));
        rightGateWay.getIncoming().add(QName.valueOf(rightFlow_3.getId()));
        rightGateWay.getOutgoing().addAll(task.getOutgoing());

        xorGateLeft.setId(leftXor_Id);
        xorGateLeft.getIncoming().add(QName.valueOf(leftFlow_3.getId()));
        xorGateLeft.getIncoming().add(QName.valueOf(xorElseSequenceFlow.getId()));
        xorGateLeft.getOutgoing().add(QName.valueOf(xorLeftSequenceFlow.getId()));

        xorGateRight.setId(rightXor_Id);
        xorGateRight.getIncoming().add(QName.valueOf(catchRightSequenceFlow.getId()));
        xorGateRight.getOutgoing().add(QName.valueOf(xorElseSequenceFlow.getId()));
        xorGateRight.getOutgoing().add(QName.valueOf(rightFlow_3.getId()));

        //add elements to the process
        flowElements.add(objectFactory.createBusinessRuleTask(businessRuleTaskCatch));
        flowElements.add(objectFactory.createServiceTask(serviceTask));
        flowElements.add(objectFactory.createParallelGateway(leftGateWay));
        flowElements.add(objectFactory.createParallelGateway(rightGateWay));
        flowElements.add(objectFactory.createExclusiveGateway(xorGateLeft));
        flowElements.add(objectFactory.createExclusiveGateway(xorGateRight));
        flowElements.add(objectFactory.createSequenceFlow(leftFlow_1));
        flowElements.add(objectFactory.createSequenceFlow(leftFlow_3));
        flowElements.add(objectFactory.createSequenceFlow(rightFlow_1));
        flowElements.add(objectFactory.createSequenceFlow(rightFlow_3));
        flowElements.add(objectFactory.createSequenceFlow(xorElseSequenceFlow));
        flowElements.add(objectFactory.createSequenceFlow(catchRightSequenceFlow));
        flowElements.add(objectFactory.createSequenceFlow(xorLeftSequenceFlow));

        BPMNShape taskShape = (BPMNShape) getShapeOrEdge(task.getId(), plane);
        removeFromProcess(task,flowElements);

        org.omg.spec.bpmn._20100524.di.ObjectFactory planeObjectFactory = new org.omg.spec.bpmn._20100524.di.ObjectFactory();

        moveEverythingUp(taskShape.getBounds().getY(),100,plane);
        moveEverythingDown(taskShape.getBounds().getY()+taskShape.getBounds().getHeight(),300,plane);
        moveEverythingToRight(taskShape.getBounds().getX(),400,plane);
        taskShape.getBounds().setX(taskShape.getBounds().getX()+150);

        //create the shapes
        BPMNShape catchShape = new BPMNShape();
        Bounds catchBounds = new Bounds();
        catchShape.setBounds(catchBounds);
        catchShape.setId(businessRuleTaskCatch.getId() + "_di");
        catchShape.setBpmnElement(QName.valueOf(businessRuleTaskCatch.getId()));
        catchShape.getBounds().setX(taskShape.getBounds().getX());
        catchShape.getBounds().setY(taskShape.getBounds().getY());
        catchShape.getBounds().setHeight(80);
        catchShape.getBounds().setWidth(100);
        plane.getDiagramElement().add(planeObjectFactory.createBPMNShape(catchShape));

        BPMNShape serviceShape = new BPMNShape();
        Bounds serviceBounds = new Bounds();
        serviceShape.setBounds(serviceBounds);
        serviceShape.setId(serviceTask.getId() + "_di");
        serviceShape.setBpmnElement(QName.valueOf(serviceTask.getId()));
        serviceShape.getBounds().setX(taskShape.getBounds().getX());
        serviceShape.getBounds().setY(taskShape.getBounds().getY() + 150);
        serviceShape.getBounds().setHeight(80);
        serviceShape.getBounds().setWidth(100);
        plane.getDiagramElement().add(planeObjectFactory.createBPMNShape(serviceShape));

        BPMNShape leftGateShape = new BPMNShape();
        Bounds leftGateBounds = new Bounds();
        leftGateShape.setBounds(leftGateBounds);
        leftGateShape.setId(leftGateWay.getId() + "_di");
        leftGateShape.setBpmnElement(QName.valueOf(leftGateWay.getId()));
        leftGateShape.getBounds().setX(taskShape.getBounds().getX() - 150);
        leftGateShape.getBounds().setY(taskShape.getBounds().getY() + taskShape.getBounds().getHeight() / 2 - 18);
        leftGateShape.getBounds().setHeight(36);
        leftGateShape.getBounds().setWidth(36);
        plane.getDiagramElement().add(planeObjectFactory.createBPMNShape(leftGateShape));

        BPMNShape rightGateShape = new BPMNShape();
        Bounds rightGateBounds = new Bounds();
        rightGateShape.setBounds(rightGateBounds);
        rightGateShape.setId(rightGateWay.getId() + "_di");
        rightGateShape.setBpmnElement(QName.valueOf(rightGateWay.getId()));
        rightGateShape.getBounds().setX(taskShape.getBounds().getX() + 150 + taskShape.getBounds().getWidth());
        rightGateShape.getBounds().setY(taskShape.getBounds().getY() + taskShape.getBounds().getHeight() / 2 - 18);
        rightGateShape.getBounds().setHeight(36);
        rightGateShape.getBounds().setWidth(36);
        plane.getDiagramElement().add(planeObjectFactory.createBPMNShape(rightGateShape));

        BPMNShape rightXorGateShape = new BPMNShape();
        Bounds rightXorGateBounds = new Bounds();
        rightXorGateShape.setBounds(rightXorGateBounds);
        rightXorGateShape.setId(xorGateRight.getId() + "_di");
        rightXorGateShape.setBpmnElement(QName.valueOf(xorGateRight.getId()));
        rightXorGateShape.getBounds().setX(catchShape.getBounds().getX() + catchShape.getBounds().getWidth() + 50);
        rightXorGateShape.getBounds().setY(catchShape.getBounds().getY() + taskShape.getBounds().getHeight() / 2 - 18);
        rightXorGateShape.getBounds().setHeight(36);
        rightXorGateShape.getBounds().setWidth(36);
        plane.getDiagramElement().add(planeObjectFactory.createBPMNShape(rightXorGateShape));

        BPMNShape leftXorGateShape = new BPMNShape();
        Bounds leftXorGateBounds = new Bounds();
        leftXorGateShape.setBounds(leftXorGateBounds);
        leftXorGateShape.setId(xorGateLeft.getId() + "_di");
        leftXorGateShape.setBpmnElement(QName.valueOf(xorGateLeft.getId()));
        leftXorGateShape.getBounds().setX(catchShape.getBounds().getX() - 50 - 36);
        leftXorGateShape.getBounds().setY(catchShape.getBounds().getY() + taskShape.getBounds().getHeight() / 2 - 18);
        leftXorGateShape.getBounds().setHeight(36);
        leftXorGateShape.getBounds().setWidth(36);
        plane.getDiagramElement().add(planeObjectFactory.createBPMNShape(leftXorGateShape));


        //adjust the already existing edges
        for (int k = 0; k < task.getIncoming().size(); k++) {
            TSequenceFlow incoming = getFlow(task.getIncoming().get(k).getLocalPart(), flowElements);
            assert incoming != null;
            BPMNEdge incomingEdge = (BPMNEdge) getShapeOrEdge(incoming.getId(), plane);
            assert incomingEdge != null;
            incomingEdge.getWaypoint().get(incomingEdge.getWaypoint().size() - 1).setX(leftGateShape.getBounds().getX());
        }

        for (int k = 0; k < task.getOutgoing().size(); k++) {
            TSequenceFlow outgoing = getFlow(task.getOutgoing().get(k).getLocalPart(), flowElements);
            assert outgoing != null;
            BPMNEdge outgoingEdge = (BPMNEdge) getShapeOrEdge(outgoing.getId(), plane);
            if (outgoingEdge != null) {
                outgoingEdge.getWaypoint().get(0).setX(rightGateShape.getBounds().getX() + rightGateShape.getBounds().getWidth());
            }
        }


        //create missing edges

        int[] rightFlow2X = new int[3];
        int[] rightFlow2Y = new int[3];
        rightFlow2X[0] = (int) (serviceShape.getBounds().getX() + serviceShape.getBounds().getWidth());
        rightFlow2X[1] = (int) (rightGateShape.getBounds().getX() + rightGateShape.getBounds().getWidth() / 2);
        rightFlow2X[2] = (int) (rightGateShape.getBounds().getX() + rightGateShape.getBounds().getWidth() / 2);

        rightFlow2Y[0] = (int) (serviceShape.getBounds().getY() + serviceShape.getBounds().getHeight() / 2);
        rightFlow2Y[1] = (int) (serviceShape.getBounds().getY() + serviceShape.getBounds().getHeight() / 2);
        rightFlow2Y[2] = (int) (rightGateShape.getBounds().getY() + rightGateShape.getBounds().getHeight());

        plane.getDiagramElement().add(createEdge(rightFlow_1.getId(), rightFlow2X, rightFlow2Y));

        BPMNEdge andFlow1Left = new BPMNEdge();
        andFlow1Left.setId(leftFlow_1.getId()+"_di");
        andFlow1Left.setBpmnElement(QName.valueOf(leftFlow_1.getId()));
        andFlow1Left.getWaypoint().add(new Point(leftGateBounds.getX()+leftGateBounds.getWidth()/2,leftGateBounds.getY()+leftGateBounds.getHeight()));
        andFlow1Left.getWaypoint().add(new Point(leftGateBounds.getX()+leftGateBounds.getWidth()/2,serviceBounds.getY()+serviceBounds.getHeight()/2));
        andFlow1Left.getWaypoint().add(new Point(serviceBounds.getX(),serviceBounds.getY()+serviceBounds.getHeight()/2));


        BPMNEdge andFlow3Left = new BPMNEdge();
        andFlow3Left.setId(leftFlow_3.getId()+"_di");
        andFlow3Left.setBpmnElement(QName.valueOf(leftFlow_3.getId()));
        andFlow3Left.getWaypoint().add(new Point(leftGateBounds.getX()+leftGateBounds.getWidth(),leftGateBounds.getY()+leftGateBounds.getHeight()/2));
        andFlow3Left.getWaypoint().add(new Point(leftXorGateBounds.getX(),leftXorGateBounds.getY()+leftXorGateBounds.getHeight()/2));

        BPMNEdge andFlow3Right = new BPMNEdge();
        andFlow3Right.setId(rightFlow_3.getId()+"_di");
        andFlow3Right.setBpmnElement(QName.valueOf(rightFlow_3.getId()));
        andFlow3Right.getWaypoint().add(new Point(rightXorGateBounds.getX()+rightXorGateBounds.getWidth(),rightXorGateBounds.getY()+rightXorGateBounds.getHeight()/2));
        andFlow3Right.getWaypoint().add(new Point(rightGateBounds.getX(),rightGateBounds.getY()+rightGateBounds.getHeight()/2));

        BPMNEdge xorLeftEdge = new BPMNEdge();
        xorLeftEdge.setId(xorLeftSequenceFlow.getId()+"_di");
        xorLeftEdge.setBpmnElement(QName.valueOf(xorLeftSequenceFlow.getId()));
        xorLeftEdge.getWaypoint().add(new Point(leftXorGateBounds.getX()+leftGateBounds.getWidth(),leftXorGateBounds.getY()+leftXorGateBounds.getHeight()/2));
        xorLeftEdge.getWaypoint().add(new Point(catchBounds.getX(),catchBounds.getY()+catchBounds.getHeight()/2));

        BPMNEdge catchOutEdge = new BPMNEdge();
        catchOutEdge.setId(catchRightSequenceFlow.getId()+"_di");
        catchOutEdge.setBpmnElement(QName.valueOf(catchRightSequenceFlow.getId()));
        catchOutEdge.getWaypoint().add(new Point(catchBounds.getX()+catchBounds.getWidth(),catchBounds.getY()+catchBounds.getHeight()/2));
        catchOutEdge.getWaypoint().add(new Point(rightXorGateBounds.getX(),rightXorGateBounds.getY()+rightXorGateBounds.getHeight()/2));

        BPMNEdge xorElseEdge = new BPMNEdge();
        xorElseEdge.setId(xorElseSequenceFlow.getId()+"_di");
        xorElseEdge.setBpmnElement(QName.valueOf(xorElseSequenceFlow.getId()));
        xorElseEdge.getWaypoint().add(new Point(rightXorGateBounds.getX()+rightXorGateBounds.getWidth()/2,rightXorGateBounds.getY()+rightXorGateBounds.getHeight()));
        xorElseEdge.getWaypoint().add(new Point(rightXorGateBounds.getX()+rightXorGateBounds.getWidth()/2,rightXorGateBounds.getY()+rightXorGateBounds.getHeight()+50));
        xorElseEdge.getWaypoint().add(new Point(leftXorGateBounds.getX()+leftXorGateBounds.getWidth()/2,rightXorGateBounds.getY()+rightXorGateBounds.getHeight()+50));
        xorElseEdge.getWaypoint().add(new Point(leftXorGateBounds.getX()+leftXorGateBounds.getWidth()/2,leftXorGateBounds.getY()+leftXorGateBounds.getHeight()));



        org.omg.spec.bpmn._20100524.di.ObjectFactory objectFactoryEdge = new org.omg.spec.bpmn._20100524.di.ObjectFactory();

        plane.getDiagramElement().add(objectFactoryEdge.createBPMNEdge(andFlow3Left));
        plane.getDiagramElement().add(objectFactoryEdge.createBPMNEdge(andFlow3Right));
        plane.getDiagramElement().add(objectFactoryEdge.createBPMNEdge(xorLeftEdge));
        plane.getDiagramElement().add(objectFactoryEdge.createBPMNEdge(catchOutEdge));
        plane.getDiagramElement().add(objectFactoryEdge.createBPMNEdge(xorElseEdge));
        plane.getDiagramElement().add(objectFactoryEdge.createBPMNEdge(andFlow1Left));

        //remove all unneeded references and objects
        references = getReferences(getIDOfObject(task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()), flowElements);
        for (TDataObjectReference data : references) {
            removeFromProcess(data, flowElements);
        }
        List<TDataObjectReference> references2 = getReferences(getIDOfObject(task.getDataOutputAssociation().get(0).getTargetRef()), flowElements);
        for (TDataObjectReference data : references2) {
            removeFromProcess(data, flowElements);
        }
        if (inputTasksCatch.size() > 1 || isAlreadyAWhiteBox(getIDOfObject(task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()),collaboration)) {
            //create WhiteBox
            replaceDataAssociationWithWhiteBox(getIDOfObject(task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()), "Sensor (IoT)", flowElements, collaboration, plane);
            //replace businessRuleTask to catch event
            changeTaskToIntermediateMessageCatchEvent(businessRuleTaskCatch, flowElements, collaboration, plane);

            //remove original reference
            removeFromProcess(task.getDataInputAssociation().get(0), flowElements);

        }
        if (outputTasks.size() > 1 || isAlreadyAWhiteBox(getIDOfObject(task.getDataOutputAssociation().get(0).getTargetRef()),collaboration)) {
            //create WhiteBox
            replaceDataAssociationWithWhiteBox(getIDOfObject(task.getDataOutputAssociation().get(0).getTargetRef()), "Actuator (IoT)", flowElements, collaboration, plane);
            //replace serviceTask to throw event
            changeTaskToIntermediateMessageThrowEvent(serviceTask, flowElements, collaboration, plane);

            //remove original reference
            removeFromProcess(task.getDataOutputAssociation().get(0), flowElements);

        }
        return new Pair<>(businessRuleTaskCatch,serviceTask);
    }

    private Pair<boolean[], String> replaceSeveralInputAssociation(TTask task, TDataObjectReference reference, List<JAXBElement<? extends TFlowElement>> flowElements, TCollaboration collaboration, BPMNPlane plane) {
        //check if the data input has only one association
        boolean[] isSingle = new boolean[task.getDataInputAssociation().size()];
        StringBuilder singleSensors = new StringBuilder("(IoT) Sensors: ");
        for (int k = 0; k < task.getDataInputAssociation().size(); k++) {
            List<TDataObjectReference> references3 = getReferences(getIDOfObject(task.getDataInputAssociation().get(k).getSourceRef().get(0).getValue()), flowElements);
            for (TDataObjectReference referenceItem : references3) {
                List<TTask> referenceTasks = getAssociatedTasks(referenceItem.getId(), false, flowElements);
                if (referenceTasks.size() <= 1) {
                    isSingle[k] = true;
                    singleSensors.append(referenceItem.getName()).append(" & ");
                    boolean flowCreated = false;
                    for (TTask taskItem : referenceTasks) {
                        for (TDataInputAssociation inputItem : taskItem.getDataInputAssociation()) {
                            if (getIDOfObject(inputItem.getSourceRef().get(0).getValue()).equals(referenceItem.getId())) {
                                if (!flowCreated) {
                                    //Create MessageFlow
                                    TMessageFlow messageFlow = new TMessageFlow();
                                    messageFlow.setId(inputItem.getId());
                                    messageFlow.setSourceRef(QName.valueOf(reference.getId()));
                                    messageFlow.setTargetRef(QName.valueOf(task.getId()));
                                    //Add to list
                                    collaboration.getMessageFlow().add(messageFlow);
                                    flowCreated = true;
                                }
                                removeFromProcess(referenceItem, flowElements);
                                break;
                            }
                        }
                    }
                } else {
                    String name = null;
                    for (TTask taskItem : referenceTasks) {
                        for (TDataInputAssociation inputItem : taskItem.getDataInputAssociation()) {
                            if (getIDOfObject(inputItem.getSourceRef().get(0).getValue()).equals(referenceItem.getId())) {
                                name = referenceItem.getName();

                                //Create MessageFlow
                                TMessageFlow messageFlow = new TMessageFlow();
                                messageFlow.setId(inputItem.getId());
                                messageFlow.setSourceRef(QName.valueOf(referenceItem.getId()));
                                messageFlow.setTargetRef(QName.valueOf(task.getId()));
                                //Add to list
                                collaboration.getMessageFlow().add(messageFlow);

                                removeFromProcess(referenceItem, flowElements);
                                changeTaskToIntermediateMessageCatchEvent(taskItem, flowElements, collaboration, plane);
                                break;
                            }
                        }
                    }
                    replaceDataAssociationWithWhiteBox(referenceItem.getId(), "(IoT) Sensor: " + name, flowElements, collaboration, plane);
                }
            }
        }
        return new Pair<>(isSingle, singleSensors.toString());
    }

    private Pair<boolean[], String> replaceSeveralOutputAssociation(TTask task, TDataObjectReference reference, List<JAXBElement<? extends TFlowElement>> flowElements, TCollaboration collaboration, BPMNPlane plane) {
        //check if the data input has only one association
        boolean[] isSingle = new boolean[task.getDataOutputAssociation().size()];
        StringBuilder singleActuators = new StringBuilder("(IoT) Actuators: ");
        for (int k = 0; k < task.getDataOutputAssociation().size(); k++) {
            List<TDataObjectReference> references = getReferences(getIDOfObject(task.getDataOutputAssociation().get(k).getTargetRef()), flowElements);
            for (TDataObjectReference referenceItem : references) {
                List<TTask> referenceTasks = getAssociatedTasks(referenceItem.getId(), true, flowElements);
                if (referenceTasks.size() <= 1) {
                    isSingle[k] = true;
                    singleActuators.append(referenceItem.getName()).append(" & ");
                    boolean flowCreated = false;
                    for (TTask taskItem : referenceTasks) {
                        for (TDataOutputAssociation outputItem : taskItem.getDataOutputAssociation()) {
                            if (getIDOfObject(outputItem.getTargetRef()).equals(referenceItem.getId())) {
                                if (!flowCreated) {
                                    //Create MessageFlow
                                    TMessageFlow messageFlow = new TMessageFlow();
                                    messageFlow.setId(outputItem.getId());
                                    messageFlow.setSourceRef(QName.valueOf(reference.getId()));
                                    messageFlow.setTargetRef(QName.valueOf(task.getId()));
                                    //Add to list
                                    collaboration.getMessageFlow().add(messageFlow);
                                    flowCreated = true;
                                }
                                removeFromProcess(referenceItem, flowElements);
                                break;
                            }
                        }
                    }
                } else {
                    String name = null;
                    for (TTask taskItem : referenceTasks) {
                        for (TDataOutputAssociation outputItem : taskItem.getDataOutputAssociation()) {
                            if (getIDOfObject(outputItem.getTargetRef()).equals(referenceItem.getId())) {
                                name = referenceItem.getName();

                                //Create MessageFlow
                                TMessageFlow messageFlow = new TMessageFlow();
                                messageFlow.setId(outputItem.getId());
                                messageFlow.setSourceRef(QName.valueOf(task.getId()));
                                messageFlow.setTargetRef(QName.valueOf(referenceItem.getId()));
                                //Add to list
                                collaboration.getMessageFlow().add(messageFlow);

                                removeFromProcess(referenceItem, flowElements);
                                changeTaskToIntermediateMessageThrowEvent(taskItem, flowElements, collaboration, plane);
                                break;
                            }
                        }
                    }
                    replaceDataAssociationWithWhiteBox(referenceItem.getId(), "(IoT) Sensor: " + name, flowElements, collaboration, plane);
                }
            }
        }
        return new Pair<>(isSingle, singleActuators.toString());
    }

    /**
     * Changes a DataInputAssociation sequence flow into a message flow
     */
    private void changeSequenceFlowToMessageFlow(TTask task, TDataInputAssociation flow, TCollaboration collaboration, BPMNPlane plane) {
        //Create MessageFlow
        TMessageFlow messageFlow = new TMessageFlow();
        messageFlow.setId(flow.getId());
        messageFlow.setSourceRef(QName.valueOf(getIDOfObject(flow.getSourceRef().get(0).getValue())));
        messageFlow.setTargetRef(QName.valueOf(task.getId()));
        //Add to list
        collaboration.getMessageFlow().add(messageFlow);

        BPMNEdge edge = (BPMNEdge) getShapeOrEdge(flow.getId(), plane);
        if (edge != null) {
            if (edge.getWaypoint().size() > 2){
                for (int m = edge.getWaypoint().size() - 2; m > 0; m--) {
                    edge.getWaypoint().remove(m);
                }
            }
        }
    }

    /**
     * Changes a DataOutputAssociation sequence flow into a message flow
     */
    private void changeSequenceFlowToMessageFlow(TTask task, TDataOutputAssociation flow, TCollaboration collaboration, BPMNPlane plane) {
        //Create MessageFlow
        TMessageFlow messageFlow = new TMessageFlow();
        messageFlow.setId(flow.getId());
        messageFlow.setSourceRef(QName.valueOf(task.getId()));
        messageFlow.setTargetRef(QName.valueOf(getIDOfObject(flow.getTargetRef())));
        //Add to list
        collaboration.getMessageFlow().add(messageFlow);

        BPMNEdge edge = (BPMNEdge) getShapeOrEdge(flow.getId(), plane);
        if (edge != null) {
            if (edge.getWaypoint().size() > 2){
                for (int m = edge.getWaypoint().size() - 2; m > 0; m--) {
                    edge.getWaypoint().remove(m);
                }
            }
        }
    }

    private void changeTaskToIntermediateMessageCatchEvent(TTask task, List<JAXBElement<? extends TFlowElement>> flowElements, TCollaboration collaboration, BPMNPlane plane) {
        //create message catch event
        TIntermediateCatchEvent catchEvent = new TIntermediateCatchEvent();
        catchEvent.setId(task.getId());
        catchEvent.setName(task.getName());
        catchEvent.getDataOutputAssociation().addAll(task.getDataOutputAssociation());
        TMessageEventDefinition messageEventDefinition = new TMessageEventDefinition();
        messageEventDefinition.setId("MessageEventDefinition_" + randomNumberSequence());
        catchEvent.getEventDefinition().add(objectFactory.createMessageEventDefinition(messageEventDefinition));
        removeFromProcess(task, flowElements);
        //add to list and reshape
        flowElements.add(objectFactory.createIntermediateCatchEvent(catchEvent));
        BPMNShape shape = (BPMNShape) getShapeOrEdge(catchEvent.getId(), plane);
        assert shape != null;
        changeShapeAttributes(catchEvent.getId(), 36, 36, (int) ((shape.getBounds().getX() + shape.getBounds().getWidth() / 2) - 18), (int) ((shape.getBounds().getY() + shape.getBounds().getHeight() / 2) - 18), false, plane);
        for (TDataOutputAssociation outputAssociation : task.getDataOutputAssociation()) {
            changeSequenceFlowToMessageFlow(task, outputAssociation, collaboration, plane);
        }
        for (TDataInputAssociation inputAssociation : task.getDataInputAssociation()) {
            changeSequenceFlowToMessageFlow(task, inputAssociation, collaboration, plane);
        }
        for (int i = 0; i < task.getOutgoing().size(); i++) {
            BPMNEdge edge = (BPMNEdge) getShapeOrEdge(task.getOutgoing().get(i).getLocalPart(), plane);
            if (edge != null) {
                edge.getWaypoint().get(0).setX(((shape.getBounds().getX() + shape.getBounds().getWidth() / 2) + 18));
                edge.getWaypoint().get(0).setY(shape.getBounds().getY() + shape.getBounds().getHeight() / 2);
            }
        }
        for (int i = 0; i < task.getIncoming().size(); i++) {
            BPMNEdge edge = (BPMNEdge) getShapeOrEdge(task.getIncoming().get(i).getLocalPart(), plane);
            if (edge != null) {
                edge.getWaypoint().get(edge.getWaypoint().size() - 1).setX(shape.getBounds().getX());
                edge.getWaypoint().get(edge.getWaypoint().size() - 1).setY(shape.getBounds().getY() + shape.getBounds().getHeight() / 2);
            }
        }
    }

    private void changeTaskToIntermediateMessageThrowEvent(TTask task, List<JAXBElement<? extends TFlowElement>> flowElements, TCollaboration collaboration, BPMNPlane plane) {
        //create message catch event
        TIntermediateThrowEvent throwEvent = new TIntermediateThrowEvent();
        throwEvent.setId(task.getId());
        throwEvent.setName(task.getName());
        throwEvent.getDataInputAssociation().addAll(task.getDataInputAssociation());
        TMessageEventDefinition messageEventDefinition = new TMessageEventDefinition();
        messageEventDefinition.setId("MessageEventDefinition_" + randomNumberSequence());
        throwEvent.getEventDefinition().add(objectFactory.createMessageEventDefinition(messageEventDefinition));
        removeFromProcess(task, flowElements);
        //add to list and reshape
        flowElements.add(objectFactory.createIntermediateThrowEvent(throwEvent));
        BPMNShape shape = (BPMNShape) getShapeOrEdge(throwEvent.getId(), plane);
        assert shape != null;
        changeShapeAttributes(throwEvent.getId(), 36, 36, (int) ((shape.getBounds().getX() + shape.getBounds().getWidth() / 2) - 18), (int) ((shape.getBounds().getY() + shape.getBounds().getHeight() / 2) - 18), false, plane);
        for (TDataOutputAssociation outputAssociation : task.getDataOutputAssociation()) {
            changeSequenceFlowToMessageFlow(task, outputAssociation, collaboration, plane);
        }
        for (TDataInputAssociation inputAssociation : task.getDataInputAssociation()) {
            changeSequenceFlowToMessageFlow(task, inputAssociation, collaboration, plane);
        }
        for (int i = 0; i < task.getOutgoing().size(); i++) {
            BPMNEdge edge = (BPMNEdge) getShapeOrEdge(task.getOutgoing().get(i).getLocalPart(), plane);
            if (edge != null) {
                edge.getWaypoint().get(0).setX(((shape.getBounds().getX() + shape.getBounds().getWidth() / 2) + 18));
                edge.getWaypoint().get(0).setY(shape.getBounds().getY() + shape.getBounds().getHeight() / 2);
            }
        }
        for (int i = 0; i < task.getIncoming().size(); i++) {
            BPMNEdge edge = (BPMNEdge) getShapeOrEdge(task.getIncoming().get(i).getLocalPart(), plane);
            if (edge != null) {
                edge.getWaypoint().get(edge.getWaypoint().size() - 1).setX(shape.getBounds().getX());
                edge.getWaypoint().get(edge.getWaypoint().size() - 1).setY(shape.getBounds().getY() + shape.getBounds().getHeight() / 2);
            }
        }
    }

    /**
     * Replaces a data association with a WhiteBox.
     *
     * @param dataId ID of the WhiteBox.
     * @param name   Name of the WhiteBox.
     */
    private void replaceDataAssociationWithWhiteBox(String dataId, String name, List<JAXBElement<? extends TFlowElement>> flowElements, TCollaboration collaboration, BPMNPlane plane) {
        if (!isAlreadyAWhiteBox(dataId,collaboration)) {
            if (!maxSet) {
                this.maxY = getMaxY(plane) + 150;
                this.minY = getMinY(plane);
                this.maxSet = true;
            }
            //in case there is no collaboration, create one
            if (collaboration.getParticipant().size() == 0) {
                TParticipant participant = new TParticipant();
                participant.setId("Participant_" + randomNumberSequence());
                participant.setName("Process 1");
                participant.setProcessRef(QName.valueOf(processID));
                collaboration.getParticipant().add(participant);
                String id = "Collaboration_" + randomNumberSequence();
                collaboration.setId(id);
                plane.setBpmnElement(QName.valueOf(id));
                plane.getDiagramElement().add(createHorizontalShape(participant.getId(), getMinX(plane) - 100, minY, getMaxX(plane) + 50, maxY));
            }
            //add white box to collaboration
            TParticipant whiteBox = new TParticipant();
            whiteBox.setId(dataId);
            whiteBoxes.add(dataId);
            whiteBox.setName(name);
            collaboration.getParticipant().add(whiteBox);

            //reposition shape
            BPMNShape shape = (BPMNShape) getShapeOrEdge(dataId, plane);
            int difference1 = (int) (shape.getBounds().getY() - minY);
            int difference2 = (int) (maxY - shape.getBounds().getY());
            if (difference1 < difference2) {
                changeShapeAttributes(dataId, 200, 50, (int) (shape.getBounds().getX() - shape.getBounds().getWidth() / 2 - 25), minY - 20 - 50, false, plane);
            } else {
                changeShapeAttributes(dataId, 200, 50, (int) (shape.getBounds().getX() - shape.getBounds().getWidth() / 2 - 25), maxY + 70, false, plane);
            }
        }
    }

    private boolean isAlreadyAWhiteBox(String dataId, TCollaboration collaboration){
        for (int i = 0; i < collaboration.getParticipant().size(); i++){
            if (dataId .equals(collaboration.getParticipant().get(i).getId())){
                return true;
            }
        }
        return false;
    }

    private List<TDataObjectReference> getReferences(String id, List<JAXBElement<? extends TFlowElement>> flowElements) {
        List<TDataObjectReference> references = new ArrayList<>();
        for (int i = 0; i < flowElements.size(); i++) {
            if (flowElements.get(i).getValue() instanceof TDataObjectReference) {
                if (((TDataObjectReference) flowElements.get(i).getValue()).getId().equals(id)) {
                    references.add(((TDataObjectReference) flowElements.get(i).getValue()));
                }
            }
        }
        return references;
    }

    /**
     * Removes the given element from the process.
     */
    private void removeFromProcess(TBaseElement element, List<JAXBElement<? extends TFlowElement>> flowElements) {
        for (int i = 0; i < flowElements.size(); i++) {
            if ((flowElements.get(i).getValue()).getId().equals(element.getId())) {
                flowElements.remove(i);
            }
        }
    }

    /**
     * Removes the given element from the collaboration.
     */
    private void removeFromCollaborationMessageFlow(String id, String associatedTaskId, TCollaboration collaboration) {
        for (int i = 0; i < collaboration.getMessageFlow().size(); i++) {
            if (collaboration.getMessageFlow().get(i).getId().equals(id) && collaboration.getMessageFlow().get(i).getSourceRef().getLocalPart().equals(associatedTaskId)) {
                collaboration.getMessageFlow().remove(i);
            } else if (collaboration.getMessageFlow().get(i).getId().equals(id) && collaboration.getMessageFlow().get(i).getTargetRef().getLocalPart().equals(associatedTaskId)) {
                collaboration.getMessageFlow().remove(i);
            }
        }
    }

    /**
     * Removes the given element from the plane.
     */
    private void removeFromPlane(String id, BPMNPlane plane) {
        for (int i = 0; i < plane.getDiagramElement().size(); i++) {
            if ((plane.getDiagramElement().get(i).getValue()).getId().equals(id)) {
                plane.getDiagramElement().remove(i);
            }
        }
    }

    /**
     * Gets the task that is associated with the dataObjectReference.
     *
     * @param id The id of the dataObjectReference aka. DataInput/OutputAssociation
     * @return The task.
     */
    private List<TTask> getAssociatedTasks(String id, boolean isForActuator , List<JAXBElement<? extends TFlowElement>> flowElements) {
        List<TTask> tasks = new ArrayList<>();
        for (int i = 0; i < flowElements.size(); i++) {
            if (flowElements.get(i).getValue() instanceof TTask) {
                TTask task = (TTask) flowElements.get(i).getValue();
                if (!isForActuator) {
                    if (task.getDataInputAssociation().size() > 0) {
                        for (TDataInputAssociation input : task.getDataInputAssociation()) {
                            for (int k = 0; k < input.getSourceRef().size(); k++) {
                                if (getIDOfObject(input.getSourceRef().get(k).getValue()).equals(id)) {
                                    tasks.add(task);
                                }
                            }
                        }
                    }
                } else {
                    if (task.getDataOutputAssociation().size() > 0) {
                        for (TDataOutputAssociation output : task.getDataOutputAssociation()) {
                            if (getIDOfObject(output.getTargetRef()).equals(id)) {
                                tasks.add(task);
                            }
                        }
                    }
                }
            }
        }
        return tasks;
    }

    private TSequenceFlow getFlow(String id, List<JAXBElement<? extends TFlowElement>> flowElements) {
        //go through all elements in the process
        for (int i = 0; i < flowElements.size(); i++) {
            //check if the element is a sequence flow
            if (flowElements.get(i).getValue() instanceof TBaseElement) {
                TBaseElement baseElement = flowElements.get(i).getValue();
                //check if the source of the flow is the current element
                if (baseElement.getId().equals(id)) {
                    //add the target of the flow to the list
                    return (TSequenceFlow) baseElement;
                }
            }
        }
        return null;
    }

    /**
     * Returns the BaseElement with the attached id.
     */
    private TBaseElement getBaseElement(String id, List<JAXBElement<? extends TFlowElement>> flowElements) {
        //go through all elements in the process
        for (int i = 0; i < flowElements.size(); i++) {
            //check if the element is a sequence flow
            if (flowElements.get(i).getValue() instanceof TBaseElement) {
                TBaseElement baseElement = flowElements.get(i).getValue();
                //check if the source of the flow is the current element
                if (baseElement.getId().equals(id)) {
                    //add the target of the flow to the list
                    return baseElement;
                }
            }
        }
        return null;
    }

    /**
     * Replaces the old id with the new id in every mention.
     */
    private void replaceAllIDsMentions(String oldID, Object newObject, String id, List<JAXBElement<? extends TFlowElement>> flowElements, BPMNPlane plane) {
        //go through all elements in the process
        for (int i = 0; i < flowElements.size(); i++) {
            //check if the element is a sequence flow
            if (flowElements.get(i).getValue() instanceof TSequenceFlow) {
                TSequenceFlow sequenceFlow = (TSequenceFlow) flowElements.get(i).getValue();
                //check if the source or target ahs the old id, if so replace it
                if (Objects.equals(getIDOfObject(sequenceFlow.getSourceRef()), oldID)) {
                    sequenceFlow.setSourceRef(newObject);
                }
                if (Objects.equals(getIDOfObject(sequenceFlow.getTargetRef()), oldID)) {
                    sequenceFlow.setTargetRef(newObject);
                }
            }
            //do the same for other nodes with outgoing and incoming types
            else if (flowElements.get(i).getValue() instanceof TFlowNode) {
                TFlowNode flowNode = (TFlowNode) flowElements.get(i).getValue();
                for (QName incoming : flowNode.getIncoming()) {
                    if (incoming.getLocalPart().equals(oldID)) {
                        flowNode.getIncoming().remove(incoming);
                        flowNode.getIncoming().add(QName.valueOf(id));
                    }
                }
                for (QName outGoing : flowNode.getOutgoing()) {
                    if (outGoing.getLocalPart().equals(oldID)) {
                        flowNode.getOutgoing().remove(outGoing);
                        flowNode.getOutgoing().add(QName.valueOf(id));
                    }
                }
            }
        }
        //check the plane elements for the old id
        for (int i = 0; i < plane.getDiagramElement().size(); i++) {
            if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNShape.class) {
                BPMNShape shape = (BPMNShape) plane.getDiagramElement().get(i).getValue();
                if (oldID.equals(shape.getBpmnElement().getLocalPart())) {
                    shape.setId(id + "_di");
                    shape.setBpmnElement(QName.valueOf(id));
                }
            } else if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNEdge.class) {
                BPMNEdge edge = (BPMNEdge) plane.getDiagramElement().get(i).getValue();
                if (oldID.equals(edge.getBpmnElement().getLocalPart())) {
                    edge.setId(id + "_di");
                    edge.setBpmnElement(QName.valueOf(id));
                }
            }
        }
    }

    /**
     * Gets the id of any element in the process tree.
     */
    private String getIDOfObject(Object object) {
        String id = "";
        if (object instanceof TActivity)
            return ((TActivity) object).getId();
        if (object instanceof TArtifact)
            return ((TArtifact) object).getId();
        if (object instanceof TEvent)
            return ((TEvent) object).getId();
        if (object instanceof TBaseElement)
            return ((TBaseElement) object).getId();
        if (object instanceof TBaseElementWithMixedContent)
            return ((TBaseElementWithMixedContent) object).getId();
        return id;
    }

    /**
     * Changes the width and height of a shape.
     */
    private void changeShapeSize(String id, int width, int height, BPMNPlane plane) {
        for (int i = 0; i < plane.getDiagramElement().size(); i++) {
            if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNShape.class) {
                BPMNShape shape = (BPMNShape) plane.getDiagramElement().get(i).getValue();
                if (id.equals(shape.getBpmnElement().getLocalPart())) {
                    List<BPMNEdge> edges = getOutgoingEdgesByX((int) (shape.getBounds().getX() + shape.getBounds().getWidth()), shape, plane);
                    for (BPMNEdge edge : edges) {
                        for (int k = 0; k < edge.getWaypoint().size(); k++) {
                            if (edge.getWaypoint().get(k).getX() == (shape.getBounds().getX() + shape.getBounds().getWidth())) {
                                edge.getWaypoint().get(k).setX((shape.getBounds().getX() + width));
                            }
                        }
                    }
                    shape.getBounds().setY(shape.getBounds().getY() - shape.getBounds().getWidth() / 2);
                    shape.getBounds().setWidth(width);
                    shape.getBounds().setHeight(height);
                }
            }
        }
    }

    /**
     * Changes the attributes of a shape.
     */
    private void changeShapeAttributes(String id, double width, double height, double x, double y, boolean isHorizontal, BPMNPlane plane) {
        for (int i = 0; i < plane.getDiagramElement().size(); i++) {
            if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNShape.class) {
                BPMNShape shape = (BPMNShape) plane.getDiagramElement().get(i).getValue();
                if (id.equals(shape.getBpmnElement().getLocalPart())) {
                    //check all outgoings
                    List<BPMNEdge> edges = getOutgoingEdgesByX((int) (shape.getBounds().getX() + shape.getBounds().getHeight()), shape, plane);
                    for (BPMNEdge edge : edges) {
                        for (int k = 0; k < edge.getWaypoint().size(); k++) {
                            if (edge.getWaypoint().get(k).getX() == (shape.getBounds().getX() + shape.getBounds().getHeight()) && Math.abs(edge.getWaypoint().get(k).getY() - shape.getBounds().getY()) < shape.getBounds().getWidth()) {
                                edge.getWaypoint().get(k).setX(x + width);
                                edge.getWaypoint().get(k).setY(y + height);
                            }
                        }
                    }
                    List<BPMNEdge> edges3 = getOutgoingEdgesByX((int) shape.getBounds().getX(), shape, plane);
                    for (BPMNEdge edge : edges3) {
                        for (int k = 0; k < edge.getWaypoint().size(); k++) {
                            if (edge.getWaypoint().get(k).getX() == shape.getBounds().getX() && Math.abs(edge.getWaypoint().get(k).getY() - shape.getBounds().getY()) < shape.getBounds().getWidth()) {
                                edge.getWaypoint().get(k).setX(x);
                                edge.getWaypoint().get(k).setY(y);
                            }
                        }
                    }
                    List<BPMNEdge> edges2 = getOutgoingEdgesByY((int) shape.getBounds().getY(), shape, plane);
                    for (BPMNEdge edge : edges2) {
                        for (int k = 0; k < edge.getWaypoint().size(); k++) {
                            if (edge.getWaypoint().get(k).getY() == shape.getBounds().getY()) {
                                edge.getWaypoint().get(k).setY(y);
                            }
                        }
                    }
                    //for some reason width is height and height is width
                    List<BPMNEdge> edges4 = getOutgoingEdgesByY((int) (shape.getBounds().getY() + shape.getBounds().getWidth()), shape, plane);
                    for (BPMNEdge edge : edges4) {
                        for (int k = 0; k < edge.getWaypoint().size(); k++) {
                            if (edge.getWaypoint().get(k).getY() == (shape.getBounds().getY() + shape.getBounds().getWidth())) {
                                edge.getWaypoint().get(k).setY(y + height);
                            }
                        }
                    }
                    //check all incoming edges
                    List<BPMNEdge> incoming_edges = getIncomingEdgesByX((int) (shape.getBounds().getX() + shape.getBounds().getHeight()), plane);
                    for (BPMNEdge edge : incoming_edges) {
                        for (int k = 0; k < edge.getWaypoint().size(); k++) {
                            if (edge.getWaypoint().get(k).getX() == (shape.getBounds().getX() + shape.getBounds().getWidth())) {
                                edge.getWaypoint().get(k).setX(x + width);
                            }
                        }
                    }
                    List<BPMNEdge> incoming_edges2 = getIncomingEdgesByY((int) shape.getBounds().getY(), shape, plane);
                    for (BPMNEdge edge : incoming_edges2) {
                        for (int k = 0; k < edge.getWaypoint().size(); k++) {
                            if (edge.getWaypoint().get(k).getY() == shape.getBounds().getY()) {
                                edge.getWaypoint().get(k).setY(y);
                            }
                        }
                    }
                    List<BPMNEdge> incoming_edges3 = getIncomingEdgesByX((int) shape.getBounds().getX(), plane);
                    for (BPMNEdge edge : incoming_edges3) {
                        for (int k = 0; k < edge.getWaypoint().size(); k++) {
                            if (edge.getWaypoint().get(k).getX() == shape.getBounds().getX()) {
                                edge.getWaypoint().get(k).setX(x);
                            }
                        }
                    }
                    List<BPMNEdge> incoming_edges4 = getIncomingEdgesByY((int) (shape.getBounds().getY() + shape.getBounds().getWidth()), shape, plane);
                    for (BPMNEdge edge : incoming_edges4) {
                        for (int k = 0; k < edge.getWaypoint().size(); k++) {
                            if (edge.getWaypoint().get(k).getY() == (shape.getBounds().getY() + shape.getBounds().getWidth())) {
                                edge.getWaypoint().get(k).setY(y + height);
                            }
                        }
                    }
                    shape.getBounds().setY(y);
                    shape.getBounds().setX(x);
                    shape.getBounds().setWidth(width);
                    shape.getBounds().setHeight(height);
                    shape.setIsHorizontal(isHorizontal);
                }
            }
        }
    }

    /**
     * Checks if the Point C is in the line drawn between Point A and Point B
     *
     * @return true if in line
     */
    private boolean inLine(Point a, Point b, Point c) {
        // Calculate the slopes of AB and AC
        double slopeAB = (b.getY() - a.getY()) / (b.getX() - a.getX());
        double slopeAC = (c.getY() - a.getY()) / (c.getX() - a.getX());

        // Check if the slopes are equal, indicating that C lies on the line AB
        return Double.compare(slopeAB, slopeAC) == 0;
    }

    /**
     * Gets all outgoing edged by the x coordinate
     */
    private List<BPMNEdge> getOutgoingEdgesByX(int x, BPMNShape shape, BPMNPlane plane) {
        List<BPMNEdge> elements = new ArrayList<>();
        for (int i = 0; i < plane.getDiagramElement().size(); i++) {
            if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNEdge.class) {
                BPMNEdge edge = (BPMNEdge) plane.getDiagramElement().get(i).getValue();
                if (inLine(new Point(shape.getBounds().getX(), shape.getBounds().getY()), new Point(shape.getBounds().getX() + shape.getBounds().getHeight(), shape.getBounds().getY()), new Point(x, edge.getWaypoint().get(0).getY()))) {
                    elements.add(edge);
                }
            }
        }
        return elements;
    }

    private List<BPMNEdge> getIncomingEdgesByX(int x, BPMNPlane plane) {
        List<BPMNEdge> elements = new ArrayList<>();
        for (int i = 0; i < plane.getDiagramElement().size(); i++) {
            if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNEdge.class) {
                BPMNEdge edge = (BPMNEdge) plane.getDiagramElement().get(i).getValue();
                if (Math.abs(x - edge.getWaypoint().get(edge.getWaypoint().size() - 1).getX()) <= 10) {
                    elements.add(edge);
                }
            }
        }
        return elements;
    }

    private List<BPMNEdge> getOutgoingEdgesByY(int y, BPMNShape shape, BPMNPlane plane) {
        List<BPMNEdge> elements = new ArrayList<>();
        for (int i = 0; i < plane.getDiagramElement().size(); i++) {
            if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNEdge.class) {
                BPMNEdge edge = (BPMNEdge) plane.getDiagramElement().get(i).getValue();
                if (inLine(new Point(shape.getBounds().getX(), shape.getBounds().getY()), new Point(shape.getBounds().getX(), shape.getBounds().getY() + shape.getBounds().getWidth()), new Point(edge.getWaypoint().get(0).getX(), y))) {
                    elements.add(edge);
                }
            }
        }
        return elements;
    }

    private List<BPMNEdge> getIncomingEdgesByY(int y, BPMNShape shape, BPMNPlane plane) {
        List<BPMNEdge> elements = new ArrayList<>();
        for (int i = 0; i < plane.getDiagramElement().size(); i++) {
            if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNEdge.class) {
                BPMNEdge edge = (BPMNEdge) plane.getDiagramElement().get(i).getValue();
                if (inLine(new Point(shape.getBounds().getX(), shape.getBounds().getY()), new Point(shape.getBounds().getX(), shape.getBounds().getY() + shape.getBounds().getWidth()), new Point(edge.getWaypoint().get(edge.getWaypoint().size() - 1).getX(), y))) {
                    elements.add(edge);
                }
            }
        }
        return elements;
    }

    /**
     * Gets the attributes of an {@link BPMNEdge} or {@link BPMNShape}.
     * Note: An array with the length of 4 is being returned containing:
     * <p>
     * [0] = x
     * </p>
     * <p>
     * [1] = y
     * </p>
     * <p>
     * [2] = x2(Edge)/width(Shape)
     * </p>
     * <p>
     * [3] = y2(Edge)/height(Shape)
     * </p>
     *
     * @param id    ID of the shape or edge.
     * @param plane {@link BPMNPlane} containing all {@link DiagramElement} .
     * @return An array with the length of 4.
     */
    private double[] getShapeOrEdgeAttributes(String id, BPMNPlane plane) {
        double[] attributes = new double[4];
        for (int i = 0; i < plane.getDiagramElement().size(); i++) {
            if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNEdge.class) {
                BPMNEdge edge = (BPMNEdge) plane.getDiagramElement().get(i).getValue();
                if (id.equals(edge.getBpmnElement().getLocalPart())) {
                    attributes[0] = edge.getWaypoint().get(0).getX();
                    attributes[1] = edge.getWaypoint().get(0).getY();
                    attributes[2] = edge.getWaypoint().get(edge.getWaypoint().size() - 1).getX();
                    attributes[3] = edge.getWaypoint().get(edge.getWaypoint().size() - 1).getY();
                    return attributes;
                }
            } else if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNShape.class) {
                BPMNShape shape = (BPMNShape) plane.getDiagramElement().get(i).getValue();
                if (id.equals(shape.getBpmnElement().getLocalPart())) {
                    attributes[0] = shape.getBounds().getX();
                    attributes[1] = shape.getBounds().getY();
                    attributes[2] = shape.getBounds().getWidth();
                    attributes[3] = shape.getBounds().getHeight();
                    return attributes;
                }
            }
        }
        return null;
    }

    /**
     * Returns the highest X coordinate in the diagram.
     */
    private int getMaxX(BPMNPlane plane) {
        int currentX = 0;
        for (int i = 0; i < plane.getDiagramElement().size(); i++) {
            if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNEdge.class) {
                BPMNEdge edge = (BPMNEdge) plane.getDiagramElement().get(i).getValue();
                int x = (int) Math.max(edge.getWaypoint().get(edge.getWaypoint().size() - 1).getX(), edge.getWaypoint().get(0).getX());
                if (x > currentX) {
                    currentX = x;
                }
            } else if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNShape.class) {
                BPMNShape shape = (BPMNShape) plane.getDiagramElement().get(i).getValue();
                int x = (int) ((int) shape.getBounds().getX() + shape.getBounds().getWidth());
                if (x > currentX) {
                    currentX = x;
                }
            }
        }
        return currentX;
    }

    /**
     * Returns the lowest X coordinate in the diagram.
     */
    private int getMinX(BPMNPlane plane) {
        int currentX = 100000;
        for (int i = 0; i < plane.getDiagramElement().size(); i++) {
            if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNEdge.class) {
                BPMNEdge edge = (BPMNEdge) plane.getDiagramElement().get(i).getValue();
                int x = (int) Math.min(edge.getWaypoint().get(edge.getWaypoint().size() - 1).getX(), edge.getWaypoint().get(0).getX());
                if (x < currentX) {
                    currentX = x;
                }
            } else if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNShape.class) {
                BPMNShape shape = (BPMNShape) plane.getDiagramElement().get(i).getValue();
                int x = (int) shape.getBounds().getX();
                if (x < currentX) {
                    currentX = x;
                }
            }
        }
        return currentX;
    }

    /**
     * Returns the highest Y coordinate in the diagram.
     */
    private int getMaxY(BPMNPlane plane) {
        int currentY = 0;
        for (int i = 0; i < plane.getDiagramElement().size(); i++) {
            if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNEdge.class) {
                BPMNEdge edge = (BPMNEdge) plane.getDiagramElement().get(i).getValue();
                int y = (int) Math.max(edge.getWaypoint().get(edge.getWaypoint().size() - 1).getY(), edge.getWaypoint().get(0).getY());
                if (y > currentY) {
                    currentY = y;
                }
            } else if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNShape.class) {
                BPMNShape shape = (BPMNShape) plane.getDiagramElement().get(i).getValue();
                int y = (int) ((int) shape.getBounds().getY() + shape.getBounds().getHeight());
                if (y > currentY) {
                    currentY = y;
                }
            }
        }
        return currentY;
    }

    /**
     * Returns the lowest Y coordinate in the diagram.
     */
    private int getMinY(BPMNPlane plane) {
        int currentY = 0;
        for (int i = 0; i < plane.getDiagramElement().size(); i++) {
            if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNEdge.class) {
                BPMNEdge edge = (BPMNEdge) plane.getDiagramElement().get(i).getValue();
                int y = (int) Math.min(edge.getWaypoint().get(edge.getWaypoint().size() - 1).getY(), edge.getWaypoint().get(0).getY());
                if (y < currentY) {
                    currentY = y;
                }
            } else if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNShape.class) {
                BPMNShape shape = (BPMNShape) plane.getDiagramElement().get(i).getValue();
                int y = (int) shape.getBounds().getY();
                if (y < currentY) {
                    currentY = y;
                }
            }
        }
        return currentY;
    }

    /**
     * Gets the Shape or Edge from the plane with the corresponding id.
     *
     * @param id    ID of the needed shape or edge.
     * @param plane The plane containing all shapes and edges.
     * @return A {@link DiagramElement} which can either be a {@link BPMNShape} or {@link BPMNEdge}
     */
    private DiagramElement getShapeOrEdge(String id, BPMNPlane plane) {
        for (int i = 0; i < plane.getDiagramElement().size(); i++) {
            if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNEdge.class) {
                BPMNEdge edge = (BPMNEdge) plane.getDiagramElement().get(i).getValue();
                if (id.equals(edge.getBpmnElement().getLocalPart())) {
                    return edge;
                }
            } else if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNShape.class) {
                BPMNShape shape = (BPMNShape) plane.getDiagramElement().get(i).getValue();
                if (id.equals(shape.getBpmnElement().getLocalPart())) {
                    return shape;
                }
            }
        }
        return null;
    }

    /**
     * Creates an edge.
     *
     * @param id The id of the referenced bpmn element.
     * @param x  An array with the x coordinates of all waypoints.
     * @param y  An array with the y coordinates of all waypoints.
     * @return A JAXBElement that can be added to the list.
     */
    private JAXBElement<BPMNEdge> createEdge(String id, int[] x, int[] y) {
        org.omg.spec.bpmn._20100524.di.ObjectFactory objectFactory = new org.omg.spec.bpmn._20100524.di.ObjectFactory();
        BPMNEdge bpmnEdge = new BPMNEdge();
        bpmnEdge.setBpmnElement(new QName(id));
        bpmnEdge.setId(id + "_di");
        for (int i = 0; i < x.length; i++) {
            Point waypoint = new Point(x[i], y[i]);
            bpmnEdge.getWaypoint().add(waypoint);
        }
        return objectFactory.createBPMNEdge(bpmnEdge);
    }


    /**
     * Creates a shape WITHOUT a label.
     *
     * @param bpmnElement The id of the referenced bpmn element.
     * @param x           x coordinate of the element.
     * @param y           y coordinate of the element.
     * @param width       Width of the shape.
     * @param height      height of the shape.
     * @return A JAXBElement that can be added to the list.
     */
    private JAXBElement<BPMNShape> createShape(String bpmnElement, int x, int y, int width, int height) {
        org.omg.spec.bpmn._20100524.di.ObjectFactory objectFactory = new org.omg.spec.bpmn._20100524.di.ObjectFactory();
        BPMNShape bpmnShape = new BPMNShape();
        bpmnShape.setBpmnElement(QName.valueOf(bpmnElement));
        bpmnShape.setId(bpmnElement + "_di");
        Bounds bounds = new Bounds();
        bounds.setX(x);
        bounds.setY(y);
        bounds.setHeight(height);
        bounds.setWidth(width);
        bpmnShape.setBounds(bounds);
        return objectFactory.createBPMNShape(bpmnShape);
    }

    /**
     * Creates a shape WITHOUT a label.
     *
     * @param bpmnElement The id of the referenced bpmn element.
     * @param x           x coordinate of the element.
     * @param y           y coordinate of the element.
     * @param width       Width of the shape.
     * @param height      height of the shape.
     * @return A JAXBElement that can be added to the list.
     */
    private JAXBElement<BPMNShape> createHorizontalShape(String bpmnElement, int x, int y, int width, int height) {
        org.omg.spec.bpmn._20100524.di.ObjectFactory objectFactory = new org.omg.spec.bpmn._20100524.di.ObjectFactory();
        BPMNShape bpmnShape = new BPMNShape();
        bpmnShape.setBpmnElement(QName.valueOf(bpmnElement));
        bpmnShape.setId(bpmnElement + "_di");
        Bounds bounds = new Bounds();
        bounds.setX(x);
        bounds.setY(y);
        bounds.setHeight(height);
        bounds.setWidth(width);
        bpmnShape.setIsHorizontal(true);
        bpmnShape.setBounds(bounds);
        return objectFactory.createBPMNShape(bpmnShape);
    }

    /**
     * Creates a shape with a label.
     *
     * @param bpmnElement The id of the referenced bpmn element.
     * @param shapeBounds An int array with the length of 4, containing [0] = x, [1] = y, [2] = width, [3] = height.
     * @param labelBounds An int array with the length of 4, containing [0] = x, [1] = y, [2] = width, [3] = height.
     * @return A JAXBElement that can be added to the list.
     */
    private JAXBElement<BPMNShape> createShapeAndLabel(String bpmnElement, int[] shapeBounds, int[] labelBounds) {
        org.omg.spec.bpmn._20100524.di.ObjectFactory objectFactory = new org.omg.spec.bpmn._20100524.di.ObjectFactory();
        BPMNShape bpmnShape = new BPMNShape();
        bpmnShape.setBpmnElement(QName.valueOf(bpmnElement));
        bpmnShape.setId(bpmnElement + "_di");
        Bounds bounds = new Bounds();
        bounds.setX(shapeBounds[0]);
        bounds.setY(shapeBounds[1]);
        bounds.setHeight(shapeBounds[2]);
        bounds.setWidth(shapeBounds[3]);
        bpmnShape.setBounds(bounds);
        //create Label
        BPMNLabel bpmnLabel = new BPMNLabel();
        Bounds bounds2 = new Bounds();
        bounds2.setX(labelBounds[0]);
        bounds2.setY(labelBounds[1]);
        bounds2.setHeight(labelBounds[2]);
        bounds2.setWidth(labelBounds[3]);
        bpmnLabel.setBounds(bounds2);
        bpmnShape.setBPMNLabel(bpmnLabel);
        return objectFactory.createBPMNShape(bpmnShape);
    }

    /**
     * Gets a random 7 letter sequence, used for IDs.
     *
     * @return A String with the length of 7.
     */
    private String randomNumberSequence() {
        // Create a new Random object
        Random rand = new Random();
        //create alphabet
        String alphabet = "abcdefghijklmnopqrstuvwxyz0123456789";
        // Generate the random sequence
        StringBuilder sequence = new StringBuilder();
        for (int i = 0; i < 7; i++) {
            int index = rand.nextInt(alphabet.length());
            sequence.append(alphabet.charAt(index));
        }
        return sequence.toString();
    }
}
