//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Aenderung an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2023.05.14 um 03:43:47 PM CEST 
//


package org.omg.spec.bpmn._20100524.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


/**
 * <p>Java-Klasse f�r tServiceTask complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="tServiceTask">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/spec/BPMN/20100524/MODEL}tTask">
 *       &lt;attribute name="implementation" type="{http://www.omg.org/spec/BPMN/20100524/MODEL}tImplementation" default="##WebService" />
 *       &lt;attribute name="operationRef" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tServiceTask")
public class TServiceTask
    extends TTask
{

    @XmlAttribute(name = "implementation")
    protected String implementation;
    @XmlAttribute(name = "operationRef")
    protected QName operationRef;

    /**
     * Ruft den Wert der implementation-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getImplementation() {
        if (implementation == null) {
            return "##WebService";
        } else {
            return implementation;
        }
    }

    /**
     * Legt den Wert der implementation-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setImplementation(String value) {
        this.implementation = value;
    }

    /**
     * Ruft den Wert der operationRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getOperationRef() {
        return operationRef;
    }

    /**
     * Legt den Wert der operationRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setOperationRef(QName value) {
        this.operationRef = value;
    }

    @Override
    public String toString() {
        return "TServiceTask{" +
                "ioSpecification=" + ioSpecification +
                ", property=" + property +
                ", dataInputAssociation=" + dataInputAssociation +
                ", dataOutputAssociation=" + dataOutputAssociation +
                ", resourceRole=" + resourceRole +
                ", loopCharacteristics=" + loopCharacteristics +
                ", isForCompensation=" + isForCompensation +
                ", startQuantity=" + startQuantity +
                ", completionQuantity=" + completionQuantity +
                ", _default=" + _default +
                ", documentation=" + documentation +
                ", extensionElements=" + extensionElements +
                ", id='" + id + '\'' +
                ", auditing=" + auditing +
                ", monitoring=" + monitoring +
                ", categoryValueRef=" + categoryValueRef +
                ", name='" + name + '\'' +
                ", incoming=" + incoming +
                ", outgoing=" + outgoing +
                ", implementation='" + implementation + '\'' +
                ", operationRef=" + operationRef +
                '}';
    }
}
