//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Aenderung an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2023.05.14 um 03:43:47 PM CEST 
//


package org.omg.spec.dd._20100524.di;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import org.omg.spec.dd._20100524.dc.Bounds;


/**
 * <p>Java-Klasse f�r Shape complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Shape">
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.omg.org/spec/DD/20100524/DI}Node">
 *       &lt;sequence>
 *         &lt;element ref="{http://www.omg.org/spec/DD/20100524/DC}Bounds"/>
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
@XmlType(name = "Shape", propOrder = {
    "bounds"
})
@XmlSeeAlso({
    LabeledShape.class
})
public abstract class Shape
    extends Node
{

    @XmlElement(name = "Bounds", namespace = "http://www.omg.org/spec/DD/20100524/DC", required = true)
    protected Bounds bounds;

    /**
     * Ruft den Wert der bounds-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Bounds }
     *     
     */
    public Bounds getBounds() {
        return bounds;
    }

    /**
     * Legt den Wert der bounds-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Bounds }
     *     
     */
    public void setBounds(Bounds value) {
        this.bounds = value;
    }

}
