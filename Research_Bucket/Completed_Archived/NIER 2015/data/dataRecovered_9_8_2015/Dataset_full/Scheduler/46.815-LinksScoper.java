/* LinksScoper
 * 
 * $Id: LinksScoper.java,v 1.10 2006/09/26 23:47:15 paul_jack Exp $
 *
 * Created on Oct 2, 2003
 * 
 * Copyright (C) 2003 Internet Archive.
 *
 * This file is part of the Heritrix web crawler (crawler.archive.org).
 *
 * Heritrix is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * any later version.
 *
 * Heritrix is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser Public License for more details.
 *
 * You should have received a copy of the GNU Lesser Public License
 * along with Heritrix; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package org.archive.crawler.postprocessor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.httpclient.URIException;
import org.archive.crawler.datamodel.CandidateURI;
import org.archive.crawler.datamodel.CrawlURI;
import org.archive.crawler.datamodel.FetchStatusCodes;
import org.archive.crawler.extractor.Link;
import org.archive.crawler.framework.Filter;
import org.archive.crawler.framework.Scoper;
import org.archive.crawler.settings.MapType;
import org.archive.crawler.settings.SimpleType;
import org.archive.crawler.settings.Type;

/**
 * Determine which extracted links are within scope.
 * TODO: To test scope, requires that Link be converted to
 * a CandidateURI.  Make it so don't have to make a CandidateURI to test
 * if Link is in scope.
 * <p>Since this scoper has to create CandidateURIs, no sense
 * discarding them since later in the processing chain CandidateURIs rather
 * than Links are whats needed scheduling extracted links w/ the
 * Frontier (Frontier#schedule expects CandidateURI, not Link).  This class
 * replaces Links w/ the CandidateURI that wraps the Link in the CrawlURI.
 *
 * @author gojomo
 * @author stack
 */
