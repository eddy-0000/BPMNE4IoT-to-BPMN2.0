//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2023.05.14 um 03:43:47 PM CEST 
//


package org.omg.spec.bpmn._20100524.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


/**
 * <p>Java-Klasse für tMultiInstanceLoopCharacteristics complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="tMultiInstanceLoopCharacteristics">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/spec/BPMN/20100524/MODEL}tLoopCharacteristics">
 *       &lt;sequence>
 *         &lt;element name="loopCardinality" type="{http://www.omg.org/spec/BPMN/20100524/MODEL}tExpression" minOccurs="0"/>
 *         &lt;element name="loopDataInputRef" type="{http://www.w3.org/2001/XMLSchema}QName" minOccurs="0"/>
 *         &lt;element name="loopDataOutputRef" type="{http://www.w3.org/2001/XMLSchema}QName" minOccurs="0"/>
 *         &lt;element name="inputDataItem" type="{http://www.omg.org/spec/BPMN/20100524/MODEL}tDataInput" minOccurs="0"/>
 *         &lt;element name="outputDataItem" type="{http://www.omg.org/spec/BPMN/20100524/MODEL}tDataOutput" minOccurs="0"/>
 *         &lt;element ref="{http://www.omg.org/spec/BPMN/20100524/MODEL}complexBehaviorDefinition" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="completionCondition" type="{http://www.omg.org/spec/BPMN/20100524/MODEL}tExpression" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="isSequential" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="behavior" type="{http://www.omg.org/spec/BPMN/20100524/MODEL}tMultiInstanceFlowCondition" default="All" />
 *       &lt;attribute name="oneBehaviorEventRef" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *       &lt;attribute name="noneBehaviorEventRef" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tMultiInstanceLoopCharacteristics", propOrder = {
    "loopCardinality",
    "loopDataInputRef",
    "loopDataOutputRef",
    "inputDataItem",
    "outputDataItem",
    "complexBehaviorDefinition",
    "completionCondition"
})
public class TMultiInstanceLoopCharacteristics
    extends TLoopCharacteristics
{

    protected TExpression loopCardinality;
    protected QName loopDataInputRef;
    protected QName loopDataOutputRef;
    protected TDataInput inputDataItem;
    protected TDataOutput outputDataItem;
    protected List<TComplexBehaviorDefinition> complexBehaviorDefinition;
    protected TExpression completionCondition;
    @XmlAttribute(name = "isSequential")
    protected Boolean isSequential;
    @XmlAttribute(name = "behavior")
    protected TMultiInstanceFlowCondition behavior;
    @XmlAttribute(name = "oneBehaviorEventRef")
    protected QName oneBehaviorEventRef;
    @XmlAttribute(name = "noneBehaviorEventRef")
    protected QName noneBehaviorEventRef;

    /**
     * Ruft den Wert der loopCardinality-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TExpression }
     *     
     */
    public TExpression getLoopCardinality() {
        return loopCardinality;
    }

    /**
     * Legt den Wert der loopCardinality-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TExpression }
     *     
     */
    public void setLoopCardinality(TExpression value) {
        this.loopCardinality = value;
    }

    /**
     * Ruft den Wert der loopDataInputRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getLoopDataInputRef() {
        return loopDataInputRef;
    }

    /**
     * Legt den Wert der loopDataInputRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setLoopDataInputRef(QName value) {
        this.loopDataInputRef = value;
    }

    /**
     * Ruft den Wert der loopDataOutputRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getLoopDataOutputRef() {
        return loopDataOutputRef;
    }

    /**
     * Legt den Wert der loopDataOutputRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setLoopDataOutputRef(QName value) {
        this.loopDataOutputRef = value;
    }

    /**
     * Ruft den Wert der inputDataItem-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TDataInput }
     *     
     */
    public TDataInput getInputDataItem() {
        return inputDataItem;
    }

    /**
     * Legt den Wert der inputDataItem-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TDataInput }
     *     
     */
    public void setInputDataItem(TDataInput value) {
        this.inputDataItem = value;
    }

    /**
     * Ruft den Wert der outputDataItem-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TDataOutput }
     *     
     */
    public TDataOutput getOutputDataItem() {
        return outputDataItem;
    }

    /**
     * Legt den Wert der outputDataItem-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TDataOutput }
     *     
     */
    public void setOutputDataItem(TDataOutput value) {
        this.outputDataItem = value;
    }

    /**
     * Gets the value of the complexBehaviorDefinition property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the complexBehaviorDefinition property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getComplexBehaviorDefinition().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TComplexBehaviorDefinition }
     * 
     * 
     */
    public List<TComplexBehaviorDefinition> getComplexBehaviorDefinition() {
        if (complexBehaviorDefinition == null) {
            complexBehaviorDefinition = new ArrayList<TComplexBehaviorDefinition>();
        }
        return this.complexBehaviorDefinition;
    }

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
     * Ruft den Wert der isSequential-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isIsSequential() {
        if (isSequential == null) {
            return false;
        } else {
            return isSequential;
        }
    }

    /**
     * Legt den Wert der isSequential-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsSequential(Boolean value) {
        this.isSequential = value;
    }

    /**
     * Ruft den Wert der behavior-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TMultiInstanceFlowCondition }
     *     
     */
    public TMultiInstanceFlowCondition getBehavior() {
        if (behavior == null) {
            return TMultiInstanceFlowCondition.ALL;
        } else {
            return behavior;
        }
    }

    /**
     * Legt den Wert der behavior-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TMultiInstanceFlowCondition }
     *     
     */
    public void setBehavior(TMultiInstanceFlowCondition value) {
        this.behavior = value;
    }

    /**
     * Ruft den Wert der oneBehaviorEventRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getOneBehaviorEventRef() {
        return oneBehaviorEventRef;
    }

    /**
     * Legt den Wert der oneBehaviorEventRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setOneBehaviorEventRef(QName value) {
        this.oneBehaviorEventRef = value;
    }

    /**
     * Ruft den Wert der noneBehaviorEventRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getNoneBehaviorEventRef() {
        return noneBehaviorEventRef;
    }

    /**
     * Legt den Wert der noneBehaviorEventRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setNoneBehaviorEventRef(QName value) {
        this.noneBehaviorEventRef = value;
    }

}
