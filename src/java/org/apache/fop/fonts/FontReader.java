/*
 * Copyright 1999-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/* $Id$ */

package org.apache.fop.fonts;

//Java
import java.util.List;
import java.util.Map;
import java.io.IOException;

import javax.xml.parsers.SAXParserFactory;

//SAX
import org.xml.sax.XMLReader;
import org.xml.sax.SAXException;
import org.xml.sax.Locator;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

//FOP
import org.apache.fop.apps.FOPException;

/**
 * Class for reading a metric.xml file and creating a font object.
 * Typical usage:
 * <pre>
 * FontReader reader = new FontReader(<path til metrics.xml>);
 * reader.setFontEmbedPath(<path to a .ttf or .pfb file or null to diable embedding>);
 * reader.useKerning(true);
 * Font f = reader.getFont();
 * </pre>
 */
public class FontReader extends DefaultHandler {

    private Locator locator = null;
    private boolean isCID = false;
    private CustomFont returnFont = null;
    private MultiByteFont multiFont = null;
    private SingleByteFont singleFont = null;
    private StringBuffer text = new StringBuffer();

    private List cidWidths = null;
    private int cidWidthIndex = 0;

    private Map currentKerning = null;

    private List bfranges = null;

    private void createFont(String path) throws FOPException {
        XMLReader parser = null;

        try {
            final SAXParserFactory factory = javax.xml.parsers.SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            parser = factory.newSAXParser().getXMLReader();
        } catch (Exception e) {
            throw new FOPException(e);
        }
        if (parser == null) {
            throw new FOPException("Unable to create SAX parser");
        }

        try {
            parser.setFeature("http://xml.org/sax/features/namespace-prefixes",
                              false);
        } catch (SAXException e) {
            throw new FOPException("You need a SAX parser which supports SAX version 2",
                                   e);
        }

        parser.setContentHandler(this);

        try {
            parser.parse(path);
        } catch (SAXException e) {
            throw new FOPException(e);
        } catch (IOException e) {
            throw new FOPException(e);
        }

    }

    /**
     * Sets the path to embed a font. A null value disables font embedding.
     * @param path URI for the embeddable file
     */
    public void setFontEmbedPath(String path) {
        returnFont.setEmbedFileName(path);
    }

    /**
     * Enable/disable use of kerning for the font
     * @param enabled true to enable kerning, false to disable
     */
    public void setKerningEnabled(boolean enabled) {
        returnFont.setKerningEnabled(enabled);
    }


    /**
     * Get the generated font object
     * @return the font
     */
    public Typeface getFont() {
        return returnFont;
    }

    /**
     * Construct a FontReader object from a path to a metric.xml file
     * and read metric data
     * @param path URI to the font metric file
     * @throws FOPException if loading the font fails
     */
    public FontReader(String path) throws FOPException {
        createFont(path);
    }

    /**
     * @see org.xml.sax.ContentHandler#startDocument()
     */
    public void startDocument() {
    }

