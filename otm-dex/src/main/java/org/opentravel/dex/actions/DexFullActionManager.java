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
import org.opentravel.dex.controllers.DexMainController;
import org.opentravel.model.OtmObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Default action manager used by OTM elements to determine what actions are available and adding listeners to
 * observable values (properties) to execute the action.
 * <p>
 * To create a StringProperty with associated un-doable action use
 * {@link #add(DexActionManager.DexActions, CurrentValue, OtmObject)} when creating an observable property.
 * <p>
 * Extends {@link DexActionManagerBase} which controls and manages actions; maintains queue of actions and notifies user
 * of performed action status.
 * 
 * @author dmh
 *
 */
public class DexFullActionManager extends DexActionManagerBase {
    private static Log log = LogFactory.getLog( DexFullActionManager.class );

    public DexFullActionManager(DexMainController mainController) {
        super( mainController );
    }

    /**
     * Use reflection on the action to get the action handler's isEnabled method and return its result.
     * <p>
     * Note: this could be static but do NOT move to DexActions because there are multiple action managers.
     */
    @Override
    public boolean isEnabled(DexActions action, OtmObject subject) {
        boolean result = false;
        if (subject.getOwningMember().isEditable())
            try {
                Method m = action.actionClass().getMethod( "isEnabled", OtmObject.class );
                result = (boolean) m.invoke( null, subject );
                // log.debug( "Method " + action.toString() + " isEnabled invoke result: " + result );
            } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
                log.error( "Could not invoke action.isEnabled( ):" + e.getMessage() );
            }
        return result;
    }

}
