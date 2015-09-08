/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.components.serializers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.avalon.excalibur.pool.Recyclable;
import org.apache.avalon.framework.configuration.Configurable;
import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.avalon.framework.parameters.Parameters;
import org.apache.cocoon.ProcessingException;
import org.apache.cocoon.environment.SourceResolver;
import org.apache.cocoon.environment.http.HttpEnvironment;
import org.apache.cocoon.serialization.Serializer;
import org.apache.cocoon.sitemap.SitemapModelComponent;
import org.xml.sax.SAXException;

/**
 * <p>A serializer converting XHTML into plain old HTML.</p>
 *
 * <p>For configuration options of this serializer, please look at the
 * {@link XHTMLSerializer} and {@link EncodingSerializer}.</p>
 *
 * <p>Any of the XHTML document type declared or used will be converted into
 * its HTML 4.01 counterpart, and in addition to those a "compatible" doctype
 * can be supported to exploit a couple of shortcuts into MSIE's rendering
 * engine. The values for the <code>doctype-default</code> can then be:</p>
 *
 * <dl>
 *   <dt>"<code>none</code>"</dt>
 *   <dd>Not to emit any dococument type declaration.</dd>
 *   <dt>"<code>compatible</code>"</dt>
 *   <dd>The HTML 4.01 Transitional (exploiting MSIE shortcut).</dd>
 *   <dt>"<code>strict</code>"</dt>
 *   <dd>The HTML 4.01 Strict document type.</dd>
 *   <dt>"<code>loose</code>"</dt>
 *   <dd>The HTML 4.01 Transitional document type.</dd>
 *   <dt>"<code>frameset</code>"</dt>
 *   <dd>The HTML 4.01 Frameset document type.</dd>
 * </dl>
 *
 * @version $Id: HTMLSerializer.java 587752 2007-10-24 02:47:02Z vgritsenko $
 */
public class HTMLSerializer
    extends org.apache.cocoon.components.serializers.util.HTMLSerializer
    implements Serializer, SitemapModelComponent, Recyclable, Configurable  {

    /**
     * @see org.apache.cocoon.sitemap.SitemapModelComponent#setup(org.apache.cocoon.environment.SourceResolver, java.util.Map, java.lang.String, org.apache.avalon.framework.parameters.Parameters)
     */
    public void setup(SourceResolver resolver,
                      Map            objectModel,
                      String         src,
                      Parameters     par)
    throws ProcessingException, SAXException, IOException {
        this.setup((HttpServletRequest) objectModel.get(HttpEnvironment.HTTP_REQUEST_OBJECT));
    }


    /**
     * Test if the component wants to set the content length.
     */
    public boolean shouldSetContentLength() {
        return false;
    }

    /**
     * Set the configurations for this serializer.
     */
    public void configure(Configuration conf)
    throws ConfigurationException {
        String encoding = conf.getChild("encoding").getValue(null);
        try {
            this.setEncoding(encoding);
        } catch (UnsupportedEncodingException exception) {
            throw new ConfigurationException("Encoding not supported: "
                                             + encoding, exception);
        }

        this.setIndentPerLevel(conf.getChild("indent").getValueAsInteger(0));
        this.setDoctypeDefault(conf.getChild("doctype-default").getValue(null));
    }
}
/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.cocoon.serialization;

import org.apache.avalon.framework.configuration.Configuration;
import org.apache.avalon.framework.configuration.ConfigurationException;
import org.apache.cocoon.CascadingIOException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.OutputStream;

/**
 * The html serializer serializes sax events into an html document.

 * @cocoon.sitemap.component.documentation
 * The html serializer serializes sax events into an html document.
 * @cocoon.sitemap.component.name      html
 * @cocoon.sitemap.component.mimetype  text/html
 * @cocoon.sitemap.component.documentation.caching Yes
 * @cocoon.sitemap.component.pooling.max  32
 * 
 * @cocoon.sitemap.component.configuration
 * <doctype-public>-//W3C//DTD HTML 4.01 Transitional//EN</doctype-public>
 * <doctype-system>http://www.w3.org/TR/html4/loose.dtd</doctype-system>
 *
 * @version $Id: HTMLSerializer.java 605689 2007-12-19 20:48:43Z vgritsenko $
 */
public class HTMLSerializer extends AbstractTextSerializer {
    
    /* (non-Javadoc)
     * @see org.apache.cocoon.serialization.AbstractTextSerializer#init()
     */
    public void init() throws Exception {
        super.init();
        this.format.put(OutputKeys.METHOD,"html");        
    }

    /**
     * Set the configurations for this serializer.
     */
    public void configure(Configuration conf)
    throws ConfigurationException {
        super.configure(conf);
        this.format.put(OutputKeys.METHOD,"html");
    }

    /**
     * Set the {@link OutputStream} where the requested resource should
     * be serialized.
     */
    public void setOutputStream(OutputStream out) 
    throws IOException {
        super.setOutputStream(out);
        try {
            TransformerHandler handler = this.getTransformerHandler();
            handler.getTransformer().setOutputProperties(this.format);
            handler.setResult(new StreamResult(this.output));
            this.setContentHandler(handler);
            this.setLexicalHandler(handler);
        } catch (Exception e) {
            final String message = "Cannot set HTMLSerializer outputstream"; 
            throw new CascadingIOException(message, e);
        }
    }
}
