import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.util.Random;

public class XMLExtractor {

    private Element root;
    private NodeList processList;
    final int TASK_WIDTH = 100;
    final int TASK_HEIGHT = 80;
    final int FLOW_LENGTH = 100;
    private Document doc;

    public XMLExtractor() {
    }

    public void convertIoTElements(File xmlFile) throws ParserConfigurationException, IOException, SAXException, TransformerException {
        // create a DocumentBuilderFactory
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

        // create a DocumentBuilder
        DocumentBuilder builder = factory.newDocumentBuilder();

        // parse the XML file and create a Document object
        doc = builder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        // Get the process element
        root = doc.getDocumentElement();
        processList = root.getElementsByTagName("bpmn2:process");
        for (int i = 0; i < processList.getLength(); i++) {
            transformList(processList.item(i));
            ((Element)processList.item(i)).setAttribute("isExecutable", ""+false);

            // save the changes to the original XML file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(xmlFile+"_copy");
            transformer.transform(source, result);
        }
    }

    private void transformList(Node node){
        Node parent = node;
        NodeList children = parent.getChildNodes();
        for(int j = 0; j < children.getLength(); j++) {
            if (children.item(j).getAttributes() != null) {
                Element element = (Element) children.item(j);
                var iot = children.item(j).getAttributes().getNamedItem("iot:type");
                // Object artifact
                if (element.getTagName().contains("bpmn2:dataObjectReference") && iot != null && iot.getNodeValue().equals("obj")) {
                    // Get the named node map of the child element
                    NamedNodeMap attrs = children.item(j).getAttributes();

                    // Remove the "attribute-to-remove" attribute from the named node map
                    attrs.removeNamedItem("iot:type");
                    if (element.getAttributes().getNamedItem("name") != null)
                        element.setAttribute("name", "(IoT)" + children.item(j).getAttributes().getNamedItem("name").getNodeValue());

                }
                //Catch Artefact
                else if (element.getTagName().contains("bpmn2:dataObjectReference") && iot != null && iot.getNodeValue().equals("artefact-catch")) {
                    // Create a new child element
                    Element newChild = doc.createElement("bpmn2:serviceTask");
                    for (int k = 0; k < children.getLength(); k++) {
                        if (children.item(k).getAttributes() != null) {
                            Element e = (Element) children.item(k);
                            Node inputNode = (Node) e.getElementsByTagName("bpmn2:dataInputAssociation").item(0);
                            if (e.getTagName().contains("bpmn2:task") && inputNode != null && children.item(j).getAttributes().getNamedItem("id").getNodeValue().equals(((Element)inputNode).getElementsByTagName("bpmn2:sourceRef").item(0).getTextContent())) {
                                newChild.setAttribute("id", children.item(k).getAttributes().getNamedItem("id").getNodeValue());
                                newChild.setAttribute("name", "(IoT) " + children.item(k).getAttributes().getNamedItem("name").getNodeValue());
                                Element subChild1 = doc.createElement("bpmn2:incoming");
                                subChild1.appendChild(doc.createTextNode(e.getElementsByTagName("bpmn2:incoming").item(0).getTextContent()));
                                Element subChild2 = doc.createElement("bpmn2:outgoing");
                                subChild2.appendChild(doc.createTextNode(e.getElementsByTagName("bpmn2:outgoing").item(0).getTextContent()));
                                newChild.appendChild(subChild1);
                                newChild.appendChild(subChild2);
                                parent.removeChild(e);
                                break;
                            }
                        }
                    }
                    //add child to node list
                    parent.appendChild(newChild);
                    parent.removeChild(element);
                    j = j-2;
                    changeSizeOfElement(element.getAttributes().getNamedItem("id").getNodeValue(), TASK_WIDTH, TASK_HEIGHT);
                }
                //IoT Intermediate Catch Event
                else if (element.getTagName().contains("bpmn2:intermediateCatchEvent") && iot != null && iot.getNodeValue().equals("catch")) {
                    if (element.getElementsByTagName("bpmn2:outgoing").getLength() == 1) {
                        // Create a new child element
                        String newID = element.getAttributes().getNamedItem("id").getNodeValue();
                        String newName = element.getAttributes().getNamedItem("name").getNodeValue();
                        String newIncoming = element.getElementsByTagName("bpmn2:incoming").item(0).getTextContent();
                        String newOutgoing = element.getElementsByTagName("bpmn2:outgoing").item(0).getTextContent();
                        Element task = (Element) createBusinessRuleTask(newID, newName, newIncoming , newOutgoing, null, null);

                        //add child to node list
                        parent.appendChild(task);
                        parent.removeChild(element);
                        j = j-1;
                        changeSizeOfElement(element.getAttributes().getNamedItem("id").getNodeValue(), TASK_WIDTH, TASK_HEIGHT);

                        //change the position of the shape.
                        int[] originIncomingCoordinates = getCoordinates(element.getElementsByTagName("bpmn2:incoming").item(0).getTextContent());
                        int _y = originIncomingCoordinates[1] - TASK_HEIGHT/2;
                        changeShapePosition(newID, -1, _y, -1 , -1);

                        //change the position of the edge.
                        String edgeID = element.getElementsByTagName("bpmn2:outgoing").item(0).getTextContent();
                        int[] originOutgoingCoordinates = getCoordinates(edgeID);
                        int _x = originOutgoingCoordinates[0] - TASK_HEIGHT/2;
                        changeEdgePosition(edgeID, _x, -1, -1 , -1);
                    }

                }
                //IoT Start Event
                else if (element.getTagName().contains("bpmn2:startEvent") && iot != null && iot.getNodeValue().equals("start")) {
                    // Create a new child element
                    Element newChild = doc.createElement("bpmn2:startEvent");
                    if (element.getAttributes().getNamedItem("name") != null)
                        newChild.setAttribute("name", "(IoT) " + element.getAttributes().getNamedItem("name").getNodeValue());
                    newChild.setAttribute("id", element.getAttributes().getNamedItem("id").getNodeValue());
                    //bpmn2:outgoing child
                    Element subChild1 = doc.createElement("bpmn2:outgoing");
                    subChild1.appendChild(doc.createTextNode(element.getElementsByTagName("bpmn2:outgoing").item(0).getTextContent()));
                    //bpmn:conditionalEventDefinition child
                    Element subChild2 = doc.createElement("bpmn2:conditionalEventDefinition");
                    subChild2.setAttribute("id", "ConditionalEventDefinition_" + randomNumberSequence());
                    Element subSubChild = doc.createElement("bpmn2:condition");
                    subSubChild.setAttribute("xsi:type","bpmn:tFormalExpression");
                    //add children to parents
                    subChild2.appendChild(subSubChild);
                    newChild.appendChild(subChild1);
                    newChild.appendChild(subChild2);
                    //add child to node list
                    parent.appendChild(newChild);
                    parent.removeChild(element);
                    j = j-1;
                }
                //IoT End Event
                else if (element.getTagName().contains("bpmn2:endEvent") && iot != null && iot.getNodeValue().equals("end")) {
                    // Create a new child element
                    String newID = element.getAttributes().getNamedItem("id").getNodeValue();
                    String newName = element.getAttributes().getNamedItem("name").getNodeValue();
                    String newIncoming = element.getElementsByTagName("bpmn2:incoming").item(0).getTextContent();
                    String flowId = "Flow_" + randomNumberSequence();
                    Element taskEvent = (Element) createServiceTask(newID, newName, newIncoming , flowId, null, null);

                    //change the position of the shape.
                    int[] originFlowCoordinates = getCoordinates(element.getElementsByTagName("bpmn2:incoming").item(0).getTextContent());
                    int _y = originFlowCoordinates[1] - TASK_HEIGHT/2;
                    changeShapePosition(newID, -1, _y, -1 , -1);

                    //flow element
                    Element flowEvent = doc.createElement("bpmn2:sequenceFlow");
                    flowEvent.setAttribute("id", flowId);
                    flowEvent.setAttribute("sourceRef", element.getAttributes().getNamedItem("id").getNodeValue());
                    String endID = "Event_" + randomNumberSequence();
                    flowEvent.setAttribute("targetRef", endID);

                    //end element
                    Element endEvent = doc.createElement("bpmn2:endEvent");
                    endEvent.setAttribute("id", endID);
                    //bpmn2:incoming child
                    Element subIncomingEnd = doc.createElement("bpmn2:incoming");
                    subIncomingEnd.appendChild(doc.createTextNode(flowId));
                    endEvent.appendChild(subIncomingEnd);


                    //add child to node list
                    parent.appendChild(taskEvent);
                    parent.appendChild(flowEvent);
                    parent.appendChild(endEvent);
                    parent.removeChild(element);
                    changeSizeOfElement(element.getAttributes().getNamedItem("id").getNodeValue(), TASK_WIDTH, TASK_HEIGHT);
                    j = j-1;

                    int[] coord = getCoordinates(element.getAttributes().getNamedItem("id").getNodeValue());
                    Node parentPlane = root.getElementsByTagName("bpmndi:BPMNPlane").item(0);

                    Element flowEdge =  doc.createElement("bpmndi:BPMNEdge");

                    //create BPMNedge
                    flowEdge.setAttribute("id", flowId + "_di");
                    flowEdge.setAttribute("bpmnElement", flowId);
                    //di:waypoint children
                    int y = coord[1]+coord[3]/2;
                    int x = coord[0]+coord[2];
                    Element waypoint1 = doc.createElement("di:waypoint");
                    waypoint1.setAttribute("x", ""+x);
                    waypoint1.setAttribute("y", ""+y);
                    Element waypoint2 = doc.createElement("di:waypoint");
                    int x2 = x+ FLOW_LENGTH;
                    waypoint2.setAttribute("x", ""+x2);
                    waypoint2.setAttribute("y", ""+y);
                    flowEdge.appendChild(waypoint1);
                    flowEdge.appendChild(waypoint2);
                    parentPlane.appendChild(flowEdge);

                    //create BPMNshape
                    Element endShape = doc.createElement("bpmndi:BPMNShape");
                    endShape.setAttribute("id", endID + "_di");
                    endShape.setAttribute("bpmnElement", endID);
                    //dc:bounds children
                    int y2 = y-18;
                    Element bounds = doc.createElement("dc:Bounds");
                    bounds.setAttribute("x", ""+x2);
                    bounds.setAttribute("y", ""+y2);
                    bounds.setAttribute("width", ""+36);
                    bounds.setAttribute("height", ""+36);
                    endShape.appendChild(bounds);
                    parentPlane.appendChild(endShape);

                }
                //IoT Intermediate Throw Event
                else if (element.getTagName().contains("bpmn2:intermediateCatchEvent") && iot != null && iot.getNodeValue().equals("throw")) {
                    // Create a new child element
                    String newID = element.getAttributes().getNamedItem("id").getNodeValue();
                    String newName = element.getAttributes().getNamedItem("name").getNodeValue();
                    String newIncoming = element.getElementsByTagName("bpmn2:incoming").item(0).getTextContent();
                    String newOutgoing = element.getElementsByTagName("bpmn2:outgoing").item(0).getTextContent();
                    //add child to node list
                    parent.appendChild(createServiceTask(newID, newName, newIncoming , newOutgoing, null, null));
                    parent.removeChild(element);
                    j = j-1;
                    changeSizeOfElement(element.getAttributes().getNamedItem("id").getNodeValue(), TASK_WIDTH, TASK_HEIGHT);

                    //change the position of the shape.
                    int[] originIncomingCoordinates = getCoordinates(element.getElementsByTagName("bpmn2:incoming").item(0).getTextContent());
                    int _y = originIncomingCoordinates[1] - TASK_HEIGHT/2;
                    changeShapePosition(newID, -1, _y, -1 , -1);

                    //change the position of the edge.
                    String edgeID = element.getElementsByTagName("bpmn2:outgoing").item(0).getTextContent();
                    int[] originOutgoingCoordinates = getCoordinates(edgeID);
                    int _x = originOutgoingCoordinates[0] - TASK_HEIGHT/2;
                    changeEdgePosition(edgeID, _x, -1, -1 , -1);
                }
                //Iot Sensor Artifact
                else if (element.getTagName().contains("bpmn2:dataObjectReference") && iot != null && iot.getNodeValue().equals("sensor")) {
                    Element collaborations = (Element) root.getElementsByTagName("bpmn2:collaboration").item(0);
                    Node parentPlane = root.getElementsByTagName("bpmndi:BPMNPlane").item(0);

                    if (collaborations == null){
                        //create white box
                        collaborations = doc.createElement("bpmn2:collaboration");
                        String cID = "Collaboration_" + randomNumberSequence();
                        collaborations.setAttribute("id", cID);
                        root.appendChild(collaborations);
                        ((Element)parentPlane).setAttribute("bpmnElement", cID);

                        //create white box
                        Element whiteBox = doc.createElement("bpmn2:participant");
                        String pID2 = "Participant_" + randomNumberSequence();
                        whiteBox.setAttribute("id", pID2);
                        whiteBox.setAttribute("processRef", root.getElementsByTagName("bpmn2:process").item(0).getAttributes().getNamedItem("id").getNodeValue());
                        collaborations.appendChild(whiteBox);

                        //create white box
                        Element whiteBoxShape = doc.createElement("bpmndi:BPMNShape");
                        whiteBoxShape.setAttribute("id", pID2 + "_di");
                        whiteBoxShape.setAttribute("bpmnElement", pID2);
                        whiteBoxShape.setAttribute("isHorizontal", ""+true);

                        Element whiteBoxShapeBounds = doc.createElement("dc:Bounds");
                        whiteBoxShapeBounds.setAttribute("x", ""+0);
                        whiteBoxShapeBounds.setAttribute("y", ""+0);
                        whiteBoxShapeBounds.setAttribute("width", ""+2000);
                        whiteBoxShapeBounds.setAttribute("height", ""+600);
                        whiteBoxShape.appendChild(whiteBoxShapeBounds);

                        parentPlane.appendChild(whiteBoxShape);
                    }
                    //create black box
                    Element blackBox = doc.createElement("bpmn2:participant");
                    String pID = element.getAttributes().getNamedItem("id").getNodeValue();
                    blackBox.setAttribute("id", pID);
                    blackBox.setAttribute("name", "(IoT)" + element.getAttributes().getNamedItem("name").getNodeValue());
                    collaborations.appendChild(blackBox);
                    //create message flow from black box
                    Element messageFlow = doc.createElement("bpmn2:messageFlow");
                    String messageID = "Flow_" + randomNumberSequence();
                    messageFlow.setAttribute("id", messageID);
                    messageFlow.setAttribute("sourceRef", pID);

                    String targetReference = null;
                    for (int k = 0; k < children.getLength(); k++) {
                        if (children.item(k).getAttributes() != null) {
                            Element e = (Element) children.item(k);
                            boolean foundTarget = false;
                            NodeList inputNodes = e.getElementsByTagName("bpmn2:dataInputAssociation");
                            for (int i = 0; i<inputNodes.getLength(); i++) {
                                Node inputNode = inputNodes.item(i);
                                if (e.getTagName().contains("bpmn2:task") && inputNode != null && element.getAttributes().getNamedItem("id").getNodeValue().equals(((Element) inputNode).getElementsByTagName("bpmn2:sourceRef").item(0).getTextContent())) {
                                    foundTarget = true;
                                    targetReference = e.getAttributes().getNamedItem("id").getNodeValue();
                                    //TODO, check for further associations
                                    parentPlane.removeChild(getNode(((Element) inputNode).getAttribute("id") + "_di"));
                                    break;
                                }
                            }
                            if (foundTarget){
                                break;
                            }
                        }
                    }

                    messageFlow.setAttribute("targetRef", targetReference);
                    collaborations.appendChild(messageFlow);
                    parent.removeChild(element);

                    int[] coord = getCoordinates(targetReference);
                    int x = coord[0]-150+coord[2]/2;
                    int y = 700;
                    changeShapePosition(pID, x, y,300,50);



                    //create BPMNedge
                    Element flowEdge = doc.createElement("bpmndi:BPMNEdge");
                    flowEdge.setAttribute("id", messageID + "_di");
                    flowEdge.setAttribute("bpmnElement", messageID);
                    //di:waypoint children
                    int y2 = coord[1]+coord[3];
                    int x2 = coord[0]+coord[2]/2;
                    int y3 = y;
                    Element waypoint1 = doc.createElement("di:waypoint");
                    waypoint1.setAttribute("x", ""+x2);
                    waypoint1.setAttribute("y", ""+y3);
                    Element waypoint2 = doc.createElement("di:waypoint");
                    waypoint2.setAttribute("x", ""+x2);
                    waypoint2.setAttribute("y", ""+y2);
                    flowEdge.appendChild(waypoint1);
                    flowEdge.appendChild(waypoint2);
                    parentPlane.appendChild(flowEdge);

                    j = j-1;
                }
                else if (element.getTagName().contains("bpmn2:subProcess")){
                    transformList(element);
                }
            }
        }
    }

