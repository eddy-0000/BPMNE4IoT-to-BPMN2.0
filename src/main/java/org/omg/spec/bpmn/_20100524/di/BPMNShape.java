//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Aenderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren.
// Generiert: 2023.05.14 um 03:43:47 PM CEST 
//


package org.omg.spec.bpmn._20100524.di;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;
import org.omg.spec.dd._20100524.di.LabeledShape;


/**
 * <p>Java-Klasse f�r BPMNShape complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="BPMNShape">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/spec/DD/20100524/DI}LabeledShape">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.omg.org/spec/BPMN/20100524/DI}BPMNLabel" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="bpmnElement" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *       &lt;attribute name="isHorizontal" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="isExpanded" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="isMarkerVisible" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="isMessageVisible" type="{http://www.w3.org/2001/XMLSchema}boolean" />
 *       &lt;attribute name="participantBandKind" type="{http://www.omg.org/spec/BPMN/20100524/DI}ParticipantBandKind" />
 *       &lt;attribute name="choreographyActivityShape" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BPMNShape", propOrder = {
    "bpmnLabel"
})
public class BPMNShape
    extends LabeledShape
{

    @XmlElement(name = "BPMNLabel")
    protected BPMNLabel bpmnLabel;
    @XmlAttribute(name = "bpmnElement")
    protected QName bpmnElement;
    @XmlAttribute(name = "isHorizontal")
    protected Boolean isHorizontal;
    @XmlAttribute(name = "isExpanded")
    protected Boolean isExpanded;
    @XmlAttribute(name = "isMarkerVisible")
    protected Boolean isMarkerVisible;
    @XmlAttribute(name = "isMessageVisible")
    protected Boolean isMessageVisible;
    @XmlAttribute(name = "participantBandKind")
    protected ParticipantBandKind participantBandKind;
    @XmlAttribute(name = "choreographyActivityShape")
    protected QName choreographyActivityShape;

    /**
     * Ruft den Wert der bpmnLabel-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BPMNLabel }
     *     
     */
    public BPMNLabel getBPMNLabel() {
        return bpmnLabel;
    }

    /**
     * Legt den Wert der bpmnLabel-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BPMNLabel }
     *     
     */
    public void setBPMNLabel(BPMNLabel value) {
        this.bpmnLabel = value;
    }

    /**
     * Ruft den Wert der bpmnElement-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getBpmnElement() {
        return bpmnElement;
    }

    /**
     * Legt den Wert der bpmnElement-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setBpmnElement(QName value) {
        this.bpmnElement = value;
    }

    /**
     * Ruft den Wert der isHorizontal-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsHorizontal() {
        return isHorizontal;
    }

    /**
     * Legt den Wert der isHorizontal-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsHorizontal(Boolean value) {
        this.isHorizontal = value;
    }

    /**
     * Ruft den Wert der isExpanded-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsExpanded() {
        return isExpanded;
    }

    /**
     * Legt den Wert der isExpanded-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsExpanded(Boolean value) {
        this.isExpanded = value;
    }

    /**
     * Ruft den Wert der isMarkerVisible-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsMarkerVisible() {
        return isMarkerVisible;
    }

    /**
     * Legt den Wert der isMarkerVisible-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsMarkerVisible(Boolean value) {
        this.isMarkerVisible = value;
    }

    /**
     * Ruft den Wert der isMessageVisible-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isIsMessageVisible() {
        return isMessageVisible;
    }

    /**
     * Legt den Wert der isMessageVisible-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsMessageVisible(Boolean value) {
        this.isMessageVisible = value;
    }

    /**
     * Ruft den Wert der participantBandKind-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ParticipantBandKind }
     *     
     */
    public ParticipantBandKind getParticipantBandKind() {
        return participantBandKind;
    }

    /**
     * Legt den Wert der participantBandKind-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ParticipantBandKind }
     *     
     */
    public void setParticipantBandKind(ParticipantBandKind value) {
        this.participantBandKind = value;
    }

    /**
     * Ruft den Wert der choreographyActivityShape-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getChoreographyActivityShape() {
        return choreographyActivityShape;
    }

    /**
     * Legt den Wert der choreographyActivityShape-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setChoreographyActivityShape(QName value) {
        this.choreographyActivityShape = value;
    }

}
