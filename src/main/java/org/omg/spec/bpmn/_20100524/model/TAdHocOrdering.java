//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Aenderung an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren.
// Generiert: 2023.05.14 um 03:43:47 PM CEST 
//


package org.omg.spec.bpmn._20100524.model;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse f�r tAdHocOrdering.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;simpleType name="tAdHocOrdering">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="Parallel"/>
 *     &lt;enumeration value="Sequential"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "tAdHocOrdering")
@XmlEnum
public enum TAdHocOrdering {

    @XmlEnumValue("Parallel")
    PARALLEL("Parallel"),
    @XmlEnumValue("Sequential")
    SEQUENTIAL("Sequential");
    private final String value;

    TAdHocOrdering(String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    public static TAdHocOrdering fromValue(String v) {
        for (TAdHocOrdering c: TAdHocOrdering.values()) {
            if (c.value.equals(v)) {
                return c;
            }
        }
        throw new IllegalArgumentException(v);
    }

}
