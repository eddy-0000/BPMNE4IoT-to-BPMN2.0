//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Aenderung an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren.
// Generiert: 2023.05.14 um 03:43:47 PM CEST 
//


package org.omg.spec.bpmn._20100524.model;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


/**
 * <p>Java-Klasse f�r tFlowElement complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="tFlowElement">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/spec/BPMN/20100524/MODEL}tBaseElement">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.omg.org/spec/BPMN/20100524/MODEL}auditing" minOccurs="0"/>
 *         &lt;element ref="{http://www.omg.org/spec/BPMN/20100524/MODEL}monitoring" minOccurs="0"/>
 *         &lt;element name="categoryValueRef" type="{http://www.w3.org/2001/XMLSchema}QName" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tFlowElement", propOrder = {
    "auditing",
    "monitoring",
    "categoryValueRef"
})
@XmlSeeAlso({
    TSequenceFlow.class,
    TDataStoreReference.class,
    TDataObject.class,
    TDataObjectReference.class,
    TFlowNode.class
})
public abstract class TFlowElement
    extends TBaseElement
{

    protected TAuditing auditing;
    protected TMonitoring monitoring;
    protected List<QName> categoryValueRef;
    @XmlAttribute(name = "name")
    protected String name;

    /**
     * Ruft den Wert der auditing-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TAuditing }
     *     
     */
    public TAuditing getAuditing() {
        return auditing;
    }

    /**
     * Legt den Wert der auditing-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TAuditing }
     *     
     */
    public void setAuditing(TAuditing value) {
        this.auditing = value;
    }

    /**
     * Ruft den Wert der monitoring-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TMonitoring }
     *     
     */
    public TMonitoring getMonitoring() {
        return monitoring;
    }

    /**
     * Legt den Wert der monitoring-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TMonitoring }
     *     
     */
    public void setMonitoring(TMonitoring value) {
        this.monitoring = value;
    }

    /**
     * Gets the value of the categoryValueRef property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the categoryValueRef property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCategoryValueRef().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link QName }
     * 
     * 
     */
    public List<QName> getCategoryValueRef() {
        if (categoryValueRef == null) {
            categoryValueRef = new ArrayList<QName>();
        }
        return this.categoryValueRef;
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

}
