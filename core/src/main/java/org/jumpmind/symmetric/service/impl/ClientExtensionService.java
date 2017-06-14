/**
 * Licensed to JumpMind Inc under one or more contributor
 * license agreements.  See the NOTICE file distributed
 * with this work for additional information regarding
 * copyright ownership.  JumpMind Inc licenses this file
 * to you under the GNU General Public License, version 3.0 (GPLv3)
 * (the "License"); you may not use this file except in compliance
 * with the License.
 * <p>
 * You should have received a copy of the GNU General Public License,
 * version 3.0 (GPLv3) along with this library; if not, see
 * <http://www.gnu.org/licenses/>.
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jumpmind.symmetric.service.impl;

import java.util.Map;
import java.util.TreeMap;

import org.jumpmind.extension.IExtensionPoint;
import org.jumpmind.symmetric.ISymmetricEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationContext;

import fr.gael.dhus.server.http.webapp.symmetricDS.SolrReplicationExtensionPoint;
import fr.gael.dhus.spring.context.ApplicationContextProvider;

/**
 * Adding this class to override the one inside the symmetricDS.war.
 * This is done to add our extension {@link SolrReplicationExtensionPoint}
 * done to add products into solr when a modification
 * (Creation, Update or Delete) is replicated by symmetricDS.
 */
public class ClientExtensionService extends ExtensionService
{

   private final static Logger log = LoggerFactory.getLogger(ClientExtensionService.class);

   protected ApplicationContext springContext;

   public ClientExtensionService(ISymmetricEngine engine, ApplicationContext springContext)
   {
      super(engine);
      this.springContext = springContext;
   }

   @Override
   public void refresh()
   {
      super.refresh();
      if (springContext != null)
      {
         Map<String, IExtensionPoint> extensionPointMap = new TreeMap<>();
         extensionPointMap.putAll(springContext.getBeansOfType(IExtensionPoint.class));
         if (springContext.getParentBeanFactory() != null &&
                springContext.getParentBeanFactory() instanceof ListableBeanFactory)
         {
            extensionPointMap.putAll(((ListableBeanFactory) springContext.getParentBeanFactory())
                  .getBeansOfType(IExtensionPoint.class));
         }

         log.info("Found {} extension points from spring that will be registered",
               extensionPointMap.size());
         for (String name: extensionPointMap.keySet())
         {
            registerExtension(name, extensionPointMap.get(name), true);
         }

         // Adding our extension to dynamically add product
         // in solr when a modification is synchronized by symmetricDS
         IExtensionPoint extension = (IExtensionPoint) ApplicationContextProvider.getBean(
               SolrReplicationExtensionPoint.class);
         registerExtension("symds", extension, true);
      }
   }

   public void setSpringContext(ApplicationContext springContext)
   {
      this.springContext = springContext;
   }

}
