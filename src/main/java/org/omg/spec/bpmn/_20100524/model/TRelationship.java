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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


/**
 * <p>Java-Klasse f�r tRelationship complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="tRelationship">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/spec/BPMN/20100524/MODEL}tBaseElement">
 *       &lt;sequence>
 *         &lt;element name="source" type="{http://www.w3.org/2001/XMLSchema}QName" maxOccurs="unbounded"/>
 *         &lt;element name="target" type="{http://www.w3.org/2001/XMLSchema}QName" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *       &lt;attribute name="type" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="direction" type="{http://www.omg.org/spec/BPMN/20100524/MODEL}tRelationshipDirection" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tRelationship", propOrder = {
    "source",
    "target"
})
public class TRelationship
    extends TBaseElement
{

    @XmlElement(required = true)
    protected List<QName> source;
    @XmlElement(required = true)
    protected List<QName> target;
    @XmlAttribute(name = "type", required = true)
    protected String type;
    @XmlAttribute(name = "direction")
    protected TRelationshipDirection direction;

    /**
     * Gets the value of the source property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the source property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getSource().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link QName }
     * 
     * 
     */
    public List<QName> getSource() {
        if (source == null) {
            source = new ArrayList<QName>();
        }
        return this.source;
    }

    /**
     * Gets the value of the target property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the target property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTarget().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link QName }
     * 
     * 
     */
    public List<QName> getTarget() {
        if (target == null) {
            target = new ArrayList<QName>();
        }
        return this.target;
    }

    /**
     * Ruft den Wert der type-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getType() {
        return type;
    }

    /**
     * Legt den Wert der type-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setType(String value) {
        this.type = value;
    }

    /**
     * Ruft den Wert der direction-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TRelationshipDirection }
     *     
     */
    public TRelationshipDirection getDirection() {
        return direction;
    }

    /**
     * Legt den Wert der direction-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TRelationshipDirection }
     *     
     */
    public void setDirection(TRelationshipDirection value) {
        this.direction = value;
    }

}
