//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2023.05.14 um 03:43:47 PM CEST 
//


package org.omg.spec.bpmn._20100524.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


/**
 * <p>Java-Klasse für tLane complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="tLane">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/spec/BPMN/20100524/MODEL}tBaseElement">
 *       &lt;sequence>
 *         &lt;element name="partitionElement" type="{http://www.omg.org/spec/BPMN/20100524/MODEL}tBaseElement" minOccurs="0"/>
 *         &lt;element name="flowNodeRef" type="{http://www.w3.org/2001/XMLSchema}IDREF" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="childLaneSet" type="{http://www.omg.org/spec/BPMN/20100524/MODEL}tLaneSet" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="partitionElementRef" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tLane", propOrder = {
    "partitionElement",
    "flowNodeRef",
    "childLaneSet"
})
public class TLane
    extends TBaseElement
{

    protected TBaseElement partitionElement;
    @XmlElementRef(name = "flowNodeRef", namespace = "http://www.omg.org/spec/BPMN/20100524/MODEL", type = JAXBElement.class, required = false)
    protected List<JAXBElement<Object>> flowNodeRef;
    protected TLaneSet childLaneSet;
    @XmlAttribute(name = "name")
    protected String name;
    @XmlAttribute(name = "partitionElementRef")
    protected QName partitionElementRef;

    /**
     * Ruft den Wert der partitionElement-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TBaseElement }
     *     
     */
    public TBaseElement getPartitionElement() {
        return partitionElement;
    }

    /**
     * Legt den Wert der partitionElement-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TBaseElement }
     *     
     */
    public void setPartitionElement(TBaseElement value) {
        this.partitionElement = value;
    }

    /**
     * Gets the value of the flowNodeRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the flowNodeRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFlowNodeRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link Object }{@code >}
     * 
     * 
     */
    public List<JAXBElement<Object>> getFlowNodeRef() {
        if (flowNodeRef == null) {
            flowNodeRef = new ArrayList<JAXBElement<Object>>();
        }
        return this.flowNodeRef;
    }

    /**
     * Ruft den Wert der childLaneSet-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TLaneSet }
     *     
     */
    public TLaneSet getChildLaneSet() {
        return childLaneSet;
    }

    /**
     * Legt den Wert der childLaneSet-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TLaneSet }
     *     
     */
    public void setChildLaneSet(TLaneSet value) {
        this.childLaneSet = value;
    }

    /**
     * Ruft den Wert der name-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Legt den Wert der name-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Ruft den Wert der partitionElementRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getPartitionElementRef() {
        return partitionElementRef;
    }

    /**
     * Legt den Wert der partitionElementRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setPartitionElementRef(QName value) {
        this.partitionElementRef = value;
    }

}