    private Node createIntermediateMessageCatchEvent(String id, String name, String inComing, String outGoing){
        // Create a new child element
        Element newChild = doc.createElement("bpmn2:intermediateCatchEvent");
        newChild.setAttribute("id", id);
        if (name != null)
            newChild.setAttribute("name", "(IoT) " + name);
        Element subChild1 = doc.createElement("bpmn2:incoming");
        subChild1.appendChild(doc.createTextNode(inComing));
        Element subChild2 = doc.createElement("bpmn2:outgoing");
        subChild2.appendChild(doc.createTextNode(outGoing));
        newChild.appendChild(subChild1);
        newChild.appendChild(subChild2);
        Element subChild3 = doc.createElement("bpmn2:messageEventDefinition");
        subChild3.setAttribute("id", "MessageEventDefinition_"+randomNumberSequence());
        newChild.appendChild(subChild3);
        return newChild;
    }

    /**
     * Changes the positioning of a Shape. Enter -1 if the origin value should be used.
     * @param id ID of Element.
     * @param x x coordinate of the shape.
     * @param y y coordinate of the shape.
     * @param width Width of the shape.
     * @param height Height of the shape.
     */
    private void changeShapePosition(String id, int x, int y, int width, int height){
        Node originShape = getNode(id+"_di");
        Element bounds = (Element) ((Element)originShape).getElementsByTagName("dc:Bounds").item(0);
        if (x != -1)
            bounds.setAttribute("x", ""+x);
        if (y != -1)
            bounds.setAttribute("y", ""+y);
        if (width != -1)
            bounds.setAttribute("width", "" + width);
        if (height != -1)
            bounds.setAttribute("height", ""+height);
    }

