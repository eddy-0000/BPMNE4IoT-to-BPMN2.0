//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2023.05.14 um 03:43:47 PM CEST 
//


package org.omg.spec.bpmn._20100524.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für tTimerEventDefinition complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="tTimerEventDefinition">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/spec/BPMN/20100524/MODEL}tEventDefinition">
 *       &lt;choice>
 *         &lt;element name="timeDate" type="{http://www.omg.org/spec/BPMN/20100524/MODEL}tExpression" minOccurs="0"/>
 *         &lt;element name="timeDuration" type="{http://www.omg.org/spec/BPMN/20100524/MODEL}tExpression" minOccurs="0"/>
 *         &lt;element name="timeCycle" type="{http://www.omg.org/spec/BPMN/20100524/MODEL}tExpression" minOccurs="0"/>
 *       &lt;/choice>
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tTimerEventDefinition", propOrder = {
    "timeDate",
    "timeDuration",
    "timeCycle"
})
public class TTimerEventDefinition
    extends TEventDefinition
{

    protected TExpression timeDate;
    protected TExpression timeDuration;
    protected TExpression timeCycle;

    /**
     * Ruft den Wert der timeDate-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TExpression }
     *     
     */
    public TExpression getTimeDate() {
        return timeDate;
    }

    /**
     * Legt den Wert der timeDate-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TExpression }
     *     
     */
    public void setTimeDate(TExpression value) {
        this.timeDate = value;
    }

    /**
     * Ruft den Wert der timeDuration-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TExpression }
     *     
     */
    public TExpression getTimeDuration() {
        return timeDuration;
    }

    /**
     * Legt den Wert der timeDuration-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TExpression }
     *     
     */
    public void setTimeDuration(TExpression value) {
        this.timeDuration = value;
    }

    /**
     * Ruft den Wert der timeCycle-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TExpression }
     *     
     */
    public TExpression getTimeCycle() {
        return timeCycle;
    }

    /**
     * Legt den Wert der timeCycle-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TExpression }
     *     
     */
    public void setTimeCycle(TExpression value) {
        this.timeCycle = value;
    }

}
