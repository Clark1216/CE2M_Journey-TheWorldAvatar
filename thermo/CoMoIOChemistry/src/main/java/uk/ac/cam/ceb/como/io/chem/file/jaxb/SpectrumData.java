//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.05.10 at 04:17:37 PM BST 
//


package uk.ac.cam.ceb.como.io.chem.file.jaxb;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;sequence minOccurs="0">
 *           &lt;element ref="{http://www.xml-cml.org/schema}xaxis"/>
 *           &lt;element ref="{http://www.xml-cml.org/schema}yaxis" maxOccurs="unbounded"/>
 *         &lt;/sequence>
 *       &lt;/sequence>
 *       &lt;attGroup ref="{http://www.xml-cml.org/schema}dictRef"/>
 *       &lt;attGroup ref="{http://www.xml-cml.org/schema}ref"/>
 *       &lt;attGroup ref="{http://www.xml-cml.org/schema}id"/>
 *       &lt;attGroup ref="{http://www.xml-cml.org/schema}convention"/>
 *       &lt;attGroup ref="{http://www.xml-cml.org/schema}title"/>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "xaxis",
    "yaxis"
})
@XmlRootElement(name = "spectrumData")
public class SpectrumData {

    protected Xaxis xaxis;
    protected List<Yaxis> yaxis;
    @XmlAttribute(name = "dictRef")
    protected java.lang.String dictRef;
    @XmlAttribute(name = "ref")
    protected java.lang.String ref;
    @XmlAttribute(name = "id")
    protected java.lang.String id;
    @XmlAttribute(name = "convention")
    protected java.lang.String convention;
    @XmlAttribute(name = "title")
    protected java.lang.String title;

    /**
     * 
     *                             
     * <pre>
     * &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;h:div xmlns:h="http://www.w3.org/1999/xhtml"&gt;The x-axis/es, usually including the list of points at which data are recorded. Mandatory if y-axis data are given.  Multiple x-axes are initially reserved for multiple scales rather than different measurements (for which an additional spectrum should be used).&lt;/h:div&gt;
     * </pre>
     * 
     *                         
     * 
     * @return
     *     possible object is
     *     {@link Xaxis }
     *     
     */
    public Xaxis getXaxis() {
        return xaxis;
    }

    /**
     * Sets the value of the xaxis property.
     * 
     * @param value
     *     allowed object is
     *     {@link Xaxis }
     *     
     */
    public void setXaxis(Xaxis value) {
        this.xaxis = value;
    }

    /**
     * 
     *                             
     * <pre>
     * &lt;?xml version="1.0" encoding="UTF-8"?&gt;&lt;h:div xmlns:h="http://www.w3.org/1999/xhtml"&gt;The y-axis/es, usually including the list of points at which data are recorded. Mandatory if x-axis data are given. Multiple y-axes are initially reserved for multiple scales rather than different measurements (for which an additional spectrum should be used).&lt;/h:div&gt;
     * </pre>
     * 
     *                         Gets the value of the yaxis property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the yaxis property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getYaxis().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Yaxis }
     * 
     * 
     */
    public List<Yaxis> getYaxis() {
        if (yaxis == null) {
            yaxis = new ArrayList<Yaxis>();
        }
        return this.yaxis;
    }

    /**
     * Gets the value of the dictRef property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getDictRef() {
        return dictRef;
    }

    /**
     * Sets the value of the dictRef property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setDictRef(java.lang.String value) {
        this.dictRef = value;
    }

    /**
     * Gets the value of the ref property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getRef() {
        return ref;
    }

    /**
     * Sets the value of the ref property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setRef(java.lang.String value) {
        this.ref = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setId(java.lang.String value) {
        this.id = value;
    }

    /**
     * Gets the value of the convention property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getConvention() {
        return convention;
    }

    /**
     * Sets the value of the convention property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setConvention(java.lang.String value) {
        this.convention = value;
    }

    /**
     * Gets the value of the title property.
     * 
     * @return
     *     possible object is
     *     {@link java.lang.String }
     *     
     */
    public java.lang.String getTitle() {
        return title;
    }

    /**
     * Sets the value of the title property.
     * 
     * @param value
     *     allowed object is
     *     {@link java.lang.String }
     *     
     */
    public void setTitle(java.lang.String value) {
        this.title = value;
    }

}
