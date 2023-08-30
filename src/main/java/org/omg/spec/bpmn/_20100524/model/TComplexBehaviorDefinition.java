//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2023.05.14 um 03:43:47 PM CEST 
//


package org.omg.spec.bpmn._20100524.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für tComplexBehaviorDefinition complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="tComplexBehaviorDefinition">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/spec/BPMN/20100524/MODEL}tBaseElement">
 *       &lt;sequence>
 *         &lt;element name="condition" type="{http://www.omg.org/spec/BPMN/20100524/MODEL}tFormalExpression"/>
 *         &lt;element name="event" type="{http://www.omg.org/spec/BPMN/20100524/MODEL}tImplicitThrowEvent" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tComplexBehaviorDefinition", propOrder = {
    "condition",
    "event"
})
public class TComplexBehaviorDefinition
    extends TBaseElement
{

    @XmlElement(required = true)
    protected TFormalExpression condition;
    protected TImplicitThrowEvent event;

    /**
     * Ruft den Wert der condition-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TFormalExpression }
     *     
     */
    public TFormalExpression getCondition() {
        return condition;
    }

    /**
     * Legt den Wert der condition-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TFormalExpression }
     *     
     */
    public void setCondition(TFormalExpression value) {
        this.condition = value;
    }

    /**
     * Ruft den Wert der event-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TImplicitThrowEvent }
     *     
     */
    public TImplicitThrowEvent getEvent() {
        return event;
    }

    /**
     * Legt den Wert der event-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TImplicitThrowEvent }
     *     
     */
    public void setEvent(TImplicitThrowEvent value) {
        this.event = value;
    }

}