    /**
     * @see org.xml.sax.ContentHandler#setDocumentLocator(Locator)
     */
    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
    }

    /**
     * @see org.xml.sax.ContentHandler#startElement(String, String, String, Attributes)
     */
    public void startElement(String uri, String localName, String qName,
                             Attributes attributes) {
        if (localName.equals("font-metrics")) {
            if ("TYPE0".equals(attributes.getValue("type"))) {
                multiFont = new MultiByteFont();
                returnFont = multiFont;
                isCID = true;
            } else if ("TRUETYPE".equals(attributes.getValue("type"))) {
                singleFont = new SingleByteFont();
                singleFont.setFontType(FontType.TRUETYPE);
                returnFont = singleFont;
                isCID = false;
            } else {
                singleFont = new SingleByteFont();
                singleFont.setFontType(FontType.TYPE1);
                returnFont = singleFont;
                isCID = false;
            }
        } else if ("embed".equals(localName)) {
            returnFont.setEmbedFileName(attributes.getValue("file"));
            returnFont.setEmbedResourceName(attributes.getValue("class"));
        } else if ("cid-widths".equals(localName)) {
            cidWidthIndex = getInt(attributes.getValue("start-index"));
            cidWidths = new java.util.ArrayList();
        } else if ("kerning".equals(localName)) {
            currentKerning = new java.util.HashMap();
            returnFont.putKerningEntry(new Integer(attributes.getValue("kpx1")),
                                        currentKerning);
        } else if ("bfranges".equals(localName)) {
            bfranges = new java.util.ArrayList();
        } else if ("bf".equals(localName)) {
            BFEntry entry = new BFEntry(getInt(attributes.getValue("us")),
                                        getInt(attributes.getValue("ue")),
                                        getInt(attributes.getValue("gi")));
            bfranges.add(entry);
        } else if ("wx".equals(localName)) {
            cidWidths.add(new Integer(attributes.getValue("w")));
        } else if ("widths".equals(localName)) {
            //singleFont.width = new int[256];
        } else if ("char".equals(localName)) {
            try {
                singleFont.setWidth(Integer.parseInt(attributes.getValue("idx")),
                        Integer.parseInt(attributes.getValue("wdt")));
            } catch (NumberFormatException ne) {
                System.out.println("Malformed width in metric file: "
                                   + ne.getMessage());
            }
        } else if ("pair".equals(localName)) {
            currentKerning.put(new Integer(attributes.getValue("kpx2")),
                               new Integer(attributes.getValue("kern")));
        }
    }

    private int getInt(String str) {
        int ret = 0;
        try {
            ret = Integer.parseInt(str);
        } catch (Exception e) {
            /**@todo log this exception */
        }
        return ret;
    }

    /**
     * @see org.xml.sax.ContentHandler#endElement(String, String, String)
     */
    public void endElement(String uri, String localName, String qName) {
        if ("font-name".equals(localName)) {
            returnFont.setFontName(text.toString());
        } else if ("ttc-name".equals(localName) && isCID) {
            multiFont.setTTCName(text.toString());
        } else if ("cap-height".equals(localName)) {
            returnFont.setCapHeight(getInt(text.toString()));
        } else if ("x-height".equals(localName)) {
            returnFont.setXHeight(getInt(text.toString()));
        } else if ("ascender".equals(localName)) {
            returnFont.setAscender(getInt(text.toString()));
        } else if ("descender".equals(localName)) {
            returnFont.setDescender(getInt(text.toString()));
        } else if ("left".equals(localName)) {
            int[] bbox = returnFont.getFontBBox();
            bbox[0] = getInt(text.toString());
            returnFont.setFontBBox(bbox);
        } else if ("bottom".equals(localName)) {
            int[] bbox = returnFont.getFontBBox();
            bbox[1] = getInt(text.toString());
            returnFont.setFontBBox(bbox);
        } else if ("right".equals(localName)) {
            int[] bbox = returnFont.getFontBBox();
            bbox[2] = getInt(text.toString());
            returnFont.setFontBBox(bbox);
        } else if ("top".equals(localName)) {
            int[] bbox = returnFont.getFontBBox();
            bbox[3] = getInt(text.toString());
            returnFont.setFontBBox(bbox);
        } else if ("first-char".equals(localName)) {
            returnFont.setFirstChar(getInt(text.toString()));
        } else if ("last-char".equals(localName)) {
            returnFont.setLastChar(getInt(text.toString()));
        } else if ("flags".equals(localName)) {
            returnFont.setFlags(getInt(text.toString()));
        } else if ("stemv".equals(localName)) {
            returnFont.setStemV(getInt(text.toString()));
        } else if ("italic-angle".equals(localName)) {
            returnFont.setItalicAngle(getInt(text.toString()));
        } else if ("missing-width".equals(localName)) {
            returnFont.setMissingWidth(getInt(text.toString()));
        } else if ("cid-type".equals(localName)) {
            multiFont.setCIDType(CIDFontType.byName(text.toString()));
        } else if ("default-width".equals(localName)) {
            multiFont.setDefaultWidth(getInt(text.toString()));
        } else if ("cid-widths".equals(localName)) {
            int[] wds = new int[cidWidths.size()];
            int j = 0;
            for (int count = 0; count < cidWidths.size(); count++) {
                Integer i = (Integer)cidWidths.get(count);
                wds[j++] = i.intValue();
            }

            //multiFont.addCIDWidthEntry(cidWidthIndex, wds);
            multiFont.setWidthArray(wds);

        } else if ("bfranges".equals(localName)) {
            multiFont.setBFEntries((BFEntry[])bfranges.toArray(new BFEntry[0]));
        }
        text.setLength(0); //Reset text buffer (see characters())
    }

    /**
     * @see org.xml.sax.ContentHandler#characters(char[], int, int)
     */
    public void characters(char[] ch, int start, int length) {
        text.append(ch, start, length);
    }

}