    /**
     * Changes the positioning of an Edge/Flow. Enter -1 if the origin value should be used.
     * @param id ID of Element.
     * @param x1 Starting x coordinate.
     * @param y1 Starting y coordinate.
     * @param x2 Ending x coordinate.
     * @param y2 Ending y coordinate.
     */
    private void changeEdgePosition(String id, int x1, int y1, int x2, int y2){
        Node originTask = getNode(id+"_di");
        Element waypoint1 = (Element) ((Element)originTask).getElementsByTagName("di:waypoint").item(0);
        Element waypoint2 = (Element) ((Element)originTask).getElementsByTagName("di:waypoint").item(((Element)originTask).getElementsByTagName("di:waypoint").getLength());
        if (x1 != -1)
            waypoint1.setAttribute("x", ""+x1);
        if (y1 != -1)
            waypoint1.setAttribute("y", ""+y1);
        if (x2 != -1)
            waypoint2.setAttribute("x", ""+x2);
        if (y2 != -1)
            waypoint2.setAttribute("y", ""+y1);
    }

    /**
     * Creates a serviceTask Node.
     * @param id
     * @param name
     * @param inComing
     * @param outGoing
     * @param propertyIDs
     * @param propertyNames
     * @return
     */
    private Node createServiceTask(String id, String name, String inComing, String outGoing, String[] propertyIDs, String[] propertyNames){
        // Create a new child element
        Element newChild = doc.createElement("bpmn2:serviceTask");
        newChild.setAttribute("id", id);
        if (name != null)
            newChild.setAttribute("name", "(IoT) " + name);
        Element subChild1 = doc.createElement("bpmn2:incoming");
        subChild1.appendChild(doc.createTextNode(inComing));
        Element subChild2 = doc.createElement("bpmn2:outgoing");
        subChild2.appendChild(doc.createTextNode(outGoing));
        newChild.appendChild(subChild1);
        newChild.appendChild(subChild2);
        if (propertyIDs != null) {
            for (String property: propertyIDs) {
                //TODO
            }
        }
        return newChild;
    }

