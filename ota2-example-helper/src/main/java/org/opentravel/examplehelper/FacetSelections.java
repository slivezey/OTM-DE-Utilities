/**
 * Copyright (C) 2014 OpenTravel Alliance (info@opentravel.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opentravel.examplehelper;

import org.opentravel.schemacompiler.codegen.example.ExampleGeneratorOptions;
import org.opentravel.schemacompiler.model.NamedEntity;
import org.opentravel.schemacompiler.model.TLAlias;
import org.opentravel.schemacompiler.model.TLFacet;
import org.opentravel.schemacompiler.model.TLFacetOwner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

/**
 * Encapsulates the list of all available facet selections for the currently-selected entity in the Example Helper
 * application.
 */
public class FacetSelections {

    private List<EntityFacetSelection> facetSelectionList = new ArrayList<>();
    private Map<QName,EntityFacetSelection> facetSelectionsByEntity = new HashMap<>();
    private FacetSelectionListener listener;

    /**
     * Configures the given <code>ExampleGeneratorOptions</code> with the user's current facet selections.
     * 
     * @param options the EXAMPLE generator options to configure
     */
    public void configureExampleOptions(ExampleGeneratorOptions options) {
        for (EntityFacetSelection facetSelection : facetSelectionList) {
            TLFacetOwner facetOwner = facetSelection.getFacetOwner();

            if (facetOwner != null) {
                TLFacet selectedFacet = facetSelection.getSelectedFacet();

                if (selectedFacet != null) {
                    options.setPreferredFacet( facetOwner, selectedFacet );
                }
            }
        }
    }

    /**
     * Returns the <code>EntityFacetSelection</code> for the given entity type. If no selection for the given entity yet
     * exists, this method will return null.
     * 
     * @param entity the OTM entity type for which to return the facet selections
     * @return EntityFacetSelection
     */
    public EntityFacetSelection getFacetSelection(NamedEntity entity) {
        NamedEntity e = nonAliasedEntity( entity );
        return facetSelectionsByEntity.get( new QName( e.getNamespace(), e.getLocalName() ) );
    }

    /**
     * Adds the given facet selection instance to the current list.
     * 
     * @param facetSelection the facet selection instance to add
     */
    public void addFacetSelection(EntityFacetSelection facetSelection) {
        if ((facetSelection != null) && !facetSelectionList.contains( facetSelection )) {
            NamedEntity otmEntity = nonAliasedEntity( facetSelection.getEntityType() );
            QName entityName = new QName( otmEntity.getNamespace(), otmEntity.getLocalName() );

            if (facetSelectionsByEntity.containsKey( entityName )) {
                throw new IllegalArgumentException( "Facet selections are already defined for entity: " + entityName );
            }
            this.facetSelectionList.add( facetSelection );
            this.facetSelectionsByEntity.put( entityName, facetSelection );
            facetSelection.setOwningFacetSelections( this );
        }
    }

    /**
     * Assigns the facet selection listener for this component.
     * 
     * @param listener the listener to assign (may be null)
     */
    public void setListener(FacetSelectionListener listener) {
        this.listener = listener;
    }

    /**
     * Called when the user has modified their selection for a substitution group facet.
     * 
     * @param updatedSelection the facet selection that was updated
     */
    protected void notifySelectionChanged(EntityFacetSelection updatedSelection) {
        if (listener != null) {
            listener.facetSelectionChanged( updatedSelection );
        }
    }

    /**
     * If the given entity is an alias, this method returns its owner. Otherwise, the original entity is returned.
     * 
     * @param entity the entity to process
     * @return NamedEntity
     */
    private NamedEntity nonAliasedEntity(NamedEntity entity) {
        return (entity instanceof TLAlias) ? ((TLAlias) entity).getOwningEntity() : entity;
    }
}
