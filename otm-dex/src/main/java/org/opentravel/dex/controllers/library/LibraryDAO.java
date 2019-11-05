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

package org.opentravel.dex.controllers.library;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.common.ImageManager;
import org.opentravel.dex.controllers.DexDAO;
import org.opentravel.model.otmContainers.OtmLibrary;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;

/**
 * The type of the TreeItem instances used in this TreeTableView. Simple Data Access Object that contains and provides
 * gui access to OTM model library members.
 *
 * @author dmh
 *
 * 
 * @author dmh
 *
 */
public class LibraryDAO implements DexDAO<OtmLibrary> {
    private static Log log = LogFactory.getLog( LibraryDAO.class );

    protected OtmLibrary library;
    String editable = "False";

    public LibraryDAO(OtmLibrary library) {
        this.library = library;
        if (library == null)
            throw new IllegalArgumentException( "No library provided to Project-Library DAO" );
    }

    public StringProperty editProperty() {
        editable = "False";
        if (library.isEditable())
            editable = "True";
        return new SimpleStringProperty( editable );
    }

    @Override
    public ImageView getIcon(ImageManager imageMgr) {
        return ImageManager.get( library.getIconType() );
    }

    @Override
    public OtmLibrary getValue() {
        return library;
    }

    public StringProperty nameProperty() {
        StringProperty np = new SimpleStringProperty( library.getName() );
        np.addListener( (o, v, n) -> {
            log.debug( "Name change request: " + n );
            library.getTL().setName( n );
        } );
        return np;
    }

    // Question - Why use ReadOnly when it doesn't seem to matter if the property is readonly or not.
    // Must be tested in controller.
    public StringProperty namespaceProperty() {
        StringProperty sp;
        if (library.isEditable() && library.isUnmanaged())
            sp = new SimpleStringProperty( library.getTL().getNamespace() );
        else
            sp = new ReadOnlyStringWrapper( library.getTL().getNamespace() );
        sp.addListener( (o, v, n) -> {
            log.debug( "Namespace change request: " + n );
            library.getTL().setNamespace( n );
        } );
        return sp;
    }

    public StringProperty prefixProperty() {
        return new SimpleStringProperty( library.getPrefix() );
    }

    public StringProperty stateProperty() {
        return new SimpleStringProperty( library.getState().toString() );
    }

    public IntegerProperty referenceProperty() {
        return new SimpleIntegerProperty( library.getTL().getReferenceCount() );
    }

    public StringProperty statusProperty() {
        return new SimpleStringProperty( library.getStatus().toString() );
    }

    public StringProperty lockedProperty() {
        return new SimpleStringProperty( library.getLockedBy() );
    }

    public StringProperty projectsProperty() {
        String projects = "";
        for (String name : library.getProjectNames())
            if (projects.isEmpty())
                projects = name;
            else
                projects = projects + ", " + name;
        return new SimpleStringProperty( projects );
    }

    public BooleanProperty readonlyProperty() {
        return new SimpleBooleanProperty( library.getTL().isReadOnly() );
    }

    @Override
    public String toString() {
        return library.toString();
    }

    public StringProperty versionProperty() {
        return new SimpleStringProperty( library.getTL().getVersion() );
    }

    /**
     * TreeItem class does not extend the Node class.
     * 
     * Therefore, you cannot apply any visual effects or add menus to the tree items. Use the cell factory mechanism to
     * overcome this obstacle and define as much custom behavior for the tree items as your application requires.
     * 
     * @param item
     * @return
     */
    public TreeItem<LibraryDAO> createTreeItem(TreeItem<LibraryDAO> parent) {
        // log.debug( "Create tree item for: " + library );
        if (parent != null) {
            TreeItem<LibraryDAO> item = new TreeItem<>( this );
            item.setExpanded( false );
            parent.getChildren().add( item );
            return item;
        }
        return null;
    }

}
