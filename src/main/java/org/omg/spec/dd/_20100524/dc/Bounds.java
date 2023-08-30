//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2023.05.14 um 03:43:47 PM CEST 
//


package org.omg.spec.dd._20100524.dc;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für Bounds complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="Bounds">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute name="x" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="y" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="width" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *       &lt;attribute name="height" use="required" type="{http://www.w3.org/2001/XMLSchema}double" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Bounds")
public class Bounds {

    @XmlAttribute(name = "x", required = true)
    protected double x;
    @XmlAttribute(name = "y", required = true)
    protected double y;
    @XmlAttribute(name = "width", required = true)
    protected double width;
    @XmlAttribute(name = "height", required = true)
    protected double height;

    /**
     * Ruft den Wert der x-Eigenschaft ab.
     * 
     */
    public double getX() {
        return x;
    }

    /**
     * Legt den Wert der x-Eigenschaft fest.
     * 
     */
    public void setX(double value) {
        this.x = value;
    }

    /**
     * Ruft den Wert der y-Eigenschaft ab.
     * 
     */
    public double getY() {
        return y;
    }

    /**
     * Legt den Wert der y-Eigenschaft fest.
     * 
     */
    public void setY(double value) {
        this.y = value;
    }

    /**
     * Ruft den Wert der width-Eigenschaft ab.
     * 
     */
    public double getWidth() {
        return width;
    }

    /**
     * Legt den Wert der width-Eigenschaft fest.
     * 
     */
    public void setWidth(double value) {
        this.width = value;
    }

    /**
     * Ruft den Wert der height-Eigenschaft ab.
     * 
     */
    public double getHeight() {
        return height;
    }

    /**
     * Legt den Wert der height-Eigenschaft fest.
     * 
     */
    public void setHeight(double value) {
        this.height = value;
    }

}