    /**
     * Creates a serviceTask Node.
     * @param id
     * @param name
     * @param inComing
     * @param outGoing
     * @param propertyIDs
     * @param propertyNames
     * @return
     */
    private Node createBusinessRuleTask(String id, String name, String inComing, String outGoing, String[] propertyIDs, String[] propertyNames){
        // Create a new child element
        Element newChild = doc.createElement("bpmn2:businessRuleTask");
        newChild.setAttribute("id", id);
        if (name != null)
            newChild.setAttribute("name", "(IoT) " + name);
        Element subChild1 = doc.createElement("bpmn2:incoming");
        subChild1.appendChild(doc.createTextNode(inComing));
        Element subChild2 = doc.createElement("bpmn2:outgoing");
        subChild2.appendChild(doc.createTextNode(outGoing));
        newChild.appendChild(subChild1);
        newChild.appendChild(subChild2);
        if (propertyIDs != null) {
            for (String property: propertyIDs) {
                //TODO
            }
        }
        return newChild;
    }

    /**
     * This gets the Node with the given id.
     * @param id The element id.
     * @return
     */
    private Node getNode(String id){
        NodeList processList = root.getElementsByTagName("bpmndi:BPMNPlane");
        for (int i = 0; i < processList.getLength(); i++) {
            Node parent = processList.item(i);
            NodeList children = parent.getChildNodes();
            for(int j = 0; j < children.getLength(); j++) {
                if (children.item(j).getAttributes() != null) {
                    if ( children.item(j).getAttributes().getNamedItem("id").getNodeValue().equals(id)) {
                        return children.item(j);
                    }
                }
            }
        }
        NodeList processList2 = root.getElementsByTagName("bpmn2:process");
        for (int i = 0; i < processList2.getLength(); i++) {
            Node parent2 = processList2.item(i);
            NodeList children2 = parent2.getChildNodes();
            for(int j = 0; j < children2.getLength(); j++) {
                if (children2.item(j).getAttributes() != null) {
                    if (children2.item(j).getAttributes().getNamedItem("id").getNodeValue().equals(id)) {
                        return children2.item(j);
                    }
                }
            }
        }
        return null;
    }

