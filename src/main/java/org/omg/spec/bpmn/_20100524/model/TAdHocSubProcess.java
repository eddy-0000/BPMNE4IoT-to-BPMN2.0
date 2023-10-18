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


/**
 * <p>Java-Klasse f�r tAdHocSubProcess complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="tAdHocSubProcess">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/spec/BPMN/20100524/MODEL}tSubProcess">
 *       &lt;sequence>
 *         &lt;element name="completionCondition" type="{http://www.omg.org/spec/BPMN/20100524/MODEL}tExpression" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="cancelRemainingInstances" type="{http://www.w3.org/2001/XMLSchema}boolean" default="true" />
 *       &lt;attribute name="ordering" type="{http://www.omg.org/spec/BPMN/20100524/MODEL}tAdHocOrdering" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tAdHocSubProcess", propOrder = {
    "completionCondition"
})
public class TAdHocSubProcess
    extends TSubProcess
{

    protected TExpression completionCondition;
    @XmlAttribute(name = "cancelRemainingInstances")
    protected Boolean cancelRemainingInstances;
    @XmlAttribute(name = "ordering")
    protected TAdHocOrdering ordering;

    /**
     * Ruft den Wert der completionCondition-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TExpression }
     *     
     */
    public TExpression getCompletionCondition() {
        return completionCondition;
    }

    /**
     * Legt den Wert der completionCondition-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TExpression }
     *     
     */
    public void setCompletionCondition(TExpression value) {
        this.completionCondition = value;
    }

    /**
     * Ruft den Wert der cancelRemainingInstances-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isCancelRemainingInstances() {
        if (cancelRemainingInstances == null) {
            return true;
        } else {
            return cancelRemainingInstances;
        }
    }

    /**
     * Legt den Wert der cancelRemainingInstances-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setCancelRemainingInstances(Boolean value) {
        this.cancelRemainingInstances = value;
    }

    /**
     * Ruft den Wert der ordering-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TAdHocOrdering }
     *     
     */
    public TAdHocOrdering getOrdering() {
        return ordering;
    }

    /**
     * Legt den Wert der ordering-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TAdHocOrdering }
     *     
     */
    public void setOrdering(TAdHocOrdering value) {
        this.ordering = value;
    }

}
