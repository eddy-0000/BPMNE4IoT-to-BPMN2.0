//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2023.05.14 um 03:43:47 PM CEST 
//


package org.omg.spec.bpmn._20100524.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


/**
 * <p>Java-Klasse für tItemDefinition complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="tItemDefinition">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/spec/BPMN/20100524/MODEL}tRootElement">
 *       &lt;attribute name="structureRef" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *       &lt;attribute name="isCollection" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *       &lt;attribute name="itemKind" type="{http://www.omg.org/spec/BPMN/20100524/MODEL}tItemKind" default="Information" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tItemDefinition")
public class TItemDefinition
    extends TRootElement
{

    @XmlAttribute(name = "structureRef")
    protected QName structureRef;
    @XmlAttribute(name = "isCollection")
    protected Boolean isCollection;
    @XmlAttribute(name = "itemKind")
    protected TItemKind itemKind;

    /**
     * Ruft den Wert der structureRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getStructureRef() {
        return structureRef;
    }

    /**
     * Legt den Wert der structureRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setStructureRef(QName value) {
        this.structureRef = value;
    }

    /**
     * Ruft den Wert der isCollection-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isIsCollection() {
        if (isCollection == null) {
            return false;
        } else {
            return isCollection;
        }
    }

    /**
     * Legt den Wert der isCollection-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setIsCollection(Boolean value) {
        this.isCollection = value;
    }

    /**
     * Ruft den Wert der itemKind-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TItemKind }
     *     
     */
    public TItemKind getItemKind() {
        if (itemKind == null) {
            return TItemKind.INFORMATION;
        } else {
            return itemKind;
        }
    }

    /**
     * Legt den Wert der itemKind-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TItemKind }
     *     
     */
    public void setItemKind(TItemKind value) {
        this.itemKind = value;
    }

}