public class LinksScoper extends Scoper
implements FetchStatusCodes {

    private static final long serialVersionUID = -4074442117992496793L;

    private static Logger LOGGER =
        Logger.getLogger(LinksScoper.class.getName());

    private final static String ATTR_SEED_REDIRECTS_NEW_SEEDS =
        "seed-redirects-new-seed";
    
    private final static Boolean DEFAULT_SEED_REDIRECTS_NEW_SEEDS =
        new Boolean(true);
    
    public static final String ATTR_LOG_REJECT_FILTERS =
        "scope-rejected-url-filters";
    
    public static final String ATTR_PREFERENCE_DEPTH_HOPS =
        "preference-depth-hops";

    private final static Integer DEFAULT_PREFERENCE_DEPTH_HOPS =
        new Integer(-1);
    
    /**
     * Instance of rejected uris log filters.
     */
    private MapType rejectLogFilters = null;
    
    /**
     * @param name Name of this filter.
     */
    public LinksScoper(String name) {
        super(name, "LinksScoper. Rules on which extracted links " +
            "are within configured scope.");
        
        Type t;
        t = addElementToDefinition(
            new SimpleType(ATTR_SEED_REDIRECTS_NEW_SEEDS,
            "If enabled, any URL found because a seed redirected to it " +
            "(original seed returned 301 or 302), will also be treated " +
            "as a seed.", DEFAULT_SEED_REDIRECTS_NEW_SEEDS));
        t.setExpertSetting(true);

        t = addElementToDefinition(new SimpleType(ATTR_PREFERENCE_DEPTH_HOPS,
            "Number of hops (of any sort) from a seed up to which a URI has higher " +
        "priority scheduling than any remaining seed. For example, if set to 1 items one " + 
        "hop (link, embed, redirect, etc.) away from a seed will be scheduled " + 
        "with HIGH priority. If set to -1, no " + 
        "preferencing will occur, and a breadth-first search with seeds " + 
        "processed before discovered links will proceed. If set to zero, a " + 
        "purely depth-first search will proceed, with all discovered links processed " + 
        "before remaining seeds.  Seed redirects are treated as one hop from a seed.",
        DEFAULT_PREFERENCE_DEPTH_HOPS));
        t.setExpertSetting(true);
        
        this.rejectLogFilters = (MapType)addElementToDefinition(
            new MapType(ATTR_LOG_REJECT_FILTERS, "Filters applied after " +
               "an URI has been rejected. If filter return " +
               "TRUE, the URI is logged (if the logging level is INFO). " +
               "Depends on " + ATTR_OVERRIDE_LOGGER_ENABLED +
               " being enabled.", Filter.class));
        this.rejectLogFilters.setExpertSetting(true);
    }

    protected void innerProcess(final CrawlURI curi) {
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(getName() + " processing " + curi);
        }
        
        // If prerequisites, nothing to be done in here.
        if (curi.hasPrerequisiteUri()) {
            handlePrerequisite(curi);
            return;
        }
        
        // Don't extract links of error pages.
        if (curi.getFetchStatus() < 200 || curi.getFetchStatus() >= 400) {
            curi.clearOutlinks();
            return;
        }
        
        if (curi.outlinksSize() <= 0) {
            // No outlinks to process.
            return;
        }

        final boolean redirectsNewSeeds = ((Boolean)getUncheckedAttribute(curi,
            ATTR_SEED_REDIRECTS_NEW_SEEDS)).booleanValue();
        int preferenceDepthHops = ((Integer)getUncheckedAttribute(curi,
            ATTR_PREFERENCE_DEPTH_HOPS)).intValue();
        Collection<CandidateURI> inScopeLinks = new HashSet<CandidateURI>();
        for (final Iterator i = curi.getOutObjects().iterator(); i.hasNext();) {
            Object o = i.next();
            if(o instanceof Link){
                final Link wref = (Link)o;
                try {
                    final int directive = getSchedulingFor(curi, wref, 
                        preferenceDepthHops);
                    final CandidateURI caURI =
                        curi.createCandidateURI(curi.getBaseURI(), wref, 
                            directive, 
                            considerAsSeed(curi, wref, redirectsNewSeeds));
                    if (isInScope(caURI)) {
                        inScopeLinks.add(caURI);
                    }
                } catch (URIException e) {
                    getController().logUriError(e, curi.getUURI(), 
                        wref.getDestination().toString());
                }
            } else if(o instanceof CandidateURI){
                CandidateURI caURI = (CandidateURI)o;
                if(isInScope(caURI)){
                    inScopeLinks.add(caURI);
                }
            } else {
                LOGGER.severe("Unexpected type: " + o);
            }
        }
        // Replace current links collection w/ inscopeLinks.  May be
        // an empty collection.
        curi.replaceOutlinks(inScopeLinks);
    }
    
    /**
     * The CrawlURI has a prerequisite; apply scoping and update
     * Link to CandidateURI in manner analogous to outlink handling. 
     * @param curi CrawlURI with prereq to consider
     */
    protected void handlePrerequisite(CrawlURI curi) {
        try {
            // Create prerequisite CandidateURI
            CandidateURI caUri =
                curi.createCandidateURI(curi.getBaseURI(),
                    (Link) curi.getPrerequisiteUri());
            int prereqPriority = curi.getSchedulingDirective() - 1;
            if (prereqPriority < 0) {
                prereqPriority = 0;
                LOGGER.severe("Unable to promote prerequisite " + caUri +
                    " above " + curi);
            }
            caUri.setSchedulingDirective(prereqPriority);
            caUri.setForceFetch(true);
            if(isInScope(caUri)) {
                // replace link with CandidateURI
                curi.setPrerequisiteUri(caUri);
            } else {
                // prerequisite is out-of-scope; mark CrawlURI as error,
                // preventinting normal S_DEFERRED handling
                curi.setFetchStatus(S_PREREQUISITE_UNSCHEDULABLE_FAILURE);
            }
       } catch (URIException ex) {
            Object[] array = {curi, curi.getPrerequisiteUri()};
            getController().uriErrors.log(Level.INFO,ex.getMessage(), array);
        } catch (NumberFormatException e) {
            // UURI.createUURI will occasionally throw this error.
            Object[] array = {curi, curi.getPrerequisiteUri()};
            getController().uriErrors.log(Level.INFO,e.getMessage(), array);
        }
    }

    protected void outOfScope(CandidateURI caUri) {
        super.outOfScope(caUri);
        if (!LOGGER.isLoggable(Level.INFO)) {
            return;
        }
        // TODO: Fix filters so work on CandidateURI.
        CrawlURI curi = (caUri instanceof CrawlURI)?
            (CrawlURI)caUri:
            new CrawlURI(caUri.getUURI());
        if (filtersAccept(this.rejectLogFilters, curi)) {
            LOGGER.info(curi.getUURI().toString());
        }
    }
    
    private boolean considerAsSeed(final CrawlURI curi, final Link wref,
            final boolean redirectsNewSeeds) {
        // Check if this is a seed with a 301 or 302.
        if (curi.isSeed()
                && (curi.getFetchStatus() == 301 ||
                    curi.getFetchStatus() == 302)
                && wref.getHopType() == Link.REFER_HOP) {
            // Check if redirects from seeds should be treated as seeds.
            if (redirectsNewSeeds) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Determine scheduling for the  <code>curi</code>.
     * As with the LinksScoper in general, this only handles extracted links,
     * seeds do not pass through here, but are given MEDIUM priority.  
     * Imports into the frontier similarly do not pass through here, 
     * but are given NORMAL priority.
     */
    protected int getSchedulingFor(final CrawlURI curi, final Link wref,
            final int preferenceDepthHops) {
        final char c = wref.getHopType();
        if (LOGGER.isLoggable(Level.FINEST)) {
            LOGGER.finest(curi + " with path=" + curi.getPathFromSeed() +
                " isSeed=" + curi.isSeed() + " with fetchStatus=" +
                curi.getFetchStatus() + " -> " + wref.getDestination() +
                " type " + c + " with context=" + wref.getContext());
        }

        switch (c) {
            case Link.REFER_HOP:
                // Treat redirects somewhat urgently
                // This also ensures seed redirects remain seed priority
                return (preferenceDepthHops >= 0 ? CandidateURI.HIGH :
                    CandidateURI.MEDIUM);
            default:
                if (preferenceDepthHops == 0)
                    return CandidateURI.HIGH;
                    // this implies seed redirects are treated as path
                    // length 1, which I belive is standard.
                    // curi.getPathFromSeed() can never be null here, because
                    // we're processing a link extracted from curi
                if (preferenceDepthHops > 0 && 
                    curi.getPathFromSeed().length() + 1 <= preferenceDepthHops)
                    return CandidateURI.HIGH;
                // Everything else normal (at least for now)
                return CandidateURI.NORMAL;
        }
    }
}