    /**
     * Gets the coordinates of an element.
     * <p>
     * Shapes:
     * <p>
     * coord[0] = x
     * <p>
     * coord[1] = y
     * <p>
     * coord[2] = width
     * <p>
     * coord[3] = height
     * <p>
     * Edges:
     * <p>
     * coord[0] = x1
     * <p>
     * coord[1] = y1
     * <p>
     * coord[2] = x1
     * <p>
     * coord[3] = y1
     * @param id
     * @return
     */
    private int[] getCoordinates(String id){
        NodeList processList = root.getElementsByTagName("bpmndi:BPMNPlane");
        for (int i = 0; i < processList.getLength(); i++) {
            Node parent = processList.item(i);
            NodeList children = parent.getChildNodes();
            for(int j = 0; j < children.getLength(); j++) {
                if (children.item(j).getAttributes() != null) {
                    if ( ((Element) children.item(j)).getTagName().contains("bpmndi:BPMNShape") && children.item(j).getAttributes().getNamedItem("bpmnElement").getNodeValue().equals(id)) {
                        // Get the NamedNodeMap of attributes for the element
                        Element element = (Element) children.item(j);
                        NamedNodeMap attributes = element.getElementsByTagName("dc:Bounds").item(0).getAttributes();
                        int[] coord = new int[4];

                        coord[0] = Integer.parseInt(attributes.getNamedItem("x").getNodeValue());
                        coord[1] = Integer.parseInt(attributes.getNamedItem("y").getNodeValue());
                        coord[2] = Integer.parseInt(attributes.getNamedItem("width").getNodeValue());
                        coord[3] = Integer.parseInt(attributes.getNamedItem("height").getNodeValue());
                        return coord;
                    }
                }
                if (children.item(j).getAttributes() != null) {
                    if ( ((Element) children.item(j)).getTagName().contains("bpmndi:BPMNEdge") && children.item(j).getAttributes().getNamedItem("bpmnElement").getNodeValue().equals(id)) {
                        // Get the NamedNodeMap of attributes for the element
                        Element element = (Element) children.item(j);
                        NamedNodeMap attributes1 = element.getElementsByTagName("di:waypoint").item(0).getAttributes();
                        NamedNodeMap attributes2 = element.getElementsByTagName("di:waypoint").item(1).getAttributes();
                        int[] coord = new int[4];

                        coord[0] = Integer.parseInt(attributes1.getNamedItem("x").getNodeValue());
                        coord[1] = Integer.parseInt(attributes1.getNamedItem("y").getNodeValue());
                        coord[2] = Integer.parseInt(attributes2.getNamedItem("x").getNodeValue());
                        coord[3] = Integer.parseInt(attributes2.getNamedItem("y").getNodeValue());
                        return coord;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Gets a random 7 letter sequence, used for IDs.
     * @return
     */
    private String randomNumberSequence(){
        // Create a new Random object
        Random rand = new Random();
        //create alphabet
        String alphabet = "abcdefghijklmnopqrstuvwxyz0123456789";
        // Generate the random sequence
        String sequence = "";
        for (int i = 0; i < 7; i++) {
            int index = rand.nextInt(alphabet.length());
            sequence += alphabet.charAt(index);
        }
        return sequence;
    }

    /**
     * Changes the size of a Shape.
     * @param id
     * @param width
     * @param height
     */
    private void changeSizeOfElement(String id, int width, int height){
        processList = root.getElementsByTagName("bpmndi:BPMNPlane");
        for (int i = 0; i < processList.getLength(); i++) {
            Node parent = processList.item(i);
            NodeList children = parent.getChildNodes();
            for(int j = 0; j < children.getLength(); j++) {
                if (children.item(j).getAttributes() != null) {
                    if ( ((Element) children.item(j)).getTagName().contains("bpmndi:BPMNShape") && children.item(j).getAttributes().getNamedItem("bpmnElement").getNodeValue().equals(id)) {
                        // Get the NamedNodeMap of attributes for the element
                        Element element = (Element) children.item(j);
                        NamedNodeMap attributes = element.getElementsByTagName("dc:Bounds").item(0).getAttributes();

                        // Find the attribute we want to modify and change its value
                        Node attribute1 = attributes.getNamedItem("width");
                        attribute1.setNodeValue("" + width);
                        Node attribute2 = attributes.getNamedItem("height");
                        attribute2.setNodeValue("" + height);
                    }
                }
            }
        }
    }
}
