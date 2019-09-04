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

package org.opentravel.dex.actions;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opentravel.dex.events.DexChangeEvent;
import org.opentravel.model.OtmObject;

public abstract class DexActionBase {
    private static Log log = LogFactory.getLog( DexActionBase.class );

    protected DexActions action = null; // Which enumeration does this action implement

    protected OtmObject otm;
    // protected DexActionManagerCore coreActionManager = null;
    protected boolean ignore;
    // protected ChangeListener<?> changeListner;

    // public ChangeListener<?> getChangeListner() {
    // return changeListner;
    // }
    //
    // public void setChangeListener(ChangeListener<?> changeListner) {
    // // this.changeListner = changeListner;
    // }

    public void removeChangeListener() {
        // this.changeListner = changeListner;
    }


    public DexActionBase() {}

    public DexActionBase(OtmObject otm) {
        // if (otm.getActionManager() instanceof DexActionManagerCore)
        // coreActionManager = (DexActionManagerCore) otm.getActionManager();

        // assert (coreActionManager != null);
        this.otm = otm;
    }

    public DexActions getType() {
        return action;
    }

    // /**
    // * Must be set before running. Needed because actions are created using reflection.
    // */
    // public void setCoreActionManager(DexActionManagerCore coreActionManger) {
    // this.coreActionManager = coreActionManger;
    // }
    //
    public OtmObject getSubject() {
        return otm;
    }

    // /**
    // * Set the subject for this action to act upon.
    // * @param subject
    // * @return false if the subject could not be set
    // */
    // public abstract boolean setSubject(OtmObject subject);

    public DexChangeEvent getEvent() {
        DexChangeEvent event = null;
        try {
            event = DexActions.getEvent( action );
        } catch (ExceptionInInitializerError | InstantiationException | IllegalAccessException e) {
            log.warn( "Failed to get event handler: " + e.getLocalizedMessage() );
            return null;
        }
        if (event != null)
            event.set( otm );
        return event;
    }


}
