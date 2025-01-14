/* ====================================================================
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
==================================================================== */

package org.apache.poi.xdgf.usermodel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.microsoft.schemas.office.visio.x2012.main.PageContentsDocument;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.ooxml.POIXMLException;
import org.apache.poi.openxml4j.opc.PackagePart;
import org.apache.poi.xdgf.exceptions.XDGFException;
import org.apache.xmlbeans.XmlException;

public class XDGFPageContents extends XDGFBaseContents {

    protected Map<Long, XDGFMaster> _masters = new HashMap<>();
    protected XDGFPage _page;

    /**
     * @since POI 3.14-Beta1
     */
    public XDGFPageContents(PackagePart part) {
        super(part);
    }

    @Override
    protected void onDocumentRead() {
        try {
            try {
                _pageContents = PageContentsDocument.Factory.parse(getPackagePart().getInputStream()).getPageContents();
            } catch (XmlException | IOException e) {
                throw new POIXMLException(e);
            }

            for (POIXMLDocumentPart part: getRelations()) {
                if (!(part instanceof XDGFMasterContents))
                    continue;
                //throw new POIXMLException("Unexpected page relation: " + part);

                XDGFMaster master = ((XDGFMasterContents)part).getMaster();
                if (master == null) {
                    throw new POIXMLException("Master entry is missing in XDGFPageContents");
                }
                _masters.put(master.getID(), master);
            }

            super.onDocumentRead();

            for (XDGFShape shape: _shapes.values()) {
                if (shape.isTopmost())
                    shape.setupMaster(this, null);
            }

        } catch (POIXMLException e) {
            throw XDGFException.wrap(this, e);
        }
    }

    /**
     * @return Parent page
     */
    public XDGFPage getPage() {
        return _page;
    }

    protected void setPage(XDGFPage page) {
        _page = page;
    }

    public XDGFMaster getMasterById(long id) {
        return _masters.get(id);
    }
}
