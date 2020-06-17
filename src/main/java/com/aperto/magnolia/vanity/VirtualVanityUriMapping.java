package com.aperto.magnolia.vanity;

/*
 * #%L
 * magnolia-vanity-url Magnolia Module
 * %%
 * Copyright (C) 2013 - 2014 Aperto AG
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */


import info.magnolia.cms.beans.config.QueryAwareVirtualURIMapping;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.ModuleRegistry;
import info.magnolia.module.site.ExtendedAggregationState;
import info.magnolia.module.site.Site;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;

import java.util.Map;
import java.util.regex.PatternSyntaxException;

import static com.aperto.magnolia.vanity.VanityUrlService.DEF_SITE;
import static org.apache.commons.lang3.StringUtils.containsAny;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Virtual Uri Mapping of vanity URLs managed in the vanity url app.
 *
 * @author frank.sommer
 */
public class VirtualVanityUriMapping implements QueryAwareVirtualURIMapping {
    private static final Logger LOGGER = LoggerFactory.getLogger(VirtualVanityUriMapping.class);
    private Provider<VanityUrlModule> _vanityUrlModuleProvider;
    private Provider<VanityUrlService> _vanityUrlServiceProvider;
    private Provider<ModuleRegistry> _moduleRegistryProvider;


    @Inject
    public void setVanityUrlModule(final Provider<VanityUrlModule> vanityUrlModuleProvider) {
        _vanityUrlModuleProvider = vanityUrlModuleProvider;
    }

    @Inject
    public void setVanityUrlService(final Provider<VanityUrlService> vanityUrlServiceProvider) {
        _vanityUrlServiceProvider = vanityUrlServiceProvider;
    }

    @Inject
    public void setModuleRegistry(final Provider<ModuleRegistry> moduleRegistryProvider) {
        _moduleRegistryProvider = moduleRegistryProvider;
    }

    // CHECKSTYLE:OFF
    @Override
    public MappingResult mapURI(String uri) {
        // CHECKSTYLE:ON
        return mapURI(uri, null);
    }

    // CHECKSTYLE:OFF
    @Override
    public MappingResult mapURI(String uri, String queryString) {
        // CHECKSTYLE:ON
        MappingResult result = null;
        try {
            if (isVanityCandidate(uri)) {
                String toUri = getUriOfVanityUrl(uri);
                if (isNotBlank(toUri)) {
                    if (!containsAny(toUri, "?#") && isNotBlank(queryString)) {
                        toUri = toUri.concat("?" + queryString);
                    }
                    result = new MappingResult();
                    result.setToURI(toUri);
                    result.setLevel(uri.length());
                }
            }
        } catch (PatternSyntaxException e) {
            LOGGER.error("A vanity url exclude pattern is not set correctly.", e);
        }
        return result;
    }

    private boolean isVanityCandidate(String uri) {
        boolean contentUri = !isRootRequest(uri);
        if (contentUri) {
            Map<String, String> excludes = _vanityUrlModuleProvider.get().getExcludes();
            for (String exclude : excludes.values()) {
                if (isNotEmpty(exclude) && uri.matches(exclude)) {
                    contentUri = false;
                    break;
                }
            }
        }
        return contentUri;
    }

    private boolean isRootRequest(final String uri) {
        return uri.length() <= 1;
    }

    private String getUriOfVanityUrl(final String vanityUrl) {
        return _vanityUrlServiceProvider.get().createRedirectUrl(
                _vanityUrlServiceProvider.get().queryForVanityUrlNode(vanityUrl, retrieveSite()));
    }

    private String retrieveSite() {
        String siteName = DEF_SITE;

        if (_moduleRegistryProvider.get().isModuleRegistered("multisite")) {
            Site site = ((ExtendedAggregationState) MgnlContext.getAggregationState()).getSite();
            siteName = site.getName();
        }

        return siteName;
    }
}
