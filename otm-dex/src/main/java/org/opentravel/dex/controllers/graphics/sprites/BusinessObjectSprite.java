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

package org.opentravel.dex.controllers.graphics.sprites;

import org.opentravel.model.otmLibraryMembers.OtmBusinessObject;
import org.opentravel.model.otmLibraryMembers.OtmLibraryMember;

import javafx.scene.canvas.GraphicsContext;

/**
 * Graphics Display Object (Sprite) for containing OTM business object.
 * 
 * @author dmh
 * @param <O>
 *
 */
public class BusinessObjectSprite extends MemberSprite<OtmLibraryMember> implements DexSprite<OtmLibraryMember> {
    // private static Log log = LogFactory.getLog( BusinessObjectSprite.class );

    public BusinessObjectSprite(OtmBusinessObject member, SpriteManager manager, GraphicsContext paramsGC) {
        super( member, manager, paramsGC );
    }

    // public List(OtmLibraryMembers) getRelated subTypes() {
    // List
    // for (OtmContributedFacet cf : member.getChildrenContributedFacets()) {
    // if (cf != null && cf.getContributor() != null) {
    // DexSprite<?> s = manager.add( cf.getContributor() );
    // Point2D p = manager.getNextInColumn( this );
    // s.set( p.getX(), p.getY() );
    // ContributedConnection c = new ContributedConnection( s, this );
    // manager.addAndDraw( c );
    // }
    // }
    //
}