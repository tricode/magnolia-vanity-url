package com.aperto.magnolia.vanity.setup;

/*
 * #%L
 * magnolia-vanity-url Magnolia Module
 * %%
 * Copyright (C) 2013 - 2017 Aperto AG
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

import com.aperto.magnolia.vanity.VanityUrlModule;
import com.aperto.magnolia.vanity.VanityUrlService;
import info.magnolia.context.MgnlContext;
import info.magnolia.module.InstallContext;
import info.magnolia.module.delta.Task;
import info.magnolia.module.delta.TaskExecutionException;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import static info.magnolia.jcr.util.PropertyUtil.getString;
import static javax.jcr.query.Query.JCR_SQL2;
import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * Upgrade task that restructures existing vanity nodes in workspace. For each item:
 * <ul>
 * <li>Value of property 'link' is stored into new property 'toPage'</li>
 * <li>Value of property 'link' is changed into 'toPage'</li>
 * </ul>
 *
 * @author mkooijman
 */
final class RestructureExistingVanityNodesTask implements Task {

    @Override
    public String getName() {
        return "Restructure existing Vanity URL's";
    }

    @Override
    public String getDescription() {
        return "Changes link property value now that we support two types of links";
    }

    @Override
    public void execute(final InstallContext installContext) throws TaskExecutionException {
        try {
            final Session jcrSession = MgnlContext.getJCRSession(VanityUrlModule.WORKSPACE);
            final QueryManager queryManager = jcrSession.getWorkspace().getQueryManager();
            final Query query = queryManager.createQuery("select * from [mgnl:vanityUrl]", JCR_SQL2);
            final QueryResult queryResult = query.execute();
            final NodeIterator nodes = queryResult.getNodes();

            if (nodes.hasNext()) {
                final Node node = nodes.nextNode();
                final String originalLink = getString(node, VanityUrlService.PN_LINKTYPE, EMPTY);

                node.setProperty(VanityUrlService.PN_LINKTYPE, VanityUrlService.PN_PAGE);
                node.setProperty(VanityUrlService.PN_PAGE, originalLink);
                jcrSession.save();
            }
        } catch (RepositoryException e) {
            throw new TaskExecutionException(e.getMessage(), e);
        }
    }

}
