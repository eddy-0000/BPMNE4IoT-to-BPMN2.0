//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// �nderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2023.05.14 um 03:43:47 PM CEST 
//


package org.omg.spec.bpmn._20100524.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;


/**
 * <p>Java-Klasse f�r tInputOutputBinding complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="tInputOutputBinding">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/spec/BPMN/20100524/MODEL}tBaseElement">
 *       &lt;attribute name="operationRef" use="required" type="{http://www.w3.org/2001/XMLSchema}QName" />
 *       &lt;attribute name="inputDataRef" use="required" type="{http://www.w3.org/2001/XMLSchema}IDREF" />
 *       &lt;attribute name="outputDataRef" use="required" type="{http://www.w3.org/2001/XMLSchema}IDREF" />
 *       &lt;anyAttribute processContents='lax' namespace='##other'/>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "tInputOutputBinding")
public class TInputOutputBinding
    extends TBaseElement
{

    @XmlAttribute(name = "operationRef", required = true)
    protected QName operationRef;
    @XmlAttribute(name = "inputDataRef", required = true)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected Object inputDataRef;
    @XmlAttribute(name = "outputDataRef", required = true)
    @XmlIDREF
    @XmlSchemaType(name = "IDREF")
    protected Object outputDataRef;

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

    /**
     * Ruft den Wert der inputDataRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getInputDataRef() {
        return inputDataRef;
    }

    /**
     * Legt den Wert der inputDataRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setInputDataRef(Object value) {
        this.inputDataRef = value;
    }

    /**
     * Ruft den Wert der outputDataRef-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Object }
     *     
     */
    public Object getOutputDataRef() {
        return outputDataRef;
    }

    /**
     * Legt den Wert der outputDataRef-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Object }
     *     
     */
    public void setOutputDataRef(Object value) {
        this.outputDataRef = value;
    }

}
