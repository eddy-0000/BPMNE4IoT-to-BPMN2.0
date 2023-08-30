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
import javax.xml.parsers.ParserConfigurationException;

import org.omg.spec.dd._20100524.di.DiagramElement;
import org.w3c.dom.*;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.bind.*;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class XMLToJava {

    private final ObjectFactory objectFactory = new ObjectFactory();
    private boolean maxSet = false;
    private int maxY;
    private int minY;

    public void convertXML(File xmlFile) throws ParserConfigurationException, IOException {
        try {
            // create a JAXB context instance for the BPMN model package
            JAXBContext jaxbProcessContext = JAXBContext.newInstance("org.omg.spec.bpmn._20100524.model");

            // create an unmarshall to convert XML to Java objects
            Unmarshaller unmarshaller = jaxbProcessContext.createUnmarshaller();
            Marshaller marshaller = jaxbProcessContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // unmarshal the XML file into a Java object of the Definitions class
            TDefinitions root = (TDefinitions) JAXBIntrospector.getValue(unmarshaller.unmarshal(xmlFile));

            TProcess process = new TProcess();
            TCollaboration collaboration = new TCollaboration();

            if (root.getRootElement().size() == 2){
                process = (TProcess) root.getRootElement().get(1).getValue();
                collaboration = (TCollaboration) root.getRootElement().get(0).getValue();
            } else if (root.getRootElement().size() == 1){
                process = (TProcess) root.getRootElement().get(0).getValue();
                root.getRootElement().add(objectFactory.createCollaboration(collaboration));
            }




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
                        cord[0] = Integer.parseInt(attributes.getNamedItem("x").getNodeValue());
                        cord[1] = Integer.parseInt(attributes.getNamedItem("y").getNodeValue());
                        cord[2] = Integer.parseInt(attributes.getNamedItem("width").getNodeValue());
                        cord[3] = Integer.parseInt(attributes.getNamedItem("height").getNodeValue());
                        if (element.getElementsByTagName("bpmndi:BPMNLabel").getLength() > 0) {
                            NamedNodeMap attributes2 = ((Element) element.getElementsByTagName("bpmndi:BPMNLabel").item(0)).getElementsByTagName("dc:Bounds").item(0).getAttributes();
                            int[] cord2 = new int[4];
                            cord2[0] = Integer.parseInt(attributes2.getNamedItem("x").getNodeValue());
                            cord2[1] = Integer.parseInt(attributes2.getNamedItem("y").getNodeValue());
                            cord2[2] = Integer.parseInt(attributes2.getNamedItem("width").getNodeValue());
                            cord2[3] = Integer.parseInt(attributes2.getNamedItem("height").getNodeValue());
                            plane.getDiagramElement().add(createShapeAndLabel(children.item(j).getAttributes().getNamedItem("bpmnElement").getNodeValue(), cord, cord2));
                        } else {
                            plane.getDiagramElement().add(createShape(children.item(j).getAttributes().getNamedItem("bpmnElement").getNodeValue(), cord[0], cord[1], cord[2], cord[3]));
                        }
                    }
                }
            }
            boolean reduceCounter = false;

            for (int i = 0; i < process.getFlowElement().size(); i++){
                if (reduceCounter){
                    i--;
                    reduceCounter = false;
                }
                if (process.getFlowElement().get(i).getValue().getClass() == TStartEvent.class) {
                    for (QName key:process.getFlowElement().get(i).getValue().getOtherAttributes().keySet()) {
                        TStartEvent startEvent = (TStartEvent)process.getFlowElement().get(i).getValue();
                        //checks if it's of iot:type="start"
                        if (key.equals(new QName("http://some-company/schema/bpmn/iot", "type")) && process.getFlowElement().get(i).getValue().getOtherAttributes().get(key).equals("start")) {
                            TStartEvent condEvent = new TStartEvent();
                            condEvent.setId(process.getFlowElement().get(i).getValue().getId());
                            condEvent.setName(process.getFlowElement().get(i).getValue().getName());
                            for (int j=0; j<startEvent.getOutgoing().size(); j++){
                                condEvent.getOutgoing().add(startEvent.getOutgoing().get(j));
                            }
                            TConditionalEventDefinition conditionalEventDefinition = new TConditionalEventDefinition();
                            TExpression expression = new TExpression();
                            expression.setId("ConditionalEvent"+randomNumberSequence());
                            conditionalEventDefinition.setCondition(expression);
                            JAXBElement <TConditionalEventDefinition> eventDefinition = objectFactory.createConditionalEventDefinition(conditionalEventDefinition);
                            condEvent.getEventDefinition().add(eventDefinition);
                            JAXBElement<TStartEvent> startElement = objectFactory.createStartEvent(condEvent);
                            process.getFlowElement().remove(i);
                            process.getFlowElement().add(startElement);
                            reduceCounter = true;
                        }
                    }
                }
                else if (process.getFlowElement().get(i).getValue().getClass() == TActivity.class) {
                    //TODO: Do Nothing?
                }
                else if (process.getFlowElement().get(i).getValue().getClass() == TBoundaryEvent.class) {
                    //TODO: Do Nothing?
                }
                else if (process.getFlowElement().get(i).getValue().getClass() == TEndEvent.class) {
                    for (QName key:process.getFlowElement().get(i).getValue().getOtherAttributes().keySet()) {
                        TEndEvent iotEnd = (TEndEvent)process.getFlowElement().get(i).getValue();
                        //checks if it's of iot:type="start"
                        if (key.equals(new QName("http://some-company/schema/bpmn/iot", "type")) && process.getFlowElement().get(i).getValue().getOtherAttributes().get(key).equals("end")) {
                            //create all needed objects
                            TSequenceFlow sequenceFlow = new TSequenceFlow();
                            TServiceTask serviceTask = new TServiceTask();
                            TEndEvent normalEnd = new TEndEvent();

                            //create IDs
                            String taskID = "Activity_"+randomNumberSequence();
                            String flowID = "Flow_"+randomNumberSequence();
                            String endID = "Event_"+randomNumberSequence();

                            //set the attributes for the service task
                            serviceTask.setId(taskID);
                            serviceTask.setName(process.getFlowElement().get(i).getValue().getName());
                            serviceTask.getIncoming().add(iotEnd.getIncoming().get(0));
                            serviceTask.getOutgoing().add(QName.valueOf(flowID));
                            if (iotEnd.getProperty().size() > 0) {
                                serviceTask.getProperty().addAll(iotEnd.getProperty());
                            } else {
                                serviceTask.getProperty().clear();
                            }
                            //replace the id of the prior object with the new one
                            replaceAllIDsMentions(process.getFlowElement().get(i).getValue().getId(), serviceTask, taskID,process,plane);

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
                            process.getFlowElement().remove(i);
                            process.getFlowElement().add(taskElement);
                            process.getFlowElement().add(flowElement);
                            process.getFlowElement().add(endElement);

                            //reposition and resize the service task
                            double[] serviceCord = getShapeOrEdgeAttributes(taskID,plane);
                            BPMNShape serviceShape = (BPMNShape) getShapeOrEdge(taskID,plane);
                            assert serviceCord != null;
                            serviceCord[1] = serviceCord[1]-serviceCord[3]/2;
                            assert serviceShape != null;
                            serviceShape.getBounds().setY(serviceCord[1]);
                            serviceShape.getBounds().setWidth(100);
                            serviceShape.getBounds().setHeight(80);

                            //calculate position of the flow nad the end event
                            serviceCord = getShapeOrEdgeAttributes(taskID,plane);
                            int[] edgeX = new int[2];
                            int[] edgeY = new int[2];
                            assert serviceCord != null;
                            edgeX[0] = (int) (serviceCord[0]+serviceCord[2]);
                            edgeY[0] = (int) (serviceCord[1]+serviceCord[3]/2);
                            edgeX[1] = (int) (serviceCord[0]+serviceCord[2]+100);
                            edgeY[1] = (int) (serviceCord[1]+serviceCord[3]/2);
                            //create shape and edge for the newly created objects
                            plane.getDiagramElement().add(createEdge(flowID, edgeX, edgeY));
                            plane.getDiagramElement().add(createShape(endID, edgeX[1], edgeY[1]-18,36,36));
                            //remove the replaced object from the list
                            i--;
                        }
                    }
                }
                else if (process.getFlowElement().get(i).getValue() instanceof TCatchEvent) {
                    for (QName key:process.getFlowElement().get(i).getValue().getOtherAttributes().keySet()) {
                        //checks if it's of iot:type="catch"
                        if (key.equals(new QName("http://some-company/schema/bpmn/iot", "type")) && process.getFlowElement().get(i).getValue().getOtherAttributes().get(key).equals("catch")) {
                            TIntermediateCatchEvent iotThrow = (TIntermediateCatchEvent)process.getFlowElement().get(i).getValue();
                            //create the service task
                            TBusinessRuleTask businessRuleTask = new TBusinessRuleTask();
                            TSequenceFlow xorOutgoingIf = new TSequenceFlow();
                            TSequenceFlow xorOutgoingElse = new TSequenceFlow();
                            TExclusiveGateway exclusiveGateway = new TExclusiveGateway();

                            //generate IDs
                            String taskID = "Activity_"+randomNumberSequence();
                            String gateWayId = "Gateway_"+randomNumberSequence();
                            String flow1Id = "Flow_"+randomNumberSequence();
                            String flow2Id = "Flow_"+randomNumberSequence();

                            //set attributes
                            exclusiveGateway.setId(gateWayId);
                            xorOutgoingIf.setId(flow1Id);
                            xorOutgoingIf.setName("True");
                            xorOutgoingElse.setId(flow2Id);
                            xorOutgoingElse.setName("False");

                            TSequenceFlow originalOutgoingFlow = (TSequenceFlow) getBaseElement(iotThrow.getOutgoing().get(0).getLocalPart(),process);
                            TSequenceFlow originalIncomingFlow = (TSequenceFlow) getBaseElement(iotThrow.getIncoming().get(0).getLocalPart(),process);

                            assert originalOutgoingFlow != null;
                            exclusiveGateway.getIncoming().add(QName.valueOf(originalOutgoingFlow.getId()));
                            exclusiveGateway.getOutgoing().add(QName.valueOf(xorOutgoingIf.getId()));
                            exclusiveGateway.getOutgoing().add(QName.valueOf(xorOutgoingElse.getId()));

                            xorOutgoingIf.setSourceRef(exclusiveGateway);
                            xorOutgoingElse.setSourceRef(exclusiveGateway);
                            xorOutgoingIf.setTargetRef(originalOutgoingFlow.getTargetRef());
                            xorOutgoingElse.setTargetRef(businessRuleTask);

                            originalOutgoingFlow.setTargetRef(exclusiveGateway);

                            businessRuleTask.setId(taskID);
                            businessRuleTask.setName(iotThrow.getName());
                            businessRuleTask.getOutgoing().add(QName.valueOf(originalOutgoingFlow.getId()));
                            businessRuleTask.getIncoming().addAll(iotThrow.getIncoming());

                            replaceAllIDsMentions(iotThrow.getId(),businessRuleTask,taskID,process,plane);


                            JAXBElement<TBusinessRuleTask> taskElement = objectFactory.createBusinessRuleTask(businessRuleTask);
                            JAXBElement<TExclusiveGateway> gateElement = objectFactory.createExclusiveGateway(exclusiveGateway);
                            JAXBElement<TSequenceFlow> flow1Element = objectFactory.createSequenceFlow(xorOutgoingIf);
                            JAXBElement<TSequenceFlow> flow2Element = objectFactory.createSequenceFlow(xorOutgoingElse);

                            process.getFlowElement().remove(i);
                            process.getFlowElement().add(taskElement);
                            process.getFlowElement().add(gateElement);
                            process.getFlowElement().add(flow1Element);
                            process.getFlowElement().add(flow2Element);

                            //replace all ids
                            replaceAllIDsMentions(iotThrow.getId(),businessRuleTask,taskID,process,plane);

                            //reposition all items
                            changeShapeSize(taskID,100,80,plane);
                            BPMNShape shape = (BPMNShape) getShapeOrEdge(taskID,plane);
                            assert shape != null;
                            shape.getBounds().setX(shape.getBounds().getX()-50);
                            BPMNEdge flow = (BPMNEdge) getShapeOrEdge(originalOutgoingFlow.getId(),plane);
                            assert flow != null;
                            flow.getWaypoint().get(0).setX(shape.getBounds().getX()+shape.getBounds().getWidth());
                            assert originalIncomingFlow != null;
                            BPMNEdge flow2 = (BPMNEdge) getShapeOrEdge(originalIncomingFlow.getId(),plane);
                            assert flow2 != null;
                            flow2.getWaypoint().get(flow2.getWaypoint().size()-1).setX(shape.getBounds().getX());

                            double[] shapeAttributes = getShapeOrEdgeAttributes(taskID,plane);
                            double[] edgeOutgoingAttributes = getShapeOrEdgeAttributes(originalOutgoingFlow.getId(),plane);
                            assert edgeOutgoingAttributes != null;
                            int edgeLength = (int) (edgeOutgoingAttributes[2]-edgeOutgoingAttributes[0]);
                            int edgeMiddle = (int) (edgeOutgoingAttributes[0]+edgeLength/2);
                            int gatewayX = edgeMiddle-18;
                            int gatewayY = (int) (edgeOutgoingAttributes[1]-18);
                            int gatewaySize = 36;


                            int[] edge1X = new int[4];
                            edge1X[0] = gatewayX+gatewaySize/2;
                            edge1X[1] = gatewayX+gatewaySize/2;
                            assert shapeAttributes != null;
                            edge1X[2] = (int) (shapeAttributes[0]+shapeAttributes[2]/2);
                            edge1X[3] = (int) (shapeAttributes[0]+shapeAttributes[2]/2);

                            int[] edge1Y = new int[4];
                            edge1Y[0] = gatewayY+gatewaySize;
                            edge1Y[1] = gatewayY+gatewaySize+50;
                            edge1Y[2] = gatewayY+gatewaySize+50;
                            edge1Y[3] = (int) (shapeAttributes[1]+shapeAttributes[3]);


                            int[] edge2X = new int[2];
                            edge2X[0] = gatewayX+gatewaySize;
                            edge2X[1] = (int) edgeOutgoingAttributes[2];

                            int[] edge2Y = new int[2];
                            edge2Y[0] = gatewayY+gatewaySize/2;
                            edge2Y[1] = gatewayY+gatewaySize/2;

                            flow.getWaypoint().get(flow.getWaypoint().size()-1).setX(gatewayX);

                            plane.getDiagramElement().add(createEdge(xorOutgoingIf.getId(),edge2X,edge2Y));
                            plane.getDiagramElement().add(createEdge(xorOutgoingElse.getId(),edge1X,edge1Y));
                            plane.getDiagramElement().add(createShape(gateWayId,gatewayX, gatewayY,gatewaySize,gatewaySize));
                            i--;
                        }

                        else if (key.equals(new QName("http://some-company/schema/bpmn/iot", "type")) && process.getFlowElement().get(i).getValue().getOtherAttributes().get(key).equals("throw")) {
                            TIntermediateCatchEvent iotThrow = (TIntermediateCatchEvent)process.getFlowElement().get(i).getValue();
                            //create the service task
                            String taskID = "Activity_"+randomNumberSequence();
                            TServiceTask serviceTask = new TServiceTask();
                            serviceTask.setId(taskID);
                            serviceTask.setName(iotThrow.getName());
                            serviceTask.getOutgoing().addAll(iotThrow.getOutgoing());
                            serviceTask.getIncoming().addAll(iotThrow.getIncoming());
                            JAXBElement<TServiceTask> taskElement = objectFactory.createServiceTask(serviceTask);
                            process.getFlowElement().remove(i);
                            process.getFlowElement().add(taskElement);
                            //replace all ids
                            replaceAllIDsMentions(iotThrow.getId(),serviceTask,taskID,process,plane);
                            changeShapeSize(taskID,100,80,plane);
                            i--;
                        }
                    }
                }
                else if (process.getFlowElement().get(i).getValue().getClass() == TDataObjectReference.class) {
                    for (int keyItem = 0; keyItem < process.getFlowElement().get(i).getValue().getOtherAttributes().keySet().toArray().length; keyItem++){
                        QName key = (QName) process.getFlowElement().get(i).getValue().getOtherAttributes().keySet().toArray()[keyItem];
                        if (key.equals(new QName("http://some-company/schema/bpmn/iot", "type")) && process.getFlowElement().get(i).getValue().getOtherAttributes().get(key).equals("obj")) {
                            process.getFlowElement().get(i).getValue().getOtherAttributes().remove(key);
                        } else if (key.equals(new QName("http://some-company/schema/bpmn/iot", "type")) && process.getFlowElement().get(i).getValue().getOtherAttributes().get(key).equals("artefact-catch")) {
                            TDataObjectReference reference = (TDataObjectReference) process.getFlowElement().get(i).getValue();
                            List<TTask> tasks = getAssociatedTasks(reference.getId(),process);
                            if (tasks.size() == 1){
                                TTask task = tasks.get(0);
                                TServiceTask serviceTask = new TServiceTask();
                                serviceTask.setId(task.getId());
                                serviceTask.setName(task.getName());
                                serviceTask.getIncoming().addAll(task.getIncoming());
                                serviceTask.getOutgoing().addAll(task.getOutgoing());
                                serviceTask.getProperty().addAll(task.getProperty());

                                JAXBElement<TServiceTask> taskElement = objectFactory.createServiceTask(serviceTask);
                                process.getFlowElement().remove(i);
                                removeFromProcess(task,process);
                                process.getFlowElement().add(taskElement);
                                i--;
                            }
                            else {
                               replaceSeveralOutputAssociation(tasks.get(0),reference,process,collaboration,plane);
                            }
                        } else if (key.equals(new QName("http://some-company/schema/bpmn/iot", "type")) && (process.getFlowElement().get(i).getValue().getOtherAttributes().get(key).equals("sensor") || process.getFlowElement().get(i).getValue().getOtherAttributes().get(key).equals("sensor-sub"))) {
                            TDataObjectReference reference = (TDataObjectReference) process.getFlowElement().get(i).getValue();
                            List<TTask> tasks = getAssociatedTasks(reference.getId(), process);
                            keyItem -= changeIoTDependantTask(tasks,reference,true,process,collaboration,plane);

                        } else if (key.equals(new QName("http://some-company/schema/bpmn/iot", "type")) && (process.getFlowElement().get(i).getValue().getOtherAttributes().get(key).equals("actor") || process.getFlowElement().get(i).getValue().getOtherAttributes().get(key).equals("actor-sub"))) {
                            TDataObjectReference reference = (TDataObjectReference) process.getFlowElement().get(i).getValue();
                            List<TTask> tasks = getAssociatedTasks(reference.getId(), process);
                            keyItem -= changeIoTDependantTask(tasks,reference,false,process,collaboration,plane);
                        }
                    }
                }
            }
            // Marshal the JAXBElement to an XML file
            String exportedFile = xmlFile.getAbsolutePath().substring(0, xmlFile.getAbsolutePath().length() - 5);
            exportedFile += "_copy.bpmn";
            marshaller.marshal(root, new File(exportedFile));
        } catch (JAXBException | SAXException e) {
            e.printStackTrace();
        }
    }

    private int changeIoTDependantTask(List<TTask> tasks, TDataObjectReference reference, boolean isSensor, TProcess process, TCollaboration collaboration, BPMNPlane plane) {
        int counter = 0;
        List<TTask> toRemove = new ArrayList<>();
        Iterator<TTask> iterator = tasks.iterator();
        while (iterator.hasNext()){
            //get the current task
            TTask task = iterator.next();
            boolean removed = false;
            for (TTask remove: toRemove){
                if (remove.getId().equals(task.getId())){
                    removed = true;
                    break;
                }
            }
            if (removed){
                iterator.remove();
                break;
            }
            if (task.getDataOutputAssociation().size() == 0 && task.getDataInputAssociation().size() == 1) {
                List<TTask> inputTasks = getAssociatedTasks(reference.getId(),process);
                if (inputTasks.size() <= 1) {
                    TBusinessRuleTask businessRuleTask = new TBusinessRuleTask();
                    businessRuleTask.setId(task.getId());
                    businessRuleTask.setName(task.getName());
                    businessRuleTask.getIncoming().addAll(task.getIncoming());
                    businessRuleTask.getOutgoing().addAll(task.getOutgoing());
                    businessRuleTask.getProperty().addAll(task.getProperty());

                    JAXBElement<TBusinessRuleTask> taskElement = objectFactory.createBusinessRuleTask(businessRuleTask);
                    removeFromProcess(reference, process);
                    removeFromProcess(task, process);
                    counter++;
                    process.getFlowElement().add(taskElement);
                } else {
                    //create WhiteBox
                    replaceDataAssociationWithWhiteBox(reference.getId(), "Sensor (IoT)" + reference.getName(),process,collaboration,plane);

                    List<TTask> tempTasks = new ArrayList<>();
                    tempTasks.add(inputTasks.get(1));
                    changeIoTDependantTask(tempTasks, reference, isSensor, process, collaboration, plane);
                    //remove original reference
                    removeFromProcess(reference,process);
                    for (TTask exTask: inputTasks) {
                        toRemove.add(exTask);
                        if (!exTask.getId().equals(inputTasks.get(1).getId())) {
                            changeTaskToIntermediateMessageCatchEvent(exTask, process, collaboration, plane);
                        }
                    }
                }
            } else if (task.getDataOutputAssociation().size() == 1 && task.getDataInputAssociation().size() == 1) {

                replaceOneInputAndOneOutputAssociation(task, process, collaboration, plane);

                List<TTask> inputTasks = getAssociatedTasks(reference.getId(),process);
                if (inputTasks.size() > 1) {
                    //create WhiteBox
                    replaceDataAssociationWithWhiteBox(reference.getId(), "Sensor (IoT)" + reference.getName(),process,collaboration,plane);
                    //remove original reference
                    removeFromProcess(reference,process);
                    if (isSensor) {
                        for (TTask exTask: inputTasks) {
                            toRemove.add(exTask);
                            changeTaskToIntermediateMessageCatchEvent(exTask,process, collaboration, plane);
                        }
                        List<TTask> outputTasks = getAssociatedTasks(getIDOfObject(task.getDataOutputAssociation().get(0).getTargetRef()),process);
                        //create WhiteBox
                        replaceDataAssociationWithWhiteBox(getIDOfObject(task.getDataOutputAssociation().get(0).getTargetRef()), "Actuator (IoT)" + reference.getName(),process,collaboration,plane);
                        for (TTask exTask: outputTasks) {
                            toRemove.add(exTask);
                            changeTaskToIntermediateMessageThrowEvent(exTask,process, collaboration, plane);
                        }
                    } else {
                        for (TTask exTask: inputTasks) {
                            toRemove.add(exTask);
                            changeTaskToIntermediateMessageThrowEvent(exTask,process, collaboration, plane);
                        }
                        List<TTask> outputTasks = getAssociatedTasks(getIDOfObject(task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()),process);
                        //create WhiteBox
                        replaceDataAssociationWithWhiteBox(getIDOfObject(task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()), "Sensor (IoT)" + reference.getName(),process,collaboration,plane);
                        for (TTask exTask: outputTasks) {
                            toRemove.add(exTask);
                            changeTaskToIntermediateMessageCatchEvent(exTask,process, collaboration, plane);
                        }
                    }
                }

                removeTaskFlows(task,collaboration);
                removeFromProcess(task, process);
                counter++;

            } else if (task.getDataOutputAssociation().size() == 0 && task.getDataInputAssociation().size() > 1){

                Pair<boolean[], String> pair1 = replaceSeveralInputAssociation(task,reference,process,collaboration,plane);
                boolean[] isSingle = pair1.getKey();
                String singleSensors = pair1.getValue();

                if (isSingle.length >= 1){
                    //create WhiteBox
                    singleSensors = singleSensors.substring(0,singleSensors.length()-3);
                    replaceDataAssociationWithWhiteBox(reference.getId(), singleSensors, process,collaboration,plane);

                    //remove original reference
                    removeFromProcess(reference,process);
                    List<TTask> inputTasks = getAssociatedTasks(reference.getId(),process);
                    if (inputTasks.size() > 1) {
                        if (isSensor) {
                            for (TTask exTask: inputTasks) {
                                toRemove.add(exTask);
                                changeTaskToIntermediateMessageCatchEvent(exTask,process, collaboration, plane);
                            }
                        } else {
                            for (TTask exTask: inputTasks) {
                                toRemove.add(exTask);
                                changeTaskToIntermediateMessageThrowEvent(exTask,process, collaboration, plane);
                            }
                        }
                    } else {
                        changeTaskToIntermediateMessageCatchEvent(task, process, collaboration, plane);
                    }
                }

            } else if (task.getDataOutputAssociation().size() == 1 && task.getDataInputAssociation().size() > 1) {

                TBusinessRuleTask businessRuleTask = replaceOneInputAndOneOutputAssociation(task,process,collaboration,plane).getKey();
                Pair<boolean[], String> pair1 = replaceSeveralInputAssociation(businessRuleTask,reference,process,collaboration,plane);
                boolean[] isSingle = pair1.getKey();
                String singleSensors = pair1.getValue();

                if (isSingle.length >= 1){
                    //create WhiteBox
                    singleSensors = singleSensors.substring(0,singleSensors.length()-3);
                    replaceDataAssociationWithWhiteBox(reference.getId(), singleSensors,process,collaboration,plane);

                    //remove original reference
                    removeFromProcess(reference,process);
                    List<TTask> inputTasks = getAssociatedTasks(reference.getId(),process);
                    if (inputTasks.size() > 1) {
                        if (isSensor) {
                            for (TTask exTask: inputTasks) {
                                toRemove.add(exTask);
                                changeTaskToIntermediateMessageCatchEvent(exTask,process, collaboration, plane);
                            }
                        } else {
                            for (TTask exTask: inputTasks) {
                                toRemove.add(exTask);
                                changeTaskToIntermediateMessageThrowEvent(exTask,process, collaboration, plane);
                            }
                        }
                    } else {
                        changeTaskToIntermediateMessageCatchEvent(task, process, collaboration, plane);
                    }
                }

                removeFromProcess(task,process);
                counter++;

            } else if (task.getDataOutputAssociation().size() > 1 && task.getDataInputAssociation().size() > 1) {

                Pair<TBusinessRuleTask, TServiceTask> pair3 = replaceOneInputAndOneOutputAssociation(task,process,collaboration,plane);
                TBusinessRuleTask businessRuleTask = pair3.getKey();
                TServiceTask serviceTask = pair3.getValue();

                Pair<boolean[], String> pair1 = replaceSeveralInputAssociation(businessRuleTask,reference,process,collaboration,plane);
                boolean[] isSingle1 = pair1.getKey();
                String singleSensors = pair1.getValue();

                Pair<boolean[], String> pair2 = replaceSeveralOutputAssociation(serviceTask,reference,process,collaboration,plane);
                boolean[] isSingle2 = pair2.getKey();
                String singleActuators = pair2.getValue();

                if (isSingle1.length >= 1){
                    //create WhiteBox
                    singleSensors = singleSensors.substring(0,singleSensors.length()-3);
                    replaceDataAssociationWithWhiteBox(reference.getId(), singleSensors,process,collaboration,plane);

                    //remove original reference
                    removeFromProcess(reference,process);
                    List<TTask> inputTasks = getAssociatedTasks(reference.getId(),process);
                    if (inputTasks.size() > 1) {
                        if (isSensor) {
                            for (TTask exTask: inputTasks) {
                                toRemove.add(exTask);
                                changeTaskToIntermediateMessageCatchEvent(exTask,process, collaboration, plane);
                            }
                        } else {
                            for (TTask exTask: inputTasks) {
                                toRemove.add(exTask);
                                changeTaskToIntermediateMessageThrowEvent(exTask,process, collaboration, plane);
                            }
                        }
                    } else {
                        changeTaskToIntermediateMessageCatchEvent(businessRuleTask, process, collaboration, plane);
                    }
                }

                if (isSingle2.length >= 1){
                    //create WhiteBox
                    singleActuators = singleActuators.substring(0,singleActuators.length()-3);
                    replaceDataAssociationWithWhiteBox(reference.getId(), singleActuators,process,collaboration,plane);

                    //remove original reference
                    removeFromProcess(reference,process);
                    List<TTask> inputTasks = getAssociatedTasks(reference.getId(),process);
                    if (inputTasks.size() > 1) {
                        if (isSensor) {
                            for (TTask exTask: inputTasks) {
                                toRemove.add(exTask);
                                changeTaskToIntermediateMessageCatchEvent(exTask,process, collaboration, plane);
                            }
                        } else {
                            for (TTask exTask: inputTasks) {
                                toRemove.add(exTask);
                                changeTaskToIntermediateMessageThrowEvent(exTask,process, collaboration, plane);
                            }
                        }
                    } else {
                        changeTaskToIntermediateMessageThrowEvent(serviceTask,process, collaboration, plane);
                    }
                }

                removeFromProcess(task,process);
                counter++;

            } else if (task.getDataOutputAssociation().size() > 1 && task.getDataInputAssociation().size() == 1) {

                TServiceTask serviceTask = replaceOneInputAndOneOutputAssociation(task,process,collaboration,plane).getValue();

                Pair<boolean[], String> pair2 = replaceSeveralOutputAssociation(serviceTask,reference,process,collaboration,plane);
                boolean[] isSingle = pair2.getKey();
                String singleActuators = pair2.getValue();

                if (isSingle.length >= 1){
                    //create WhiteBox
                    singleActuators = singleActuators.substring(0,singleActuators.length()-3);
                    replaceDataAssociationWithWhiteBox(reference.getId(), singleActuators,process,collaboration,plane);

                    //remove original reference
                    removeFromProcess(reference,process);
                    List<TTask> inputTasks = getAssociatedTasks(reference.getId(),process);
                    if (inputTasks.size() > 1) {
                        if (isSensor) {
                            for (TTask exTask: inputTasks) {
                                toRemove.add(exTask);
                                changeTaskToIntermediateMessageCatchEvent(exTask,process, collaboration, plane);
                            }
                        } else {
                            for (TTask exTask: inputTasks) {
                                toRemove.add(exTask);
                                changeTaskToIntermediateMessageThrowEvent(exTask,process, collaboration, plane);
                            }
                        }
                    } else {
                        changeTaskToIntermediateMessageThrowEvent(serviceTask,process, collaboration, plane);
                    }
                }

                removeFromProcess(task,process);
                counter++;

            } else if (task.getDataOutputAssociation().size() > 1 && task.getDataInputAssociation().size() == 0) {

                Pair<boolean[], String> pair = replaceSeveralOutputAssociation(task,reference,process,collaboration,plane);
                boolean[] isSingle = pair.getKey();
                String singleActuators = pair.getValue();

                if (isSingle.length >= 1){
                    //create WhiteBox
                    singleActuators = singleActuators.substring(0,singleActuators.length()-3);
                    replaceDataAssociationWithWhiteBox(reference.getId(), singleActuators,process,collaboration,plane);

                    //remove original reference
                    removeFromProcess(reference,process);
                    List<TTask> inputTasks = getAssociatedTasks(reference.getId(),process);
                    if (inputTasks.size() > 1) {
                        if (isSensor) {
                            for (TTask exTask: inputTasks) {
                                toRemove.add(exTask);
                                changeTaskToIntermediateMessageCatchEvent(exTask,process, collaboration, plane);
                            }
                        } else {
                            for (TTask exTask: inputTasks) {
                                toRemove.add(exTask);
                                changeTaskToIntermediateMessageThrowEvent(exTask,process, collaboration, plane);
                            }
                        }
                    } else {
                        changeTaskToIntermediateMessageThrowEvent(task,process, collaboration, plane);
                    }
                }

            } else if (task.getDataOutputAssociation().size() == 1 && task.getDataInputAssociation().size() == 0) {
                List<TTask> outputTasks = getAssociatedTasks(reference.getId(),process);
                if (outputTasks.size() <= 1) {
                    TServiceTask serviceTask = new TServiceTask();
                    serviceTask.setId(task.getId());
                    serviceTask.setName(task.getName());
                    serviceTask.getIncoming().addAll(task.getIncoming());
                    serviceTask.getOutgoing().addAll(task.getOutgoing());
                    serviceTask.getProperty().addAll(task.getProperty());

                    JAXBElement<TServiceTask> taskElement = objectFactory.createServiceTask(serviceTask);
                    removeFromProcess(task, process);
                    counter++;
                    removeFromProcess(reference, process);
                    process.getFlowElement().add(taskElement);
                } else {
                    //create WhiteBox
                    replaceDataAssociationWithWhiteBox(reference.getId(), "Sensor (IoT)" + reference.getName(),process,collaboration,plane);

                    List<TTask> tempTasks = new ArrayList<>();
                    tempTasks.add(outputTasks.get(1));
                    changeIoTDependantTask(tempTasks, reference, isSensor, process, collaboration, plane);
                    //remove original reference
                    removeFromProcess(reference,process);
                    for (TTask exTask: outputTasks) {
                        toRemove.add(exTask);
                        if (!exTask.getId().equals(outputTasks.get(1).getId())) {
                            changeTaskToIntermediateMessageThrowEvent(exTask, process, collaboration, plane);
                        }
                    }
                }
            }
        }
        return counter;
    }

    /**
     * Removes all messageFlows from the associated Task.
     */
    private void removeTaskFlows(TTask task, TCollaboration collaboration){
        for (TDataInputAssociation inputAssociation: task.getDataInputAssociation()){
            removeFromCollaborationMessageFlow(inputAssociation.getId(), task.getId(),collaboration);
        }
        for (TDataOutputAssociation outputAssociation: task.getDataOutputAssociation()){
            removeFromCollaborationMessageFlow(outputAssociation.getId(), task.getId(),collaboration);
        }
    }

    private Pair<TBusinessRuleTask, TServiceTask> replaceOneInputAndOneOutputAssociation(TTask task, TProcess process, TCollaboration collaboration, BPMNPlane plane){
        List<TTask> outputTasks = getAssociatedTasks(getIDOfObject(task.getDataOutputAssociation().get(0).getTargetRef()), process);
        List<TTask> inputTasks = getAssociatedTasks(getIDOfObject(task.getDataInputAssociation().get(0).getSourceRef()), process);

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
            TSequenceFlow incoming = getFlow(task.getIncoming().get(k).getLocalPart(),process);
            assert incoming != null;
            incoming.setTargetRef(leftGateWay);
        }
        for (int k = 0; k < task.getOutgoing().size(); k++) {
            TSequenceFlow outgoing = getFlow(task.getOutgoing().get(k).getLocalPart(),process);
            assert outgoing != null;
            outgoing.setSourceRef(rightGateWay);
        }

        //create business task
        businessRuleTask.setId(rule_Id);
        businessRuleTask.setName("(IoT) "+task.getName());
        businessRuleTask.getIncoming().add(QName.valueOf(leftFlow_2_Id));
        businessRuleTask.getOutgoing().add(QName.valueOf(rightFlow_2_Id));
        businessRuleTask.getDataInputAssociation().addAll(task.getDataInputAssociation());
        businessRuleTask.getProperty().addAll(task.getProperty());


        //create service task
        serviceTask.setId(service_Id);
        serviceTask.setName("(IoT) "+task.getName());
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
        process.getFlowElement().add(objectFactory.createBusinessRuleTask(businessRuleTask));
        process.getFlowElement().add(objectFactory.createServiceTask(serviceTask));
        process.getFlowElement().add(objectFactory.createParallelGateway(leftGateWay));
        process.getFlowElement().add(objectFactory.createParallelGateway(rightGateWay));
        process.getFlowElement().add(objectFactory.createSequenceFlow(leftFlow_1));
        process.getFlowElement().add(objectFactory.createSequenceFlow(leftFlow_2));
        process.getFlowElement().add(objectFactory.createSequenceFlow(rightFlow_1));
        process.getFlowElement().add(objectFactory.createSequenceFlow(rightFlow_2));

        BPMNShape taskShape = (BPMNShape) getShapeOrEdge(task.getId(),plane);

        org.omg.spec.bpmn._20100524.di.ObjectFactory planeObjectFactory = new org.omg.spec.bpmn._20100524.di.ObjectFactory();

        //create the shapes
        BPMNShape ruleShape = new BPMNShape();
        Bounds ruleBounds = new Bounds();
        ruleShape.setBounds(ruleBounds);
        ruleShape.setId(businessRuleTask.getId()+"_di");
        ruleShape.setBpmnElement(QName.valueOf(businessRuleTask.getId()));
        assert taskShape != null;
        ruleShape.getBounds().setX(taskShape.getBounds().getX());
        ruleShape.getBounds().setY(taskShape.getBounds().getY()-50);
        ruleShape.getBounds().setHeight(80);
        ruleShape.getBounds().setWidth(100);
        plane.getDiagramElement().add(planeObjectFactory.createBPMNShape(ruleShape));

        BPMNShape serviceShape = new BPMNShape();
        Bounds serviceBounds = new Bounds();
        serviceShape.setBounds(serviceBounds);
        serviceShape.setId(serviceTask.getId()+"_di");
        serviceShape.setBpmnElement(QName.valueOf(serviceTask.getId()));
        serviceShape.getBounds().setX(taskShape.getBounds().getX());
        serviceShape.getBounds().setY(taskShape.getBounds().getY() + 40);
        serviceShape.getBounds().setHeight(80);
        serviceShape.getBounds().setWidth(100);
        plane.getDiagramElement().add(planeObjectFactory.createBPMNShape(serviceShape));

        BPMNShape leftGateShape = new BPMNShape();
        Bounds leftGateBounds = new Bounds();
        leftGateShape.setBounds(leftGateBounds);
        leftGateShape.setId(leftGateWay.getId()+"_di");
        leftGateShape.setBpmnElement(QName.valueOf(leftGateWay.getId()));
        leftGateShape.getBounds().setX(taskShape.getBounds().getX()-50);
        leftGateShape.getBounds().setY(taskShape.getBounds().getY()+taskShape.getBounds().getHeight()/2-18);
        leftGateShape.getBounds().setHeight(36);
        leftGateShape.getBounds().setWidth(36);
        plane.getDiagramElement().add(planeObjectFactory.createBPMNShape(leftGateShape));

        BPMNShape rightGateShape = new BPMNShape();
        Bounds rightGateBounds = new Bounds();
        rightGateShape.setBounds(rightGateBounds);
        rightGateShape.setId(rightGateWay.getId()+"_di");
        rightGateShape.setBpmnElement(QName.valueOf(rightGateWay.getId()));
        rightGateShape.getBounds().setX(taskShape.getBounds().getX()+25+taskShape.getBounds().getWidth());
        rightGateShape.getBounds().setY(taskShape.getBounds().getY()+taskShape.getBounds().getHeight()/2-18);
        rightGateShape.getBounds().setHeight(36);
        rightGateShape.getBounds().setWidth(36);
        plane.getDiagramElement().add(planeObjectFactory.createBPMNShape(rightGateShape));


        //adjust the already existing edges
        for (int k = 0; k < task.getIncoming().size(); k++) {
            TSequenceFlow incoming = getFlow(task.getIncoming().get(k).getLocalPart(),process);
            assert incoming != null;
            BPMNEdge incomingEdge = (BPMNEdge) getShapeOrEdge(incoming.getId(),plane);
            assert incomingEdge != null;
            incomingEdge.getWaypoint().get(incomingEdge.getWaypoint().size()-1).setX(leftGateShape.getBounds().getX());
        }

        for (int k = 0; k < task.getOutgoing().size(); k++) {
            TSequenceFlow outgoing = getFlow(task.getOutgoing().get(k).getLocalPart(),process);
            assert outgoing != null;
            BPMNEdge outgoingEdge = (BPMNEdge) getShapeOrEdge(outgoing.getId(),plane);
            if (outgoingEdge != null) {
                outgoingEdge.getWaypoint().get(0).setX(rightGateShape.getBounds().getX() + rightGateShape.getBounds().getWidth());
            }
        }


        //create missing edges
        int[] leftFlow1X = new int[3];
        int[] leftFlow1Y = new int[3];
        leftFlow1X[0] = (int) (leftGateShape.getBounds().getX()+leftGateShape.getBounds().getWidth()/2);
        leftFlow1X[1] = (int) (leftGateShape.getBounds().getX()+leftGateShape.getBounds().getWidth()/2);
        leftFlow1X[2] = (int) (ruleShape.getBounds().getX());

        leftFlow1Y[0] = (int) (leftGateShape.getBounds().getY()+leftGateShape.getBounds().getHeight());
        leftFlow1Y[1] = (int) (ruleShape.getBounds().getY()+ruleShape.getBounds().getHeight()/2);
        leftFlow1Y[2] = (int) (ruleShape.getBounds().getY()+ruleShape.getBounds().getHeight()/2);

        plane.getDiagramElement().add(createEdge(leftFlow_1.getId(),leftFlow1X,leftFlow1Y));

        int[] leftFlow2X = new int[3];
        int[] leftFlow2Y = new int[3];
        leftFlow2X[0] = (int) (leftGateShape.getBounds().getX()+leftGateShape.getBounds().getWidth()/2);
        leftFlow2X[1] = (int) (leftGateShape.getBounds().getX()+leftGateShape.getBounds().getWidth()/2);
        leftFlow2X[2] = (int) (serviceShape.getBounds().getX());

        leftFlow2Y[0] = (int) (leftGateShape.getBounds().getY()+leftGateShape.getBounds().getHeight());
        leftFlow2Y[1] = (int) (serviceShape.getBounds().getY()+ruleShape.getBounds().getHeight()/2);
        leftFlow2Y[2] = (int) (serviceShape.getBounds().getY()+ruleShape.getBounds().getHeight()/2);

        plane.getDiagramElement().add(createEdge(leftFlow_2.getId(),leftFlow2X,leftFlow2Y));

        int[] rightFlow1X = new int[3];
        int[] rightFlow1Y = new int[3];
        rightFlow1X[0] = (int) (ruleShape.getBounds().getX()+ruleShape.getBounds().getWidth());
        rightFlow1X[1] = (int) (rightGateShape.getBounds().getX()+rightGateShape.getBounds().getWidth()/2);
        rightFlow1X[2] = (int) (rightGateShape.getBounds().getX()+rightGateShape.getBounds().getWidth()/2);

        rightFlow1Y[0] = (int) (ruleShape.getBounds().getY()+ruleShape.getBounds().getHeight()/2);
        rightFlow1Y[1] = (int) (ruleShape.getBounds().getY()+ruleShape.getBounds().getHeight()/2);
        rightFlow1Y[2] = (int) (rightGateShape.getBounds().getY());

        plane.getDiagramElement().add(createEdge(rightFlow_1.getId(),rightFlow1X,rightFlow1Y));

        int[] rightFlow2X = new int[3];
        int[] rightFlow2Y = new int[3];
        rightFlow2X[0] = (int) (serviceShape.getBounds().getX()+serviceShape.getBounds().getWidth());
        rightFlow2X[1] = (int) (rightGateShape.getBounds().getX()+rightGateShape.getBounds().getWidth()/2);
        rightFlow2X[2] = (int) (rightGateShape.getBounds().getX()+rightGateShape.getBounds().getWidth()/2);

        rightFlow2Y[0] = (int) (serviceShape.getBounds().getY()+serviceShape.getBounds().getHeight()/2);
        rightFlow2Y[1] = (int) (serviceShape.getBounds().getY()+serviceShape.getBounds().getHeight()/2);
        rightFlow2Y[2] = (int) (rightGateShape.getBounds().getY()+rightGateShape.getBounds().getHeight());

        plane.getDiagramElement().add(createEdge(rightFlow_2.getId(),rightFlow2X,rightFlow2Y));

        //remove all unneeded references and objects
        List<TDataObjectReference> references = getReferences(getIDOfObject(task.getDataInputAssociation().get(0).getSourceRef().get(0).getValue()),process);
        for (TDataObjectReference data: references) {
            removeFromProcess(data,process);
        }
        List<TDataObjectReference> references2 = getReferences(getIDOfObject(task.getDataOutputAssociation().get(0).getTargetRef()),process);
        for (TDataObjectReference data: references2) {
            removeFromProcess(data,process);
        }
        if (inputTasks.size() > 1) {
            //create WhiteBox
            replaceDataAssociationWithWhiteBox(getIDOfObject(task.getDataInputAssociation().get(0).getSourceRef()), "Sensor (IoT)" + task.getDataInputAssociation().get(0).getSourceRef().get(0).getName(),process, collaboration, plane);
            //replace businessRuleTask to catch event
            changeTaskToIntermediateMessageCatchEvent(businessRuleTask,process, collaboration, plane);

            //remove original reference
            removeFromProcess(task.getDataInputAssociation().get(0), process);

        }
        if (outputTasks.size() > 1) {
            //create WhiteBox
            replaceDataAssociationWithWhiteBox(getIDOfObject(task.getDataOutputAssociation().get(0).getTargetRef()), "Actuator (IoT)" + getIDOfObject(task.getDataOutputAssociation().get(0).getTargetRef()),process, collaboration, plane);
            //replace serviceTask to throw event
            changeTaskToIntermediateMessageThrowEvent(businessRuleTask,process, collaboration, plane);

            //remove original reference
            removeFromProcess(task.getDataOutputAssociation().get(0), process);

        }
        return new Pair<>(businessRuleTask,serviceTask);
    }

    private Pair<boolean[],String> replaceSeveralInputAssociation(TTask task, TDataObjectReference reference, TProcess process, TCollaboration collaboration, BPMNPlane plane){
        //check if the data input has only one association
        boolean[] isSingle = new boolean[task.getDataInputAssociation().size()];
        StringBuilder singleSensors = new StringBuilder("(IoT) Sensors: ");
        for (int k = 0; k < task.getDataInputAssociation().size(); k++){
            List<TDataObjectReference> references3 = getReferences(getIDOfObject(task.getDataInputAssociation().get(k).getSourceRef().get(0).getValue()),process);
            for (TDataObjectReference referenceItem: references3) {
                List<TTask> referenceTasks = getAssociatedTasks(referenceItem.getId(),process);
                if (referenceTasks.size() <= 1){
                    isSingle[k] = true;
                    singleSensors.append(referenceItem.getName()).append(" & ");
                    boolean flowCreated = false;
                    for (TTask taskItem: referenceTasks) {
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
                                removeFromProcess(referenceItem,process);
                                break;
                            }
                        }
                    }
                } else {
                    String name = null;
                    for (TTask taskItem: referenceTasks) {
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

                                removeFromProcess(referenceItem, process);
                                changeTaskToIntermediateMessageCatchEvent(taskItem,process, collaboration, plane);
                                break;
                            }
                        }
                    }
                    replaceDataAssociationWithWhiteBox(referenceItem.getId(), "(IoT) Sensor: " + name,process,collaboration,plane);
                }
            }
        }
        return new Pair<>(isSingle, singleSensors.toString());
    }

    private Pair<boolean[],String> replaceSeveralOutputAssociation(TTask task, TDataObjectReference reference, TProcess process, TCollaboration collaboration, BPMNPlane plane){
        //check if the data input has only one association
        boolean[] isSingle = new boolean[task.getDataOutputAssociation().size()];
        StringBuilder singleActuators = new StringBuilder("(IoT) Actuators: ");
        for (int k = 0; k < task.getDataOutputAssociation().size(); k++){
            List<TDataObjectReference> references = getReferences(getIDOfObject(task.getDataOutputAssociation().get(k).getTargetRef()),process);
            for (TDataObjectReference referenceItem: references) {
                List<TTask> referenceTasks = getAssociatedTasks(referenceItem.getId(),process);
                if (referenceTasks.size() <= 1){
                    isSingle[k] = true;
                    singleActuators.append(referenceItem.getName()).append(" & ");
                    boolean flowCreated = false;
                    for (TTask taskItem: referenceTasks) {
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
                                removeFromProcess(referenceItem,process);
                                break;
                            }
                        }
                    }
                } else {
                    String name = null;
                    for (TTask taskItem: referenceTasks) {
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

                                removeFromProcess(referenceItem, process);
                                changeTaskToIntermediateMessageThrowEvent(taskItem,process, collaboration, plane);
                                break;
                            }
                        }
                    }
                    replaceDataAssociationWithWhiteBox(referenceItem.getId(), "(IoT) Sensor: " + name,process,collaboration,plane);
                }
            }
        }
        return new Pair<>(isSingle, singleActuators.toString());
    }

    /**
     * Changes a DataInputAssociation sequence flow into a message flow
     */
    private void changeSequenceFlowToMessageFlow(TTask task, TDataInputAssociation flow, TCollaboration collaboration){
        //Create MessageFlow
        TMessageFlow messageFlow = new TMessageFlow();
        messageFlow.setId(flow.getId());
        messageFlow.setSourceRef(QName.valueOf(getIDOfObject(flow.getSourceRef().get(0).getValue())));
        messageFlow.setTargetRef(QName.valueOf(task.getId()));
        //Add to list
        collaboration.getMessageFlow().add(messageFlow);
    }

    /**
     * Changes a DataOutputAssociation sequence flow into a message flow
     */
    private void changeSequenceFlowToMessageFlow(TTask task, TDataOutputAssociation flow, TCollaboration collaboration){
        //Create MessageFlow
        TMessageFlow messageFlow = new TMessageFlow();
        messageFlow.setId(flow.getId());
        messageFlow.setSourceRef(QName.valueOf(task.getId()));
        messageFlow.setTargetRef(QName.valueOf(getIDOfObject(flow.getTargetRef())));
        //Add to list
        collaboration.getMessageFlow().add(messageFlow);
    }

    private void changeTaskToIntermediateMessageCatchEvent(TTask task, TProcess process, TCollaboration collaboration, BPMNPlane plane){
        //create message catch event
        TIntermediateCatchEvent catchEvent = new TIntermediateCatchEvent();
        catchEvent.setId(task.getId());
        catchEvent.setName(task.getName());
        catchEvent.getDataOutputAssociation().addAll(task.getDataOutputAssociation());
        TMessageEventDefinition messageEventDefinition = new TMessageEventDefinition();
        messageEventDefinition.setId("MessageEventDefinition_"+randomNumberSequence());
        catchEvent.getEventDefinition().add(objectFactory.createMessageEventDefinition(messageEventDefinition));
        removeFromProcess(task,process);
        //add to list and reshape
        process.getFlowElement().add(objectFactory.createIntermediateCatchEvent(catchEvent));
        BPMNShape shape = (BPMNShape) getShapeOrEdge(catchEvent.getId(),plane);
        assert shape != null;
        changeShapeAttributes(catchEvent.getId(),36,36, (int) ((shape.getBounds().getX()+shape.getBounds().getWidth()/2)-18), (int) ((shape.getBounds().getY()+shape.getBounds().getHeight()/2)-18),false,plane);
        for (TDataOutputAssociation outputAssociation: task.getDataOutputAssociation()){
            changeSequenceFlowToMessageFlow(task,outputAssociation, collaboration);
        }
        for (TDataInputAssociation inputAssociation: task.getDataInputAssociation()){
            changeSequenceFlowToMessageFlow(task,inputAssociation, collaboration);
        }
        for (int i = 0; i < task.getOutgoing().size(); i++) {
            BPMNEdge edge = (BPMNEdge) getShapeOrEdge(task.getOutgoing().get(i).getLocalPart(), plane);
            if (edge != null) {
                edge.getWaypoint().get(0).setX(((shape.getBounds().getX() + shape.getBounds().getWidth() / 2) + 18));
                edge.getWaypoint().get(0).setY(shape.getBounds().getY()+shape.getBounds().getHeight()/2);
            }
        }
    }

    private void changeTaskToIntermediateMessageThrowEvent(TTask task, TProcess process, TCollaboration collaboration, BPMNPlane plane){
        //create message catch event
        TIntermediateThrowEvent throwEvent = new TIntermediateThrowEvent();
        throwEvent.setId(task.getId());
        throwEvent.setName(task.getName());
        throwEvent.getDataInputAssociation().addAll(task.getDataInputAssociation());
        TMessageEventDefinition messageEventDefinition = new TMessageEventDefinition();
        messageEventDefinition.setId("MessageEventDefinition_"+randomNumberSequence());
        throwEvent.getEventDefinition().add(objectFactory.createMessageEventDefinition(messageEventDefinition));
        removeFromProcess(task,process);
        //add to list and reshape
        process.getFlowElement().add(objectFactory.createIntermediateThrowEvent(throwEvent));
        BPMNShape shape = (BPMNShape) getShapeOrEdge(throwEvent.getId(),plane);
        assert shape != null;
        changeShapeAttributes(throwEvent.getId(),36,36, (int) ((shape.getBounds().getX()+shape.getBounds().getWidth()/2)-18), (int) ((shape.getBounds().getY()+shape.getBounds().getHeight()/2)-18),false,plane);
        for (TDataOutputAssociation outputAssociation: task.getDataOutputAssociation()){
            changeSequenceFlowToMessageFlow(task,outputAssociation, collaboration);
        }
        for (TDataInputAssociation inputAssociation: task.getDataInputAssociation()){
            changeSequenceFlowToMessageFlow(task,inputAssociation, collaboration);
        }
        for (int i = 0; i < task.getOutgoing().size(); i++) {
            BPMNEdge edge = (BPMNEdge) getShapeOrEdge(task.getOutgoing().get(i).getLocalPart(), plane);
            if (edge != null) {
                edge.getWaypoint().get(0).setX(((shape.getBounds().getX() + shape.getBounds().getWidth() / 2) + 18));
                edge.getWaypoint().get(0).setY(shape.getBounds().getY()+shape.getBounds().getHeight()/2);
            }
        }
    }

    /**
     * Replaces a data association with a WhiteBox.
     * @param dataId ID of the WhiteBox.
     * @param name Name of the WhiteBox.
     */
    private void replaceDataAssociationWithWhiteBox(String dataId, String name, TProcess process, TCollaboration collaboration, BPMNPlane plane){
        if (!maxSet) {
            this.maxY = getMaxY(plane) + 100;
            this.minY = getMinY(plane) - 100;
            this.maxSet = false;
        }
        //in case there is no collaboration, create one
        if (collaboration.getParticipant().size() == 0){
            TParticipant participant = new TParticipant();
            participant.setId("Participant_"+randomNumberSequence());
            participant.setName("Process 1");
            participant.setProcessRef(QName.valueOf(process.getId()));
            collaboration.getParticipant().add(participant);
            String id = "Collaboration_"+randomNumberSequence();
            collaboration.setId(id);
            plane.setBpmnElement(QName.valueOf(id));
            plane.getDiagramElement().add(createHorizontalShape(participant.getId(),getMinX(plane)-100,minY,getMaxX(plane)+100,maxY));
        }

        //add white box to collaboration
        TParticipant whiteBox = new TParticipant();
        whiteBox.setId(dataId);
        whiteBox.setName(name);
        collaboration.getParticipant().add(whiteBox);

        //reposition shape
        BPMNShape shape = (BPMNShape) getShapeOrEdge(dataId,plane);
        assert shape != null;
        int difference1 = (int) (shape.getBounds().getY()-minY);
        int difference2 = (int) (maxY - shape.getBounds().getY());
        if (difference1 < difference2) {
            changeShapeAttributes(dataId,200,50, (int) (shape.getBounds().getX()-shape.getBounds().getWidth()/2-25),minY-100,false,plane);
        } else {
            changeShapeAttributes(dataId,200,50, (int) (shape.getBounds().getX()-shape.getBounds().getWidth()/2-25),maxY+100,false,plane);
        }
    }

    private List<TDataObjectReference> getReferences(String id, TProcess process){
        List<TDataObjectReference> references = new ArrayList<>();
        for (int i = 0; i < process.getFlowElement().size(); i++) {
            if (process.getFlowElement().get(i).getValue() instanceof TDataObjectReference reference) {
                if (reference.getId().equals(id)){
                    references.add(reference);
                }
            }
        }
        return references;
    }

    /**
     * Removes the given element from the process.
     */
    private void removeFromProcess(TBaseElement element, TProcess process){
        for (int i = 0; i < process.getFlowElement().size(); i++) {
            if ((process.getFlowElement().get(i).getValue()).getId().equals(element.getId())){
                process.getFlowElement().remove(i);
            }
        }
    }

    /**
     * Removes the given element from the collaboration.
     */
    private void removeFromCollaborationMessageFlow(String id, String associatedTaskId, TCollaboration collaboration){
        for (int i = 0; i < collaboration.getMessageFlow().size(); i++) {
            if (collaboration.getMessageFlow().get(i).getId().equals(id) && collaboration.getMessageFlow().get(i).getSourceRef().getLocalPart().equals(associatedTaskId)){
                collaboration.getMessageFlow().remove(i);
            } else if (collaboration.getMessageFlow().get(i).getId().equals(id) && collaboration.getMessageFlow().get(i).getTargetRef().getLocalPart().equals(associatedTaskId)){
                collaboration.getMessageFlow().remove(i);
            }
        }
    }

    /**
     * Removes the given element from the plane.
     */
    private void removeFromPlane(String id, BPMNPlane plane){
        for (int i = 0; i < plane.getDiagramElement().size(); i++) {
            if ((plane.getDiagramElement().get(i).getValue()).getId().equals(id)){
                plane.getDiagramElement().remove(i);
            }
        }
    }

    /**
     * Gets the task that is associated with the dataObjectReference.
     * @param id The id of the dataObjectReference aka. DataInput/OutputAssociation
     * @return The task.
     */
    private List<TTask> getAssociatedTasks(String id, TProcess process){
        List<TTask> tasks = new ArrayList<>();
        for (int i = 0; i < process.getFlowElement().size(); i++) {
            if (process.getFlowElement().get(i).getValue() instanceof TTask task) {
                if (task.getDataInputAssociation().size() > 0){
                    for (TDataInputAssociation input:task.getDataInputAssociation()){
                        for (int k = 0; k<input.getSourceRef().size(); k++) {
                            if (getIDOfObject(input.getSourceRef().get(k).getValue()).equals(id)) {
                                tasks.add(task);
                            }
                        }
                    }
                } else if (task.getDataOutputAssociation().size() > 0){
                    for (TDataOutputAssociation output:task.getDataOutputAssociation()){
                        if (getIDOfObject(output.getTargetRef()).equals(id)){
                            tasks.add(task);
                        }
                    }
                }
            }
        }
        return tasks;
    }

    private TSequenceFlow getFlow (String id, TProcess process){
        //go through all elements in the process
        for (int i = 0; i < process.getFlowElement().size(); i++) {
            //check if the element is a sequence flow
            if (process.getFlowElement().get(i).getValue() instanceof TBaseElement) {
                TBaseElement baseElement = process.getFlowElement().get(i).getValue();
                //check if the source of the flow is the current element
                if (baseElement.getId().equals(id)){
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
    private TBaseElement getBaseElement(String id, TProcess process){
        //go through all elements in the process
        for (int i = 0; i < process.getFlowElement().size(); i++) {
            //check if the element is a sequence flow
            if (process.getFlowElement().get(i).getValue() instanceof TBaseElement) {
                TBaseElement baseElement = process.getFlowElement().get(i).getValue();
                //check if the source of the flow is the current element
                if (baseElement.getId().equals(id)){
                    //add the target of the flow to the list
                    return baseElement;
                }
            }
        }
        return null;
    }

    /**
     * Replaces the old id with the new id in every mention.
     * @param oldID The old id.
     * @param newObject The new object that hold the new id.
     * @param id The id which needs to be inserted.
     * @param process The {@link  TProcess} which holds all bpmn elements.
     * @param plane The {@link BPMNPlane} which holds all diagram elements.
     */
    private void replaceAllIDsMentions (String oldID, Object newObject, String id, TProcess process, BPMNPlane plane){
        //go through all elements in the process
        for (int i = 0; i < process.getFlowElement().size(); i++) {
            //check if the element is a sequence flow
            if (process.getFlowElement().get(i).getValue() instanceof TSequenceFlow sequenceFlow) {
                //check if the source or target ahs the old id, if so replace it
                if (Objects.equals(getIDOfObject(sequenceFlow.getSourceRef()), oldID)){
                    sequenceFlow.setSourceRef(newObject);
                }
                if (Objects.equals(getIDOfObject(sequenceFlow.getTargetRef()), oldID)){
                    sequenceFlow.setTargetRef(newObject);
                }
            }
            //do the same for other nodes with outgoing and incoming types
            else if (process.getFlowElement().get(i).getValue() instanceof TFlowNode flowNode){
                for (QName incoming: flowNode.getIncoming()){
                    if (incoming.getLocalPart().equals(oldID)) {
                        flowNode.getIncoming().remove(incoming);
                        flowNode.getIncoming().add(QName.valueOf(id));
                    }
                }
                for (QName outGoing: flowNode.getOutgoing()){
                    if (outGoing.getLocalPart().equals(oldID)) {
                        flowNode.getOutgoing().remove(outGoing);
                        flowNode.getOutgoing().add(QName.valueOf(id));
                    }
                }
            }
        }
        //check the plane elements for the old id
        for (int i = 0; i<plane.getDiagramElement().size(); i++) {
            if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNShape.class) {
                BPMNShape shape = (BPMNShape) plane.getDiagramElement().get(i).getValue();
                if (oldID.equals(shape.getBpmnElement().getLocalPart())) {
                    shape.setId(id+"_di");
                    shape.setBpmnElement(QName.valueOf(id));
                }
            }
            else if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNEdge.class) {
                BPMNEdge edge = (BPMNEdge) plane.getDiagramElement().get(i).getValue();
                if (oldID.equals(edge.getBpmnElement().getLocalPart())) {
                    edge.setId(id+"_di");
                    edge.setBpmnElement(QName.valueOf(id));
                }
            }
        }
    }

    /**
     * Gets the id of any element in the process tree.
     */
    private String getIDOfObject(Object object){
        String id = "";
        if (object instanceof TActivity)
            return ((TActivity)object).getId();
        if (object instanceof TArtifact)
            return ((TArtifact)object).getId();
        if (object instanceof TEvent)
            return ((TEvent)object).getId();
        if (object instanceof TBaseElement)
            return ((TBaseElement)object).getId();
        if (object instanceof TBaseElementWithMixedContent)
            return ((TBaseElementWithMixedContent)object).getId();
        return id;
    }

    /**
     * Changes the width and height of a shape.
     */
    private void changeShapeSize(String id, int width, int height, BPMNPlane plane){
        for (int i = 0; i<plane.getDiagramElement().size(); i++) {
            if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNShape.class) {
                BPMNShape shape = (BPMNShape) plane.getDiagramElement().get(i).getValue();
                if (id.equals(shape.getBpmnElement().getLocalPart())) {
                    List<BPMNEdge> edges = getOutgoingEdgesByX((int) (shape.getBounds().getX()+shape.getBounds().getWidth()), shape, plane);
                    for (BPMNEdge edge:edges){
                        for (int k = 0; k<edge.getWaypoint().size(); k++) {
                            if (edge.getWaypoint().get(k).getX() == (shape.getBounds().getX()+shape.getBounds().getWidth()))
                            {
                                edge.getWaypoint().get(k).setX((shape.getBounds().getX()+width));
                            }
                        }
                    }
                    shape.getBounds().setY(shape.getBounds().getY()-shape.getBounds().getWidth()/2);
                    shape.getBounds().setWidth(width);
                    shape.getBounds().setHeight(height);
                }
            }
        }
    }

    /**
     * Changes the attributes of a shape.
     */
    private void changeShapeAttributes(String id, int width, int height, int x, int y, boolean isHorizontal, BPMNPlane plane){
        for (int i = 0; i<plane.getDiagramElement().size(); i++) {
            if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNShape.class) {
                BPMNShape shape = (BPMNShape) plane.getDiagramElement().get(i).getValue();
                if (id.equals(shape.getBpmnElement().getLocalPart())) {
                    //check all outgoings
                    List<BPMNEdge> edges = getOutgoingEdgesByX((int) (shape.getBounds().getX()+shape.getBounds().getHeight()), shape, plane);
                    for (BPMNEdge edge:edges){
                        for (int k = 0; k<edge.getWaypoint().size(); k++) {
                            if (edge.getWaypoint().get(k).getX() == (shape.getBounds().getX()+shape.getBounds().getHeight()) && Math.abs(edge.getWaypoint().get(k).getY()-shape.getBounds().getY()) < shape.getBounds().getWidth())
                            {
                                edge.getWaypoint().get(k).setX(x+width);
                                edge.getWaypoint().get(k).setY(y+height);
                            }
                        }
                    }
                    List<BPMNEdge> edges3 = getOutgoingEdgesByX((int) shape.getBounds().getX(), shape, plane);
                    for (BPMNEdge edge:edges3){
                        for (int k = 0; k<edge.getWaypoint().size(); k++) {
                            if (edge.getWaypoint().get(k).getX() == shape.getBounds().getX() && Math.abs(edge.getWaypoint().get(k).getY()-shape.getBounds().getY()) < shape.getBounds().getWidth())
                            {
                                edge.getWaypoint().get(k).setX(x);
                                edge.getWaypoint().get(k).setY(y);
                            }
                        }
                    }
                    List<BPMNEdge> edges2 = getOutgoingEdgesByY((int) shape.getBounds().getY(), shape, plane);
                    for (BPMNEdge edge:edges2){
                        for (int k = 0; k<edge.getWaypoint().size(); k++) {
                            if (edge.getWaypoint().get(k).getY() == shape.getBounds().getY())
                            {
                                edge.getWaypoint().get(k).setY(y);
                            }
                        }
                    }
                    //for some reason width is height and height is width
                    List<BPMNEdge> edges4 = getOutgoingEdgesByY((int) (shape.getBounds().getY()+shape.getBounds().getWidth()), shape, plane);
                    for (BPMNEdge edge:edges4){
                        for (int k = 0; k<edge.getWaypoint().size(); k++) {
                            if (edge.getWaypoint().get(k).getY() == (shape.getBounds().getY()+shape.getBounds().getWidth()))
                            {
                                edge.getWaypoint().get(k).setY(y + height);
                            }
                        }
                    }
                    //check all incoming edges
                    List<BPMNEdge> incoming_edges = getIncomingEdgesByX((int) (shape.getBounds().getX()+shape.getBounds().getHeight()), plane);
                    for (BPMNEdge edge:incoming_edges){
                         for (int k = 0; k<edge.getWaypoint().size(); k++) {
                             if (edge.getWaypoint().get(k).getX() == (shape.getBounds().getX()+shape.getBounds().getWidth()))
                            {
                                edge.getWaypoint().get(k).setX(x+width);
                            }
                        }
                    }
                    List<BPMNEdge> incoming_edges2 = getIncomingEdgesByY((int) shape.getBounds().getY(), shape, plane);
                    for (BPMNEdge edge:incoming_edges2){
                        for (int k = 0; k<edge.getWaypoint().size(); k++) {
                            if (edge.getWaypoint().get(k).getY() == shape.getBounds().getY())
                            {
                                edge.getWaypoint().get(k).setY(y);
                            }
                        }
                    }
                    List<BPMNEdge> incoming_edges3 = getIncomingEdgesByX((int) shape.getBounds().getX(), plane);
                    for (BPMNEdge edge:incoming_edges3){
                        for (int k = 0; k<edge.getWaypoint().size(); k++) {
                            if (edge.getWaypoint().get(k).getX() == shape.getBounds().getX())
                            {
                                edge.getWaypoint().get(k).setX(x);
                            }
                        }
                    }
                    List<BPMNEdge> incoming_edges4 = getIncomingEdgesByY((int) (shape.getBounds().getY()+shape.getBounds().getWidth()), shape, plane);
                    for (BPMNEdge edge:incoming_edges4){
                        for (int k = 0; k<edge.getWaypoint().size(); k++) {
                             if (edge.getWaypoint().get(k).getY() == (shape.getBounds().getY()+shape.getBounds().getWidth()))
                            {
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
    private List<BPMNEdge> getOutgoingEdgesByX(int x, BPMNShape shape, BPMNPlane plane){
        List<BPMNEdge> elements = new ArrayList<>();
        for (int i = 0; i<plane.getDiagramElement().size(); i++) {
            if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNEdge.class) {
                BPMNEdge edge = (BPMNEdge) plane.getDiagramElement().get(i).getValue();
                if (inLine(new Point(shape.getBounds().getX(), shape.getBounds().getY()), new Point(shape.getBounds().getX()+shape.getBounds().getHeight(), shape.getBounds().getY()), new Point(x,edge.getWaypoint().get(0).getY()))) {
                    elements.add(edge);
                }
            }
        }
        return elements;
    }

    private List<BPMNEdge> getIncomingEdgesByX(int x, BPMNPlane plane){
        List<BPMNEdge> elements = new ArrayList<>();
        for (int i = 0; i<plane.getDiagramElement().size(); i++) {
            if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNEdge.class) {
                BPMNEdge edge = (BPMNEdge) plane.getDiagramElement().get(i).getValue();
                if (Math.abs(x-edge.getWaypoint().get(edge.getWaypoint().size()-1).getX()) <= 10) {
                    elements.add(edge);
                }
            }
        }
        return elements;
    }

    private List<BPMNEdge> getOutgoingEdgesByY(int y, BPMNShape shape, BPMNPlane plane){
        List<BPMNEdge> elements = new ArrayList<>();
        for (int i = 0; i<plane.getDiagramElement().size(); i++) {
            if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNEdge.class) {
                BPMNEdge edge = (BPMNEdge) plane.getDiagramElement().get(i).getValue();
                if (inLine(new Point(shape.getBounds().getX(), shape.getBounds().getY()), new Point(shape.getBounds().getX(), shape.getBounds().getY()+shape.getBounds().getWidth()), new Point(edge.getWaypoint().get(0).getX(),y))) {
                    elements.add(edge);
                }
            }
        }
        return elements;
    }

    private List<BPMNEdge> getIncomingEdgesByY(int y, BPMNShape shape, BPMNPlane plane){
        List<BPMNEdge> elements = new ArrayList<>();
        for (int i = 0; i<plane.getDiagramElement().size(); i++) {
            if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNEdge.class) {
                BPMNEdge edge = (BPMNEdge) plane.getDiagramElement().get(i).getValue();
                if (inLine(new Point(shape.getBounds().getX(), shape.getBounds().getY()), new Point(shape.getBounds().getX(), shape.getBounds().getY()+shape.getBounds().getWidth()), new Point(edge.getWaypoint().get(edge.getWaypoint().size()-1).getX(),y))) {
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
     *     [0] = x
     * </p>
     * <p>
     *     [1] = y
     * </p>
     * <p>
     *     [2] = x2(Edge)/width(Shape)
     * </p>
     * <p>
     *     [3] = y2(Edge)/height(Shape)
     * </p>
     * @param id ID of the shape or edge.
     * @param plane {@link BPMNPlane} containing all {@link DiagramElement} .
     * @return An array with the length of 4.
     */
    private double[] getShapeOrEdgeAttributes(String id, BPMNPlane plane){
        double[] attributes = new double[4];
        for (int i = 0; i<plane.getDiagramElement().size(); i++){
            if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNEdge.class){
                BPMNEdge edge = (BPMNEdge)plane.getDiagramElement().get(i).getValue();
                if (id.equals(edge.getBpmnElement().getLocalPart())){
                    attributes[0] = edge.getWaypoint().get(0).getX();
                    attributes[1] = edge.getWaypoint().get(0).getY();
                    attributes[2] = edge.getWaypoint().get(edge.getWaypoint().size()-1).getX();
                    attributes[3] = edge.getWaypoint().get(edge.getWaypoint().size()-1).getY();
                    return attributes;
                }
            } else if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNShape.class){
                BPMNShape shape = (BPMNShape)plane.getDiagramElement().get(i).getValue();
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
    private int getMaxX(BPMNPlane plane){
        int currentX = 0;
        for (int i = 0; i<plane.getDiagramElement().size(); i++){
            if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNEdge.class){
                BPMNEdge edge = (BPMNEdge)plane.getDiagramElement().get(i).getValue();
                int x = (int) Math.max(edge.getWaypoint().get(edge.getWaypoint().size()-1).getX(),edge.getWaypoint().get(0).getX());
                if (x > currentX){
                    currentX = x;
                }
            } else if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNShape.class){
                BPMNShape shape = (BPMNShape)plane.getDiagramElement().get(i).getValue();
                int x = (int) ((int) shape.getBounds().getX() + shape.getBounds().getWidth());
                if (x > currentX){
                    currentX = x;
                }
            }
        }
        return currentX;
    }

    /**
     * Returns the lowest X coordinate in the diagram.
     */
    private int getMinX(BPMNPlane plane){
        int currentX = 100000;
        for (int i = 0; i<plane.getDiagramElement().size(); i++){
            if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNEdge.class){
                BPMNEdge edge = (BPMNEdge)plane.getDiagramElement().get(i).getValue();
                int x = (int) Math.min(edge.getWaypoint().get(edge.getWaypoint().size()-1).getX(),edge.getWaypoint().get(0).getX());
                if (x < currentX){
                    currentX = x;
                }
            } else if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNShape.class){
                BPMNShape shape = (BPMNShape)plane.getDiagramElement().get(i).getValue();
                int x = (int) shape.getBounds().getX();
                if (x < currentX){
                    currentX = x;
                }
            }
        }
        return currentX;
    }

    /**
     * Returns the highest Y coordinate in the diagram.
     */
    private int getMaxY(BPMNPlane plane){
        int currentY = 0;
        for (int i = 0; i<plane.getDiagramElement().size(); i++){
            if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNEdge.class){
                BPMNEdge edge = (BPMNEdge)plane.getDiagramElement().get(i).getValue();
                int y = (int) Math.max(edge.getWaypoint().get(edge.getWaypoint().size()-1).getY(),edge.getWaypoint().get(0).getY());
                if (y > currentY){
                    currentY = y;
                }
            } else if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNShape.class){
                BPMNShape shape = (BPMNShape)plane.getDiagramElement().get(i).getValue();
                int y = (int) ((int) shape.getBounds().getY() + shape.getBounds().getHeight());
                if (y > currentY){
                    currentY = y;
                }
            }
        }
        return currentY;
    }

    /**
     * Returns the lowest Y coordinate in the diagram.
     */
    private int getMinY(BPMNPlane plane){
        int currentY = 0;
        for (int i = 0; i<plane.getDiagramElement().size(); i++){
            if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNEdge.class){
                BPMNEdge edge = (BPMNEdge)plane.getDiagramElement().get(i).getValue();
                int y = (int) Math.min(edge.getWaypoint().get(edge.getWaypoint().size()-1).getY(),edge.getWaypoint().get(0).getY());
                if (y < currentY){
                    currentY = y;
                }
            } else if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNShape.class){
                BPMNShape shape = (BPMNShape)plane.getDiagramElement().get(i).getValue();
                int y = (int) shape.getBounds().getY();
                if (y < currentY){
                    currentY = y;
                }
            }
        }
        return currentY;
    }

    /**
     * Gets the Shape or Edge from the plane with the corresponding id.
     * @param id ID of the needed shape or edge.
     * @param plane The plane containing all shapes and edges.
     * @return A {@link DiagramElement} which can either be a {@link BPMNShape} or {@link BPMNEdge}
     */
    private DiagramElement getShapeOrEdge(String id, BPMNPlane plane){
        for (int i = 0; i<plane.getDiagramElement().size(); i++){
            if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNEdge.class){
                BPMNEdge edge = (BPMNEdge)plane.getDiagramElement().get(i).getValue();
                if (id.equals(edge.getBpmnElement().getLocalPart())){
                    return edge;
                }
            } else if (plane.getDiagramElement().get(i).getValue().getClass() == BPMNShape.class){
                BPMNShape shape = (BPMNShape)plane.getDiagramElement().get(i).getValue();
                if (id.equals(shape.getBpmnElement().getLocalPart())) {
                    return shape;
                }
            }
        }
        return null;
    }

    /**
     * Creates an edge.
     * @param id The id of the referenced bpmn element.
     * @param x An array with the x coordinates of all waypoints.
     * @param y An array with the y coordinates of all waypoints.
     * @return A JAXBElement that can be added to the list.
     */
    private JAXBElement<BPMNEdge> createEdge(String id, int[] x, int[] y){
        org.omg.spec.bpmn._20100524.di.ObjectFactory objectFactory = new org.omg.spec.bpmn._20100524.di.ObjectFactory();
        BPMNEdge bpmnEdge = new BPMNEdge();
        bpmnEdge.setBpmnElement(new QName(id));
        bpmnEdge.setId(id + "_di");
        for (int i = 0; i<x.length; i++){
            Point waypoint = new Point(x[i],y[i]);
            bpmnEdge.getWaypoint().add(waypoint);
        }
        return objectFactory.createBPMNEdge(bpmnEdge);
    }


    /**
     * Creates a shape WITHOUT a label.
     * @param bpmnElement The id of the referenced bpmn element.
     * @param x x coordinate of the element.
     * @param y y coordinate of the element.
     * @param width Width of the shape.
     * @param height height of the shape.
     * @return A JAXBElement that can be added to the list.
     */
    private JAXBElement<BPMNShape> createShape(String bpmnElement, int x, int y, int width, int height){
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
     * @param bpmnElement The id of the referenced bpmn element.
     * @param x x coordinate of the element.
     * @param y y coordinate of the element.
     * @param width Width of the shape.
     * @param height height of the shape.
     * @return A JAXBElement that can be added to the list.
     */
    private JAXBElement<BPMNShape> createHorizontalShape(String bpmnElement, int x, int y, int width, int height){
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
     * @param bpmnElement The id of the referenced bpmn element.
     * @param shapeBounds An int array with the length of 4, containing [0] = x, [1] = y, [2] = width, [3] = height.
     * @param labelBounds An int array with the length of 4, containing [0] = x, [1] = y, [2] = width, [3] = height.
     * @return A JAXBElement that can be added to the list.
     */
    private JAXBElement<BPMNShape> createShapeAndLabel(String bpmnElement, int[] shapeBounds, int[] labelBounds){
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
     * @return A String with the length of 7.
     */
    private String randomNumberSequence(){
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
