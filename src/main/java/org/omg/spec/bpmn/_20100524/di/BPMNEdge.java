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
import org.omg.spec.dd._20100524.di.LabeledEdge;


/**
 * <p>Java-Klasse f�r BPMNEdge complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="BPMNEdge">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/spec/DD/20100524/DI}LabeledEdge">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.omg.org/spec/BPMN/20100524/DI}BPMNLabel" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="bpmnElement" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *       &lt;attribute name="sourceElement" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *       &lt;attribute name="targetElement" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *       &lt;attribute name="messageVisibleKind" type="{http://www.omg.org/spec/BPMN/20100524/DI}MessageVisibleKind" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BPMNEdge", propOrder = {
    "bpmnLabel"
})
public class BPMNEdge
    extends LabeledEdge
{

    @XmlElement(name = "BPMNLabel")
    protected BPMNLabel bpmnLabel;
    @XmlAttribute(name = "bpmnElement")
    protected QName bpmnElement;
    @XmlAttribute(name = "sourceElement")
    protected QName sourceElement;
    @XmlAttribute(name = "targetElement")
    protected QName targetElement;
    @XmlAttribute(name = "messageVisibleKind")
    protected MessageVisibleKind messageVisibleKind;

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
     * Ruft den Wert der sourceElement-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getSourceElement() {
        return sourceElement;
    }

    /**
     * Legt den Wert der sourceElement-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setSourceElement(QName value) {
        this.sourceElement = value;
    }

    /**
     * Ruft den Wert der targetElement-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getTargetElement() {
        return targetElement;
    }

    /**
     * Legt den Wert der targetElement-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setTargetElement(QName value) {
        this.targetElement = value;
    }

    /**
     * Ruft den Wert der messageVisibleKind-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link MessageVisibleKind }
     *     
     */
    public MessageVisibleKind getMessageVisibleKind() {
        return messageVisibleKind;
    }

    /**
     * Legt den Wert der messageVisibleKind-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link MessageVisibleKind }
     *     
     */
    public void setMessageVisibleKind(MessageVisibleKind value) {
        this.messageVisibleKind = value;
    }

}
