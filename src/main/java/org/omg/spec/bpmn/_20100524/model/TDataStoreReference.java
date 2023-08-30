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
 * <p>Java-Klasse für tDataStoreReference complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="tDataStoreReference">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/spec/BPMN/20100524/MODEL}tFlowElement">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.omg.org/spec/BPMN/20100524/MODEL}dataState" minOccurs="0"/>
 *       &lt;/sequence>
 *       &lt;attribute name="itemSubjectRef" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *       &lt;attribute name="dataStoreRef" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tDataStoreReference", propOrder = {
    "dataState"
})
public class TDataStoreReference
    extends TFlowElement
{

    protected TDataState dataState;
    @XmlAttribute(name = "itemSubjectRef")
    protected QName itemSubjectRef;
    @XmlAttribute(name = "dataStoreRef")
    protected QName dataStoreRef;

    /**
     * Ruft den Wert der dataState-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link TDataState }
     *     
     */
    public TDataState getDataState() {
        return dataState;
    }

    /**
     * Legt den Wert der dataState-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link TDataState }
     *     
     */
    public void setDataState(TDataState value) {
        this.dataState = value;
    }

    /**
     * Ruft den Wert der itemSubjectRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getItemSubjectRef() {
        return itemSubjectRef;
    }

    /**
     * Legt den Wert der itemSubjectRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setItemSubjectRef(QName value) {
        this.itemSubjectRef = value;
    }

    /**
     * Ruft den Wert der dataStoreRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link QName }
     *     
     */
    public QName getDataStoreRef() {
        return dataStoreRef;
    }

    /**
     * Legt den Wert der dataStoreRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link QName }
     *     
     */
    public void setDataStoreRef(QName value) {
        this.dataStoreRef = value;
    }

}
