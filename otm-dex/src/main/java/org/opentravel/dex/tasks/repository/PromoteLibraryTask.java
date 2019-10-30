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

package org.opentravel.dex.tasks.repository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.controllers.DexIncludedController;
import org.opentravel.dex.controllers.DexStatusController;
import org.opentravel.dex.events.DexRepositoryItemReplacedEvent;
import org.opentravel.dex.tasks.DexTaskBase;
import org.opentravel.dex.tasks.TaskResultHandlerI;
import org.opentravel.model.OtmModelManager;
import org.opentravel.model.otmContainers.OtmLibrary;
import org.opentravel.model.otmContainers.OtmProject;
import org.opentravel.schemacompiler.model.TLLibraryStatus;
import org.opentravel.schemacompiler.repository.ProjectItem;
import org.opentravel.schemacompiler.repository.RepositoryException;
import org.opentravel.schemacompiler.repository.RepositoryItem;
import org.opentravel.schemacompiler.repository.RepositoryItemState;

/**
 * A JavaFX task for Promoting Otm Libraries (Under Review, Final, Obsolete)
 * 
 * @author dmh
 *
 */
public class PromoteLibraryTask extends DexTaskBase<OtmLibrary> {
    private static Log log = LogFactory.getLog( PromoteLibraryTask.class );

    // private DexStatusController statusController;
    private DexIncludedController<?> eventController;
    private OtmProject proj = null;
    private OtmLibrary library = null;
    // private OtmModelManager modelManager;

    /**
     * Create a lock library task.
     * 
     * @param taskData - an repository item to lock.
     * @param handler - results handler
     * @param statusController - status controller that can post message and progress indicator
     * @param eventController - controller to publish repository item replaced event
     * @param modelManager - model manager that holds projects that could contain the library in this repository item
     */
    public PromoteLibraryTask(OtmLibrary taskData, TaskResultHandlerI handler, DexStatusController statusController,
        DexIncludedController<?> eventController, OtmModelManager modelManager) {
        super( taskData, handler, statusController );
        if (taskData == null)
            return;

        this.library = taskData;
        this.eventController = eventController;
        // this.statusController = statusController;
        // this.modelManager = modelManager;

        // Try to find the actual modeled library. A modeled library will be created by opening a project.
        // library = modelManager.get(taskData.getNamespace() + "/" + taskData.getLibraryName());
        // See if there is an open project to manage this item and use it
        // if (library != null) {
        proj = modelManager.getManagingProject( library );
        // }

        // Replace start message from super-type.
        msgBuilder = new StringBuilder( "Locking: " );
        msgBuilder.append( library.getName() );
        updateMessage( msgBuilder.toString() );
    }

    public static boolean isEnabled(OtmLibrary lib, TLLibraryStatus targetStatus) {
        // if (lib.isEditable())
        // return false;

        // Check state and status
        if (targetStatus == TLLibraryStatus.OBSOLETE) {
            if (lib.getState() != RepositoryItemState.MANAGED_WIP || lib.getStatus() != TLLibraryStatus.DRAFT)
                return false;
        } else if (lib.getState() != RepositoryItemState.MANAGED_UNLOCKED)
            return false;

        ProjectItem pi = null;
        OtmProject project = lib.getManagingProject();
        for (ProjectItem candidate : project.getTL().getProjectItems())
            if (lib.getProjectItems().contains( candidate ))
                pi = candidate;
        if (!(pi instanceof RepositoryItem))
            return false;

        if (pi.getStatus().nextStatus() != targetStatus)
            return false;

        return true;
    }

    @Override
    public void doIT() throws RepositoryException {
        log.debug( "Promote library task: " + library );

        if (proj != null) {
            log.debug( "Promote with project item: " + proj.getProjectItem( library.getTL() ).hashCode() );
            proj.getTL().getProjectManager().promote( proj.getProjectItem( library.getTL() ) );
        }
        // throwRepoItemReplacedEvent( oldItem, newItem );
    }

    /**
     * Inform application that a repository item has changed. May be needed when locking an item since the items are
     * held in other controller's DAOs.
     * 
     * @param oldItem
     * @param newItem
     */
    private void throwRepoItemReplacedEvent(RepositoryItem oldItem, RepositoryItem newItem) {
        eventController.publishEvent( new DexRepositoryItemReplacedEvent( this, oldItem, newItem ) );
    }

}
